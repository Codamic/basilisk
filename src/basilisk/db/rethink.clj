(ns basilisk.db.rethink
  (:require [rethinkdb.query :as r]
            [environ.core :refer [env]]))

(defn- host [] (:host (env :rethink)))
(defn- port [] (:port (env :rethink)))
(defn- db   [] (:db   (env :rethink)))

(def connection (atom nil))

(defn connect
  "Connect ro rethink cluster"
  []
  (let [conn (r/connect :host (host)
                        :port (port)
                        :db   (db))]
    (swap! connection (fn [_] conn))))


(defn disconnect
  "Disconnect from rethink cluster"
  []
  (r/close @connection)
  (reset! connection nil))
