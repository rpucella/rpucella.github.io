<script>
  document.title = 'Homework 4 - FOCS FA23'
</script>

# Homework 4

## Due Date: Sunday Oct 22, 2023 (23h59)

- This homework is to be done individually. You may discuss problems
with fellow students, but all submitted work must be entirely your
own, and should not be from any other course, present, past, or
future. If you use a solution from another source you must cite it
&mdash; this includes when that source is someone else helping
you.

- **Please do not post your solutions on a public website or a public repository like GitHub.**

- All programming is to be done in Haskell using GHC v9. 

- Code your answers by modifying the file [`homework4.hs`](homework4.hs) provided. Add your **name**, your **email address**, and any **remarks** that you
wish to make to the instructor to the block comment at the head of the file.

- **Please do not change the types in the signature of the
function stubs I provide**. Doing so will make it
impossible to load your code into the testing infrastructure,
and make me unhappy.

- Feel free to define as many helper functions as you need.


## Electronic Submission Instructions

- Start a _fresh_  `ghci` shell.

- Load your homework code via `:load` *path-to-file*`/homework4.hs` to make sure that there are no errors when I will load your code.

- If there are any error, do not submit. I can't test what I can't `:load`.

- When you're ready to submit, log into our <a href="https://chat.rpucella.net">class chat</a>
and click <b>Submit File</b> in the profile menu (your initials in the upper right corner).

***

**Update** 10/19:

- Fixed parameter names of the delta functions in the provided TMs to reflect the fact that the first argument is the state and the second argument is the symbol
- Fixed 3(D) to account for default value

* * *

Here is a type for deterministic Turing machines with integer-named states:

    data TM = TM { states :: [Int],
                   inputAlpha :: [Char],
                   tapeAlpha :: [Char],
                   delta :: Int -> Char -> (Int, Char, Int),   -- 1 = Right, -1 = Left
                   start :: Int,
                   accept :: Int,
                   reject :: Int
                 }

The definition follows that of Turing machines we've seen in class. The transition function is given
by a _Haskell function_ taking as input the current state _p_ and the symbol _a_ in the cell under the pointer and returning a triple _(q, b, dir)_ indicating that the machine should transition
into a state _q_, writing _b_ in the cell under the pointer, and then moving the pointer in direction
_dir_ (left when it is -1, and right when it is 1). We use `_` as the symbol for blank space, which
should always be part of the tape alphabet.

(As we saw, functions in Haskell are values like any other &mdash;
they can be put into lists and records, they can be passed as arguments to other functions, they can
be returned from other functions. Here, we store a function in the field of a record.)

As a simple example of a Turing machine, here is the code implementing a Turing machine accepting
the language &lcub;a<sup>n</sup>b<sup>n</sup> | n &ge; 0&rcub;:

    anbn :: TM
    anbn = 
      let d state sym =
             case (state, sym) of
               (1, 'a') -> (2, 'X', 1)
               (1, '_') -> (777, '_', 1)
               (2, 'a') -> (2, 'a', 1)
               (2, 'Y') -> (2, 'Y', 1)
               (2, 'b') -> (4, 'Y', -1)
               (4, 'Y') -> (4, 'Y', -1)
               (4, 'a') -> (7, 'a', -1)
               (4, 'X') -> (6, 'X', 1)
               (6, 'Y') -> (6, 'Y', 1)
               (6, '_') -> (777, '_', 1)
               (7, 'a') -> (7, 'a', -1)
               (7, 'X') -> (1, 'X', 1)
               (_, c) -> (666, c, 1) in
        TM { states = [1, 2, 4, 6, 7, 777, 666],
             inputAlpha = ['a', 'b'],
             tapeAlpha = ['a', 'b', 'X', 'Y', '_'],
             start = 1,
             accept = 777,
             reject = 666,
             delta = d }

Note that I define the function `d` locally before placing it inside the record. Note also that the final clause of the `case` definining that function use a _wildcard_ to match any state and any symbol:

               (_, c) -> (666,c , 1)

