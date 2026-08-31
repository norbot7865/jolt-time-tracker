(ns jtt.bootstrap.core
  "Production composition without frontend toolkit initialization."
  (:require [jtt.adapter.clockify.core :as clockify]
            [jtt.adapter.config.core :as config]
            [jtt.adapter.http.core :as http]
            [jtt.adapter.provider.registry :as registry]
            [jtt.application.core :as application]))

(defn _snapshot [adapter]
  (fn []
    (let [active ((:active-entry adapter) {})
          entries ((:recent-entries adapter) {})
          projects ((:projects adapter))]
      {:active (:body active)
       :entries (:body entries)
       :projects (:body projects)})))

(defn _capabilities [adapter]
  (assoc adapter
         :discover (fn []
                     {:user (:body ((:current-user adapter)))
                      :workspaces (:body ((:workspaces adapter)))})
         :snapshot (_snapshot adapter)))

(defn compose
  ([settings] (compose settings {}))
  ([settings {:keys [executor env]}]
   (require 'jtt.adapter.kimai.core)
   (let [paths {:canonical (config/config-path settings)
                :legacy (config/_legacy-config-path settings)}
         cfg (config/apply-overrides
              (merge settings (config/load-config paths {}))
              (or env (System/getenv)))
         provider (:provider cfg)
         adapters {:clockify (clockify/make-adapter cfg (or executor http/execute))
                   :kimai ((var-get (ns-resolve 'jtt.adapter.kimai.core '_make-adapter)) cfg (or executor http/execute))}]
     (if-let [adapter (get adapters provider)]
       (application/configure (_capabilities adapter) cfg)
       (registry/select provider adapters)))))
