(ns cg.screens.ui
  (:require
    [play-clj.core :refer :all]
    [play-clj.ui :refer :all]))

(defn on-show [screen entities]
  (update! screen :renderer (stage))
  (label "This is a UI Label!" (color :white)))

(defn on-render [screen entities]
  (render! screen entities))