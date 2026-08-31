(ns jtt.bootstrap.smoke)

(defn deterministic-output []
  "jtt-toolchain:42")

(defn -main [& _]
  (println (deterministic-output)))
