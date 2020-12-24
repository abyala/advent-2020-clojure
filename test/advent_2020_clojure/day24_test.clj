(ns advent-2020-clojure.day24-test
  (:require [clojure.test :refer :all]
            [advent-2020-clojure.day24 :refer :all]))

(def TEST_DATA "sesenwnenenewseeswwswswwnenewsewsw\nneeenesenwnwwswnenewnwwsewnenwseswesw\nseswneswswsenwwnwse\nnwnwneseeswswnenewneswwnewseswneseene\nswweswneswnenwsewnwneneseenw\neesenwseswswnenwswnwnwsewwnwsene\nsewnenenenesenwsewnenwwwse\nwenwwweseeeweswwwnwwe\nwsweesenenewnwwnwsenewsenwwsesesenwne\nneeswseenwwswnwswswnw\nnenwswwsewswnenenewsenwsenwnesesenew\nenewnwewneswsewnwswenweswnenwsenwsw\nsweneswneswneneenwnewenewwneswswnese\nswwesenesewenwneswnwwneseswwne\nenesenwswwswneneswsenwnewswseenwsese\nwnwnesenesenenwwnenwsewesewsesesew\nnenewswnwewswnenesenwnesewesw\neneswnwswnwsenenwnwnwwseeswneewsenese\nneswnwewnwnwseenwseesewsenwsweewe\nwseweeenwnesenwwwswnew\n")
(def PUZZLE_DATA (slurp "resources/day24_data.txt"))

(deftest part1-test
  (is (= 10 (part1 TEST_DATA)))
  (is (= 382 (part1 PUZZLE_DATA))))

(deftest next-turn-test
  (is (= #{[0 0] [1 -1] [2 0] [3 1] [4 0]}
         (next-turn #{[0 0] [2 0] [3 1]})))
  (is (= #{[2 0]}
         (next-turn #{[0 0] [4 0]})))
  (is (= #{[0 0] [3 1] [-1 1] [2 2] [4 0] [1 -1]}
         (next-turn #{[0 0] [2 0] [1 1] [3 1]}))))

(deftest part2-test
  (is (= 2208 (part2 TEST_DATA)))
  (is (= 3964 (part2 PUZZLE_DATA))))

