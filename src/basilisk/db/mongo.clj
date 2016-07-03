(ns basilisk.db.mongo
  (:require [monger.core :as mg]
            [environ.core :refer [env]])
  (:import [org.bson.types ObjectId]))


(defn- mongodb-opt
  []
  (env :mongo))

(defn- mongodb-host
  []
  (:host mongodb-opt))

(defn- mongodb-port
  []
  (:port mongodb-opt))

(defn- db-name
  []
  (:db-name mongodb-opt))

(def db (atom {}))

(defn connect
  "Connect to mongodb cluster."
  []
  (let [conn (mg/connect {:host (mongodb-host) :port (mongodb-port)})]
    (swap! db #({:connection conn,
                 :db (mg/get-db conn db-name)}))))


(defn insert
  "Insert new documne into the given collection"
  [coll doc]
  (let [id (ObjectId.)
        doc-with-id {:_id id}]
    (if (contains? doc :_id)
      (mg/insert (:db @db) coll doc)
      (mg/insert (:db @db) coll (merge doc doc-with-id)))))

(defn disconnect
  "Disconnect from mongodb cluster."
  []
  (mg/disconnect (:connection @db)))
