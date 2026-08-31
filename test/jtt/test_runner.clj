(ns jtt.test-runner
  (:require [clojure.test :as test]
            jtt.bootstrap.smoke-test
            jtt.frontend.cli.compatibility-test
            jtt.phase0.primitives-test
            jtt.phase0.tui-test
            jtt.port.contracts-test))

(defn -main [& _]
  (let [{:keys [fail error] :as summary}
        (test/run-tests 'jtt.bootstrap.smoke-test
                        'jtt.frontend.cli.compatibility-test
                        'jtt.phase0.primitives-test
                        'jtt.phase0.tui-test
                        'jtt.port.contracts-test)]
    (when (pos? (+ fail error))
      (throw (ex-info "Jolt tests failed" summary)))
    (println "jtt tests passed")))
