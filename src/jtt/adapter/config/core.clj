(ns jtt.adapter.config.core
  "Desktop JSON config boundary; Android credentials are not accepted here."
  (:require [clojure.data.json :as json]
            [clojure.java.io :as io]))

(defn config-path [{:keys [config-home home]}]
  (let [root (or config-home (str home "/.config"))]
    (str root "/gtt/config.json")))

(defn read-config [path]
  (when (.exists (io/file path))
    (json/read-str (slurp path) :key-fn keyword)))

(defn redacted [config]
  (dissoc config :api-key :token :authorization))

(defn save-config! [path config]
  (let [target (io/file path) tmp (io/file (str path ".tmp"))]
    (.mkdirs (.getParentFile target))
    (spit tmp (json/write-str config))
    (when-not (.renameTo tmp target)
      (throw (ex-info "config replacement failed" {:operation :config-save})))
    (redacted config)))
