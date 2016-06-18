(ns basilisk.tasks.collector.tsetmc.core
  (:require [clojure.core.async :as async]
            [clojure-csv.core :as csv]
            [clj-http.client :as http]))

(def site-url {:fa {:index-csv "http://www.tsetmc.com/tsev2/data/MarketWatchInit.aspx?h=0&r=0"}})

(def input-channel (async/chan 1000))


(defn fetch-csv []
  (clojure.string/split (:body (http/get (:index-csv (:fa site-url)))) #";"))


(defn with-each-symbol [input]
  (let [lines (take 5 (fetch-csv))]
    (doseq [line lines]
      (let [field (clojure.string/split line #",")]
        (println (str ">>>>> " field))))))

(defn collect-symbols [])
