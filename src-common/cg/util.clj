(ns cg.util
  (:require
    [cg.settings :as s]
    [play-clj.core :refer :all]
    [play-clj.g2d :refer :all]
    [play-clj.g2d-physics :refer :all]
    [play-clj.math :refer :all]
    [play-clj.ui :refer :all]
    [cg.entities.roid :as roid]))

;; DRAW THE ENTITY FROM THE MIDDLE
(defn trans-pos [_ x]
  (- x s/half-sprite))

(defn flip-y-axis [screen y]
  (- (height screen) y))

(defn x-rand [screen ciel]
  (trans-pos screen (rand-int ciel)))

(defn y-rand [screen ciel]
  (trans-pos screen (rand-int ciel)))

(defn random-point [screen]
  [(x-rand screen (width screen))
   (y-rand screen (height screen))])

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

(defn speed []
  (case (rand-int 5)
    0 (assoc roid/roid-base :speed 0.2)
    1 (assoc roid/roid-base :speed 0.4)
    2 (assoc roid/roid-base :speed 0.6)
    3 (assoc roid/roid-base :speed 0.8)
    4 (assoc roid/roid-base :speed 1)))