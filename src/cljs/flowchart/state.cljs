(ns flowchart.state
  (:require [reagent.core :as reagent :refer [atom]]))

;; State

(defonce ^:private elems' (atom {}))

(defonce mouse-state (atom {:position [0 0]
                            :left {:pressed? false :dragstart [0 0] :delta [0 0] :start-elem nil}
                            :middle {:pressed? false :dragstart [0 0] :delta [0 0] :start-elem nil}
                            :right {:pressed? false :dragstart [0 0] :delta [0 0] :start-elem nil}}))

(defonce ^:private drag (atom nil))

(defonce ^:private elem-type' (atom :stmt))

;; Subscriptions

(defn cursor-position []
  (reagent/cursor mouse-state [:position]))

(defn start-elem []
  (reagent/cursor mouse-state [:left :start-elem]))

(defn elems []
  (reagent/track #(vals @elems')))

(defn elem-type []
  elem-type')

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

;; Mutations

(defn drag-start! [id]
  (reset! drag id))

(defn drag-end! [id]
  (swap! elems' assoc-in [id :pos] @(actual-pos id)))

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
   :text ""
   :pos [x y]
   :outlinks []})

(defn add-elem! [x y]
  (let [{:keys [id] :as e} (elem @(elem-type) x y)]
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

(defn- button-up! [button]
  (do (swap! mouse-state update button merge {:pressed? false :delta [0 0] :start-elem nil})
      (reset! drag nil)))

(defn handle-key-press! [key-code]
  (when-let [new-elem-type (case key-code
                             66 :branch ;(b)ranch
                             83 :stmt ;(s)tatement
                             78 :note ;(n)ote
                             84 :start ;(t)erminal
                             nil)]
    (reset! elem-type' new-elem-type)))
