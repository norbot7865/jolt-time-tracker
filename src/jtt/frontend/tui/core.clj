(ns jtt.frontend.tui.core
  "Small Glimmer TUI composition boundary over shared presentation data.")

(defn view-model [{:keys [provider status today running]}]
  {:header (str (name (or provider :clockify)) " — " (or status "loading"))
   :today (str "Today: " (or today "00:00:00"))
   :timer (if running "Stop" "Start")})
