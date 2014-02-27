(ns spelunky.bytes
  (:require [gloss.core.protocols :as p])
  (:use [clojure.string :only [lower-case]])
  (:import [org.bouncycastle.jce.provider BouncyCastleProvider]
           [java.nio ByteBuffer]
           [java.security MessageDigest Security]))


;;; install bouncy castle provider
(Security/addProvider (BouncyCastleProvider.))


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


(defn ripemd160
  [barray]
  (let [digest (MessageDigest/getInstance "RIPEMD160", "BC")]
    (.update digest barray)
    (.digest digest)))


(defn bitcoin-hash
  "Returns the double-sha256 of the bytes as a little-endian hex-string"
  [bytes]
  (-> bytes double-sha256 bytes->ints reverse ints->hex))


(let [chars (vec "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz")]
  (defn base58-encode
    [bytes]
    (let [zero-count (count (take-while #{0} bytes))]
      (loop [n (bigint (byte-array bytes)) base58 ()]
        (if (zero? n)
          (apply str (concat (repeat zero-count \1) base58))
          (recur (quot n 58) (conj base58 (chars (rem n 58))))))))


  (let [ints (zipmap chars (range))]
    (defn- revert-base
      [string]
      (first
       (reduce (fn [[acc pow] x]
                 [(+ acc (* pow x)) (* pow 58)])
               [(bigint 0) (bigint 1)]
               (reverse (map ints string))))))


  (defn base58-decode
    [string]
    (let [zero-count (count (take-while #{\1} string))]
      (->> (revert-base (drop-while #{\1} string))
           biginteger
           .toByteArray
           (concat (repeat zero-count (byte 0)))
           byte-array))))


(defn pubkey-hash->address
  [bytes]
  (let [versioned (conj (seq bytes) (byte 0))
        hash (double-sha256 (byte-array versioned))]
    (base58-encode (byte-array (concat versioned (take 4 hash))))))


(defn address->pubkey-hash
  [address]
  (let [decoded (base58-decode address)]
    (->> decoded
         (take (- (count decoded) 4))
         (drop 1)
         byte-array)))


(defn pubkey->address
  [bytes]
  (pubkey-hash->address (ripemd160 (sha256 bytes))))
