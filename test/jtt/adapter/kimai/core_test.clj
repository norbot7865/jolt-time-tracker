(ns jtt.adapter.kimai.core-test
  (:require [clojure.test :refer [deftest is]]
            [jtt.adapter.kimai.core :as kimai]))

(deftest plans-kimai-bearer-wire
  (is (= "https://synthetic.invalid/api" (kimai/base-url "https://synthetic.invalid/")))
  (is (= "Bearer synthetic-token" (get (kimai/headers "synthetic-token") "Authorization")))
  (is (= {:method :post :path "https://synthetic.invalid/api/timesheets"
          :body {:projectId 3 :activityId 4}}
         (kimai/request "https://synthetic.invalid" :start {:projectId 3 :activityId 4}))))

(deftest normalizes-kimai-numeric-identifiers-and-synthetic-scope
  (let [calls (atom [])
        executor (fn [operation request]
                   (swap! calls conj [operation request])
                   {:status 200 :body [{:id 3 :name "Project" :customer 7}]})
        adapter (#'kimai/_make-adapter {:server-url "https://synthetic.invalid"
                                        :api-key "synthetic-token"}
                                       executor)]
    (is (= [{:id "3" :name "Project" :client-id "7" :client-name nil :billable nil}]
           (:body ((:projects adapter)))))
    (is (= "Bearer synthetic-token" (get-in @calls [0 1 :headers "Authorization"])))
    (is (= [{:id "default" :name "Kimai"}]
           (:body ((:workspaces adapter)))))))
