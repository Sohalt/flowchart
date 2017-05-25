(ns flowchart.elems
  (:require [flowchart.common :as common]
            [thi.ng.geom.svg.core :as svg]))

(defmulti render (fn [elem] (:type elem)))

(defmethod render :start [{:keys [id text]}]
  (let [w 60
        h 20]
    [:g
     [common/draggable-component
      id
      [:ellipse {:cx w :cy h :rx w :ry h :style {:fill "plum"}}]
      [common/edit-text [10 10] text]]
     [common/outlinks id]]))

(defmethod render :stmt [{:keys [id text]}]
  (let [w 120
        h 40]
    [:g
     [common/draggable-component
      id
      (svg/rect [0 0] w h {:style {:fill "blue"}})
      [common/edit-text [5 5] text]]
     [common/outlinks id]]))

(defmethod render :branch [{:keys [id text]}]
  (let [w 140
        h 60
        w' (/ w 2)
        h' (/ h 2)]
    [:g
     [common/draggable-component
      id
      (svg/polygon [[0 h'] [w' 0] [w h'] [w' h]] {:style {:fill "orange"}})
      [common/edit-text [40 40] text]]
     [common/outlinks id]]))

(defmethod render :note [{:keys [id text]}]
  (let [corner 20
        w 140
        h 200]
    [:g
     [common/draggable-component
      id
      (svg/polygon [[corner 0] [w 0] [w h] [0 h] [0 corner]]
                   {:style {:fill "beige"}})
      (svg/polygon [[corner 0] [corner corner] [0 corner]]
                   {:style {:fill "burlywood"}})
      [common/edit-text [(* 1.2 corner) (* 1.2 corner)] text]]]))

