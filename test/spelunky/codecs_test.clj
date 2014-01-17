(ns spelunky.codecs-test
  (:use midje.sweet
        spelunky.test
        spelunky.codecs))


(facts
 (decode-from-hex timestamp "80436d38")
 => #inst "2000-01-01"

 (encode-to-hex timestamp #inst "2000-01-01")
 => "80436d38")


(facts
 (decode-from-hex (hex-string-be (repeat 4 :ubyte)) "00000001")
 => "00000001"

 (encode-to-hex (hex-string-be (repeat 4 :ubyte)) "00000001")
 => "00000001")


(facts
 (decode-from-hex (hex-string-le (repeat 4 :ubyte)) "01000000")
 => "00000001"

 (encode-to-hex (hex-string-le (repeat 4 :ubyte)) "01000000")
 => "00000001")


(facts
 (decode-from-hex block-header (str "f9beb4d9" "00000000")) ; magic-number 0
 => {:magic-number 3652501241
     :length 0})


(facts
 (decode-from-hex block (str "01000000" "29ab5f49" "00000000" "00000000"
                             "00000000" "00000000" "00000000" "00000000"
                             "00000000" "00000000" "3ba3edfd" "7a7b12b2"
                             "7ac72c3e" "67768f61" "7fc81bc3" "888a5132"
                             "3a9fb8aa" "4b1e5e4a" "ffff001d" "1dac2b7c"))
 => {:version 1
     :timestamp #inst "2009-01-03T18:15:05"
     :prev-block "0000000000000000000000000000000000000000000000000000000000000000"
     :merkle-root "4a5e1e4baab89f3a32518a88c31bc87f618f76673e2cc77ab2127b7afdeda33b"
     :bits 486604799
     :nonce 2083236893})
