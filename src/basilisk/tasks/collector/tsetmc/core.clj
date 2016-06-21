(ns basilisk.tasks.collector.tsetmc.core
  (:require [clojure.core.async :as async]
            [clojure-csv.core :as csv]
            [clj-http.client :as http]))

(def site-url {:fa {:index-csv "http://www.tsetmc.com/tsev2/data/MarketWatchInit.aspx?h=0&r=0"}})

(def input-channel (async/chan 1000))

(defrecord TradeSymbol [symbol name tsetmc-id tsetmc-name])


(defn fetch-csv
  "Fetch the CSV file from remote host and add each line to a channel"
  [output-channel]
  (async/go
    (let [buffer (:body (http/get (:index-csv (:fa site-url))))
          lines (clojure.string/split buffer  #";")]
      (map #(async/>! output-channel %) lines))))


(defn create-records
  "Fetch each line of the CSV from the input-channel and create a record then push it
  to the output channel."
  [input-channel output-channel]
  (async/go-loop []
    (when-let [line (async/<! input-channel)]
      (let [fields (clojure.string/split line #",")]
        (if (= 23 (count fields))
          (async/>! output-channel
                    (map->TradeSymbol
                     {:symbol      (get fields 2)
                      :name        (get fields 3)
                      :tsetmc-id   (get fields 0)
                      :tsetmc-name (get fields 1)}))))
      (recur))))

(defn with-each-symbol []
  (let [lines (take 5 (fetch-csv))]
    (map create-records lines)))

(defn collect-symbols
  []
  (let [in (async/chan 10000)
        rec-chan (async/chan 10000)]
    (fetch-csv in)
    (create-records in rec-chan)
    (async/go-loop [data (async/<! rec-chan)] (println data))))
