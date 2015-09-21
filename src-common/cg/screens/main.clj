(ns cg.screens.main
  (:require
    [cg.settings :as s]
    [cg.util :as u]
    [cg.entities.roid :as roid]
    [cg.entities.ship :as ship]
    [play-clj.core :refer :all]
    [play-clj.g2d :refer :all]
    [play-clj.g2d-physics :refer :all]
    [play-clj.math :refer :all]
    [play-clj.ui :refer :all]
    [clojure.pprint :refer [pprint]]))

(defn on-show [screen entities]
  (let [screen
        (update! screen
                 :renderer (stage)
                 :world (box-2d (width screen) (height screen)))

        ;; Loading the tilesheet
        sheet (texture "roidsheet.png")
        tiles (texture! sheet :split s/sprite-width s/sprite-width)
        roid-image-a (texture (aget tiles 0 0))
        roid-image-b (texture (aget tiles 0 1))
        ship-image (texture (aget tiles 0 2))

        ;; Creating initial asteroids
        roid-entities (mapv                                 ;; outputs a vector instead of a list (look at filterv)
                        (fn [n]
                          (if (even? n)
                            (roid/random-spawn! screen roid-image-a)
                            (roid/random-spawn! screen roid-image-b)))
                        (range 20))

        ;; Creating a ship entity
        ship (ship/spawn! screen ship-image (u/center-x screen) (u/center-y screen) 0)]
    (conj roid-entities ship)))

(defn on-render [screen entities]
  (clear!)
  #_(step! screen entities)                                 ;; Leave until physics are needed.
  (render! screen entities))

(defn random-move [screen entities]
  (map
    (fn [entity]
      (let [[rand-x rand-y] (u/random-point screen)
            input-x (u/x-pos screen (:input-x screen))
            input-y (u/y-pos screen (:input-y screen))]
        (case (:type entity)
          :ship (u/set-position entity input-x input-y)
          :roid (u/set-position entity rand-x rand-y))))
    entities))

(defn on-touch-up [screen entities]
  (println "\n\n :on-touch-up")
  (println (:input-x screen))                               ; the x position of the finger/mouse
  (println (:input-y screen))                               ; the y position of the finger/mouse
  (println (:pointer screen))                               ; the pointer for the event
  (println (:button screen))                                ; the mouse button that was released (see button-code)

  (random-move screen entities))

(defn on-hide
  [screen entities]
  (println "\n\n :on-hide")
  entities)