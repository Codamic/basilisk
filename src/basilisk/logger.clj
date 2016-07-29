(ns basilisk.logger
  (:require [clojure.tools.logging :as log]))

; :refer [debug debugf info infof error errorf fatal fatalf log logf trace tracef
;; (doseq [n `[debug debugf info infof error errorf fatal fatalf log
;;             logf trace tracef]])
(defn info [str]
  (log/info str))

(defn debug [str]
  (log/debug str))

(defn error [str]
  (log/error str))

(defn fatal [str]
  (log/fatal str))

(defn trace [str]
  (log/trace str))

(defn log [type str]
  (log/log type str))

(defn infof [str & rest]
  (log/infof str rest))

;; (defn debug [str]
;;   (log/debug str))

;; (defn error [str]
;;   (log/error str))

;; (defn fatal [str]
;;   (log/fatal str))

;; (defn trace [str]
;;   (log/trace str))

;; (defn log [str]
;;   (log/log str))
