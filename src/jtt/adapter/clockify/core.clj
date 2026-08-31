(ns jtt.adapter.clockify.core
  "Clockify wire planning kept outside domain values.")

(defn request [base workspace operation params]
  (let [root (str base "/workspaces/" workspace)]
    (case operation
      :workspaces {:method :get :path (str base "/workspaces")}
      :projects {:method :get :path (str root "/projects/active")}
      :tasks {:method :get :path (str root "/projects/" (:project-id params) "/tasks")}
      :recent-entries {:method :get :path (str root "/user/" (:user-id params) "/time-entries")}
      :active-entry {:method :get :path (str root "/user/" (:user-id params) "/time-entries") :query {:in-progress true}}
      :start {:method :post :path (str root "/time-entries") :body params}
      :stop {:method :patch :path (str root "/time-entries/" (:entry-id params)) :body params}
      :update-running {:method :put :path (str root "/time-entries/" (:entry-id params)) :body params}
      :delete {:method :delete :path (str root "/time-entries/" (:entry-id params))}
      (throw (ex-info "unsupported Clockify operation" {:operation operation})))))

(defn headers [api-key]
  {"X-Api-Key" api-key "Accept" "application/json"})
