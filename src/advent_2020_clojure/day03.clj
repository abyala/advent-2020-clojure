(ns advent-2020-clojure.day03
  (:require [clojure.string :as str]))

(def origin [0 0])
(def part1-path [3 1])
(def part2-paths [[1 1] [3 1] [5 1] [7 1] [1 2]])

(defn target-coordinates [path num-tree-lines]
  (drop-last
    (reductions
      (fn [[x y] [x' y']] (let [target-x (+ x x')
                                target-y (+ y y')]
                            (if (>= target-y num-tree-lines)
                              (reduced nil)
                              [target-x target-y])))
      origin
      (repeat path))))

(defn value-at [tree-lines row col]
  (-> (tree-lines row)
      cycle
      (nth col)))

(defn tree? [c] (= c \#))

(defn part1 [input]
  (let [tree-lines (str/split-lines input)]
    (->> (map (fn [row] (let [col (* 3 row)]
                          (value-at tree-lines row col)))
              (range (count tree-lines)))
         (filter tree?)
         count)))

(defn solve [input path]
  (let [tree-lines (str/split-lines input)]
    (->> (target-coordinates path (count tree-lines))
         (map (fn [[x y]] (value-at tree-lines y x)))
         (filter tree?)
         count)))

(defn part1 [input]
  (solve input part1-path))

(defn part2 [input]
  (->> part2-paths
       (map (partial solve input))
       (apply *)))