(ns advent-2020-clojure.utils)

(defn xor [f coll]
  (= 1 (count (filter f coll))))
