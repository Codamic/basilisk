(ns basilisk.logger.core
  "Logger abstraction namespace. We don't use components
  for the logger because besically it's not necessary."
  (:require [taoensso.timbre :as timbre]
            [clojure.tools.logging :as log]
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


(defn initialize
  "Initialize logger configuration.
  Params:
    * instance-name: The name of current running instance of Basalisk.
    * zookeeper: An instance of zookeeper component.
    * config:    A hashmap of timbre configuration you want to change."
  [instance-name zookeeper config]
  nil)
