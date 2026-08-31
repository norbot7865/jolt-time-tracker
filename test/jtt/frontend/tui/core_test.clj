(ns jtt.frontend.tui.core-test
  (:require [clojure.test :refer [deftest is]]
            [jtt.frontend.tui.core :as tui]))

(deftest view-model-is-toolkit-neutral
  (is (= {:header "clockify — ready" :today "Today: 01:02:03" :timer "Stop"}
         (tui/view-model {:provider :clockify :status "ready"
                          :today "01:02:03" :running true}))))
