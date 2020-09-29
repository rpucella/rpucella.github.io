# Homework 1

## Due Date: Thursday, September 24, 2020 (23h59)


<span style="color: blue;">**UPDATE 1: Submissions accepted without penalty until Saturday, September 26, 2020 (23h59)**<br>You may also submit multiple times - I will use the latest submissions before the deadline</span>

<span style="color: blue;">**UPDATE 2: You only need to do any 15 problems out the 18 below to get full marks**</span>


- This homework is to be done individually. You may discuss problems
with fellow students, but all submitted work must be entirely your
own, and should not be from any other course, present, past, or
future. If you use a solution from another source you must cite it
&mdash; this includes when that source is someone else helping
you.

- **Please do not post your solutions on a
public website or a public repository like GitHub.**

- All programming is to be done in OCaml v4.

- Code your answers by modifying the file [`homework1.ml`](homework1.ml) provided. Add your **name**, your **email address**, and any **remarks** that you
wish to make to the instructor to the block comment at the head of the file.

- **Please do not change the types in the signature of the
function stubs I provide**. Doing so will make it
impossible to load your code into the testing infrastructure,
and make me unhappy.

- Feel free to define helper functions if you need them.


## Electronic Submission Instructions

- Start a _fresh_  OCaml shell.

- Load your homework code via `#use "homework1.ml";;` to make sure that there are no errors when I will load your code.

- If there are any error, do not submit. I can't test what I can't `#use`.

- When you're ready to submit, send an email with your file
`homework1.ml` as an attachment to `olin.submissions@gmail.com` with
subject _Homework 1 submission_.


* * *

## Question 1: OCaml Exercises

The functions below are all recursive over the integers. Most of these
functions are only defined when some of their arguments are
positive. I don't care what you do when the arguments are outside the
domain of definition. You can either raise an exception using
`failwith "message"`, or return a default value.

### (A)

Code a recursive function **`gcd`** of
type `int -> int -> int` which takes two
non-negative integers and returns the greatest common divisor of those
integers.

Sample output:

    # gcd 1 1;;
    - : int = 1
    # gcd 1 3;;
    - : int = 1
    # gcd 2 4;;
    - : int = 2
    # gcd 4 2;;
    - : int = 2
    # gcd 4 6;;
    - : int = 2
    # gcd 9 12;;
    - : int = 3
    # gcd 60 70;;
    - : int = 10

Hint: Check out the Euclidean algorithm. The modulus operator in OCaml is `mod`, as in `3 mod 2`. 


### (B)

Code a recursive function **`selfConcat`** of type
`string -> int -> string list` where `selfConcat s n` takes a string `s` and a non-negative integer `n` and returns the result of concatenating `s` with itself `n` times. (When `n` is zero, the result should be the empty string.)

Sample output:

    # selfConcat "" 0;;
    - : string = ""
    # selfConcat "" 1;;
    - : string = ""
    # selfConcat "" 2;;
    - : string = ""
    # selfConcat "" 3;;
    - : string = ""
    # selfConcat "a" 0;;
    - : string = ""
    # selfConcat "a" 1;;
    - : string = "a"
    # selfConcat "a" 2;;
    - : string = "aa"
    # selfConcat "a" 3;;
    - : string = "aaa"
    # selfConcat "hello" 0;;
    - : string = ""
    # selfConcat "hello" 1;;
    - : string = "hello"
    # selfConcat "hello" 2;;
    - : string = "hellohello"
    # selfConcat "hello" 3;;
    - : string = "hellohellohello"
    # selfConcat "hello" 10;;
    - : string = "hellohellohellohellohellohellohellohellohellohello"

Note that the operator for string concatenation in Ocaml is `^`.



### (C)

