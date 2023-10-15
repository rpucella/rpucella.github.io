<script>
  document.title = 'Homework 3 - FOCS FA23'
</script>

# Homework 3

## Due Date: Sunday Oct 15, 2023 (23h59)

- This homework is to be done individually. You may discuss problems
with fellow students, but all submitted work must be entirely your
own, and should not be from any other course, present, past, or
future. If you use a solution from another source you must cite it
&mdash; this includes when that source is someone else helping
you.

- **Please do not post your solutions on a public website or a public repository like GitHub.**

- All programming is to be done in Haskell using GHC v9. 

- Code your answers by modifying the file [`homework3.hs`](homework3.hs) provided. Add your **name**, your **email address**, and any **remarks** that you
wish to make to the instructor to the block comment at the head of the file.

- **Please do not change the types in the signature of the
function stubs I provide**. Doing so will make it
impossible to load your code into the testing infrastructure,
and make me unhappy.

- Feel free to define as many helper functions as you need.


## Electronic Submission Instructions

- Start a _fresh_  `ghci` shell.

- Load your homework code via `:load` *path-to-file*`/homework3.hs` to make sure that there are no errors when I will load your code.

- If there are any error, do not submit. I can't test what I can't `:load`.

- When you're ready to submit, log into our <a href="https://chat.rpucella.net">class chat</a>
and click <b>Submit File</b> in the profile menu (your initials in the upper right corner).


* * *

## Question 1: Set Functions

A set is a collection of elements in which repetition and order is irrelevant; mathematically, the
sets {1, 1, 2, 3} and {2, 3, 3, 1} are the same set.

We use lists as a representation for sets. Our set operations will use that set representation. For simplicity, we're going to focus on sets of integers. 

Let's impose the invariant that **a list representing a set cannot have repeated elements**.  It is
therefore your responsibility in the functions for this question to make sure that when your function produces sets, you have no repeated elements. When expecting sets as arguments, it is okay
for you to assume that the lists given as inputs don't have repeated elements.

Order is irrelevant. It's okay if the order of elements in your results differ from my
sample outputs below, as long as there are no repetitions. My tester will handle it.

Most of these functions are recursive. I will generally not bother pointing out when I'm asking for
a recursive function.


### (A)

Code a function **`set`** of type **`[Int]-> [Int]`** which takes
a list `xs` and returns a set (a list without repeated elements)
with the same elements as `xs`.

    > set []
    []

    > set [1]
    [1]

    > set [1, 2, 3]
    [1,2,3]

    > set [1, 2, 3, 2, 1]
    [3,2,1]

    > set [1, 2, 3, 2, 1, 2, 3, 4, 3, 2, 1]
    [4,3,2,1]

Note that library function `elem` can be used to check if an element is a member of a list, as in `elem 10 [1, 2, 10, 20]`.


### (B)

Code a function **`setSub`** of type **`[Int] -> [Int] -> Bool`** which takes two sets `s` and `t`
and returns True exactly when `s` is a subset of `t` (that is, every element of `s` is an element of
`t`).

    > setSub [] []
    True

    > setSub [] [1]
    True

    > setSub [1] [1]
    True

    > setSub [1] [1, 2]
    True

    > setSub [1] [1, 2, 3]
    True

    > setSub [1, 2] [1, 2, 3]
    True

    > setSub [2, 1] [1, 2, 3]
    True

    > setSub [3, 2, 1] [1, 2, 3]
    True

    > setSub [4, 3, 2, 1] [1, 2, 3]
    False

    > setSub [4, 3, 2] [1, 2, 3]
    False

    > setSub [4] [1, 2, 3]
    False

    > setSub [4] []
    False


### (C)

