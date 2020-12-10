(ns advent-2020-clojure.day10
  (:require [clojure.string :as str]))

(defn parse-jolts [input]
  (->> (str/split-lines input)
       (map #(Integer/parseInt %))
       (cons 0)
       sort))

(defn part1 [input]
  (let [{ones 1 threes 3} (->> (parse-jolts input)
                               (partition 2 1)
                               (map #(- (second %) (first %)))
                               frequencies)]
    (* ones (inc threes))))

(defn incr-all-by [m [k & ks] amount]
  (cond
    (nil? k) m
    (m k) (incr-all-by (update m k + amount) ks amount)
    :else (incr-all-by m ks amount)))

(defn part2 [input]
  (let [jolts (-> input parse-jolts reverse)]
    (loop [[j & js] jolts
           paths (-> jolts (zipmap (repeat 0)) (assoc j 1))]
      (if (zero? j)
        (paths 0)
        (recur js
               (incr-all-by paths
                            (map (partial - j) [1 2 3])
                            (paths j)))))))