(ns basilisk.logger)


(defn debug
  [msg]
  (if (nil? msg)
    (println (str "[DEBUG]: nil"))
    (println (str "[DEBUG]: " msg))))

(defn info
  [msg]
  (println (str "[INFO]: " msg)))
