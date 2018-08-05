(ns color-connect.core)

(def grid-width 10)
(def grid-height 10)

(def friendly-color "blue")
(def enemy-color "red")

(defn create-state
  []
  {:grid (-> (reduce (fn [grid row-index]
                       (merge grid
                              (reduce (fn [grid column-index]
                                        (assoc grid [row-index column-index] {:color    nil
                                                                              :friendly 0
                                                                              :enemy    0}))
                                      grid
                                      (range grid-width))))
                     {}
                     (range grid-height))
             (assoc-in [[1 1] :color] friendly-color)
             (assoc-in [[5 7] :color] friendly-color))})

(defn get-square-color
  [state {row-index :row-index column-index :column-index}]
  (get-in state [:grid [row-index column-index] :color]))

(defn get-square-nuance
  [state {row-index :row-index column-index :column-index}]
  (let [cell (get-in state [:grid [row-index column-index]])]
    (if-let [color (:color cell)]
      color
      (let [friendly (:friendly cell)
            enemy (:enemy cell)]
        (if (> friendly enemy)
          (let [friendly-strength (* 255 (/ (- friendly enemy) 0.5))]
            (str "rgb(" (- 255 friendly-strength) "," (- 255 friendly-strength) ",255)"))
          (let [enemy-strength (* 255 (/ (- friendly enemy) 0.5))]
            (str "rgb(255," (- 255 enemy-strength) "," (- 255 enemy-strength) ")")))))))

(defn empty-square?
  [state {row-index :row-index column-index :column-index}]
  (nil? (get-square-color state {:row-index    row-index
                                 :column-index column-index})))

(defn set-square-color
  [state {row-index :row-index column-index :column-index color :color}]
  (assoc-in state [:grid [row-index column-index] :color] color))

(defn on-board?
  [[row-index column-index]]
  (and (<= 0 row-index (dec grid-height))
       (<= 0 column-index (dec grid-width))))

(defn get-neighbouring-indices
  [[row-index column-index]]
  (->> [[0 1] [1 1] [1 0] [1 -1] [0 -1] [-1 -1] [-1 0] [-1 1]]
       (map (fn [direction]
              (mapv + direction [row-index column-index])))
       (filter on-board?)))

(defn game-won?
  [state]
  false)

(defn color-spread
  "A cell spreads its colour to neighbours."
  [state]
  (update state :grid (fn [grid]
                        (reduce (fn [grid row-index]
                                  (merge grid
                                         (reduce (fn [grid column-index]
                                                   (update grid [row-index column-index] (fn [cell]
                                                                                           (reduce (fn [{color :color friendly :friendly enemy :enemy :as cell} [neighbour-row-index neighbour-column-index]]
                                                                                                     (let [neighbour-color (get-square-color state {:row-index neighbour-row-index :column-index neighbour-column-index})]
                                                                                                       (condp = neighbour-color
                                                                                                         friendly-color (update cell :friendly + 0.05)
                                                                                                         enemy-color (update cell :enemy + 0.1)
                                                                                                         cell)))
                                                                                                   cell
                                                                                                   (get-neighbouring-indices [row-index column-index])))))
                                                 grid
                                                 (range grid-width))))
                                grid
                                (range grid-height)))))

(defn increase-enemy-presence
  [state]
  (let [random-row-index (rand-int grid-height)
        random-column-index (rand-int grid-width)]
    (update-in state [:grid [random-row-index random-column-index] :enemy] + 0.5)))

(defn color-spawn
  "Looks at all cells that have no determined color and spawns a color if either side has enough presence."
  [state]
  (update state :grid (fn [grid]
                        (reduce (fn [grid row-index]
                                  (merge grid
                                         (reduce (fn [grid column-index]
                                                   (update grid [row-index column-index] (fn [{color :color friendly :friendly enemy :enemy :as cell}]
                                                                                           (if-not (nil? color)
                                                                                             cell
                                                                                             (cond
                                                                                               (>= (- friendly enemy) 0.5) (assoc cell :color friendly-color)
                                                                                               (<= (- friendly enemy) -0.5) (assoc cell :color enemy-color)
                                                                                               :else cell)))))
                                                 grid
                                                 (range grid-width))))
                                grid
                                (range grid-height)))))

(defn handle-cell-click
  [state {row-index :row-index column-index :column-index}]
  (if-not (empty-square? state {:row-index    row-index
                                :column-index column-index})
    state
    (-> state
        (set-square-color {:row-index row-index :column-index column-index :color friendly-color})
        (color-spread)
        (increase-enemy-presence)
        (color-spawn))))