which roughly reads: if when `(sym, state)` is of the form `(_, c)` for some `c` as the second component of the pair &mdash; we don't care about the first component &mdash; then return `(666, c, 1)`.

I also provide you with another Turing machine `anbncn` accepting &lcub;a<sup>n</sup>b<sup>n</sup>c<sup>n</sup> | n &ge; 0&rcub;.

Recall from lecture that a configuration represents a snapshot of the Turing machine during
execution: the current state of the machine, the content of the tape, and the position of the
tape pointer. Type `Config` is the type of a configuration, defined as follows:

    data Config = Config { state :: Int,
                           tape :: [Char],
                           position :: Int
                         }

Field `state` is the current state, field `tape` is the content of the tape, and `position` is the
position of the tape pointer, with 1 as the left-most position on the tape. The tape content in a
configuration can always be extended to the right with blank symbols `_` while denoting the same
configuration. So in all of my sample outputs, you should ignore trailing blank symbols on the tape,
and feel free to handle trailing blanks differently. (My tester will disregard trailing blanks.)

In Question 1, you will complete a simulator for Turing machines that lets you simulate the execution of a Turing machine on a given input string. The simulator for a Turing machine is a function `run` that I give you:

    run :: TM -> String -> IO String
    run m input =
      let loop c =
            if isAcceptConfig m c
            then return "ACCEPT"
            else if isRejectConfig m c
            then return "REJECT"
            else do c' <- return (step m c)
                    printConfig m c'
                    loop c'
          init = startConfig m input in
        do validateStates m
           printConfig m init
           loop init

(You don't have to understand this function, it uses concepts from Haskell we haven't seen to manage the IO.)

Once you complete Question 1, you will be able to simulate, for instance, machine `anbn` on some input and determine if it accepts the string (returns `"ACCEPT"`) or rejects the string (returns `"REJECT"`):

    ghci> run anbn "aaabbb"
    ----------------------------------------
    Validating Turing machine:
     state 1
     state 777
     state 666
     state 2
     state 4
     state 6
     state 7
    ----------------------------------------
    1    [a] a  a  b  b  b 
    2     X [a] a  b  b  b 
    2     X  a [a] b  b  b 
    2     X  a  a [b] b  b 
    4     X  a [a] Y  b  b 
    7     X [a] a  Y  b  b 
    7    [X] a  a  Y  b  b 
    1     X [a] a  Y  b  b 
    2     X  X [a] Y  b  b 
    2     X  X  a [Y] b  b 
    2     X  X  a  Y [b] b 
    4     X  X  a [Y] Y  b 
    4     X  X [a] Y  Y  b 
    7     X [X] a  Y  Y  b 
    1     X  X [a] Y  Y  b 
    2     X  X  X [Y] Y  b 
    2     X  X  X  Y [Y] b 
    2     X  X  X  Y  Y [b]
    4     X  X  X  Y [Y] Y 
    4     X  X  X [Y] Y  Y 
    4     X  X [X] Y  Y  Y 
    6     X  X  X [Y] Y  Y 
    6     X  X  X  Y [Y] Y 
    6     X  X  X  Y  Y [Y]
    6     X  X  X  Y  Y  Y [_]
    777   X  X  X  Y  Y  Y  _ [_]
    "ACCEPT"


    ghci> run anbn "aaaabbb"
    ----------------------------------------
    Validating Turing machine:
     state 1
     state 777
     state 666
     state 2
     state 4
     state 6
     state 7
    ----------------------------------------
    1    [a] a  a  a  b  b  b 
    2     X [a] a  a  b  b  b 
    2     X  a [a] a  b  b  b 
    2     X  a  a [a] b  b  b 
    2     X  a  a  a [b] b  b 
    4     X  a  a [a] Y  b  b 
    7     X  a [a] a  Y  b  b 
    7     X [a] a  a  Y  b  b 
    7    [X] a  a  a  Y  b  b 
    1     X [a] a  a  Y  b  b 
    2     X  X [a] a  Y  b  b 
    2     X  X  a [a] Y  b  b 
    2     X  X  a  a [Y] b  b 
    2     X  X  a  a  Y [b] b 
    4     X  X  a  a [Y] Y  b 
    4     X  X  a [a] Y  Y  b 
    7     X  X [a] a  Y  Y  b 
    7     X [X] a  a  Y  Y  b 
    1     X  X [a] a  Y  Y  b 
    2     X  X  X [a] Y  Y  b 
    2     X  X  X  a [Y] Y  b 
    2     X  X  X  a  Y [Y] b 
    2     X  X  X  a  Y  Y [b]
    4     X  X  X  a  Y [Y] Y 
    4     X  X  X  a [Y] Y  Y 
    4     X  X  X [a] Y  Y  Y 
    7     X  X [X] a  Y  Y  Y 
    1     X  X  X [a] Y  Y  Y 
    2     X  X  X  X [Y] Y  Y 
    2     X  X  X  X  Y [Y] Y 
    2     X  X  X  X  Y  Y [Y]
    2     X  X  X  X  Y  Y  Y [_]
    666   X  X  X  X  Y  Y  Y  _ [_]
    "REJECT"


