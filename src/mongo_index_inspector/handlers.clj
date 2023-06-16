(ns mongo-index-inspector.handlers 
  (:require [hiccup.core :refer [h]]
            [hiccup.form :as form]
            [hiccup.page :as hp]
            [mongo-index-inspector.domain :as domain]
            [mongo-index-inspector.logging :as logging]
            [ring.util.response :as response]))

(defmacro compiled-at [] (System/currentTimeMillis))

(defn page [& content]
  {:status 200
   :headers {"Content-type" "text/html"}
   :body
   (hp/html5
    {:lang "en"}
    [:head
     [:meta {:charset "utf-8"}]
     [:meta {:name "viewport"
             :content "width=device-width,initial-scale=1"}]
     [:title "MongoDB Index Inspector"]
     (hp/include-css "/css/reset.css")
     (hp/include-css (str "/css/screen.css?version=" (compiled-at)))]
    [:body content])})

(defn confirmation-button [text]
  #_{:clj-kondo/ignore [:invalid-arity]}
  (form/submit-button {:class "button" :name :ok} text))

(defn primary-button-link [url text]
  [:a {:class "button" :href url} text])

(defn secondary-button-link [url text]
  [:a {:class "secondary-button" :href url} text])

(defn render-environment-overview-page [{:keys [datasource]} _]
  (let [environments (domain/get-all-environments datasource)]
    (page
     [:h1 "Environments"]
     (when (seq environments)
       [:table
        [:tr
         [:th "Name"]
         [:th "Indexes fetched on"]
         [:th {:colspan 2} "Actions"]]
        (for [{:keys [environment-id name uri indexes-updated-at]} environments]
          [:tr
           [:td {:title uri} name]
           [:td (or indexes-updated-at "-")]
           [:td (secondary-button-link (str "/environments/" environment-id "/collect-indexes") "Collect indexes")]
           [:td (secondary-button-link (str "/environments/" environment-id "/remove") "Remove from overview")]])])
     [:div {:class "horizontal-buttons"}
      (primary-button-link "/environments/add" "Add environment")
      (when (seq environments) (primary-button-link "/indexes" "Compare indexes"))])))

(defn render-create-environment-page [_ _]
  (page
   [:h1 "Add environment"]
   (form/form-to
    [:post "/environments/add"]
    [:label "Name"
     (form/text-field {:required true} :name)]
    [:label "URI"
     (form/text-field {:required true} :uri)]
    (confirmation-button "Add environment"))))

(defn create-environment [{:keys [datasource]} request]
  (let [{:keys [name uri]} (:params request)]
    (domain/create-environment! datasource name uri)
    (response/redirect (str "/") :see-other)))

(defn cancellation-button [url text]
  (secondary-button-link url text))

(defn render-delete-environment-page [{:keys [datasource]} request]
  (let [{:keys [id]} (:path-params request)
        {:keys [name]} (domain/get-environment datasource id)]
    (page
     [:h1 "Remove " (h name) " from overview"]
     [:p
      "Are you sure you want to remove " (h name) " from the overview of environments?"]
     (form/form-to
      [:post (str "/environments/" id "/remove")]
      [:div {:class "horizontal-buttons"}
       (cancellation-button "/" "No, go back")
       (confirmation-button "Yes, remove")]))))

(defn delete-environment [{:keys [datasource]} request]
  (let [{:keys [id]} (:path-params request)
        {:keys [ok]} (:params request)]
    (when ok
      (domain/delete-environment! datasource id))
    (response/redirect "/" :see-other)))

(defn collect-indexes [{:keys [datasource logger mongo-client]} request]
  (let [{:keys [id]} (:path-params request)]
    (try
      (domain/collect-indexes! datasource mongo-client id) 
      (response/redirect "/" :see-other)
      (catch Exception e
        (logging/log-error! logger "Unable to collect indexes" e)
        (response/redirect (str "/environments/" id "/collect-indexes/error") )))))

(defn render-unable-to-collect-indexes-page [{:keys [datasource]} request]
  (let [{:keys [id]} (:path-params request)
        {:keys [name uri]}(domain/get-environment datasource id)]
    (page
     [:h1 "Unable to collect indexes"]
     [:p
      "The indexes for environment " [:span {:title uri} (h name)] " could not be collected."]
     [:a {:href "/"} "Back to overview of environments"])))

(defn render-index-overview-page [{:keys [datasource]} _]
  (let [indexes (domain/get-all-indexes datasource)
        number-of-environments (count indexes)
        partitioned-indexes (partition-by domain/extract-index (sort domain/index-comparator (domain/flatten-indexes indexes)))]
    (page
     [:h1 "Overview of indexes"]
     [:table
      [:tr
       [:th "Database"]
       [:th "Collection"]
       [:th "Name"]
       [:th "Environment"]
       [:th "Key"]
       [:th "Expire after seconds"]
       [:th "Hidden"]
       [:th "Partial-filter expression"]
       [:th "Sparse"]
       [:th "Unique"]]
      (for [partition partitioned-indexes
            :let [matching? (= number-of-environments (count partition))]]
        (for [{:keys [database collection key expire-after-seconds hidden partial-filter-expression sparse unique name environment]} partition]
          [:tr {:class (when matching? "matching-index")}
           [:td database]
           [:td collection]
           [:td name]
           [:td environment]
           [:td (str key)]
           [:td (or expire-after-seconds "-")]
           [:td hidden]
           [:td (str (or partial-filter-expression "-"))]
           [:td sparse]
           [:td unique]]))])))

(defn render-domain-exception-page [exception]
  (let [{:keys [ui-message]} (ex-data exception)]
    (page
     [:h1 "Something went wrong!"]
     [:p ui-message])))

(def internal-server-error-page
  (page
   [:h1 "Unexpected error"]
   [:p
    "An unexpected error occurred."]))

(defn handle-exception [exception logger]
  (if (and
       (instance? clojure.lang.ExceptionInfo exception)
       (:ui-message (ex-data exception)))
    (render-domain-exception-page exception)
    (do
      (logging/log-error! logger "Unexpected exception" exception)
      internal-server-error-page)))
