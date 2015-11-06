(ns cg.entities.ship
  (:require
    [cg.settings :as s]
    [cg.util :as u]
    [cg.entities.shared :as shared]
    [play-clj.core :refer :all]
    [play-clj.g2d :refer :all]
    [play-clj.g2d-physics :refer :all]
    [play-clj.math :refer :all]
    [play-clj.ui :refer :all]))

(def ship-base
  {:type :ship
   :radius 45})

(defn create!
  [screen texture & [x y a]]
  (merge texture

         ;; Engine Bits
         (shared/base-physics-entity screen x y a)

         ;; Game Logic:
         ship-base))

(def spawn! (partial shared/spawn! create!))

(def random-spawn! (partial shared/random-spawn! create!))