(ns jtt.frontend.gtk.core-test
  (:require [clojure.test :refer [deftest is]]
            [jtt.frontend.gtk.core :as gtk]))

(deftest lifecycle-and-reload-preserve-state
  (gtk/stop!)
  (is (= true (:running (gtk/start!))))
  (is (= 1 (:reloads (gtk/reload!))))
  (is (= false (:running (gtk/stop!)))))

(deftest reducer-and-view-model-cover-tracker-state
  (let [base {:running false :status "ready" :entries []}
        started (gtk/reduce-event base {:type :started :entry {:id "run"}})
        selected (gtk/reduce-event (assoc started :entries [{:id "old" :description "done"}])
                                   {:type :selected :entry {:id "old"}})
        failed (gtk/reduce-event selected {:type :failure :error :offline})]
    (is (= "run" (get-in started [:running :id])))
    (is (= "old" (get-in selected [:selected :id])))
    (is (= "error" (:status failed)))
    (is (= {:header "Jolt Time Tracker — ready" :timer "Start"
            :rows [{:id "old" :description "done"}] :selected "old"}
           (gtk/view-model (assoc selected :running false :status "ready"))))))
