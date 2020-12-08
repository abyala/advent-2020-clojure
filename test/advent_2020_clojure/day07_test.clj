(ns advent-2020-clojure.day07-test
  (:require [clojure.test :refer :all]
            [advent-2020-clojure.day07 :refer :all]))

(def TEST_DATA "light red bags contain 1 bright white bag, 2 muted yellow bags.\ndark orange bags contain 3 bright white bags, 4 muted yellow bags.\nbright white bags contain 1 shiny gold bag.\nmuted yellow bags contain 2 shiny gold bags, 9 faded blue bags.\nshiny gold bags contain 1 dark olive bag, 2 vibrant plum bags.\ndark olive bags contain 3 faded blue bags, 4 dotted black bags.\nvibrant plum bags contain 5 faded blue bags, 6 dotted black bags.\nfaded blue bags contain no other bags.\ndotted black bags contain no other bags.")
(def TEST_DATA2 "shiny gold bags contain 2 dark red bags.\ndark red bags contain 2 dark orange bags.\ndark orange bags contain 2 dark yellow bags.\ndark yellow bags contain 2 dark green bags.\ndark green bags contain 2 dark blue bags.\ndark blue bags contain 2 dark violet bags.\ndark violet bags contain no other bags.")
(def PUZZLE_DATA (slurp "resources/day07_data.txt"))

(deftest part1-test
  (is (= 4 (part1 TEST_DATA)))
  (is (= 197 (part1 PUZZLE_DATA))))

(deftest part2-test
  (is (= 32 (part2 TEST_DATA)))
  (is (= 126 (part2 TEST_DATA2)))
  (is (= 85324 (part2 PUZZLE_DATA))))
