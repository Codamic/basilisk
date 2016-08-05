(ns basilisk.logger.appenders.kafka
  "Kafka appender for logger system. This appender will store
  logs in a Kafka topic. Each executable unit will has its ouwn
  partition."
  )


(defn output-fn
  [data]
  data )


(defn appender
  [{:keys [output_]} configs]
  (println "appender")
  (println output_))
