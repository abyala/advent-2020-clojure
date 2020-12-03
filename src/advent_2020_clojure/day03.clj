(ns advent-2020-clojure.day03
  (:require [clojure.string :as str]))

(def origin [0 0])
(defn tree? [c] (= c \#))

(defn target-coordinates [path num-tree-lines]
  (take-while some?
              (iterate #(let [[_ y :as point] (map + path %)]
                          (when (< y num-tree-lines) point))
                       origin)))

(defn value-at [tree-lines row col]
  (-> (tree-lines row)
      cycle
      (nth col)))

(defn solve [input path]
  (let [tree-lines (str/split-lines input)]
    (->> (target-coordinates path (count tree-lines))
         (map (fn [[x y]] (value-at tree-lines y x)))
         (filter tree?)
         count)))

(def part1-path [3 1])
(def part2-paths [[1 1] [3 1] [5 1] [7 1] [1 2]])

(defn part1 [input]
  (solve input part1-path))

(defn part2 [input]
  (->> part2-paths
       (map (partial solve input))
       (apply *)))