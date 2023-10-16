<script>
  document.title = 'Homework 1 - FOCS FA23'
</script>

# Homework 1

## Due Date: Thursday, Sept 21, 2023 (23h59)

- This homework is to be done individually. You may discuss problems
with fellow students, but all submitted work must be entirely your
own, and should not be from any other course, present, past, or
future. If you use a solution from another source you must cite it
&mdash; this includes when that source is someone else helping
you.

- **Please do not post your solutions on a public website or a public repository like GitHub.**

- All programming is to be done in Haskell using GHC v9. 

- Code your answers by modifying the file [`homework1.hs`](homework1.hs) provided. Add your **name**, your **email address**, and any **remarks** that you
wish to make to the instructor to the block comment at the head of the file.

- **Please do not change the types in the signature of the
function stubs I provide**. Doing so will make it
impossible to load your code into the testing infrastructure,
and make me unhappy.

- Feel free to define as many helper functions as you need.


## Electronic Submission Instructions

- Start a _fresh_  `ghci` shell.

- Load your homework code via `:load homework1.hs` to make sure that there are no errors when I will load your code.

- If there are any error, do not submit. I can't test what I can't `:load`.

- When you're ready to submit, log into our <a href="https://chat.rpucella.net">class chat</a>
and click <b>Submit File</b> in the profile menu (your initials in the upper right corner).

* * *

<span style="color: blue;">
UPDATE 9/14/23: added sample outputs and fixed stub for `longerThan` in `homework1.hs`.
</span>

* * *


## Question 1: Haskell Exercises

These functions are not recursive, and are defined over floating point
numbers (type `Float`).


### (A)

Code a function

    clamp :: Float -> Float -> Float -> Float

where `clamp a b v` returns `v` if `v` is between `a` and `b` (inclusively), and otherwise returns 
the smallest of `a` and `b` if `v` is smaller, or the largest of `a`
and `b` if `v` is larger. In most uses, `a` would be the smaller value
and `b` would be the larger value, but that's not a requirement.

<details open>
<summary>Sample outputs:</summary>

    ghci> clamp 1 2 1.5
    1.5

    ghci> clamp 1 2 1
    1.0

    ghci> clamp 1 2 2
    2.0

    ghci> clamp 1 2 0.5
    1.0

    ghci> clamp 1 2 1.5
    1.5

    ghci> clamp 2 1 1.5
    1.5
    
</details>

Note that you have library functions `min` and `max` available.


### (B)

Code a function

    interpolate :: Float -> Float -> Float -> Float

where `interpolate a b v` maps the interval `[0, 1]` to the interval
`[a, b]`, and returns the value corresponding to value `v` with
respect `[0, 1]` to the corresponding value with respect to `[a, b]`. 
Thus, `0.5` is in the middle of `[0, 1]`, so it should map to 
`15` with respect to `[10, 20]` or to `[20, 10]`.

Note that `v` could be outside `[0, 1]`. So 1.5 with respect to `[0, 1]` 
is halfway past the end point, so corresponds to 25 with respect
to `[10, 20]`, and corresponds to 30 with respect to `[0, 20]`. It
also corresponds to 5 with respect to `[20, 10]`.

This is obviously all about ratios.

    ghci> interpolate 10 20 0
    10.0

    ghci> interpolate 10 20 1
    20.0

    ghci> interpolate 10 20 0.5
    15.0

    ghci> interpolate 10 20 0.25
    12.5

    ghci> interpolate 10 20 1.5
    25.0

    ghci> interpolate 20 10 1.5
    5.0


* * *


## Question 2: List Functions

All of the functions below can be written as recursive
functions over lists. They all fit the general pattern of:

    f xs ... = 
       case xs of
         [] -> e1
         x:xs' -> e2

where `e2` can use `x`, `xs'`, and the
recursive function itself. For some functions, you may need to do some
additional matching in `e2`.

You can always define helper functions if it makes your life easier.


### (A)

Code a function

    pairDouble :: [Int] -> [(Int, Int)]