The [Collatz conjecture](https://en.wikipedia.org/wiki/Collatz_conjecture) says that if you start with any positive _n_ (greater than 0) and iterate the transformation **when _n_ is even return _n_ / 2 and when _n_ is odd return 3 _n_ + 1**, then this process eventually yields 1 after a finite number of steps.

Code a recursive function **`collatz`** of type `int -> int list`
where `collatz n` takes a positive integer `n` and returns the sequence of integers obtained by the process described above, ending in 1.
  
Sample output:

    # collatz 1;;
    - : int list = [1]
    # collatz 2;;
    - : int list = [2; 1]
    # collatz 3;;
    - : int list = [3; 10; 5; 16; 8; 4; 2; 1]
    # collatz 4;;
    - : int list = [4; 2; 1]
    # collatz 12;;
    - : int list = [12; 6; 3; 10; 5; 16; 8; 4; 2; 1]
    # collatz 19;;
    - : int list =
    [19; 58; 29; 88; 44; 22; 11; 34; 17; 52; 26; 13; 40; 20; 10; 5; 16; 8; 4  2; 1]
    # collatz 27;;
    - : int list =
    [27; 82; 41; 124; 62; 31; 94; 47; 142; 71; 214; 107; 322; 161; 484; 242; 121;
     364; 182; 91; 274; 137; 412; 206; 103; 310; 155; 466; 233; 700; 350; 175;
     526; 263; 790; 395; 1186; 593; 1780; 890; 445; 1336; 668; 334; 167; 502;
     251; 754; 377; 1132; 566; 283; 850; 425; 1276; 638; 319; 958; 479; 1438;
     719; 2158; 1079; 3238; 1619; 4858; 2429; 7288; 3644; 1822; 911; 2734; 1367;
     4102; 2051; 6154; 3077; 9232; 4616; 2308; 1154; 577; 1732; 866; 433; 1300;
     650; 325; 976; 488; 244; 122; 61; 184; 92; 46; 23; 70; 35; 106; 53; 160; 80;
     40; 20; 10; 5; 16; 8; 4; 2; 1]



### (D)

Code a recursive function **`range`** of type
`int -> int -> int list` where `range i j` takes two integers `i` and `j` and returns the list of all consecutive integers between `i` (inclusive) and `j` (exclusive). The list is empty if no integer is in the specified range.

Sample output:

    # range 0 0;;
    - : int list = []
    # range 0 1;;
    - : int list = [0]
    # range 0 10;;
    - : int list = [0; 1; 2; 3; 4; 5; 6; 7; 8; 9]
    # range 5 25;;
    - : int list =
    [5; 6; 7; 8; 9; 10; 11; 12; 13; 14; 15; 16; 17; 18; 19; 20; 21; 22; 23; 24]


* * *


## Question 2: List Functions

All of the functions below can be written as recursive
functions over lists. They all fit the general pattern of:

    let rec f xs ... = match xs with
                       | [] -> e1
                       | x::xs' -> e2

where `e2` can use `x`, `xs'`, and the
recusive function itself. For some functions, you may need to do some
additional matching in `e2`, or you can add additional
patterns to the match to capture some special cases.


### (A)

Code a recursive function **`squares`** of
type `int list -> int list` which takes
a list `lst` of integers and returns a new list in which every element of `lst` is squared. 

Sample output:

    # squares [];;
    - : int list = []
    # squares [1];;
    - : int list = [1]
    # squares [1; 2];;
    - : int list = [1; 4]
    # squares [1; 2; 3];;
    - : int list = [1; 4; 9]
    # squares [1; 2; 3; 4];;
    - : int list = [1; 4; 9; 16]
    # squares [1; 2; 3; 4; 10; 4];;
    - : int list = [1; 4; 9; 16; 100; 16]


### (B)

Code a recursive function **`prependString`** of type **`string ->
string list -> string list`** which takes a string `s` and a list of strings
`l` and returns the list obtained by prepending `s` in front of
every string in `l`.

    # prependString "" [];;
    - : string list = []
    # prependString "" ["Alice" ; "Bob"];;
    - : string list = ["Alice"; "Bob"]
    # prependString "Hello " [];;
    - : string list = []
    # prependString "Hello " ["Alice"];;
    - : string list = ["Hello Alice"]
    # prependString "Hello " ["Alice"; "Bob"];;
    - : string list = ["Hello Alice"; "Hello Bob"]
    # prependString "Hello " ["Alice"; "Bob"; "Charlie"];;
    - : string list = ["Hello Alice"; "Hello Bob"; "Hello Charlie"]
    # prependString "Hello " ["Alice"; "Bob"; "Charlie"; "Alice"];;
    - : string list =
    ["Hello Alice"; "Hello Bob"; "Hello Charlie"; "Hello Alice"]
    # prependString "Hello " ["Alice"; "Bob"; "Charlie"; ""];;
    - : string list = ["Hello Alice"; "Hello Bob"; "Hello Charlie"; "Hello "]




### (C)

Code a recursive function **`doubleUp`** of
type `int list -> int list` which takes
a list `lst` of integers and returns a new list where every element of
`lst` is duplicated, with the duplicate doubled. Schematically, if called with an input `[a; b; c]`, the 
result should be `[a; 2 * a ; b; 2 * b ; c; 2 * c]`.  
            
Sample output:

    # doubleUp [];;
    - : int list = []
    # doubleUp [10];;
    - : int list = [10; 20]
    # doubleUp [10; 11];;
    - : int list = [10; 20; 11; 22]
    # doubleUp [10; 11; 12];;
    - : int list = [10; 20; 11; 22; 12; 24]
    # doubleUp [10; 11; 12; 13];;
    - : int list = [10; 20; 11; 22; 12; 24; 13; 26]
    # doubleUp [10; 11; 12; 13; 14];;
    - : int list = [10; 20; 11; 22; 12; 24; 13; 26; 14; 28]
    # doubleUp [10; 11; 12; 13; 14; 13; 12; 11; 10];;
    - : int list =
    [10; 20; 11; 22; 12; 24; 13; 26; 14; 28; 13; 26; 12; 24; 11; 22; 10; 20]


### (D)

Code a recursive function **`nonNegative`** of type `int list -> int list`
which takes a list `lst` of integers and returns a new list with only
the non-negative (that is, greater or equal to 0) elements of `lst`. 

Sample output:

    # nonNegative [];;
    - : int list = []
    # nonNegative [0];;
    - : int list = [0]
    # nonNegative [10];;
    - : int list = [10]
    # nonNegative [-1];;
    - : int list = []
    # nonNegative [-1; -2];;
    - : int list = []
    # nonNegative [-1; 10; -2];;
    - : int list = [10]
    # nonNegative [-1; 10; -2; 20];;
    - : int list = [10; 20]
    # nonNegative [-1; 10; -2; 20; -3; -4; 30; 40; -5];;
    - : int list = [10; 20; 30; 40]


### (E)

Code a recursive function **`concatAll`** of
type `('a list ) list -> 'a list` which takes
a list of lists `lst` and returns a new list made of up all the lists
in `lst` concatenated together.  

Note that operation `@` can be used to concatenate two lists together:
`[1; 2] @ [3; 4; 5] = [1; 2; 3; 4; 5]`. 

Sample output:

    # concatAll [];;
    - : 'a list = []
    # concatAll [[1; 2; 3]];;
    - : int list = [1; 2; 3]
    # concatAll [[1; 2; 3]; [4; 5]];;
    - : int list = [1; 2; 3; 4; 5]
    # concatAll [[1; 2; 3]; [4; 5]; []];;
    - : int list = [1; 2; 3; 4; 5]
    # concatAll [[1; 2; 3]; [4; 5]; []; [6; 7]];;
    - : int list = [1; 2; 3; 4; 5; 6; 7]
    # concatAll [[0]; [10]; [20]; [30]; [40; 50]];;
    - : int list = [0; 10; 20; 30; 40; 50]
    # concatAll [[]; []; []];;
    - : 'a list = []



### (F) (slightly challenging)

Code a recursive function **`classify`** of type `int list -> (int
list * int list)` which takes a list `lst` of integers and returns a
pair of lists, where the first list in the pair contains all the
elements in `lst` that are nonnegative (>= 0) and the second list in the
pair contains all the elements in `lst` that are negative (< 0).  

Sample output:

    # classify [];;
    - : int list * int list = ([], [])
    # classify [1];;
    - : int list * int list = ([1], [])
    # classify [1; 2];;
    - : int list * int list = ([1; 2], [])
    #classify [1; 2; -1];;
    - : int list * int list = ([1; 2], [-1])
    # classify [1; 2; -1; 0];;
    - : int list * int list = ([1; 2; 0], [-1])
    # classify [1; 2; -1; 0; 1; -2];;
    - : int list * int list = ([1; 2; 0; 1], [-1; -2])
    # classify [1; 2; -1; 0; 1; -2; 0; -2; -2; 2];;
    - : int list * int list = ([1; 2; 0; 1; 0; 2], [-1; -2; -2; -2])
    # classify [-1; -2; -3];;
    - : int list * int list = ([], [-1; -2; -3])


* * *


## Question 3: Vectors

We can implement vectors as lists of integers. Operations
on vectors such as addition or scalar multiplication can then
be implemented as recursive functions over lists.

One difference from Question 2
is that you will sometimes have to recurse over two lists at
the same time, which means matching over two
lists. One way to do so is to first match over the first list,
and then match over the second list in each case of the first
list (if applicable). For example:

    let rec f xs ys = match xs with
                      | [] -> e1
                      | x::xs' -> (match ys with
                                   | [] -> e2
                                   | y::ys' -> e3)

Another possibility is to match on both
lists at the same time, by treating them as pairs of
lists. For example:

    let rec f xs ys = match (xs,ys) with
                      | ([],[]) -> e1
                      | (x::xs',[]) -> e2
                      | ([],y::ys') -> e3
                      | (x::xs',y::ys') -> e4

These functions only make sense if their vector arguments have the
same length. You can assume that your code will be given vectors of the same
length. I'm only going to test your code with vectors of the
same length. Still, you should probably make sure that your
code does return a value in case vectors do not have the same
length (I don't care what that value is), or raises an exception using
`failwith "message"`. 


### (A)

Code a recursive function **`scale`** of
type `int -> int list -> int list`
which takes an integer `a` and a vector `v` and
computes the scalar multiplication `a v` (multiplying
every component of `v` by `a`).

Sample output:

    # scale 10 [];;
    - : int list = []
    # scale 0 [1;2;3];;
    - : int list = [0; 0; 0]
    # scale 1 [1;2;3];;
    - : int list = [1; 2; 3]
    # scale 2 [1;2;3];;
    - : int list = [2; 4; 6]
    # scale 10 [1;2;3];;
    - : int list = [10; 20; 30]


### (B)

Code a recursive function **`add`** of type `int list -> int list ->
int list` which takes two vectors `v` and `w` and returns the vector
sum `v + w`.

Sample output:

    # add [] [];;
    - : int list = []
    # add [10] [20];;
    - : int list = [30]
    # add [10;20;30] [20;40;60];;
    - : int list = [30; 60; 90]


### (C)

Code a recursive function **`length`** of type
`int list -> float` which takes a vector `v` and returns the length
(or [Euclidean norm](https://en.wikipedia.org/wiki/Norm_(mathematics)#Euclidean_norm)) of
vector `v`, defined to be the square root of the sum of the squares of the
components of `v`.

The square root can be computed using the OCaml function `sqrt`.

Sample output:

    # length [];;
    - : float = 0.
    # length [10];;
    - : float = 10.
    # length [10; 20];;
    - : float = 22.3606797749978981
    # length [10; 20; 30];;
    - : float = 37.416573867739416
    # length [10; 20; 30; 40];;
    - : float = 54.7722557505166137
    # length [10; 10];;
    - : float = 14.142135623730951


### (D)

Code a recursive function **`inner`** of
type `int list -> int list -> int`
which takes two vectors `v` and `w` and returns the [inner
product](https://en.wikipedia.org/wiki/Dot_product) `v`  &middot; `w`.

Sample output:

    # inner [] [];;
    - : int = 0
    # inner [10] [20];;
    - : int = 200
    # inner [1;2;3] [4;5;6];;
    - : int = 32
    # inner [1;2;3] [8;10;12];;
    - : int = 64


* * *

## Question 4: Set Functions

A set is a collection of elements in
which repetition and order is irrelevant; the sets {1,1,2,3}
and {2,3,3,1} the same set.

We use lists as a representation for sets. We define set
operations that work with that set representation. We require
the invariant that a list representing a set cannot have repeated
elements. It is therefore your responsibility in the functions below
to make sure that when your functions produce lists representing sets,
you have no repeated elements.

Most of these functions are recursive. I will generally not bother
pointing out when I'm asking for a recursive function.

Order is not important in sets, therefore it is okay if the order of
elements in your results differ from my sample outputs below.


### (A)

Code a function **`set`** of type **`'a list -> 'a list`** which takes
a list `xs` and returns a set (i.e., a list without repeated elements)
with the same elements as `xs`.

Sample output:

    # set [];;
    - : 'a list = []
    # set [1];;
    - : int list = [1]
    # set [1; 2; 3];;
    - : int list = [1; 2; 3]
    # set [1; 2; 3; 2; 1];;
    - : int list = [3; 2; 1]
    # set [1; 2; 3; 2; 1; 2; 3; 4; 3; 2; 1];;
    - : int list = [4; 3; 2; 1]
    # set ["hello"; "world"; "hello"];;
    - : string list = ["world"; "hello"]

Note that library function `List.mem` can be used to check if an element is a member of a list, as in `List.mem 10 [1; 2; 10; 20]`.


### (B)


Code a function **`set_sub`** of type **`'a list -> 'a list ->
bool`** which takes two sets `s` and `t` and returns true exactly when
`s` is a subset of `t` (that is, every element of `s` is an element of
`t`).

Sample output:

    # set_sub [] [];;
    - : bool = true
    # set_sub [] [1];;
    - : bool = true
    # set_sub [1] [1];;
    - : bool = true
    # set_sub [1] [1; 2];;
    - : bool = true
    # set_sub [1] [1; 2; 3];;
    - : bool = true
    # set_sub [1; 2] [1; 2; 3];;
    - : bool = true
    # set_sub [2; 1] [1; 2; 3];;
    - : bool = true
    # set_sub [3; 2; 1] [1; 2; 3];;
    - : bool = true
    # set_sub [4; 3; 2; 1] [1; 2; 3];;
    - : bool = false
    # set_sub [4; 3; 2] [1; 2; 3];;
    - : bool = false
    # set_sub [4] [1; 2; 3];;
    - : bool = false
    # set_sub [4] [];;
    - : bool = false
    # set_sub ["hello"] ["hello"; "world"];;
    - : bool = true


### (C)

Code a function **`set_union`** of type **`'a list -> 'a list -> 'a list`** which takes two sets `s` and `t` and
returns a set made up of all the elements in `s` and `t`.

    # set_union [] [];;
    - : 'a list = []
    # set_union [] [1; 2];;
    - : int list = [1; 2]
    # set_union [1; 2] [];;
    - : int list = [1; 2]
    # set_union [1; 2] [3; 4];;
    - : int list = [1; 2; 3; 4]
    # set_union [1; 2] [2; 3];;
    - : int list = [1; 2; 3]
    # set_union [1; 2] [1; 2; 3; 4];;
    - : int list = [1; 2; 3; 4]
    # set_union [1; 2; 3; 4] [1; 2];;
    - : int list = [3; 4; 1; 2]
    # set_union ["hello"] ["world"];;
    - : string list = ["hello"; "world"]


### (D)

Code a function **`set_inter`** of type **`'a list -> 'a list -> 'a list`** which
takes two sets `s` and `t` and returns a new set made up of all the elements in common to `s` and `t`.

Sample output:

    # set_inter [] [];;
    - : 'a list = []
    # set_inter [1] [];;
    - : int list = []
    # set_inter [1; 2] [];;
    - : int list = []
    # set_inter [] [1; 2];;
    - : int list = []
    # set_inter [1] [1; 2];;
    - : int list = [1]
    # set_inter [1; 2] [1; 2];;
    - : int list = [1; 2]
    # set_inter [1; 2; 3] [1; 2];;
    - : int list = [1; 2]
    # set_inter [1; 2; 3] [2];;
    - : int list = [2]
    # set_inter ["a"; "b"; "c"] ["b"; "c"; "d"];;
    - : string list = ["b"; "c"]

