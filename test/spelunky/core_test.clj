(ns spelunky.core-test
  (:use midje.sweet
        spelunky.test
        spelunky.core
        [spelunky.bytes :only [bytes->hex]]))


(facts
 (read-block (hex->stream (str "00000000 00000000")))
 => (throws clojure.lang.ExceptionInfo)


 (-> (read-block (hex->stream (str "f9beb4d9" "00000000" "01000000" "29ab5f49"
                                   "00000000" "00000000" "00000000" "00000000"
                                   "00000000" "00000000" "00000000" "00000000"
                                   "3ba3edfd" "7a7b12b2" "7ac72c3e" "67768f61"
                                   "7fc81bc3" "888a5132" "3a9fb8aa" "4b1e5e4a"
                                   "ffff001d" "1dac2b7c"))))
 => {:version 1
     :timestamp #inst "2009-01-03T18:15:05"
     :prev-block [0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0]
     :merkle-root [59 163 237 253 122 123 18 178 122 199 44 62 103 118 143 97 127 200 27 195 136 138 81 50 58 159 184 170 75 30 94 74]
     :bits 486604799
     :nonce 2083236893})
