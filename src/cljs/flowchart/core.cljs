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
          pos (atom [x y])
          drag? (atom false)]
      (fn []
        [:g {:transform (apply gstring/format "translate(%d,%d)"
                               (map + @pos
                                    (if @drag?
                                      (get-in @mouse-state [:middle :delta])
                                      [0 0])))
             :on-mouse-down #(case (.-button %)
                               0 (swap! mouse-state assoc-in [:left :start-elem] :stmt)
                               1 (reset! drag? true))
             :on-mouse-up #(when (= 1 (.-button %))
                             (swap! pos (partial map + (get-in @mouse-state [:middle :delta])))
                             (reset! drag? false))
             :key (gensym)}
         [:rect {:width w :height h :style {:fill "blue"}}]
         [:text {:x 5 :y (* h .6)} (str text @drag?)]]))))

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
  [:g (svg/line-decorated from to nil (svg/arrow-head 10 (/ Math/PI 4) true))])

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
  (swap! mouse-state update button merge {:pressed? false :delta [0 0] :start-elem nil}))

(defn svg-component
  [& body]
  (svg/svg
   {:width (.-innerWidth js/window)
    :height (.-innerHeight js/window)
    :id "canvas"
    :style {:background-color "#fff"
            :display "block"
            :stroke "black"}
    :on-click (fn [e] (when (= 0 (.-button e)) (swap! elems conj (->Stmt (.-clientX e) (.-clientY e) "foo"))))
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
                   (button-up! (case (.-button e)
                                 0 :left
                                 1 :middle
                                 2 :right)))}
   body))

(defn svg-page []
  [:div
   [svg-component
    [:text {:x 50 :y 50} (with-out-str (pprint @mouse-state))]
    (for [elem @elems]
        [render elem])
    (let [s @mouse-state
          from (get-in s [:left :dragstart])
          to (map + from (get-in s [:left :delta]))]
      (when (and (get-in s [:left :pressed?]) (= (get-in s [:left :start-elem]) :stmt))
        [arrow from to]))]])

;; -------------------------
;; Initialize app

(defn mount-root []
  (reagent/render [#'svg-page] (.getElementById js/document "app")))

(defn init! []
  (mount-root))
