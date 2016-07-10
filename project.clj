(defproject basilisk "0.1.0-SNAPSHOT"
  :description "Data warehouse plaftform for Codamic."
  :url "http://github.com/Codamic/basilisk"
  :license {:name "GNU Public Licence version 2"
            :url "http://www.gnu.org/licenses/old-licenses/gpl-2.0.en.html"}

  :dependencies [[org.clojure/clojure "1.9.0-alpha9"]
                 [clj-http "3.1.0"]
                 [environ "1.0.3"]

                 ;; Type checking
                 [prismatic/schema "1.1.2"]

                 ;; Metrics
                 [metrics-clojure "2.7.0"]

                 ;; Kafka
                 [kafka-clj "3.6.5"]
                 [com.stuartsierra/component "0.3.1"]
                 [clojure-csv/clojure-csv "2.0.2"]
                 [riemann-clojure-client "0.4.2"]
                 [rethinkdb "0.10.1"]
                 [org.clojure/core.async "0.2.385"]]

  :plugins [[lein-environ "1.0.3"]
            [funcool/codeina "0.4.0"
             :exclusions [org.clojure/clojure]]]

  :codeina {:sources ["src"]
            :reader :clojure
            :target "doc/"
            :src-uri "http://github.com/Codamic/basilisk/blob/master/"
            :src-uri-prefix "#L"}

  :main ^:skip-aot basilisk.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}
             :dev {:dependencies [[org.clojure/tools.namespace "0.2.11"]]
                   :source-paths ["dev"]
                   :env {:rethink-host "rethink"
                         :rethink-port "28015"
                         :rethink-db   "basilisk_dev"}}}
  )
