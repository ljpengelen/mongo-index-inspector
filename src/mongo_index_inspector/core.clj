(ns mongo-index-inspector.core
  (:require [clojure.java.browse :refer [browse-url]]
            [config.core :refer [env]]
            [integrant.core :as ig]
            [mongo-index-inspector.db :as db]
            [mongo-index-inspector.logging :as logging]
            [mongo-index-inspector.migrations :as migrations]
            [mongo-index-inspector.mongo-client.core :as mongo-client]
            [mongo-index-inspector.routes :refer [app]]
            [org.httpkit.server :as http-kit])
  (:gen-class))

(def system-config
  {::datasource {:jdbc-url (:jdbc-url env)}
   ::db-fns nil
   ::handler {:datasource (ig/ref ::datasource)
              :logger (ig/ref ::logger)
              :mongo-client (ig/ref ::mongo-client)}
   ::logger nil
   ::mongo-client nil
   ::server {:handler (ig/ref ::handler)
             :logger (ig/ref ::logger)
             :port (:port env)}
   ::uncaught-exception-handler {:logger (ig/ref ::logger)}})

(defmethod ig/init-key ::db-fns [_ _]
  (db/def-db-fns))

(defmethod ig/init-key ::datasource [_ {:keys [jdbc-url]}]
  jdbc-url)

(defmethod ig/init-key ::logger [_ _]
  (logging/create-logger))

(defmethod ig/init-key ::mongo-client [_ _]
  (let [client (mongo-client/create-mongo-client!)]
    client))

(defmethod ig/init-key ::handler [_ state]
  (app state))

(defn error-logger [logger]
  (fn [message exception]
    (logging/log-error! logger message exception)))

(defn warn-logger [logger]
  (fn [message exception]
    (logging/log-warning! logger message exception)))

(defmethod ig/init-key ::server [_ {:keys [handler logger port]}]
  (let [options {:error-logger (error-logger logger)
                 :warn-logger (warn-logger logger)
                 :port port}]
    (http-kit/run-server handler options)))

(defmethod ig/halt-key! ::server [_ server]
  (server))

(defmethod ig/init-key ::uncaught-exception-handler [_ {:keys [logger]}]
  (Thread/setDefaultUncaughtExceptionHandler
   (logging/uncaught-exception-handler logger)))

(defn -main [& _]
  (migrations/migrate!)
  (ig/init system-config)
  (browse-url "http://localhost:3000/"))
