(ns user
  (:require [clojure.java.browse :refer [browse-url]]
            [config.core :refer [env reload-env]]
            [integrant-repl-autoreload.core :refer [start-auto-reset
                                                    stop-auto-reset]]
            [integrant.repl :refer [go halt reset set-prep!]]
            [mongo-index-inspector.core :refer [system-config]]
            [mongo-index-inspector.domain :as domain]
            [mongo-index-inspector.migrations :as migrations]
            [migratus.core :as migratus]))

(set-prep! (constantly system-config))

(comment
  (go)
  (browse-url "http://localhost:3000/")
  (reset)
  (halt)
  (start-auto-reset {:relevant-file? (constantly true)})
  (stop-auto-reset))

(comment
  (reload-env))

(comment
  (migrations/migrate!)
  (migrations/rollback!)
  (migratus/create nil "indexes"))

(comment
  (domain/get-all-indexes (:jdbc-url env))
  (domain/get-all-environments (:jdbc-url env))
  (domain/create-environment! (:jdbc-url env) "dev" "mongodb://localhost:27017"))
