(ns basilisk.logger.core
  (:require [taoensso.timbre :as timbre]
            [environ.core :refer [env]]
            [com.stuartsierra.component :as component]))

(defn info [str]
  (log/info str))

(defn debug [str]
  (log/debug str))

(defn error [str]
  (log/error str))

(defn fatal [str]
  (log/fatal str))

(defn warn [str]
  (log/warn str))

(defn trace [str]
  (log/trace str))

(defn log [type str]
  (log/log type str))

(defn infof [str & rest]
  (log/infof str rest))


(defrecord Logger []
  ;; Implements component/Lifecycle protocol
  component/Lifecycle
  (start [this]
    this)

  (stop [this]
    this))
