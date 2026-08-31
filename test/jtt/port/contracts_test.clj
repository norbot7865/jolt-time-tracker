(ns jtt.port.contracts-test
  (:require [clojure.test :refer [deftest is]]
            [jtt.port.contracts :as contracts]))

(deftest provider-contract-covers-workflows
  (is (every? contracts/provider-operations
              [:current-user :workspaces :projects :tasks :recent-entries
               :active-entry :start :stop :update-running :delete]))
  (is (= {:stopped {:id "old"}
          :started {:id "new"}
          :error nil}
         (contracts/continue-result {:id "old"} {:id "new"} nil)))
  (is (contracts/bounded-wire? "synthetic" 16))
  (is (not (contracts/bounded-wire? "too-long" 3))))
