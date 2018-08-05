(ns color-connect.main
  (:require [reagent.core :as reagent]
            [color-connect.core :as core]
            [color-connect.view :refer [app-view]]))

(enable-console-print!)

(defonce state-atom (atom nil))

(def reagent-state-atom (reagent/atom (deref state-atom)))

(when (nil? (deref state-atom))
  (add-watch state-atom
             :engine
             (fn [_ _ _ _]
               (let [state (deref state-atom)]
                 (reset! reagent-state-atom state)

                 )))
  (reset! state-atom (core/create-state)))

(defn handle-event
  [event]
  (condp = (:name event)

    :cell-click
    (swap! state-atom core/handle-cell-click (:data event))

    :reset
    (reset! state-atom (core/create-state))))

(reagent/render-component [app-view {:state-atom    reagent-state-atom
                                     :trigger-event handle-event}]
                          (. js/document (getElementById "app")))