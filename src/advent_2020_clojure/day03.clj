(ns advent-2020-clojure.day03
  (:require [clojure.string :as str]))

(defn part1 [input]
  (let [tree-lines (str/split-lines input)]
    (->> (map (fn [row] (let [col (* 3 row)]
                          (nth (cycle (tree-lines row)) col)
                          ))
              (range (count tree-lines)))
         (filter (partial = \#))
         count)))

(defn part2 [input]
  -10
  )