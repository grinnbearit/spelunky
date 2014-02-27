(ns spelunky.bytes-test
  (:use midje.sweet
        spelunky.test
        spelunky.bytes)
  (:import [java.io ByteArrayInputStream]))


(facts
 (ints->hex [222 173 190 239]) => "deadbeef"
 (hex->ints "deadbeef") => [222 173 190 239])


(facts
 (bytes->hex (ints->bytes [255 1 127])) => "ff017f"
 (bytes->ints (hex->bytes "ff017f")) => [255 1 127])


(facts
 (bytes->short (hex->bytes "0001")) => 1
 (bytes->short (hex->bytes "0100")) => 256)


(facts
 (bytes->int (hex->bytes "00000001")) => 1
 (bytes->int (hex->bytes "00010000")) => 65536)


(facts
 (bytes->hex (short->bytes 1))   => "0001"
 (bytes->hex (short->bytes 256)) => "0100")


(facts
 (bytes->hex (int->bytes 1))     => "00000001"
 (bytes->hex (int->bytes 65536)) => "00010000")


(facts
 (bytes->ints (read-bytes (ByteArrayInputStream. (hex->bytes "000100")) 4)) => [0 1 0 0]
 (bytes->hex (read-bytes (ByteArrayInputStream. (hex->bytes "abcdef")) 3)) => "abcdef"
 (bytes->hex (read-bytes (ByteArrayInputStream. (hex->bytes "ffaabbccddeeff")) 3)) => "ffaabb")


(facts
 (-> (hex->bytes "6465616462656566")
     sha256
     bytes->hex)
 => "2baf1f40105d9501fe319a8ec463fdf4325a2a5df445adf3f572f626253678c9"

 (-> (hex->bytes "2baf1f40105d9501fe319a8ec463fdf4325a2a5df445adf3f572f626253678c9")
     sha256
     bytes->hex)
 => "e107944e77a688feae4c2d4db5951923812dd0f72026a11168104ee1b248f8a9")


(facts
 (-> (hex->bytes "6465616462656566")
     double-sha256
     bytes->hex)
 => "e107944e77a688feae4c2d4db5951923812dd0f72026a11168104ee1b248f8a9")


(facts
 (-> (hex->bytes "6465616462656566")
     ripemd160
     bytes->hex)
 => "0c08020a7343ef0868a8b1874ba36d1b646e62e7")


(facts
 (-> (hex->bytes "6465616462656566")
     bitcoin-hash)
 => "a9f848b2e14e106811a12620f7d02d81231995b54d2d4caefe88a6774e9407e1")


(facts
 (-> (hex->bytes "6465616462656566")
     base58-encode)
 => "Hny6RY5JvhT"


 (-> (hex->bytes "0001")
     base58-encode)
 => "12")


(facts
 (-> (base58-decode "Hny6RY5JvhT")
     bytes->hex)
 => "6465616462656566"


 (-> (base58-decode "12")
     bytes->hex)
 => "0001")


(facts
 (pubkey-hash->address (hex->bytes "6465616462656566"))
 => "12ttJFtXekxnC9RMBi")


(facts
 (-> (address->pubkey-hash "12ttJFtXekxnC9RMBi")
     bytes->hex)
 => "6465616462656566")


(facts
 (-> (hex->bytes "6465616462656566")
     pubkey->address)
 => "1FUfmBUhCM6Yeh4qZ3noes54CDLngiJHS8")
