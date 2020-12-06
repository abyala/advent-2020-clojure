(ns advent-2020-clojure.day06-test
  (:require [clojure.test :refer :all]
            [advent-2020-clojure.day06 :refer :all]))

(def PUZZLE_DATA (slurp "resources/day06_data.txt"))

(deftest part1-test
  (is (= 6683 (part1 PUZZLE_DATA))))

(deftest part2-test
  (is (= 3122 (part2 PUZZLE_DATA))))



