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
                 :world (box-2d (width screen) (height screen))
                 :texture-ship (texture! (texture "ship.png") :split s/sprite-width s/sprite-width)
                 :texture-roid (texture! (texture "roidsheet.png") :split s/sprite-width s/sprite-width)
                 :texture-cat (texture! (texture "cat.png") :split s/sprite-width s/sprite-width))

        ;; Loading the spritesheet

        tiles (:texture-ship screen)
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
        :roid (u/set-position entity
                              (- (:x entity) 0.5)
                              (- (:y entity) 0.5)
                              (+ (:angle entity) (:spin entity)))))
    entities))

(defn rand-direction
  "Takes the random direction and speed asteroids spawn with and moves them"
  [screen entities]
  (map (fn [entity]
         (if (u/is-type? :roid entity)
           (let [x-speed (:x-speed entity)
                 x-dir (:x-dir entity)
                 y-speed (:y-speed entity)
                 y-dir (:y-dir entity)]
             (u/set-position entity
                             (x-dir (:x entity) x-speed)
                             (y-dir (:y entity) y-speed)
                             (+ (:angle entity) (:spin entity))))
           entity))
       entities))


(defn destroy-offscreen
  "Thanks oakes!"
  [screen entities]
  (remove
    (fn [entity]
      (let [e-size (* 4 s/sprite-width)
            e-rect (rectangle (:x entity) (:y entity) e-size e-size)
            screen-rect (rectangle 0 0 (game :width) (game :height))]
        (and (= :roid (:type entity))
             (not (rectangle! screen-rect :overlaps e-rect)))))
    entities))

(defn check-attached
  "Check if close enough to ship x y attach!"
  [screen entites]
  )

(defn reel-in
  "Changes the direction and speed of asteroid to aim towards the ship"
  [screen entities]
  (let [{ix :x iy :y} (first (filter #(= :ship (:type %)) entities))]
    ;(println "weee" ix ", " iy)
    ;(println entities)
    (map
      (fn [entity]
        (if (and (= :roid (:type entity))
                 (true? (:hit? entity))
                 (false? (:attached? entity)))
          (let [x-speed (/ (Math/abs (- ix (:x entity))) 100)
                y-speed (/ (Math/abs (- iy (:y entity))) 100)
                x-dir (if (> ix (:x entity))
                        +
                        -)
                y-dir (if (> iy (:y entity))
                        +
                        -)]
            (assoc entity :x-speed x-speed :y-speed y-speed :x-dir x-dir :y-dir y-dir))
          entity))
      entities)))

(defn possibly-asteroid [screen entities]
  (let [roid-image (texture (aget (:texture-roid screen) (rand-int 6) (rand-int 3)))]
    (if (= 5 (rand-int 10))
      (do #_(println "Entities: " (count entities))
        (conj entities (roid/spawn-edge! screen roid-image)))
      entities)))

(defn possibly-cat [screen entities]
  (let [cat (texture (aget (:texture-cat screen) 0 0))]
    (if (= 5 (rand-int 1000))
      (conj entities (roid/spawn-edge! screen cat))
      entities)))

(defn on-render
  "Continuously draws at 60 fps"
  [screen entities]
  (clear!)
  #_(step! screen entities)
  (->> entities
       ;; all your game logic here.
       (reel-in screen)
       #_(check-attached screen)
       (rand-direction screen)
       (destroy-offscreen screen)
       (possibly-cat screen)
       (possibly-asteroid screen)
       (render! screen)))

(defn random-move [screen entities]
  (map
    (fn [entity]
      (let [input-x (u/trans-pos screen (:input-x screen))
            input-y (u/trans-pos screen (:input-y screen))]
        (case (:type entity)
          :ship (u/set-position entity input-x input-y 0)
          :roid entity)))
    entities))

(defn click-move
  "Draw ship at mouse click - note click pts (0,0) are from top left, draw (0,0) is from bottom left"
  [screen entities]
  (->> entities
       (map
         (fn [entity]
           (if (u/is-type? :ship entity)
             (let [input-x (u/trans-pos screen (:input-x screen))
                   input-y (u/trans-pos screen (u/flip-y-axis screen (:input-y screen)))]
               (u/set-position entity input-x input-y 0))
             entity)))))

(defn process-hit
  "loops through entity type :roid and detects if mouse hit it, then selects that roid"
  [x y entities]
  (map
    (fn [entity]
      (if (and (u/is-type? :roid entity)
               (<= (Math/abs (- x (+ s/half-sprite (:x entity)))) s/half-sprite)
               (<= (Math/abs (- y (+ s/half-sprite (:y entity)))) s/half-sprite))
        (do (println "hit")
            (assoc entity :hit? true))
        entity))
    entities))

(defn on-touch-up
  "When clicked/touched on screen. It must return entities."
  [screen entities]
  (let [new-y (u/flip-y-axis screen (:input-y screen))
        new-x (:input-x screen)]
    (println "\n\n :on-touch-up")
    (println new-x)                                         ; the x position of the finger/mouse
    (println new-y)                                         ; the y position of the finger/mouse
    (println (:pointer screen))                             ; the pointer for the event
    (println (:button screen))                              ; the mouse button that was released (see button-code)
    (->> entities
         (process-hit new-x new-y))))

(defn on-hide
  [screen entities]
  (println "\n\n :on-hide")
  entities)