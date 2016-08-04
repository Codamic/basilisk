(ns basilisk.logger.core
  "Logger abstraction namespace. We don't use components
  for the logger because besically it's not necessary."
  (:require [taoensso.timbre :as timbre]
            [clojure.tools.logging :as log]
            [basilisk.logger.kafka :as kafka]
            [environ.core :refer [env]]
            [basilisk.logger.kafka :refer [kafka-appender]]
            [com.stuartsierra.component :as component]))


(def default-config
  {:min-level :debug
   :appenders {:stdout {:min-level :debug,
                        :output-fn  (fn [data] "NIL")
                        :fn         (fn [data] "-")}}})
(def config (atom default-config))

(defn log
  [level str]
  (doseq [[name appender-config] (:appenders @config)]
    (let [min-level (or (:min-level appender-config)
                       (:min-level @config))]
      (if (>= (level-value level) (level-value min-level))
        (process-log level str appender-config)))))

(defn info [str]
  (log/info str))

(log :debug "xzczxczxc")
