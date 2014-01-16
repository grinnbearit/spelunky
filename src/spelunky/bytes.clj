(ns spelunky.bytes
  (:use [clojure.string :only [lower-case]])
  (:import [java.nio ByteBuffer]))


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