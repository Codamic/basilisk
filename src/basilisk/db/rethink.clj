(ns basilisk.db.rethink
  (:require [rethinkdb.query :as r]
            [environ.core :refer [env]]
            [schema.core :as s]
            [basilisk.logger :as logger])

(defn- host [] (env :rethink-host))
(defn- port [] (Integer/parseInt (env :rethink-port)))
(defn- db   [] (env :rethink-db))

;; Connection atom contains the connection object
(def connection (atom nil))

(defn connect
  "Connect ro rethink cluster"
  []
  (logger/debug (host))
  (logger/debug (port))
  (logger/debug (db))
  (let [conn (r/connect :host (host)
                        :port (port)
                        :db   (db))]
    (swap! connection (fn [_] conn))))


(s/defn create-table
  "Create a table with given name and specs"
  [name :- s/Str]
  (-> (r/table-create name)
      (r/run @connection))

  [name :- s/Str
   spec :- java.util.Map]
  (let [index-fn (get spec :index-fn (fn [row] (r/get-field row :genre)))
        index-name (get spec :index-name)]
    (-> (r/table-create name)
        (when (not (nil? index-name))
          (r/index-create index-name index-fn))
        (r/run @connection)))

(defn disconnect
  "Disconnect from rethink cluster"
  []
  (r/close @connection)
  (reset! connection nil))
