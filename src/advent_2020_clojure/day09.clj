(ns advent-2020-clojure.day09
  (:require [clojure.string :as str]))

(defn parse-input [input]
  (->> (str/split-lines input)
       (map #(Long/parseLong %))))

(defn pair-sums-to? [nums target]
  (->> (for [[x & xs] (iterate next nums) :while x
             y xs]
         (+ x y))
       (some (partial = target))))

(defn weakness [nums size]
  (->> (partition (inc size) 1 nums)
       (map (partial split-at size))
       (keep (fn [[sub [target]]]
               (when (not (pair-sums-to? sub target))
                 target)))
       first))

(defn part1 [input size]
  (weakness (parse-input input) size))

(defn block-adding-to [nums target]
  (reduce (fn [[r sum] v]
            (let [new-range (cons v r)
                  new-sum (+ sum v)]
              (cond
                (= new-sum target) (reduced new-range)
                (> new-sum target) (reduced nil)
                :else [new-range new-sum])))
          [[] 0]
          nums))

(defn part2 [input size]
  (let [nums (parse-input input)
        target (weakness nums size)]
    (loop [n nums]
      (if-let [r (block-adding-to n target)]
        (+ (apply min r)
           (apply max r))
        (recur (rest n))))))