which takes a list `xs` of integers and returns a new list in which
every element of `xs` becomes a pair of that element and its double.

    ghci> pairDouble []
    []

    ghci> pairDouble [1]
    [(1,2)]

    ghci> pairDouble [1, 2]
    [(1,2),(2,4)]

    ghci> pairDouble [1, 2, 3]
    [(1,2),(2,4),(3,6)]

    ghci> pairDouble [1, 2, 3, -4]
    [(1,2),(2,4),(3,6),(-4,-8)]

    ghci> pairDouble [1, 2, 3, -4, -5]
    [(1,2),(2,4),(3,6),(-4,-8),(-5,-10)]

    ghci> pairDouble [1, 2, 3, -4, -5, 5]
    [(1,2),(2,4),(3,6),(-4,-8),(-5,-10),(5,10)]


### (B)

Code a function

    cap :: Int -> [Int] -> [Int]

which takes a threshold integer `m` and a list `xs` of integers and
returns a new list in which every element of `xs` is capped at `m`:
it becomes `m` if it is larger than `m`, and is unchanged otherwise.

    ghci> cap 5 []
    []

    ghci> cap 5 [1, 2, 3]
    [1,2,3]

    ghci> cap 5 [6, 7, 8]
    [5,5,5]

    ghci> cap 5 [1, 2, 3, 4, 5, 6, 7, 8]
    [1,2,3,4,5,5,5,5]

    ghci> cap 5 [1, 3, 5, 7, 9, 7, 5, 3, 1]
    [1,3,5,5,5,5,5,3,1]

    

### (C)

Code a function

     prefix :: String -> [String] -> [String]
     
which takes a string `s` and a list of strings `xs` and returns the
list obtained by prefixing every string in `xs` with `s`.

    ghci> prefix "" []
    []

    ghci> prefix "" ["Alice", "Bob"]
    ["Alice","Bob"]

    ghci> prefix "Hello " []
    []

    ghci> prefix "Hello " ["Alice"]
    ["Hello Alice"]

    ghci> prefix "Hello " ["Alice", "Bob"]
    ["Hello Alice","Hello Bob"]

    ghci> prefix "Hello " ["Alice", "Bob", "Bob"]
    ["Hello Alice","Hello Bob","Hello Bob"]

    

### (D)

Code a function

    longerThan : Int -> [String] -> [String]

which takes an integer `n` and a list `xs` of strings and returns a
new list containing only the strings in `xs` that have length _at least_
`n`, in the same order as they appear in `xs`.

    ghci> longerThan 5 []
    []

    ghci> longerThan 5 ["hello"]
    ["hello"]

    ghci> longerThan 5 ["hello", "world"]
    ["hello","world"]

    ghci> longerThan 5 ["hello", "world", "full", "of", "wonders"]
    ["hello","world","wonders"]

    ghci> longerThan 5 ["feed", "well"]
    []

    ghci> longerThan 0 ["", "a", "aa", "aaa"]
    ["","a","aa","aaa"]

    
Note that you can use function `length` to get the length of a string.


### (E)

Code a function

    within :: Int -> Int -> [Int] -> [Int]

which takes two integers `a` and `b` and a list `xs` of integers and
returns a new list with only the integers in `xs` that are between
`a` and `b` (inclusively).

    ghci> within 10 20 []
    []

    ghci> within 10 20 [5]
    []

    ghci> within 10 20 [15]
    [15]

    ghci> within 10 20 [25]
    []

    ghci> within 10 20 [5, 10 , 15, 20, 25]
    [10,15,20]

    ghci> within 10 15 [5, 10 , 15, 20, 25]
    [10,15]

    ghci> within 15 25 [5, 10 , 15, 20, 25]
    [15,20,25]

    ghci> within 25 15 [5, 10 , 15, 20, 25]
    [15,20,25]

    
Make this work whether `a` or `b` is the smallest number.


### (F)

Code a function

    find :: Int -> [(Int, a)] -> a -> a

where `find v xs d` takes an integer `v`, a list of pairs `xs`, and a
default value `d`, and tries the find the first pair in `xs` where the
first component of the pair is `v`. When it finds such a pair, it
returns the second component of that pair. If there is no pair in `xs`
whose first component is `v`, then default value `d` is returned
instead.

    ghci> find 5 [] "nothing"
    "nothing"

    ghci> find 5 [(1, "one")] "nothing"
    "nothing"

    ghci> find 5 [(1, "one"), (5, "five")] "nothing"
    "five"

    ghci> find 5 [(1, "one"), (5, "five"), (10, "ten")] "nothing"
    "five"

    ghci> find 5 [(1, "one"), (5, "five"), (10, "ten"), (5, "another-five")] "nothing"
    "five"

    ghci> find 1 [(1, "one"), (5, "five"), (10, "ten")] "nothing"
    "one"

    ghci> find 5 [(1, 10001), (5, 10005), (10, 10010)] 0
    10005

    

