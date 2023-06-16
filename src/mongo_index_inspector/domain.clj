(ns mongo-index-inspector.domain 
  (:require [clojure.edn :as edn]
            [mongo-index-inspector.db :as db]
            [mongo-index-inspector.mongo-client.core :as mongo-client]))

(defn get-all-environments [datasource]
  (db/get-all-environments datasource))

(defn create-environment! [datasource name uri]
  (db/create-environment! datasource {:name name
                                   :uri uri}))

(defn get-environment [datasource id]
  (if-let [environment (db/get-environment datasource {:id id})]
    environment
    (throw (ex-info "Environment not found" {:ui-message "This environment doesn't exist."}))))

(defn delete-environment! [datasource id]
  (db/delete-environment! datasource {:id id}))

(defn collect-indexes! [datasource mongo-client id]
  (let [{:keys [uri]} (db/get-environment datasource {:id id})
        indexes (mongo-client/get-indexes mongo-client uri)]
    (db/update-indexes! datasource {:id id
                                    :indexes indexes
                                    :indexes-updated-at (str (java.time.Instant/now))})))

(defn get-all-indexes [datasource]
  (map (fn [{:keys [environment-id name indexes]}] {:environment-id environment-id
                                                    :name name
                                                    :indexes (edn/read-string indexes)})
       (db/get-all-indexes datasource)))

(defn flatten-indexes [indexes]
  (for [indexes-for-environment indexes
        :let [environment-name (:name indexes-for-environment)
              indexes-for-dbs (:indexes indexes-for-environment)]
        [database-name indexes-for-db] indexes-for-dbs
        [collection-name indexes-for-collection] indexes-for-db
        index-for-collection indexes-for-collection
        :let [{:keys [key name sparse unique hidden expireAfterSeconds partialFilterExpression]} index-for-collection]]
    {:database database-name
     :collection collection-name
     :key key
     :expire-after-seconds expireAfterSeconds
     :hidden (true? hidden)
     :partial-filter-expression partialFilterExpression
     :sparse (true? sparse)
     :unique (true? unique) 
     :name name
     :environment environment-name}))

(defn extract-index [{:keys [collection key expire-after-seconds hidden partial-filter-expression sparse unique]}]
  [collection key expire-after-seconds hidden partial-filter-expression sparse unique])

(defn index-map-to-vector [{:keys [database collection key expire-after-seconds hidden partial-filter-expression sparse unique environment]}]
[database collection (str key) expire-after-seconds hidden (str partial-filter-expression) sparse unique environment])

(defn index-comparator [x y] (compare (index-map-to-vector x) (index-map-to-vector y)))
