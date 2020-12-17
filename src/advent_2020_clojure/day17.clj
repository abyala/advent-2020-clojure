(ns advent-2020-clojure.day17
  (:require [clojure.string :as str]))

(defn neighbors-of [[x y z]]
  (for [new-x [-1 0 1]
        new-y [-1 0 1]
        new-z [-1 0 1]
        :when (some #(not= 0 %) [new-x new-y new-z])]
    [(+ x new-x) (+ y new-y) (+ z new-z)]))

(defn parse-input [input]
  (->> (str/split-lines input)
       (map-indexed (fn [y row] (map-indexed (fn [x c] [[x y 0] c])
                                             row)))
       (apply concat)
       (keep (fn [[coords v]] (when (= v \#) coords)))
       (into #{})))

(defn bounding-cube [active-points]
  (mapv (fn [offset]
          (let [values (map #(get % offset) active-points)]
            [(dec (apply min values)) (inc (apply max values))]))
        [0 1 2]))

(defn inclusive-range [[low high]]
  (range low (inc high)))

(defn points-in-cube [[cube-x cube-y cube-z]]
  (for [x (inclusive-range cube-x)
        y (inclusive-range cube-y)
        z (inclusive-range cube-z)]
    [x y z]))

(defn num-active-neighbors [active-points point]
  (->> point neighbors-of (filter (partial active-points)) count))

(defn next-point [active-points point]
  (let [actives (num-active-neighbors active-points point)]
    (if (active-points point)
      (contains? #{2 3} actives)
      (= 3 actives))))

(defn next-board [active-points]
  (->> active-points
       bounding-cube
       points-in-cube
       (keep #(when (next-point active-points %) %))
       set))

(defn part1 [input]
  (count (nth (iterate next-board (parse-input input)) 6)))

