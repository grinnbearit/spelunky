(ns spelunky.codecs
  ^{:doc (str "References:"
              "http://james.lab6.com/2012/01/12/bitcoin-285-bytes-that-changed-the-world/"
              "https://en.bitcoin.it/wiki/Protocol_specification"
              "http://2.bp.blogspot.com/-DaJcdsyqQSs/UsiTXNHP-0I/AAAAAAAATC0/kiFRowh-J18/s1600/blockchain.png"
              "https://blockchain.info/block/000000000019d6689c085ae165831e934ff763ae46a2a6c172b3f1b60a8ce26f")}
  (:use [gloss.core :only [defcodec ordered-map compile-frame repeated]]
        [spelunky.bytes :only [bitcoin-hash]]
        [spelunky.codecs.internal :only [hex-string-le variable-length-integer buffer-store timestamp]]))


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
  (buffer-store
   (ordered-map
    :version :uint32-le                                         ; so far its 1
    :inputs (repeated input :prefix variable-length-integer)    ; list of inputs
    :outputs (repeated output :prefix variable-length-integer)  ; list of outputs
    :lock-time :uint32-le)                                      ; so far its 0
   (fn [val] (dissoc val :hash))
   (fn [val buf] (assoc val :hash (bitcoin-hash buf)))))


(defcodec block-header
  (buffer-store
   (ordered-map
    :version :uint32-le                                         ; so far its 1
    :timestamp timestamp                                        ; the creation time of this block
    :prev-block (hex-string-le (repeat 32 :ubyte))              ; hash of the previous block
    :merkle-root (hex-string-le (repeat 32 :ubyte))             ; the merkle-tree root hash, see http://en.wikipedia.org/wiki/Merkle_tree
    :bits :uint32-le                                            ; target difficulty, see https://en.bitcoin.it/wiki/Target
    :nonce :uint32-le)                                          ; the lucky nonce, https://en.bitcoin.it/wiki/Nonce
   (fn [val] (dissoc val :hash))
   (fn [val buf] (assoc val :hash (bitcoin-hash buf)))))


(defcodec block
  (compile-frame
   [block-header (repeated txn :prefix variable-length-integer)]
   (fn [val] [(dissoc val :txns) (:txns val)])
   (fn [[bh txns]] (assoc bh :txns txns))))
