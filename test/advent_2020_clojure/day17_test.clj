(ns advent-2020-clojure.day17-test
  (:require [clojure.test :refer :all]
            [advent-2020-clojure.day17 :refer :all]))

(def TEST_DATA ".#.\n..#\n###")
(def PUZZLE_DATA (slurp "resources/day17_data.txt"))

(deftest part1-test
  (is (= 112 (part1 TEST_DATA)))
  (is (= 375 (part1 PUZZLE_DATA))))

(deftest part2-test
  (is (= 848 (part2 TEST_DATA)))
  (is (= -1 (part2 PUZZLE_DATA))))