* * *


## Question 1: Simulating Turing Machines


### (A)

Code a function **`startConfig`** with type **`TM -> String -> Config`** where `startConfig m w` returns the starting configuration for Turing machine `m` with `w` as input string.

    > startConfig anbn ""
    Config {state = 1, tape = "", position = 1}
    
    > startConfig anbn "aabb"
    Config {state = 1, tape = "aabb", position = 1}
    
    > startConfig anbn "aabbaa"
    Config {state = 1, tape = "aabbaa", position = 1}


### (B)

Code functions **`isAcceptConfig`** and **`isRejectConfig`** each of type **`TM -> Config -> Bool`** where `isAcceptConfig m c` returns True if and only if `c` is an accepting configuration for Turing machine `m`, and `isRejectConfig m c` returns True if and only if `c` is a rejecting configuration for Turing machine `m`.

    > isAcceptConfig anbn (Config {state = 1, tape = "aabb", position = 1})
    False
    
    > isAcceptConfig anbn (Config {state = 2, tape = "aabb", position = 1})
    False
    
    > isAcceptConfig anbn (Config {state = 666, tape = "aabb", position = 1})
    False
    
    > isAcceptConfig anbn (Config {state = 777, tape = "aabb", position = 1})
    True
    
    > isRejectConfig anbn (Config {state = 1, tape = "aabb", position = 1})
    False
    
    > isRejectConfig anbn (Config {state = 2, tape = "aabb", position = 1})
    False
    
    > isRejectConfig anbn (Config {state = 666, tape = "aabb", position = 1})
    True
    
    > isRejectConfig anbn (Config {state = 777, tape = "aabb", position = 1})
    False


### (C)

Code a function **`getNth`** with type **`[a] -> Int -> a`** where `getNth xs n`
returns the element at in the `n`<sup>th</sup> position in list `xs`. The
first element of the list is taken to be at position 1.

If `n` is not a valid position in the list, the function should return an error.

    > getNth ['a', 'b', 'c', 'd'] 1
    'a'
    
    > getNth ['a', 'b', 'c', 'd'] 2
    'b'
    
    > getNth ['a', 'b', 'c', 'd'] 3
    'c'
    
    > getNth ['a', 'b', 'c', 'd'] 4
    'd'
    
    > getNth ['a', 'b', 'c', 'd'] 5
    *** Exception: Out of bounds
    
    > getNth ['a', 'b', 'c', 'd'] 0
    *** Exception: Out of bounds


### (D)

Code a function **`replaceNth`** with type **`[a] -> Int -> a -> [a]`** where `replaceNth xs n x`
returns a new list which looks just like `xs` except the element in `n`<sup>th</sup> position is `x`. The
first element of the list is taken to be at position 1.

