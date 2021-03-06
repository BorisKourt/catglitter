(ns cg.entities.roid
  (:require
    [cg.settings :as s]
    [cg.util :as u]
    [cg.entities.shared :as shared]
    [play-clj.core :refer :all]
    [play-clj.g2d :refer :all]
    [play-clj.g2d-physics :refer :all]
    [play-clj.math :refer :all]
    [play-clj.ui :refer :all]))

(def roid-base
  {:type :roid
   :attached? false
   :hit? false
   :bounced? false})


(defn create!
  [screen texture & [x y a]]
  (merge texture

         ;; Engine Bits
         (shared/base-physics-entity screen x y a)

         ;; Game Logic:
         (assoc roid-base :spin (rand-nth (range -0.5 0.5 0.01))
                          :x-speed (rand-nth (range 0.2 1 0.2))
                          :y-speed (rand-nth (range 0.2 1 0.2))
                          :x-dir (if (= 0 (rand-int 2)) + -)
                          :y-dir (if (= 0 (rand-int 2)) + -))))

(def random-spawn! (partial shared/random-spawn! create!))

(defn spawn-top [screen]
  [(- 0 (- s/half-sprite (u/x-rand screen (+ s/sprite-width (width screen)))))
   (u/trans-pos screen (+ s/half-sprite (height screen)))])

(defn spawn-right [screen]
  [(u/trans-pos screen (+ s/half-sprite (width screen)))
   (- 0 (- s/half-sprite (u/y-rand screen (+ s/sprite-width (height screen)))))])

(defn spawn-bottom [screen]
  [(- 0 (- s/half-sprite (u/x-rand screen (+ s/sprite-width (width screen)))))
   (u/trans-pos screen (- 0 s/half-sprite))])

(defn spawn-left [screen]
  [(u/trans-pos screen (- 0 s/half-sprite))
   (- 0 (- s/half-sprite (u/y-rand screen (+ s/sprite-width (height screen)))))])

(defn spawn-off-edge [create-fun screen texture]
  (let [spawn-pt (case (rand-int 4)
                   0 (spawn-top screen)
                   1 (spawn-right screen)
                   2 (spawn-bottom screen)
                   3 (spawn-left screen))
        new-x (first spawn-pt)
        new-y (second spawn-pt)]

    (shared/spawn-with-physics!
      create-fun
      screen texture
      new-x
      new-y

      (rand-int 360))))

(def spawn-edge! (partial spawn-off-edge create!))