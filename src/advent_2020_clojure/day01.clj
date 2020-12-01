(ns advent-2020-clojure.day01
  (:require [clojure.string :as str]))

(defn part1 [input]
  (let [expenses (->> (str/split-lines input)
                      (map #(Integer/parseInt %)))]
    (apply * (first (for [x expenses
                          y expenses
                          :when (and (< x y)
                                     (= 2020 (+ x y)))]
                      [x y])))))

(defn part2 [input]
  (let [expenses (->> (str/split-lines input)
                      (map #(Integer/parseInt %)))]
    (apply * (first (for [x expenses
                          y expenses
                          z expenses
                          :when (and (< x y z)
                                     (= 2020 (+ x y z)))]
                      [x y z])))))