Code a function **`setEqual`** of type **`[Int] -> [Int] -> Bool`** which takes two sets `s` and `t`
and returns True exactly when `s` and `t` are equal as sets (that is, they have the same elements).

    > setEqual [] []
    True

    > setEqual [] [1]
    False

    > setEqual [] [1, 2]
    False

    > setEqual [1, 2] []
    False

    > setEqual [1, 2] [1]
    False

    > setEqual [1, 2] [1, 2]
    True

    > setEqual [1, 2] [2, 1]
    True

    > setEqual [1, 2] [2, 1, 3]
    False

    > setEqual [3, 1, 2] [2, 1, 3]
    True
    

### (D)

Code a function **`setUnion`** of type **`[Int] -> [Int] -> [Int]`** which takes two sets `s` and
`t` and returns a set made up of all the elements in `s` and `t`.

    > setUnion [] []
    []

    > setUnion [] [1, 2]
    [1,2]

    > setUnion [1, 2] []
    [1,2]

    > setUnion [1, 2] [3, 4]
    [1,2,3,4]

    > setUnion [1, 2] [2, 3]
    [1,2,3]

    > setUnion [1, 2] [1, 2, 3, 4]
    [1,2,3,4]

    > setUnion [1, 2, 3, 4] [1, 2]
    [3,4,1,2]
    

### (E)

Code a function **`setInter`** of type **`[Int] -> [Int] -> [Int]`** which takes two sets `s` and
`t` and returns a new set made up of all the elements in common to `s` and `t`.

    > setInter [] []
    []

    > setInter [1] []
    []

    > setInter [1, 2] []
    []

    > setInter [] [1, 2]
    []

    > setInter [1] [1, 2]
    [1]

    > setInter [1, 2] [1, 2]
    [1,2]

    > setInter [1, 2, 3] [1, 2]
    [1,2]

    > setInter [1, 2, 3] [2]
    [2]

    > setInter [1, 2, 3] [2, 3]
    [2,3]

    > setInter [2, 3] [1, 2, 3]
    [2,3]


***


## Question 2: Mapping and Filtering


Recall that _higher-order functions_ are functions that take other functions as arguments. Two important higher-order functions are `map` and `filter`, both built-in in Haskell.

Function `map :: (a -> b) -> [a] -> [b]` takes a function `f` and a list `xs` and returns the list obtained by applying `f` successively to every element of `xs`. 

Function `filter :: (a -> Bool) -> [a] -> [a]` takes a predicate `p` (a function returning a
Boolean) and a list `xs` and returns the list of all the elements in `xs` for which the predicate
returns True.

For each function in this question, full points will be awarded if the function and its helpers use
`map` and `filter` instead of recursion.

To illustrate, here is the `squares` function defined using explicit recursion:

    squares :: [Int] -> [Int]
    squares xs = 
      case xs of
        [] -> []
        x:xs' -> (x * x) : squares xs'

Here is the `squares` function defined without using explicit recursion:

    squares :: [Int] -> [Int]
    squares xs = 
      map (\x -> x * x) xs

If you can't get a function to work without explicit recursion, please go ahead and implement a
version with explicit recursion for partial marks.


### (A)

Code a function **`mapFunctions`** of type **`[(a -> b)] -> a -> [b]`** where `mapFunctions fs x` returns the list obtained by applying every function in `fs` to `x`, in order.

    > mapFunctions [] 0
    []

    > mapFunctions [\x -> x+1] 0
    [1]

    > mapFunctions [\x -> x+1, \x -> x + 2] 0
    [1,2]

    > mapFunctions [\x -> x+1, \x -> x + 2, \x -> x + 99] 0
    [1,2,99]

    > mapFunctions [\x -> x+1, \x -> x + 2, \x -> x + 99] 100
    [101,102,199]


### (B)

