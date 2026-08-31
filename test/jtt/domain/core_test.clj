(ns jtt.domain.core-test
  (:require [clojure.test :refer [deftest is]]
            [jtt.domain.core :as domain]))

(deftest domain-math-and-validation
  (is (= :clockify (domain/provider-name {})))
  (is (domain/running? {:start-ms 10}))
  (is (= 90 (domain/elapsed-ms {:start-ms 10 :end-ms 100} 200)))
  (is (= 3600 (domain/today-total-ms [{:start-ms -100 :end-ms 3600}] 4000 0 3600)))
  (is (domain/valid-timer-input? {:description "focus"}))
  (is (not (domain/valid-timer-input? {:description "  "}))))
