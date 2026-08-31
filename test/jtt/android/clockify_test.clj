(ns jtt.android.clockify-test
  (:require [clojure.test :refer [deftest is]]
            [jtt.android.clockify :as clockify]))

(deftest minimal-clockify-effects-are-bounded-and-correlated
  (is (= [{:type :effect/http :provider :clockify :operation :snapshot
           :correlation-id "status" :timeout-ms 10000}]
         (clockify/status-effects "status")))
  (is (= :start (:operation (first (clockify/timer-effects "timer" false)))))
  (is (= :stop (:operation (first (clockify/timer-effects "timer" true))))))
