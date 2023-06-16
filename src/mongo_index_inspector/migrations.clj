(ns mongo-index-inspector.migrations
  (:require [config.core :refer [env]]
            [migratus.core :as migratus]))

(def config {:store :database
             :db {:connection-uri (:jdbc-url env)}})

(defn migrate! [& _]
  (migratus/migrate config))

(defn rollback! []
  (migratus/rollback config))
