(defproject basilisk "0.1.0-SNAPSHOT"
  :description "Data warehouse plaftform for Codamic."
  :url "http://github.com/Codamic/basilisk"
  :license {:name "GNU Public Licence version 2"
            :url "http://www.gnu.org/licenses/old-licenses/gpl-2.0.en.html"}

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [clj-http "2.2.0"]
                 [org.clojure/tools.cli "0.3.5"]
                 [hickory "0.6.0"]
                 [environ "1.0.3"]
                 [com.novemberain/monger "3.0.2"]
                 [clojure-csv/clojure-csv "2.0.1"]
                 [riemann-clojure-client "0.4.2"]
                 [org.clojure/core.async "0.2.382"]]

  :plugins [[lein-environ "1.0.3"]]
  :main ^:skip-aot basilisk.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}
             :dev {:mongo {:host "mongodb"
                           :port 7878
                           :db-name "basilisk_dev"}}}
  )
