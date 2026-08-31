(ns jtt.domain.core
  "Pure provider-neutral values, validation, and deterministic tracker math."
  (:require [clojure.string :as str]))

(def supported-providers #{:clockify :kimai})
(def timer-input-keys [:description :project-id :task-id :billable :tags])

(defn provider-name [config]
  (or (:provider config) :clockify))

(defn _valid-provider? [provider]
  (contains? supported-providers provider))

(defn running? [entry]
  (and (or (:start entry) (:start-ms entry))
       (nil? (or (:end entry) (:end-ms entry)))))

(defn elapsed-ms [entry now-ms]
  (let [start (:start-ms entry)
        end (or (:end-ms entry) now-ms)]
    (if (and start (> end start)) (- end start) 0)))

(defn today-total-ms [entries now-ms day-start-ms day-end-ms]
  (reduce + 0 (map (fn [entry]
                     (let [start (max day-start-ms (:start-ms entry))
                           end (min day-end-ms (or (:end-ms entry) now-ms))]
                       (if (> end start) (- end start) 0))) entries)))

(defn valid-timer-input? [{:keys [description project-id task-id]}]
  (and (string? description)
       (<= 1 (count (str/trim description)) 3000)
       (or (nil? task-id) (and (string? project-id) (not (str/blank? project-id))))))

(defn _normalize-timer-input [input]
  (let [description (:description input)
        tags (:tags input)]
    (assoc input
           :description (when (string? description) (str/trim description))
           :tags (if (vector? tags) (vec tags) tags))))

(defn _merge-timer-input [current patch]
  (reduce (fn [merged key]
            (if (and (contains? patch key)
                     (not (and (= key :tags) (nil? (:tags patch)))))
              (assoc merged key (get patch key))
              merged))
          current
          timer-input-keys))

(defn copy-entry [entry]
  (if (vector? (:tags entry))
    (assoc entry :tags (vec (:tags entry)))
    entry))

(defn sort-completed [entries]
  (vec (sort-by :start-ms > (map copy-entry entries))))

(defn _normalize-snapshot [{:keys [active entries projects]} now-ms day-start-ms day-end-ms]
  (let [completed (sort-completed (remove running? entries))
        active (when active (copy-entry active))
        total-entries (if active (conj completed active) completed)]
    {:active active
     :entries completed
     :projects (vec projects)
     :today-ms (today-total-ms total-entries now-ms day-start-ms day-end-ms)}))
