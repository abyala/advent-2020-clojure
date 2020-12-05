(ns advent-2020-clojure.day05
  (:require [clojure.string :as str]))

(defn split-seat [seat] (split-at 7 seat))
(defn to-binary-digit [c] (if (#{\F \L} c) 0 1))

(defn binary-space-partition [instructions]
  (as-> instructions x
        (map to-binary-digit x)
        (apply str x)
        (Integer/parseInt x 2)))

(defn seat-id [seat]
  (let [[r c] (split-seat seat)]
    (-> (* (binary-space-partition r) 8)
        (+ (binary-space-partition c)))))

(defn missing-within-collection [coll]
  (reduce (fn [prev v]
            (let [target (inc prev)]
              (if (= v target)
                v
                (reduced target))))
          (sort coll)))

(defn apply-to-seat-ids [input f]
  (->> (str/split-lines input)
       (map seat-id)
       f))

(defn part1 [input]
  (apply-to-seat-ids input (partial apply max)))

(defn part2 [input]
  (apply-to-seat-ids input missing-within-collection))