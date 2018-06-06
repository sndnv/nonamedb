(defproject nonamedb "1.0.0-SNAPSHOT"
  :description ""
  :url "https://github.com/sndnv/nonamedb"
  :license {:name "Apache License, Version 2.0"
            :url "http://www.apache.org/licenses/"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [com.taoensso/timbre "4.10.0"]
                 [co.paralleluniverse/quasar-core "0.7.9"]
                 [co.paralleluniverse/pulsar "0.7.9"]
                 [compojure "1.6.1"]
                 [ring/ring-jetty-adapter "1.6.3"]
                 [ring/ring-mock "0.3.2"]]
  :plugins [[lein-ring "0.12.4"]]
  :java-agents [[co.paralleluniverse/quasar-core "0.7.9"]]
  :target-path "target/%s")
