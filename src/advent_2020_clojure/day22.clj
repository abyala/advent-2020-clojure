(ns advent-2020-clojure.day22
  (:require [clojure.string :as str]
            [advent-2020-clojure.utils :as utils]))

(defn next-turn [[deck-a deck-b]]
  (cond
    (empty? deck-a) deck-b
    (empty? deck-b) deck-a
    :else (let [a (first deck-a)
                b (first deck-b)]
            (if (> a b)
              [(concat (rest deck-a) (list a b)) (rest deck-b)]
              [(rest deck-a) (concat (rest deck-b) (list b a))]))))

(defn winner [deck-a deck-b]
  (->> (iterate next-turn [deck-a deck-b])
       (keep (fn [[a b]]
               (cond
                 (empty? a) b
                 (empty? b) a
                 :else nil)))
       first))

(defn calculate-score [deck]
  (->> (vec deck)
       (map-indexed (fn [idx v] (* v (- (count deck) idx))))
       (apply +)))

(defn part1 [input]
  (let [[deck-a deck-b] (->> (utils/split-blank-line input)
                             (mapv (fn [str] (->> (str/split-lines str)
                                                  rest
                                                  (map #(Integer/parseInt %))))))]
    (->> (winner deck-a deck-b)
         calculate-score)))

(defn play-recursive-game [starting-game]
  (loop [[deck-a deck-b :as game] starting-game, seen #{}]
    (cond
      (empty? deck-a) [() deck-b]
      (empty? deck-b) [deck-a ()]
      (seen game) [deck-a ()] ; Assume this doesn't happen at the top-level!
      :else (let [a (first deck-a) b (first deck-b)
                  winner-is-a? (if (and (<= a (count (rest deck-a)))
                                        (<= b (count (rest deck-b))))
                                 (empty? (second (play-recursive-game [(take a (rest deck-a))
                                                                       (take b (rest deck-b))])))
                                 (> a b))]
              (recur (if winner-is-a?
                       [(concat (rest deck-a) (list a b)) (rest deck-b)]
                       [(rest deck-a) (concat (rest deck-b) (list b a))])
                     (conj seen game))))))

(defn part2 [input]
  (let [game (->> (utils/split-blank-line input)
                             (mapv (fn [str] (->> (str/split-lines str)
                                                  rest
                                                  (map #(Integer/parseInt %))))))]
   (->> (play-recursive-game game)
        (keep #(when (seq %) (calculate-score %)))
        first)))