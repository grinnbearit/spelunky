(ns spelunky.codecs
  (:use [gloss.core :only [defcodec ordered-map compile-frame]]
        [spelunky.bytes :only [hex->ints ints->hex]]))


(def timestamp
  (compile-frame :uint32-le #(/ (.getTime %) 1000) #(java.util.Date. (* 1000 %))))


(defn hex-string-be
  [frame]
  (compile-frame frame hex->ints ints->hex))


(defn hex-string-le
  [frame]
  (compile-frame frame (comp reverse hex->ints) (comp ints->hex reverse)))


(defcodec block-header
  (ordered-map
   :magic-number :uint32-le             ; block separator
   :length :uint32-le))                 ; total length of the block


(defcodec block
  (ordered-map
   :version :uint32-le                  ; so far its 1
   :timestamp timestamp                 ; the creation time of this block
   :prev-block (-> (repeat 32 :ubyte)   ; hash of the previous block
                   hex-string-le)
   :merkle-root (-> (repeat 32 :ubyte)  ; the merkle-tree root hash, see http://en.wikipedia.org/wiki/Merkle_tree
                    hex-string-le)
   :bits :uint32-le                     ; target difficulty, see https://en.bitcoin.it/wiki/Target
   :nonce :uint32-le))                  ; the lucky nonce, https://en.bitcoin.it/wiki/Nonce
