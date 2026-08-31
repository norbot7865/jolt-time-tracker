(ns jtt.domain.core
  "Pure provider-neutral values and timer math."
  (:require [clojure.string :as str]))

(defn provider-name [config]
  (or (:provider config) :clockify))

(defn running? [entry]
  (and (or (:start entry) (:start-ms entry))
       (nil? (or (:end entry) (:end-ms entry)))))

(defn elapsed-ms [entry now-ms]
  (let [start (:start-ms entry)
        end (or (:end-ms entry) now-ms)]
    (if (and start (> end start)) (- end start) 0)))

(defn valid-timer-input? [{:keys [description]}]
  (and (string? description) (<= 1 (count (str/trim description)) 500)))

(defn today-total-ms [entries now-ms day-start-ms day-end-ms]
  (reduce + 0 (map (fn [entry]
                     (let [start (max day-start-ms (:start-ms entry))
                           end (min day-end-ms (or (:end-ms entry) now-ms))]
                       (if (> end start) (- end start) 0))) entries)))
