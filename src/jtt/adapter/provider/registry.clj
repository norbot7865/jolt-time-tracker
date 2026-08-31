(ns jtt.adapter.provider.registry
  "Provider selection without loading frontend toolkits.")

(defn select [provider adapters]
  (let [provider (or provider :clockify)]
    (or (get adapters provider)
        (throw (ex-info "unsupported provider" {:type :config/unsupported-provider
                                                  :provider provider})))))
