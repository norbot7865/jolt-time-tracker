(ns jtt.frontend.tui.core
  "Glimmer ncurses host with portable state transitions."
  (:require [glimmer.core :as ui]
            [glimmer.ratom :as ratom]
            [glimmer-tui.core :as tui]
            [jtt.application.core :as app]
            [jtt.bootstrap.core :as bootstrap]))

(defn initial-state []
  {:provider :clockify :status "ready" :today "00:00:00"
   :running nil :entries [] :cursor 0 :scroll 0 :modal nil :description "Working"})

(defn reduce-event [state event]
  (case (:type event)
    :resize (assoc state :size (:size event))
    :scroll (update state :scroll #(max 0 (+ (or % 0) (:delta event))))
    :focus (assoc state :cursor (max 0 (:cursor event)))
    :modal (assoc state :modal (:modal event))
    :snapshot (merge state (select-keys (:snapshot event) [:running :entries :today :status]))
    :failure (assoc state :status "error" :error (:error event))
    :started (assoc state :running (:entry event) :status "running")
    :stopped (assoc state :running nil :status "ready")
    state))

(defn view-model [{:keys [provider status today running entries cursor modal]}]
  {:header (str (name (or provider :clockify)) " — " (or status "loading"))
   :today (str "Today: " (or today "00:00:00"))
   :timer (if running "Stop" "Start")
   :rows (mapv (fn [entry] {:key (:id entry) :label (or (:description entry) "")}) entries)
   :cursor (or cursor 0)
   :modal modal})

(defonce state (ratom/atom (initial-state)))

(defn dispatch! [session event]
  (case event
    :start (future (let [result (app/start session {:description (:description @state)})]
                     (swap! state reduce-event (if (:error result) {:type :failure :error result}
                                                    {:type :started :entry result}))))
    :stop (future (let [result (app/stop session (:running @state))]
                    (swap! state reduce-event (if (:error result) {:type :failure :error result}
                                                   {:type :stopped}))))
    :refresh (future (let [result (app/snapshot session)]
                       (swap! state reduce-event (if (:error result) {:type :failure :error result}
                                                      {:type :snapshot :snapshot result}))))
    :quit (tui/quit!)
    nil))

(defn app-root [session]
  (let [model (view-model @state)]
    [:vbox {:spacing 1 :margin 1}
     [:label {:label (:header model) :bold true}]
     [:label {:label (:today model)}]
     [:hbox {:spacing 1}
      [:button {:label (:timer model) :autofocus true
                :on-click #(dispatch! session (if (:running @state) :stop :start))}]
      [:button {:label "Refresh" :on-click #(dispatch! session :refresh)}]
      [:button {:label "Quit" :on-click #(dispatch! session :quit)}]]
     [:frame {:label "Entries" :border :rounded :height-request 8}
      (into [:vbox {}] (map (fn [{:keys [key label]}] [:label {:key key :label label}]) (:rows model)))]]))

(defn start! [session & {:keys [auto-quit-ms]}]
  (reset! state (initial-state))
  (ui/run #(app-root session) :auto-quit-ms auto-quit-ms))

(defn -main [& _]
  (if-not (tui/usable-terminal?)
    (println "JTT TUI SKIP (no usable terminal)")
    (start! (bootstrap/compose {:home (System/getProperty "user.home")}))))
