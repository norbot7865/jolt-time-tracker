(ns jtt.adapter.clockify.core-test
  (:require [clojure.test :refer [deftest is]]
            [jtt.adapter.clockify.core :as clockify]))

(defn recording-executor [calls body]
  (fn [operation request]
    (swap! calls conj [operation request])
    {:status 200 :body body}))

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

(deftest clockify-capability-map-normalizes-and-redacts-at-transport-boundary
  (let [calls (atom [])
        config {:server-url "https://synthetic.invalid" :workspace-id "w" :api-key "synthetic-key"}
        adapter (clockify/make-adapter config
                                       (recording-executor calls [{:id "p" :name "Project" :isBillable true}]))]
    (is (= [{:id "p" :name "Project" :client-id nil :client-name nil :billable true}]
           (:body ((:projects adapter)))))
    (is (= :projects (ffirst @calls)))
    (is (= {"X-Api-Key" "synthetic-key" "Accept" "application/json"}
           (get-in @calls [0 1 :headers])))
    (is (= "https://synthetic.invalid/workspaces/w/projects/active"
           (get-in @calls [0 1 :path])))))
