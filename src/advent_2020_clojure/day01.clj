(ns advent-2020-clojure.day01
  (:require [clojure.string :as str]))

(defn permutations
  "Creates a list of lists, containing all permutations of the incoming data, each with
  the intended length."
  [length data]
  (nth (iterate
         (fn [d] (mapcat #(for [x data] (cons x %))
                         d))
         (map list data))
       (dec length)))

(defn all-increasing? [v] (apply < v))
(defn adds-to-2020?   [v] (= 2020 (apply + v)))
(defn product-of-all  [v] (apply * v))

(defn solve [length input]
  (->> (str/split-lines input)
       (map #(Integer/parseInt %))
       (permutations length)
       (keep #(when (and (all-increasing? %)
                         (adds-to-2020? %))
                (product-of-all %)))
       first))

(defn part1 [input] (solve 2 input))
(defn part2 [input] (solve 3 input))