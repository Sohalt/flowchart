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
          h 40]
      (.log js/console "foo")
      [:g {:transform (gstring/format "translate(%d,%d)" x y)
           :on-click #(js/alert "foo")}
       [:rect {:width w :height h :style {:fill "none" :stroke "black"}}]
       [:text {:x 5 :y (* h .6)}text]])))

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
  [& elems]
  (svg/svg
   {:width "720"
    :height "400"
    :id "canvas"
    :style {:outline "2px solid black"
            :background-color "#fff"}}
   elems))

(defn svg-page []
  [:div [:h2 "Some SVG"]
   [svg-component
    (map render @elems)]])

;; -------------------------
;; Initialize app

(defn mount-root []
  (reagent/render [#'svg-page] (.getElementById js/document "app")))

(defn init! []
  (mount-root))
