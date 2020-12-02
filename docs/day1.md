# Day One: Report Repair

* [Problem statement](https://adventofcode.com/2020/day/1)
* [Solution code](https://github.com/abyala/advent-2020-clojure/blob/master/src/advent_2020_clojure/day01.clj)

---

## Part 1

This problem requires reading a list of integers, finding the two that add up
to the value 2020, and then returning the product of those numbers. Pretty 
straightforward problem.

My initial solution (foreshadowing!!) was to calculate this as a single function.
As a convention for AoC, I tend to name the solution to each part `part1` and
`part2`, so it's easy to know what to look at. Clojure namespace files require
compilation in order, so if function `foo` depends on function `bar` and
they are in the same file, `bar` must appear first. This is quite a change
from my Java or Kotlin code, where I like to put the most business-relevant,
low-granulairty methods at the top and then the implementations below.

The first thing I want to do is parse the input. In this case, I'm expecting a
single string of the integers, separated by new-lines. For my test data, I
just copy-pasted the data from the website, and for the puzzle data, I stored the
data in a file and use the `slurp` function to read it all in as a single string.

To parse the data, I use the wonderful `thread-last` macro `->>`, which takes
in a list of expressions, and supplies the result of each expression as the final
value of the next expression. So if I wanted to take a numeric value `x`, 
square it, triple the result, and then add 1000, this is much easier to read with
pipelining/threadng. Being able to read left-to-right, or top-to-bottom instead
of inside-out makes Lisp much easier to work with:

```
; Without threading
(+ 1000 (* 3 (* x x)))

; or
(+ 1000
   (* 3
      (* x x)))


; With threading
(->> (* x x)
     (* 3)
     (+ 1000)))
```

So to parse the data, I use `str/split-lines` to break the string into a sequence
of strings, separated by the newline character. Then I use the `map` function
to apply the `Integer/parseInt` function to each value in the sequence, resulting
in a sequence of integers. Note that this is _the_ `Integer/parseInt` static Java
method, so you can see the interop is pretty slick. I take the results and bind
the result to the `expenses` symbol, which is lexically scoped. This is Clojure's
equivalent of making a local variable within a method.

```clojure
(let [expenses (->> (str/split-lines input)
                    (map #(Integer/parseInt %)))]
     OTHER STUFF HERE)
```

Next, I want to find all possible pairs of non-equal values, such that they add
up to 2020. Once found, return them as a vector, or a two-element tuple. For this,
we use Clojure's `for` function, which like the `let` macro, takes in a vector
of bindings and an expression. Here I bind both `x` and `y` to the integer sequence
created above, so this is pretty close to nested loops. The `:when` keyword
allows filtering of the `for` bindings, so in this case I use it to restrict the
pairs to those where x is smaller than y (to avoid duplicates) and where they 
add up to 2020. The evaluated value is just the vector `[x y]`.

```clojure
(for [x expenses
      y expenses
      :when (and (< x y)
                 (= 2020 (+ x y)))]
     [x y])
```

We're almost done.  The `for` function returns a sequence of values, and in this
case I know we only expect one value, so I wrap the results in the `first` function.
That returns a single `[x y]` vector of integers, so all I need to do is multiply
the values with each other. The `apply` function takes in 2 or more arguments,
where the first is the function to apply, and the second is the container of values.
So my function is just `*` for multiplication, and the expression is everything else
I just computed.  Put all together, the function looks like this:
 
```clojure
(defn part1 [input]
    (let [expenses (->> (str/split-lines input)
                        (map #(Integer/parseInt %)))]
      (apply * (first (for [x expenses
                            y expenses
                            :when (and (< x y)
                                       (= 2020 (+ x y)))]
                        [x y])))))
```                        

But wait -- that last section looks all scary and nested. Can't we pipeline it?
Sure we can!

```clojure
(defn part1 [input]
  (let [expenses (->> (str/split-lines input)
                      (map #(Integer/parseInt %)))]
    (->> (for [x expenses
               y expenses
               :when (and (< x y)
                          (= 2020 (+ x y)))]
           [x y])
         first
         (apply *))))
```

## Part 2

This is essentially the same algorithm as part 1, except that we need three
numbers to add up to 2020, instead of two. The easiest way to solve this is with
a copy-paste job, adding in the third binding `z` go to along with `x` and `y`.

```clojure
(defn part2 [input]
  (let [expenses (->> (str/split-lines input)
                      (map #(Integer/parseInt %)))]
    (->> (for [x expenses
               y expenses
               z expenses
               :when (and (< x y z)
                          (= 2020 (+ x y z)))]
           [x y z])
         first
         (apply *))))
```

## Rewrite

What I like to do with AoC problems, when feasible, is to refactor part 1 such
that I get as much reuse as possible between parts 1 and 2. In this case, we can
see that the only real difference is the desired length of the vectors of ints
that add up to 2020 -- two for part 1, and three for part 2. Also, I want to be
a good little functional programmer and pretend that the logic of adding up to
2020, or multiplying the result, could potentially have business meaning. So
let's do some decomposition.

The first step is to create a function called `permutations` that takes in a
desired sequence length and a sequence of data, and provide all permutations of
data. So for `(permutations 2 [3 4])` we should get back `((3 3) (3 4) (4 3) (4 4))`.
In theory, if part 3 of this problem asked for the 15 input values that add up to
2020, we shouldn't have to make a structural change to the design.

For this to work, we use the `iterate` function, which often can take the place of
a `reduce` function. It takes in a function to apply and some initialization 
data, and it returns an infinite sequence of applying the function to the 
output of the previous iteration. Let's start with the initial data first -
we use `(map list data)` which takes each value in the `data` sequence and
turns it into a single element list. So `(map list [1 2 3])` should return
`((1) (2) (3))`. This is a working base case; if I wanted all single-element
tuples in a list, I would expect a list of single-element lists. Plus,
`(map list data)` sounds like I'm confused about data types, but here `map` is
the mapping function and `list` is a function to create a list. No confusion!

Then the `iterate` function needs to flatmap each input data value onto each of
these lists. The Clojure equivalent of flatmap is `mapcat`, since we map a
function to the input data, and then concatenate the values back into a list.
In this case, the function uses the `for` function only once, since we want to add
a single value to each incoming list, and we use the `cons` function to add an
element to the front of another list. Note that since we don't care about ordering,
let's favor the Clojure list instead of the vector, which means that the mapping
function will add the new data to the head of the list.

The last piece is to take the `nth` value of the infinite sequence. Calling
`(nth f 0)` would provide the input data, in this case a list of length 1, so
we actually want to call `(nth f (dec target-length))`. 

```clojure
(defn permutations
  "Creates a list of lists, containing all permutations of the incoming data, each with
  the intended length."
  [length data]
  (nth (iterate
         (fn [d] (mapcat #(for [x data] (cons x %))
                         d))
         (map list data))
       (dec length)))
```

Next, we'll make three tiny functions that explain the target functionality
without getting bogged down in implementation. `all-increasing?` will make sure
that all elements in a list are strictly increasing, so that we can keep values
`(10 2010)` and throw out `(2010 10)`. `adds-to-2020?` makes sure the values...
add up to 2020. And `product-of-all` multiplies all values together.

What I like about these functions is the use of `apply`. Remember that `apply`
applies a function to all following values. So where in Java you might need 
to say `if ((x < y) && (y < z))`, in Clojure you can say `(if (< x y z))`. 
Again, if we needed to look at a tuple of 15 values, the function doesn't change.

```clojure
(defn all-increasing? [v] (apply < v))
(defn adds-to-2020?   [v] (= 2020 (apply + v)))
(defn product-of-all  [v] (apply * v))
 ```

Then we get to the key objective -- a nice `solve` function that we can share
between parts 1 and 2. Let's put the pieces together.
Using a thread-last pipeline, we split the input String by line, map each String
value to an Integer, and calculate all permutations of those values given a target
length. The `keep` function says to apply a mapping function and throw away the
nulls, like Kotlin's `mapNotNull`. So here we say when we have a matching tuple,
such that the values are all increasing and add to 2020, return the product.
Finally, returning the first (and only?) value calculated.

```clojure
(defn solve [length input]
  (->> (str/split-lines input)
       (map #(Integer/parseInt %))
       (permutations length)
       (keep #(when (and (all-increasing? %)
                         (adds-to-2020? %))
                (product-of-all %)))
       first))
```

We'll know we have a good solution if the `part1` and `part2` functions look
pretty.  Let's see if that's the case:

```clojure
(defn part1 [input] (solve 2 input))
(defn part2 [input] (solve 3 input))
``` 

I think they won a freaking beauty contest.  Day 1 complete with Clojure!