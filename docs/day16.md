# Day Sixteen: Ticket Translation

* [Problem statement](https://adventofcode.com/2020/day/16)
* [Solution code](https://github.com/abyala/advent-2020-clojure/blob/master/src/advent_2020_clojure/day16.clj)

---

Today's problem was a matter of parsing a complex incoming string, and then doing a bunch of set manipulation, maps,
and filters. To be honest, I didn't find this one very entertaining, so I'm just going to quickly run through what
each function does, show how the shape of the data changes from function to function, and call it a day.

---

## Part 1

The `parse-input` function parses the incoming String into my state object. I leverage the `utils/split-blank-line-seq`
function from a few problems ago, which converts the giant String into three components -- one for the rules, the
"your ticket" section, and the "nearby tickets" section. Then it's a little regex and a whole lot of Integer parsing.
The output shape is a map, that definitely could be simplified if I were so inclined.
 
```clojure
; Output shape
{:fields [["name" [low1 high1] [low2 high2]]]
 :my-ticket [1 2 3 4]
 :nearby-tickets [[1 2 3] [4 5 6] [7 8 9]]}
```

The `invalid-fields` function looks at all of the fields and combines them into a single list of `[low high]` pairs.
Then the `flatten` function fully flattens the nearby tickets into a single list of integer values, which we filter
down using `not-any?` to return only the values that aren't within any of the ranges. The output shape is a simple
list of integers, representing the invalid fields.

```clojure
(defn invalid-fields [{:keys [fields nearby-tickets]}]
  (let [every-range (->> (map rest fields) (apply concat))]
    (->> (flatten nearby-tickets)
         (filter (fn [fld] (not-any? (fn [[x y]] (<= x fld y)) every-range))))))
```

Then `part1` just adds together all invalid fields.

```clojure
(defn part1 [input]
  (->> input parse-input invalid-fields (apply +)))
```

---

## Part 2

Part 2 was a little trickier to solve, and my answer is a bit wordy. But I wanted to focus a bit more on transforming
the data from one state to another, rather than doing everything inline as a giant list. So the solution is a bunch
of small transformations.

The overall strategy was to rip apart the complexity of the ticket structure. So I wanted to make a map of sets. To
start, every ticket field was mapped to the set of all possible field indices. Then we go through every nearby ticket,
and for every field type in which the ticket field doesn't fit, remove that index from the field type's set of 
indices. By removing every rule violation, we should be left with only the field types and the indexes for which all
tickets validate. So with that... 

`valid-tickets-only` takes in the full state, and returns only the nearby tickets that are valid, meaning that they
don't have any of the fields described previously in the `invalid-fields` function. This returns all of the remaining
tickets in their normal form, so `[[1 2 3] [4 5 6] [7 8 9]]`.

```clojure
(defn valid-tickets-only [{:keys [nearby-tickets] :as parsed}]
  (let [bad-fields (-> parsed invalid-fields set)]
    (filterv (fn [t] (not-any? #(bad-fields %) t))
             nearby-tickets)))
```

The next function, `ticket-pairs`, rips out every `[idx v]` tuple across all of the nearby tickets, since I found that
easier to reason with than keeping the original vector of vectors. So this function takes in the ticket list, of shape
`[[1 2 3] [4 5 6] [7 8 9]]` and returns a simple `[[0 1] [1 2] [2 3] [0 4] [1 5] [2 6] [0 7] [1 8] [2 9]]` vector.

```clojure
(defn ticket-pairs [tickets]
  (mapcat (fn [ticket] (map-indexed (fn [idx v] (vector idx v)) ticket))
          tickets))
```

The first beefy function is `possible-indexes`, with its coordinating function `all-possible-indexes`.
`possible-indexes` takes in the list of all `ticket-pairs` and the total number of fields in the tickets, and then
a single `field` rule definition of form `["name" [low1 high1] [low2 high2]]` from problem state. We run a reducing
function, starting with a set of all possible indexes, and then we look at every ticket pair. If that pair's value
doesn't comply with the rule, remove that pair's index from the set of possible indexes. There's a whole bunch of
extra calculations going on, but this algorithm is still very fast. As a reminder to my future self -- we use `dissoc`
to remove a mapping from a map, but `disj` to remove a value from a set. Why one function can't do both...

Then `all-possible-fields` just maps each field to the set of possible indexes, and throws it all into a map.

```clojure
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
```
Armed with the possible fields, our goal is to use `field-mappings` to actually decide which field belongs to which
index. We use a simple `loop-recur`, starting with all fields being `unsolved`, and moving them one-by-one into the
`solved` set. In each loop, we find one field that only has one index that is valid and hasn't been claimed yet by
another field.  Knowing the field name and its singular index, we use `remove-all-traces` to eliminate that index
from all remaining fields' set of indexes, and loop again.

```clojure
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
```

Finally, `part2` puts the pieces together. Starting with the input, we parse it, identify the valid tickets, flatten
them into ticket pairs, and identify the mappings. With the correct mappings in-place, we use a `keep-when` to pick
the fields whose name starts with `departure`, map each to the value that that index in `my-ticket`, and multiple the
values together.

```clojure
(defn part2 [input]
  (let [{:keys [fields my-ticket] :as parsed} (parse-input input)
        valid-tickets (valid-tickets-only parsed)
        nearby-ticket-pairs (ticket-pairs valid-tickets)
        mappings (field-mappings fields nearby-ticket-pairs)]
    (->> mappings
         (keep (fn [[name idx]]
                 (when (str/starts-with? name "departure") (get my-ticket idx))))
         (apply *))))
```

All in all, there's a bunch of code here, but it's all reasonably straightforward... now that it's done!