If `n` is not a valid position in the list, the function should return an error.

    > replaceNth ['a', 'b', 'c', 'd'] 1 'x'
    "xbcd"
    
    > replaceNth ['a', 'b', 'c', 'd'] 2 'x'
    "axcd"
    
    > replaceNth ['a', 'b', 'c', 'd'] 3 'x'
    "abxd"
    
    > replaceNth ['a', 'b', 'c', 'd'] 4 'x'
    "abcx"
    
    > replaceNth ['a', 'b', 'c', 'd'] 0 'x'
    "abcd*** Exception: Out of bounds
    
    > replaceNth ['a', 'b', 'c', 'd'] 5 'x'
    "abcd*** Exception: Out of bounds


### (E)

Code a function **`step`** with type **`TM -> Config -> Config`** where `step m c` returns the configuration
obtained by taking one step of Turing machine `m` from configuration `c`.

Remember that the tape head cannot move at positions less than 1 (it bottoms out at 1, so moving
left from position 1 is like not moving at all), but it's fine for the tape head to move to the
right past the end of the content of the tape in the configuration &mdash; you just need to add
blanks (symbol `_`).

    -– Running through anbn step by step over input string `aabb`
    
    > step anbn (Config {state = 1, tape = "aabb", position = 1})
    Config {state = 2, tape = "Xabb", position = 2}
    
    > step anbn (Config {state = 2, tape = "Xabb", position = 2})
    Config {state = 2, tape = "Xabb", position = 3}
    
    > step anbn (Config {state = 2, tape = "Xabb", position = 3})
    Config {state = 4, tape = "XaYb", position = 2}
    
    > step anbn (Config {state = 4, tape = "XaYb", position = 2})
    Config {state = 7, tape = "XaYb", position = 1}
    
    > step anbn (Config {state = 7, tape = "XaYb", position = 1})
    Config {state = 1, tape = "XaYb", position = 2}
    
    > step anbn (Config {state = 1, tape = "XaYb", position = 2})
    Config {state = 2, tape = "XXYb", position = 3}
    
    > step anbn (Config {state = 2, tape = "XXYb", position = 3})
    Config {state = 2, tape = "XXYb", position = 4}
    
    > step anbn (Config {state = 2, tape = "XXYb", position = 4})
    Config {state = 4, tape = "XXYY", position = 3}
    
    > step anbn (Config {state = 4, tape = "XXYY", position = 3})
    Config {state = 4, tape = "XXYY", position = 2}
    
    > step anbn (Config {state = 4, tape = "XXYY", position = 2})
    Config {state = 6, tape = "XXYY", position = 3}
    
    > step anbn (Config {state = 6, tape = "XXYY", position = 3})
    Config {state = 6, tape = "XXYY", position = 4}
    
    > step anbn (Config {state = 6, tape = "XXYY", position = 4})
    Config {state = 6, tape = "XXYY", position = 5}
    
    > step anbn (Config {state = 6, tape = "XXYY", position = 5})
    Config {state = 777, tape = "XXYY_", position = 6}
    
    > step anbn (Config {state = 777, tape = "XXYY_", position = 6})
    Config {state = 666, tape = "XXYY__", position = 7}
    

Once you have completed Parts (A)–(E), you should be able to use the `run` function to simulate Turing machines. 


* * *


## Question 2: Constructing Turing Machines

In this question, you will construct Turing machines. You
can test your Turing machines with the code you wrote in Question
1. 

For submission purposes, I ask you to define those Turing machines as constants with a fixed name
given below in each question. They are initialized with a dummy Turing machine that you should
replace with your own definition.



### (A)

Construct a halting Turing machine **`tm_ab3`** that accepts the language
consisting of all strings over the alphabet `{a, b}` of the form
`a`<sup>n</sup>`b`<sup>3n</sup></sup> for any n &ge; 0 (that is, all string with n `a`'s follow by 3n `b`'s).

Sample output not showing printed configurations:

    > run tm_ab3 ""
    "ACCEPTED"
    
    > run tm_ab3 "abbb"
    "ACCEPTED"
    
    > run tm_ab3 "aabbbbbb"
    "ACCEPTED"
    
    > run tm_ab3 "aaabbbbbbbbb"
    "ACCEPTED"
    
    > run tm_ab3 "a"
    "REJECTED"
    
    > run tm_ab3 "abb"
    "REJECTED"
    
    > run tm_ab3 "abbbb"
    "REJECTED"
    
    > run tm_ab3 "aabbb"
    "REJECTED"
    
    > run tm_ab3 "abbba"
    "REJECTED"
    
    > run tm_ab3 "abbbabbb"
    "REJECTED"
    
    > run tm_ab3 "bbba"
    "REJECTED"


