(ns spelunky.bytes
  (:require [gloss.core.protocols :as p])
  (:use [clojure.string :only [lower-case]]
        [gloss.core :only [compile-frame]]
        [gloss.data.bytes :only [take-contiguous-bytes byte-count]])
  (:import [java.nio ByteBuffer]
           [java.security MessageDigest]))


(defn read-bytes
  [stream n]
  (let [buffer (byte-array n)]
    (.read stream buffer)
    buffer))


(defn ints->bytes
  [int-seq]
  (->> int-seq
       (map #(if (< % 128) % (- % 256)))
       (map byte)
       byte-array))


(defn bytes->ints
  [barray]
  (->> barray
       (map int)
       (map #(if (neg? %) (+ % 256) %))))


(defn ints->hex
  [int-seq]
  (->> int-seq
       (map #(format "%02x" %))
       (apply str)))


(let [charset (set "0123456789abcdef")
      char->int (zipmap "0123456789abcdef"
                        (range 17))]
  (defn hex->ints
    [hexstr]
    (->> hexstr
         lower-case
         (filter charset)
         (map char->int)
         (partition 2)
         (map (fn [[x y]] (+ (bit-shift-left x 4) y))))))


(defn bytes->short
  [bytes]
  (.getShort (ByteBuffer/wrap bytes)))


(defn short->bytes
  [n]
  (-> (ByteBuffer/allocate 2)
      (.putShort n)
      .flip
      .array))


(defn bytes->int
  [bytes]
  (.getInt (ByteBuffer/wrap bytes)))


(defn int->bytes
  [n]
  (-> (ByteBuffer/allocate 4)
      (.putInt n)
      .flip
      .array))


(def bytes->hex
  (comp ints->hex bytes->ints))


(def hex->bytes
  (comp ints->bytes hex->ints))


(defn sha256
  [barray]
  (let [digest (MessageDigest/getInstance "SHA-256")]
    (.update digest barray)
    (.digest digest)))


(defn double-sha256
  [barray]
  (let [digest (MessageDigest/getInstance "SHA-256")]
    (.update digest barray)
    (.update digest (.digest digest))
    (.digest digest)))


(defn bitcoin-hash
  "Returns the double-sha256 of the bytes as a little-endian hex-string"
  [bytes]
  (-> bytes double-sha256 bytes->ints reverse ints->hex))


(deftype BufferStore [frame]
  p/Reader
  (read-bytes [this buf-seq]
    (if-let [size (p/sizeof frame)]
      (let [store (take-contiguous-bytes buf-seq size)
            [success x xs] (p/read-bytes frame buf-seq)]
        [success [x store] xs])
      (let [[success x xs] (p/read-bytes frame buf-seq)
            store (take-contiguous-bytes buf-seq
                                         (- (byte-count buf-seq)
                                            (byte-count xs)))]
        [success [x store] xs])))
  p/Writer
  (sizeof [this]
    (p/sizeof frame))
  (write-bytes [this buf val]
    (p/write-bytes frame buf val)))


(defn buffer-store
  "On read, returns a vector of the decoded value and its byte buffer,
expects only the decoded value on write

The post-decoder takes the byte buffer as a second argument"
  ([frame]
     (BufferStore. (compile-frame frame)))
  ([frame pre-encoder post-decoder]
     (compile-frame (buffer-store frame)
                    pre-encoder
                    (fn [[val buf]] (post-decoder val buf)))))
