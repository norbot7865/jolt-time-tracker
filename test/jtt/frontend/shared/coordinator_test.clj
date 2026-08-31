(ns jtt.frontend.shared.coordinator-test
  (:require [clojure.test :refer [deftest is]]
            [jtt.frontend.shared.coordinator :as c]))

(deftest stale-results-are-discarded-and-secrets-redacted
  (let [s1 (c/begin-request (c/initial-state))
        s2 (c/begin-request s1)]
    (is (= s2 (c/accept-response s2 1 {:old true})))
    (is (= :ready (:phase (c/accept-response s2 2 {:new true}))))
    (is (= :ready (:phase (c/apply-response s2 2 {:ok true}))))
    (is (= "[REDACTED]" (:token (c/redact {:token "secret"}))))
    (is (= :error (:phase (c/reject-response s2 2 :timeout))))))
