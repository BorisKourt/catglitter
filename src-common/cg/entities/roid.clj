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
  {:type      :roid
   :attached? false
   :resource-type :diamond
   :resource-amount 10})

(defn create!
  [screen texture & [x y a]]
  (merge texture

         ;; Engine Bits
         (shared/base-physics-entity screen x y a)

         ;; Game Logic:
         (assoc roid-base :resource (inc (rand-int 9)))))

(def spawn! (partial shared/spawn! create!))

(def random-spawn! (partial shared/random-spawn! create!))