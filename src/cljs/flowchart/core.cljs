(ns flowchart.core
    (:require [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [accountant.core :as accountant]))

;; -------------------------
;; Views

(defn home-page []
  [:div [:h2 "Welcome to flowchart"]
   [:div
    [:a {:href "/about"} "go to about page"]
    [:a {:href "/svg"} "go to the svg page"]]])

(defn about-page []
  [:div [:h2 "About flowchart"]
   [:div
    [:a {:href "/"} "go to the home page"]
    [:a {:href "/svg"} "go to the svg page"]]])

(defn svg-component
  [elems]
  [:svg {:width "720"
         :height "400"
         :id "canvas"
         :style {:outline "2px solid black"
                 :background-color "#fff"}}
   elems])

(defn svg-page []
  [:div [:h2 "About flowchart"]
   [:div [:a {:href "/"} "go to the home page"]]
   [svg-component [:circle {:cx 50 :cy 50 :r 60 :style {:fill "black"}}]]])

(defn current-page []
  [:div [(session/get :current-page)]])

;; -------------------------
;; Routes

(secretary/defroute "/" []
  (session/put! :current-page #'home-page))

(secretary/defroute "/about" []
  (session/put! :current-page #'about-page))

(secretary/defroute "/svg" []
  (session/put! :current-page #'svg-page))

;; -------------------------
;; Initialize app

(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (accountant/configure-navigation!
    {:nav-handler
     (fn [path]
       (secretary/dispatch! path))
     :path-exists?
     (fn [path]
       (secretary/locate-route path))})
  (accountant/dispatch-current!)
  (mount-root))
