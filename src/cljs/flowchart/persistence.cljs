(ns flowchart.persistence
  (:require [cljs.reader :refer [read-string]]
            [flowchart.state :as state]))

(defn- make-key [name]
  (str "flowchart-save:" name))

(defn save!
  ([]
   (save! "default"))
  ([name]
   (.setItem js/localStorage (make-key name) (pr-str (state/get-elems-state)))))

(defn load!
  ([]
   (load! "default"))
  ([name]
   (if-let [elems (.getItem js/localStorage (make-key name))]
     (state/set-elems-state! (read-string elems)))))
