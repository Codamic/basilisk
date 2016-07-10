(ns basilisk.worker
  (:require [com.stuartsierra.component :as component]))


(defrecord Worker []
  component/LifeCycle

  (start [this])

  (stop [this]))
