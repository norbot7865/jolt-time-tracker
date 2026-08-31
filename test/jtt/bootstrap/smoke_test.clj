(ns jtt.bootstrap.smoke-test
  (:require [clojure.test :refer [deftest is]]
            [jtt.bootstrap.smoke :as smoke]))

(deftest deterministic-output-is-portable
  (is (= "jtt-toolchain:42" (smoke/deterministic-output))))
