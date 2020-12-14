(ns advent-2020-clojure.day14
  (:require [clojure.string :as str]))

; State: {:mask "", :memory {n v}}

(def empty-mask "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")
(def empty-state
  {:mask empty-mask :memory {}})

; NOTE: This should take in a String, not a number...
(defn pad-n [n]
  (let [binary (Long/toBinaryString n)
        pad (apply str (take (- 36 (count binary))
                             (repeat \0)))]
    (str pad binary)))
(defn binary-to-long [b]
  (Long/parseLong b 2))

(defn mask-n [n mask]
  (->> (map (fn [m v] (if (= \X m) v m))
            mask
            (pad-n n))
       (apply str)
       binary-to-long))

(defn process-line [line state]
  (condp re-matches line
    #"mask = (\w+)" :>> (fn [[_ mask]] (assoc state :mask mask))
    #"mem\[(\d+)\] = (\d+)" :>> (fn [[_ loc v]] (assoc-in state [:memory (Long/parseLong loc)]
                                                          (mask-n (Long/parseLong v) (:mask state))))))

(defn part1 [input]
  (loop [[line & next-lines] (str/split-lines input), state empty-state]
    (if-not line
      (->> (:memory state) vals (apply +))
      (recur next-lines (process-line line state)))))

; TODO: Change to a char array
(defn floating-addresses [s]
  (if-let [idx (str/index-of s \X)]
    (into (floating-addresses (apply str (assoc (vec s) idx 0)))
          (floating-addresses (apply str (assoc (vec s) idx 1))))
    (list s)))

(defn convert-address-to-masked-addresses [address mask]
  (let [binary (pad-n (Long/parseLong address))]
    (->> (map (fn [m v] (if (= m \0) v m))
              mask
              binary)
         (apply str)
         floating-addresses)))

(defn process-line2 [line state]
  (condp re-matches line
    #"mask = (\w+)" :>> (fn [[_ mask]] (assoc state :mask mask))
    #"mem\[(\d+)\] = (\d+)" :>> (fn [[_ loc v]]
                                  (let [all-addresses (convert-address-to-masked-addresses loc (:mask state))]
                                    (->> all-addresses
                                         (map binary-to-long)
                                         (map #(vector % (Integer/parseInt v)))
                                         (into (:memory state))
                                         (assoc state :memory))))))

(defn part2 [input]
  (loop [[line & next-lines] (str/split-lines input), state empty-state]
    (if-not line
      (->> (:memory state) vals (apply +))
      (recur next-lines (process-line2 line state)))))