(ns advent-2020-clojure.day05
  (:require [clojure.string :as str]))

(defn split-seat [seat] (split-at 7 seat))
(defn low-instruction? [c] (#{\F \L} c))

(defn midpoint [low high]
  (-> (- high low) (/ 2) (+ low)))

(defn binary-space-partition [num-vals instructions]
  (->> (reduce (fn [[low high] dir]
                 (let [midpoint (midpoint low high)]
                   (if (low-instruction? dir) [low midpoint] [midpoint high])))
               [0 num-vals]
               instructions)
       first))

(defn find-row [instructions]
  (binary-space-partition 128 instructions))

(defn find-column [instructions]
  (binary-space-partition 8 instructions))

(defn seat-id [seat]
  (let [[r c] (split-seat seat)]
    (-> (* (find-row r) 8)
        (+ (find-column c)))))

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