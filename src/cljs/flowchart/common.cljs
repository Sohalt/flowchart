(ns flowchart.common
  (:require [flowchart.state :as state]
            [thi.ng.geom.svg.core :as svg]
            [reagent.core :as reagent :refer [atom]]
            [goog.string :as gstring]))

(defn arrow [from to]
  [:g (svg/line-decorated from to nil (svg/arrow-head 10 (/ Math/PI 4) true))])

(defn draggable-component [id & body]
  (let [position (state/actual-pos id)]
    (fn [id & body]
      (into
       [:g {:transform (apply gstring/format "translate(%d,%d)"
                              @position)
            :on-mouse-down #(case (.-button %)
                              0 (state/link-start! id)
                              1 (state/drag-start! id))
            :on-mouse-up #(case (.-button %)
                            0 (state/link-end! id)
                            1 (state/drag-end! id))
            :on-mouse-over #(if @(state/right-pressed?) (state/remove-elem! id))}]
       body))))

(defn outlinks [id]
  (let [start (state/actual-pos id)
        outlinks (state/outlinks id)]
    (fn [id]
      (into [:g] (mapv (fn [dest] [arrow @start @(state/actual-pos dest)]) @outlinks)))))

(defn edit-text [[x y] text]
  (let [t (atom text)
        editing? (atom false)]
    (fn []
      [:g {:transform (gstring/format "translate(%d,%d)" x y)}
       (if @editing?
         [:foreignObject {:width "100" :height "20"}
          [:textarea {:style {:width "100%"
                              :height "100%"}
                      :auto-focus true
                      :value @t
                      :on-key-down (fn [e]
                                     (when (= 27 (.-keyCode e)) ; ESC
                                       (.blur (.-target e))))
                      :on-change (fn [e]
                                   (.preventDefault e)
                                   (reset! t (.-value (.-target e))))
                      :on-blur #(reset! editing? false)}]]
         [svg/text [10 20] @t {:on-click #(do (.stopPropagation %)
                                              (reset! editing? true))}])])))