**Hint**: modify `anbn`


## (B)

Construct a halting Turing machine **`tm_palindrome`** that accepts the language
consisting of all strings over the alphabet `{a, b}` that are _palindromes_, that is, that read the same forward and backward. 

Note that there are both even-length palindromes such as &epsilon;, `aa`, `abba`, `abbbba`, and odd-length
palindromes, such as `a`, `bab`, `bbbbb`.

Sample output not showing printed configurations:

    > run tm_palindrome ""
    "ACCEPTED"
    
    > run tm_palindrome "a"
    "ACCEPTED"
    
    > run tm_palindrome "aa"
    "ACCEPTED"
    
    > run tm_palindrome "aaa"
    "ACCEPTED"
    
    > run tm_palindrome "aba"
    "ACCEPTED"
    
    > run tm_palindrome "abba"
    "ACCEPTED"
    
    > run tm_palindrome "aabbbbaa"
    "ACCEPTED"
    
    > run tm_palindrome "ababa"
    "ACCEPTED"
    
    > run tm_palindrome "abbbbba"
    "ACCEPTED"
    
    > run tm_palindrome "ab"
    "REJECTED"
    
    > run tm_palindrome "abb"
    "REJECTED"
    
    > run tm_palindrome "aabbb"
    "REJECTED"
    
    > run tm_palindrome "ba"
    "REJECTED"
    
    > run tm_palindrome "bbba"
    "REJECTED"


***

## Question 3: Folding

In Homework 3, we saw two important higher-order functions working on lists, `map` and `filter`. Function `map` lets you transform a list by applying (mapping) a given function over every element of the list, while `filter` lets you transform a list by removing all the elements that do not satisfy a given predicate. (These functions do not actually modify the original list, they create new lists with the results.)

Both `map` and `filter` capture a certain form of recursive functions over lists. For example, `map f xs` basically abstracts away from the following recursive pattern:

    exampleMap:: [a] -> [a]
    exampleMap xs = 
      case xs of 
        [] -> []
        x:xs' -> (<function> x) : exampleMap xs'

and `filter p xs` abstracts away from the following recursive pattern:

    exampleFlt :: [a] -> [a]
    exampleFlt xs = 
      case xs of 
        [] -> []
        x::xs' -> if <predicate> x 
                  then x : exampleFlt xs'
                  else exampleFlt xs'

There are other functions that can abstract from even more general recursive patterns. The most general, which in a precise sense you can think of capturing the _essence of recursive functions over lists_ is called **fold** (and more specifically, fold right — yes, there is a fold left, no, we're probably not going to use it — feel free to look it up though). It also exists in other programming languages under the name `reduce`.

Fold is built into Haskell, but you could define it easily enough if you needed to:

    foldr :: (a -> b -> b) -> b -> [a] -> b
    foldr f init xs = 
      case xs of
        [] -> init
        x:xs' -> f x (foldr f init xs')

It looks somewhat mysterious. But here's how you use it. Suppose you wanted to write a function `sum` that takes a list of integers and returns the sum of the elements in the list. You obviously can't write it with `map` or with `filter` (why?). The recursive definition is easy though:

    sum :: [Int] -> Int
    sum xs =
      case xs of
        [] -> 0
        x:xs' -> x + sum xs'
        
