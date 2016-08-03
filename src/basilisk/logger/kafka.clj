(ns basilisk.logger.kafka)

(defn formatter
  [config]
  (println "formatter")
  (println config)
  {:k1 1 :k2 2})

(defn appender
  [data]
  (println "appender")
  (println ((:output-fn data) data)))


(defn kafka-appender
  []
  {:enabled?  true
   :async?    false
   :output-fn formatter
   :fn        appender})
