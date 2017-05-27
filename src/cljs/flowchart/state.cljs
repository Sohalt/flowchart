(ns flowchart.state
  (:require [reagent.core :as reagent :refer [atom]]
            [historian.core :as hist]))

;; State

(defonce ^:private elems' (atom {}))

(defn get-elems-state []
  @elems')

(defn set-elems-state! [state]
  (reset! elems' state))

(defonce mouse-state (atom {:position [0 0]
                            :left {:pressed? false :dragstart [0 0] :delta [0 0] :start-elem nil}
                            :middle {:pressed? false :dragstart [0 0] :delta [0 0] :start-elem nil}
                            :right {:pressed? false :dragstart [0 0] :delta [0 0] :start-elem nil}}))

(defonce ^:private menu (atom {:visible false
                     :position [0 0]}))

(defonce ^:private drag (atom nil))

(defonce _ (hist/record! elems' :elems))

;; Subscriptions

(defn cursor-position []
  (reagent/cursor mouse-state [:position]))

(defn menu-position []
  (reagent/cursor menu [:position]))

(defn menu-visible []
  (reagent/cursor menu [:visible]))

(defn start-elem []
  (reagent/cursor mouse-state [:left :start-elem]))

(defn elems []
  (reagent/track #(vals @elems')))

(defn dragged? [id]
  (reagent/track #(= @drag id)))

(defn right-pressed? []
  (reagent/cursor mouse-state [:right :pressed?]))

(defn internal-pos [id]
  (reagent/cursor elems' [id :pos]))

(defn actual-pos [id]
  (let [internal-pos (internal-pos id)
        dragged? (dragged? id)
        delta (reagent/cursor mouse-state [:middle :delta])]
    (reagent/track #(map + @internal-pos (if @dragged?
                                           @delta
                                           [0 0])))))

(defn outlinks [id]
  (reagent/cursor elems' [id :outlinks]))

(defn text [id]
  (reagent/cursor elems' [id :text]))

;; Mutations

(defn drag-start! [id]
  (reset! drag id))

(defn drag-end! [id]
  (swap! elems' assoc-in [id :pos] @(actual-pos id))
  (reset! drag nil))

(defn link! [from to]
  (swap! elems' update-in [from :outlinks] conj to))

(defn link-start! [id]
  (swap! mouse-state update :left merge {:start-elem id}))

(defn link-end! [id]
  (when-let [from (get-in @mouse-state [:left :start-elem])]
    (link! from id)))

(defn elem [type x y]
  {:id (keyword (gensym "id"))
   :type type
   :text "foobar"
   :pos [x y]
   :outlinks []})

(defn add-elem! [type]
  (let [[x y] @(cursor-position)
        {:keys [id] :as e} (elem type x y)]
    (swap! elems' assoc id e)))

(defn- map-vals [m f]
  (->> m (map (fn [[k v]] [k (f v)])) (into {})))

(defn remove-elem! [id]
  (swap! elems'
         (fn [elems]
           (-> elems
               (dissoc id)
               (map-vals #(update % :outlinks (partial remove #{id})))))))

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

(defn show-menu! []
  (swap! menu
         (fn [m]
           (-> m
               (assoc :position @(cursor-position))
               (assoc :visible true)))))

(defn hide-menu! []
  (swap! menu assoc :visible false))

(defn- button-up! [button]
  (swap! mouse-state update button merge {:pressed? false :delta [0 0] :start-elem nil}))
