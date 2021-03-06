(ns cellular.ant
  (:require
   [clojure.repl :refer [doc source]]
   [lanterna.screen :as s]
   [clojure.core.async :as a :refer [go go-loop <! >!]]))

(def scr (s/get-screen :swing))

(defn fill-screen
  ([scr] (fill-screen scr "#"))
  ([scr c]
     (let [[width height] (s/get-size scr)]
       (doseq [y (range height)]
         (s/put-string scr 0 y (apply str (repeat width c)))))))

(defn turn
  [dir face]
  (case dir
    :right (case face
             :up :right
             :right :down
             :down :left
             :left :up)
    :left (case face
            :up :left
            :left :down
            :down :right
            :right :up)
    dir))

(defn dir
  [b]
  (if (zero? b)
    :left
    :right))

(defn out-of-bounds?
  [scr [x y]]
  (let [[width height] (s/get-size scr)]
    (not-every? false? [(neg? x) (> x (dec width))
                        (neg? y) (> y (dec height))])))

(defn foreward
  [scr [x y :as coords] face]
  (let [new-coords (case face
                     :up [x (dec y)]
                     :down [x (inc y)]
                     :left [(dec x) y ]
                     :right [(inc x) y]
                     coords)]
    (if (out-of-bounds? scr new-coords)
      coords
      new-coords)))

(defn flip
  [cell]
  (bit-flip cell 0))

(defn face-char
  [face]
  (case face
    :up "^"
    :left "<"
    :right ">"
    :down "v"))

(defn color
  [x]
  (if (zero? x)
    :black
    :white))

(defn print-grid
  [scr grid]
  (doseq [y (range (count grid))
          x (range (count (first grid)))
          :let [cell (get-in grid [y x])]]
    (s/put-string scr x y "#" {:bg (color cell)
                               :fg (color cell)})))

(defn langton-ant
  [scr]
  (let [[width height] (s/get-size scr)
        grid-init (mapv (fn [_] (into [] (repeat width 1)))
                        (range height))]
    (s/start scr)

    (loop [grid grid-init
           ant {:coords [(int (/ width 2))
                         (int (/ height 2))]
                :face :up}]
      

      (print-grid scr grid)

      (let [[x y] (:coords ant)
            cell (get-in grid [y x])]
        
        (s/put-string scr x y
                      (face-char (:face ant))
                      {:bg (color (get-in grid [y x]))
                       :fg :red})

        (s/redraw scr)

        (if-not (= (s/get-key scr) :escape)
          (let [d (turn (dir cell) (:face ant))]
            (recur @(future (Thread/sleep 120) (update-in grid
                                                 [y x] flip))
                   (assoc ant
                     :coords (foreward scr (:coords ant) d)
                     :face d)))
          
          (s/stop scr))))))
