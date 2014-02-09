(ns spelunky.bytes-test
  (:use midje.sweet
        spelunky.test
        spelunky.bytes
        [gloss.core :only [compile-frame repeated]])
  (:import [java.io ByteArrayInputStream]))


(facts
 (ints->hex [222 173 190 239]) => "deadbeef"
 (hex->ints "deadbeef") => [222 173 190 239])


(facts
 (bytes->hex (ints->bytes [255 1 127])) => "ff017f"
 (bytes->ints (hex->bytes "ff017f")) => [255 1 127])


(facts
 (bytes->short (hex->bytes "0001")) => 1
 (bytes->short (hex->bytes "0100")) => 256)


(facts
 (bytes->int (hex->bytes "00000001")) => 1
 (bytes->int (hex->bytes "00010000")) => 65536)


(facts
 (bytes->hex (short->bytes 1))   => "0001"
 (bytes->hex (short->bytes 256)) => "0100")


(facts
 (bytes->hex (int->bytes 1))     => "00000001"
 (bytes->hex (int->bytes 65536)) => "00010000")


(facts
 (bytes->ints (read-bytes (ByteArrayInputStream. (hex->bytes "000100")) 4)) => [0 1 0 0]
 (bytes->hex (read-bytes (ByteArrayInputStream. (hex->bytes "abcdef")) 3)) => "abcdef"
 (bytes->hex (read-bytes (ByteArrayInputStream. (hex->bytes "ffaabbccddeeff")) 3)) => "ffaabb")


(facts
 (let [byte-frame (compile-frame (buffer-store :ubyte)
                                 identity
                                 (fn [[x buf]] [x (bytes->hex (.array buf))]))]
   (decode-from-hex byte-frame "00")
   => [0 "00"]

   (encode-to-hex byte-frame 0)
   => "00")

 (let [header-frame (compile-frame (buffer-store (repeated :ubyte :prefix :ubyte))
                                   identity
                                   (fn [[x buf]] [x (bytes->hex (.array buf))]))]
   (decode-from-hex header-frame "0100")
   => [[0] "0100"]

   (encode-to-hex header-frame [0])
   => "0100"))
