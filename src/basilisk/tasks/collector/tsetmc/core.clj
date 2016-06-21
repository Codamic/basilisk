(ns basilisk.tasks.collector.tsetmc.core
  (:require [clojure.core.async
             :as async
             :refer [<! >! <!! >!! go go-loop chan close! thread]]
            [clojure-csv.core :as csv]
            [clj-http.client :as http]))

(def site-url {:fa {:index-csv "http://www.tsetmc.com/tsev2/data/MarketWatchInit.aspx?h=0&r=0"}})

(def in (chan 1000))

(defrecord TradeSymbol [symbol name tsetmc-id tsetmc-name])

(defn- parse-lines [string]
  (clojure.string/split string #";"))

(defn csv-fetcher
  "Fetch the csv and return a channel containing the result."
  []
  (thread (:body (http/get (:index-csv (:fa site-url))))))

(defn csv-parser
  "Parse the csv string and put each record in a channel"
  [in]
  (let [out (chan 10000)]
    (go-loop [data (<! in)]
      (if (nil? data)
        (close! data)
        (>! out  (parse-lines data))))))

(defn line-parser
  "Parse each line and create a record from it and put it in a nre channel"
  [in]
  (let [out (chan 10000)]
    (go-loop [line (<! in)]
      (let [fields (clojure.string/split line #",")]
        (if (= 23 (count fields))
          (>! out (map->TradeSymbol
                    {:symbol      (get fields 2)
                     :name        (get fields 3)
                     :tsetmc-id   (get fields 0)
                     :tsetmc-name (get fields 1)}))))
      (recur (<! in)))))


(defn printer
  [in]
  (go-loop [data (<! in)]
    (println (str ">>" (:name data)))
    (recur (<! in))))

(defn collect-symbols
  []
  (let [csv (csv-fetcher)
        lines (csv-parser csv)
        records (line-parser lines)]
                                        ;(printer records)
    ))
