(ns cg.entities.shared
  (:require
    [cg.settings :as s]
    [cg.util :as u]
    [play-clj.core :refer :all]
    [play-clj.g2d :refer :all]
    [play-clj.g2d-physics :refer :all]
    [play-clj.math :refer :all]
    [play-clj.ui :refer :all]))

(declare make-body!)

(defn base-physics-entity [screen & [xp yp ia]]
  {:body (make-body! screen s/half-sprite)
   :height s/sprite-width
   :width s/sprite-width
   :x xp
   :y yp
   :origin-x s/half-sprite
   :origin-y s/half-sprite
   :angle ia})

(defn make-body!
  "Add a physics shape.
   In this case a circle."
  [screen radius]
  (let [body (add-body! screen (body-def :dynamic))]
    (->> (circle-shape :set-radius radius
                       :set-position (vector-2 radius radius))
         (fixture-def :density 1 :friction 0 :restitution 1 :shape)
         (body! body :create-fixture))
    body))

(defn spawn-with-physics! [spawning-function! screen texture x y a]
  (doto
    (spawning-function! screen texture x y a)
    (body-position! x y 0)
    (body! :set-linear-velocity 10 10)))


(defn spawn! [create-fun screen texture x y a]
  (spawn-with-physics!
    create-fun
    screen texture
    x y a))

(defn random-spawn! [create-fun screen texture]
  (spawn-with-physics!
    create-fun
    screen texture
    (first (u/random-point screen))
    (second (u/random-point screen))
    (rand-int 360)))