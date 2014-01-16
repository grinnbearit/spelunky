(ns spelunky.codecs-test
  (:use midje.sweet
        spelunky.test
        spelunky.codecs))


(facts
 (decode-from-hex block-header (str "f9beb4d9" "00000000")) ; magic-number 0
 => {:magic-number 3652501241
     :length 0})
