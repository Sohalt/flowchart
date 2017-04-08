(ns flowchart.core
  (:require [reagent.core :as reagent :refer [atom]]
            [goog.string :as gstring]
            [goog.string.format]))


;; -------------------------
;; Views

(defn stmt [x y text]
  (let [w 120
        h 40]
    [:g {:transform (gstring/format "translate(%d,%d)" x y)}
     [:rect {:width w :height h :style {:fill "none" :stroke "black"}}]
     [:text {:x 5 :y (* h .6)}text]]))

(defn branch [x y text]
  (let [l 80]
    [:g {:transform (gstring/format "translate(%d,%d)" x y)}
     [:rect { :width l :height l
                :style {:fill "none" :stroke "blue"}
                :transform "rotate(45)"}]
     [:text text]]))

(defn svg-component
  [& elems]
  [:svg {:width "720"
         :height "400"
         :id "canvas"
         :style {:outline "2px solid black"
                 :background-color "#fff"}}
   elems])

(defn svg-page []
  [:div [:h2 "Some SVG"]
   [svg-component
    [:circle {:cx 60 :cy 50 :r 60 :style {:fill "black"}}]
    [:circle {:cx 60 :cy 10 :r 60 :style {:fill "red"}}]
    (map #(stmt 120 % (str "abc " %)) (range 20 200 50))
    [branch 300 100 "bar"]]])

;; -------------------------
;; Initialize app

(defn mount-root []
  (reagent/render [#'svg-page] (.getElementById js/document "app")))

(defn init! []
  (mount-root))
