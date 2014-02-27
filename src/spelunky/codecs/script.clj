(ns spelunky.codecs.script
  (:use [clojure.set :only [map-invert]]
        [spelunky.bytes :only [hex->ints ints->hex
                               ints->bytes bytes->ints
                               bytes->short short->bytes
                               bytes->int int->bytes
                               pubkey->address pubkey-hash->address]]))


(def int->opcode
  { 0 :0                     76 :pushdata1       77 :pushdata2        78  :pushdata4
   79 :1neate                80 :reserved        81 :1                82  :2
   83 :3                     84 :4               85 :5                86  :6
   87 :7                     88 :8               89 :9                90  :10
   91 :11                    92 :12              93 :13               94  :14
   95 :15                    96 :16              97 :nop              98 :ver
   99 :if                   100 :notif          101 :verif           102 :vernotif
   103 :else                104 :endif          105 :verify          106 :return
   107 :toaltstack          108 :fromaltstack   109 :2drop           110 :2dup
   111 :3dup                112 :2over          113 :2rot            114 :2swap
   115 :ifdup               116 :depth          117 :drop            118 :dup
   119 :nip                 120 :over           121 :pick            122 :roll
   123 :rot                 124 :swap           125 :tuck            126 :cat
   127 :substr              128 :left           129 :right           130 :size
   131 :invert              132 :and            133 :or              134 :xor
   135 :equal               136 :equalverify    137 :reserved1       138 :reserved2
   139 :1add                140 :1sub           141 :2mul            142 :2div
   143 :negate              144 :abs            145 :not             146 :0notequal
   147 :add                 148 :sub            149 :mul             150 :div
   151 :mod                 152 :lshift         153 :rshift          154 :booland
   155 :boolor              156 :numequal       157 :numequalverify  158 :numnotequal
   159 :lessthan            160 :greaterthan    161 :lessthanorequal 162 :greaterthanorequal
   163 :min                 164 :max            165 :within          166 :ripemd160
   167 :sha1                168 :sha256         169 :hash160         170 :hash256
   171 :codeseparator       172 :checksig       173 :checksigverify  174 :checkmultisig
   175 :checkmultisigverify 176 :nop1           177 :nop2            178 :nop3
   179 :nop4                180 :nop5           181 :nop6            182 :nop7
   183 :nop8                184 :nop9           185 :nop10           253 :pubkeyhash
   254 :pubkey              255 :invalidopcode})


(def opcode->int
  (map-invert int->opcode))


(defmulti decode-int
  (fn [code _]
    (cond (<= 1 code 75)
          :pushdata

          (<= 76 code 78)
          (int->opcode code)

          (contains? int->opcode code)
          :opcode)))


(defmulti encode-op
  (fn [op _]
    op))


(defmethod decode-int :pushdata
  [code unparsed]
  [[:pushdata (ints->hex (take code unparsed))]
   (drop code unparsed)])


(defmethod encode-op :pushdata
  [_ [data & unparsed]]
  [(conj (hex->ints data) (quot (count data) 2))
   unparsed])


(defmethod decode-int :pushdata1
  [_ unparsed]
  (let [size (first unparsed)]
    [[:pushdata1 (ints->hex (take size (rest unparsed)))]
     (drop size (rest unparsed))]))


(defmethod encode-op :pushdata1
  [opcode [data & unparsed]]
  [(concat [(opcode->int opcode) (quot (count data) 2)]
           (hex->ints data))
   unparsed])


(defmethod decode-int :pushdata2
  [_ unparsed]
  (let [size (-> (take 2 unparsed) reverse ints->bytes bytes->short)]
    [[:pushdata2 (ints->hex (take size (drop 2 unparsed)))]
     (drop size (drop 2 unparsed))]))


(defmethod encode-op :pushdata2
  [opcode [data & unparsed]]
  (let [size (quot (count data) 2)
        ints (-> size short->bytes bytes->ints reverse)]
    [(concat [(opcode->int opcode)]
             ints
             (hex->ints data))
     unparsed]))


(defmethod decode-int :pushdata4
  [_ unparsed]
  (let [size (-> (take 4 unparsed) reverse ints->bytes bytes->int)]
    [[:pushdata4 (ints->hex (take size (drop 4 unparsed)))]
     (drop size (drop 4 unparsed))]))


(defmethod encode-op :pushdata4
  [opcode [data & unparsed]]
  (let [size (quot (count data) 2)
        ints (-> size int->bytes bytes->ints reverse)]
    [(concat [(opcode->int opcode)]
             ints
             (hex->ints data))
     unparsed]))


(defmethod decode-int :opcode
  [code unparsed]
  [[(int->opcode code)] unparsed])


(defmethod decode-int :default
  [code unparsed]
  [[(ints->hex [code])] unparsed])


(defmethod encode-op :default
  [data unparsed]
  (if-let [op (opcode->int data)]
    [[op] unparsed]
    [(hex->ints data) unparsed]))


(defn decode-script
  [ints]
  (loop [parsed [] unparsed ints]
    (if (empty? unparsed)
      parsed
      (let [[tokens left] (decode-int (first unparsed) (rest unparsed))]
        (recur (reduce conj parsed tokens) left)))))


(defn encode-script
  [script]
  (loop [parsed [] unparsed script]
    (if (empty? unparsed)
      parsed
      (let [[tokens left] (encode-op (first unparsed) (rest unparsed))]
        (recur (reduce conj parsed tokens) left)))))


(defn matches?
  [pattern script]
  (letfn [(match? [matcher token]
            (if (keyword? matcher)
              (= token matcher)
              (matcher token)))]

    (and (= (count script) (count pattern))
         (every? (fn [[m t]] (match? m t)) (map list pattern script)))))


(let [pushdata? #{:pushdata :pushdata1 :pushdata2 :pushdata4}
      hex->bytes (comp ints->bytes hex->ints)]
  (defn extract-address
    "Extracts a bitcoin address from the output script"
    [script]
    (cond (matches? [pushdata? string? :checksig]
                    script)
          (pubkey->address (hex->bytes (nth script 1)))

          (matches? [:dup :hash160 pushdata? string? :equalverify :checksig]
                    script)
          (pubkey-hash->address (hex->bytes (nth script 3)))

          (matches? [pushdata? string? :drop pushdata? string? :checksig]
                    script)
          (pubkey->address (hex->bytes (nth script 4)))

          :else
          nil)))
