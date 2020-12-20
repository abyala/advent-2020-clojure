(ns advent-2020-clojure.day19
  (:require [clojure.string :as str]
            [advent-2020-clojure.utils :as utils]
            [clojure.set :as set]))

(defn parse-rule [input]
  (let [[_ id rule-str] (re-matches #"(\d+): (.*)" input)]
    (if (= \" (first rule-str))
      [id {:s (str (second rule-str))}]
      (let [path-strs (->> rule-str (re-seq #"[^|]+") (map str/trim))]
        [id {:paths (map #(str/split % #" ") path-strs)}]))))

(defn parse-rules [rule-str]
  (->> rule-str str/split-lines (map parse-rule) (into {})))

(defn match-length [rules word rule]
  (let [{:keys [s paths]} (rules rule)]
    (cond
      (str/blank? word) nil
      (some? s) (when (= s (subs word 0 (count s))) #{(count s)})
      :else (->> (for [path paths]
                   (reduce (fn [lengths id]
                             (if (empty? lengths)
                               #{}
                               (->> (map (fn [length]
                                           (->> (match-length rules (subs word length) id)
                                                (keep identity)
                                                (map (fn [p] (+ length p)))
                                                set))
                                         lengths)
                                    (apply set/union))))
                           #{0}
                           path))
                 (keep identity)
                 (apply set/union)))))

(defn valid? [rules word]
  (some #(= % (count word)) (match-length rules word "0")))

(defn part1 [input]
  (let [[rule-str message-str] (utils/split-blank-line input)
        rules (parse-rules rule-str)]
    (->> (str/split-lines message-str)
         (keep #(valid? rules %))
         count)))

(defn part2 [input]
  (let [[rule-str message-str] (utils/split-blank-line input)
        rules (-> (parse-rules rule-str)
                  (assoc "8" (second (parse-rule "8: 42 | 42 8")))
                  (assoc "11" (second (parse-rule "11: 42 31 | 42 11 31"))))]
    (->> (str/split-lines message-str)
         (keep #(when (valid? rules %) %))
         count)))