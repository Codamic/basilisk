(ns basilisk.loader)

(import (java.io File)
        (java.net URL URLClassLoader)
        (java.lang.reflect Method))

(defn add-to-cp [#^String jarpath]
  (let [#^URL url (.. (File. jarpath) toURI toURL)
        url-ldr-cls (. (URLClassLoader. (into-array URL [])) getClass)
        arr-cls (into-array Class [(. url getClass)])
        arr-obj (into-array Object [url])
        #^Method mthd (. url-ldr-cls getDeclaredMethod "addURL" arr-cls)]
    (doto mthd
      (.setAccessible true)
      (.invoke (ClassLoader/getSystemClassLoader) arr-obj))
    (println (format "Added %s to classpath" jarpath))))
