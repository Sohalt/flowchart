(ns flowchart.core
    (:require [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [accountant.core :as accountant]))

;; -------------------------
;; Views

(defn svg-component
  [elems]
  [:svg {:width "720"
         :height "400"
         :id "canvas"
         :style {:outline "2px solid black"
                 :background-color "#fff"}}
   elems])

(defn svg-page []
  [:div [:h2 "Some SVG"]
   [svg-component [:circle {:cx 60 :cy 50 :r 60 :style {:fill "black"}}]]])

;; -------------------------
;; Initialize app

(defn mount-root []
  (reagent/render [#'svg-page] (.getElementById js/document "app")))

(defn init! []
  (mount-root))
