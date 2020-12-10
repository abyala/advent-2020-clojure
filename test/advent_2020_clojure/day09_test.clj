(ns advent-2020-clojure.day09-test
  (:require [clojure.test :refer :all]
            [advent-2020-clojure.day09 :refer :all]))

(def TEST_DATA "35\n20\n15\n25\n47\n40\n62\n55\n65\n95\n102\n117\n150\n182\n127\n219\n299\n277\n309\n576")
(def PUZZLE_DATA (slurp "resources/day09_data.txt"))

(deftest part1-test
  (is (= 127 (part1 TEST_DATA 5)))
  (is (= 1721308972 (part1 PUZZLE_DATA 25))))

(deftest part2-test
  (is (= 62 (part2 TEST_DATA 5)))
  (is (= 209694133 (part2 PUZZLE_DATA 25))))
