(ns color-connect.view
  (:require [color-connect.core :as core]))

(defn play-area
  [{state :state trigger-event :trigger-event}]
  [:div {:style {:background-color "white"}}
   (map (fn [row-index]
          [:div {:key   row-index
                 :style {:display "flex"}}
           (map (fn [column-index]
                  (println (core/get-square-nuance state {:row-index    row-index
                                                          :column-index column-index}))
                  [:div {:key      column-index
                         :style    {:background-color (core/get-square-nuance state {:row-index    row-index
                                                                                     :column-index column-index})
                                    :border           "1px solid black"
                                    :height           "50px"
                                    :width            "50px"
                                    }
                         :on-click (fn []
                                     (trigger-event {:name :cell-click
                                                     :data {:row-index    row-index
                                                            :column-index column-index}}))}
                   (str row-index column-index)])
                (range core/grid-width))])
        (range core/grid-height))])

(defn app-view
  [{state-atom :state-atom trigger-event :trigger-event}]
  (let [state (deref state-atom)]
    [:div
     [play-area {:state state :trigger-event trigger-event}]
     [:button {:on-click (fn []
                           (trigger-event {:name :reset}))}
      "Reset"]
     [:div "Connect the blue color to win... but watch out for the red!"]]))