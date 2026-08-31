(ns jtt.adapter.http.core
  "Pinned Jolt HTTP transport with bounded, redacted results."
  (:require [clojure.data.json :as json]
            [jolt.http-client :as http]
            [jolt.http.platform :as platform]))

(def default-timeout-ms 10000)
(def default-max-body-chars 1048576)

(defn _safe-error [operation cause]
  {:error {:type :provider/http
           :operation operation
           :message "provider request failed"
           :cause cause}})

(defn _options [{:keys [headers body timeout-ms]}]
  (cond-> {:headers headers
           :conn-timeout (or timeout-ms default-timeout-ms)
           :socket-timeout (or timeout-ms default-timeout-ms)}
    body (assoc :body (json/write-str body) :content-type :json)))

(defn _call [request options]
  (case (:method request)
    :get (http/get (:path request) options)
    :post (http/post (:path request) options)
    :put (http/put (:path request) options)
    :delete (http/delete (:path request) options)
    (http/request (assoc options :method (:method request) :url (:path request)))))

(defn execute [operation request]
  (let [limit (or (:max-body-chars request) default-max-body-chars)]
    (platform/set-max-response-ms! (or (:timeout-ms request) default-timeout-ms))
    (try
      (let [response (_call request (_options request))
            body (:body response)]
        (cond
          (not (<= 200 (:status response) 299)) (_safe-error operation :non-2xx)
          (not (<= (count body) limit)) (_safe-error operation :response-too-large)
          :else {:status (:status response)
                 :body (json/read-str body :key-fn keyword)}))
      (catch Exception error
        (_safe-error operation (if (:status (ex-data error)) :non-2xx :transport)))
      (finally (platform/set-max-response-ms! nil)))))
