(ns spelunky.core
  (:use [spelunky.bytes :only [read-bytes]]
        [spelunky.codecs :only [block-header]]
        [gloss.io :only [decode]]))


(defn read-block
  [stream]
  (let [{:keys [magic-number length]} (decode block-header (read-bytes stream 8))]
    (if (not= magic-number 3652501241)
      (throw (ex-info "Malformed Header" {}))
      (read-bytes stream length))))
