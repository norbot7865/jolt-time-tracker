(ns jtt.adapter.config.core
  "Desktop JSON config boundary; Android credentials are not accepted here."
  (:require [clojure.data.json :as json]
            [clojure.java.io :as io]))

(def override-keys
  {:provider ["GTT_PROVIDER"]
   :server-url ["GTT_SERVER_URL"]
   :api-key ["GTT_API_KEY" "CLOCKIFY_API_KEY"]
   :user-id ["GTT_USER_ID" "CLOCKIFY_USER_ID"]
   :workspace-id ["GTT_WORKSPACE_ID" "CLOCKIFY_WORKSPACE_ID"]
   :workspace-name ["GTT_WORKSPACE_NAME" "CLOCKIFY_WORKSPACE_NAME"]})

(defn config-path [{:keys [config-home home]}]
  (let [root (or config-home (str home "/.config"))]
    (str root "/gtt/config.json")))

(defn _legacy-config-path [{:keys [home]}]
  (str home "/.config/clockify-tui/config.json"))

(defn read-config [path]
  (when (.exists (io/file path))
    (json/read-str (slurp path) :key-fn keyword)))

(defn redacted [config]
  (reduce (fn [result key]
            (if (contains? result key)
              (assoc result key "[REDACTED]")
              result))
          config
          [:api-key :token :authorization]))

(defn apply-overrides [config env]
  (reduce (fn [result [key names]]
            (if-let [value (some #(get env %) names)]
              (assoc result key value)
              result))
          (or config {})
          override-keys))

(defn load-config [paths env]
  (let [canonical (read-config (:canonical paths))
        legacy (when-not canonical (read-config (:legacy paths)))]
    (apply-overrides (or canonical legacy {}) env)))

(defn _private! [file]
  (java.nio.file.Files/setPosixFilePermissions
   (.toPath file)
   (java.nio.file.attribute.PosixFilePermissions/fromString "rw-------")))

(defn save-config! [path config]
  (let [target (io/file path)
        parent (.getParentFile target)
        _ (.mkdirs parent)
        tmp (java.nio.file.Files/createTempFile (.toPath parent) ".jtt-" ".json"
                                                 (make-array java.nio.file.attribute.FileAttribute 0))]
    (spit (str tmp) (json/write-str config))
    (_private! (.toFile tmp))
    (java.nio.file.Files/move tmp (.toPath target)
                              (into-array java.nio.file.CopyOption
                                          [java.nio.file.StandardCopyOption/ATOMIC_MOVE
                                           java.nio.file.StandardCopyOption/REPLACE_EXISTING]))
    (_private! target)
    (redacted config)))

(defn _migrate-legacy! [paths env]
  (let [canonical (:canonical paths)
        legacy (:legacy paths)]
    (if (or (read-config canonical) (nil? (read-config legacy)))
      (load-config paths env)
      (let [config (load-config paths env)]
        (save-config! canonical config)
        config))))
