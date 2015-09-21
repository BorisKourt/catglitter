(ns cg.core.desktop-launcher
  (:require [cg.core :refer :all])
  (:import [com.badlogic.gdx.backends.lwjgl LwjglApplication]
           [org.lwjgl.input Keyboard])
  (:gen-class))

(defn -main
  []
  (LwjglApplication. cg-game "CAT GLITTER!" 1280 800)
  (Keyboard/enableRepeatEvents true))
