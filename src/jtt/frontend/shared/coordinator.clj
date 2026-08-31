(ns jtt.frontend.shared.coordinator
  "Toolkit-neutral state transitions; no frontend toolkit dependencies.")

(defn initial-state [] {:generation 0 :phase :idle :snapshot nil :error nil})

(defn begin-request [state]
  (-> state
      (update :generation inc)
      (assoc :phase :loading :error nil)))

(defn accept-response [state generation snapshot]
  (if (= generation (:generation state))
    (assoc state :phase :ready :snapshot snapshot :error nil)
    state))

(defn reject-response [state generation error]
  (if (= generation (:generation state))
    (assoc state :phase :error :error {:message "request failed" :cause error})
    state))

(defn redact [value]
  (cond
    (map? value) (into {} (map (fn [[k v]] [k (if (#{:token :api-key :authorization} k) "[REDACTED]" (redact v))]) value))
    (vector? value) (mapv redact value)
    :else value))
