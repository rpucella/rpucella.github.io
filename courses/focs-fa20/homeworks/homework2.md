# Homework 2

## Due Date: Thursday, October 8, 2020 (23h59)

- This homework is to be done individually. You may discuss problems
with fellow students, but all submitted work must be entirely your
own, and should not be from any other course, present, past, or
future. If you use a solution from another source you must cite it
&mdash; this includes when that source is someone else helping
you.

- **Please do not post your solutions on a
public website or a public repository like GitHub.**

- All programming is to be done in OCaml v4.

- Code your answers by modifying the file [`homework2.ml`](homework2.ml) provided. Add your **name**, your **email address**, and any **remarks** that you
wish to make to the instructor to the block comment at the head of the file.

- **Please do not change the types in the signature of the
function stubs I provide**. Doing so will make it
impossible to load your code into the testing infrastructure,
and make me unhappy.

- Feel free to define helper functions if you need them.


## Electronic Submission Instructions

- Start a _fresh_  OCaml shell.

- Load your homework code via `#use "homework2.ml";;` to make sure that there are no errors when I will load your code.

- If there are any error, do not submit. I can't test what I can't `#use`.

- When you're ready to submit, send an email with your file
`homework2.ml` as an attachment to `olin.submissions@gmail.com` with
subject _Homework 2 submission_.

* * *


Consider the following OCaml type for finite automata:

    type fa = { states: int list;
                alphabet: string list;
                delta: (int * string * int) list;
                start: int;
                final: int list }

A finite automaton is described by a record with fields holding the
various components of the automaton: the set of states, the alphabet,
the transition relation &Delta;, the start state, and the set of final
states. Lists are used to represent sets. The transition relation is
represented using list of triples (_p_,_a_,_q_), stating that in state
_p_, reading symbol _a_ makes the machine transition to state _q_. For simplicity,
I'm representing states using integers. It's easy enough to modify this declaration
to make finite state machines generic over the type of the state.

