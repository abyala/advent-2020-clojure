# Day Nine: Encoding Error

* [Problem statement](https://adventofcode.com/2020/day/8)
* [Solution code](https://github.com/abyala/advent-2020-clojure/blob/master/src/advent_2020_clojure/day09.clj)

---

Today's problem was an exercise in good old fashioned numeric manipulation. I'll admit I don't love my solution, but 
it gets the job done.

## Part 1

In this part, we are given a list of longs, and are asked to look for the first number before which there are no two
different values that add up to it. The puzzle data requires a look-back range of 25, while the test data looks back
to only the previous 5 values.  I'll skip the `parse-input` function because it's predictable, other than the fact
that we need to use Longs instead of Ints.

The first function I want is `pair-sums-to?`, which will return `true` if a pair within the `nums` argument adds up
to the `target` argument. I've been reading more recently about the `for` function and some of its powers, so even
though this isn't the most succinct solution, I think it's neat enough to try out. Shout out to
[@alekszelark](https://twitter.com/alekszelark) for his day 1 code that taught me about this.  It might be easiest
to start with the code and then explain it.

```clojure
(defn pair-sums-to? [nums target]
  (->> (for [[x & xs] (iterate next nums) :while x
             y xs]
         (+ x y))
       (some (partial = target))))
```

We are going to create pairs of `[x y]` from the sequence `nums`, returning `true` if any pair adds up to `target`.
the function returns `true` if any such pair exists. To start, we use `(iterate next nums)` to return the initial
`nums` sequence, and then every future sequence without the first value. It binds the first value of that sequence to
`x` and the rest to `xs`, and stops when there is no binding for `x`. Essentially, this will let us look at every
value `x` within the collection, and then see all other values _after_ `x` in the collection. We then bind `y` to
`xs`, thus giving us all of the `[x y]` pairs without duplicates. Once we have all of the pairs, we add them up,
and apply `(some (partial = target))` to return `true` if there's a match.

All that's left is a function to find the weakness in the program, or rather, the value that has no matching pair.
Of interest here is the `partition` function, which has 3 forms. The normal one is `(partition n coll)`, which 
breaks a collection into smaller, non-overlapping collections of size `n`. There's `(partition n skip coll)`, which
breaks a collection into smaller collections of size `n`, but the start of each smaller collection is `skip` away
from the start of the previous. Then there's a third version that involves padding the last value, but never mind
that. Because we want to find all values with its preceding look-back range, I used the second form, applying
`(partition (inc size) 1 nums)`. The partition size is `(inc size)` because we need the size of the lookback plus
one more for the target value. And the `skip` size is `1` so we get every possible window.

The rest is pretty straightforward. Split each partition by stripping away the last value, thus isolating the 
lookback from its target, and then use our friend `keep-when` to find the first target that's not valid. Finally,
`part1` parses the data and calls this little function of ours.

 ```clojure
(defn weakness [nums size]
  (->> (partition (inc size) 1 nums)
       (map (partial split-at size))
       (keep (fn [[sub [target]]]
               (when (not (pair-sums-to? sub target))
                 target)))
       first))

(defn part1 [input size]
  (weakness (parse-input input) size))
```

---

## Part 2

For this part, we need to find a contiguous list of numbers (why do the instructions say a contiguous set??) that add
up to the invalid value from part 1. I'm going to create a function called `block-adding-to` that takes in a sequence
of numbers and a target, and checks if there is any list of numbers _starting from the beginning_ that adds up to
the target. I implemented this several ways, but this one was actually really fast and easy to understand.

My solution involves a `reduce` function over all of the numbers in the sequence, and my reduction is a vector of all
elements seen so far, and the sum so far. Then as it reduces, it checks the sum with the next element in the list.
If it hits the target, use the `reduced` short-circuit to return all elements examined, as that's the answer. If the
sum exceeds the target, use `reduced` again to short-circuit, but return `nil` since no additional positive numbers
will bring the sum back down. Else, `cons` the next value onto the list of values seen so far, along with the new sum.

Note: my original solution also used a `reduce` function, but instead of holding onto the entire sequence, I kept only
a collection of the `min`, `max`, and `sum` of the values, since we don't need to keep all of the data. But I favor
solutions that could theoretically be reused and extended and are easy to understand, even if it's a bit more verbose.

```clojure
(defn block-adding-to [nums target]
  (reduce (fn [[r sum] v]
            (let [new-range (cons v r)
                  new-sum (+ sum v)]
              (cond
                (= new-sum target) (reduced new-range)
                (> new-sum target) (reduced nil)
                :else [new-range new-sum])))
          [[] 0]
          nums))
```

With that, it's time to find which sequence contains a the correct block, and here a `loop-recur` is the easiest
solution I found. We'l loop over the sequence of numbers, recurring with `(rest n)` to drop the first value from the
list. Then if the current (sub-) list contains the contiguous block, then add together the min and max values within
the sequence.  Piece of cake!

```clojure
(defn part2 [input size]
  (let [nums (parse-input input)
        target (weakness nums size)]
    (loop [n nums]
      (if-let [r (block-adding-to n target)]
        (+ (apply min r)
           (apply max r))
        (recur (rest n))))))
```