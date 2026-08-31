(ns jtt.frontend.parity-test
  (:require [clojure.test :refer [deftest is]]
            [jtt.frontend.gtk.core :as gtk]
            [jtt.frontend.shared.coordinator :as coordinator]
            [jtt.frontend.tui.core :as tui]))

(deftest all-desktop-hosts-consume-shared-state
  (let [model {:provider :clockify :status "ready" :today "00:01:00" :running true}
        expected {:header "clockify — ready" :today "Today: 00:01:00" :timer "Stop"
                  :rows [] :cursor 0 :modal nil}]
    (is (= expected (tui/view-model model)))
    (is (= :ready (:phase (coordinator/accept-response
                           (coordinator/begin-request (coordinator/initial-state))
                           1 model))))
    (gtk/start!)
    (is (:running (gtk/status)))
    (gtk/stop!)))
