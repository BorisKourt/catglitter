(defproject com.sugarbagel/catglitter "0.4.0"
  :description "Cat Glitter"

  :dependencies [[com.badlogicgames.gdx/gdx "1.7.0"]
                 [com.badlogicgames.gdx/gdx-backend-lwjgl "1.7.0"]
                 [com.badlogicgames.gdx/gdx-box2d "1.7.0"]
                 [com.badlogicgames.gdx/gdx-box2d-platform "1.7.0"
                  :classifier "natives-desktop"]
                 [com.badlogicgames.gdx/gdx-bullet "1.7.0"]
                 [com.badlogicgames.gdx/gdx-bullet-platform "1.7.0"
                  :classifier "natives-desktop"]
                 [com.badlogicgames.gdx/gdx-platform "1.7.0"
                  :classifier "natives-desktop"]
                 [org.clojure/clojure "1.7.0"]
                 [org.clojure/math.numeric-tower "0.0.4"]
                 [play-clj "0.4.7"]]

  :plugins [[lein-ancient "0.6.7"]
            [lein-gorilla "0.3.5-SNAPSHOT"]]
  
  :source-paths ["src" "src-common"]
  :javac-options ["-target" "1.8" "-source" "1.8" "-Xlint:-options"]
  :aot [cg.core.desktop-launcher]
  :main cg.core.desktop-launcher)
