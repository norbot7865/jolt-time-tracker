(ns jtt.adapter.provider.registry-test
  (:require [clojure.test :refer [deftest is]]
            [jtt.adapter.provider.registry :as registry]))

(deftest selects-clockify-default-and-kimai
  (let [adapters {:clockify :clockify-adapter :kimai :kimai-adapter}]
    (is (= :clockify-adapter (registry/select nil adapters)))
    (is (= :kimai-adapter (registry/select :kimai adapters)))
    (is (thrown-with-msg? clojure.lang.ExceptionInfo #"unsupported provider"
                          (registry/select :unknown adapters)))))
