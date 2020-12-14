(ns advent-2020-clojure.day14-test
  (:require [clojure.test :refer :all]
            [advent-2020-clojure.day14 :refer :all]))

(def TEST_DATA_1 "mask = XXXXXXXXXXXXXXXXXXXXXXXXXXXXX1XXXX0X\nmem[8] = 11\nmem[7] = 101\nmem[8] = 0")
(def TEST_DATA_2 "mask = 000000000000000000000000000000X1001X\nmem[42] = 100\nmask = 00000000000000000000000000000000X0XX\nmem[26] = 1\n")
(def PUZZLE_DATA (slurp "resources/day14_data.txt"))

(deftest part1-test
  (is (= 165 (part1 TEST_DATA_1)))
  (is (= 13556564111697  (part1 PUZZLE_DATA))))

(deftest part2-test
  (is (= 208 (part2 TEST_DATA_2)))
  (is (= 4173715962894  (part2 PUZZLE_DATA))))
