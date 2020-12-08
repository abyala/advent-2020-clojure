(ns advent-2020-clojure.day07
  (:require [clojure.string :as str]
            [clojure.set :as set]))

(def shiny-gold "shiny gold")

(defn parse-line
  "Parses a single line of text into the following data structure:
   [name {:children {names num-required}}] "
  [line]
  (let [[_ container matches] (re-matches #"([^,]+) bags contain (.*)" line)
        matches (->> (re-seq #"(\d+) ([^,]+) bag" matches)
                     (map (fn [[_ c desc]] [desc (Integer/parseInt c)]))
                     (into {}))]
    [container {:children matches}]))

(defn parse-ruleset
  "Parses the data set into a map of all bags to their children and parents:
  {name {:children {names num-required} :parents #{names}}}"
  [input]
  (->> (str/split-lines input)
       (map parse-line)
       (into {})))

(defn parent-mappings
  "Converts a parsed ruleset into a map of bags to their parents:
  {name #{parent-names}}."
  [ruleset]
  (->> ruleset
       (mapcat (fn [[name {:keys [children]}]]
                 (for [[c _] children] [c name])))
       (group-by first)
       (map (fn [[child parents]]
              [child (set (map second parents))]))
       (into {})))

(defn ancestors-of
  "Given a parent-map, returns all ancestors of a bag."
  [parent-map name]
  (->> (parent-map name)
       (map #(conj (ancestors-of parent-map %) %))
       (apply set/union)))

(defn part1 [input]
  (-> (parse-ruleset input)
      parent-mappings
      (ancestors-of shiny-gold)
      count))

(defn total-children
  "Recursively checks the number of bags needed to fit in a single bag."
  [rules name]
  (->> (get-in rules [name :children])
       (map (fn [[child num]] (-> (total-children rules child)
                                  inc
                                  (* num))))
       (apply +)))

(defn part2 [input]
  (total-children (parse-ruleset input) shiny-gold))