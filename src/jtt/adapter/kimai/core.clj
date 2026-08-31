(ns jtt.adapter.kimai.core
  "Kimai 2.x wire planning and normalized capability adapter."
  (:require [clojure.string :as str]))

(defn base-url [server]
  (str (str/replace server #"/+$" "") "/api"))

(defn headers [token]
  {"Authorization" (str "Bearer " token) "Accept" "application/json"})

(defn request [server operation params]
  (let [root (base-url server)]
    {:method (case operation
               (:start :stop :update) :post
               :delete :delete
               :get)
     :path (case operation
             :current-user (str root "/users/me")
             :workspaces (str root "/version")
             :projects (str root "/projects")
             :activities (str root "/activities")
             :tasks (str root "/activities")
             :timesheets (str root "/timesheets")
             :entry (str root "/timesheets/" (:id params))
             :active-entry (str root "/timesheets/active")
             :start (str root "/timesheets")
             :stop (str root "/timesheets/" (:id params) "/stop")
             :update (str root "/timesheets/" (:id params))
             :delete (str root "/timesheets/" (:id params))
             (throw (ex-info "unsupported Kimai operation" {:operation operation})))
     :body (when (#{:start :stop :update} operation) params)}))

(defn _id [value]
  (when (some? value)
    (str value)))

(defn _entry [value]
  (when value
    {:id (_id (:id value))
     :description (:description value)
     :project-id (_id (or (:project-id value) (:project value)))
     :task-id (_id (or (:task-id value) (:activity value)))
     :billable (or (:billable value) (:isBillable value))
     :tags (vec (or (:tags value) []))
     :start-ms (:start-ms value)
     :end-ms (:end-ms value)}))

(defn _project [value]
  {:id (_id (:id value)) :name (:name value)
   :client-id (_id (or (:client-id value) (:customer value)))
   :client-name (:customer-name value)
   :billable (or (:billable value) (:isBillable value))})

(defn _activity [value]
  {:id (_id (:id value)) :name (:name value)
   :project-id (_id (or (:project-id value) (:project value)))
   :status (:visible value)})

(defn _adapt-body [operation body]
  (case operation
    (:start :stop :update :entry :active-entry) (_entry body)
    :projects (mapv _project body)
    (:activities :tasks) (mapv _activity body)
    :timesheets (mapv _entry body)
    :workspaces [{:id "default" :name "Kimai"}]
    body))

(defn _make-adapter [config executor]
  (let [server (:server-url config)
        token (:api-key config)
        call (fn [operation params]
               (let [result (executor operation
                                      (assoc (request server operation params)
                                             :headers (headers token)))]
                 (if (:error result) result (update result :body #(_adapt-body operation %)))))]
    {:current-user #(call :current-user {})
     :workspaces #(call :workspaces {})
     :projects #(call :projects {})
     :tasks #(call :tasks %)
     :recent-entries #(call :timesheets %)
     :entry #(call :entry {:id (:entry-id %)})
     :active-entry #(call :active-entry {})
     :start #(call :start %)
     :stop #(call :stop {:id (:id %)})
     :update-running #(call :update {:id (:id %) :input (:input %)})
     :delete #(call :delete {:id (:id %)})}))
