# Day Eighteen: Operation Order

* [Problem statement](https://adventofcode.com/2020/day/18)
* [Solution code](https://github.com/abyala/advent-2020-clojure/blob/master/src/advent_2020_clojure/day18.clj)

---

I'm going to declare right now that I had a ton of fun with this problem. As is becoming the case, it's
a little hard to talk about Part 1 without also looking at Part 2, since I tend to refactor the code so
much to be reusable between both parts. And since this is my blog post, I make the rules!

The problem states that we are given several lines of text, each representing an arithmetic equation we need
to solve. The operations are `+`, `*`, and parentheses, but the order of operations is all wacky. While
parentheses always take priority, in part one we apply addition or multiplication based strictly on the
left-to-right ordering, and in part two we prioritize addition over multiplication.  My Dear Aunt Sally feels
an excuse is quite in order!

## Planning ahead

Let's start with the end in mind. We know that parts 1 and 2 differ only in how we prioritize which operation
to perform at a time, so let's figure that out. I know I'm going to handle the parentheses especially, so I
think at some point I'm going to have an expression without parentheses, and I'll make a pass through the
data to decide whether or not to apply any given operation. So that means I need to look at each mathematical
line as a sequence of tokens, and I need to decide whether to take action on any single token.

Let's parse the data. I want to take a String like `1 + (2 * (3 * 4) + 5)` and see it as a simple vector of
values. The parentheses are always next to a number, but everything else is space delimited. For the numeric
strings, let's turn them into `BigInteger`s right away, since we'll get overflow if we use `Long`s. The regex
is a little ugly, but it's not bad. If I put in artifical spaces to get `\d+ | \( | \) | \*`, you can see
it's essentially one big `or`, and `re-seq` returns a sequence of tokens that match the regex.  The `mapv`
only attempts to parse values that aren't symbols; in theory, I could have done a try-catch parse for each
token instead.

I won't spend any time going over [Clojure-Java interop](https://clojure.org/reference/java_interop), but
suffice it to say that `(BigInteger. "123")`, with that extra period, calls the constructor of `BigInteger`
with the parameter `"123"`.

```clojure
(defn parse-mathematical-string [line]
  (->> (re-seq #"\d+|\(|\)|\+|\*" line)
       (mapv #(if (#{"+" "*" "(" ")"} %) % (BigInteger. %)))))
```

Now let's zip to the very end. I'm going to have a `solve` function that takes in my data file and the
ordered operations for non-parenthetical vectors.  It's going to parse each line, run some calculation
with those operations, and then add the results together. Since we're dealing with `BigIntegers`, we
can't use `(apply + values)` and instead have to `reduce` the result using the Java method `.add`.

```clojure
(defn solve [input operations]
  (->> (str/split-lines input)
       (map parse-mathematical-string)
       (map #(calculate % operations))
       (reduce #(.add %1 %2))))

; Helper functions for order-of-operations
(def only-add (partial = "+"))
(def only-mul (partial = "*"))
(def add-or-mul (partial #{"+" "*"}))

(defn part1 [input] (solve input [add-or-mul]))
(defn part2 [input] (solve input [only-add only-mul]))
```

Now that we have an idea of the overall structure, we can fill in the details.

## Calculation functions

Now that I'm getting more comfortable with Clojure, I'm trying to focus more on business-looking
functions. The `calculate` function should take in the vector of tokens and the sequence of
operations, and return the `BigInteger` result. When looking at a list of tokens, we should apply
a simple priority. If there's only one value, return it.  If there are parentheses, handle them.
Otherwise, do "ordered-arithmetic" with the operation filters that were passed in.

```clojure
(defn calculate [tokens operations]
  (or (unpack-simple-expression tokens)
      (apply-parentheses tokens operations)
      (ordered-arithmatic tokens operations)))
```

The `unpack-simple-expression` function is, well, simple.  If there's only one token, return it.
Remember that `when` will return `nil` if its predicate is false, and the `or` in `calculate`
will return the first non-`nil` value.

```clojure
(defn unpack-simple-expression [tokens]
  (when (= 1 (count tokens)) (first tokens)))
```

Next I want to get to `apply-parentheses`, but first we need to helper functions. We are going
to need Java's `indexOf` and `lastIndexOf` functions, but they return `-1` if the value is not found,
and magic constants are evil. So I'll start with an `index-of-or-nil` to make this easier to read
and use in Clojure. Second, I need a function that takes in a vector of tokens, removes several
of them, and replaces them with a new value, so `replace-subvec` will handle that for us.

```clojure
(defn index-of-or-nil [coll v]
  (let [idx (.indexOf coll v)]
    (when (>= idx 0) idx)))

(defn replace-subvec [v low high new-value]
  (apply conj (subvec v 0 low) new-value (subvec v high)))
```

Ok, let's handle `apply-parentheses`. The easiest way to handle the nesting is to find the
_first_ closed paren, and the _last_ open paren before it, because that will represent the 
range of an expression without additional parentheses. `when-let` will only process if we
find an index for `close-paren`, and if it's there, we can assume we'll find one for
`open-paren`. Then we extract the subvector between these two indices and call `calculate`
on it.  Armed with that simplification, we call `replace-subvec` to create our new vector
of tokens, and we call back to `calculate` with the new simplified vector.

One thing to note is that I've set up a mutual dependency between the functions `calculate`
and `apply-parentheses`. My initial one-function-to-rule-them-all solution didn't have this,
but I'm making little functions. Clojure requires function definitions to appear in order,
so we need to use `declare` to create a definition for `calculate` that `apply-parentheses`
can hook into, before we define it later in the file. It's a little ugly, but at least it
brings to light something that generally we don't want, so maybe that's a hidden benefit
of this limitation! 

```clojure
; Forward declaration, needed for mutual dependent functions.
(declare calculate)

(defn apply-parentheses [tokens operations]
  (when-let [close-paren (index-of-or-nil tokens ")")]
    (let [open-paren (.lastIndexOf (subvec tokens 0 close-paren) "(")
          new-value (-> (subvec tokens (inc open-paren) close-paren)
                        (calculate operations))]
      (-> (replace-subvec tokens open-paren (inc close-paren) new-value)
          (calculate operations)))))
```

At this point, we've handled parentheses and identity, so it's time to handle addition and
subtraction. Let's make a little function called `simple-math` that takes in a vector and
the index of the operation, and it returns a reduced vector having applied the operation
to the values on either side. Nothing fancy here.

```clojure
(defn simple-math [tokens idx]
  (let [[op tok-a tok-b] (map #(tokens (+ idx %)) [0 -1 1])
        new-val (case op
                  "*" (.multiply tok-a tok-b)
                  "+" (.add tok-a tok-b))]
    (replace-subvec tokens (dec idx) (+ idx 2) new-val)))
```

Then the last piece to handle is the so-called "ordered arithmetic." Remember that the
operations from part 1 will be `[add-or-mul]` while part 2 will be `[only-add only-mul]`,
so we've got a vector of unary predicates. I'll implement this as a recursive function,
since we need to look at all of the operations, and all of the tokens. Each time through,
we'll short-circuit if this is a simple expression with only one value. If not, see if we
can find the index of a token that passes the current predicate. If so, call `simple-math`
and recursively call this function with the new value. Otherwise, drop the current operation
and recursively call back in with the next one. Note that we assume the expressions are all
valid, so we don't have to worry about running out of operations.

```clojure
(defn ordered-arithmetic [tokens [op & other-ops :as operations]]
  (or (unpack-simple-expression tokens)
      (when-let [idx (->> tokens
                          (keep-indexed (fn [idx tok] (when (op tok) idx)))
                          first)]
        (-> (simple-math tokens idx)
            (ordered-arithmetic operations)))
      (ordered-arithmetic tokens other-ops)))
```

So yeah, I had a ton of fun with this problem!
