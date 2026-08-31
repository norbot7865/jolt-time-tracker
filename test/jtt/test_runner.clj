(ns jtt.test-runner
  (:require [clojure.test :as test]
            jtt.bootstrap.smoke-test
            jtt.frontend.cli.compatibility-test))

(defn -main [& _]
  (let [{:keys [fail error] :as summary}
        (test/run-tests 'jtt.bootstrap.smoke-test
                        'jtt.frontend.cli.compatibility-test)]
    (when (pos? (+ fail error))
      (throw (ex-info "Jolt tests failed" summary)))
    (println "jtt tests passed")))
