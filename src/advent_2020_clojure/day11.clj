(ns advent-2020-clojure.day11
  (:require [clojure.string :as str]
            [advent-2020-clojure.utils :refer [mapv-indexed]]))

(def occupied-seat \#)
(def empty-seat \L)
(def space \.)
(defn occupied-seat? [c] (= occupied-seat c))
(defn empty-seat? [c] (= empty-seat c))
(defn seat? [c] (or (occupied-seat? c) (empty-seat? c)))

(def all-directions '([-1 -1] [-1 0] [-1 1]
                      [0 -1] [0 1]
                      [1 -1] [1 0] [1 1]))

(defn all-neighbor-paths [grid point]
  (let [rows (count grid)
        cols (count (first grid))
        in-range? (fn [[y x]] (and (< -1 y rows)
                                   (< -1 x cols)))]
    (for [dir all-directions]
      (->> (iterate (partial map + dir) point)
           rest
           (take-while in-range?)))))

(defn first-in-path [grid f path]
  (->> path
       (map (partial get-in grid))
       (filter f)
       first))

(defn occupied-neighbors-by [grid point f]
  (->> (all-neighbor-paths grid point)
       (map (partial first-in-path grid f))
       (filter occupied-seat?)
       count))

(defn next-point [grid point f awkwardness]
  (let [c (get-in grid point)]
    (if-not (seat? c)
      space
      (let [occ (occupied-neighbors-by grid point f)]
        (cond
          (and (empty-seat? c) (zero? occ)) occupied-seat
          (and (occupied-seat? c) (>= occ awkwardness)) empty-seat
          :else c)))))

(defn next-turn [grid f awkwardness]
  (mapv-indexed (fn [y row]
                  (mapv-indexed (fn [x _]
                                  (next-point grid [y x] f awkwardness))
                                row))
                grid))

(defn solve [input f awkwardness]
  (->> (iterate #(next-turn % f awkwardness)
                (str/split-lines input))
       (partition 2 1)
       (filter (partial apply =))
       ffirst
       (apply str)
       (filter occupied-seat?)
       count))

(defn part1 [input] (solve input some? 4))
(defn part2 [input] (solve input seat? 5))

