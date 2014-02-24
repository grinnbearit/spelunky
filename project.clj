(defproject spelunky "0.1.0"
  :description "A bitcoin blockchain parser"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [gloss "0.2.2"]
                 [org.bouncycastle/bcprov-jdk16 "1.46"]]
  :plugins [[lein-midje "3.1.3"]]
  :profiles {:dev {:dependencies [[midje "1.6.0"]]}})
