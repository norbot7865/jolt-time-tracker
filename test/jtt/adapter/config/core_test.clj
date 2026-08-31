(ns jtt.adapter.config.core-test
  (:require [clojure.test :refer [deftest is]]
            [jtt.adapter.config.core :as config]))

(deftest config-path-and-redaction
  (let [dir (str (java.nio.file.Files/createTempDirectory "jtt-config-test" (make-array java.nio.file.attribute.FileAttribute 0)))
        path (config/config-path {:config-home dir :home "/unused"})
        value {:provider "kimai" :server-url "https://synthetic.invalid" :api-key "secret"}]
    (is (.endsWith path "/gtt/config.json"))
    (is (= {:provider "kimai" :server-url "https://synthetic.invalid"}
           (config/redacted value)))
    (is (= (config/redacted value) (config/save-config! path value)))
    (is (= "kimai" (:provider (config/read-config path))))))
