(ns spelunky.core-test
  (:use midje.sweet
        spelunky.test
        spelunky.core
        [spelunky.bytes :only [bytes->hex]]))


(facts
 (read-block (hex->stream (str "00000000 00000000")))
 => (throws clojure.lang.ExceptionInfo)


 (-> (read-block (hex->stream (str "f9beb4d9" "00000000")))
     bytes->hex)
 => "")
