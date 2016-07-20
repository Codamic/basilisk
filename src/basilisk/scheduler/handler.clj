(ns basilisk.scheduler.handler
  "This module contains the handler function that is responsible for
  handling Mesos master messages."
  (:require [basilisk.logger :as logger]
            [basilisk.scheduler.utils :as util]
            [clojusc.twig :refer [pprint]
            [mesomatic.scheduler :as scheduler])))

;;; Temporary code
;;; TODO: we have to fetch most of these stuff via a configuration file or something

(defn cmd-info-map
  [master-info framework-id]
  {:executor-id (util/get-uuid)
   :name "dummy executor"
   :framework-id {:value framework-id}
   :command {:value "cd / && ls" :shell true}})


(defmulti handle-msg
  "This is a custom multimethod for handling messages that are received on the
  async scheduler channel.
  Note that:
  * though the methods are associated with types whose names match the
    scheduler API, these functions and those are quite different and do not
    accept the same parameters
  * each handler's callback (below) only takes two parameters:
     1. state that gets passed to successive calls (if returned by the handler)
     2. the payload that is sent to the async channel by the scheduler API
  * as such, if there is something in a message which you would like to persist
    or have access to in other functions, you'll need to assoc it to state."
  (comp :type last vector))


(defmethod handle-msg :registered
  [state payload]
  (let [master-info (get-master-info payload)
        framework-id (get-framework-id payload)
        exec-info (cmd-info-map master-info framework-id)]
    (logger/info "Registered with framework id:" framework-id)
    (logger/trace "Got master info:" (pprint master-info))
    (logger/trace "Got state:" (pprint state))
    (logger/trace "Got exec info:" (pprint exec-info))
    (assoc state :exec-info exec-info
                 :master-info master-info
                 :framework-id {:value framework-id})))

(defmethod handle-msg :disconnected
  [state payload]
  (logger/infof "Framework %s disconnected." (get-framework-id payload))
  state)

(defmethod handle-msg :resource-offers
  [state payload]
  (logger/info "Handling :resource-offers message ...")
  (logger/trace "Got state:" (pprint state))
  (let [offers-data (get-offers payload)
        offer-ids (offers/get-ids offers-data)
        tasks (offers/process-all state payload offers-data)
        driver (get-driver state)]
    (logger/trace "Got offers data:" offers-data)
    (logger/trace "Got offer IDs:" (map :value offer-ids))
    (logger/trace "Got other payload:" (pprint (dissoc payload :offers)))
    (logger/debug "Created tasks:"
               (string/join ", " (map task/get-pb-name tasks)))
    (logger/tracef "Got payload for %d task(s): %s"
                (count tasks)
                (pprint (into [] (map pprint tasks))))
    (logger/info "Launching tasks ...")
    (scheduler/accept-offers
      driver
      offer-ids
      [{:type :operation-launch
        :tasks tasks}])
    (assoc state :offers offers-data :tasks tasks)))

(defmethod handle-msg :status-update
  [state payload]
  (let [status (get-status payload)
        state-name (get-state payload)]
    (logger/infof "Handling :status-update message with state '%s' ..."
               state-name)
    (logger/trace "Got state:" (pprint state))
    (logger/trace "Got status:" (pprint status))
    (logger/trace "Got status info:" (pprint payload))
    (scheduler/acknowledge-status-update (get-driver state) status)
    (if-not (healthy? payload)
      (do-unhealthy-status state-name state payload)
      (do-healthy-status state payload))))

(defmethod handle-msg :disconnected
  [state payload]
  (logger/infof "Framework %s disconnected." (get-framework-id payload))
  state)

(defmethod handle-msg :offer-rescinded
  [state payload]
  (let [framework-id (get-framework-id state)
        offer-id (get-offer-id payload)]
    (logger/infof "Offer %s rescinded from framework %s."
               offer-id (get-framework-id payload))
    state))

(defmethod handle-msg :framework-message
  [state payload]
  (let [framework-id (get-framework-id state)
        executor-id (get-executor-id payload)
        slave-id (get-slave-id payload)]
    (log-framework-msg framework-id executor-id slave-id payload)
    state))

(defmethod handle-msg :slave-lost
  [state payload]
  (let [slave-id (get-slave-id payload)]
    (logger/error "Framework %s lost connection with slave %s."
               (get-framework-id payload)
               slave-id)
    state))

(defmethod handle-msg :executor-lost
  [state payload]
  (let [executor-id (get-executor-id payload)
        slave-id (get-slave-id payload)
        status (get-status payload)]
    (logger/errorf (str "Framework lost connection with executor %s (slave=%s) "
                     "with status code %s.")
                executor-id slave-id status)
    state))

(defmethod handle-msg :error
  [state payload]
  (let [message (get-message payload)]
    (logger/error "Got error message: " message)
    (logger/debug "Data:" (pprint payload))
    state))

(defmethod handle-msg :default
  [state payload]
  (logger/warn "Unhandled message: " (pprint payload))
  state)
