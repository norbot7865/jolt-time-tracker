(ns jtt.frontend.tui.core-test
  (:require [clojure.test :refer [deftest is]]
            [jtt.frontend.tui.core :as tui]))

(deftest view-model-is-toolkit-neutral-and-keyed
  (is (= {:header "clockify — ready" :today "Today: 01:02:03" :timer "Stop"
          :rows [{:key "e" :label "focus"}] :cursor 0 :modal nil}
         (tui/view-model {:provider :clockify :status "ready" :today "01:02:03"
                          :running true :entries [{:id "e" :description "focus"}]}))))

(deftest reducer-covers-focus-modal-scroll-resize-and-provider-results
  (let [base (tui/initial-state)
        focused (tui/reduce-event base {:type :focus :cursor 2})
        modal (tui/reduce-event focused {:type :modal :modal :confirm-delete})
        resized (tui/reduce-event modal {:type :resize :size [80 24]})
        scrolled (tui/reduce-event resized {:type :scroll :delta 3})
        started (tui/reduce-event scrolled {:type :started :entry {:id "run"}})
        stopped (tui/reduce-event started {:type :stopped})]
    (is (= 2 (:cursor focused)))
    (is (= :confirm-delete (:modal modal)))
    (is (= [80 24] (:size resized)))
    (is (= 3 (:scroll scrolled)))
    (is (= "run" (get-in started [:running :id])))
    (is (nil? (:running stopped)))
    (is (= "error" (:status (tui/reduce-event base {:type :failure :error :offline}))))))
