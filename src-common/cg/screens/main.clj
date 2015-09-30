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

        ;; Loading the spritesheet
        sheet (texture "roidsheet.png")
        tiles (texture! sheet :split s/sprite-width s/sprite-width)
        roid-image-a (texture (aget tiles 0 0))
        roid-image-b (texture (aget tiles 0 1))
        ship-image (texture (aget tiles 0 2))

        ;; Creating initial asteroids
        roid-entities (mapv                                 ;; outputs a vector instead of a list (look at filterv)
                        (fn [n]
                          (if (even? n)
                            (assoc (roid/random-spawn! screen roid-image-a) :attached? true)
                            (roid/random-spawn! screen roid-image-b)))
                        (range 200))

        ;; Creating a ship entity
        ship (ship/spawn! screen ship-image (u/center-x screen) (u/center-y screen) 0)]
    (conj roid-entities ship)))

(defn destroy-depleted [screen entities]
  (filter
    (fn [entity]
      (not (and (= :roid (:type entity))
               (>= 0 (:resource entity)))))
    entities))

(defn update-asteroid-status [screen entities]
  (map
    (fn [entity]
      (if (and (= :roid (:type entity))
               (:attached? entity))
        (assoc entity :resource (dec (:resource entity)))
        entity))
    entities))

(defn move-roids [screen entities]
  (map
    (fn [entity]
      (case (:type entity)
        :ship entity
        :roid (u/set-position entity (dec (:x entity)) (dec (:y entity)))))
    entities))

(defn destroy-offscreen [screen entities]
  (println (count entities))
  (if (> (count entities) 100)
      (->> entities
       #_(filter
         (fn [entity]
           (and (= :roid (:type entity))
                (or (>= (- 0 s/sprite-width) (:x entity))
                    (>= (- 0 s/sprite-height) (:y entity))))))
       (take 100))
      entities))

(defn possibly-asteroid [screen entities]
  (let [sheet (texture "roidsheet.png")
        tiles (texture! sheet :split s/sprite-width s/sprite-width)
        roid-image-a (texture (aget tiles 0 0))]
    (if (= 5 (rand-int 10))
      (conj entities (roid/spawn-edge! screen roid-image-a))
      entities)))

(defn on-render [screen entities]
  (clear!)
  #_(step! screen entities)                                 ;; Leave until physics are needed.
  (->> entities
       ;; all your game logic here.
       ;; (destroy-offscreen screen)
       ;; (update-asteroid-status screen)
       (move-roids screen)
       (destroy-offscreen screen)
       #_(destroy-depleted screen)
       (possibly-asteroid screen)
       (render! screen)))

#_(defn random-move [screen entities]
    (map
      (fn [entity]
        (let [[rand-x rand-y] (u/random-point screen)
              input-x (u/x-pos screen (:input-x screen))
              input-y (u/y-pos screen (:input-y screen))]
          (case (:type entity)
            :ship (u/set-position entity input-x input-y)
            :roid entity)))
      entities))

(defn click-move [screen entities]
  (map
    (fn [entity]
      (let [[rand-x rand-y] (u/random-point screen)
            input-x (u/x-pos screen (:input-x screen))
            input-y (u/y-pos-on-click screen (:input-y screen))]
        (case (:type entity)
          :ship (u/set-position entity input-x input-y)
          :roid entity)))
    entities))


(defn on-touch-up
  "It must return entities."
  [screen entities]
  (println "\n\n :on-touch-up")
  (println (:input-x screen))                               ; the x position of the finger/mouse
  (println (:input-y screen))                               ; the y position of the finger/mouse
  (println (:pointer screen))                               ; the pointer for the event
  (println (:button screen))                                ; the mouse button that was released (see button-code)

  (->> entities
       (update-asteroid-status screen)
       (click-move screen)))

(defn on-hide
  [screen entities]
  (println "\n\n :on-hide")
  entities)