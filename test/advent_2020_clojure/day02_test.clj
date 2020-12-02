(ns advent-2020-clojure.day02-test
  (:require [clojure.test :refer :all]
            [advent-2020-clojure.day02 :refer :all]))

(def TEST_DATA "1-3 a: abcde\n1-3 b: cdefg\n2-9 c: ccccccccc")
(def PUZZLE_DATA (slurp "resources/day02_data.txt"))

(deftest parse-line-test
  (is (= [1 3 \a "abcde"] (parse-line "1-3 a: abcde")))
  (is (= [23 25 \j "ababab"] (parse-line "23-25 j: ababab"))))

(deftest valid-password-test
  (is (sled-password? [1 3 \a "abcde"]))
  (is (not (sled-password? [1 3 \b "cdefg"])))
  (is (sled-password? [2 9 \c "ccccccccc"])))

(deftest part1-test
  (is (= 2 (part1 TEST_DATA)))
  (is (= 538 (part1 PUZZLE_DATA))))

(deftest part2-test
  (is (= 1 (part2 TEST_DATA)))
  (is (= 489 (part2 PUZZLE_DATA))))