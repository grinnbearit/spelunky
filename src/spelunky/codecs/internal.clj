(ns spelunky.codecs.internal
  (:use [gloss.core :only [defcodec compile-frame header nil-frame]]
        [gloss.core.protocols :only [Reader Writer read-bytes write-bytes sizeof]]
        [gloss.data.bytes :only [take-contiguous-bytes byte-count]]
        [spelunky.bytes :only [hex->ints ints->hex]]))


(def timestamp
  (compile-frame :uint32-le #(/ (.getTime %) 1000) #(java.util.Date. (* 1000 %))))


(defn hex-string-be
  [frame]
  (compile-frame frame hex->ints ints->hex))


(defn hex-string-le
  [frame]
  (compile-frame frame (comp reverse hex->ints) (comp ints->hex reverse)))


;;; thanks @danneu https://github.com/ztellman/gloss/issues/27#issuecomment-27522187
(defn proxy-frame [x]
  (compile-frame nil-frame
                 identity
                 (constantly x)))


(defcodec variable-length-integer
  (header :ubyte
          #(case %
             0xfd (compile-frame :uint16-le)
             0xfe (compile-frame :uint32-le)
             0xff (compile-frame :uint64-le)
             (proxy-frame %))
          #(cond (<  % 0xfd)       %
                 (<= % 0xffff)     0xfd
                 (<= % 0xffffffff) 0xfe
                 :else             0xff)))


(deftype BufferStore [frame]
  Reader
  (read-bytes [this buf-seq]
    (if-let [size (sizeof frame)]
      (let [store (take-contiguous-bytes buf-seq size)
            [success x xs] (read-bytes frame buf-seq)]
        [success [x store] xs])
      (let [[success x xs] (read-bytes frame buf-seq)
            store (take-contiguous-bytes buf-seq
                                         (- (byte-count buf-seq)
                                            (byte-count xs)))]
        [success [x store] xs])))
  Writer
  (sizeof [this]
    (sizeof frame))
  (write-bytes [this buf val]
    (write-bytes frame buf val)))


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
