(ns basilisk.coordination.utils
  (:require [zookeeper :as zk]))

(defn node-data
  "retrive the data of the given znode"
  [coordinator node]
  (let [connection (:connection coordinator)]
    (String. (:data (zk/data connection node)) "UTF8")))
