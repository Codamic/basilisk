(ns basilisk.core
  ;(:require [clojure.tools.cli :refer [parse-opts]])
  (:gen-class))


(defn- task-name-to-function
  "Convert a string to function path."
  [string]
  (let [tokens (clojure.string/split string #":")]
    (case (count tokens)
      0 nil
      1 (str "basilisk.tasks.core/" (first tokens))
      (do
        (str "basilisk.tasks."
             (clojure.string/join
              "."
              (subvec tokens 0 (- (count tokens) 1))) "/" (last tokens))))))

(defn- print-usage
  [task]
  (println (str "Can't find task '" task "'.")))


(defn- run-task
  [task args]
  (let [func (resolve task)]
    (if (nil? func)
      (print-usage task)
      (apply (resolve task) args))))

(defn -main
  "I don't do a whole lot ... yet."
  [command & args]
  (let [task (task-name-to-function command)]

    (if (nil? task)
      (print-usage command)
      (run-task (symbol task) args))))
