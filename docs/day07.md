# Day Seven: Handy Haversacks

* [Problem statement](https://adventofcode.com/2020/day/6)
* [Solution code](https://github.com/abyala/advent-2020-clojure/blob/master/src/advent_2020_clojure/day06.clj)

---

## Part 1

Today's problem was all about creating a tree, and then looking up and down said tree. The problem states that certain
bags can contain certain numbers of other bags. For part 1, we have to start from a "shiny gold" bag, and count the
number of types of other bags that either can hold it, or can hold it by way of intermediate bags.

As always, we start off parsing the input. This will sound obvious, but it's really important to determine the shape
of the data we want to create when parsing data, especially in a language like Clojure where `(= code data)`. Given
how the input looks, I figured we should get a map of each bag type, pointing to the children it can hold.  Each child
would itself be a map of the bag type to the number it holds.  See the examples below:

```
; Input one
light red bags contain 1 bright white bag, 2 muted yellow bags.
{"light red" {"bright white" 1, "muted yellow" 2}}

; Input two
faded blue bags contain no other bags.
{"faded blue" {}}
```
 
 So let's start with parsing a single line in the aptly-named `parse-line` function. It takes in a string and returns
 the above structure. Note that after the word `contain` there can be zero or more child mappings, so let's take this
 step by step. First, let's strip our the name of the bag, and separate that from the potential children. Our good
 friend `re-matches` will handle this. Because `name` is an overloaded term, I'll define the parent `container`.
 
```clojure
(let [[_ container matches] (re-matches #"([^,]+) bags contain (.*)" line)
     MORE_TO_COME)
```

Now we could use either a `loop-recur` or a recursive function to keep searching through the `matches` binding for all
possible children, but Clojure has a better way -- the `re-seq` function. This takes in a regular expression and an 
input string, and returns a sequence of all matches it finds.  So we'll apply that to the `matches` binding, grabbing
both the count as `c` and the child name as `desc`. If the mapping function creates a two-element vector of 
`[desc c]`, then we can use `(into {} %)` to combine those pairs into one big map. Finally, with one big map of all
children, the `parse-line` function returns the entire data structure we need:

```clojure
(defn parse-line [line]
  (let [[_ container matches] (re-matches #"([^,]+) bags contain (.*)" line)
        matches (->> (re-seq #"(\d+) ([^,]+) bag" matches)
                     (map (fn [[_ c desc]] [desc (Integer/parseInt c)]))
                     (into {}))]
    [container {:children matches}]))
```

Ok, then it's time to parse the entire input. Usually, I'd make a function called `parse-input`, but we're going to do
a bunch of data transformations, so let's call it `parse-ruleset`. Nothing special here - split the input by line, map
each line using `parse-line`, and throw it all into one giant map that represents our ruleset.

```clojure
(defn parse-ruleset [input]
  (->> (str/split-lines input)
       (map parse-line)
       (into {})))
```

With the data all parsed and ready to go, let's look again at part 1. We want to start from a node and look up at its
parents and grandparents, but our data is structured to start from a node and look at its children. So let's flip the
data around. `parent-mappings` should reformat our ruleset into a map of each bag to a set of its immediate parents.

To do that, we'll apply a few transformations.  First, we'll use `mapcat` on each element of the ruleset. Remember that
calling `map` on a map will break the contents down into `[k v]` vectors, where in this case the key is the bag name 
and the value is the map of child names to their quantities. We want `mapcat` to unravel each element of the ruleset
into a sequence of `[child parent]` elements, all in one big sequence. Then we use `group-by` to create a new map of
each bag to a vector of its `[child parent]` vectors, because that will lead us to the list of all parents. Next,
we apply a map over this new map, where each entry will be of format `[child [[child parent]]]`. We don't need all of
that structure; we just want this to be of format `[child #{parent}]`, so our map strips out the second element of
`[[child parent]]` for us. Then we push all of that back into yet another new map. The result is a nice data structure
we can use for calculating parents:  `{child #{parent}}`.

It sounds like a lot of transformations - and it is. But run it through the REPL one line at a time, and you'll see
it's really not that bad.

```clojure
(defn parent-mappings [ruleset]
  (->> ruleset
       (mapcat (fn [[name {:keys [children]}]]
                 (for [[c _] children] [c name])))
       (group-by first)
       (map (fn [[child parents]]
              [child (set (map second parents))]))
       (into {})))
```

We're almost done. We now want to calculate all of the ancestors of a bag type, and for that we'll use a little
recursion. Starting from the `parent-map` shown above, we grab the set of parents of our bag, and recursively call
`ancestors-of` for each one. (We assume there are no loops - if a red bag holds a green bag which holds a red bag, 
I think the universe would collapse into itself and all life as we know it would end.) Each recursive call returns
a set, so we just union them all together. The base case works here too -- if nothing holds a bag, then
`ancestors-of` should return an empty set, which it does.

```clojure
(defn ancestors-of [parent-map name]
  (->> (parent-map name)
       (map #(conj (ancestors-of parent-map %) %))
       (apply set/union)))
```

And then finally for part 1, we parse the input, convert it into the parent mappings, get the set of ancestors, and
count them up!

```clojure
(defn part1 [input]
  (-> (parse-ruleset input)
      parent-mappings
      (ancestors-of shiny-gold)
      count))
```

---

## Part 2

For this part, we need to look down the graph at the children of a bag, instead of its parents. We need to see the
total number of bags that fit within a single "shiny gold" bag. Luckily, our initially-parsed data structure is ready
for us to use!

Calculating the number of children is again a move for recursion. I thought about memoizing initial data set, by
calculating the number of bags within each bag once apiece; that would certainly show better performance if our data
set were extremely large, but Big-O notation is meaningless when the data is small, and I'd rather optimize for
simplicity when performance isn't crucial.

The recursive algorithm starts by finding the children of the target bag type. Remember that the structure of the
ruleset is `{name {:children {child-name count}}}`. Looking back, I could have simplified the data structure as
`{name {child-name count}}`, but it's too late to change now. The `get-in` function helps us, by taking a map and
a vector of accessors, and calling them one by one. So `(get-in rules [name :children])` has the same effect as
calling `(-> (name rules) :children)`. Then with that list of children, we recursively count the number of children,
increment the result to account for the child bag itself, and then multiple that by the number of children that the
parent contains.  Add them all up, and we're done!

```clojure
(defn total-children [rules name]
  (->> (get-in rules [name :children])
       (map (fn [[child num]] (-> (total-children rules child)
                                  inc
                                  (* num))))
       (apply +)))

(defn part2 [input]
  (total-children (parse-ruleset input) shiny-gold))
```