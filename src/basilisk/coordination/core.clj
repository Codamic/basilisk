(ns basilisk.coordinator.core
  (:require [zookeeper :as zk]
            [basilisk.logger :as log]
            [zookeeper.util :as util]
            [environ.core :refer [env]]
            [com.stuartsierra.component :as component]))

;; TODO: Get this values via a configuration or something
(def my-group-name "/mary")

(def my-instance-name (str my-group-name "/instance-"))
(def my-name nil)
;; ------------------------------------------------------

(def cluster_leader (atom nil))

(defn node-name
  "Extract the node name from a path"
  [path]
  (.substring (:name path) (inc (count my-group-name))))

(defn elect-leader
  "Elect new leader for cluster"
  [coordinator]
  (let [connection (:connection coordinator)
        members (util/sort-sequential-nodes
                 (zk/children connection my-group-name))
        leader (first members)]

    (log/info (str "Elect '" leader "' as new leader."))
    (swap! cluster_leader (fn [x] leader))
    leader))


(defn elect-new-leader
  "Elect new leader based on current event"
  [coordinator event]
  (if (= (:event-type event) :NodeDeleted)
    (do
      (log/warn (str "Node '"(node-name) "' goes down."))
      (elect-leader coordinator))))


(defn connect-to-cluster
  "Connect to sunny cluster and report the situation"
  []
  (let [sunny (zk/connect (env :zookeeper-servers))]
    (when-not (zk/exists sunny "/mary")
      (zk/create sunny my-group-name :persistent? true))
      [sunny (zk/create sunny my-instance-name :persistent? true
                 :async? true
                 :sequential? true
                 :watcher (fn [event] (elect-leader sunny event)))]))

(defn am-i-leader?
  "Return true if current instance be the cluster leader"
  []
  (log/debug "My name is: '" my-name "'")
  (and (not my-name) (= my-name @cluster_leader)))

(defn join-sunny
  "Connect to the sunny cluster and get the leader"
  []
  (let [[sunny_client my_name] (connect-to-cluster)
        leader (elect-leader sunny_client)]
    (def my-name (node-name my_name))
    (def sunny sunny_client)))


(defn leave-sunny
  "Leave the sunny cluster"
  []
  (when (resolve 'sunny)
    (zk/close sunny)))

(defmacro on-change-leader
  "Execute its body when cluster leader changed. Can be use to
  execute leader tasks."
  [& body]
  `(add-watch cluster_leader :watcher (fn [key atom old-state new-state] ~@body)))


(defrecord Coordinator [address connection]
  ;; Implement the Lifecycle protocol
  component/Lifecycle
  (start [this]
    (let [connection (zk/connect (address))]
      (assoc this :connection connection)))

  (stop [this]
    (zk/close connection)
    (assoc this :connection nil)))
