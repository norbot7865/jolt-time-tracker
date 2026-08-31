(ns jtt.android.stress-test
  (:require [clojure.test :refer [deftest is]]
            [jtt.android.reducer :as reducer]
            [jtt.android.stress :as stress]))

(deftest repeated-wire-events-are-deterministic
  (is (= {:lifecycle :resumed}
         (stress/repeated-status-events reducer/reduce-event 100))))
