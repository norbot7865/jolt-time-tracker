(ns jtt.frontend.gtk.core
  "Glimmer GTK4 host. Provider work stays injected behind application capabilities."
  (:require [glimmer.core :as ui]
            [glimmer.ratom :as ratom]
            [glimmer-gtk.core]
            [jtt.application.core :as app]
            [jtt.bootstrap.core :as bootstrap]))

(defonce state (ratom/atom {:running false :reloads 0 :status "ready"
                            :entries [] :selected nil :description "Working"}))

(defn reduce-event [current event]
  (case (:type event)
    :snapshot (merge current (select-keys (:snapshot event) [:entries :running :status]))
    :started (assoc current :running (:entry event) :status "running")
    :stopped (assoc current :running false :status "ready")
    :selected (assoc current :selected (:entry event))
    :failure (assoc current :status "error" :error (:error event))
    current))

(defn view-model [{:keys [status running entries selected]}]
  {:header (str "Jolt Time Tracker — " status)
   :timer (if running "Stop" "Start")
   :rows (mapv #(select-keys % [:id :description]) entries)
   :selected (:id selected)})

(defn dispatch! [session event]
  (future
    (let [current @state
          entry (or (:selected current) (:running current))
          result (case event
                   :refresh (app/snapshot session)
                   :start (app/start session {:description (:description current)})
                   :stop (app/stop session entry)
                   :update (app/update-running session entry {:description (:description current)})
                   :continue (app/continue-workflow session entry)
                   :delete (app/delete session entry))
          transition (cond
                       (:error result) {:type :failure :error result}
                       (= event :refresh) {:type :snapshot :snapshot result}
                       (= event :start) {:type :started :entry result}
                       (= event :stop) {:type :stopped}
                       :else {:type :snapshot :snapshot (app/snapshot session)})]
      (swap! state reduce-event transition))))

(defn app-root [session]
  (let [{:keys [header timer rows]} (view-model @state)]
    [:vbox {:spacing 10 :margin 16}
     [:label {:label header}]
     [:hbox {:spacing 8}
      [:button {:label timer :on-click #(dispatch! session (if (:running @state) :stop :start))}]
      [:button {:label "Refresh" :on-click #(dispatch! session :refresh)}]
      [:button {:label "Update" :on-click #(dispatch! session :update)}]
      [:button {:label "Continue" :on-click #(dispatch! session :continue)}]
      [:button {:label "Delete" :on-click #(dispatch! session :delete)}]]
     (into [:vbox {:spacing 4}]
           (map (fn [{:keys [id description]}]
                  [:button {:key id :label (or description id)
                            :on-click #(swap! state reduce-event {:type :selected :entry {:id id :description description}})}])
                rows))]))

(defn start!
  ([] (swap! state assoc :running true))
  ([session & {:keys [auto-quit-ms]}]
   (swap! state assoc :running true :ui-running true)
   (ui/run #(app-root session) :app-id "org.jolt.time-tracker" :title "Jolt Time Tracker"
           :width 720 :height 480 :auto-quit-ms auto-quit-ms)))

(defn stop! [] (swap! state assoc :running false :ui-running false))

(defn reload! []
  (swap! state update :reloads inc)
  (when (:ui-running @state) (ui/reload!))
  @state)

(defn status [] @state)

(defn -main [& args]
  (start! (bootstrap/compose {:home (System/getProperty "user.home")})
          :auto-quit-ms (when (some #{"--smoke"} args) 1200)))
