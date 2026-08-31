(ns jtt.domain.core-test
  (:require [clojure.test :refer [deftest is]]
            [jtt.domain.core :as domain]))

(deftest domain-math-validation-and-provider-selection
  (is (= :clockify (domain/provider-name {})))
  (is (domain/_valid-provider? :kimai))
  (is (not (domain/_valid-provider? :unknown)))
  (is (domain/running? {:start-ms 10}))
  (is (= 90 (domain/elapsed-ms {:start-ms 10 :end-ms 100} 200)))
  (is (= 3600 (domain/today-total-ms [{:start-ms -100 :end-ms 3600}] 4000 0 3600)))
  (is (domain/valid-timer-input? {:description "focus" :project-id "p" :task-id "t"}))
  (is (not (domain/valid-timer-input? {:description "focus" :task-id "t"})))
  (is (not (domain/valid-timer-input? {:description "  "}))))

(deftest normalizes-merges-and-sorts-portable-data
  (is (= {:description "focus" :tags ["a"]}
         (domain/_normalize-timer-input {:description " focus " :tags ["a"]})))
  (is (= {:description "new" :project-id "p" :tags ["a"]}
         (domain/_merge-timer-input {:description "old" :project-id "p" :tags ["a"]}
                                   {:description "new" :tags nil})))
  (is (= [{:id "new" :start-ms 20 :end-ms 21 :tags ["x"]}
         {:id "old" :start-ms 10 :end-ms 11}]
         (domain/sort-completed [{:id "old" :start-ms 10 :end-ms 11}
                                 {:id "new" :start-ms 20 :end-ms 21 :tags ["x"]}])))
  (is (= {:active {:id "run" :start-ms 50}
          :entries [{:id "new" :start-ms 40 :end-ms 45}]
          :projects [{:id "p"}]
          :today-ms 55}
         (domain/_normalize-snapshot {:active {:id "run" :start-ms 50}
                                     :entries [{:id "new" :start-ms 40 :end-ms 45}
                                               {:id "run" :start-ms 50}]
                                     :projects [{:id "p"}]}
                                    100 0 100))))
