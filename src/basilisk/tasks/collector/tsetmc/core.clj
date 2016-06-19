(ns basilisk.tasks.collector.tsetmc.core
  (:require [clojure.core.async :as async]
            [clojure-csv.core :as csv]
            [clj-http.client :as http]))

(def site-url {:fa {:index-csv "http://www.tsetmc.com/tsev2/data/MarketWatchInit.aspx?h=0&r=0"}})

(def input-channel (async/chan 1000))

(defrecord TradeSymbol [symbol name tsetmc-id tsetmc-name])


(defn fetch-csv []
  (async/>!! input-channel
             (clojure.string/split (:body (http/get (:index-csv (:fa site-url)))) #";")))

(defn create-record [line]
  (let [fields (clojure.string/split line #",")]
    (if (= 23 (count fields))
      (map->TradeSymbol
        {:symbol      (get fields 2)
         :name        (get fields 3)
         :tsetmc-id   (get fields 0)
         :tsetmc-name (get fields 1)})
      nil)))

(defn with-each-symbol []
  (let [lines (take 5 (fetch-csv))]
    (map create-record lines)))

(defn collect-symbols [])
