(ns jtt.frontend.gtk.core
  "Optional GTK composition root; state remains portable data.")

(defonce state (atom {:running false :reloads 0}))

(defn start! [] (swap! state assoc :running true))
(defn stop! [] (swap! state assoc :running false))
(defn reload! [] (swap! state update :reloads inc))
(defn status [] @state)
