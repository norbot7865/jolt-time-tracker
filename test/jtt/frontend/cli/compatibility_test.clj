(ns jtt.frontend.cli.compatibility-test
  (:require [babashka.cli :as cli]
            [clojure.test :refer [deftest is testing]]))

(defn dispatch-handler [result]
  (fn [options] (assoc options :result result)))

(deftest babashka-cli-loads-and-parses-unix-options
  (testing "aliases, booleans, scalar coercion, collection coercion, custom coercion, and positionals"
    (is (= {:verbose true
            :count 2
            :mode :active
            :tags [:work :review]
            :label "label:today"
            :entry "first"}
           (cli/parse-opts
            ["-v" "--count" "2" "--mode" "active"
             "--tags" "work" "--tags" "review" "--label" "today" "first"]
            {:alias {:v :verbose}
             :coerce {:verbose :boolean
                      :count :long
                      :mode :keyword
                      :tags [:keyword]
                      :label #(str "label:" %)}
             :args->opts [:entry]})))))

(deftest babashka-cli-dispatches-hierarchy
  (let [table [{:cmds ["entry"] :fn (dispatch-handler :entry)}
               {:cmds ["entry" "start"] :fn (dispatch-handler :start)
                :args->opts [:description]}
               {:cmds [] :fn (dispatch-handler :help)}]]
    (is (= {:dispatch ["entry" "start"]
            :opts {:description "focus"}
            :args nil
            :result :start}
           (cli/dispatch table ["entry" "start" "focus"])))))

(deftest babashka-cli-enforces-strict-validation
  (is (thrown-with-msg?
       clojure.lang.ExceptionInfo
       #"Unknown option"
       (cli/parse-opts ["--unknown"] {:restrict [:known]})))
  (is (thrown-with-msg?
       clojure.lang.ExceptionInfo
       #"Invalid value"
       (cli/parse-opts ["--count" "0"]
                       {:coerce {:count :long}
                        :validate {:count pos?}}))))

(deftest babashka-cli-renders-help-without-a-terminal
  (let [help (cli/format-opts {:spec {:count {:alias :c
                                                :coerce :long
                                                :desc "Number of entries"}}
                               :order [:count]})]
    (is (string? help))
    (is (re-find #"--count" help))
    (is (re-find #"Number of entries" help))))
