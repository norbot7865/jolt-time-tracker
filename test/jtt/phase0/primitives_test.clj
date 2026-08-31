(ns jtt.phase0.primitives-test
  (:require [clojure.data.json :as json]
            [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [jolt.http-client :as http]
            [jolt.http.platform :as platform]))

(deftest http-json-round-trip
  (platform/set-max-response-ms! 2000)
  (let [response (http/get (or (System/getenv "JTT_FIXTURE_URL")
                               "http://127.0.0.1:18080/ok")
                           {:conn-timeout 1000 :socket-timeout 1000})
        payload (json/read-str (:body response) :key-fn keyword)]
    (is (= 200 (:status response)))
    (is (= {:ok true :message "synthetic"} payload))
    (is (<= (count (:body response)) 64))
    (platform/set-max-response-ms! nil)))

(deftest time-crosses-local-day-without-unicode-assumptions
  (let [zone (java.time.ZoneOffset/of "-05:00")
        before (java.time.Instant/parse "2024-01-02T04:59:59Z")
        after (java.time.Instant/parse "2024-01-02T05:00:00Z")]
    (is (= (java.time.LocalDate/of 2024 1 1)
           (.toLocalDate (java.time.ZonedDateTime/ofInstant before zone))))
    (is (= (java.time.LocalDate/of 2024 1 2)
           (.toLocalDate (java.time.ZonedDateTime/ofInstant after zone))))))

(deftest config-save-is-private-and-same-directory-replace
  (let [dir (java.nio.file.Files/createTempDirectory "jtt-config" (make-array java.nio.file.attribute.FileAttribute 0))
        path (.resolve dir "config.edn")
        tmp (.resolve dir "config.edn.tmp")
        secret "synthetic-token-not-for-errors"]
    (spit (str tmp) (pr-str {:token secret :workspace "demo"}))
    (java.nio.file.Files/setPosixFilePermissions
     tmp (java.nio.file.attribute.PosixFilePermissions/fromString "rw-------"))
    (java.nio.file.Files/move tmp path (into-array java.nio.file.CopyOption
                                                   [java.nio.file.StandardCopyOption/ATOMIC_MOVE
                                                    java.nio.file.StandardCopyOption/REPLACE_EXISTING]))
    (is (= dir (.getParent path)))
    (is (java.nio.file.Files/exists path (make-array java.nio.file.LinkOption 0)))
    (is (= "rw-------"
           (java.nio.file.attribute.PosixFilePermissions/toString
            (java.nio.file.Files/getPosixFilePermissions path))))
    (is (= secret (:token (read-string (slurp (str path))))))
    (is (not (.exists (io/file (str tmp)))))))
