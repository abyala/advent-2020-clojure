(ns advent-2020-clojure.day14
  (:require [clojure.string :as str]))

; State: {:mask "", :memory {"n" v}}
(def empty-mask "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")
(def empty-state {:mask empty-mask :memory {}})

(defn zero-pad [s]
  (let [space-padded (->> s Long/parseLong Long/toBinaryString (format "%36s"))]
    (str/replace space-padded \space \0)))

(defn binary-to-long [b]
  (Long/parseLong b 2))

(defn mask-value [v mask]
  (->> (zero-pad v)
       (map (fn [m v] (if (= \X m) v m)) mask)
       (apply str)
       binary-to-long))

(defn update-mask [state mask]
  (assoc state :mask mask))

(defn set-bitmasked-value [state addr v]
  (assoc-in state [:memory addr] (mask-value v (:mask state))))

(defn floating-addresses [s]
  (if-not (str/index-of s \X)
    (list s)
    (mapcat #(floating-addresses (str/replace-first s \X %))
            [\0 \1])))

(defn masked-addresses [address mask]
  (->> (zero-pad address)
       (map (fn [m v] (if (= m \0) v m)) mask)
       (apply str)
       floating-addresses))

(defn set-bitmasked-addresses [state addr v]
  (->> (:mask state)
       (masked-addresses addr)
       (map #(vector % (Integer/parseInt v)))
       (update state :memory (partial into))))

(defn process-line [line state mem-command]
  (condp re-matches line
    #"mask = (\w+)" :>> (fn [[_ mask]] (update-mask state mask))
    #"mem\[(\d+)\] = (\d+)" :>> (fn [[_ addr v]] (mem-command state addr v))))

(defn solve [input mem-command]
  (->> (str/split-lines input)
       (reduce #(process-line %2 %1 mem-command) empty-state)
       :memory
       vals
       (apply +)))

(defn part1 [input] (solve input set-bitmasked-value))
(defn part2 [input] (solve input set-bitmasked-addresses))