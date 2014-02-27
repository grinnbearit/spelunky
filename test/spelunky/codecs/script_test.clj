(ns spelunky.codecs.script-test
  (:use [midje.sweet]
        [spelunky.test]
        [spelunky.codecs.script]))


(facts
 (decode-script [1 1 76 1 1 77 1 0 1 78 1 0 0 0 1 172 186])
 => [:pushdata "01" :pushdata1 "01" :pushdata2 "01" :pushdata4 "01" :checksig "ba"])


(facts
 (encode-script [:pushdata "01" :pushdata1 "01" :pushdata2 "01" :pushdata4 "01" :checksig "ba"])
 => [1 1 76 1 1 77 1 0 1 78 1 0 0 0 1 172 186])


(facts
 (matches? [even? odd? :x] [2 1 :x])   => true
 (matches? [even? odd? :x] [2 1 :y])   => false
 (matches? [even? odd? :x] [2 1 :x 1]) => false
 (matches? [even? odd? :x] [2 2 :x])   => false)


(facts
 (extract-address [:pushdata
                   (str "04678afdb0fe5548271967f1a67130b7"
                        "105cd6a828e03909a67962e0ea1f61de"
                        "b649f6bc3f4cef38c4f35504e51ec112"
                        "de5c384df7ba0b8d578a4c702b6bf11d"
                        "5f")
                   :checksig])
 => "1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa"

 (extract-address [:dup
                   :hash160
                   :pushdata
                   "62e907b15cbf27d5425399ebf6f0fb50ebb88f18"
                   :equalverify
                   :checksig])
 => "1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa"

 (extract-address [:pushdata
                   "00000000"
                   :drop
                   :pushdata
                   (str "04678afdb0fe5548271967f1a67130b7"
                        "105cd6a828e03909a67962e0ea1f61de"
                        "b649f6bc3f4cef38c4f35504e51ec112"
                        "de5c384df7ba0b8d578a4c702b6bf11d"
                        "5f")
                   :checksig])
 => "1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa"

 (extract-address [(str "04678afdb0fe5548271967f1a67130b7"
                        "105cd6a828e03909a67962e0ea1f61de"
                        "b649f6bc3f4cef38c4f35504e51ec112"
                        "de5c384df7ba0b8d578a4c702b6bf11d"
                        "5f")])
 => nil)
