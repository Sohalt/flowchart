(ns flowchart.elems
  (:require [flowchart.common :as common]
            [flowchart.state :as state]
            [thi.ng.geom.svg.core :as svg]))

(defmulti render (fn [elem] (:type elem)))

(defmethod render :term [{:keys [id text]}]
  (let [w 120
        h 40
        w' (/ w 2)
        h' (/ h 2)
        padding 10]
    [:g
     [common/draggable-component
      id
      [:ellipse {:cx w' :cy h' :rx w' :ry h' :style {:fill "plum"}}]
      [common/edit-text [padding padding] (- w (* 2 padding)) (- h (* 2 padding)) (state/text id)]]
     [common/outlinks id]]))

(defmethod render :stmt [{:keys [id text]}]
  (let [w 120
        h 40
        padding 5]
    [:g
     [common/draggable-component
      id
      (svg/rect [0 0] w h {:style {:fill "blue"}})
      [common/edit-text [padding padding] (- w (* 2 padding)) (- h (* 2 padding)) (state/text id)]]
     [common/outlinks id]]))

(defmethod render :branch [{:keys [id text]}]
  (let [w 140
        h 60
        w' (/ w 2)
        h' (/ h 2)
        padding 20]
    [:g
     [common/draggable-component
      id
      (svg/polygon [[0 h'] [w' 0] [w h'] [w' h]] {:style {:fill "orange"}})
      [common/edit-text [padding padding] (- w (* 2 padding)) (- h (* 2 padding)) (state/text id)]]
     [common/outlinks id]]))

(defmethod render :note [{:keys [id text]}]
  (let [corner 20
        w 140
        h 200
        padding 5]
    [:g
     [common/draggable-component
      id
      (svg/polygon [[corner 0] [w 0] [w h] [0 h] [0 corner]]
                   {:style {:fill "beige"}})
      (svg/polygon [[corner 0] [corner corner] [0 corner]]
                   {:style {:fill "burlywood"}})
      [common/edit-text [padding (+ corner padding)] (- w (* 2 padding)) (- h (* 2 padding) corner) (state/text id)]]]))

