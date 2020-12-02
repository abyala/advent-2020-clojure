(ns advent-2020-clojure.day02
  (:require [clojure.string :as str])
  (:use [advent-2020-clojure.utils :only [xor]]))

(defn parse-line [line]
  (let [[_ min max letter word] (re-matches #"(\d+)-(\d+) (\w): (\w+)" line)]
    [(Integer/parseInt min) (Integer/parseInt max) (first letter) word]))

(defn sled-password? [[min max c word]]
  (let [matches (->> (filter (partial = c) word)
                     count)]
    (<= min matches max)))

(defn toboggan-password? [[min max c word]]
  (xor #(= c (get word (dec %)))
       [min max]))

(defn solve [input rule]
  (->> (str/split-lines input)
       (map parse-line)
       (filter rule)
       count))

(defn part1 [input] (solve input sled-password?))
(defn part2 [input] (solve input toboggan-password?))
