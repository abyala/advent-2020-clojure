# Day Twenty-Two: Crab Combat

* [Problem statement](https://adventofcode.com/2020/day/22)
* [Solution code](https://github.com/abyala/advent-2020-clojure/blob/master/src/advent_2020_clojure/day22.clj)

---

We found a crab that's actually a card shark! Today's problem was a fairly straightforward one of a two-player
card game called Combat, which is predictably simialr to the game [War](https://bicyclecards.com/how-to-play/war/).
Both parts 1 and 2 involve playing the card game, looking at the winning hand, and applying a calculation to
determine the winner's final score. The cleanup was pretty straightforward, so once again I'll solve both
problems at once.

The difference we'll see between parts 1 and 2 is that part2 allows determining the winner of a hand by
recursively starting a new game with subsets of each player's hands. In part 1, we can skip past that logic
entirely when picking the winner for a hand, but for part 2 we have to add it in.

There is one other implementation detail that's introduced in part 2 - we must identify infinite loops, wherein
the players will have played the identical hand already, in which case player 1 is always the winner. I
implement that with a simple map as a cache, and while I could have easily avoided using it during part 1,
its presence doesn't affect the program's correctness or performance, so I left it in.

### Parts 1 and 2

As always, let's start parsing. `parse-board` reuses our `split-blank-line` function to create a sequence of
two strings, one each for players 1 and 2. `parse-player` splits the input by each line, throws away the
header line that identifies the player, and then maps each value as an integer.

```clojure
(defn parse-player [s]
  (->> (str/split-lines s)
       rest
       (map #(Integer/parseInt %))))

(defn parse-board [input]
  (->> (utils/split-blank-line input)
       (map parse-player)))
```

I put most of the logic into the `play-game` function, although I threw in a few tiny helper functions
to make the structure of `play-game` easier to see.  Still, I'll break the function down piece by piece,
and introduce the helper functions when we get to them.

First off, the function takes in the starting board, expressed as a 2-element sequence of decks for players
1 and 2. We also pass in the `recursive?` flag to express the rules.  The overall function uses `loop-recur`
over the current board and the set of boards already seen. If either deck is empty, the game is over, and
we return a construct of `{:winner :player1-or-2 :deck deck}`. If we've looped such that we've seen the board
already, just define the winner is `player`; note that we can't define what the winning hand was since there's
a loop, so we trust that only sub-games get stuck in infinite loops since the top-level game needs a score.
Then if neither of those cases are true, then figure out who won the round, have that player take the other
player's top card, and follow the loop.

All that's left to figure out is who's the winner of the round, and for that we use a simple `cond`.
If we need to recurse into a sub-game, then do so and extract the winner. (Details coming soon.)
Otherwise, the player with the highest card in the front wins.

I created two helper functions called `create-subgame?` and `subdecks` to assist with the subgame logic.
`create-subgame?` is a boolean function that says whether the winner is determined through recursion.
It requires `recursive?` being true, meaning that we're in part 2, and that the top of each deck is no
larger than the number of other elements in the deck. If that's the case, we need to use `subdecks`
to map each deck to its subdeck; that is, for each deck, remove the top card `n`, and then keep the
next `n` cards in the deck. Once we know we need to create a subgame and we've created the subdecks,
feed them in to a new `play-game` function and pull out the winner.

```clojure
(defn create-subgame? [board recursive?]
  (and recursive?
       (every? #(<= (first %) (count (rest %)))
               board)))

(defn subdecks [board]
  (map #(take (first %) (rest %)) board))

(defn play-game [starting-board recursive?]
  (loop [[deck1 deck2 :as board] starting-board, seen #{}]
    (cond
      (empty? deck1) {:winner :player2 :deck deck2}
      (empty? deck2) {:winner :player1 :deck deck1}
      (seen board) {:winner :player1}                       ; Assume this doesn't happen at the top-level!
      :else (let [[a b] (map first board)
                  winner (cond
                           (create-subgame? board recursive?) (-> (subdecks board)
                                                                  (play-game recursive?)
                                                                  :winner)
                           (> a b) :player1
                           :else :player2)]
              (recur (case winner
                       :player1 [(concat (rest deck1) (list a b)) (rest deck2)]
                       :player2 [(rest deck1) (concat (rest deck2) (list b a))])
                     (conj seen board))))))
```

Once we've played the game and we have a result, of format `{:winner :player1-or-2 :deck deck}`, we need
to score the winning hand using a `score` function. We multiply each card in the deck by the
reversed 1-index; in a deck of 10 cards, we multiple the first card by 10, the second by 9, etc.
Normally I'd reach for my trusty `map-indexed` function, but there's a cleaner way to look at this.
We can start with a decending range of integers, starting with the cardinality of the deck. Then
we can use `(map * deck <the-range>)` to multiply each index in the deck by the value from the range.
Finally, we add the values back up.

```clojure
(defn score [deck]
  (->> (range (count deck) 0 -1)
       (map * deck)
       (apply +)))
```

The `solve` function is trivial now. We parse the input, play the game, extract the deck out of the
result (we don't care who won), and calculate the score.

```clojure
(defn solve [input recursive?]
  (let [board (parse-board input)]
    (->> (play-game board recursive?)
         :deck
         score)))
```

And now my favorite part -- we create the `part1` and `part2` functions, which call the `solve`
function and declare that part1 does not allow recursion while part2 does.

```clojure
(defn part1 [input] (solve input false))
(defn part2 [input] (solve input true))
```