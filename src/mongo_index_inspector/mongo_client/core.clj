(ns mongo-index-inspector.mongo-client.core
  (:require [mongo-index-inspector.mongo-client.internal :as internal]))

(defprotocol MongoClient
  (get-indexes [this uri-string]))

(defn create-mongo-client! []
  (reify MongoClient
    (get-indexes
     [_ uri-string]
     (let [client (internal/create-mongo-client! uri-string)
           indexes (internal/get-indexes client)]
       (internal/disconnect! client)
       indexes))))
