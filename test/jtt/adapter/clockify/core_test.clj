(ns jtt.adapter.clockify.core-test
  (:require [clojure.test :refer [deftest is]]
            [jtt.adapter.clockify.core :as clockify]))

(deftest plans-wire-operations-with-auth-at-boundary
  (is (= {:method :post :path "https://synthetic.invalid/workspaces/w/time-entries"
          :body {:description "synthetic"}}
         (clockify/request "https://synthetic.invalid" "w" :start {:description "synthetic"})))
  (is (= {:method :get
          :path "https://synthetic.invalid/workspaces/w/user/u/time-entries"
          :query {:in-progress true}}
         (clockify/request "https://synthetic.invalid" "w" :active-entry {:user-id "u"})))
  (is (= {"X-Api-Key" "synthetic-key" "Accept" "application/json"}
         (clockify/headers "synthetic-key"))))
