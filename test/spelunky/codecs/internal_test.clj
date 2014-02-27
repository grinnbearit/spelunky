(ns spelunky.codecs-test
  (:use midje.sweet
        spelunky.test
        spelunky.codecs.internal
        [spelunky.bytes :only [bytes->hex]]
        [gloss.core :only [compile-frame repeated]]))


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
 (let [byte-frame (compile-frame (buffer-store :ubyte
                                               identity
                                               (fn [x buf] [x (bytes->hex (.array buf))])))]
   (decode-from-hex byte-frame "00")
   => [0 "00"]

   (encode-to-hex byte-frame 0)
   => "00")

 (let [header-frame (compile-frame (buffer-store (repeated :ubyte :prefix :ubyte)
                                                 identity
                                                 (fn [x buf] [x (bytes->hex (.array buf))])))]
   (decode-from-hex header-frame "0100")
   => [[0] "0100"]

   (encode-to-hex header-frame [0])
   => "0100"))


(facts
 (let [codec (script (repeat 4 :ubyte))]
   (decode-from-hex codec "4d010001")        => [:pushdata2 "01"]
   (encode-to-hex   codec [:pushdata2 "01"]) => "4d010001"))
