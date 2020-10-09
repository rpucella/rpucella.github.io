# Homework 2

## Due Date: Thursday, October 15, 2020 (23h59)

- This homework is to be done individually. You may discuss problems
with fellow students, but all submitted work must be entirely your
own, and should not be from any other course, present, past, or
future. If you use a solution from another source you must cite it
&mdash; this includes when that source is someone else helping
you.

- **Please do not post your solutions on a
public website or a public repository like GitHub.**

- All programming is to be done in OCaml v4.

- Code your answers by modifying the file [`homework3.ml`](homework3.ml) provided. Add your **name**, your **email address**, and any **remarks** that you
wish to make to the instructor to the block comment at the head of the file.

- **Please do not change the types in the signature of the
function stubs I provide**. Doing so will make it
impossible to load your code into the testing infrastructure,
and make me unhappy.

- Feel free to define helper functions if you need them.


## Electronic Submission Instructions

- Start a _fresh_  OCaml shell.

- Load your homework code via `#use "homework3.ml";;` to make sure that there are no errors when I will load your code.

- If there are any error, do not submit. I can't test what I can't `#use`.

- When you're ready to submit, send an email with your file
`homework3.ml` as an attachment to `olin.submissions@gmail.com` with
subject _Homework 3 submission_.

* * *


In this homework, you will be working with and developing [higher order functions](../notes-functional.html).

For each function you have to code in this homework, full points will
be awarded if the function and its helpers do not use explicit
recursion. In other words, the function and its helpers should not use the `rec`
keyword for function definitions.

This means that you will have to use `List.map`, `List.filter`,
`List.fold_right`, or some other higher-order function in the [`List`
module](https://caml.inria.fr/pub/docs/manual-ocaml/libref/List.html). One
additional function you may find useful is `List.map2`, which acts
like `List.map` but maps over two lists, passing elements of both
lists to the mapping function and producing a single list as result.

To illustrate, here is the `squares` function defined using explicit recursion:

    let rec squares (xs: int list): int list =
      match xs with
      | [] -> []
      | x::xs' -> (x * x) :: (squares xs')

Here is the `squares` function defined without using explicit recursion:

    let squares (xs: int list): int list =
      List.map (fun x -> x * x) xs

