(ns cg.util
  (:require
    [cg.settings :as s]
    [play-clj.core :refer :all]
    [play-clj.g2d :refer :all]
    [play-clj.g2d-physics :refer :all]
    [play-clj.math :refer :all]
    [play-clj.ui :refer :all]))

(defn x-pos [_ x]
  (- x s/half-sprite))

(defn y-pos [screen y]
  (- (height screen) y s/half-sprite)) ;; because y is inverted?

(defn x-rand [screen ciel]
  (x-pos screen (rand-int ciel)))

(defn y-rand [screen ciel]
  (y-pos screen (rand-int ciel)))

(defn random-point [screen]
  [(y-rand screen (height screen))
   (x-rand screen (width screen))])

(defn set-position
  "Sets the position with physics body."
  [entity x y]
  (doto
    (assoc entity :x x :y y)
    (body-position! x y (:angle entity))))

(defn center-x [screen]
  (- (/ (width screen) 2) s/half-sprite))

(defn center-y [screen]
  (- (/ (height screen) 2) s/half-sprite))