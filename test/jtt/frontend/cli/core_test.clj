(ns jtt.frontend.cli.core-test
  (:require [clojure.test :refer [deftest is]]
            [jtt.frontend.cli.core :as cli]))

(deftest dispatches-status-and-rejects-unknown
  (let [session {:capabilities {:snapshot (fn [] {:status :ok})}}]
    (is (= {:status :ok} (cli/run session ["status"])))
    (is (thrown-with-msg? clojure.lang.ExceptionInfo #"unknown command"
                          (cli/run session ["wat"])))))
