(ns advent-2020-clojure.day17
  (:require [clojure.string :as str]))

(defn neighbors-of [[x y z w] four-d?]
  (for [new-w (if four-d? [-1 0 1] [0])
        new-x [-1 0 1]
        new-y [-1 0 1]
        new-z [-1 0 1]
        :when (some #(not= 0 %) [new-x new-y new-z new-w])]
    [(+ x new-x) (+ y new-y) (+ z new-z) (+ w new-w)]))

(defn parse-input [input]
  (->> (str/split-lines input)
       (map-indexed (fn [y row] (map-indexed (fn [x c] [[x y 0 0] c])
                                             row)))
       (apply concat)
       (keep (fn [[coords v]] (when (= v \#) coords)))
       (into #{})))

(defn bounding-cube [active-points]
  (mapv (fn [offset]
          (let [values (map #(get % offset) active-points)]
            [(dec (apply min values)) (inc (apply max values))]))
        [0 1 2 3]))

(defn inclusive-range [[low high]]
  (range low (inc high)))

(defn points-in-cube [[cube-x cube-y cube-z cube-w]]
  (for [x (inclusive-range cube-x)
        y (inclusive-range cube-y)
        z (inclusive-range cube-z)
        w (inclusive-range cube-w)]
    [x y z w]))

(defn num-active-neighbors [active-points four-d? point]
  (->> (neighbors-of point four-d?)
       (filter #(active-points %))
       count))

(defn next-point [active-points four-d? point]
  (let [actives (num-active-neighbors active-points four-d? point)]
    (if (active-points point)
      (contains? #{2 3} actives)
      (= 3 actives))))

(defn next-board [active-points four-d?]
  (->> active-points
       bounding-cube
       points-in-cube
       (keep #(when (next-point active-points four-d? %) %))
       set))

(defn part1 [input]
  (count (nth (iterate #(next-board % false) (parse-input input)) 6)))

(defn part2 [input]
  (count (nth (iterate #(next-board % true) (parse-input input)) 6)))
