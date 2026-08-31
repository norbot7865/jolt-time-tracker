(ns jtt.adapter.config.core-test
  (:require [clojure.test :refer [deftest is]]
            [jtt.adapter.config.core :as config]))

(defn temp-dir []
  (str (java.nio.file.Files/createTempDirectory
        "jtt-config-test" (make-array java.nio.file.attribute.FileAttribute 0))))

(deftest config-paths-overrides-and-redaction
  (let [dir (temp-dir)
        paths {:canonical (config/config-path {:config-home dir :home "/unused"})
               :legacy (config/_legacy-config-path {:home dir})}
        value {:provider "kimai" :server-url "https://synthetic.invalid" :api-key "secret"}]
    (is (.endsWith (:canonical paths) "/gtt/config.json"))
    (is (.endsWith (:legacy paths) "/.config/clockify-tui/config.json"))
    (is (= {:provider "kimai" :server-url "https://synthetic.invalid" :api-key "[REDACTED]"}
           (config/redacted value)))
    (config/save-config! (:canonical paths) value)
    (is (= "kimai" (:provider (config/read-config (:canonical paths)))))
    (is (= "new" (:api-key (config/apply-overrides value
                                                    {"GTT_API_KEY" "new"
                                                     "CLOCKIFY_API_KEY" "old"}))))))

(deftest migrates-legacy-with-private-atomic-output
  (let [dir (temp-dir)
        paths {:canonical (config/config-path {:config-home dir :home dir})
               :legacy (config/_legacy-config-path {:home dir})}
        legacy {:provider "clockify" :api-key "synthetic" :user-id "u"}]
    (config/save-config! (:legacy paths) legacy)
    (is (= legacy (config/_migrate-legacy! paths {})))
    (is (= legacy (config/read-config (:canonical paths))))
    (is (= "rw-------"
           (java.nio.file.attribute.PosixFilePermissions/toString
            (java.nio.file.Files/getPosixFilePermissions
             (.toPath (java.io.File. (:canonical paths)))))))
    (is (= "override" (:api-key (config/load-config paths {"CLOCKIFY_API_KEY" "override"}))))))
