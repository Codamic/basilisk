(ns mar.sunny.roles
  (:require [clojure.tools.logging :as log]
            [mary.sunny.core :as sunny]))


(defmacro as-leader-do
  "Run given code if this node was the cluster leader"
  [& body]
  `(when (sunny/am-i-leader?) ~@body))

(defmacro as-crew-do
  "Run given code if this node was not the cluster leader"
  [& body]
  `(when-not (sunny/am-i-leader?) ~@body))
