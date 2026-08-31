(ns jtt.application.core-test
  (:require [clojure.test :refer [deftest is]]
            [jtt.application.core :as app]))

(defn fake-capabilities [state]
  {:discover (fn [] {:user {:id "user"} :workspaces [{:id "workspace"}]})
   :snapshot (fn [] @state)
   :start (fn [input] (if (= "fail" (:description input))
                        {:error (app/safe-error :start :synthetic)}
                        (let [entry {:id "new" :input input}]
                          (swap! state assoc :running entry)
                          entry)))
   :stop (fn [entry] (swap! state dissoc :running) entry)
   :update-running (fn [entry input] (assoc entry :input input))
   :delete (fn [entry] (swap! state update :deleted conj (:id entry)))})

(deftest lifecycle-continue-and-partial-update
  (let [state (atom {:active {:id "run" :start-ms 10}
                     :entries [{:id "old" :start-ms 5 :end-ms 6}]
                     :projects [{:id "p"}] :deleted []})
        session (app/configure (fake-capabilities state) {})
        input {:description " focus " :project-id "p" :billable true :tags ["a"]}
        started (app/start session input)
        updated (app/update-running session started {:description "updated"})
        continued (app/continue-workflow session (assoc started :input {:description "fail"}))]
    (is (app/configured? session))
    (is (= "user" (get-in (app/discover session) [:user :id])))
    (is (= "focus" (get-in started [:input :description])))
    (is (= {:description "updated" :project-id "p" :billable true :tags ["a"]}
           (:input updated)))
    (is (= "new" (:id (:stopped continued))))
    (is (nil? (:started continued)))
    (is (= :synthetic (get-in continued [:error :cause])))
    (is (= {:active {:id "run" :start-ms 10}
            :entries [{:id "old" :start-ms 5 :end-ms 6}]
            :projects [{:id "p"}]
            :today-ms 91}
           (app/normalized-snapshot session 100 0 100)))
    (app/delete session {:id "old"})
    (is (= ["old"] (:deleted @state)))))

(deftest workflows-reject-safe-invalid-states
  (let [session (app/configure {} {})]
    (is (= :unsupported-provider (:cause (:error (app/configure {} {:provider :unknown})))))
    (is (= :invalid-input (:cause (:error (app/start session {:description ""})))))
    (is (= :no-running-entry (:cause (:error (app/stop session nil)))))
    (is (= :missing-entry-id (:cause (:error (app/delete session {})))))
    (is (= :source-running (:cause (:error (app/continue-workflow session {:start-ms 1})))))))
