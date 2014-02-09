(ns spelunky.codecs
  ^{:doc (str "References:"
              "http://james.lab6.com/2012/01/12/bitcoin-285-bytes-that-changed-the-world/"
              "https://en.bitcoin.it/wiki/Protocol_specification"
              "http://2.bp.blogspot.com/-DaJcdsyqQSs/UsiTXNHP-0I/AAAAAAAATC0/kiFRowh-J18/s1600/blockchain.png"
              "https://blockchain.info/block/000000000019d6689c085ae165831e934ff763ae46a2a6c172b3f1b60a8ce26f")}
  (:use [gloss.core :only [defcodec ordered-map compile-frame header nil-frame repeated]]
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


(defcodec blockchain-header
  (ordered-map
   :magic-number :uint32-le                                    ; block separator
   :length :uint32-le))                                        ; total length of the block


(defcodec input
  (ordered-map
   :hash (hex-string-le (repeat 32 :ubyte))                    ; the transaction hash this input unlocks
   :index :uint32-le                                           ; the index of the output in the referenced transaction
   :script (repeated :ubyte :prefix variable-length-integer)   ; https://en.bitcoin.it/wiki/Script
   :sequence :uint32-le))                                      ; currently 0xffffffff


(defcodec output
  (ordered-map
   :value :uint64-le                                           ; the amount being transferred in satoshis (1/10^8 of a bitcoin)
   :script (repeated :ubyte :prefix variable-length-integer))) ; see input script


(defcodec txn
  (ordered-map
   :version :uint32-le                                         ; so far its 1
   :inputs (repeated input :prefix variable-length-integer)    ; list of inputs
   :outputs (repeated output :prefix variable-length-integer)  ; list of outputs
   :lock-time :uint32-le))                                     ; so far its 0


(defcodec block-header
  (ordered-map
   :version :uint32-le                                         ; so far its 1
   :timestamp timestamp                                        ; the creation time of this block
   :prev-block (hex-string-le (repeat 32 :ubyte))              ; hash of the previous block
   :merkle-root (hex-string-le (repeat 32 :ubyte))             ; the merkle-tree root hash, see http://en.wikipedia.org/wiki/Merkle_tree
   :bits :uint32-le                                            ; target difficulty, see https://en.bitcoin.it/wiki/Target
   :nonce :uint32-le))                                         ; the lucky nonce, https://en.bitcoin.it/wiki/Nonce


(defcodec block
  (compile-frame
   [block-header (repeated txn :prefix variable-length-integer)]
   (fn [val] [(dissoc val :txns) (:txns val)])
   (fn [[bh txns]] (assoc bh :txns txns))))
