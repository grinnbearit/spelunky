(ns spelunky.test
  (:use [gloss.io :only [encode decode]]
        [spelunky.bytes :only [bytes->hex hex->bytes]])
  (:import [java.io ByteArrayInputStream]))


(defn encode-to-hex
  [frame val]
  (->> (encode frame val)
       (map #(.array %))
       (map bytes->hex)
       (apply str)))


(defn decode-from-hex
  [frame hex]
  (decode frame (hex->bytes hex)))


(defn hex->stream
  [hex]
  (ByteArrayInputStream. (hex->bytes hex)))
