(ns advent-2020-clojure.day18-test
  (:require [clojure.test :refer :all]
            [advent-2020-clojure.day18 :refer :all]))

(def PUZZLE_DATA (slurp "resources/day18_data.txt"))

(deftest calculate-test
  (letfn [(part1-calc [line]
            (calculate (parse-mathematical-string line) [add-or-mul]))]
    (testing "Simple math"
      (is (= 71 (part1-calc "1 + 2 * 3 + 4 * 5 + 6"))))
    (testing "Parentheses"
      (is (= 51 (part1-calc "1 + (2 * 3) + (4 * (5 + 6))")))
      (is (= 26 (part1-calc "2 * 3 + (4 * 5)")))
      (is (= 437 (part1-calc "5 + (8 * 3 + 9 + 3 * 4 * 3)")))
      (is (= 12240 (part1-calc "5 * 9 * (7 * 3 * 3 + 9 * 3 + (8 + 6 * 4))")))
      (is (= 13632 (part1-calc "((2 + 4 * 9) * (6 + 9 * 8 + 6) + 6) + 2 + 4 * 2"))))))

(deftest part1-test
  (is (= 5374004645253 (part1 PUZZLE_DATA))))

(deftest part2-test
  (is (= 328 (part2 "1 + 2 * 3 + 4 * 5 + 6\n1 + (2 * 3) + (4 * (5 + 6))\n2 * 3 + (4 * 5)")))
  (is (= 1773 (part2 "1 + 2 * 3 + 4 * 5 + 6\n1 + (2 * 3) + (4 * (5 + 6))\n2 * 3 + (4 * 5)\n5 + (8 * 3 + 9 + 3 * 4 * 3)")))
  (is (= 88782789402798 (part2 PUZZLE_DATA))))

