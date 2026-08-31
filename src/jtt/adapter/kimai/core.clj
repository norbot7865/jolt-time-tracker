(ns jtt.adapter.kimai.core
  "Kimai wire planning; provider details stop at this adapter boundary."
  (:require [clojure.string :as str]))

(defn base-url [server]
  (str (str/replace server #"/+$" "") "/api"))

(defn headers [token]
  {"Authorization" (str "Bearer " token) "Accept" "application/json"})

(defn request [server operation params]
  (let [root (base-url server)]
    {:method (if (#{:start :stop :update} operation) :post :get)
     :path (case operation
             :projects (str root "/projects")
             :activities (str root "/activities")
             :timesheets (str root "/timesheets")
             :start (str root "/timesheets")
             :stop (str root "/timesheets/" (:id params))
             :update (str root "/timesheets/" (:id params))
             (throw (ex-info "unsupported Kimai operation" {:operation operation})))
     :body (when (#{:start :stop :update} operation) params)}))
