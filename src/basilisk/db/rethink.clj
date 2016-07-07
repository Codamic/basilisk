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


(defn create-table
  "Create a table with given name and specs"
  ([name]
  (-> (r/table-create name)
      (r/run @connection)))

  ([name spec-map]
  (let [index-name (get spec-map :index-name)
        index-fn   (get spec-map :index-fn (fn [row] (r/get-field row index-name)))]
    (-> (r/table-create name)
        (when (not (nil? index-name))
          (r/index-create index-name index-fn))
        (r/run @connection)))))

(defn disconnect
  "Disconnect from rethink cluster"
  []
  (r/close @connection)
  (reset! connection nil))
