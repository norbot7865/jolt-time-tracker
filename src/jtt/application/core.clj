(ns jtt.application.core
  "Provider-independent workflows over injected capabilities and clock data."
  (:require [jtt.domain.core :as domain]
            [jtt.port.contracts :as contracts]))

(defn safe-error [operation cause]
  {:type :application/error :operation operation :message "provider operation failed" :cause cause})

(defn error-result [operation cause]
  {:error (safe-error operation cause)})

(defn configured? [session]
  (and (domain/_valid-provider? (:provider session))
       (:capabilities session)))

(defn configure [capabilities config]
  (let [provider (domain/provider-name config)]
    (if (domain/_valid-provider? provider)
      (assoc config :provider provider :capabilities capabilities)
      (error-result :configure :unsupported-provider))))

(defn invoke [session operation & args]
  (if-let [capability (get (:capabilities session) operation)]
    (apply capability args)
    (error-result operation :unsupported-operation)))

(defn discover [session]
  (invoke session :discover))

(defn snapshot [session]
  (invoke session :snapshot))

(defn normalized-snapshot [session now-ms day-start-ms day-end-ms]
  (let [result (snapshot session)]
    (if (:error result)
      result
      (domain/_normalize-snapshot result now-ms day-start-ms day-end-ms))))

(defn start [session input]
  (let [input (domain/_normalize-timer-input input)]
    (if (domain/valid-timer-input? input)
      (invoke session :start input)
      (error-result :start :invalid-input))))

(defn stop [session entry]
  (if entry
    (invoke session :stop entry)
    (error-result :stop :no-running-entry)))

(defn update-running [session entry patch]
  (let [input (domain/_merge-timer-input (:input entry) patch)]
    (if (domain/valid-timer-input? input)
      (invoke session :update-running entry input)
      (error-result :update-running :invalid-input))))

(defn continue-workflow [session entry]
  (cond
    (domain/running? entry) (contracts/continue-result nil nil (safe-error :continue :source-running))
    (nil? entry) (contracts/continue-result nil nil (safe-error :continue :missing-entry))
    :else (let [stopped (stop session entry)]
            (if (:error stopped)
              (contracts/continue-result nil nil (:error stopped))
              (let [started (start session (:input entry))]
                (contracts/continue-result stopped (when-not (:error started) started) (:error started)))))))

(defn delete [session entry]
  (if (and entry (:id entry))
    (invoke session :delete entry)
    (error-result :delete :missing-entry-id)))
