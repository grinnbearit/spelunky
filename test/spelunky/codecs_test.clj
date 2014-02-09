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
 (decode-from-hex variable-length-integer "fc") => 0xfc
 (encode-to-hex variable-length-integer 0xfc) => "fc"

 (decode-from-hex variable-length-integer (str "fd" "ffff")) => 0xffff
 (encode-to-hex variable-length-integer 0xffff) => "fdffff"

 (decode-from-hex variable-length-integer (str "fe" "ffffffff")) => 0xffffffff
 (encode-to-hex variable-length-integer 0xffffffff) => "feffffffff"

 (decode-from-hex variable-length-integer (str "ff" "ffffffff" "ffffffff")) => 0xffffffffffffffff
 (encode-to-hex variable-length-integer 0xffffffffffffffff) => "ffffffffffffffffff")


(facts
 (decode-from-hex blockchain-header (str "f9beb4d9" "00000000")) ; magic-number 0
 => {:magic-number 0xd9b4bef9
     :length 0})


(facts
 (decode-from-hex input (str "00000000" "00000000" "00000000" "00000000"
                             "00000000" "00000000" "00000000" "00000000"
                             "00000000" "0100ffff" "ffff"))
 => {:hash (str "00000000" "00000000" "00000000" "00000000"
                "00000000" "00000000" "00000000" "00000000")
     :index 0
     :script [0]
     :sequence 0xffffffff})


(facts
 (decode-from-hex output (str "00f2052a" "01000000" "0100"))
 => {:value 5000000000
     :script [0]})


(facts
 (decode-from-hex txn (str "01000000" "01000000" "00000000" "00000000"
                           "00000000" "00000000" "00000000" "00000000"
                           "00000000" "00000000" "000100ff" "ffffff01"
                           "00f2052a" "01000000" "01000000" "0000"))
 => {:version 1
     :inputs [{:hash (str "00000000" "00000000" "00000000" "00000000"
                          "00000000" "00000000" "00000000" "00000000")
               :index 0
               :script [0]
               :sequence 0xffffffff}]
     :outputs [{:value 5000000000
                :script [0]}]
     :lock-time 0})


(facts
 (decode-from-hex block-header (str "01000000" "29ab5f49" "00000000" "00000000"
                                    "00000000" "00000000" "00000000" "00000000"
                                    "00000000" "00000000" "3ba3edfd" "7a7b12b2"
                                    "7ac72c3e" "67768f61" "7fc81bc3" "888a5132"
                                    "3a9fb8aa" "4b1e5e4a" "ffff001d" "1dac2b7c"))
 => {:version 1
     :timestamp #inst "2009-01-03T18:15:05"
     :prev-block (str "00000000" "00000000" "00000000" "00000000"
                      "00000000" "00000000" "00000000" "00000000")
     :merkle-root (str "4a5e1e4b" "aab89f3a" "32518a88" "c31bc87f"
                       "618f7667" "3e2cc77a" "b2127b7a" "fdeda33b")
     :bits 486604799
     :nonce 2083236893})


(facts
 (decode-from-hex block (str "01000000" "29ab5f49" "00000000" "00000000"
                             "00000000" "00000000" "00000000" "00000000"
                             "00000000" "00000000" "3ba3edfd" "7a7b12b2"
                             "7ac72c3e" "67768f61" "7fc81bc3" "888a5132"
                             "3a9fb8aa" "4b1e5e4a" "ffff001d" "1dac2b7c"
                             "01010000" "00010000" "00000000" "00000000"
                             "00000000" "00000000" "00000000" "00000000"
                             "00000000" "00000000" "00000100" "ffffffff"
                             "0100f205" "2a010000" "00010000" "000000"))
 => {:version 1
     :timestamp #inst "2009-01-03T18:15:05"
     :prev-block (str "00000000" "00000000" "00000000" "00000000"
                      "00000000" "00000000" "00000000" "00000000")
     :merkle-root (str "4a5e1e4b" "aab89f3a" "32518a88" "c31bc87f"
                       "618f7667" "3e2cc77a" "b2127b7a" "fdeda33b")
     :bits 486604799
     :nonce 2083236893
     :txns [{:version 1
             :inputs [{:hash (str "00000000" "00000000" "00000000" "00000000"
                                  "00000000" "00000000" "00000000" "00000000")
                       :index 0
                       :script [0]
                       :sequence 0xffffffff}]
             :outputs [{:value 5000000000
                        :script [0]}]
             :lock-time 0}]})
