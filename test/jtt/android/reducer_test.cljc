(ns jtt.android.reducer-test
  (:require [clojure.test :refer [deftest is]]
            [jtt.android.reducer :as reducer]))

(deftest lifecycle-and-bounded-http-effect
  (let [[state effects] (reducer/reduce-event (reducer/initial-state)
                                               {:type :status/request :correlation-id "c1"})]
    (is (= :created (:lifecycle state)))
    (is (= :status (get-in state [:pending "c1"])))
    (is (= {:type :effect/http :correlation-id "c1" :operation :snapshot :timeout-ms 10000}
           (first effects)))))

(deftest malformed-and-oversized-events-fail-safely
  (let [[state effects] (reducer/reduce-event (reducer/initial-state) {:bad true})]
    (is (= :wire/invalid-event (get-in state [:error :type])))
    (is (empty? effects))))
