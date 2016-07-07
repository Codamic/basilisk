(ns user
  (:require [schema.core :as s]
            [clojure.tools.namespace.repl :refer [refresh]]))

(s/set-fn-validation! true)
(defn reload [] (refresh))
