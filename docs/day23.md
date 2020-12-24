# Day Twenty-Three: Crab Cups

* [Problem statement](https://adventofcode.com/2020/day/23)
* [Solution code](https://github.com/abyala/advent-2020-clojure/blob/master/src/advent_2020_clojure/day23.clj)

---

Another day, another game to play with our friendly neighborhood crab. This time, we're playing multiple
rounds of picking up cups and moving them around in a circle. This puzzle is all about finding a good
data structure to represent the data, and while my solution for part 2 isn't the fastest (30-40 seconds),
I feel it's workable to pass.

---

## Part 1

To start off, let's consider our data structure. In Java, I'd use a simple `LinkedList` to represent the
circle of cups, since it's trivial to mutate the data. And to be honest, that works just fine in Clojure
too, at least in part one. The biggest problem with using a `LinkedList` is that we have to do a linear scan
when we look for the destination cup, and we increase the number of cups in the circle and/or the number of
rounds in the game, that `O(m*n)` algorithm, where `m=size-of-circle` and `n=number-of-rounds` slows down
quickly. To change the runtime to `O(n)`, we need to use an associative data structure, so I've chosen a map,
where each key is a `cup-id` and its value is the `cup-id` that's next in the circle.

To start, let's parse the input. First we'll do a simple integer parse of the input String, using
`(partition 2 1 cup-ids cup-ids)` to make the mapping. The `(partition 2 1...)` part says to break the input
into groupings of size 2, such that the `step` allows the first tuple's value to overlap with the second
tuple's key. I use `cup-ids` twice; the second instance is the collection we want to partition, while the
first collection is padding to apply to complete any incomplete groupings. Because the last element in the
collection will be incomplete (there's no value after the last one), we pad it with the first value of the
collection itself, thus making this a circular list. Finally, the data structure includes the newly-constructed
`:cups` map, the `:current` player (first in the list), and the number of records in the circle, for a simple
optimization.

```clojure
(defn init-cups [input]
  (let [cup-ids (map #(Integer/parseInt (str %)) input)
        data-map (->> (partition 2 1 cup-ids cup-ids)
                      (map vec)
                      (into {}))]
    {:current (first cup-ids) :cups data-map :size (count cup-ids)}))
```

Next, I made a simple helper function called `next-cup`, which takes in a cup ID and looks up the next cup
in the circle. It's just a simplification to make the rest of the code more expressive.

```clojure
(defn next-cup [state id]
  (get-in state [:cups id]))
```

We've represented the data as a map, but there are times when we'll want to treat it as a linked list, such
that we can pull out a number of values in ID order as a sequence. For this, I created the lazy sequence
function `cup-seq`. This function looks up the next value using `next-cup`, defaulting to starting at the
`current` cup, returning that value, and then lazily recursing. 

```clojure
(defn cup-seq
  ([state] (cup-seq state (:current state)))
  ([state lookup]
   (let [v (next-cup state lookup)]
     (lazy-seq (cons v (cup-seq state v))))))
```

The `destination-cup` will help us figure out the ID of the cup after which we'll place the three that we remove.
This is just a simple `loop-recur` that looks for a value starting from 1 less than the `current` cup, looping from
`0` up to the max cup size, and skipping over any cup in the `disallowed` set.

```clojure
(defn destination-cup [{:keys [current size]} disallowed]
  (loop [v (dec current)]
    (cond
      (zero? v) (recur size)
      (disallowed v) (recur (dec v))
      :else v)))
```

With that infrastructure in place, let's write the `next-turn` function to play out one turn of the game. Using
the `cup-seq` function, we'll pull out the next four values -- the first three will moved after the `destination`,
and the next one will become the next `current` value on the next turn. We then calculate the destination and the
cup currently next to the destination. We can provide the new `state` with a single `assoc` call. The big change
is to the `:cups`, and everything in there is the stuff we'd normally do with a mutable `LinkedList`:

1. Because we removed the three picked cups, the `current` cup is now next to the `next-current`, meaning the one
four cups away.
2. The destination cup now points to `pick1`, as it's the start of the three selected cups.
3. The last selected cup, `pick3`, now points to `next-dest`, which is the cup that `dest` used to point to.

We don't need to make any changes to `pick1` or `pick2`, `pick1` still points to `pick2`, and `pick2` still points
to `pick3`.

```clojure
(defn next-turn [{:keys [current cups] :as state}]
  (let [[pick1 pick2 pick3 next-current] (cup-seq state)
        dest (destination-cup state #{pick1 pick2 pick3})
        next-dest (next-cup state dest)]
    (assoc state :current next-current
                 :cups (assoc cups current next-current
                                   dest pick1
                                   pick3 next-dest))))
```

Now that we can play a single turn, let's play a full game! This one is a breeze - use `iterate` to keep playing
turns indefinitely, and use `nth` to pull out the turn number we want. One thing I should mention is that I
usually use the thread-last macro `->>` as a way of applying functions to each element in a collection. While
the `iterate` function returns a sequence of results, `nth` acts on the sequence itself rather than every element,
which is why it accepts the collection as its first parameter instead of its last. Therefore, we use the
thread-first macro `->` here.

```clojure
(defn play-game [state turns]
  (-> (iterate next-turn state)
      (nth turns)))
```

We're almost done.  For part 1, we'll need to assign a label to the final state, but concatenating the list of
all cups starting clockwise from cup `1`. Once again, we can use out magic `cup-seq` to start from ID=1 instead of
`current`, use `take-while` to keep all the values until we hit ID 1, and then string it all together.

```clojure
(defn label [state]
  (->> (cup-seq state 1)
       (take-while #(not= % 1))
       (apply str)))
```

Finally, to solve part 1, we'll initialize the cup state, play the game, and return the label. Note again the
use of the thread-first macro.

```clojure
(defn part1 [input num-turns]
  (-> (init-cups input)
      (play-game num-turns)
      label))
```

---

## Part 2

Given what we've written for part 1, in part 2 we just throw a whole lot more data at the problem. There's really
only one large function we need to implement here, and that is a way to extend the original cup state to support
a large number of additional cups being added. The `extend-cups` function will accept an already-created cup
state and the value of the largest cup we want to hold. Mental note - cups are numbered from 1 to max but most
functions like `range` operate from 0 to `(- count 1)`, so we'll have to watch out for that.

First, I made a tiny helper function called `tail`, which returns the ID of the cup that points to the head of
the state. In theory I could have kept the original input String and picked up the last character, but I thought
it cleaner to the algorithm flow to use the input once and then throw it away, so this little function just finds
the first (and only) value that points to the "current" value.

```clojure
(defn tail [{:keys [current cups]}]
  (->> cups
       (keep (fn [[k v]]
               (when (= current v) k)))
       first))
```

Then the `extend-cup` function does a lot, but it's all reasonable. First, we want to make a map of items we
want to add to the existing `:cups` map. We'll again use the four-argument `partition` function, starting from
one more than the largest existing cup ID, and ending with `(inc max-cup)`, since range uses the last parameter as
an _exclusive_ upper bound, and we use the `current` value, the head of the list, as the padding value. We throw
those tuples into a map of `{tail extension-start}` so the final value of the initial state will join over to the
first new value.  Finally, in addition to extending the `size` of the new state, we use `merge` to overwrite any
values in the original `:cups` map with the new values. In practice, this should only overwrite the old tail value,
and then add in a bunch of new ones.

```clojure
(defn extend-cups [state max-cup]
  (let [{:keys [current cups]} state
        extension-start (inc (apply max (keys cups)))
        extension-end (inc max-cup)
        extensions (->> (partition 2 1 [current] (range extension-start extension-end))
                        (map vec)
                        (into {(tail state) extension-start}))]
    (assoc state :cups (merge cups extensions)
                 :size max-cup))) 
```

And then for the `part2` function, we take the initial state and extend the cups, and then we play the game until
the intended turn number. Finally, we have to multiply the two successive values after cup ID 1, so once more we
use `cup-seq`, `take` the next two values, and multiple them.  There ya go!

```clojure
(defn part2 [input max-cup num-turns]
  (let [state (-> (init-cups input) (extend-cups max-cup))
        answer (play-game state num-turns)]
    (->> (cup-seq answer 1)
         (take 2)
         (apply *))))
```

So again, this isn't the fastest algorithm, but the first several attempts were much slower, so I'll buy it!
We're almost done with Advent of Code!