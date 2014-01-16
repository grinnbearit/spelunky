(ns spelunky.core
  (:use [gloss.core :only [defcodec
                           header
                           ordered-map
                           compile-frame]]))


(defcodec block-header
  (ordered-map
   :magic-number :uint32-le
   :length :uint32-le))
