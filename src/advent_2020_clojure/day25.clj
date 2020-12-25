(ns advent-2020-clojure.day25)

(defn crypto-transform [subject-number]
  (iterate #(-> (* % subject-number) (rem 20201227)) 1))

(defn crack-private-key [subject-number public-key]
  (->> (crypto-transform subject-number)
       (keep-indexed (fn [idx v] (when (= v public-key) idx)))
       first))

(defn part1 [key1-pub key2-pub]
  (->> (crypto-transform key2-pub)
       (drop (crack-private-key 7 key1-pub))
       first))
