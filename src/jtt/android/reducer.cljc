(ns jtt.android.reducer
  "Pure Android wire reducer; credentials are host capabilities, never state.")

(def max-event-chars 4096)

(defn initial-state [] {:lifecycle :created :pending {} :status nil :error nil})

(defn- valid-event? [event]
  (and (map? event) (keyword? (:type event))
       (<= (count (pr-str event)) max-event-chars)))

(defn reduce-event [state event]
  (if-not (valid-event? event)
    [(assoc state :error {:type :wire/invalid-event :message "invalid event"}) []]
    (case (:type event)
      :lifecycle/resume [(assoc state :lifecycle :resumed) []]
      :lifecycle/pause [(assoc state :lifecycle :paused) []]
      :status/request (let [id (or (:correlation-id event) "status-1")]
                        [(assoc-in state [:pending id] :status)
                         [{:type :effect/http :correlation-id id :operation :snapshot :timeout-ms 10000}]])
      :http/result [(-> state (update :pending dissoc (:correlation-id event))
                         (assoc :status (:value event))) []]
      :timer/start [{:phase :starting} [{:type :effect/http :correlation-id (:correlation-id event)
                                          :operation :start :timeout-ms 10000}]]
      :timer/stop [{:phase :stopping} [{:type :effect/http :correlation-id (:correlation-id event)
                                         :operation :stop :timeout-ms 10000}]]
      [state []])))
