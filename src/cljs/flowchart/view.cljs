(ns flowchart.view
  (:require [flowchart.elems :as elems]
            [flowchart.state :as state]
            [thi.ng.geom.svg.core :as svg]))

(defn arrow [from to]
  [:g (svg/line-decorated from to nil (svg/arrow-head 10 (/ Math/PI 4) true))])

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
        [arrow @(state/actual-pos start) @cursor-position]))))

(defn elems []
  (let [elems (state/elems)]
    (fn []
      [:g
       (for [elem @elems]
         [elems/render elem])])))
