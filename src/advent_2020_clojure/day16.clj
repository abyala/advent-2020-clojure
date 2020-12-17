(ns advent-2020-clojure.day16
  (:require [clojure.string :as str]
            [advent-2020-clojure.utils :as utils]))

(defn parse-input [input]
  (let [[all-fields-txt [_ my-ticket-txt] [_ & nearby-tickets-txt]] (utils/split-blank-line-seq input)]
    {:fields         (->> all-fields-txt
                          (mapv #(let [[_ name from1 to1 from2 to2] (re-matches #"(.*): (\d+)-(\d+) or (\d+)-(\d+)" %)]
                                   [name
                                    [(Integer/parseInt from1) (Integer/parseInt to1)]
                                    [(Integer/parseInt from2) (Integer/parseInt to2)]])))
     :my-ticket      (->> (str/split my-ticket-txt #",")
                          (mapv #(Integer/parseInt %)))
     :nearby-tickets (->> nearby-tickets-txt
                          (mapv (fn [s] (->> (str/split s #",")
                                             (mapv #(Integer/parseInt %))))))}))

(defn invalid-fields [{:keys [fields nearby-tickets]}]
  (let [every-range (->> (map rest fields) (apply concat))]
    (->> (flatten nearby-tickets)
         (filter (fn [fld] (not-any? (fn [[x y]] (<= x fld y)) every-range))))))

(defn part1 [input]
  (->> input parse-input invalid-fields (apply +)))

(defn possible-indexes [ticket-pairs num-fields [name [low1 high1] [low2 high2]]]
  (->> (reduce (fn [acc [idx v]]
                 (if (or (<= low1 v high1) (<= low2 v high2))
                   acc
                   (disj acc idx)))
               (set (range num-fields))
               ticket-pairs)
       (vector name)))

(defn all-possible-indexes [fields nearby-ticket-pairs]
  (->> fields
       (map #(possible-indexes nearby-ticket-pairs (count fields) %))
       (into {})))

(defn remove-all-traces [possible-indexes index]
  (loop [fields possible-indexes, keys (keys possible-indexes)]
    (if-let [k (first keys)]
      (recur (update fields k #(disj % index))
             (rest keys))
      fields)))

(defn field-mappings [fields nearby-ticket-pairs]
  (loop [unsolved (all-possible-indexes fields nearby-ticket-pairs)
         solved {}]
    (if (empty? unsolved)
      solved
      (let [[name actual-index] (->> unsolved
                                     (keep (fn [[name indexes]]
                                             (when (= 1 (count indexes))
                                               [name (first indexes)])))
                                     first)]
        (recur (-> unsolved
                   (dissoc name)
                   (remove-all-traces actual-index))
               (assoc solved name actual-index))))))

(defn valid-tickets-only [{:keys [nearby-tickets] :as parsed}]
  (let [bad-fields (-> parsed invalid-fields set)]
    (filterv (fn [t] (not-any? #(bad-fields %) t))
             nearby-tickets)))

(defn ticket-pairs [tickets]
  (mapcat (fn [ticket] (map-indexed (fn [idx v] (vector idx v))
                                    ticket))
          tickets))

(defn part2 [input]
  (let [{:keys [fields my-ticket] :as parsed} (parse-input input)
        valid-tickets (valid-tickets-only parsed)
        nearby-ticket-pairs (ticket-pairs valid-tickets)
        mappings (field-mappings fields nearby-ticket-pairs)]
    (->> mappings
         (keep (fn [[name idx]]
                 (when (str/starts-with? name "departure") (get my-ticket idx))))
         (apply *))))
