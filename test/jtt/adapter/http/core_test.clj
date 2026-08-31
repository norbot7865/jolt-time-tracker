(ns jtt.adapter.http.core-test
  (:require [clojure.test :refer [deftest is]]
            [jtt.adapter.http.core :as http]))

(deftest bounds-errors-without-leaking-request-credentials
  (let [oversized (apply str (repeat 10 "x"))]
    (is (= :response-too-large
           (get-in (http/execute :snapshot {:method :get
                                             :path "http://127.0.0.1:18080/ok"
                                             :headers {"Authorization" "Bearer synthetic"}
                                             :max-body-chars 1})
                   [:error :cause])))
    (is (= :non-2xx
           (get-in (http/execute :snapshot {:method :get
                                             :path "http://127.0.0.1:18080/missing"})
                   [:error :cause])))
    (is (not (.contains (pr-str (http/_safe-error :snapshot oversized)) "Authorization")))))
