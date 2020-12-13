# Day Thirteen: Shuttle Search

* [Problem statement](https://adventofcode.com/2020/day/13)
* [Solution code](https://github.com/abyala/advent-2020-clojure/blob/master/src/advent_2020_clojure/day13.clj)

---

I'll admit -- I hate these puzzles, because they depend on figuring out clever math tricks, instead of
working out programs. Because Part 2 depends on determining it's the Chinese Remainder Theorem, and
then remembering back to my college days what that means, I just borrowed other people's advice and
plugged the input data into [wolframalpha.com](wolframalpha.com) instead of forcing it to be a
programming problem.

---

# Part 1

For this part of the problem, we are given an input with two lines. The first is the smallest time
index we are willing to accept, and the second is a comma-separated list of bus IDs that run at a
frequency equal to their IDs. Our goal is to find the first bus that shows up after the smallest time
index, and multiply that by the difference in time from the index.

I'll admit, this was so aggravating a problem that I didn't feel compelled to clean it up from the
original code that got me my solution, so this is a sloppy single function.

First I split the input into its two lines. The first line is a single integer, so I parse that into
the `earliest` binding. To get the list of `busses`, I take the second line and split it apart by
commas; then I removed all values of `x`, and parsed the rest as integers using `keep-when`.

The next step was to define a collection `options`, which represents the first time index `t` after
`earliest` when each bus appears; the goal is to transform each bus `b` into a vector of `[id t]`. 
For simplicity, I used a `for` instead of a `map` because it kept the format flatter. For each bus 
`b` in `busses`, I used `(iterate (partial + b) b)` to make a sequence of values, starting from
`b` and incrementing by `b`. I then call `(drop-while (partial > earliest))` to remove all values
smaller than the `earliest` value. Finally, `first` grabs the first such value. That leaves us with
 `options` being of structure `[[id t]]`.
 
Starting with the `options`, I sort them by the second value, the time index, to find the earliest
number after the starting index, as that's the target bus. Then I map the tuple by multiplying the
`id` (first parameter) by the time difference (second parameter minus the `earliest`), and grab
the first value from the collection.

It's ugly, I know.

```clojure
(defn part1 [input]
  (let [lines (str/split-lines input)
        earliest (-> (first lines) Integer/parseInt)
        busses (->> (str/split (second lines) #",")
                    (keep #(when (not= % "x") (Integer/parseInt %))))]
    (let [options (for [b busses]
                    (vector b (->> (iterate (partial + b) b)
                                   (drop-while (partial > earliest))
                                   first)))]
      (->> options
           (sort-by second)
           (map (fn [[id start]] (* id (- start earliest))))
           first))))
```

--- 

## Part 2

Again, this is the Chinese Remainder Theorem, which I haven't thought about since the last time it
showed up in AoC.  Instead of figuring it back out again, I read on
[a Reddit post](https://www.reddit.com/r/adventofcode/comments/kc4njx/2020_day_13_solutions/gfnhrk3/)
that if I expressed my input data as a comma separated string of `(t + [offset]) mod [bus-id]`, I can
plug it in to [Wolfram Alpha](https://www.wolframalpha.com/) and be done with it. So I coded up the
String concatenation just so there would be _some_ code.

The algorithm reads the data, splits the second line by commas, and maps each non-`x` value to a
tuple of its index and its value. Then I just mapped each value using `map` and combined them with
`str/join`. Whatever.

```clojure
(defn part2 [input]
  (let [lines (str/split-lines input)
        demands (->> (str/split (second lines) #",")
                     (keep-indexed (fn [idx v] (when (not= v "x") [idx (Integer/parseInt v)])))
                     (into {}))]
    (->> (map (fn [[idx v]] (str "(t + " idx ") mod " v " = 0")) demands)
         (str/join ", "))))
```

---

## Part 2 Actual Code

My friend Todd Ginsberg actually
[solved this program without the Chinese Remainder Theorem by name](https://todd.ginsberg.com/post/advent-of-code/2020/day13/),
and his write-up is fantastic. So I took his implementation and coded it up in Clojure-ish style. Nicely
done, Todd!