For another illustration, here is the `length` function defined using explicit recursion:

    let rec length (xs: 'a list): int =
      match xs with
      | [] -> 0
      | x::xs' -> 1 + length xs

Here is the length function defined without using explicit recursion:

    let length (xs: 'a list): int =
      List.fold_right (fun x r -> 1 + r) xs 0

If you can't get a function to work without explicit recursion, please
go ahead and implement a version with explicit recursion.


* * *


## Question 1: Higher-Order Functions I


### (A) 

Code a function **`absNonZero`** with type **`int list -> int list`**
where `absNonZero xs` returns the list obtained by taking the absolute
value of every non-zero element in `xs`, and dropping the zero elements.

Sample output:

    # absNonZero [];;
    - : int list = []
    # absNonZero [1; 2; 3];;
    - : int list = [1; 2; 3]
    # absNonZero [-1; 2; -3];;
    - : int list = [1; 2; 3]
    # absNonZero [0; 0; 0];;
    - : int list = []
    # absNonZero [-1; 0; 2; 0; -3];;
    - : int list = [1; 2; 3]



### (B)

Code a function **`map_functions`** with type **`('a -> 'b) list -> 'a
-> 'b list`** where `map_functions fs x` returns the list obtained by
applying every function in `fs` to `x`.

Sample output:

    # map_functions [] 0;;
    - : 'a list = []
    # map_functions [(fun x -> x+1)] 0;;
    - : int list = [1]
    # map_functions [(fun x -> x+1); (fun x -> x+2)] 0;;
    - : int list = [1; 2]
    # map_functions [(fun x -> x+1); (fun x -> x+2); (fun x -> x+99)] 0;;
    - : int list = [1; 2; 99]
    # map_functions [(fun x -> x+1); (fun x -> x+2); (fun x -> x+99)] 100;;
    - : int list = [101; 102; 199]


### (C)

Code a function **`compose_all`** with type **`('a -> 'a) list -> 'a
-> 'a`** where `compose_all fs x` returns the result `f1 (f2 (f3
(... (fk x)...)))` when `fs` is `[f1; f2; ...; fk]`. It 
returns input `x` when `fs` is empty.

Sample output:

    # compose_all [] 10;;
    - : int = 10
    # compose_all [(fun x -> x+1)] 10;;
    - : int = 11
    # compose_all [(fun x -> x+1); (fun x -> x+2)] 10;;
    - : int = 13
    # compose_all [(fun x -> x+1); (fun x -> x+2); (fun x -> x+3)] 10;;
    - : int = 16
    # compose_all [(fun x -> x+1); (fun x -> x+2); (fun x -> x*3)] 10;;
    - : int = 33
    

### (D)

Code a function **`pairs1`** of type `'a -> 'b list -> ('a * 'b)
list` where `pairs1 x ys` returns the list of all pairs of `x` with an
element of `ys`. 

Sample output:

    # pairs1 9 [];;
    - : (int * 'a) list = []
    # pairs1 9 ["a"];;
    - : (int * string) list = [(9, "a")]
    # pairs1 9 ["a"; "b"];;
    - : (int * string) list = [(9, "a"); (9, "b")]
    # pairs1 9 ["a"; "b"; "c"];;
    - : (int * string) list = [(9, "a"); (9, "b"); (9, "c")]
    # pairs1 9 ["a"; "b"; "c"; "c"];;
    - : (int * string) list = [(9, "a"); (9, "b"); (9, "c"); (9, "c")]
    # pairs1 "hello" [1; 2; 3];;
    - : (string * int) list = [("hello", 1); ("hello", 2); ("hello", 3)]


### (E)

Code a function **`pairs`** of type `'a list -> 'b list -> ('a * 'b)
list` where `pairs xs ys` returns all the ways of pairing up an
element of `xs` with an element of `ys`.

Sample output:

    # pairs [] [];;
    - : ('a * 'b) list = []
    # pairs [1; 2] [];;
    - : (int * 'a) list = []
    # pairs [] ["a"; "b"; "c"];;
    - : ('a * string) list = []
    # pairs [1] ["a"; "b"; "c"];;
    - : (int * string) list = [(1, "a"); (1, "b"); (1, "c")]
    # pairs [1; 2] ["a"];;
    - : (int * string) list = [(1, "a"); (2, "a")]
    # pairs [1; 2] ["a"; "b"; "c"];;
    - : (int * string) list =
    [(1, "a"); (1, "b"); (1, "c"); (2, "a"); (2, "b"); (2, "c")]


**Hint**: Use `pairs1`. 


* * *

## Question 2: Higher-Order Functions II


### (A)

Code a function `cons_all` of type `a -> ('a list) list -> ('a list) list` where
`cons_all x xss` returns the list obtained by adding `x` as the first element of every list in `xss`.

Sample output:

    # cons_all 1 [];;
    - : int list list = []
    # cons_all 1 [[]];;
    - : int list list = [[1]]
    # cons_all 1 [[66]];;
    - : int list list = [[1; 66]]
    # cons_all 1 [[66]; [99]];;
    - : int list list = [[1; 66]; [1; 99]]
    # cons_all 1 [[66]; [99]; []];;
    - : int list list = [[1; 66]; [1; 99]; [1]]
    # cons_all 1 [[66]; [99]; []; [66; 99]];;
    - : int list list = [[1; 66]; [1; 99]; [1]; [1; 66; 99]]
    # cons_all "+" [["hello"; "world"]; ["goodbye"]];;
    - : string list list = [["+"; "hello"; "world"]; ["+"; "goodbye"]]


### (B)

Code a function `prefixes` of type `'a list -> ('a list) list` where
`prefixes xs` returns the list of all prefixes of `xs`. For instance,
the prefixes of `[x1; x2; x3]` are `[]`, `[x1]`, `[x1; x2]`, and `[x1;
x2; x3]`. Note that the empty list is always a prefix.

Sample output:

    # prefixes [];;
    - : 'a list list = [[]]
    # prefixes [1];;
    - : int list list = [[]; [1]]
    # prefixes [1; 2; 3; 4];;
    - : int list list = [[]; [1]; [1; 2]; [1; 2; 3]; [1; 2; 3; 4]]
    # prefixes ["a"; "b"];;
    - : string list list = [[]; ["a"]; ["a"; "b"]]


### (C)

Code a function `suffixes` of type `'a list -> ('a list) list` where
`suffixes xs` returns the list of all suffixes of `xs`. For instance, the
suffixes of `[x1; x2; x3]` are `[]`, `[x3]`, `[x2; x3]`, and `[x1; x2; x3]`.
Note that the empty list is always a suffix.

Sample output:

    # suffixes [];;
    - : 'a list list = [[]]
    # suffixes [1];;
    - : int list list = [[1]; []]
    # suffixes [1; 2; 3; 4];;
    - : int list list = [[1; 2; 3; 4]; [2; 3; 4]; [3; 4]; [4]; []]
    # suffixes ["a"; "b"];;
    - : string list list = [["a"; "b"]; ["b"]; []]


### (D)

Code a function `splits` of type `'a list -> ('a list * 'a list) list`
where `splits xs` returns a list of all ways of splitting list `xs` into two sublists `xs1` and `xs2` such that `xs1 @ xs2` yields `xs`.

Sample output:

    # splits [];;
    - : ('a list * 'a list) list = [([], [])]
    # splits [1];;
    - : (int list * int list) list = [([], [1]); ([1], [])]
    # splits [1; 2; 3; 4];;
    - : (int list * int list) list =
    [([], [1; 2; 3; 4]); ([1], [2; 3; 4]); ([1; 2], [3; 4]); ([1; 2; 3], [4]);
     ([1; 2; 3; 4], [])]
    # splits ["a"; "b"];;
    - : (string list * string list) list =
    [([], ["a"; "b"]); (["a"], ["b"]); (["a"; "b"], [])]

**Hint**: Use `prefixes` and `suffixes`


### (E)

Code a function `inject` of type '`a -> 'a list -> ('a list) list`
where `inject x xs` returns a list of all the ways in which value 
`x` can be added to the list `xs`.

Sample output:

    # inject 99 [];;
    - : int list list = [[99]]
    # inject 99 [1];;
    - : int list list = [[99; 1]; [1; 99]]
    # inject 99 [1; 2];;
    - : int list list = [[99; 1; 2]; [1; 99; 2]; [1; 2; 99]]
    # inject 99 [1; 2; 3; 4];;
    - : int list list =
    [[99; 1; 2; 3; 4]; [1; 99; 2; 3; 4]; [1; 2; 99; 3; 4]; [1; 2; 3; 99; 4]; 
     [1; 2; 3; 4; 99]]
    # inject "X" ["a"; "b"];;
    - : string list list = [["X"; "a"; "b"]; ["a"; "X"; "b"]; ["a"; "b"; "X"]]
    
**Hint**: Use `splits`.


### Bonus question for your edification but no points

Code a function `permutations` of type `'a list -> ('a list) list`
where `permutations xs` returns the list of all permutations
of `xs`. A permutation of a list is a list
containing the exact same elements, but in a different
oder, treating repeated elements as distinct elements.

Sample output:

    # permutations [];;
    - : 'a list list = [[]]
    # permutations [1];;
    - : int list list = [[1]]
    # permutations [1; 2];;
    - : int list list = [[1; 2]; [2; 1]]
    # permutations [1; 2; 3; 4];;
    - : int list list =
    [[1; 2; 3; 4]; [2; 1; 3; 4]; [2; 3; 1; 4]; [2; 3; 4; 1]; [1; 3; 2; 4];
     [3; 1; 2; 4]; [3; 2; 1; 4]; [3; 2; 4; 1]; [1; 3; 4; 2]; [3; 1; 4; 2];
     [3; 4; 1; 2]; [3; 4; 2; 1]; [1; 2; 4; 3]; [2; 1; 4; 3]; [2; 4; 1; 3];
     [2; 4; 3; 1]; [1; 4; 2; 3]; [4; 1; 2; 3]; [4; 2; 1; 3]; [4; 2; 3; 1];
     [1; 4; 3; 2]; [4; 1; 3; 2]; [4; 3; 1; 2]; [4; 3; 2; 1]]
    # permutations ["a"; "b"];;
    - : string list list = [["a"; "b"]; ["b"; "a"]]

**Hint**: The permutations of a list are all the ways of adding the
  first element of the list into every permutation of the rest of the list.



* * *


## Question 3: Simulating Deterministic Finite Automata

I claimed in class that one reason why we might care about
deterministic finite automata is that they are much easier to
simulate than general (possibly nondeterministic) finite automata. The
point of this question is to illustrate exactly that 
point, by comparing to Question 1 from Homework 2. 

Consider the following OCaml type for _deterministic_ finite automata:

    type dfa = { states: int list;
                 alphabet: char list;
                 delta: int -> char -> int;
                 start: int;
                 final: int list }

A deterministic finite automaton is described by a record in pretty
much the same way a finite automaton was described in Homework 2 (list
of states, list of characters making up the alphabet, start state,
list of final states) except for the transition function `delta`, which
here is an actual OCaml function that takes a state `q` and a symbol
`sym` and returns the state you transition to from state `q` upon
symbol `sym`. We can use a function here instead of a list because of
the determinism. 

Similar to last homework, here is an example of a deterministic
finite automaton that accepts the set of all strings over `{x, y}`
that contains a number of `x`s that is a multiple of 3.  

    let dfa1 = { 
      states = [1; 2; 3];
      alphabet = ['x'; 'y'];
      delta = (fun q x -> match (q, x) with
                          | (1, 'x') -> 2
                          | (2, 'x') -> 3
                          | (3, 'x') -> 1
                          | (1, 'y') -> 1
                          | (2, 'y') -> 2
                          | (3, 'y') -> 3);
      start = 1;
      final = [1]
    } 

Similarly, here is a deterministic finite automaton that accepts the
set of all strings over `{x, y}` that start and end with an `x`.
    
    let dfa2 = { 
      states = [10; 11; 12; 13];
      alphabet = ['x'; 'y'];
      delta = (fun q x -> match (q, x) with
                          | (10, 'x') -> 11
                          | (11, 'x') -> 12
                          | (11, 'y') -> 11
                          | (12, 'x') -> 12
                          | (12, 'y') -> 11
                          | (_, _) -> 13);
      start = 10;
      final = [12]
    } 

(Note the final clause in the `match` of the transition function
`delta` :  the `_` is a wildcard matching any value, so that every
state and character combination that doesn't match any of the
preceding clauses will match the final clause and send the automaton
to state 13. From state 13, every transition will lead back to state
13, so a state like state 13 is called a _sink state_. Once the
automaton reaches state 13, it remains in state 13. And since state 13
is not a final state, once a string makes the automaton hit state 13,
the string will never be accepted.)

Code a function **`accept`** of type **`dfa -> string -> bool`**
where `accept m s` returns true if and only if deterministic finite automaton `m`
accepts string `s`. That is, it returns true exactly if following
the transitions of `m` labeled by the symbols in `s` from the start
state lead to a final state.

You can use function `explode` (provided in the homework code) to turn
a string into a list of symbols (Ocaml type `char`). For testing
purposes, I've provided you with a function `lang` where `lang m k`
prints out all the strings of length up to `k` accepted by finite
automaton `m`. Note that it calls `accept`, so it won't work correctly
until you implement that function.

Sample output:

    # lang dfa1 5;;
      <epsilon>
      y
      yy
      xxx
      yyy
      yxxx
      xyxx
      xxyx
      xxxy
      yyyy
      yyxxx
      yxyxx
      xyyxx
      yxxyx
      xyxyx
      xxyyx
      yxxxy
      xyxxy
      xxyxy
      xxxyy
      yyyyy
    - : unit = ()
    # lang dfa2 5;;
      xx
      xxx
      xyx
      xxxx
      xyxx
      xxyx
      xyyx
      xxxxx
      xyxxx
      xxyxx
      xyyxx
      xxxyx
      xyxyx
      xxyyx
      xyyyx
    - : unit = ()

**Hint**: Define a helper function `followString : int -> char list ->
int` that takes a state `q` and a list of symbols `syms` and gives you back the
state that you reach if you follow the symbols in `syms` in order
starting from state `q`. It should be way simpler than `followString`
and `followStringFromSet` from Homework 2.