Records are basically dictionaries, except that they are statically
typed, and the field names making up a record are part of its
type. For information how to work with records in OCaml, [check out
this link and scroll down to the _Records_
section](http://www.cs.cornell.edu/courses/cs3110/2011sp/recitations/rec02-ocaml/ocaml.htm). Note
that aside from pattern matching, you can also access the fields of a
record using the `.` notation: `record.fld` will return the value of
field `fld` of `record`.

Here is an example of a finite automaton that accepts the
language of all strings over `{x, y, z}` whose last three
symbols are `y`s.

    let faLastThreeY = {
      states = [0; 1; 2; 3];
      alphabet = ['x'; 'y'; 'z'];
      delta = [ (0, 'x', 0);
                (0, 'y', 0);
                (0, 'z', 0);
                (0, 'y', 1);
                (1, 'y', 2);
                (2, 'y', 3); ];
      start = 0;
      final = [3]
    } 

Here is an example of a (deterministic) finite automaton that accepts
the set of all strings over `{x, y}` that contains a number
of `x`s that is a multiple of 3.  

    let faThreeX = { 
      states = [1; 2; 3];
      alphabet = ['x'; 'y'];
      delta = [ (1, 'x', 2);
                (2, 'x', 3);
                (3, 'x', 1);
                (1, 'y', 1);
                (2, 'y', 2);
                (3, 'y', 3) ];
      start = 1;
      final = [1]
    } 


* * *


## Question 1: Simulating Finite Automata

We are going to code up a simulator for finite automata. The only real difficulty is figuring out how to handle choices. What we are going to do is code a function `followString` below that takes a state and a string of symbols and returns _all_ the states that can be reached by starting from that state and following transitions labeled with the given symbols. Therefore, to check if a finite automaton accepts a string, we start from the start state of the automaton, and check if any of the states reachable by following the symbols from the string is a final state. We're going to build up to that in steps.


### (A)

Code a function **`hasFinal`** of
type **`fa -> int list -> bool`**
where `hasFinal m qs` returns true if one of the
states in `qs` is a final state of
automaton `m`, and false
otherwise. 

Sample output:

    # hasFinal faLastThreeY [];;
    - : bool = false
    # hasFinal faLastThreeY [0];;
    - : bool = false
    # hasFinal faLastThreeY [0;1];;
    - : bool = false
    # hasFinal faLastThreeY [0;1;2];;
    - : bool = false
    # hasFinal faLastThreeY [0;1;2;3];;
    - : bool = true
    # hasFinal faLastThreeY [3];;
    - : bool = true
    # hasFinal faThreeX [1;2];;
    - : bool = true


### (B)

Code a function **`followSymbol`** of
type **`fa -> int -> char -> int list`**
where `followSymbol m q sym` returns the list of
states of `m` that can be reached by a transition 
labeled `sym` from state `q`.

Sample output:

    # followSymbol faLastThreeY 0 'x';;
    - : int list = [0]
    # followSymbol faLastThreeY 0 'y';;
    - : int list = [0; 1]
    # followSymbol faLastThreeY 1 'x';;
    - : int list = []
    # followSymbol faLastThreeY 1 'y';;
    - : int list = [2]
    # followSymbol faLastThreeY 2 'x';;
    - : int list = []
    # followSymbol faLastThreeY 2 'y';;
    - : int list = [3]
    # followSymbol faLastThreeY 3 'x';;
    - : int list = []
    # followSymbol faLastThreeY 3 'y';;
    - : int list = []
    # followSymbol faThreeX 1 'x';;
    - : string list = [2]

Order and repetition in the resulting list are irrelevant.


### (C)

Code a function **`followSymbolFromSet`** of
type **`fa -> int list -> char -> int list`**
where `followSymbolFromSet m qs sym` returns the list of
states of `m` that are reachable by following a transition 
labeled `sym` from _any_ of the states
in `qs`.

For your own sanity, it helps if the list you get has no duplicate
states. Execution of later functions may slow to a crawl
otherwise. The easiest way to avoid duplicate states is to implement a function that
adds states to an existing list of states making sure that it only
adds a state if it isn't already in the list.

Sample output:

    # followSymbolFromSet faThreeX [] 'x';;
    - : string list = []
    # followSymbolFromSet faThreeX [1] 'x';;
    - : string list = [2]
    # followSymbolFromSet faThreeX [1] 'y';;
    - : string list = [1]
    # followSymbolFromSet faThreeX [1; 2] 'x';;
    - : string list = [2; 3]
    # followSymbolFromSet faThreeX [1; 2] 'y';;
    - : string list = [1; 2]
    # followSymbolFromSet faLastThreeY [0;1] 'x';;
    - : int list = [0]
    # followSymbolFromSet faLastThreeY [0;1] 'y';;
    - : int list = [0; 1; 2]

Order and repetition in the resulting list are irrelevant.

**Hint**: use `followSymbol`.

### (D)

Code a function **`followStringFromSet`** of
type **`fa -> int list -> char list -> int list`**
where `followStringFromSet m qs syms` returns the list of
states of `m` that are reachable by following a
sequence of transitions labeled by the symbols
in `syms` (in order) starting from any of the states
in `qs`.

Again, it helps if the list you get contains no duplicate states.

Use `followStringFromSet` to define a function `followString` of type **`fa -> int -> char list -> int list`** where `followString m q syms` returns the list of states of `m` that are reachable by following a sequence of transitions labeled by the symbols in `sym` starting from state `q`.

Sample output:

    # followStringFromSet faThreeX [] [];;
    - : string list = []
    # followStringFromSet faThreeX [] ['x'];;
    - : string list = []
    # followStringFromSet faThreeX [] ['x';'y'];;
    - : string list = []
    # followStringFromSet faThreeX [1] [];;
    - : string list = [1]
    # followStringFromSet faThreeX [1] ['x'];;
    - : string list = [2]
    # followStringFromSet faThreeX [1] ['x';'y'];;
    - : string list = [2]
    # followStringFromSet faThreeX [1] ['x';'x'];;
    - : string list = [3]
    # followStringFromSet faThreeX [1;2] ['x';'x'];;
    - : string list = [3; 1]
    # followStringFromSet faThreeX [1;2] ['x';'x';'y'];;
    - : string list = [3; 1]
    # followStringFromSet faThreeX [1;2] ['x';'x';'y';'x'];;
    - : string list = [1; 2]
    # followStringFromSet faLastThreeY [0;1] ['x';'y';'y';'y'];;
    - : int list = [0; 1; 2; 3]

    # followString faThreeX 1 [];;
    - : int list = [1]
    # followString faThreeX 1 ['x'];;
    - : int list = [2]
    # followString faThreeX 1 ['x'; 'y'];;
    - : int list = [2]
    # followString faThreeX 1 ['x'; 'x'];;
    - : int list = [3]
    # followString faThreeX 2 ['x'; 'x'];;
    - : int list = [1]
    # followString faThreeX 2 ['x'; 'x'; 'y'];;
    - : int list = [1]
    # followString faLastThreeY 0 ['x'; 'y'; 'y'; 'y'];;
    - : int list = [0; 1; 2; 3]
    # followString faLastThreeY 0 ['x'; 'y'; 'y'];;
    - : int list = [0; 1; 2]
    # followString faLastThreeY 1 ['x'; 'y'; 'y'; 'y'];;
    - : int list = []

Order and repetition in the resulting list are irrelevant.


### (E)

Code a function **`accept`** of type **`fa -> string -> bool`**
where `accept m s` returns true if and only if finite automaton `m`
accepts the string `s`. That is, it returns true exactly if there is a
way to follow the transitions of `m` labeled by the symbols in `s`
from the start state to a final state.

You can use function `explode` (provided in the homework code) to turn
a string into a list of symbols (Ocaml type `char`). You should probably also use
`followString` and `hasFinal`...

For testing purposes, I've provided you with a function `lang` where
`lang m k` prints out all the strings of length up to `k` accepted by
finite automaton `m`. Note that it calls `accept`, so it won't work
correctly until you implement that function.

Sample output:

    # accept faThreeX "yxyxy";;
    - : bool = false
    # accept faThreeX "yxyxyx";;
    - : bool = true
    # accept faThreeX "yxyxyxy";;
    - : bool = true
    # accept faLastThreeY "xyy";;
    - : bool = false
    # accept faLastThreeY "xyyy";;
    - : bool = true
    # accept faLastThreeY "xyyyy";;
    - : bool = true
    # lang faLastThreeY 6;;
      yyy
      xyyy
      yyyy
      zyyy
      xxyyy
      yxyyy
      zxyyy
      xyyyy
      yyyyy
      zyyyy
      xzyyy
      yzyyy
      zzyyy
      xxxyyy
      yxxyyy
      zxxyyy
      xyxyyy
      yyxyyy
      zyxyyy
      xzxyyy
      yzxyyy
      zzxyyy
      xxyyyy
      yxyyyy
      zxyyyy
      xyyyyy
      yyyyyy
      zyyyyy
      xzyyyy
      yzyyyy
      zzyyyy
      xxzyyy
      yxzyyy
      zxzyyy
      xyzyyy
      yyzyyy
      zyzyyy
      xzzyyy
      yzzyyy
      zzzyyy


* * *

## Question 2: Constructing Finite Automata

In this question, you will come up with finite automata. You can test
your automata with the code you wrote in Question 1.

For submission purposes, I ask you to define those finite automata as
constants using `let` with a fixed name given below in each
question. There is already a placeholder in `homework2.ml` for those
answers. Just replace the placeholder with your definition.



### (A)

Construct a finite automaton **`fa_q2_a`** for the language consisting of
all strings over the alphabet `{x,y,z}` of length 3n+1 or 3n+2 for any n
(i.e., of length 1, 2, 4, 5, 7, 8, 10, 11, ...)

Sample output:

    # lang fa_q2_a 6;;
      x
      y
      z
      xx
      yx
      zx
      xy
      yy
      zy
      xz
      yz
      zz
      xxxx
      yxxx
      zxxx
      xyxx
      yyxx
      zyxx
      xzxx
      yzxx
      zzxx
      xxyx
      yxyx
      zxyx
      xyyx
      yyyx
      zyyx
      xzyx
      yzyx
      zzyx
      xxzx
      yxzx
      zxzx
      xyzx
      yyzx
      zyzx
      xzzx
      yzzx
      zzzx
      xxxy
      yxxy
      zxxy
      xyxy
      yyxy
      zyxy
      xzxy
      yzxy
      zzxy
      xxyy
      yxyy
      zxyy
      xyyy
      yyyy
      zyyy
      xzyy
      yzyy
      zzyy
      xxzy
      yxzy
      zxzy
      xyzy
      yyzy
      zyzy
      xzzy
      yzzy
      zzzy
      xxxz
      yxxz
      zxxz
      xyxz
      yyxz
      zyxz
      xzxz
      yzxz
      zzxz
      xxyz
      yxyz
      zxyz
      xyyz
      yyyz
      zyyz
      xzyz
      yzyz
      zzyz
      xxzz
      yxzz
      zxzz
      xyzz
      yyzz
      zyzz
      xzzz
      yzzz
      zzzz
      xxxxx
      yxxxx
      zxxxx
      xyxxx
      yyxxx
      zyxxx
      xzxxx
      yzxxx
      zzxxx
      xxyxx
      yxyxx
      zxyxx
      xyyxx
      yyyxx
      zyyxx
      xzyxx
      yzyxx
      zzyxx
      xxzxx
      yxzxx
      zxzxx
      xyzxx
      yyzxx
      zyzxx
      xzzxx
      yzzxx
      zzzxx
      xxxyx
      yxxyx
      zxxyx
      xyxyx
      yyxyx
      zyxyx
      xzxyx
      yzxyx
      zzxyx
      xxyyx
      yxyyx
      zxyyx
      xyyyx
      yyyyx
      zyyyx
      xzyyx
      yzyyx
      zzyyx
      xxzyx
      yxzyx
      zxzyx
      xyzyx
      yyzyx
      zyzyx
      xzzyx
      yzzyx
      zzzyx
      xxxzx
      yxxzx
      zxxzx
      xyxzx
      yyxzx
      zyxzx
      xzxzx
      yzxzx
      zzxzx
      xxyzx
      yxyzx
      zxyzx
      xyyzx
      yyyzx
      zyyzx
      xzyzx
      yzyzx
      zzyzx
      xxzzx
      yxzzx
      zxzzx
      xyzzx
      yyzzx
      zyzzx
      xzzzx
      yzzzx
      zzzzx
      xxxxy
      yxxxy
      zxxxy
      xyxxy
      yyxxy
      zyxxy
      xzxxy
      yzxxy
      zzxxy
      xxyxy
      yxyxy
      zxyxy
      xyyxy
      yyyxy
      zyyxy
      xzyxy
      yzyxy
      zzyxy
      xxzxy
      yxzxy
      zxzxy
      xyzxy
      yyzxy
      zyzxy
      xzzxy
      yzzxy
      zzzxy
      xxxyy
      yxxyy
      zxxyy
      xyxyy
      yyxyy
      zyxyy
      xzxyy
      yzxyy
      zzxyy
      xxyyy
      yxyyy
      zxyyy
      xyyyy
      yyyyy
      zyyyy
      xzyyy
      yzyyy
      zzyyy
      xxzyy
      yxzyy
      zxzyy
      xyzyy
      yyzyy
      zyzyy
      xzzyy
      yzzyy
      zzzyy
      xxxzy
      yxxzy
      zxxzy
      xyxzy
      yyxzy
      zyxzy
      xzxzy
      yzxzy
      zzxzy
      xxyzy
      yxyzy
      zxyzy
      xyyzy
      yyyzy
      zyyzy
      xzyzy
      yzyzy
      zzyzy
      xxzzy
      yxzzy
      zxzzy
      xyzzy
      yyzzy
      zyzzy
      xzzzy
      yzzzy
      zzzzy
      xxxxz
      yxxxz
      zxxxz
      xyxxz
      yyxxz
      zyxxz
      xzxxz
      yzxxz
      zzxxz
      xxyxz
      yxyxz
      zxyxz
      xyyxz
      yyyxz
      zyyxz
      xzyxz
      yzyxz
      zzyxz
      xxzxz
      yxzxz
      zxzxz
      xyzxz
      yyzxz
      zyzxz
      xzzxz
      yzzxz
      zzzxz
      xxxyz
      yxxyz
      zxxyz
      xyxyz
      yyxyz
      zyxyz
      xzxyz
      yzxyz
      zzxyz
      xxyyz
      yxyyz
      zxyyz
      xyyyz
      yyyyz
      zyyyz
      xzyyz
      yzyyz
      zzyyz
      xxzyz
      yxzyz
      zxzyz
      xyzyz
      yyzyz
      zyzyz
      xzzyz
      yzzyz
      zzzyz
      xxxzz
      yxxzz
      zxxzz
      xyxzz
      yyxzz
      zyxzz
      xzxzz
      yzxzz
      zzxzz
      xxyzz
      yxyzz
      zxyzz
      xyyzz
      yyyzz
      zyyzz
      xzyzz
      yzyzz
      zzyzz
      xxzzz
      yxzzz
      zxzzz
      xyzzz
      yyzzz
      zyzzz
      xzzzz
      yzzzz
      zzzzz
    - : unit = ()


### (B)

Construct a finite automaton **`fa_q2_b`** for the language
consisting of all strings over the alphabet `{x,y,z}` with exactly two `x`
and no `y` in them (and any number of `z`).

Sample output:

    # lang fa_q2_b 6;;
      xx
      zxx
      xzx
      xxz
      zzxx
      zxzx
      xzzx
      zxxz
      xzxz
      xxzz
      zzzxx
      zzxzx
      zxzzx
      xzzzx
      zzxxz
      zxzxz
      xzzxz
      zxxzz
      xzxzz
      xxzzz
      zzzzxx
      zzzxzx
      zzxzzx
      zxzzzx
      xzzzzx
      zzzxxz
      zzxzxz
      zxzzxz
      xzzzxz
      zzxxzz
      zxzxzz
      xzzxzz
      zxxzzz
      xzxzzz
      xxzzzz
    - : unit = ()


### (C)

Construct a finite automaton **`fa_q2_c`** for the language
consisting of all strings over the alphabet `{x,y,z}` with exactly two `x`
and one `y` in them (and any number of `z`).

Sample output:

    # lang fa_q2_c 6;;
      yxx
      xyx
      xxy
      zyxx
      yzxx
      zxyx
      xzyx
      yxzx
      xyzx
      zxxy
      xzxy
      xxzy
      yxxz
      xyxz
      xxyz
      zzyxx
      zyzxx
      yzzxx
      zzxyx
      zxzyx
      xzzyx
      zyxzx
      yzxzx
      zxyzx
      xzyzx
      yxzzx
      xyzzx
      zzxxy
      zxzxy
      xzzxy
      zxxzy
      xzxzy
      xxzzy
      zyxxz
      yzxxz
      zxyxz
      xzyxz
      yxzxz
      xyzxz
      zxxyz
      xzxyz
      xxzyz
      yxxzz
      xyxzz
      xxyzz
      zzzyxx
      zzyzxx
      zyzzxx
      yzzzxx
      zzzxyx
      zzxzyx
      zxzzyx
      xzzzyx
      zzyxzx
      zyzxzx
      yzzxzx
      zzxyzx
      zxzyzx
      xzzyzx
      zyxzzx
      yzxzzx
      zxyzzx
      xzyzzx
      yxzzzx
      xyzzzx
      zzzxxy
      zzxzxy
      zxzzxy
      xzzzxy
      zzxxzy
      zxzxzy
      xzzxzy
      zxxzzy
      xzxzzy
      xxzzzy
      zzyxxz
      zyzxxz
      yzzxxz
      zzxyxz
      zxzyxz
      xzzyxz
      zyxzxz
      yzxzxz
      zxyzxz
      xzyzxz
      yxzzxz
      xyzzxz
      zzxxyz
      zxzxyz
      xzzxyz
      zxxzyz
      xzxzyz
      xxzzyz
      zyxxzz
      yzxxzz
      zxyxzz
      xzyxzz
      yxzxzz
      xyzxzz
      zxxyzz
      xzxyzz
      xxzyzz
      yxxzzz
      xyxzzz
      xxyzzz
    - : unit = ()


### (D)

Construct a finite automaton **`fa_q2_d`** for
the language consisting of all strings over the alphabet
`{x,y,z}` in which there are an odd number
of `x`, an even number of `y`, and any number of `z`.

Sample output:

    # lang fa_q2_d 6;;
      x
      zx
      xz
      xxx
      yyx
      zzx
      yxy
      xyy
      zxz
      xzz
      zxxx
      xzxx
      zyyx
      yzyx
      xxzx
      yyzx
      zzzx
      zyxy
      yzxy
      zxyy
      xzyy
      yxzy
      xyzy
      xxxz
      yyxz
      zzxz
      yxyz
      xyyz
      zxzz
      xzzz
      xxxxx
      yyxxx
      zzxxx
      yxyxx
      xyyxx
      zxzxx
      xzzxx
      yxxyx
      xyxyx
      xxyyx
      yyyyx
      zzyyx
      zyzyx
      yzzyx
      zxxzx
      xzxzx
      zyyzx
      yzyzx
      xxzzx
      yyzzx
      zzzzx
      yxxxy
      xyxxy
      xxyxy
      yyyxy
      zzyxy
      zyzxy
      yzzxy
      xxxyy
      yyxyy
      zzxyy
      yxyyy
      xyyyy
      zxzyy
      xzzyy
      zyxzy
      yzxzy
      zxyzy
      xzyzy
      yxzzy
      xyzzy
      zxxxz
      xzxxz
      zyyxz
      yzyxz
      xxzxz
      yyzxz
      zzzxz
      zyxyz
      yzxyz
      zxyyz
      xzyyz
      yxzyz
      xyzyz
      xxxzz
      yyxzz
      zzxzz
      yxyzz
      xyyzz
      zxzzz
      xzzzz
      zxxxxx
      xzxxxx
      zyyxxx
      yzyxxx
      xxzxxx
      yyzxxx
      zzzxxx
      zyxyxx
      yzxyxx
      zxyyxx
      xzyyxx
      yxzyxx
      xyzyxx
      xxxzxx
      yyxzxx
      zzxzxx
      yxyzxx
      xyyzxx
      zxzzxx
      xzzzxx
      zyxxyx
      yzxxyx
      zxyxyx
      xzyxyx
      yxzxyx
      xyzxyx
      zxxyyx
      xzxyyx
      zyyyyx
      yzyyyx
      xxzyyx
      yyzyyx
      zzzyyx
      yxxzyx
      xyxzyx
      xxyzyx
      yyyzyx
      zzyzyx
      zyzzyx
      yzzzyx
      xxxxzx
      yyxxzx
      zzxxzx
      yxyxzx
      xyyxzx
      zxzxzx
      xzzxzx
      yxxyzx
      xyxyzx
      xxyyzx
      yyyyzx
      zzyyzx
      zyzyzx
      yzzyzx
      zxxzzx
      xzxzzx
      zyyzzx
      yzyzzx
      xxzzzx
      yyzzzx
      zzzzzx
      zyxxxy
      yzxxxy
      zxyxxy
      xzyxxy
      yxzxxy
      xyzxxy
      zxxyxy
      xzxyxy
      zyyyxy
      yzyyxy
      xxzyxy
      yyzyxy
      zzzyxy
      yxxzxy
      xyxzxy
      xxyzxy
      yyyzxy
      zzyzxy
      zyzzxy
      yzzzxy
      zxxxyy
      xzxxyy
      zyyxyy
      yzyxyy
      xxzxyy
      yyzxyy
      zzzxyy
      zyxyyy
      yzxyyy
      zxyyyy
      xzyyyy
      yxzyyy
      xyzyyy
      xxxzyy
      yyxzyy
      zzxzyy
      yxyzyy
      xyyzyy
      zxzzyy
      xzzzyy
      yxxxzy
      xyxxzy
      xxyxzy
      yyyxzy
      zzyxzy
      zyzxzy
      yzzxzy
      xxxyzy
      yyxyzy
      zzxyzy
      yxyyzy
      xyyyzy
      zxzyzy
      xzzyzy
      zyxzzy
      yzxzzy
      zxyzzy
      xzyzzy
      yxzzzy
      xyzzzy
      xxxxxz
      yyxxxz
      zzxxxz
      yxyxxz
      xyyxxz
      zxzxxz
      xzzxxz
      yxxyxz
      xyxyxz
      xxyyxz
      yyyyxz
      zzyyxz
      zyzyxz
      yzzyxz
      zxxzxz
      xzxzxz
      zyyzxz
      yzyzxz
      xxzzxz
      yyzzxz
      zzzzxz
      yxxxyz
      xyxxyz
      xxyxyz
      yyyxyz
      zzyxyz
      zyzxyz
      yzzxyz
      xxxyyz
      yyxyyz
      zzxyyz
      yxyyyz
      xyyyyz
      zxzyyz
      xzzyyz
      zyxzyz
      yzxzyz
      zxyzyz
      xzyzyz
      yxzzyz
      xyzzyz
      zxxxzz
      xzxxzz
      zyyxzz
      yzyxzz
      xxzxzz
      yyzxzz
      zzzxzz
      zyxyzz
      yzxyzz
      zxyyzz
      xzyyzz
      yxzyzz
      xyzyzz
      xxxzzz
      yyxzzz
      zzxzzz
      yxyzzz
      xyyzzz
      zxzzzz
      xzzzzz
    - : unit = ()

### (E)

Construct a finite automaton **`fa_q2_e`** for
the language consisting of all strings over the alphabet
`{x,y,z}` in which there are an odd number
of `x`, an even number of `y`, and exactly one `z`.

Sample output:

    # lang fa_q2_e 6;;
      zx
      xz
      zxxx
      xzxx
      zyyx
      yzyx
      xxzx
      yyzx
      zyxy
      yzxy
      zxyy
      xzyy
      yxzy
      xyzy
      xxxz
      yyxz
      yxyz
      xyyz
      zxxxxx
      xzxxxx
      zyyxxx
      yzyxxx
      xxzxxx
      yyzxxx
      zyxyxx
      yzxyxx
      zxyyxx
      xzyyxx
      yxzyxx
      xyzyxx
      xxxzxx
      yyxzxx
      yxyzxx
      xyyzxx
      zyxxyx
      yzxxyx
      zxyxyx
      xzyxyx
      yxzxyx
      xyzxyx
      zxxyyx
      xzxyyx
      zyyyyx
      yzyyyx
      xxzyyx
      yyzyyx
      yxxzyx
      xyxzyx
      xxyzyx
      yyyzyx
      xxxxzx
      yyxxzx
      yxyxzx
      xyyxzx
      yxxyzx
      xyxyzx
      xxyyzx
      yyyyzx
      zyxxxy
      yzxxxy
      zxyxxy
      xzyxxy
      yxzxxy
      xyzxxy
      zxxyxy
      xzxyxy
      zyyyxy
      yzyyxy
      xxzyxy
      yyzyxy
      yxxzxy
      xyxzxy
      xxyzxy
      yyyzxy
      zxxxyy
      xzxxyy
      zyyxyy
      yzyxyy
      xxzxyy
      yyzxyy
      zyxyyy
      yzxyyy
      zxyyyy
      xzyyyy
      yxzyyy
      xyzyyy
      xxxzyy
      yyxzyy
      yxyzyy
      xyyzyy
      yxxxzy
      xyxxzy
      xxyxzy
      yyyxzy
      xxxyzy
      yyxyzy
      yxyyzy
      xyyyzy
      xxxxxz
      yyxxxz
      yxyxxz
      xyyxxz
      yxxyxz
      xyxyxz
      xxyyxz
      yyyyxz
      yxxxyz
      xyxxyz
      xxyxyz
      yyyxyz
      xxxyyz
      yyxyyz
      yxyyyz
      xyyyyz
    - : unit = ()


* * *


## Question 3: Finite Automata Constructions

In this question, we implement constructions on finite
automata, including some thatcame up in class when we discussed how to
go from regular expressions to finite automata.

The sample outputs use the following simple automata:

    let fa1 = { states = [0; 1; 2];
                alphabet = ['x'; 'y'; 'z'];
                delta = [ (0,'x',1);
                          (1,'x',2);
                          (2,'x',0) ];
                start = 0;
                final = [0] }
        
    let fa2 = { states = [10; 11; 12];
                alphabet = ['x'; 'y'; 'z'];
                delta = [ (10,'y',11);
                          (11,'z',12);
                          (12,'y',11) ];
                start = 10;
                final = [12] }
            
The language of `fa1` is the language described by regular expression
`(xxx)`<sup>\*</sup>, and the language of `fa2` is the language
described by regular expression `(yz)(yz)`<sup>\*</sup>.

You may assume that the alphabets of the machines are the same and
that the set of states of different machines are different. This does
mean that you cannot construct an automaton by combining two instances
of the same automaton. (**To think about**: how would you get around
such a restriction?) The stub functions I provide enforce these assumptions, as you will see.

Because combining automata often requires adding new states, I have provided you with two functions `freshState` and `freshState2` that take respectively one finite automaton and two finite automata and return a state that is _not_ present in the input automata. 


### (A)

Given a language A and a symbol `a`, the language _prepend_(a, A) is the language obtained by prepending `a` to every string in A. For example, if A = {`aab`, `aba`, `b`, `bb`, `bbb`}, then _prepend_(a, A) = { `aaab`, `aaba`, `ab`, `abb`, `abbb` }. 

If you have a finite automaton accepting A, it is easy to transform it into a finite automaton accepting _prepend_(a, A): simply add a new start state to the automaton, and add a transition from the new start state to the old start state labeled `a`. You should convince yourself this construction works.

Code a function **`prependM`** of
type **`fa -> char -> fa`**
where `prependM m a` returns a finite automaton that accepts _prepend_(a, A) where A is the language accepted by `m`. 

Sample output:

    # lang fa1 10;;
      <epsilon>
      xxx
      xxxxxx
      xxxxxxxxx
    - : unit = ()
    # lang fa2 10;;
      yz
      yzyz
      yzyzyz
      yzyzyzyz
      yzyzyzyzyz
    - : unit = ()
    # lang (prependM fa1 'z') 10;;
      z
      zxxx
      zxxxxxx
      zxxxxxxxxx
    - : unit = ()
    # lang (prependM fa2 'z') 10;;
      zyz
      zyzyz
      zyzyzyz
      zyzyzyzyz
    - : unit = ()


### (B)

Given a language A and a _string_ u, the language _prependString_(u, A) is the language obtained by prepending the string `u` to every string in A. For example, if A = {`aab`, `aba`, `b`, `bb`, `bbb`}, then _prependString_(abba, A) = { `abbaaab`, `abbaaba`, `abbab`, `abbabb`, `abbabbb` }. 

Code a function **`prependStringM`** of
type **`fa -> string -> fa`**
where `prependStringM m u` returns a finite automaton that accepts _prependString_(u, A) where A is the language accepted by `m`. 

Sample output:

    # lang fa1 10;;
      <epsilon>
      xxx
      xxxxxx
      xxxxxxxxx
    - : unit = ()
    # lang fa2 10;;
      yz
      yzyz
      yzyzyz
      yzyzyzyz
      yzyzyzyzyz
    - : unit = ()
    # lang (prependStringM fa1 "zxz") 10;;
      zxz
      zxzxxx
      zxzxxxxxx
    - : unit = ()
    # lang (prependStringM fa2 "zxz") 10;;
      zxzyz
      zxzyzyz
      zxzyzyzyz
    - : unit = ()
    # lang (prependStringM fa1 "zz") 10;;
      zz
      zzxxx
      zzxxxxxx
    - : unit = ()
    # lang (prependStringM fa2 "zz") 10;;
      zzyz
      zzyzyz
      zzyzyzyz
      zzyzyzyzyz
    - : unit = ()

**Hint:** notice that _prependString_(abba, A) = _prepend_(a, _prepend_(b, _prepend_(b, _prepend_(a, A)))). Generalize, and use `prependM` from part (A). Or you can do it the hard way, adding N states to the finite automaton, where N is the length of the string being prepended. Whichever works for you.


### (C)

Code a function `replaceSource` of type **`(int * char * int) -> int -> (int * char * int)`** where
`replaceSource delta state` returns a new transition function made up of all the transitions in `delta` in which the source state of each transition has been replaced by `state`. 

Sample output:

    # replaceSource [] 99;;
    - : (int * 'a * 'b) list = []
    # replaceSource [(1, 'a', 2)] 99;;
    - : (int * char * int) list = [(99, 'a', 2)]
    # replaceSource [(1, 'a', 2); (1, 'b', 3)] 99;;
    - : (int * char * int) list = [(99, 'a', 2); (99, 'b', 3)]
    # replaceSource [(1, 'a', 2); (1, 'b', 3); (2, 'a', 2)] 99;;
    - : (int * char * int) list = [(99, 'a', 2); (99, 'b', 3); (99, 'a', 2)]
    # replaceSource fa1.delta 99;;
    - : (int * char * int) list = [(99, 'x', 1); (99, 'x', 2); (99, 'x', 0)]

Order and repetition in the resulting list are irrelevant.



### (D)

Code a function **`transitionsFromState`** of type **`int * (int * char * int) -> (int * char * int)`** where
`transitionsFromState state delta` returns the list of all transitions in `delta` that have `state` as a source state.

Sample output:

    # transitionsFromState 1 [];;
    - : (int * 'a * 'b) list = []
    # transitionsFromState 1 [(1, 'a', 2)];;
    - : (int * char * int) list = [(1, 'a', 2)]
    # transitionsFromState 1 [(1, 'a', 2); (1, 'b', 3)];;
    - : (int * char * int) list = [(1, 'a', 2); (1, 'b', 3)]
    # transitionsFromState 1 [(1, 'a', 2); (1, 'b', 3); (2, 'a', 3)];;
    - : (int * char * int) list = [(1, 'a', 2); (1, 'b', 3)]
    # transitionsFromState 2 [(1, 'a', 2); (1, 'b', 3); (2, 'a', 3)];;
    - : (int * char * int) list = [(2, 'a', 3)]
    # transitionsFromState 3 [(1, 'a', 2); (1, 'b', 3); (2, 'a', 3)];;
    - : (int * char * int) list = []
    # transitionsFromState 1 fa1.delta ;;
    - : (int * char * int) list = [(1, 'x', 2)]
    # transitionsFromState 2 fa1.delta ;;
    - : (int * char * int) list = [(2, 'x', 0)]

Order and repetition in the resulting list are irrelevant.


### (E)

Code a function **`unionM`** of
type **`fa -> fa -> fa`**
where `unionM m1 m2` returns a finite automaton that accepts
the union of the language accepted by `m1` and the language
accepted by `m2`. 

**Hint:** Use the construction detailed in the notes. You will probably benefit from using functions `replaceSource` and `transitionsFromState` from parts (C) and (D) above.

Make sure you handle the cases where either of the two automata
accepts the empty string.

Sample output:

    # lang fa1 10;;
      <epsilon>
      xxx
      xxxxxx
      xxxxxxxxx
    - : unit = ()
    # lang fa2 10;;
      yz
      yzyz
      yzyzyz
      yzyzyzyz
      yzyzyzyzyz
    - : unit = ()
    # lang (unionM fa1 fa2) 10;;
      <epsilon>
      yz
      xxx
      yzyz
      xxxxxx
      yzyzyz
      yzyzyzyz
      xxxxxxxxx
      yzyzyzyzyz
    - : unit = ()
    # lang (unionM fa2 fa1) 10;;
      <epsilon>
      yz
      xxx
      yzyz
      xxxxxx
      yzyzyz
      yzyzyzyz
      xxxxxxxxx
      yzyzyzyzyz
    - : unit = ()



_If you get bored, give a shot to coding functions `concatM` and `starM`, where `contactM m1 m2` returns a finite automaton that accepts the language obtained by concatenating the languages of `m1` and `m2`, and `starM m` returns a finite automaton that accepts the language obtained by "starring" the language of `m`._
