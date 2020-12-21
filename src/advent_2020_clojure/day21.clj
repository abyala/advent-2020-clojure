(ns advent-2020-clojure.day21
  (:require [clojure.string :as str]
            [clojure.set :as set]))

(defn parse-food [line]
  (let [[_ ingredient-str allergen-str] (re-matches #"(.*) \(contains (.*)\)" line)
        ingredients (set (str/split ingredient-str #" "))
        allergens (set (str/split allergen-str #", "))]
    (->> (map #(vector % ingredients) allergens)
         (into {}))))

(defn parse-input [input]
  (->> input str/split-lines (map parse-food)))

(defn ingredients-with-allergens [foods]
  (loop [unidentified (apply merge-with set/intersection foods)
         identified {}]
    (if (empty? unidentified)
      identified
      (let [[allergen ingredient] (->> unidentified
                                       (keep (fn [[k v]]
                                               (when (= 1 (count v)) [k (first v)])))
                                       first)]
        (recur (->> (dissoc unidentified allergen)
                    (map (fn [pair] (update pair 1 #(disj % ingredient))))
                    (into {}))
               (assoc identified allergen ingredient))))))

(defn ingredient-frequencies [foods]
  (->> foods
       (map (partial (comp second first)))
       (apply concat)
       frequencies))

(defn part1 [input]
  (let [foods (parse-input input)
        allergens-to-foods (ingredients-with-allergens foods)
        ingr-freqs (ingredient-frequencies foods)]
    (->> (map second allergens-to-foods)
         (reduce (partial dissoc) ingr-freqs)
         (map second)
         (apply +))))

(defn part2 [input]
  (->> (parse-input input)
       ingredients-with-allergens
       (sort-by first)
       (map second)
       (str/join ",")))