(ns jtt.adapter.kimai.core-test
  (:require [clojure.test :refer [deftest is]]
            [jtt.adapter.kimai.core :as kimai]))

(deftest plans-kimai-bearer-wire
  (is (= "https://synthetic.invalid/api" (kimai/base-url "https://synthetic.invalid/")))
  (is (= "Bearer synthetic-token" (get (kimai/headers "synthetic-token") "Authorization")))
  (is (= {:method :post :path "https://synthetic.invalid/api/timesheets"
          :body {:projectId 3 :activityId 4}}
         (kimai/request "https://synthetic.invalid" :start {:projectId 3 :activityId 4}))))
