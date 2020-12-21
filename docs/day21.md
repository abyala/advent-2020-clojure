# Day Twenty-One: Allergen Assessment

* [Problem statement](https://adventofcode.com/2020/day/21)
* [Solution code](https://github.com/abyala/advent-2020-clojure/blob/master/src/advent_2020_clojure/day21.clj)

---

Today was a pretty straightforward puzzle, especially considering how complex the past few days of projects
have been. Because this mostly deals with playing around with map and set operations, I don't think I'm
going to find ways to simplify today's problem.  Let's just call it an exercise in stamping out the code.

Almost all of the work goes into part 1, so I'll explain the work in there.

---

## Part 1

We are given a data file of foods, where each food is a list of space-separated unrecognizable ingredients,
and one or more allergens. The goal is to find which ingredients are not allergens, and return the sum of
all times said ingredients appear in a food.

Again, we're going to do a little extra processing. Sorry!

First off, let's handle data parsing. My initial thought was to make a `parse-food` function, which reads
in a single line and returns a map of each allergen to the set of ingredients in which
it might be found.  So if the food were cheesecake and were defined as
`cheese cinnamon flour magic (contains dairy, gluten)`, the result would be
`{"dairy" #{"cheese" "cinnamon" "flour" "magic"}, "gluten" #{"cheese" "cinnamon" "flour" "magic"}}`.

Then `parse-input` just returns a sequence all of those maps found in the input file.
 
```clojure
(defn parse-food [line]
  (let [[_ ingredient-str allergen-str] (re-matches #"(.*) \(contains (.*)\)" line)
        ingredients (set (str/split ingredient-str #" "))
        allergens (set (str/split allergen-str #", "))]
    (->> (map #(vector % ingredients) allergens)
         (into {}))))

(defn parse-input [input]
  (->> input str/split-lines (map parse-food)))
```

The bulk of the work appears in `ingredients-with-allergens`, which takes in the parsed
sequence of allergens mapped to their set of potential ingredient culprits, and returns
a map of each allergen pointing to its actual ingredient. We're going to do a
`loop-recur`, but we need to do a little more data conditioning first. The idea is that
among all of the allergens, we assume that at least one of them appears connected to
only one ingredient. At that point, we've identified the connection between the
ingredient and the allergen, and we can assume that none of the other allergens are
associated to the same ingredient.

Since `parse-food` returns a sequence of maps, one
for each food, mapping each allergen to the potential ingredients, we want to combine
all of these maps together. The `merge-with` function comes to the rescue here, as that
function combines the entries in multiple maps by applying a joining function. In this
case, we're mapping the allergen to the set of potential ingredients, so for each food
we want to look at the intersection of ingredients; if food A says that allergen
`dairy` came from either `ingredient-a` or `ingredient-b`, and food B says that allergen
`dairy` came from either `ingredient-a` or `ingredient-c`, we know that `ingredient-a`
must be the ingredient as it applies to both rules. All of this can be accomplished
very simply by calling `(apply merge-with set/intersection foods)`.

Then the rest of the function performs a loop of all allergens without an associated
ingredient and the list of identified allergen-ingredient pairs. Each time, we
find an element in `unidentified` where the set of ingredients has only one value.
With that pair defined, we recurse into the loop by removing the allergen from the
`unidentified` map, and by also removing the ingredient from every other unidentified
allergen's set of ingredients.

```clojure
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
```

Then we need another function that takes in the same sequence of maps of foods, and
returns the number of times each ingredient appears in a food. Each map will contain
1 or more entries, but they each have the same set of values, since the food has the 
same ingredients no matter which allergen we look at.  So we can grab the first allergen,
whatever it is, from each map, and grab its set value using
`(map (partial (comp second first)) foods)` where `first` is the first map entry, and
`second` is the value for that entry, i.e. the set of ingredients. With that done, we
concatenate all of the sets into a big sequence, and call `frequencies` to get our result.

```clojure
(defn ingredient-frequencies [foods]
  (->> foods
       (map (partial (comp second first)))
       (apply concat)
       frequencies))
```

Finally, we're ready to solve part 1. We'll parse the data and calculate the food
sequences and call `ingredients-with-allergens` to find out which ingredient contains
which allergen. We want to throw away all of the ingredients associated to allergens,
so we'll pull out the ingredients using `(map second allergens-to-foods)`, then
dissociate them from the frequency map, leaving us with just a map of each "safe"
ingredient and its frequency count.  Then we just add it up to get the answer!

```clojure
(defn part1 [input]
  (let [foods (parse-input input)
        allergens-to-foods (ingredients-with-allergens foods)
        ingr-freqs (ingredient-frequencies foods)]
    (->> (map second allergens-to-foods)
         (reduce (partial dissoc) ingr-freqs)
         (map second)
         (apply +))))
```

---

## Part 2

As I already said, we did enough work for Part 1 and Part 2 requires almost no work.
We need to sort the allergens by the names of their ingredients, and then combine them
into a comma-separated string, and there's nothing to it.  Parse the data and grab
the map of allergens to ingredients. We can call `sort` on a map, which sorts the 
`[key value]` vectors, so we sort by the allergen (`first`) and then map out the
ingredient (`second`). Finally, join the strings with `","` as the delimiter. Piece
of cake!

```clojure
(defn part2 [input]
  (->> (parse-input input)
       ingredients-with-allergens
       (sort-by first)
       (map second)
       (str/join ","))) 
```