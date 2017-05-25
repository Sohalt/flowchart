(ns flowchart.core
  (:require [flowchart.state :as state]
            [flowchart.view :as view]
            [reagent.core :as reagent :refer [atom]]
            [cljs.pprint :refer [pprint]]
            [thi.ng.geom.svg.core :as svg]
            [goog.string :as gstring]
            [goog.string.format]))

;; -------------------------
;; Views

(defn wrap-exact-event-target [f]
  (fn [e]
    (when (= (.-target e) (.-currentTarget e))
      (f e))))

(defn wrap-stop-propagation [f]
  (fn [e]
    (.stopPropagation e)
    (f e)))

(defn svg-component
  [& body]
  (svg/svg
   {:width (.-innerWidth js/window)
    :height (.-innerHeight js/window)
    :id "canvas"
    :style {:background-color "#fff"
            :display "block"
            :stroke "black"}
    :on-click (fn [e] (do (.preventDefault e)
                          (when (= 0 (.-button e))
                            (state/add-elem! (.-clientX e) (.-clientY e)))))
    :on-mouse-down (fn [e]
                     (let [x (.-clientX e)
                           y (.-clientY e)]
                       (when-not (and (= "TEXTAREA" (.-tagName (.-target e)))
                                      (= 0 (.-button e)))
                         (.preventDefault e)
                         (state/button-down! (case (.-button e)
                                         0 :left
                                         1 :middle
                                         2 :right) x y))
                       false))
    :on-mouse-move (fn [e]
                     (let [x (.-clientX e)
                           y (.-clientY e)]
                       (state/mouse-move! x y)))
    :on-mouse-up (fn [e]
                   (state/button-up! (case (.-button e)
                                 0 :left
                                 1 :middle
                                 2 :right)))}
   body))

(defn svg-page []
  [svg-component
   [svg/text [50 50] (with-out-str (pprint @state/mouse-state))]
   [view/mouse-label]
   [view/mouse-arrow]
   [view/elems]])

;; -------------------------
;; Initialize app

(defn mount-root []
  (reagent/render [#'svg-page] (.getElementById js/document "app")))

(defn init! []
  (set! (.-onkeydown js/window) (fn [e] (state/handle-key-press! (.-keyCode e))))
  (mount-root))
