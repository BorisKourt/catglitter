(ns cg.core
  (:require
    [cg.screens.main :as ms]
    [cg.screens.ui :as us]
    [play-clj.core :refer :all]
    [play-clj.ui :refer :all]))

(declare main-screen romjam-game ui-screen)

(defscreen main-screen
           :on-show
           (fn [screen entities]
             (ms/on-show screen entities))
           :on-render
           (fn [screen entities]
             (ms/on-render screen entities))
           ; the screen was replaced
           :on-hide
           (fn [screen entities]
             (ms/on-hide screen entities))
           ; finger/mouse depressed
           :on-touch-up
           (fn [screen entities]
             (ms/on-touch-up screen entities)))

(defscreen ui-screen
           :on-show
           (fn [screen entities]
             (us/on-show screen entities))
           :on-render
           (fn [screen entities]
             (us/on-render screen entities)))

(defn on-create-game [this]
  (set-screen! this main-screen ui-screen))

(defgame cg-game
         :on-create on-create-game)
