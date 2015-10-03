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


;; FIRST RENDERED ON START UP ONCE
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
                        (range 4))

        ;; Creating a ship entity
        ship (ship/spawn! screen ship-image (u/center-x screen) (u/center-y screen) 0)]
    (conj roid-entities ship)))

#_(defn destroy-depleted [screen entities]
    (filter
      (fn [entity]
        (not (and (= :roid (:type entity))
                  (>= 0 (:resource entity)))))
      entities))


#_(defn update-asteroid-status [screen entities]
    (map
      (fn [entity]
        (if (and (= :roid (:type entity))
                 (:attached? entity))
          (assoc entity :resource (dec (:resource entity)))
          entity))
      entities))

(defn move-roids [entities]
  (map (fn [entity]
      (case (:type entity)
        :ship entity
        :roid (u/set-position entity (dec (:x entity)) (dec (:y entity)))))
    entities))

(defn rand-direction [entities]
  (map (fn [entity]
         (let [x-speed (:x-speed entity)
               x-dir (:x-dir entity)
               y-speed (:y-speed entity)
               y-dir (:y-dir entity)]
           (u/set-position entity (x-dir (:x entity) x-speed) (y-dir (:y entity) y-speed))))
       entities))

;; GARBAGE COLLECTION - DELETES ASTEROID ENTITIES OUTSIDE SCREEN BOUNDS
(defn destroy-offscreen [screen entities]
  (let [new-entities (->> entities
                          (remove
                            (fn [entity]
                              (and (= :roid (:type entity))
                                   (or (>= (- 0 (* 4 s/sprite-width)) (:x entity))
                                       (>= (- 0 (* 4 s/sprite-width)) (:y entity)))))))]
    #_(println "entities removed" (- (count entities) (count new-entities)))
    new-entities))

(defn possibly-asteroid [screen entities]
  (let [sheet (texture "roidsheet.png")
        tiles (texture! sheet :split s/sprite-width s/sprite-width)
        roid-image-a (texture (aget tiles 0 0))]
    (if (= 5 (rand-int 200))
      (conj entities (roid/spawn-edge! screen roid-image-a))
      entities)))

(defn on-render [screen entities]
  (clear!)
  #_(step! screen entities)                                 ;; Leave until physics are needed.
  (->> entities
       ;; all your game logic here.
       ;; (update-asteroid-status screen)
       (move-roids)
       #_(rand-direction)
       (destroy-offscreen screen)
       #_(destroy-depleted screen)
       (possibly-asteroid screen)
       ((fn [entities]
          (println "hit????" (some :hit? entities))
          entities))
       (render! screen)))

(defn random-move [screen entities]
  (map
    (fn [entity]
      (let [[rand-x rand-y] (u/random-point screen)
            input-x (u/trans-pos screen (:input-x screen))
            input-y (u/trans-pos screen (:input-y screen))]
        (case (:type entity)
          :ship (u/set-position entity input-x input-y)
          :roid entity)))
    entities))


;; DRAW SHIP AT MOUSE CLICK - NOTE CLICK PTS (0,0) ARE FROM TOP LEFT, DRAW (0,0) IS FROM BOTTOM LEFT
(defn click-move [screen entities]
  (map
    (fn [entity]
      (let [input-x (u/trans-pos screen (:input-x screen))
            input-y (u/trans-pos screen (u/flip-y-axis screen (:input-y screen)))]
        (case (:type entity)
          :ship (u/set-position entity input-x input-y)
          :roid entity)))
    entities))

(defn hit-roid? [x y entity]
  (and (= :roid (:type entity))
       (<= (Math/abs (- x (+ s/half-sprite (:x entity)))) s/half-sprite)
       (<= (Math/abs (- y (+ s/half-sprite (:y entity)))) s/half-sprite)
       ))

(defn process-hit [x y entities]
  (map
    (fn [entity]
      (if (hit-roid? x y entity)
        (assoc entity :hit? true)
        (assoc entity :hit? false)))
    entities))

;; WHEN CLICKED/TOUCHED ON SCREEN
(defn on-touch-up
  "It must return entities."
  [screen entities]
  (let [new-y (u/flip-y-axis screen (:input-y screen))
        new-x (:input-x screen)]
    (println "\n\n :on-touch-up")
    (println new-x)                             ; the x position of the finger/mouse
    (println new-y)                                         ; the y position of the finger/mouse
    (println (:pointer screen))                             ; the pointer for the event
    (println (:button screen))                              ; the mouse button that was released (see button-code)
    (when-let [hit (hit-roid? new-x new-y entities)]
      (println "HITTTTT!!!!!!" hit))
    (->> entities
         #_(update-asteroid-status screen)
         (click-move screen)
         (process-hit new-x new-y))))

(defn on-hide
  [screen entities]
  (println "\n\n :on-hide")
  entities)