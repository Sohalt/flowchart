(ns flowchart.common
  (:require [flowchart.state :as state]
            [thi.ng.geom.svg.core :as svg]
            [reagent.core :as reagent :refer [atom]]
            [goog.string :as gstring]
            [clojure.string :as str]))

(defn multiline-text [[x y] text & [attrs]]
  (into [:text (svg/svg-attribs attrs {:x x :y y})]
        (map-indexed (fn [i line]
                       [:tspan {:x x :dy "1.2em" #_(str (+ y (* i 1.2)) "em")} line])
                     (str/split-lines text))))

(defn arrow [from to]
  [:g (svg/line-decorated from to nil (svg/arrow-head 10 (/ Math/PI 4) true))])

(defn draggable-component [id & body]
  (into
   [:g {:transform (apply gstring/format "translate(%d,%d)"
                          @(state/actual-pos id))
        :on-mouse-down #(case (.-button %)
                          0 (state/link-start! id)
                          1 (state/drag-start! id))
        :on-mouse-up #(case (.-button %)
                        0 (state/link-end! id)
                        1 (state/drag-end! id))
        :on-mouse-over #(if @(state/right-pressed?) (state/remove-elem! id))}]
   body))

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
         [multiline-text [10 20] @t {:on-click #(do (.stopPropagation %)
                                              (reset! editing? true))}])])))
