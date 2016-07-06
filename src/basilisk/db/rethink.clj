(ns basilisk.db.rethink
  (:require [rethinkdb.query :as r]
            [environ.core :refer [env]]
            [basilisk.logger :as logger]))

(defn- host [] (env :rethink-host))
(defn- port [] (Integer/parseInt (env :rethink-port)))
(defn- db   [] (env :rethink-db))

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


(defn disconnect
  "Disconnect from rethink cluster"
  []
  (r/close @connection)
  (reset! connection nil))
