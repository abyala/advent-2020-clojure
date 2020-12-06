(ns advent-2020-clojure.day06
  (:require [clojure.set :as set]
            [advent-2020-clojure.utils :as utils]))

(defn sum-across-groups [input f]
  (->> (utils/split-blank-line-seq input)
       (map (partial map set))
       (map (partial apply f))
       (map count)
       (apply +)))

(defn part1 [input]
  (sum-across-groups input set/union))

(defn part2 [input]
  (sum-across-groups input set/intersection))