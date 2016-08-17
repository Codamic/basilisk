(ns basilisk.logger.appenders.kafka
  "Kafka appender for logger system. This appender will store
  logs in a Kafka topic. Each executable unit will has its ouwn
  partition."
  (:require [basilisk.encoders.transit :as transit]
            [kafka-clj.client :as kafka]))


(defn output-fn
  [data]
  data)


(defn appender
  [{:keys [output_]} configs]

  (println output_)
  ())
