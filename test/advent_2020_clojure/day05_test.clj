(ns advent-2020-clojure.day05-test
  (:require [clojure.test :refer :all]
            [advent-2020-clojure.day05 :refer :all]))

(def PUZZLE_DATA (slurp "resources/day05_data.txt"))

(deftest midpoint-test
  (is (= 64 (midpoint 0 128)))
  (is (= 32 (midpoint 0 64)))
  (is (= 96 (midpoint 64 128))))

(deftest find-row-test
  (is (= 70 (find-row "BFFFBBF")))
  (is (= 14 (find-row "FFFBBBF")))
  (is (= 102 (find-row "BBFFBBF"))))

(deftest find-column-test
  (is (= 7 (find-column "RRR")))
  (is (= 4 (find-column "RLL"))))

(deftest seat-id-test
  (is (= 567 (seat-id "BFFFBBFRRR")))
  (is (= 119 (seat-id "FFFBBBFRRR")))
  (is (= 820 (seat-id "BBFFBBFRLL"))))

(deftest missing-within-collection-test
  (is (= 3 (missing-within-collection [1 4 2 0 6 7 5]))))

(deftest part1-test
  (is (= 818 (part1 PUZZLE_DATA))))

(deftest part2-test
  (is (= 559 (part2 PUZZLE_DATA))))