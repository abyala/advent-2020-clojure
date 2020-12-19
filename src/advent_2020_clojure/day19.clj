(ns advent-2020-clojure.day19
  (:require [clojure.string :as str]
            [advent-2020-clojure.utils :as utils]))

(defn parse-rule [input]
  (let [[_ id rule-str] (re-matches #"(\d+): (.*)" input)]
    (if (= \" (first rule-str))
      [id {:s (str (second rule-str))}]
      (let [path-strs (->> rule-str (re-seq #"[^|]+") (map str/trim))]
        [id {:paths (map #(str/split % #" ") path-strs)}]))))

(defn parse-rules [rule-str]
  (->> rule-str str/split-lines (map parse-rule) (into {})))

(defn word-combinations [[a-words b-words & others]]
  (if (some? b-words)
    (let [words (for [a a-words
                      b b-words]
                  (str a b))]
      (word-combinations (cons words others)))
    a-words))

(defn valid-strings [rules id]
  (let [{:keys [s paths]} (rules id)]
    (if (some? s)
      (list s)
      (->> paths
           (map (fn [path]
                  (->> path
                       (map #(valid-strings rules %))
                       word-combinations)))
           (apply concat)))))

(defn part1 [input]
  (let [[rule-str message-str] (utils/split-blank-line input)
        rules (parse-rules rule-str)
        messages (str/split-lines message-str)
        valids (set (valid-strings rules "0"))]
    (->> messages
         (filter #(valids %))
         count)))