### (G) (challenging)

Code a function

    split :: Int -> [Int] -> ([Int], [Int])

which takes a threshold integer `m` and a list `xs` of integers and
returns a pair of lists, where the first list in the pair contains all
the elements in `xs` that are at most `m` and the second list in the
pair contains all the elements in `xs` that are greater than `m`.

The order of the elements in each list should be consistent with the
order of the elements in the original list.

    ghci> split 5 []
    ([],[])

    ghci> split 5 [0]
    ([0],[])

    ghci> split 5 [0, 5]
    ([0,5],[])

    ghci> split 5 [0, 5, 10]
    ([0,5],[10])

    ghci> split 5 [0, 5, 10, 15]
    ([0,5],[10,15])

    ghci> split 5 [0, 5, 10, 15, 2]
    ([0,5,2],[10,15])

    ghci> split 5 [0, 5, 10, 15, 2, 7]
    ([0,5,2],[10,15,7])

    ghci> split 5 [1, 2, 3, 4]
    ([1,2,3,4],[])

    ghci> split 5 [6, 7, 8, 9]
    ([],[6,7,8,9])

    
    

* * *


## Question 3: Vectors

We can implement vectors as lists of floating point
numbers. Operations on vectors such as addition or scalar
multiplication can then be implemented as recursive functions over
lists.

One difference from Question 2 is that you will sometimes have to
recurse over two lists at the same time, which means matching over two
lists. One way to do so is to first match over the first list, and
then match over the second list in each case of the first list (if
applicable). For example:

    f xs ys =
      case xs of
        [] -> e1
        x:xs' -> case ys of
                    [] -> e2
                    y:ys' -> e3

