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
    [clojure.math.numeric-tower :as math]
    [clojure.pprint :refer [pprint]]))

(defn play-music []
  (music "musicloop.wav"))

(defn on-show [screen entities]
  (let [screen (update! screen
                        :renderer (stage)
                        :world (box-2d (width screen) (height screen))
                        :texture-ship (texture! (texture "ship.png") :split s/sprite-width s/sprite-width)
                        :texture-roid (texture! (texture "roidsheet.png") :split s/sprite-width s/sprite-width)
                        :texture-cat (texture! (texture "cat.png") :split s/sprite-width s/sprite-width)
                        :ship-x 20
                        :ship-y 20
                        :new-music (play-music))

        ;; Loading the spritesheet
        tiles (:texture-ship screen)
        ship-image (texture (aget tiles 0 6))

        ;; Creating a ship entity
        ship (ship/spawn! screen ship-image (:ship-x screen) (:ship-y screen) 135)]
    (update! screen :blob [ship])
    [ship]))


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
         (if (and (u/is-type? :roid entity)
                  (false? (:attached? entity)))
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


(defn boundary [check entity]
  (when (not= check entity)
    (and (and (> (+ (:x check) (:radius entity) (:radius check) -15)
                 (:x entity))
              (< (- (:x check) (:radius entity) (:radius check) 15)
                 (:x entity)))
         (and (> (+ (:y check) (:radius entity) (:radius check) -15)
                 (:y entity))
              (< (- (:y check) (:radius entity) (:radius check) 15)
                 (:y entity))))))

(defn check-attached
  "Check if close enough to ship/attached roid position and attach!"
  [screen entities]
  (mapv
    (fn [entity]

      (if (and (not (:attached? entity))
               (:hit? entity)
               (u/is-type? :roid entity))

        (if (first (filterv #(boundary % entity) (:blob screen)))
          (let [new-entity (assoc entity :x (math/round (:x entity))
                                         :y (math/round (:y entity))
                                         :attached? true
                                         :speed-x 0 :speed-y 0 :spin 0)]
            (update! screen :blob (conj (:blob screen) new-entity))
            new-entity)

          entity)
        entity))

    entities))

(defn reel-in
  "Changes the direction and speed of asteroid to aim towards the ship"
  [screen entities]
  (let [{ix :x iy :y} (first (filter #(= :ship (:type %)) entities))]
    (map
      (fn [entity]
        (if (and (= :roid (:type entity))
                 (:hit? entity)
                 (not (:attached? entity)))
          (let [x-speed (/ (math/abs (- ix (:x entity))) 100)
                y-speed (/ (math/abs (- iy (:y entity))) 100)
                x-dir (if (> ix (:x entity)) + -)
                y-dir (if (> iy (:y entity)) + -)]
            (assoc entity :x-speed x-speed :y-speed y-speed :x-dir x-dir :y-dir y-dir))
          entity))
      entities)))


(defn which? [n]
  (cond
    (< n 76) 0
    (and (>= n 76) (< n 92)) 1
    (>= n 92) 2
    :else 0))

(defn possibly-roid
  "Spawns roids with different texture, chance of occurance, and radius"
  [screen entities]
  (if (= 5 (rand-int 30))
    (let [types [:c :s :m]
          num-type (which? (rand-int 100))
          type (nth types num-type)
          size (which? (rand-int 100))
          resource-by-range [10 40 70]
          roid-image (texture (aget (:texture-roid screen) num-type (- 2 size)))]
      (conj entities
            (assoc (roid/spawn-edge! screen roid-image)
              :roid-type type
              :radius (nth s/roid-rad size)
              :ore (+ (rand-int 20) (nth resource-by-range size)))))
    entities))

(defn bounce-roid [screen entities]
  (mapv
    (fn [entity]

      (if (and (not (:attached? entity))
               (not (:hit? entity))
               (u/is-type? :roid entity))

        (if (first (filter #(boundary % entity) (:blob screen)))

          (if (not (:bounced? entity))
            (assoc entity :x-dir (if (= - (:x-dir entity)) + -)
                          :y-dir (if (= - (:y-dir entity)) + -)
                          :x-speed (* (:x-speed entity) 2)
                          :y-speed (* (:y-speed entity) 2)
                          :bounced? true
                          :spin (* (:spin entity) -4))
            (assoc entity :x-dir (if (= - (:x-dir entity)) + -)
                          :y-dir (if (= - (:y-dir entity)) + -)
                          :spin (* (:spin entity) -1)))
          entity)

        entity))

    entities))

(defn on-render
  "Continuously draws at 60 fps"
  [screen entities]
  (clear!)
  #_(step! screen entities)
  (->> entities
       ;; all your game logic here.
 ;;      (music "musicloop.wav" :play)
       (possibly-roid screen)
       (check-attached screen)
       (reel-in screen)
       (rand-direction screen)
       (bounce-roid screen)
       (destroy-offscreen screen)
       (render! screen)))

(defn process-hit
  "loops through entity type :roid and detects if mouse hit it, then selects that roid"
  [screen x y entities]
  (map
    (fn [entity]
      (if (and (u/is-type? :roid entity)
               (<= (math/abs (- x (+ s/half-sprite (:x entity)))) s/half-sprite)
               (<= (math/abs (- y (+ s/half-sprite (:y entity)))) s/half-sprite))
        (do (println "hit")
            (println (:ore entity))
            (println (:radius entity))
            (pprint (map (fn [e] (str (:x e) " <> " (:y e))) (:blob screen)))
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

    (pprint (:blob entities))
    (->> entities
         (process-hit screen new-x new-y))))

(defn on-hide
  [screen entities]
  (println "\n\n :on-hide")
  entities)
