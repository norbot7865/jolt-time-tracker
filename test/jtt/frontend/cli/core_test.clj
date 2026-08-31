(ns jtt.frontend.cli.core-test
  (:require [clojure.test :refer [deftest is]]
            [jtt.frontend.cli.core :as cli]))

(def session
  {:provider :clockify
   :api-key "synthetic"
   :capabilities {:snapshot (fn [] {:active {:id "run" :input {:description "old"}}
                                    :entries [{:id "old" :input {:description "done"}}]})
                  :discover (fn [] {:user {:id "u"}})
                  :start (fn [input] {:id "new" :input input})
                  :stop (fn [entry] (assoc entry :stopped true))
                  :update-running (fn [entry input] (assoc entry :input input))
                  :delete (fn [entry] {:deleted (:id entry)})
                  :projects (fn [] [{:id "p"}])
                  :tasks (fn [_] [{:id "t"}])}})

(deftest dispatches-complete-command-surface
  (is (= {:user {:id "u"}} (cli/run session ["discover"])))
  (is (= "focus" (get-in (cli/run session ["start" "--description" "focus"]) [:input :description])))
  (is (= true (:stopped (cli/run session ["stop"]))))
  (is (= "new" (get-in (cli/run session ["update" "--description" "new"]) [:input :description])))
  (is (= "old" (get-in (cli/run session ["continue" "--id" "old"]) [:stopped :id])))
  (is (= {:deleted "old"} (cli/run session ["delete" "--id" "old"])))
  (is (= [{:id "old" :input {:description "done"}}] (cli/run session ["list"])))
  (is (= [{:id "p"}] (cli/run session ["projects"])))
  (is (= [{:id "t"}] (cli/run session ["tasks" "--project-id" "p"])))
  (is (= "[REDACTED]" (:api-key (cli/run session ["config"]))))
  (is (seq (:help (cli/run session ["help"]))))
  (is (= :unknown-command (get-in (cli/run session ["wat"]) [:error :cause])))
  (is (= 0 (cli/_exit-code {:status :ok})))
  (is (= 2 (cli/_exit-code {:error {:cause :unknown-command}}))))
