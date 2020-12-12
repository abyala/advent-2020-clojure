(ns advent-2020-clojure.day11
  (:require [clojure.string :as str]
            [advent-2020-clojure.utils :refer [mapv-indexed]]))

(defn occupied-seat? [c] (= \# c))
(defn empty-seat? [c] (= \L c))
(defn seat? [c] (or (occupied-seat? c) (empty-seat? c)))

(def all-directions '([-1 -1] [-1 0] [-1 1]
                      [0 -1] [0 1]
                      [1 -1] [1 0] [1 1]))

(defn all-neighbor-paths [point grid]
  (let [rows (count grid)
        cols (count (first grid))
        in-range? (fn [[y x]] (and (< -1 y rows)
                                   (< -1 x cols)))]
    (for [dir all-directions]
      (->> (iterate (partial map + dir) point)
           rest
           (take-while in-range?)
           (map vec)))))

(defn first-in-path [grid f path]
  (->> path
       (map (partial get-in grid))
       (filter f)
       first))

(defn occupied-neighbors-by [point grid f]
  (->> (all-neighbor-paths point grid)
       (map (partial first-in-path grid f))
       (filter occupied-seat?)
       count))

(defn next-point [point grid f awkwardness]
  (let [c (get-in grid point)
        occ (occupied-neighbors-by point grid f)]
    (cond
      (and (empty-seat? c) (zero? occ)) \#
      (and (occupied-seat? c) (>= occ awkwardness)) \L
      :else c)))

(defn next-turn [grid f awkwardness]
  (->> grid
       (mapv-indexed (fn [y row]
                       (->> row
                            (mapv-indexed (fn [x _]
                                            (next-point [y x] grid f awkwardness)))
                            (apply str))))))


(defn solve [input f awkwardness]
  (->> (iterate #(next-turn % f awkwardness)
                (str/split-lines input))
       (partition 2 1)
       (drop 1)
       (filter (partial apply =))
       ffirst
       flatten
       (apply str)
       (filter occupied-seat?)
       count))

(defn part1 [input] (solve input some? 4))
(defn part2 [input] (solve input seat? 5))

