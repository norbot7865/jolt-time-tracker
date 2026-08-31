(ns jtt.test-runner
  (:require [clojure.test :as test]
            jtt.bootstrap.smoke-test
            jtt.frontend.cli.compatibility-test
            jtt.phase0.primitives-test
            jtt.phase0.tui-test
            jtt.port.contracts-test
            jtt.domain.core-test
            jtt.application.core-test
            jtt.frontend.shared.coordinator-test
            jtt.android.reducer-test
            jtt.adapter.config.core-test
            jtt.adapter.clockify.core-test
            jtt.adapter.kimai.core-test
            jtt.adapter.provider.registry-test
            jtt.frontend.cli.core-test
            jtt.frontend.tui.core-test
            jtt.frontend.gtk.core-test
            jtt.frontend.parity-test))

(defn -main [& _]
  (let [{:keys [fail error] :as summary}
        (test/run-tests 'jtt.bootstrap.smoke-test
                        'jtt.frontend.cli.compatibility-test
                        'jtt.phase0.primitives-test
                        'jtt.phase0.tui-test
                        'jtt.port.contracts-test
                        'jtt.domain.core-test
                        'jtt.application.core-test
                        'jtt.frontend.shared.coordinator-test
                        'jtt.android.reducer-test
                        'jtt.adapter.config.core-test
                        'jtt.adapter.clockify.core-test
                        'jtt.adapter.kimai.core-test
                        'jtt.adapter.provider.registry-test
                        'jtt.frontend.cli.core-test
                        'jtt.frontend.tui.core-test
                        'jtt.frontend.gtk.core-test
                        'jtt.frontend.parity-test)]
    (when (pos? (+ fail error))
      (throw (ex-info "Jolt tests failed" summary)))
    (println "jtt tests passed")))
