(ns jtt.frontend.gtk.core-test
  (:require [clojure.test :refer [deftest is]]
            [jtt.frontend.gtk.core :as gtk]))

(deftest lifecycle-and-reload-preserve-state
  (gtk/stop!)
  (is (= true (:running (gtk/start!))))
  (is (= 1 (:reloads (gtk/reload!))))
  (is (= false (:running (gtk/stop!)))))
