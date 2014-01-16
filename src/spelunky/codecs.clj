(ns spelunky.codecs
  (:use [gloss.core :only [defcodec ordered-map]]))


(defcodec block-header
  (ordered-map
   :magic-number :uint32-le
   :length :uint32-le))
