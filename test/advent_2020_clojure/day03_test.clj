(ns advent-2020-clojure.day03-test
  (:require [clojure.test :refer :all]
            [advent-2020-clojure.day03 :refer :all]))

(def TEST_DATA "..##.......\n#...#...#..\n.#....#..#.\n..#.#...#.#\n.#...##..#.\n..#.##.....\n.#.#.#....#\n.#........#\n#.##...#...\n#...##....#\n.#..#...#.#")
(def PUZZLE_DATA (slurp "resources/day03_data.txt"))

(deftest part1-test
  (is (= 7 (part1 TEST_DATA)))
  (is (= 169 (part1 PUZZLE_DATA))))