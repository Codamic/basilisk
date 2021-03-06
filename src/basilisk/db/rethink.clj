(ns basilisk.db.rethink
  (:require [rethinkdb.query :as r]
            [environ.core :refer [env]]
            [schema.core :as s]
            [basilisk.logger :as logger]))

(defn- host [] (env :rethink-host))
(defn- port [] (Integer/parseInt (env :rethink-port)))
(defn- db   [] (env :rethink-db))

;; Connection atom contains the connection object
(def connection (atom nil))

;; Private API

(defn- connected?
  "Check the connectivity to database"
  []
  (not (nil? @connection)))

(defn- _connect
  "Private connect function"
  []
  (logger/debug (host))
  (logger/debug (port))
  (logger/debug (db))
  (let [conn (r/connect :host (host)
                        :port (port)
                        :db   (db))]
    (swap! connection (fn [_] conn))))


;; Public API
(defn connect
  "Connect ro rethink cluster"
  []
  (when (not (connected?))
    (_connect)))

(defn initialize
  "Initialize the database by creating it and its data structures."
  []
  (connect)
  (r/run (r/db-create (db)) @connection))

(defn create-table
  "Create a table with given name and specs"
  ([name]
   (connect)
   ;; Query execution
   (-> (r/db (db))
       (r/table-create name)
       (r/run @connection)))

  ([name spec-map]
   (connect)
   (let [index-name (get spec-map :index-name)
         index-fn   (get spec-map :index-fn (fn [row] (r/get-field row index-name)))]
     ;; Query execution
     (-> (r/db (db))
         (r/table-create name)
         (when (not (nil? index-name))
           (r/index-create index-name index-fn))
         (r/run @connection)))))

(defn disconnect
  "Disconnect from rethink cluster"
  []
  (when (connected?)
    (logger/debug "Disconnected from rethink")
    (r/close @connection)
    (reset! connection nil)))
