(ns advent-2020-clojure.day10-test
  (:require [clojure.test :refer :all]
            [advent-2020-clojure.day10 :refer :all]))

(def TEST_DATA_1 "16\n10\n15\n5\n1\n11\n7\n19\n6\n12\n4")
(def TEST_DATA_2 "28\n33\n18\n42\n31\n14\n46\n20\n48\n47\n24\n23\n49\n45\n19\n38\n39\n11\n1\n32\n25\n35\n8\n17\n7\n9\n4\n2\n34\n10\n3\n")
(def PUZZLE_DATA (slurp "resources/day10_data.txt"))

(deftest part1-test
  (is (= 35 (part1 TEST_DATA_1)))
  (is (= 220 (part1 TEST_DATA_2)))
  (is (= 2277 (part1 PUZZLE_DATA))))

(deftest part2-test
  (is (= 8 (part2 TEST_DATA_1)))
  (is (= 19208 (part2 TEST_DATA_2)))
  (is (= 37024595836928 (part2 PUZZLE_DATA))))
