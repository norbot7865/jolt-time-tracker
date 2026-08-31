(ns jtt.android.stress
  "Host-independent allocation/lifecycle stress helper for the Android wire reducer.")

(defn repeated-status-events [reduce-fn iterations]
  (loop [state {:lifecycle :created} n iterations]
    (if (zero? n)
      state
      (let [[next-state _] (reduce-fn state {:type :lifecycle/resume})]
        (recur next-state (dec n))))))
