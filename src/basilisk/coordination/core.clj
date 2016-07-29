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
  [role]
  (str root "/" role))


(defn full-path
  "Return the full path of the given node name"
  [role node-name]
  (str (directory-path role) "/" node-name))

(defn node-name
  "Extract the node name from a path"
  [path role]
  (clojure.string/replace path (directory-path role) ""))


(defn elect-leader
  "Elect new leader for cluster"
  [coordinator connection]
  (let [role        (:role       coordinator)
        leader_atom (:leader     coordinator)
        members     (util/sort-sequential-nodes
                     (zk/children connection (directory-path role)))
        leader      (first members)]

    (log/info (str "Elect '" leader "' as new leader."))
    (swap! leader_atom (fn [x] leader))
    leader))


(defn elect-new-leader
  "Elect new leader based on current event"
  [coordinator connection event]
  (let [role (:role coordinator)]
    (if (= (:event-type event) :NodeDeleted)
      (do
        (log/warn (str "Node '" (node-name (:path event)) "' goes down."))
        (elect-leader coordinator connection)))))


(defn connect-to-cluster
  "Connect to the cluster for the given role and report the situation"
  [coordinator connection]
  (let [role (:role  coordinator)]
    (when-not (zk/exists connection root)
      (do (zk/create connection root :persistent? true)
          (zk/create connection (directory-path role) :persistent? true)))

    (when-not (zk/exists connection (directory-path role))
      (zk/create connection (directory-path role) :persistent? true))

    (zk/create connection (full-path role instance-name) :persistent? true
               :async? false
               :sequential? true
               :watcher (fn [event] (elect-new-leader coordinator
                                                      connection
                                                      event)))))

(defn am-i-leader?
  "Return true if current instance be the cluster leader"
  [coordinator]
  (let [me     (:me coordinator)
        leader (:leader coordinator)]

    (log/debug (str "My name is: '" me "'"))
    (and (not (nil? me)) (= me leader))))

(defn join-the-cluster
  "Connect to the role cluster and get the leader"
  [coordinator connection role]
  (let [my_name (connect-to-cluster coordinator connection)]
    (elect-leader coordinator connection)
    (node-name my_name role)))


(defn leave-the-cluster
  "Leave the sunny cluster"
  [coordinator]
  (println (:connection coordinator))
  (zk/close (:connection coordinator)))


(defrecord Coordinator [address leader role]
  ;; Implement the Lifecycle protocol
  component/Lifecycle
  (start [this]
    (let [connection  (zk/connect address)
          me          (join-the-cluster this connection role)]

      (assoc this
             :connection connection
             :leader     leader
             :me         me)))

  (stop [this]
    (leave-the-cluster this)
    (assoc this :connection nil)))


(defn make-coordinator
  "Create a coordinator component. Using the given address
  for coordinator to connect."
  [address]
  (map->Coordinator {:address address :leader (atom nil)
                     :role "web"}))

;; (def a (make-coordinator "localhost:2181"))
;; (-> a
;;     component/start
;;     component/stop)
