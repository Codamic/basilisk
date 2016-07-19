(ns basilisk.scheduler.core
  "This namespace contains the Mesos scheduler for the Basilisk
  framework."
  (:require [com.stuartsierra.component :as component]
            [mesomatic.async.scheduler :as async-scheduler]
            [mesomatic.scheduler :as scheduler :refer [scheduler-driver]]
            [mesomatic.types :as types]
            [clojure.core.async :as async :refer [go <! chan]]
            [basilisk.logger :as logger]))


;; TODO: We have to populate this hashmap using a configuration file or
;;       something. But for now it's ok to use it this way.
(def framework-info-map {:name "Basilisk"
                         :principal "basilisk"})

(defn run
  "This is the function that actually runs the framework."
  [master task-count]

  (logger/info "Running the Basilisk Framework...")
  (let [ch (chan)
        sched (async-scheduler/scheduler ch)
        driver (scheduler-driver sched
                                 framework-info-map
                                 master
                                 nil
                                 false)]
    (logger/debug "Starting Basilisk Scheduler ...")
    (scheduler/start! driver)
    (log/debug "Reducing over hello-world scheduler channel messages ...")
    (async/reduce handle-msg {:driver driver
                              :channel ch
                              :exec-info nil
                              :launched-tasks 0
                              :limits (assoc limits :max-tasks task-count)} ch)
    (scheduler/join! driver)))