Code a function **`keepPalindromes`** of type **`String -> [String] -> [String]`** where `keepPalindromes s xs` returns the strings in `xs` with the property that appending them to `s` yield a _palindrome_, that is, a string equal to its own reverse, such as `abbba`.

    > keepPalindromes "" []
    []

    > keepPalindromes "" ["a", "ab", "ba", "abbb", "bbba", "abbba"]
    ["a","abbba"]

    > keepPalindromes "a" ["a", "ab", "ba", "abbb", "bbba", "abbba"]
    ["a","ba","bbba"]

    > keepPalindromes "b" ["a", "ab", "ba", "abbb", "bbba", "abbba"]
    ["ab"]

    > keepPalindromes "ab" ["a", "ab", "ba", "abbb", "bbba", "abbba"]
    ["a","ba","bbba"]



### (C)

Code a function **`incrementPositive`** of type **`[Int] -> [Int]`** where `incrementPositive xs` returns the list of only the positive integers in `xs`, each incremented by 1. Number 0 is _not_ considered positive.

    > incrementPositive []
    []

    > incrementPositive [1, 2, 3]
    [2,3,4]

    > incrementPositive [-1, -2, -3, 1, 2, 3]
    [2,3,4]

    > incrementPositive [-1, -2, -3]
    []

    > incrementPositive [-1, 1, -2, 2, -3, 3]
    [2,3,4]


### (D)

Code a function **`distribute`** of type **`a -> [b] -> [(a, b)]`** where `distribute x ys` returns the list of all pairs of `x` paired with an element of `ys`.

    > distribute 9 []
    []

    > distribute 9 ["a"]
    [(9,"a")]

    > distribute 9 ["a", "b"]
    [(9,"a"),(9,"b")]

    > distribute 9 ["a", "b", "c"]
    [(9,"a"),(9,"b"),(9,"c")]

    > distribute 9 ["a", "b", "c", "c"]
    [(9,"a"),(9,"b"),(9,"c"),(9,"c")]

    > distribute "hello" [1, 2, 3, 4, 5]
    [("hello",1),("hello",2),("hello",3),("hello",4),("hello",5)]


### (E)

Code a function **`consAll`** of type **`a -> [[a]] -> [[a]]`** where `consAll x xss` returns the list obtained by adding `x` as the first element of every list in `xss`.


    > consAll 1 []
    []

    > consAll 1 [[]]
    [[1]]

    > consAll 1 [[66]]
    [[1,66]]

    > consAll 1 [[66], [99]]
    [[1,66],[1,99]]

    > consAll 1 [[66], [99], []]
    [[1,66],[1,99],[1]]

    > consAll 1 [[66], [99], [], [66, 99]]
    [[1,66],[1,99],[1],[1,66,99]]

    > consAll "+" [["hello", "world"], ["goodbye"]]
    [["+","hello","world"],["+","goodbye"]]


***

## Question 3: Simulating Nondeterministic Finite Automata

In Homework 2, you wrote a simulator for _deterministic_ finite state machines (DFAs). It was pretty
simple: you start in the start state of the finite state machine, then you go to the next state
according to the symbol you get from the input string and the transition relation of the
machine. You're only ever in one state, and determine whether to accept based on the state you end
up with when you exhaust all symbols from the input string.

In this question, we write a simulator for _nondeterministic_ finite state machines (NFAs). Now,
there are a few ways of doing such a simulation. One is to convert the NFA into a DFA using
the subset construction I described in class, and simulate the resulting DFA using the simulator for
Homework 2. 

Here, we'll focus on simulating NFAs directly. We are going to use the "pebbles" technique I
described in class to follow all possible paths at the same time: we first put a pebble on the start
state, then repeatedly move all the pebbles we have put on the various states to new states
according to transitions labeled appropriately in the transition relation, creating new pebbles when
any given pebble can move to multiple states from a given state. We accept the input string when we have seen every
symbol from the input string and there is at least one pebble on a final state. Indeed, if there's a pebble
on some final stte, there is a path from the start state to a finalstate, namely the path
the pebble took to get to that final state.

As usual, we'll build this in pieces. 

