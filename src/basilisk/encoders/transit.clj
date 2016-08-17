(ns basilisk.encoders.transit
  "This namespace contains several functions to encode and decode data
  from and to transit format."
  (:require [cognitect.transit :as t])
  (:import  [java.io ByteArrayInputStream ByteArrayOutputStream]))


(defn decode
  "Parse the given transit encoded data and return the corresponding
  clojure data structure"
  ([data]
   (decode data :json))

  ([data type]
   (let [in (ByteArrayInputStream. (.toByteArray data))
         reader (t/reader in type)]
     (t/read reader))))

(defn encode
  "Encode the given data to given internal type (default :json)."
  ([data]
   (encode data :json))

  ([data type]
   (let [out (ByteArrayOutputStream. 4096)
         writer (t/writer out type)]
     (t/write writer data)
     out)))
