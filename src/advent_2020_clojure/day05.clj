(ns advent-2020-clojure.day05
  (:require [clojure.string :as str]))

(defn split-seat [seat] (split-at 7 seat))

(defn get-row [front]
  (first (reduce (fn [[min max] dir]
                       (let [midpoint (+ min (/ (- max min) 2))]
                         (if (= \F dir) [min midpoint]
                                        [midpoint max])))
                     [0 128]
                     front)))

(defn get-column [back]
  (first (reduce (fn [[min max] dir]
                   (let [midpoint (+ min (/ (- max min) 2))]
                     (if (= \L dir) [min midpoint]
                                    [midpoint max])))
                 [0 8]
                 back)))

(defn seat-id [seat]
  (let [[front back] (split-seat seat)]
    (-> (* (get-row front) 8)
        (+ (get-column back)))))

(defn part1 [input]
  (->> (str/split-lines input)
       (map seat-id)
       (apply max)))