The type of an NFA is just the type of finite state machines we saw last time. We simply remove the
restriction that there is a single translation out of any given state labeled with any given symbol. (This restriction was never captured in the type anyway.)

    data FA = FA {
      states :: [Int],
      alphabet :: [Char],
      delta :: [(Int, Char, Int)],
      start :: Int,
      final :: [Int]
    }


### (A)

Code a function **`hasFinal`** of type **`[Int] -> [Int] -> Bool`** where `hasFinal final qs` returns True
    when one of the states in `qs` is a final state in `final`.

    > hasFinal (final faStartaEndc) [0, 1]
    False

    > hasFinal (final faStartaEndc) [1, 2]
    True
    
    
    > hasFinal (final faLastabc) []
    False

    > hasFinal (final faLastabc) [0]
    False

    > hasFinal (final faLastabc) [0, 1]
    False

    > hasFinal (final faLastabc) [0, 1, 2]
    False

    > hasFinal (final faLastabc) [0, 1, 2, 3]
    True

    > hasFinal (final faLastabc) [3]
    True


### (B)

Code a function **`followSymbol`** of type **`[(Int, Char, Int)] -> [Int] -> Char -> [Int]`** where `followSymbol delta qs sym` returns a list of states that you can reach from _any_ of the states in `qs` by following symbol `sym` given a transition relation `delta`.

Intuitively, think of `qs` as the states where you have a pebble, and think of the result of the
function as the set of states where you have pebbles after moving pebbles according to symbol `sym`
and the transition relation.

Note that the order or repetition of states in the result is irrelevant. (I will compare the results
as sets.)

    > followSymbol (delta faStartaEndc) [] 'c'
    []

    > followSymbol (delta faStartaEndc) [0] 'c'
    [99]

    > followSymbol (delta faStartaEndc) [0, 1] 'c'
    [99,2]

    > followSymbol (delta faStartaEndc) [0, 1, 2] 'c'
    [99,2,2]

    > followSymbol (delta faStartaEndc) [0, 1, 2, 99] 'c'
    [99,2,2,99]

    > followSymbol (delta faStartaEndc) [1, 2, 99] 'c'
    [2,2,99]


    > followSymbol (delta faLastabc) [] 'a'
    []

    > followSymbol (delta faLastabc) [0] 'a'
    [0,1]

    > followSymbol (delta faLastabc) [0, 1, 2, 3] 'a'
    [0,1]

    > followSymbol (delta faLastabc) [0, 1, 2, 3] 'b'
    [0,2]

    > followSymbol (delta faLastabc) [0, 1, 2, 3] 'c'
    [0,3]
    

**Hint:** This can be challenging to do directly. I suggest you write a helper function `followSymbolFromState` of type `[(Int, Char, Int]) -> Int -> Char -> [Int]` where `followSymbolFromState delta q sym` returns a list of states that you can reach by following symbol `sym` from the single state `q` given the transition relation `delta`.


### (C)

Code a function **`followString`** of type **`[(Int, Char, Int)] -> [Int] -> [Char] -> [Int]`** where `followString delta qs syms` returns a list of states that you can reach from _any_ of the states in `qs` by following the _string_ of symbols in `syms` in order, given a transition relation `delta`. 

Intuitively, think of `qs` as the state where you have a pebble, think of the result as the set of states where you have pebbles after moving pebbles according to the sequence of symbols in `syms` and the transition relation.

