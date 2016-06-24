(ns basilisk.tasks.collectors.tsetmc.core
  (:require [clojure.core.async
             :as async
             :refer [<! >! <!! >!! go go-loop chan close! thread]]
            [clojure-csv.core :as csv]
            [clj-http.client :as http]))

(def site-url {:fa {:index-csv "http://www.tsetmc.com/tsev2/data/MarketWatchInit.aspx?h=0&r=0"}})

(defrecord TradeSymbol [symbol name tsetmc-id tsetmc-name])

;; Helper functions
(defn- parse-lines [string]
  (clojure.string/split string #";"))

(defn- create-record
  [coll]
  (map->TradeSymbol
   {:symbol      (get coll 2)
    :name        (get coll 3)
    :tsetmc-id   (get coll 0)
    :tsetmc-name (get coll 1)}))


;; Global functions
(defn csv-fetcher
  "Fetch the csv and return a channel containing the result."
  []
  (thread (:body (http/get (:index-csv (:fa site-url))))))

(defn csv-parser
  "Parse the csv string and put each record in a channel"
  [in]
  (let [out (chan 10000)
        data  (<!! in)]
    (async/onto-chan out (parse-lines data))
    out))


(defn line-parser
  "Parse each line and create a record from it and put it in a nre channel"
  [in]
  (let [out (chan 10000)]
    (go-loop []
      (when-let [line (<! in)]
        (let [fields (clojure.string/split line #",")
              fields_count (count fields)
              record (case fields_count
                    23 (create-record fields)
                    30 (create-record fields)
                    41 (create-record (subvec fields 18))
                    nil)]
          (if (not (nil? record))
            (do (>! out record)))))
      (recur))
    out))

(defn printer
  [in]
  (loop []
    (when-let [data (<!! in)]
      (if (not (nil? data))
        (do
          (clojure.pprint/pprint data)
          (recur))
        (close! in)))))

(defn collect-symbols
  "Collect the symbols data such as name and id from tsetmc"
  []
  (let [csv (csv-fetcher)
        lines (csv-parser csv)
        records (line-parser lines)]
    records))
