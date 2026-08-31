(ns jtt.port.contracts
  "Provider-neutral contracts frozen from the gtt ports surface.")

(def provider-operations
  #{:current-user :workspaces :projects :tasks :recent-entries :entry
    :active-entry :start :stop :update-running :delete})

(def continue-result-keys [:stopped :started :error])

(defn continue-result [stopped started error]
  {:stopped stopped :started started :error error})

(defn bounded-wire? [value max-chars]
  (and (string? value) (<= (count value) max-chars)))
