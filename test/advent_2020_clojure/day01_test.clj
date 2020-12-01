(ns advent-2020-clojure.day01-test
  (:require [clojure.test :refer :all]
            [advent-2020-clojure.day01 :refer :all]))

(def TEST_DATA "1721\n979\n366\n299\n675\n1456")
(def PUZZLE_DATA (slurp "resources/day01_data.txt"))

(deftest part1-test
  (is (= 514579 (part1 TEST_DATA)))
  (is (= 1020084 (part1 PUZZLE_DATA))))

(deftest part2-test
  (is (= 241861950 (part2 TEST_DATA)))
  (is (= 295086480 (part2 PUZZLE_DATA))))