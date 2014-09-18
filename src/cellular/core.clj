(ns cellular.core
  (:gen-class)
  (:require
   [clojure.repl :refer [doc source]]
   [lanterna.screen :as s]
   [clojure.core.async :as a :refer [go go-loop <! >!]]
   [cellular.ant :as ant]))

(defn -main
  [& args]
  (ant/langton-ant (s/get-screen :swing)))
