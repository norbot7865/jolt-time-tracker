(ns jtt.android.clockify
  "Minimal Clockify Android effect planning; credentials stay in the host executor.")

(defn effect [operation correlation-id]
  {:type :effect/http :provider :clockify :operation operation
   :correlation-id correlation-id :timeout-ms 10000})

(defn status-effects [id]
  [(effect :snapshot id)])

(defn timer-effects [id running?]
  [(effect (if running? :stop :start) id)])
