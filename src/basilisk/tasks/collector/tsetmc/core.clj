(ns basilisk.tasks.collector.tsetmc.core
  (:require [clojure.core.async :as async]
            [clojure-csv.core :as csv]
            [clj-http.client :as http]))

(def site-url {:fa {:index-csv "http://www.tsetmc.com/tsev2/data/MarketWatchInit.aspx?h=0&r=0"}})

(def input-channel (async/chan 1000))

(println (:index-csv (:fa site-url)))
(defn fetch-csv []
  (csv/parse-csv (http/get (:index-csv (:fa site-url))))

  (defn collect-symbols []))
