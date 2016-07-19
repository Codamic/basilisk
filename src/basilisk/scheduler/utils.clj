(ns basilisk.scheduler.utils
  (:require [clojure.core.async :as async]
            [basilisk.logger :as logger]
            [mesomatic.scheduler :as scheduler]
            [clojure.string :as string]
            [leiningen.core.main :as lein])
  (:import java.util.UUID))


;;; Payload utility functions
;;; --------------------------
(defn get-framework-id
  ""
  [payload]
  (get-in payload [:framework-id :value]))

(defn get-offers
  ""
  [payload]
  (get-in payload [:offers]))

(defn get-error-msg
  ""
  [payload]
  (let [msg (get-in payload [:status :message])]
    (cond
      (empty? msg) (name (get-in payload [:status :reason]))
      :true msg)))

(defn get-master-info
  ""
  [payload]
  (:master-info payload))

(defn get-offer-id
  ""
  [payload]
  (:offer-id payload))

(defn get-status
  ""
  [payload]
  (:status payload))

(defn get-state
  ""
  [payload]
  (name (get-in payload [:status :state])))

(defn healthy?
  ""
  [payload]
  (get-in payload [:status :healthy]))

(defn get-executor-id
  ""
  [payload]
  (get-in payload [:executor-id :value]))

(defn get-slave-id
  ""
  [payload]
  (get-in payload [:slave-id :value]))

(defn get-message
  ""
  [payload]
  (:message payload))

(defn get-bytes
  ""
  [payload]
  (.toStringUtf8 (get-in payload [:status :data])))

(defn log-framework-msg
  ""
  [framework-id executor-id slave-id payload]
  (let [bytes (String. (:data payload))
        log-type? (partial string/includes? bytes)]
    (cond
      (log-type? "TRACE") (logger/trace bytes)
      (log-type? "DEBUG") (logger/debug bytes)
      (log-type? "INFO") (logger/info bytes)
      (log-type? "WARN") (logger/warn bytes)
      (log-type? "ERROR") (logger/error bytes)
      :else (logger/infof
              "Framework %s got message from executor %s (slave=%s): %s"
              framework-id executor-id slave-id bytes))))

(defn get-task-state
  ""
  [payload]
  (get-in payload [:status :state]))

(defn get-task-id
  ""
  [payload]
  (get-in payload [:status :task-id :value]))


;;; State utility functions
;;; -----------------------
(defn get-driver
  ""
  [state]
  (:driver state))

(defn get-channel
  ""
  [state]
  (:channel state))

(defn get-exec-info
  ""
  [state]
  (:exec-info state))

(defn get-max-tasks
  ""
  [state]
  (get-in state [:limits :max-tasks]))

;;; General utility functions
;;; -------------------------

(defn get-uuid
  "A Mesos/protobufs-friendly UUID wrapper."
  []
  (->> (UUID/randomUUID)
       (str)
       (assoc {} :value)))

(defn lower-key
  "Convert a string to a lower-cased keyword."
  [str]
  (-> str
      (string/lower-case)
      (keyword)))

(defn keys->keyword
  "Convert all the keys in a map from strings to lower-cased keywords."
  [m]
  (zipmap
    (map lower-key (keys m))
    (vals m)))

(defn make-env
  "Convert the OS environment variables to a Mesos-ready map."
  []
  (->> (System/getenv)
       (keys->keyword)
       (into [])
       (assoc {} :variables)))

(defn cwd
  ""
  []
  (-> "."
      (java.io.File.)
      (.getAbsolutePath)))

(defn finish
  ""
  [& {:keys [exit-code]}]
  (lein/exit exit-code)
  exit-code)

(defn get-metas
  ""
  [an-ns]
  (->> an-ns
       (ns-publics)
       (map (fn [[k v]] [k (meta v)]))
       (into {})))

(defn get-meta
  "Takes the same form as the general `get-in` function:
      (get-meta 'my.name.space ['my-func :doc])"
  [an-ns rest]
  (-> an-ns
      (get-metas)
      (get-in rest)))

(defn get-docstring
  ""
  [an-ns fn-name]
  (get-meta an-ns [fn-name :doc]))

(defn do-unhealthy-status
  ""
  [state-name state payload]
  (logger/debug "Doing unhealthy check ...")
  (do
    (logger/errorf "%s - %s"
                state-name
                (get-error-msg payload))
    (async/close! (get-channel state))
    (scheduler/stop! (get-driver state))
    (finish :exit-code 127)
    state))

(defn check-task-finished
  ""
  [state payload]
  (if (= (get-task-state payload) :task-finished)
    (let [task-count (inc (:launched-tasks state))
          new-state (assoc state :launched-tasks task-count)]
      (logger/debug "Incremented task-count:" task-count)
      (logger/info "Tasks finished:" task-count)
      (if (>= task-count (get-max-tasks state))
        (do
          (scheduler/stop! (get-driver state))
          (finish :exit-code 0)
          new-state)
        new-state))
    state))

(defn check-task-abort
  ""
  [state payload]
  (if (or (= (get-task-state payload) :task-lost)
          (= (get-task-state payload) :task-killed)
          (= (get-task-state payload) :task-failed))
    (let [status (:status payload)]
      (logger/errorf (str "Aborting because task %s is in unexpected state %s "
                       "with reason %s from source %s with message '%s'")
                  (get-task-id payload)
                  (:state status)
                  (:reason status)
                  (:source status)
                  (:message status))
      (scheduler/abort! (get-driver state))
      (finish :exit-code 127)
      state)
    state))


(defn do-healthy-status
  ""
  [state payload]
  (logger/debug "Doing healthy check ...")
  (-> state
      (check-task-finished payload)
      (check-task-abort payload)))
