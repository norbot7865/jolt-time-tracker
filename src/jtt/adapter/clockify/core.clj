(ns jtt.adapter.clockify.core
  "Clockify wire planning and normalized capability adapter."
  (:require [jtt.adapter.http.core :as http]))

(defn request [base workspace operation params]
  (let [root (str base "/workspaces/" workspace)]
    (case operation
      :current-user {:method :get :path (str base "/user")}
      :workspaces {:method :get :path (str base "/workspaces")}
      :projects {:method :get :path (str root "/projects/active")}
      :tasks {:method :get :path (str root "/projects/" (:project-id params) "/tasks")}
      :recent-entries {:method :get :path (str root "/user/" (:user-id params) "/time-entries")}
      :entry {:method :get :path (str root "/time-entries/" (:entry-id params))}
      :active-entry {:method :get :path (str root "/user/" (:user-id params) "/time-entries") :query {:in-progress true}}
      :start {:method :post :path (str root "/time-entries") :body params}
      :stop {:method :patch :path (str root "/time-entries/" (:entry-id params)) :body params}
      :update-running {:method :put :path (str root "/time-entries/" (:entry-id params)) :body params}
      :delete {:method :delete :path (str root "/time-entries/" (:entry-id params))}
      (throw (ex-info "unsupported Clockify operation" {:operation operation})))))

(defn headers [api-key]
  {"X-Api-Key" api-key "Accept" "application/json"})

(defn _entry [value]
  (when value
    {:id (str (:id value))
     :description (:description value)
     :project-id (or (:project-id value) (:projectId value))
     :task-id (or (:task-id value) (:taskId value))
     :billable (or (:billable value) (:isBillable value))
     :tags (vec (or (:tags value) (:tagIds value) []))
     :start-ms (:start-ms value)
     :end-ms (:end-ms value)}))

(defn _project [value]
  {:id (str (:id value)) :name (:name value)
   :client-id (or (:client-id value) (:clientId value))
   :client-name (or (:client-name value) (:clientName value))
   :billable (or (:billable value) (:isBillable value))})

(defn _task [value]
  {:id (str (:id value)) :name (:name value)
   :project-id (str (or (:project-id value) (:projectId value)))
   :status (:status value)})

(defn _adapt-body [operation body]
  (case operation
    (:start :stop :update-running :entry :active-entry) (_entry body)
    :projects (mapv _project body)
    :tasks (mapv _task body)
    :recent-entries (mapv _entry body)
    body))

(defn make-adapter [config executor]
  (let [base (:server-url config)
        workspace (:workspace-id config)
        api-key (:api-key config)
        call (fn [operation params]
               (let [result (executor operation
                                      (assoc (request base workspace operation params)
                                             :headers (headers api-key)))]
                 (if (:error result) result (update result :body #( _adapt-body operation %)))))]
    {:current-user #(call :current-user {})
     :workspaces #(call :workspaces {})
     :projects #(call :projects {})
     :tasks #(call :tasks %)
     :recent-entries #(call :recent-entries %)
     :entry #(call :entry %)
     :active-entry #(call :active-entry %)
     :start #(call :start %)
     :stop #(call :stop {:entry-id (:id %)})
     :update-running #(call :update-running {:entry-id (:id %) :input (:input %)})
     :delete #(call :delete {:entry-id (:id %)})}))

(defn _native-adapter [config]
  (make-adapter config http/execute))
