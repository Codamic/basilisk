(ns basilisk.coordinator.core
  (:require [zookeeper :as zk]
            [basilisk.logger :as log]
            [zookeeper.util :as util]
            [environ.core :refer [env]]
            [com.stuartsierra.component :as component]))

;; The leader of current node role.
(def instance-name "instance-")

;; ------------------------------------------------------
;; TODO: Get this values via a configuration or something
(def root "/mary")
(def role "scheduler")
;; ------------------------------------------------------

(defn directory-path
  "Return the directory path for current node containing the
  root path alongside with role directory"
  []
  (str root "/" role "/"))

(defn full-path
  "Return the full path of the given node name"
  [role node-name]
  (str (directory-path role) node-name))

(defn node-name
  "Extract the node name from a path"
  [path]
  (clojure.string/replace (:name path)
                          (directory-path)
                          ""))


(defn elect-leader
  "Elect new leader for cluster"
  [coordinator]
  (let [connection  (:connection coordinator)
        role        (:role       coordinator)
        leader_atom (:leader     coordinator)
        members     (util/sort-sequential-nodes
                     (zk/children connection (directory-path role)))
        leader      (first members)]

    (log/info (str "Elect '" leader "' as new leader."))
    (swap! leader_atom (fn [x] leader))
    leader))


(defn elect-new-leader
  "Elect new leader based on current event"
  [coordinator event]
  (if (= (:event-type event) :NodeDeleted)
    (do
      (log/warn (str "Node '" (node-name (:path event)) "' goes down."))
      (elect-leader coordinator))))


(defn connect-to-cluster
  "Connect to the cluster for the given role and report the situation"
  [coordinator]
  (let [connection (:connection coordinator)
        role       (:role       coordinator)]

    (when-not (zk/exists connection root)
      (do (zk/create connection root :persistent? true)
          (zk/create connection (directory-path role) :persistent? true)))

    (when-not (zk/exists connection (directory-path role))
      (zk/create connection (directory-path role) :persistent? true))

    (zk/create connection (full-path role instance-name) :persistent? true
               :async? true
               :sequential? true
               :watcher (fn [event] (elect-new-leader connection event)))))

(defn am-i-leader?
  "Return true if current instance be the cluster leader"
  [coordinator]
  (let [me     (:me coordinator)
        leader (:leader coordinator)]

    (log/debug "My name is: '" my-name "'")
    (and (not (nil? me)) (= me leader))))

(defn join-to-cluster
  "Connect to the role cluster and get the leader"
  [coordinator]
  (let [my_name (connect-to-cluster coordinator)]

    (elect-leader coordinator)
    (node-name my_name)))


(defn leave-the-cluster
  "Leave the sunny cluster"
  [coordinator]
  (zk/close (:connection coordinator)))


(defrecord Coordinator [address connection leader]
  ;; Implement the Lifecycle protocol
  component/Lifecycle
  (start [this]
    (let [connection  (zk/connect (address))
          me          (join-to-cluster this)]

      (assoc this
             :connection connection
             :leader     leader
             :me         me)))

  (stop [this]
    (leave-the-cluster this)
    (assoc this :connection nil)))
