# Day Six: Custom Customs

* [Problem statement](https://adventofcode.com/2020/day/6)
* [Solution code](https://github.com/abyala/advent-2020-clojure/blob/master/src/advent_2020_clojure/day06.clj)

---

## Part 1

This is a problem that got smaller and smaller the more I looked at it. We're talking to groups of people
who are answering their customs declaration questions. To solve this problem, we need to look at each
group of people's answers, count up the number of distinct answers they gave, and then sum up those counts.

The first thing I did was recognize that this is the second AoC problem that asked me to split an input
file based on a completely blank line; [day 4](day04.md) was the first. So I wanted to add helper functions
to the `utils` namespace, such that I could refactor the day 4 code. There is a small difference between
the day 4 code and the day 6 code, in that for day 4 I wanted to keep the newline characters to limit the
changes, while for day 6 I wanted to collect each line as a String within a sequence. So I made two
functions -- `split-blank-line` which is the equivalent of `split-lines` but with double lines, and
`split-blank-line-seq` which split the internal lines too.

90% of this is only necessary because I code on Windows. Yaaaaaay...

```clojure
(defn split-blank-line [input]
  (-> (str/replace input "\r" "")
      (str/split #"\n\n")))

(defn split-blank-line-seq [input]
  (->> (split-blank-line input)
       (map str/split-lines)))
```

With that out of the way, let's look at the problem again. If we call `split-blank-line-seq` on the
input String, we get back a sequence of vectors. From the sample data, it looks like
`(["abc"] ["a" "b" "c"] ["ab" "ac"] ["a" "a" "a" "a"] ["b"])`.  We need to look at each element of the sequence and
count the number of distinct letters. When we hear "distinct," we should think "set."  So the easiest option for 
each element is to turn each word into a set, then union the sets together using `apply`. That will leave us with a
sequence of sets of characters. Then we count up the number of elements in each set, and add them all together.

```clojure
(defn part1 [input]
  (->> (utils/split-blank-line-seq input)
       (map (partial map set))
       (map (partial apply set/union))
       (map (partial count))
       (apply +)))
```

---

## Part 2

For this part, we need to look at those same groups of folks, find the number of answers that _all_ of them provided,
and then add up the numbers across all groups. How convenient -- we're already working with sets of sets for each 
group. All we need now is to use `intersection` instead of `union` to get to the right answer.

```clojure
(defn part2 [input]
  (->> (utils/split-blank-line-seq input)
       (map (partial map set))
       (map (partial apply set/intersection))
       (map (partial count))
       (apply +)))
```

Well now it should be obvious what the last step is -- find the point of commonality, and make the `part1` and
`part2` functions trivial. And it should be easy to see what needs to be done -- abstract away the operation
to perform on each set of sets. We'll make the driving function `sum-across-groups` take in the input string and
the function to perform, and bada bing, bada boom, we're done.

```clojure
(defn sum-across-groups [input f]
  (->> (utils/split-blank-line-seq input)
       (map (partial map set))
       (map (partial apply f))
       (map count)
       (apply +)))

(defn part1 [input]
  (sum-across-groups input set/union))

(defn part2 [input]
  (sum-across-groups input set/intersection))
```