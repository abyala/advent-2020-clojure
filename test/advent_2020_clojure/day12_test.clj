(ns advent-2020-clojure.day12-test
  (:require [clojure.test :refer :all]
            [advent-2020-clojure.day12 :refer :all]))

(def TEST_DATA "F10\nN3\nF7\nR90\nF11")
(def PUZZLE_DATA (slurp "resources/day12_data.txt"))

(deftest part1-test
  (is (= 25 (part1 TEST_DATA)))
  (is (= 1177 (part1 PUZZLE_DATA))))

(deftest part2-test
  (is (= 286 (part2 TEST_DATA)))
  (is (= 46530 (part2 PUZZLE_DATA))))