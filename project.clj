(defproject com.sugarbagel/catglitter "0.2.1"
  :description "Cat Glitter"
  
  :dependencies [[com.badlogicgames.gdx/gdx "1.6.5"]
                 [com.badlogicgames.gdx/gdx-backend-lwjgl "1.6.5"]
                 [com.badlogicgames.gdx/gdx-box2d "1.6.5"]
                 [com.badlogicgames.gdx/gdx-box2d-platform "1.6.5"
                  :classifier "natives-desktop"]
                 [com.badlogicgames.gdx/gdx-bullet "1.6.5"]
                 [com.badlogicgames.gdx/gdx-bullet-platform "1.6.5"
                  :classifier "natives-desktop"]
                 [com.badlogicgames.gdx/gdx-platform "1.6.5"
                  :classifier "natives-desktop"]
                 [org.clojure/clojure "1.7.0"]
                 [play-clj "0.4.7"]]

  :plugins [[lein-ancient "0.6.7"]]
  
  :source-paths ["src" "src-common"]
  :javac-options ["-target" "1.8" "-source" "1.8" "-Xlint:-options"]
  :aot [cg.core.desktop-launcher]
  :main cg.core.desktop-launcher)
