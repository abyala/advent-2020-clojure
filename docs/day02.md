# Day Two: Password Philosophy

* [Problem statement](https://adventofcode.com/2020/day/2)
* [Solution code](https://github.com/abyala/advent-2020-clojure/blob/master/src/advent_2020_clojure/day02.clj)

---

## Part 1

Today's problem was a pretty straightforward one. We are given an input that
represents password rules and password attempts, and we need to return the number
of passwords that meet their rules.

In part one, we learn that the password rule states the number of times a particular
character must appear within the password itself. The format of each line is
`XX-YY C: SSSSS` where `C` is the test character that must appear between `XX` and
`YY` times (inclusively) within the string `SSSSS`.

I tend to write a `parse-line` in most AoC problems; often I'll usually add a
`parse-input` function too. This time, though, `parse-line` was enough. To do the
data extraction, I used `re-matches`, which is a function that takes in a regex
pattern and the test string. A Clojure regex is just a string preceded by a hash
sign, so `#"\d*"` would be a pattern. `re-matches` returns a sequence of the 
entire String and then each group, so that's a great candidate for destructuring.
Since I don't need the entire string, I use an underscore for the first binding.

To finish the `parse-line` function, I parse Integers out of the two values, and
then grab the first character in the third string. Clojure uses Java's `String`
class, but will natively convert it into a list of characters when using a
sequence function. So with a binding strangely being called `letter`, I can just
request `(first letter)` to get the first characger. I could create a map out of
my data, but the problem is simple enough that a vector of `[min max c word]`
seems fine to me.

```clojure
(defn parse-line [line]
  (let [[_ min max letter word] (re-matches #"(\d+)-(\d+) (\w): (\w+)" line)]
    [(Integer/parseInt min) (Integer/parseInt max) (first letter) word]))
```

Next, I need a function to apply to test the rule against the password. We later
learn that this tobogggan test was actually a phoney sled password test, so hence
my function name. Rather than use the `frequencies` function, which will show up
in a later problem I'm sure, I just filtered each character based on whether or
not it matched character `c`. Note again that a String can be fed into the `map`
function since it gets converted into a character array automatically.

Finally, as we saw in problem 1, the Clojure equivalent of
`if ((min <= matches) && (matches <= max))` is the very clean `(<= min matches max)`.

```clojure
(defn sled-password? [[min max c word]]
  (let [matches (->> (filter (partial = c) word)
                     count)]
    (<= min matches max)))
```

Then I need to do the actual logic for my `solve` function, and there's nothing
tricky here. Using a thread-last pipeline, we split the input String by line,
map each String using the `parse-line` function into its vector, filter out the
passwords that pass the rule, and
finally count up the number of matches. For `part1`, I just call this `solve`
function with the input data and the ruleset, in this case `sled-password?`

```clojure
(defn solve [input rule]
  (->> (str/split-lines input)
       (map parse-line)
       (filter rule)
       count))

(defn part1 [input] (solve input sled-password?))
```

---

## Part 2

Well how convenient is this -- we have to do the same thing we did in part 1,
but instead we need to apply different logic to see if the password is correct.
The new rule is that within the password String `SSSSS`, the characters at index
`XX` xor `YY` must be character `C`. It's not complicated, but it takes a tiny
bit more work than part 1.

First of all, Clojure doesn't have a function for `xor` for some reason. So, I
guess I'll have to make it myself. My function will apply a function `f` to a
collection, and return true if the function returns `true` for exactly one value.

```clojure
(defn xor [f coll]
  (= 1 (count (filter f coll))))
```

So to test the password, we just use another function. The input are the two indexes
`min` and `max`, although the directions state that the rules are 1-indexed while
Clojure is 0-indexed, so we'll need to `dec` the values. We make an anonymous
function to test if `get word n` (returns the nth character in the String)
matches the character `c`. Finally, we use the new `xor` function, passing in
the anonymous function and the sequence of indexes.

One quick note: because I put the `xor` function in a separate namespace called
`utils`, I need to provide access to it. I could type in
`advent-2020-clojure.utils/xor`, but that's silly. I could also `require` the
namespace and map it to `utils`, typing in `utils/xor`. But in this case, I
think `xor` is fundamental enough of a function that I'll leverage `:use` to
add a direct reference of the `xor` function into the current namespace. 


```clojure
(ns advent-2020-clojure.day02
  (:require [clojure.string :as str])
  (:use [advent-2020-clojure.utils :only [xor]]))

(defn toboggan-password? [[min max c word]]
  (xor #(= c (get word (dec %)))
       [min max]))
```

And this passes my "is the part2 function pretty" standard, since it's all
reuse, baby!

```clojure
(defn part2 [input] (solve input toboggan-password?))
```