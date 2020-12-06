(ns advent-2020-clojure.utils
  (:require [clojure.string :as str]))

(defn xor
  "Given a collection, returns true if exactly one element resolves to \"true\""
  [f coll]
  (= 1 (count (filter f coll))))

(defn split-blank-line
  "Given an input string, returns a sequence of sub-strings, separated by a completely
  blank string. This function preserves any newlines between blank lines, and it filters
  out Windows' \"\r\" characters."
  [input]
  (as-> (str/replace input "\r" "") x
        (str/split x #"\n\n")))

(defn split-blank-line-seq
  "Given an input string, returns a sequence of vectors of strings. Completely blank
  lines separate the outer sequence, and the inner vectors are split by newlines."
  [input]
  (->> (split-blank-line input)
       (map str/split-lines)))