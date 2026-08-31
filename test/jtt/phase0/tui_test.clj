(ns jtt.phase0.tui-test
  (:require [clojure.test :refer [deftest is]]
            [glimmer-tui.layout :as layout]
            [glimmer-tui.render :as render]
            [glimmer-tui.screen :as screen]
            [glimmer-tui.widget :as widget]
            [glimmer-tui.widgets]))

(deftest buffer-screen-renders-and-interacts-headlessly
  (let [scr (screen/buffer-screen 12 2)
        root (widget/create! :vbox {})
        _ (widget/append-child! :vbox root (widget/create! :label {:label "ready"}))
        tree (layout/layout (widget/snapshot root) 12 2)]
    (render/render! scr tree {})
    (is (= ["ready" ""] (screen/lines scr)))
    (is (= [12 2] (screen/size scr)))
    (screen/present! scr)))
