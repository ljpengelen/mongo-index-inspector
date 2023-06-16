(ns mongo-index-inspector.db
  (:require [hugsql.adapter.next-jdbc :as next-adapter]
            [hugsql.core :as hugsql]))

(hugsql/set-adapter! (next-adapter/hugsql-adapter-next-jdbc))

(defn def-db-fns []
  (binding [*ns* (find-ns 'mongo-index-inspector.db)]
    (hugsql/def-db-fns "mongo_index_inspector/db.sql")))

(declare get-all-environments create-environment! get-environment delete-environment!
         update-indexes! get-all-indexes)
