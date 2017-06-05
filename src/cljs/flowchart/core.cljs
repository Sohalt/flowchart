(ns flowchart.core
  (:require [flowchart.state :as state]
            [flowchart.view :as view]
            [flowchart.events :as events]
            [reagent.core :as reagent]
            [thi.ng.geom.svg.core :as svg]))

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
    :on-mouse-down (fn [e]
                     (let [x (.-clientX e)
                           y (.-clientY e)
                           f (wrap-exact-event-target #(state/show-menu!))]
                       (when-not (and (= "TEXTAREA" (.-tagName (.-target e)))
                                      (= 0 (.-button e)))
                         (.preventDefault e)
                         (state/button-down! (case (.-button e)
                                         0 :left
                                         1 :middle
                                         2 :right) x y)
                         (when (= 0 (.-button e)) (f e)))
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
   [view/mouse-arrow]
   [view/elems]])

(defn app-page []
  [:div {:on-mouse-up #(state/hide-menu!)}
   [view/debug]
   [svg-page]
   [view/menu]
   [view/controls]])

;; -------------------------
;; Initialize app

(defn mount-root []
  (reagent/render [#'app-page] (.getElementById js/document "app")))

(defn init! []
  (mount-root)
  (events/bind-keys))