Note that the order or repetition of states in the result is irrelevant. (I will compare the results
as sets.)


    > followString (delta faStartaEndc) [] "a"
    []

    > followString (delta faStartaEndc) [0] "a"
    [1]

    > followString (delta faStartaEndc) [0] "ab"
    [1]

    > followString (delta faStartaEndc) [0] "abc"
    [2]

    > followString (delta faStartaEndc) [0] "ac"
    [2]

    > followString (delta faStartaEndc) [0] "c"
    [99]

    > followString (delta faStartaEndc) [0, 1] "ac"
    [2,2]

    > followString (delta faStartaEndc) [0, 1] "ca"
    [99,1]


    > followString (delta faLastabc) [] ""
    []

    > followString (delta faLastabc) [] "a"
    []

    > followString (delta faLastabc) [] "ab"
    []

    > followString (delta faLastabc) [0] ""
    [0]

    > followString (delta faLastabc) [0] "a"
    [0,1]

    > followString (delta faLastabc) [0] "ab"
    [0,2]

    > followString (delta faLastabc) [0] "abc"
    [0,3]

    > followString (delta faLastabc) [0] "abbc"
    [0]

    > followString (delta faLastabc) [0] "aabc"
    [0,3]

    > followString (delta faLastabc) [1, 2] "bc"
    [3]

    > followString (delta faLastabc) [1, 2] "cb"
    []


### (D) 

Code a function **`accept`** of type **`FA -> String -> Bool`** where `accept m s` returns True when string `s` is accepted by NFA `m`, and false otherwise.

As in Homework 2, you can use the function `lang` I give you to get all the strings (up to a certain length) that a given NFA accepts, once you have implemented `accept`.

    > accept faStartaEndc "abbbc"
    True

    > accept faStartaEndc "abbbcb"
    False

    > lang faStartaEndc 6
      ac
      aac
      abc
      acc
      aaac
      abac
      acac
      aabc
      abbc
      acbc
      aacc
      abcc
      accc
      aaaac
      abaac
      acaac
      aabac
      abbac
      acbac
      aacac
      abcac
      accac
      aaabc
      ababc
      acabc
      aabbc
      abbbc
      acbbc
      aacbc
      abcbc
      accbc
      aaacc
      abacc
      acacc
      aabcc
      abbcc
      acbcc
      aaccc
      abccc
      acccc
      aaaaac
      abaaac
      acaaac
      aabaac
      abbaac
      acbaac
      aacaac
      abcaac
      accaac
      aaabac
      ababac
      acabac
      aabbac
      abbbac
      acbbac
      aacbac
      abcbac
      accbac
      aaacac
      abacac
      acacac
      aabcac
      abbcac
      acbcac
      aaccac
      abccac
      acccac
      aaaabc
      abaabc
      acaabc
      aababc
      abbabc
      acbabc
      aacabc
      abcabc
      accabc
      aaabbc
      ababbc
      acabbc
      aabbbc
      abbbbc
      acbbbc
      aacbbc
      abcbbc
      accbbc
      aaacbc
      abacbc
      acacbc
      aabcbc
      abbcbc
      acbcbc
      aaccbc
      abccbc
      acccbc
      aaaacc
      abaacc
      acaacc
      aabacc
      abbacc
      acbacc
      aacacc
      abcacc
      accacc
      aaabcc
      ababcc
      acabcc
      aabbcc
      abbbcc
      acbbcc
      aacbcc
      abcbcc
      accbcc
      aaaccc
      abaccc
      acaccc
      aabccc
      abbccc
      acbccc
      aacccc
      abcccc
      accccc


    > accept faLastabc "aaaabc"
    True

    > accept faLastabc "aaaabcb"
    False

    > lang faLastabc 6
      abc
      aabc
      babc
      cabc
      aaabc
      baabc
      caabc
      ababc
      bbabc
      cbabc
      acabc
      bcabc
      ccabc
      aaaabc
      baaabc
      caaabc
      abaabc
      bbaabc
      cbaabc
      acaabc
      bcaabc
      ccaabc
      aababc
      bababc
      cababc
      abbabc
      bbbabc
      cbbabc
      acbabc
      bcbabc
      ccbabc
      aacabc
      bacabc
      cacabc
      abcabc
      bbcabc
      cbcabc
      accabc
      bccabc
      cccabc
  
**Hint:** use `hasFinal` and `followString`.

