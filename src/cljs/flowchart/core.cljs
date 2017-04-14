(ns flowchart.core
  (:require [reagent.core :as reagent :refer [atom]]
            [cljs.pprint :refer [pprint]]
            [thi.ng.geom.svg.core :as svg]
            [goog.string :as gstring]
            [goog.string.format]))

;; -------------------------
;; State

(defonce elems (atom {}))

(defonce mouse-state (atom {:position [0 0]
                            :left {:pressed? false :dragstart [0 0] :delta [0 0] :start-elem nil}
                            :middle {:pressed? false :dragstart [0 0] :delta [0 0] :start-elem nil}
                            :right {:pressed? false :dragstart [0 0] :delta [0 0] :start-elem nil}}))

(defonce drag (atom nil))

(defonce elem-type (atom :stmt))

;; -------------------------
;; Views

(defn arrow [from to]
  [:g {:key (gensym)} (svg/line-decorated from to nil (svg/arrow-head 10 (/ Math/PI 4) true))])

(defn dragged? [id]
  (reagent/track #(= @drag id)))

(defn dragged! [id]
  (reset! drag id))

(defn right-pressed? []
  (reagent/track #(get-in @mouse-state [:right :pressed?])))

(defn internal-pos [id]
  (reagent/track #(get-in @elems [id :pos])))

(defn actual-pos [id]
  (reagent/track #(map + @(internal-pos id) (if @(dragged? id)
                                              (get-in @mouse-state [:middle :delta])
                                              [0 0]))))

(defn outlinks [id]
  (reagent/track #(get-in @elems [id :outlinks])))

(defn link! [from to]
  (swap! elems update-in [from :outlinks] conj to))

(defn elem [type x y text]
    {:id (keyword (gensym "id"))
     :type type
     :text text
     :pos [x y]
     :outlinks []})

(defn add-elem! [{:keys [id] :as elem}]
  (swap! elems assoc id elem))

(defn remove-elem! [id]
  (swap! elems dissoc id))

(defmulti render (fn [elem] (:type elem)))

(defn draggable-component [id & body]
  (vec (concat
        [:g
         (vec
          (concat
           [:g {:transform (apply gstring/format "translate(%d,%d)"
                                  @(actual-pos id))
                :on-mouse-down #(case (.-button %)
                                  0 (swap! mouse-state update :left merge {:start-elem id})
                                  1 (dragged! id))
                :on-mouse-up #(case (.-button %)
                                0 (when-let [from (get-in @mouse-state [:left :start-elem])]
                                    (link! from id))
                                1 (swap! elems assoc-in [id :pos] @(actual-pos id)))
                :on-mouse-over #(if @(right-pressed?) (remove-elem! id))}]
           body))]
        (mapv (fn [dest] (arrow @(actual-pos id) @(actual-pos dest))) @(outlinks id)))))

(defmethod render :start [{:keys [id text]}]
  (let [w 60
        h 20]
    (draggable-component
     id
     [:ellipse {:cx (/ w 2) :cy (/ h 2) :rx w :ry h :style {:fill "plum"}}]
     (svg/text [5 (* h .6)] text))))

(defmethod render :stmt [{:keys [id text]}]
  (let [w 120
        h 40]
    (draggable-component
     id
     (svg/rect [0 0] w h {:style {:fill "blue"}})
     (svg/text [5 (* h .6)] text))))

(defmethod render :branch [{:keys [id text]}]
  (let [w 140
        h 60
        w' (/ w 2)
        h' (/ h 2)]
    (draggable-component
     id
     (svg/polygon [[0 h'] [w' 0] [w h'] [w' h]] {:style {:fill "orange"}})
     (svg/text [40 (* h .6)] text))))

(defmethod render :note [{:keys [id text]}]
  (let [corner 20
        w 140
        h 200]
    (draggable-component
     id
     (svg/polygon [[corner 0] [w 0] [w h] [0 h] [0 corner]]
                  {:style {:fill "beige"}})
     (svg/polygon [[corner 0] [corner corner] [0 corner]]
                  {:style {:fill "burlywood"}})
     (svg/text [(* 1.2 corner) (* 1.2 corner)] text))))

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
  (swap! mouse-state (fn [state]
                       (-> state
                           (update-drag x y)
                           (assoc :position [x y])))))

(defn- button-up! [button]
  (do (swap! mouse-state update button merge {:pressed? false :delta [0 0] :start-elem nil})
      (reset! drag nil)))

(defn svg-component
  [& body]
  (svg/svg
   {:width (.-innerWidth js/window)
    :height (.-innerHeight js/window)
    :id "canvas"
    :style {:background-color "#fff"
            :display "block"
            :stroke "black"}
    :on-click (fn [e] (when (= 0 (.-button e))
                        (add-elem! (elem @elem-type (.-clientX e) (.-clientY e) "foo"))))
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

(defn handle-key-press! [key-code]
  (when-let [new-elem-type (case key-code
                             66 :branch ;(b)ranch
                             83 :stmt ;(s)tatement
                             78 :note ;(n)ote
                             84 :start ;(t)erminal
                             nil)]
    (reset! elem-type new-elem-type)))

(defonce foo (set! (.-onkeydown js/window) (fn [e] (handle-key-press! (.-keyCode e)))))

(defn svg-page []
  [:div
   [svg-component
    [:text {:x 50 :y 50} (with-out-str (pprint @mouse-state))]
    #_(let [s @mouse-state
          from (get-in s [:left :dragstart])
          to (map + from (get-in s [:left :delta]))]
      (when (and (get-in s [:left :pressed?]) (= (get-in s [:left :start-elem 0]) :stmt))
        [arrow from to]))
    (for [elem (vals @elems)]
      [render elem])]])

;; -------------------------
;; Initialize app

(defn mount-root []
  (reagent/render [#'svg-page] (.getElementById js/document "app")))

(defn init! []
  (mount-root))
