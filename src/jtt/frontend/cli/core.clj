(ns jtt.frontend.cli.core
  "Strict, toolkit-free command entry point over injected application capabilities."
  (:require [babashka.cli :as cli]
            [clojure.data.json :as json]
            [jtt.adapter.config.core :as config]
            [jtt.application.core :as app]
            [jtt.bootstrap.core :as bootstrap]))

(def commands #{"configure" "discover" "config" "status" "start" "stop" "update" "continue" "list" "projects" "tasks" "delete" "help"})

(defn _error [cause]
  {:error {:type :cli/error :cause cause}})

(defn _options [args]
  (dissoc (cli/parse-opts args) :_))

(defn _snapshot [session]
  (let [result (app/snapshot session)]
    (if (:error result) result result)))

(defn _entry [session options]
  (let [snapshot (_snapshot session)
        id (:id options)]
    (or (when (nil? id) (:active snapshot))
        (some #(when (= id (:id %)) %) (:entries snapshot)))))

(defn _input [options]
  (select-keys options [:description :project-id :task-id :billable :tags]))

(defn run [session args]
  (let [cmd (or (first args) "help")
        options (_options (next args))]
    (if-not (commands cmd)
      (_error :unknown-command)
      (case cmd
        "help" {:help (sort commands)}
        "configure" (config/redacted (dissoc session :capabilities))
        "config" (config/redacted (dissoc session :capabilities))
        "status" (_snapshot session)
        "discover" (app/discover session)
        "start" (app/start session (_input options))
        "stop" (app/stop session (_entry session options))
        "update" (app/update-running session (_entry session options) (_input options))
        "continue" (app/continue-workflow session (_entry session options))
        "delete" (app/delete session (_entry session options))
        "list" (:entries (_snapshot session))
        "projects" (app/invoke session :projects)
        "tasks" (app/invoke session :tasks options)))))

(defn _write-result! [value json?]
  (binding [*out* (if (:error value) *err* *out*)]
    (println (if json? (json/write-str value) (pr-str value)))))

(defn _exit-code [result]
  (if (:error result) 2 0))

(defn -main [& args]
  (let [json? (boolean (some #{"--json"} args))
        args (vec (remove #{"--json"} args))]
    (try
      (let [result (run (bootstrap/compose {:home (System/getProperty "user.home")}) args)]
        (_write-result! result json?)
        (System/exit (_exit-code result)))
      (catch Exception _
        (binding [*out* *err*] (println "jtt: command failed"))
        (System/exit 2)))))
