(ns basilisk.logger.core
  "Logger abstraction namespace. We don't use components
  for the logger because besically it's not necessary.

  In order to use logger first your need to setup the configuration
  in `project.clj` ( which we already did ).

  After that you can simply use the `trace`, `debug`, `info`, `warn`
  `error`, `fatal` and `seppuku` macros to log what ever you like.

  #### Configuration

  In order to configure logger you have to provide a `map` to `initialize`
  function of define the map in `project.clj` under the specific profile
  (for example under `:dev`). The configuration map schema is as follow:

  ```clojure
  {
    :mint-level :debug    ; The logger level. Log entries with less priority
                          ;  will skip.

    :output-fn  default-output-fn     ; (optional) default output function
                                      ; to generate the msg format. Each
                                      ; appender can have its own function.

    :appenders {       ; A map of appenders. Checkout the `basilisk.logger.appenders`
      :stdout {        ; Simple console appender
        :min-level :info
        :fn (fn [data] (println (:output data)))
      }
    }
  }
  ```

  "
  (:require [taoensso.timbre :as timbre]
            [clojure.tools.logging :as log]
            [basilisk.logger.kafka :as kafka]
            [environ.core :refer [env]]
            [com.stuartsierra.component :as component]))

(declare default-output-fn)

(def ^:no-doc default-config

  {:min-level :debug
   :output-fn default-output-fn
   :hostname  (.getHostName (java.net.InetAddress/getLocalHost))
   :appenders {:stdout {:min-level :debug,
                        :fn         (fn [data] (println (:output data)))}

               :stdout1 {:min-level :debug,
                         :output-fn  (fn [data] "NIL")
                         :fn         (fn [data] (println (:output data)))}}})

(def ^:no-doc config (atom default-config))

(def levels
  "Default level priorities. If you use a level name
  which does not exists in this map, It would get priority
  of zero."
  { :trace   10
    :debug   20
    :info    40
    :warn    60
    :error   80
    :fatal   100
    :seppuku 200 })


;; Private functions -------------------------------------
(defn- level-value
  "Return the corresponding integer for given level name."
  [level-symbol]
  (get levels level-symbol 0))

(defn- level-matched?
  "Whether given level is the same or with higher priority of
  limit level."
  [level limit]
  (>= (level-value level) (level-value limit)))

(defn- process-log
  "Pack and pass the data to output-fn and fn of each appender.
  If an appender does not have any output-fn the default one
  will be selected."
  [data]
  (let [output-fn   (or (:output-fn data)
                        (:output-fn @config))

        output      (output-fn data)
        appender-fn (:fn data)
        new-data    (merge data {:output output})]
    (appender-fn new-data)))


(defn- timestamp
  "It's obvious. isn't it ?"
  []
  (let [date (java.util.Date.)]
    (.format (java.text.SimpleDateFormat. "yyyy-MM-dd H:m:s") date)))


;; Public functions & macros -------------------------------------
(defn default-output-fn
  "Default output generator for logger system. If appenders don't
  introduce their own appender function, this one will generates the
  output string."
  [data]
  (let [msg (apply str (:args data))]
    ; I coulda use format instead of str, but format does not supported in cljs
    (str (:timestamp data) " "
         (:instance-name data) " "
         (get data :ns "ns") ":"
         (get data :fn-name "fn") " "
         (get data :file "-") ":"
         (:line data) " ["
         (-> (:level data)
             (clojure.string/replace ":" "")
             clojure.string/upper-case) "] "
         (apply str (:args data)))))


(defmacro log
  "Log the given message as args with the given level."
  [level & args]
  (doseq [[name appender-config] (:appenders @config)]
    (let [min-level (or (:min-level appender-config)
                        (:min-level @config))
          metadata  (meta  &form)
          timestamp (timestamp)]

      (when (level-matched? level min-level)
        (let [data (merge appender-config
                          {:line      (:line metadata)
                           :file      (:file metadata)
                           :ns        (:ns   metadata)
                           :fn-name   (:name metadata)
                           :timestamp timestamp
                           :level     level
                           :args      args})]
          `~(process-log data))))))

(defmacro trace
  [& args]
  `(log :trace ~@args))


(defmacro debug
  [& args]
  `(log :debug ~@args))

(defmacro info
  [& args]
  `(log :info ~@args))

(defmacro warn
  [& args]
  `(log :warn ~@args))

(defmacro error
  [& args]
  `(log :error ~@args))

(defmacro fatal
  [& args]
  `(log :fatal ~@args))

(defmacro seppuku
  [& args]
  `(log :seppuku ~@args))

(defn initialize
  ([]
   (initialize nil nil)
   [zookeeper]
   (initialize zookeeper nil)

   [zookeeper config]
   (let [instance-name (:me zookeeper)]))
