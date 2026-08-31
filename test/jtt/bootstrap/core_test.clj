(ns jtt.bootstrap.core-test
  (:require [clojure.test :refer [deftest is]]
            [jtt.bootstrap.core :as bootstrap]))

(defn fake-executor [_ request]
  (let [path (:path request)]
    {:status 200
     :body (cond
             (= path "https://synthetic.invalid/user") {:id "u"}
             (= path "https://synthetic.invalid/workspaces") [{:id "w"}]
             (.contains path "/projects/active") []
             (.contains path "/time-entries") []
             :else [])}))

(deftest composes-clockify-without-loading-frontends
  (let [session (bootstrap/compose {:provider :clockify
                                    :server-url "https://synthetic.invalid"
                                    :workspace-id "w"
                                    :api-key "synthetic"}
                                   {:executor fake-executor :env {}})]
    (is (= :clockify (:provider session)))
    (is (= "u" (get-in ((:discover (:capabilities session))) [:user :id])))
    (is (= [] (get-in ((:snapshot (:capabilities session))) [:entries])))))

(deftest unknown-provider-is-safe
  (is (thrown-with-msg? clojure.lang.ExceptionInfo #"unsupported provider"
                        (bootstrap/compose {:provider :other
                                            :home "/tmp"}
                                           {:executor fake-executor :env {}}))))
