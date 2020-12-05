# Day Five: Binary Boarding

* [Problem statement](https://adventofcode.com/2020/day/5)
* [Solution code](https://github.com/abyala/advent-2020-clojure/blob/master/src/advent_2020_clojure/day05.clj)

---

## Part 1

I rather liked today's puzzle - simple problem, no tricky surprises. Also, I think I've already
solved it four or five ways, so I like the variety of options!

We're looking for our seat on a plane, given instructions on how to get the row and column based on
an input String. Of the 128 rows (0-127) and 8 columns (0-7), we split the rows in half and go to
the front half (0-63) if the first character is `F` or the back half (64-127) if the first character
is `B`. Then repeat for a total of 7 times until we find the right row.  Then do the same for the 
last 3 characters using `L` for low and `R` for high to find the column. Then the seat ID is
the row * 8 plus the column.

So first, let's split each 10-character seat assignment into two strings - the first 7 and the last 3.
We could manually create a vector using `[(take 7 seat) (drop 7 seat)]`, but `(split-at 7 seat)`
does the work for us. This will create a vector with two char sequences.

```clojure
(defn split-seat [seat] (split-at 7 seat))
```

Next, we need to figure out how to find the correct row or column using the so-called "binary
space partition" algorithm. The easiest solution I could find involves treating the front and back
portions of the seat as binary numbers - if we convert `F` and `L` into `0`, and `B` and `R` into `1`,
then these Strings convert beautifully. From the test cases, `FBFBBFF` should resolve to row 44,
which is `0101100`. Similarly, `RLR` is 5, which in binary is `101`.

Let's write a little helper function that converts a character into either a zero or one. Once
again, we can use `(if (a-set test))` as a boolean expression to see if `test` is a member of the
set `a-set`.

```clojure
(defn to-binary-digit [c] (if (#{\F \L} c) 0 1))
```

Now we have to do a little string manipulation to convert a sequence of characters into its integer
value. We again use `as->` since we'll be mixing the collection operations `map` and `apply`, which
use thread-last, with a non-collection operation that uses thread-first. The `binary-space-partition`
function maps each digit to its binary form, then concatenates the sequence into one big string of
zeros and ones, and then leverages Java's `Integer.parseInt` method with a radix of `2` to
convert the String as binary into an integer.

```clojure
(defn binary-space-partition [instructions]
  (as-> instructions x
        (map to-binary-digit x)
        (apply str x)
        (Integer/parseInt x 2)))
```
  
Calculating a seat ID is now a fairly trivial task. We split the 10-digit string into the front
and back halfs using `split-seat`, and our `let` statement immediately destructures this vector
into two variables `[r c]` for row and column. Then we multiply the binary form of the row by 8,
and add it to the binary form of the column.

```clojure
(defn seat-id [seat]
  (let [[r c] (split-seat seat)]
    (-> (* (binary-space-partition r) 8)
        (+ (binary-space-partition c)))))
```

Finally, to finish part 1, we need to find the largest seat ID.  A little threading gets us the
answer.

```clojure
(defn part1 [input]
  (->> (str/split-lines input)
       (map seat-id)
       (apply max)))
```

---

## Part 2

For part 2, we need to find which seat ID is not filled. The instructions say that the seat IDs
from the input make a contiguous list, but there's one seat missing. It won't be the lowest or
highest value, so there's one value in the list such that the seat ID one higher does not exist.

Again, there are lots of ways of doing this, but I think this is a good case for the `reduce`
function. We'll make a function `missing-within-collection`, which takes a collection of numeric
values and finds the first missing value. The input to `reduce` will be `(sort coll)`, so we
can read through the list once instead of treating it as a set. Then the function to apply should
look at the previous value calculated and the next value in the list. If `next` is one greater 
than `previous`, then we haven't found it yet, so the reducing function should return `next`.
However, if `next` is not one greater than `previous`, then we can tell the `reduce` function that
we've found our answer and it can stop doing its calculations. The `reduced` function accomplishes
just that. This is `reduce`'s equivalent of applying a `break` within a loop, or an early `return`
within a method.

```clojure
(defn missing-within-collection [coll]
  (reduce (fn [prev v]
            (let [target (inc prev)]
              (if (= v target)
                v                      ; This isn't the answer. Keep reducing.
                (reduced target))))    ; We found it! Short-circuit with the value we didn't find.
          (sort coll)))
```

And then, of course, I always want my `part1` and `part2` functions to be simple if at all possible.
Both functions require reading the input data, converting each value into its seat ID, and then
doing something to it. So we'll pull out most of the logic from the old `part1` into a new function
called `apply-to-seat-ids`, which takes in the input data and the function to apply to collection
of seat IDs.  This should wrap everything up cleanly.

```clojure
(defn apply-to-seat-ids [input f]
  (->> (str/split-lines input)
       (map seat-id)
       f))

(defn part1 [input]
  (apply-to-seat-ids input (partial apply max)))

(defn part2 [input]
  (apply-to-seat-ids input missing-within-collection))
```