Let's break it down: when the list is empty, we return a specific initial value (here 0), and when the list is not empty, we take both the current element of the list (`x`) and the result of the recursive call (`sum xs'`), and we combine the two to get the final result (using `+`). To be pedantic about it, I could isolate the initial value and the function that combines the current element and the result of the recursive call:

    sum :: [Int] -> Int
    sum xs =
      case xs of
        [] -> let init = 0 in
                init
        x:xs' -> let combine x rec = x + rec in
                   combine x (sum xs')

(Think about the type of `combine` in the middle there: it is `Int -> Int -> Int`.)

What's the relationship with `foldr`? Well, `foldr` is like `sum` except it's taking both the combine function and the initial value as parameters. To see it, take `sum` and pull `combine` and `init` out as parameters:

    sum :: (Int -> Int -> Int) -> Int -> [Int] -> Int
    sum combine init xs = 
      case xs of
        [] -> init
        x:xs' -> combine x (sum combine xs')
        
Renaming `sum` as `foldr` and `combine` as `f` gives us back the original `foldr`, under a more specialized type than it needs to be:

    foldr :: (Int -> Int -> Int) -> Int -> [Int] -> Int
    foldr f init xs = 
      case xs of
        [] -> init
        x:xs' -> f x (foldr f init xs')

That means that since we have `foldr` as a built-in function, we can define `sum` simply as:

    sum xs = foldr (\x -> \rec -> x + rec) 0 xs
    
More generally, any function defined by the pattern:

    exampleFld :: [a] -> b
    exampleFld xs =
      case xs of 
        [] -> <init>
        x:xs' -> <combine> x (exampleFld xs')

can be defined with recursion using:

    exampleFld xs = foldr <combine> <init> xs
    
In this question, I'm going to ask you to write functions using `foldr`. For each function in this
question, full points will be awarded if the function and its helpers use `foldr` instead of
recursion. If you can't get a function to work without explicit recursion, please go ahead and
implement a version with explicit recursion for partial marks.


### (A) 

Code a function **`compose`** with type **`[a -> a] -> (a -> a)`** where `compose fs` return a function that when applied to `x` return `f1 (f2 (f3 (... (fk x)...)))` when `fs` is the list `[f1, f2, ..., fk]`, or simply `x` when `fs` is empty.

    > compose [] 10
    10

    > compose [\x -> x + 1] 10
    11

    > compose [\x -> x + 1, \x -> x * 2] 10
    21

    > compose [\x -> x * 2, \x -> x + 1] 10
    22

    > compose [\x -> x * 2, \x -> x + 1, \x -> x * 3] 10
    62

    > compose [\x -> x * 2, \x -> x + 1, \x -> x * 3] 11
    68
    

### (B)

Code a function **`prefixes`** of type `'a list -> ('a list) list` where `prefixes xs` returns the list of all prefixes of `xs`. For instance, the prefixes of `[x1; x2; x3]` are `[]`, `[x1]`, `[x1; x2]`, and `[x1; x2; x3]`. Note that the empty list is always a prefix.

    > prefixes []
    [[]]

    > prefixes [1]
    [[],[1]]

    > prefixes [1, 2, 3, 4]
    [[],[1],[1,2],[1,2,3],[1,2,3,4]]


**Hint**: Use function `consAll` you wrote for homework 3.


### (C)

Code a function **`suffixes`** of type `'a list -> ('a list) list` where
`suffixes xs` returns the list of all suffixes of `xs`. For instance, the
suffixes of `[x1; x2; x3]` are `[]`, `[x3]`, `[x2; x3]`, and `[x1; x2; x3]`.
Note that the empty list is always a suffix.

    > suffixes []
    [[]]

    > suffixes [1]
    [[1],[]]

    > suffixes [1, 2, 3, 4]
    [[1,2,3,4],[2,3,4],[3,4],[4],[]]
    

### (D)

Code a function **`maxElement`** of type **`[Int] -> Int -> Int`** where `maxElement xs def` returns the largest integer in `xs`, or `def` if the list is empty.

Make sure you can handle negative numbers in the list.

    > maxElement [] 0
    0

    > maxElement [] 3
    3

    > maxElement [1, 2, 3, 4] 0
    4

    > maxElement [1, 2, 3, 4] 99
    4

    > maxElement [-1, -2, -3, -4] 0
    -1

    > maxElement [0, 1, 2, 3, 2, 1, 0, -1, -2, -1, 0] 0
    3

**Hint**: first check if the list is empty before calling `foldr` to compute that maximal element.
