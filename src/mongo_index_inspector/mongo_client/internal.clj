(ns mongo-index-inspector.mongo-client.internal
  (:require [monger.collection :refer [indexes-on system-collection?]]
            [monger.core :as m]
            [monger.db :refer [get-collection-names]])
  (:import [com.mongodb MongoClient MongoClientURI MongoCommandException]))

(defn create-mongo-client! [uri-string]
  (MongoClient. (MongoClientURI. uri-string)))

(defn disconnect! [client]
  (m/disconnect client))

(defn get-database-names [client]
  (disj (m/get-db-names client) "admin" "config" "local"))

(defn get-indexes-for-collection [database collection-name]
  (try
    (indexes-on database collection-name)
    (catch MongoCommandException _ [])))

(defn get-indexes-for-database [client database-name]
  (let [database (m/get-db client database-name)
        collection-names (get-collection-names database)]
    (into {} (for [collection-name collection-names]
               (when-not (system-collection? collection-name)
                 [collection-name (get-indexes-for-collection database collection-name)])))))

(defn get-indexes [client]
  (let [database-names (get-database-names client)]
    (into {} (for [database-name database-names]
               [database-name (get-indexes-for-database client database-name)]))))
