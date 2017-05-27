(ns flowchart.view
  (:require [flowchart.elems :as elems]
            [flowchart.common :as common]
            [flowchart.state :as state]
            [reagent.core :as reagent :refer [atom]]
            [thi.ng.geom.svg.core :as svg]
            [pie]))

(defn mouse-label []
  (let [cursor-position (state/cursor-position)
        elem-type (state/elem-type)]
    (fn []
      [svg/text (map + [5 5] @cursor-position) (name @elem-type)])))

(defn mouse-arrow []
  (let [start-elem (state/start-elem)
        cursor-position (state/cursor-position)]
    (fn []
      (if-let [start @start-elem]
        [common/arrow @(state/actual-pos start) @cursor-position]))))

(defn elems []
  (let [elems (state/elems)]
    (fn []
      [:g
       (for [elem @elems]
         [elems/render elem])])))

(defn menu []
  (let [menu-position (state/menu-position)
        visible (state/menu-visible)]
    (let [radius 100
          inner-radius 20]
      (reagent/create-class
       {:reagent-render
        (fn []
          (let [[x y] @menu-position]
            [:div {:style {:position "absolute"
                           :left (- x radius)
                           :top (- y radius)
                           :display (if @visible "block" "none")}}]))
        :component-did-mount
        (fn [this]
          (let [node (reagent/dom-node this)
                menu (js/pie (clj->js [{:label "stmt"   :class "stmt"   :trigger #(do (state/hide-menu!) (state/add-elem! :stmt))}
                                       {:label "branch" :class "branch" :trigger #(do (state/hide-menu!) (state/add-elem! :branch))}
                                       {:label "term"   :class "term"   :trigger #(do (state/hide-menu!) (state/add-elem! :term))}
                                       {:label "note"   :class "note"   :trigger #(do (state/hide-menu!) (state/add-elem! :note))}])
                             (clj->js {:innerRadius inner-radius
                                       :radius radius}))]
            (.appendChild node menu)))}))))
