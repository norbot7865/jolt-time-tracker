(ns jtt.application.core
  "Provider-independent sequential workflows over injected capabilities."
  (:require [jtt.domain.core :as domain]))

(defn safe-error [operation cause]
  {:type :application/error :operation operation :message "provider operation failed" :cause cause})

(defn configure [capabilities config]
  (assoc config :provider (domain/provider-name config) :capabilities capabilities))

(defn discover [session]
  ((:discover (:capabilities session))))

(defn snapshot [session]
  ((:snapshot (:capabilities session))))

(defn start [session input]
  (if (domain/valid-timer-input? input)
    ((:start (:capabilities session)) input)
    {:error (safe-error :start :invalid-input)}))

(defn stop [session entry]
  ((:stop (:capabilities session)) entry))

(defn update-running [session entry input]
  ((:update-running (:capabilities session)) entry input))

(defn continue-workflow [session entry]
  (let [stopped ((:stop (:capabilities session)) entry)]
    (if (:error stopped)
      {:stopped nil :started nil :error (:error stopped)}
      (let [started ((:start (:capabilities session)) (:input entry))]
        (if (:error started)
          {:stopped stopped :started nil :error (:error started)}
          {:stopped stopped :started started :error nil})))))

(defn delete [session entry]
  ((:delete (:capabilities session)) entry))
