(ns flowchart.view
  (:require [flowchart.elems :as elems]
            [flowchart.common :as common]
            [flowchart.state :as state]
            [flowchart.persistence :as persistence]
            [cljs.pprint :refer [pprint]]
            [reagent.core :as reagent :refer [atom]]
            [thi.ng.geom.svg.core :as svg]
            [pie]
            [clojure.string :as str]))

(defn debug []
  [:div {:style {:position :absolute
                 :top 0
                 :left 0
                 :right 0
                 :color "white"
                 :background-color "#00000055"}}
   [:pre
    (with-out-str (pprint @state/mouse-state))
    (with-out-str (pprint @state/elems'))]])

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

(defn controls []
  [:div
   [:button {:on-click #(persistence/save!)} "save"]
   [:button {:on-click #(persistence/load!)} "load"]])
