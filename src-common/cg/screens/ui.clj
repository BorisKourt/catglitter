(ns cg.screens.ui
  (:require
    [play-clj.core :refer :all]
    [play-clj.ui :refer :all]))

(defn on-show [screen entities]
  (let [screen (update! screen :renderer (stage))]
    [(assoc (label "C-Type:" (color :white)) :type :c)
     (assoc (label "S-Type:" (color :white)) :type :s)
     (assoc (label "M-Type:" (color :white)) :type :m)
     (assoc (label "2" (color :white)) :type :c-val)
     (assoc (label "2" (color :white)) :type :s-val)
     (assoc (label "2" (color :white)) :type :m-val)]))

(defn draw-labels [screen entities]
  (let [draw-x-center (/ (width screen) 2)
        draw-y (- (height screen) 50)
        offset 55]
    (map (fn [entity]
           (case (:type entity)
             :c (assoc entity :x (/ draw-x-center 2) :y draw-y)
             :c-val (assoc entity :x (+ (/ draw-x-center 2) offset) :y draw-y)
             :s (assoc entity :x draw-x-center :y draw-y)
             :s-val (assoc entity :x (+ offset draw-x-center) :y draw-y)
             :m (assoc entity :x (+ draw-x-center (/ draw-x-center 2)) :y draw-y)
             :m-val (assoc entity :x (+ draw-x-center (/ draw-x-center 2) offset) :y draw-y)
             entity)) entities)))

(defn on-render [screen entities]
  (->>
    entities
    (draw-labels screen)
        (render! screen)))
