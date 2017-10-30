(ns flowchart.events
  (:require [flowchart.state :as state]
            [historian.keys]
            [goog.events :as events])
  (:import [goog.events EventType]))


(defn bind-ctrl-space []
  (events/listen js/window EventType.KEYDOWN
                 #(when (and (.-ctrlKey %) (= (.-keyCode %) 32)) ;CTRL+SPACE
                    (.preventDefault %)
                    (swap! state/debug not))))

(defn bind-keys []
  (historian.keys/bind-keys)
  (bind-ctrl-space))
