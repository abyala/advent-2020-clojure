(ns advent-2020-clojure.day05-test
  (:require [clojure.test :refer :all]
            [advent-2020-clojure.day05 :refer :all]))

(def PUZZLE_DATA (slurp "resources/day05_data.txt"))

(deftest binary-space-partition-test
  (testing "Row examples"
    (is (= 70 (binary-space-partition "BFFFBBF")))
    (is (= 14 (binary-space-partition "FFFBBBF")))
    (is (= 102 (binary-space-partition "BBFFBBF"))))
  (testing "Column examples"
    (is (= 7 (binary-space-partition "RRR")))
    (is (= 4 (binary-space-partition "RLL")))))

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