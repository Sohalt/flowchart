(ns flowchart.core
  (:require [reagent.core :as reagent :refer [atom]]
            [cljs.pprint :refer [pprint]]
            [thi.ng.geom.svg.core :as svg]
            [goog.string :as gstring]
            [goog.string.format]))

;; -------------------------
;; State

(defonce elems (atom []))

(defonce mouse-state (atom {:left {:pressed? false :dragstart [0 0] :delta [0 0]}
                            :middle {:pressed? false :dragstart [0 0] :delta [0 0]}
                            :right {:pressed? false :dragstart [0 0] :delta [0 0]}}))


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
             :on-mouse-up #(do (case (.-button %)
                                 0 (reset! left-pressed? false)
                                 1 (reset! middle-pressed? false)))}
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

(defn arrow [from to]
  (svg/line from to {:style {:stroke "black"}}))

(defn- button-down! [button x y]
  (swap! mouse-state update button merge {:pressed? true :dragstart [x y]}))

(defn update-delta [x' y' s k]
  (let [[x y] (get-in s [k :dragstart])]
    (assoc-in s [k :delta] [(- x' x) (- y' y)])))


(defn- update-drag [state x y]
  (let [pressed-keys (keys (filter (fn [[button {:keys [pressed?]}]] pressed?)
                                   state))]
    (reduce (partial update-delta x y) state pressed-keys)))

(defn- mouse-move! [x y]
  (swap! mouse-state update-drag x y))

(defn- button-up! [button]
  (swap! mouse-state update button merge {:pressed? false :delta [0 0]}))

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
                     (let [x (.-clientX e)
                           y (.-clientY e)]
                       (.preventDefault e)
                       (button-down! (case (.-button e)
                                       0 :left
                                       1 :middle
                                       2 :right) x y)
                       false))
    :on-mouse-move (fn [e]
                     (let [x (.-clientX e)
                           y (.-clientY e)]
                       (mouse-move! x y)))
    :on-mouse-up (fn [e]
                   (.preventDefault e)
                   (button-up! (case (.-button e)
                                 0 :left
                                 1 :middle
                                 2 :right))
                   false)}
   body))

(defn svg-page []
  [:div
   [svg-component
    [:text {:x 50 :y 50} (with-out-str (pprint @mouse-state))]
    (let [s @mouse-state
          from (get-in s [:left :dragstart])
          to (map + from (get-in s [:left :delta]))]
      (when (get-in s [:left :pressed?])
        [arrow from to]))
    #_(for [elem @elems]
        [render elem])]])

;; -------------------------
;; Initialize app

(defn mount-root []
  (reagent/render [#'svg-page] (.getElementById js/document "app")))

(defn init! []
  (mount-root))
