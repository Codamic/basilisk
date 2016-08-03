(defproject basilisk "0.1.0-SNAPSHOT"
  :description "Clustering  and big data processing platform."
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

                 ;; Mesos Client
 		 [spootnik/mesomatic "0.28.0-r0"]
                 [spootnik/mesomatic-async "0.28.0-r0"]

                 ;; Kafka
                 [kafka-clj "3.6.5"]


                 ;; Zookeeper
                 [zookeeper-clj "0.9.4"]

                 ;; Component library
                 [com.stuartsierra/component "0.3.1"]

                 ;; Leiningen itself
                 [leiningen-core "2.6.1"]

                 ;; Logging library
                 [com.taoensso/timbre "4.7.0"]

                 [clj-json "0.5.3"]
                 [clojure-csv/clojure-csv "2.0.2"]
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
