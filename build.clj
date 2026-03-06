(ns build
  (:require [clojure.tools.build.api :as b]))

(def lib 'jokenpo)
(def version "1.0.0")
(def class-dir "target/classes")
(def basis (b/create-basis {:project "deps.edn"
                            :aliases [:run/cli]}))
(def uber-file (format "target/%s-%s-cli.jar" (name lib) version))

(defn clean [_]
  (b/delete {:path "target"}))

(defn uber [_]
  (clean nil)
  (b/copy-dir {:src-dirs ["src" "resources"]
               :target-dir class-dir})
  (b/compile-clj {:basis basis
                  :src-dirs ["src"]
                  :class-dir class-dir
                  :ns-compile ['jokenpo.cli.core]})
  (b/uber {:class-dir class-dir
           :uber-file uber-file
           :basis basis
           :main 'jokenpo.cli.core}))