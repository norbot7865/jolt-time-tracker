(ns jtt.application.core-test
  (:require [clojure.test :refer [deftest is]]
            [jtt.application.core :as app]))

(defn fake-capabilities [state]
  {:discover (fn [] {:user {:id "user"} :workspaces [{:id "workspace"}]})
   :snapshot (fn [] @state)
   :start (fn [input] (if (= "fail" (:description input))
                        {:error (app/safe-error :start :synthetic)}
                        (let [entry {:id "new" :input input}] (swap! state assoc :running entry) entry)))
   :stop (fn [entry] (swap! state dissoc :running) entry)
   :update-running (fn [entry input] (assoc entry :input input))
   :delete (fn [entry] (swap! state update :deleted conj (:id entry)))})

(deftest lifecycle-and-continue-partial-result
  (let [state (atom {:entries []})
        session (app/configure (fake-capabilities state) {})
        input {:description "focus"}
        started (app/start session input)
        updated (app/update-running session started {:description "updated"})
        continued (app/continue-workflow session (assoc started :input {:description "fail"}))]
    (is (= :clockify (:provider session)))
    (is (= "user" (get-in (app/discover session) [:user :id])))
    (is (= "updated" (get-in updated [:input :description])))
    (is (= "new" (:id (:stopped continued))))
    (is (nil? (:started continued)))
    (is (= :synthetic (get-in continued [:error :cause])))))
