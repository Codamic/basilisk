(ns basilisk.logger.kafka)


(defn formatter
  [data]
  (println "formatter")
  {:some 123 :kei 453})


(defn appender
  [{:keys [output_]} configs]
  (println "appender")
  (println output_))
