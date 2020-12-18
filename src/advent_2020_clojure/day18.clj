(ns advent-2020-clojure.day18
  (:require [clojure.string :as str]))

(defn index-of-or-nil
  "Returns the first index of the value `v` in the collection, or `nil` if it is not found."
  [coll v]
  (let [idx (.indexOf coll v)]
    (when (>= idx 0) idx)))

(defn parse-mathematical-string
  "Parses a line of arithmatic into a vector of BigIntegers or single-character String symbols."
  [line]
  (->> (re-seq #"\d+|\(|\)|\+|\*" line)
       (mapv #(if (#{"+" "*" "(" ")"} %) % (BigInteger. %)))))

(defn replace-subvec
  "Given a vector, remove the values from index `low` to `high`, inclusive, and replace with
  the `new-value`."
  [v low high new-value]
  (apply conj (subvec v 0 low) new-value (subvec v high)))

; Forward declaration, needed for mutual dependent functions.
(declare calculate)

(defn unpack-simple-expression
  "Returns the BigInteger expression within a single-element mathematical vector, or else nil."
  [tokens]
  (when (= 1 (count tokens)) (first tokens)))

(defn apply-parentheses
  "Returns the calculation of the inner-most parentheses, if any, or nil if there are no parentheses."
  [tokens operations]
  (when-let [close-paren (index-of-or-nil tokens ")")]
    (let [open-paren (.lastIndexOf (subvec tokens 0 close-paren) "(")
          new-value (-> (subvec tokens (inc open-paren) close-paren)
                        (calculate operations))]
      (-> (replace-subvec tokens open-paren (inc close-paren) new-value)
          (calculate operations)))))

(defn simple-math
  "Given a mathematical vector, executes the operation at the index and returns the simplified vector."
  [tokens idx]
  (let [[op tok-a tok-b] (map #(tokens (+ idx %)) [0 -1 1])
        new-val (case op
                  "*" (.multiply tok-a tok-b)
                  "+" (.add tok-a tok-b))]
    (replace-subvec tokens (dec idx) (+ idx 2) new-val)))

(defn ordered-arithmetic
  "Given a mathematical vector without parentheses, simplifies the value to a single BigInteger
  using the ordered operations."
  [tokens [op & other-ops :as operations]]
  (or (unpack-simple-expression tokens)
      (when-let [idx (->> tokens
                          (keep-indexed (fn [idx tok] (when (op tok) idx)))
                          first)]
        (-> (simple-math tokens idx)
            (ordered-arithmetic operations)))
      (ordered-arithmetic tokens other-ops)))

(defn calculate
  "Returns the BigInteger value of a mathematical vector, using the ordered operations."
  [tokens operations]
  (or (unpack-simple-expression tokens)
      (apply-parentheses tokens operations)
      (ordered-arithmetic tokens operations)))

(defn solve [input operations]
  (->> (str/split-lines input)
       (map parse-mathematical-string)
       (map #(calculate % operations))
       (reduce #(.add %1 %2))))

; Helper functions for order-of-operations
(def only-add (partial = "+"))
(def only-mul (partial = "*"))
(def add-or-mul (partial #{"+" "*"}))

(defn part1 [input] (solve input [add-or-mul]))
(defn part2 [input] (solve input [only-add only-mul]))