An alternate, slightly more advanced, approach relies on generalized pattern matching, 
matching on both lists at the same time by creating a pair of lists. For example, something like:

    f xs ys =
      case (xs, ys) of
        ([], []) -> e4
        (x:xs', []) -> e5
        ([], y:ys') -> e6
        (x:xs', y:ys') -> e7

Please only use this last approach if it makes sense to you. There is
no effective difference between the two.

These functions only make sense if their vector arguments have the
same length. You can assume that your code will be given vectors of
the same length. I'm only going to test your code with vectors of the
same length. Still, you should probably make sure that your code does
return a value in case vectors do not have the same length (I don't
care what that value is), or error out with `error "message"`.


### (A)

Code a function

    vScale :: Float -> [Float] -> [Float]

which takes a float `a` and a vector `v` and computes the scalar
multiplication `a v` (multiplying every component of `v` by `a`).

    ghci> vScale 10 []
    []

    ghci> vScale 0 [1, 2, 3]
    [0.0,0.0,0.0]

    ghci> vScale 1 [1, 2, 3]
    [1.0,2.0,3.0]

    ghci> vScale 2 [1, 2, 3]
    [2.0,4.0,6.0]

    ghci> vScale 2.5 [1, 2, 3]
    [2.5,5.0,7.5]

    ghci> vScale (-2.5) [1, 2, 3]
    [-2.5,-5.0,-7.5]

    

### (B)

Code a function

    vAdd :: [Float] -> [Float] -> [Float]

which takes two vectors `v` and `w` and returns the vector sum `v + w`.

    ghci> vAdd [] []
    []

    ghci> vAdd [10] [20]
    [30.0]

    ghci> vAdd [10, 20, 30] [20, 40, 60]
    [30.0,60.0,90.0]

    

### (C)

Code a function

    vLength :: [Float] -> Float

which takes a vector `v` and returns the length (or [Euclidean
norm](https://en.wikipedia.org/wiki/Norm_(mathematics)#Euclidean_norm))
of vector `v`, defined to be the square root of the sum of the squares
of the components of `v`.

The square root can be computed using library function `sqrt`.

I suggest you write a helper function to compute the sum of the
squares of the components of `v` first, and use that in `vLength`.

    ghci> vLength []
    0.0

    ghci> vLength [10]
    10.0

    ghci> vLength [10, 20]
    22.36068

    ghci> vLength [10, 20, 30]
    37.416573

    ghci> vLength [10, 20, 30, 40]
    54.772255

    ghci> vLength [10, 10]
    14.142136

    

### (D)

Code a function

    vInner :: [Float] -> [Float] -> Float
    
which takes two vectors `v` and `w` and returns the [inner
product](https://en.wikipedia.org/wiki/Dot_product) `v` &middot; `w`.

    ghci> vInner [] []
    0.0

    ghci> vInner [10] [20]
    200.0

    ghci> vInner [1, 2, 3] [4, 5, 6]
    32.0

    ghci> vInner [1, 2, 3] [8, 10, 12]
    64.0

    

***

## Question 4: More general recursion

There are more general forms of recursions that will occur much less
often in this course. Sometimes, those forms of recursions can be
reduced to a recursion over lists (first problem below), other times
not (second problem below).

### (A)

Code a function

    repeatStr :: String -> Int -> [String]

where `repeatStr s n` takes a string `s` and a non-negative integer `n`
and returns the result of concatenating `s` with itself `n`
times. When `n` is zero or negative, the result should be the empty
string.

    ghci> repeatStr "" 0
    ""

    ghci> repeatStr "" 1
    ""

    ghci> repeatStr "" 10
    ""

    ghci> repeatStr "a" 0
    ""

    ghci> repeatStr "a" 1
    "a"

    ghci> repeatStr "a" 10
    "aaaaaaaaaa"

    ghci> repeatStr "hello" 0
    ""

    ghci> repeatStr "hello" 1
    "hello"

    ghci> repeatStr "hello" 10
    "hellohellohellohellohellohellohellohellohellohello"

    
Note that the operator for string concatenation in Haskell is `++`.

It is possible to write this as a recursion over the natural number argument `n`, using the fact
that `repeatStr s n` is ` s ++ (repeatStr s (n - 1))` when `n` is greater than 0.

But another way to implemnent the function which does not require any
new concepts is to use the Haskell notation `[1..n]` to create the
list `[1, 2, ..., n]` and pass it to a helper function that takes a
string `s` and a list such as `[1, 2, ..., n]` and uses recursion over
lists to construct `s ++ s ++ s ++ ... ++ s`.



### (B)

The [Collatz
conjecture](https://en.wikipedia.org/wiki/Collatz_conjecture) says
that if you start with any positive _n_ (greater than 0) and iterate
the transformation **when _n_ is even return _n_ / 2 and when _n_ is
odd return 3 _n_ + 1**, then this process eventually yields 1 after a
finite number of steps.

Code a function

    collatzSeq :: Int -> [Int]

where `collatzSeq n` takes a positive integer `n` and returns
the sequence of integers obtained by the process described above,
starting with the number `n` itself and ending with the final 1.
 
    ghci> collatzSeq 1
    [1]

    ghci> collatzSeq 2
    [2,1]

    ghci> collatzSeq 3
    [3,10,5,16,8,4,2,1]

    ghci> collatzSeq 4
    [4,2,1]

    ghci> collatzSeq 12
    [12,6,3,10,5,16,8,4,2,1]

    ghci> collatzSeq 19
    [19,58,29,88,44,22,11,34,17,52,26,13,40,20,10,5,16,8,4,2,1]

    ghci> collatzSeq 27
    [27,82,41,124,62,31,94,47,142,71,214,107,322,161,484,242,121,364,182,
    91,274,137,412,206,103,310,155,466,233,700,350,175,526,263,790,395,
    1186,593,1780,890,445,1336,668,334,167,502,251,754,377,1132,566,283,
    850,425,1276,638,319,958,479,1438,719,2158,1079,3238,1619,4858,2429,
    7288,3644,1822,911,2734,1367,4102,2051,6154,3077,9232,4616,2308,1154,
    577,1732,866,433,1300,650,325,976,488,244,122,61,184,92,46,23,70,35,
    106,53,160,80,40,20,10,5,16,8,4,2,1]


