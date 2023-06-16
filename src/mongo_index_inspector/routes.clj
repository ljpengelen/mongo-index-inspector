(ns mongo-index-inspector.routes
  (:require [mongo-index-inspector.handlers :as h]
            [reitit.ring :as ring]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.params :refer [wrap-params]]))

(defn wrap-pretty-exceptions [handler logger]
  (fn [request]
    (try
      (handler request)
      (catch Exception e
        (h/handle-exception e logger)))))

(defn no-caching-response [response]
  (assoc-in response [:headers "Cache-Control"] "no-cache, no-store"))

(defn wrap-no-caching [handler]
  (fn [request]
    (no-caching-response (handler request))))

(defn wrap-state [handler state]
  (fn [request]
    (handler state request)))

(defn app [{:keys [logger] :as state}]
  (ring/ring-handler
   (ring/router
    [["/" {:get h/render-environment-overview-page}]
     ["/environments"
      ["/add" {:get h/render-create-environment-page
               :post h/create-environment}]
      ["/:id"
       ["/collect-indexes"
        ["" {:get h/collect-indexes}]
        ["/error" {:get h/render-unable-to-collect-indexes-page}]]
       ["/remove" {:get h/render-delete-environment-page
                   :post h/delete-environment}]]]
     ["/indexes" {:get h/render-index-overview-page}]]
    {:data {:middleware [[wrap-pretty-exceptions logger]
                         wrap-params
                         wrap-keyword-params
                         wrap-no-caching
                         [wrap-state state]]}})
   (ring/routes
    (ring/create-resource-handler
     {:path "/"})
    (ring/create-default-handler
     {:not-found (constantly {:status 404 :body "Not found"})}))))
