(ns flowchart.core
  (:require [reagent.core :as reagent :refer [atom]]
            [thi.ng.geom.svg.core :as svg]
            [goog.string :as gstring]
            [goog.string.format]))

;; -------------------------
;; State

(defonce elems (atom []))

;; -------------------------
;; Views

(defprotocol Component
  (render [_]))

(defrecord Stmt [x y text]
  Component
  (render [_]
    (let [w 120
          h 40
          w' (/ w 2)
          h' (/ h 2)
          x (atom x)
          y (atom y)
          left-pressed? (atom false)
          middle-pressed? (atom false)]
      (fn []
        [:g {:transform (gstring/format "translate(%d,%d)" (- @x w') (- @y h'))
             :on-mouse-down #(case (.-button %)
                               0 (reset! left-pressed? true)
                               1 (reset! middle-pressed? true))
             :on-mouse-move (fn [e]
                              (when @middle-pressed?
                                (reset! x (.-clientX e))
                                (reset! y (.-clientY e))))
             :on-mouse-up #(case (.-button %)
                             0 (reset! left-pressed? false)
                             1 (reset! middle-pressed? false))}
         [:rect {:width w :height h :style {:fill "blue" :stroke "black"}}]
         [:text {:x 5 :y (* h .6)}text]]))))

(defrecord Branch [x y text]
  Component
  (render [_]
    (let [l 80]
      [:g {:transform (gstring/format "translate(%d,%d)" x y)}
       [:rect {:width l :height l
               :style {:fill "none" :stroke "blue"}
               :transform "rotate(45)"}]
       [:polygon {:points ""}]
       [:text text]])))

(defn svg-component
  [& body]
  (svg/svg
   {:width (.-innerWidth js/window)
    :height (.-innerHeight js/window)
    :id "canvas"
    :style {:outline "2px solid black"
            :background-color "#fff"
            :display "block"}
    :on-mouse-down (fn [e]
                     (when (= 1 (.-buttons e))
                       (swap! elems conj (->Stmt (.-clientX e) (.-clientY e) "foo"))))}
   body))



(defn svg-page []
  [svg-component
   (for [elem @elems]
     [render elem])])

;; -------------------------
;; Initialize app

(defn mount-root []
  (reagent/render [#'svg-page] (.getElementById js/document "app")))

(defn init! []
  (mount-root))
