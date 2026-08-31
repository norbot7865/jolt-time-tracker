(ns jtt.frontend.cli.core
  "Strict, toolkit-free command entry point over injected application capabilities."
  (:require [babashka.cli :as cli]
            [jtt.application.core :as app]))

(def commands #{"configure" "discover" "config" "status" "start" "stop" "update" "continue" "list" "projects" "tasks" "delete"})

(defn run [session args]
  (let [cmd (first args) rest-args (next args)]
    (when-not (commands cmd)
      (throw (ex-info "unknown command" {:command cmd})))
    (case cmd
      "status" (app/snapshot session)
      "discover" (app/discover session)
      "start" (app/start session (cli/parse-opts rest-args {:coerce {:description str}}))
      "stop" {:error {:type :cli/not-implemented :command :stop}}
      "update" {:error {:type :cli/not-implemented :command :update}}
      "continue" {:error {:type :cli/not-implemented :command :continue}}
      "delete" {:error {:type :cli/not-implemented :command :delete}}
      {:error {:type :cli/not-implemented :command (keyword cmd)}})))
