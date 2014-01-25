(ns spelunky.core-test
  (:use midje.sweet
        spelunky.test
        spelunky.core
        [spelunky.bytes :only [bytes->hex]]))


(facts
 (read-block (hex->stream (str "00000000 00000000")))
 => (throws clojure.lang.ExceptionInfo)


 (read-block (hex->stream (str "f9beb4d9" "8f000000" "01000000" "29ab5f49"
                               "00000000" "00000000" "00000000" "00000000"
                               "00000000" "00000000" "00000000" "00000000"
                               "3ba3edfd" "7a7b12b2" "7ac72c3e" "67768f61"
                               "7fc81bc3" "888a5132" "3a9fb8aa" "4b1e5e4a"
                               "ffff001d" "1dac2b7c" "01010000" "00010000"
                               "00000000" "00000000" "00000000" "00000000"
                               "00000000" "00000000" "00000000" "00000000"
                               "00000100" "ffffffff" "0100f205" "2a010000"
                               "00010000" "000000")))
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
