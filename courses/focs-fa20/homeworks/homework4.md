# Homework 4

## Due Date: Thursday, October 21, 2020 (23h59)

- This homework is to be done individually. You may discuss problems
with fellow students, but all submitted work must be entirely your
own, and should not be from any other course, present, past, or
future. If you use a solution from another source you must cite it
&mdash; this includes when that source is someone else helping
you.

- **Please do not post your solutions on a
public website or a public repository like GitHub.**

- All programming is to be done in OCaml v4.

- Code your answers by modifying the file [`homework4.ml`](homework4.ml) provided. Add your **name**, your **email address**, and any **remarks** that you
wish to make to the instructor to the block comment at the head of the file.

- **Please do not change the types in the signature of the
function stubs I provide**. Doing so will make it
impossible to load your code into the testing infrastructure,
and make me unhappy.

- Feel free to define helper functions if you need them.


## Electronic Submission Instructions

- Start a _fresh_  OCaml shell.

- Load your homework code via `#use "homework4.ml";;` to make sure that there are no errors when I will load your code.

- If there are any error, do not submit. I can't test what I can't `#use`.

- When you're ready to submit, send an email with your file
`homework4.ml` as an attachment to `olin.submissions@gmail.com` with
subject _Homework 4 submission_.

* * *

Here is a type for deterministic Turing machines, with integer states:

    type tm = {
        states : int list;
        input_alphabet : char list;
        tape_alphabet : char list;
        left_marker : char;
        blank : char;
        delta : (int * char) -> (int * char * int);   (* 0 = Left, 1 = Right *)
        start : int;
        accept : int;
        reject : int
      }

The definition follows that of Turing machines we've seen in
class. The transition function is given by an _OCaml function_
taking as input a pair _(p,a)_ of the current state and the
symbol in the cell under the pointer and returning a triple _(q, b, dir)_ indicating that
the machine should transition into a state _q_, writing _b_
in the 
cell under the pointer, and then moving the 
pointer in direction _dir_ (left when it is 0, and right when
it is 1).

Recall from homework 3 that in OCaml, like most modern languages
(Python, Javascript, Java), functions are values like any other --
they can be put into lists and records, they can be passed as
arguments to other functions, they can be returned from other
functions. For example, here is a function that takes a one-argument
function as an argument as well as an integer, and applies the
function twice in succession to the integer:

    # let twice (f:int -> int) (x:int):int = f (f x);; 
    val twice : (int -> int) -> int -> int = <fun>
    
    # let double x = 2 * x;;
    val double : int -> int = <fun>
    
    # double 10;;
    - : int = 20
    
    # twice double 10;;
    - : int = 40
    
    # let square x = x * x;;
    val square : int -> int = <fun>
    
    # square 20;;
    - : int = 400
    
    # twice square 20;;
    - : int = 160000

As in previous homeworks, there is a function `explode` that takes a
string and explodes it into a list of its constituent symbols.

As a simple example, here is the code implementing a Turing machine
accepting the regular language &lcub;a<sup>m</sup>b<sup>n</sup> |
m,n&ge;0&rcub;:

let asbs =
  let d inp = (match inp with
               | (1, 'a') -> (1, 'a', 1)
               | (1, 'b') -> (10, 'b', 1)
               | (1, '>') -> (1, '>', 1)
               | (1, '_') -> (777, '_', 1)
               | (10, 'b') -> (10, 'b', 1)
               | (10, '_') -> (777, '_', 1)
               | (777, 'a') -> (777, 'a', 1)
               | (777, 'b') -> (777, 'b', 1)
               | (777, '>') -> (777, '>', 1)
               | (777, '_') -> (777, '_', 1)
               | (_,c) -> (666,c,1))
  in { states = [1; 10; 777; 666];
       input_alphabet = ['a';'b'];
       tape_alphabet = ['a';'b';'_';'>'];
       blank = '_';
       left_marker = '>';
       start = 1;
       accept = 777;
       reject = 666;
       delta = d }

Note the function `d` defined locally and placed inside the record.

I also provided you with further sample Turing machines `anbn`
and 
`anbncn` accepting &lcub;a<sup>n</sup>b<sup>n</sup> | n &ge;
0&rcub; and &lcub;a<sup>n</sup>b<sup>n</sup>c<sup>n</sup> | n&ge;
0&rcub; respectively.  Note that `anbn` uses different symbols for the
left marker and for the blank, to highlight that your simulator should
not bake them in and rather read them from the Turing machine description.

Recall from lecture that a configuration captures a snapshot of the
Turing machine during execution: the current state of a Turing
machine, the content of the tape, and the position of the tape
head. Type `config` is the type of a configuration, defined as
follows:

    type config = {
        state : int;
        tape: char list;
        position: int
      }

Field `state` is the current state, field `tape` is
the content of the tape, and `position` is the position of the
tape head, with 0 as the left-most position on the tape. A
configuration should always include enough symbols in `tape` to
include the given `position`, possibly by padding the end of
the `tape` list with blank symbols (where the blank symbol to use is
given in the Turing machine description).


* * *


## Question 1: Simulating Turing Machines


### (A)

Code a function **`startConfig`** with type **`tm -> string ->
config`** where `startConfig m w` returns the starting configuration 
for Turing machine `m` with `w` as input string.

Sample output:

    # startConfig asbs "";;
    - : config = {state = 1; tape = ['>']; position = 0}
    
    # startConfig asbs "ab";;
    - : config = {state = 1; tape = ['>'; 'a'; 'b']; position = 0}
    
    # startConfig asbs "aaabbbaa";;
    - : config = {state = 1; tape = ['>'; 'a'; 'a'; 'a'; 'b'; 'b'; 'b'; 'a'; 'a']; position = 0}
    
    # startConfig anbn "";;
    - : config = {state = 1; tape = ['|']; position = 0}
    
    # startConfig anbn "aabb";;
    - : config = {state = 1; tape = ['|'; 'a'; 'a'; 'b'; 'b']; position = 0}
    
    # startConfig anbn "aabbaa";;
    - : config = {state = 1; tape = ['|'; 'a'; 'a'; 'b'; 'b'; 'a'; 'a']; position = 0}


### (B)

Code functions **`acceptConfig`** and **`rejectConfig`** each of type
**`tm -> config -> bool`** where `acceptConfig m c` returns
`true` if and only if `c` is an accepting configuration for Turing
machine `m`, and `rejectConfig m c` returns `true` if and only if `c`
is a rejecting configuration for Turing machine `m`.

Sample output:

    # acceptConfig asbs {state = 1; tape = ['_']; position = 0};;
    - : bool = false
    
    # acceptConfig asbs {state = 10; tape = ['>'; 'a'; '_']; position = 0};;
    - : bool = false
    
    # acceptConfig asbs {state = 777; tape = ['b'; '>'; 'a'; '_']; position = 1};;
    - : bool = true
    
    # acceptConfig asbs {state = 666; tape = ['b'; '>'; 'a'; '_']; position = 1};;
    - : bool = false
    
    # rejectConfig asbs {state = 1; tape = ['_']; position = 0};;
    - : bool = false
    
    # rejectConfig asbs {state = 10; tape = ['>'; 'a'; '_']; position = 0};;
    - : bool = false
    
    # rejectConfig asbs {state = 777; tape = ['b'; '>'; 'a'; '_']; position = 1};;
    - : bool = false
    
    # rejectConfig asbs {state = 666; tape = ['b'; '>'; 'a'; '_']; position = 1};;
    - : bool = true


### (C)

Code a function **`replace_nth`** with
type **`'a list -> int -> 'a -> 'a list`**
where `replace_nth xs n x` returns a new
list which looks just like `xs` except the
element in `n`<sup>th</sup> position
is `x`. (The first element of the list is at
position 0.)

If `n` is not a valid position in the list, the list should be returned unchanged.

Sample output:

    # replace_nth [] 0 'x';;
    - : char list = []
    
    # replace_nth ['a'; 'b'; 'c'; 'd'] 0 'x';;
    - : char list = ['x'; 'b'; 'c'; 'd']
    
    # replace_nth ['a'; 'b'; 'c'; 'd'] 1 'x';;
    - : char list = ['a'; 'x'; 'c'; 'd']
    
    # replace_nth ['a'; 'b'; 'c'; 'd'] 2 'x';;
    - : char list = ['a'; 'b'; 'x'; 'd']
    
    # replace_nth ['a'; 'b'; 'c'; 'd'] 3 'x';;
    - : char list = ['a'; 'b'; 'c'; 'x']
    
    # replace_nth [10; 20; 30; 40] 1 99;;
    - : int list = [10; 99; 30; 40]


### (D)

Code a function **`step`** with type **`tm -> config -> config`** where `step m c` returns the configuration obtained by 
taking one step of Turing machine `m` from configuration `c`.
Function `step m` is basically the relation `C` &#8594; `D` described in
lecture.

Sample output:

    # step asbs {state = 1; tape = ['_']; position = 0};;
    - : config = {state = 777; tape = ['_'; '_']; position = 1}
    
    # step asbs {state = 1; tape = ['>'; 'a'; 'b'; 'b']; position = 2};;
    - : config = {state = 10; tape = ['>'; 'a'; 'b'; 'b']; position = 3}
    
    # step asbs {state = 10; tape = ['>'; 'a'; 'a'; 'b']; position = 2};;
    - : config = {state = 666; tape = ['>'; 'a'; 'a'; 'b']; position = 3}
    
    # step asbs {state = 10; tape = ['>'; 'a'; 'b'; 'b']; position = 2};;
    - : config = {state = 10; tape = ['>'; 'a'; 'b'; 'b']; position = 3}
    
    # step anbn {state = 10; tape = ['|'; 'a'; 'b'; '.']; position = 3};;
    - : config = {state = 11; tape = ['|'; 'a'; 'b'; '.'; '.']; position = 4}
    
    # step anbn {state = 11; tape = ['|'; 'a'; 'b'; '.']; position = 3};;
    - : config = {state = 11; tape = ['|'; 'a'; 'b'; '.']; position = 2}
    
    # step anbn {state = 12; tape = ['|'; 'a'; 'b']; position = 1};;
    - : config = {state = 13; tape = ['|'; 'X'; 'b']; position = 2}
    
    # step anbn { state = 13; tape = ['|'; 'X'; 'b']; position = 2};;
    - : config = {state = 11; tape = ['|'; 'X'; 'X'; '.']; position = 3}


### (E)

Code a function **`run`** with type **`tm -> string -> bool`**
where `run m w` returns `true` if `m` accepts input string
`w`, and returns `false` if `m` rejects input string `w`. It'd be nice
(but not required) if your function also prints the sequence of
configurations that Turing machine `m` goes through during its
computation. This is _really_ going to help you debug your Turing
machines in the following questions.

I provided you with a function `printConfig` that prints a configuration (taking as inputs a Turing machine and a configuration to print).  The
easiest way to use it is in a `let`, as follows:

    let _ = printConfig tm c in
      <some value to compute and return>
  
This code excerpt will print configuration `c` of Turing machine `tm`
and then evaluate `<some value to compute and return>`, as usual.

Sample output:

    # run asbs "aab";;
    1    [>] a  a  b 
    1     > [a] a  b 
    1     >  a [a] b 
    1     >  a  a [b]
    10    >  a  a  b [_]
    777   >  a  a  b  _ [_]
    - : bool = true
    
    # run anbn "aabb";;
    1    [|] a  a  b  b 
    1     | [a] a  b  b 
    1     |  a [a] b  b 
    1     |  a  a [b] b 
    10    |  a  a  b [b]
    10    |  a  a  b  b [.]
    11    |  a  a  b  b  . [.]
    11    |  a  a  b  b [.] . 
    11    |  a  a  b [b] .  . 
    11    |  a  a [b] b  .  . 
    11    |  a [a] b  b  .  . 
    11    | [a] a  b  b  .  . 
    11   [|] a  a  b  b  .  . 
    12    | [a] a  b  b  .  . 
    13    |  X [a] b  b  .  . 
    13    |  X  a [b] b  .  . 
    11    |  X  a  X [b] .  . 
    11    |  X  a [X] b  .  . 
    11    |  X [a] X  b  .  . 
    11    | [X] a  X  b  .  . 
    11   [|] X  a  X  b  .  . 
    12    | [X] a  X  b  .  . 
    12    |  X [a] X  b  .  . 
    13    |  X  X [X] b  .  . 
    13    |  X  X  X [b] .  . 
    11    |  X  X  X  X [.] . 
    11    |  X  X  X [X] .  . 
    11    |  X  X [X] X  .  . 
    11    |  X [X] X  X  .  . 
    11    | [X] X  X  X  .  . 
    11   [|] X  X  X  X  .  . 
    12    | [X] X  X  X  .  . 
    12    |  X [X] X  X  .  . 
    12    |  X  X [X] X  .  . 
    12    |  X  X  X [X] .  . 
    12    |  X  X  X  X [.] . 
    777   |  X  X  X  X  . [.]
    - : bool = true
    
    # run anbncn "aabbcc";;
    1    [>] a  a  b  b  c  c 
    1     > [a] a  b  b  c  c 
    1     >  a [a] b  b  c  c 
    1     >  a  a [b] b  c  c 
    10    >  a  a  b [b] c  c 
    10    >  a  a  b  b [c] c 
    15    >  a  a  b  b  c [c]
    15    >  a  a  b  b  c  c [_]
    11    >  a  a  b  b  c  c  _ [_]
    11    >  a  a  b  b  c  c [_] _ 
    11    >  a  a  b  b  c [c] _  _ 
    11    >  a  a  b  b [c] c  _  _ 
    11    >  a  a  b [b] c  c  _  _ 
    11    >  a  a [b] b  c  c  _  _ 
    11    >  a [a] b  b  c  c  _  _ 
    11    > [a] a  b  b  c  c  _  _ 
    11   [>] a  a  b  b  c  c  _  _ 
    12    > [a] a  b  b  c  c  _  _ 
    13    >  X [a] b  b  c  c  _  _ 
    13    >  X  a [b] b  c  c  _  _ 
    14    >  X  a  X [b] c  c  _  _ 
    14    >  X  a  X  b [c] c  _  _ 
    11    >  X  a  X  b  X [c] _  _ 
    11    >  X  a  X  b [X] c  _  _ 
    11    >  X  a  X [b] X  c  _  _ 
    11    >  X  a [X] b  X  c  _  _ 
    11    >  X [a] X  b  X  c  _  _ 
    11    > [X] a  X  b  X  c  _  _ 
    11   [>] X  a  X  b  X  c  _  _ 
    12    > [X] a  X  b  X  c  _  _ 
    12    >  X [a] X  b  X  c  _  _ 
    13    >  X  X [X] b  X  c  _  _ 
    13    >  X  X  X [b] X  c  _  _ 
    14    >  X  X  X  X [X] c  _  _ 
    14    >  X  X  X  X  X [c] _  _ 
    11    >  X  X  X  X  X  X [_] _ 
    11    >  X  X  X  X  X [X] _  _ 
    11    >  X  X  X  X [X] X  _  _ 
    11    >  X  X  X [X] X  X  _  _ 
    11    >  X  X [X] X  X  X  _  _ 
    11    >  X [X] X  X  X  X  _  _ 
    11    > [X] X  X  X  X  X  _  _ 
    11   [>] X  X  X  X  X  X  _  _ 
    12    > [X] X  X  X  X  X  _  _ 
    12    >  X [X] X  X  X  X  _  _ 
    12    >  X  X [X] X  X  X  _  _ 
    12    >  X  X  X [X] X  X  _  _ 
    12    >  X  X  X  X [X] X  _  _ 
    12    >  X  X  X  X  X [X] _  _ 
    12    >  X  X  X  X  X  X [_] _ 
    777   >  X  X  X  X  X  X  _ [_]
    - : bool = true
    
    # run anbn "aabbbb";;
    1    [|] a  a  b  b  b  b 
    1     | [a] a  b  b  b  b 
    1     |  a [a] b  b  b  b 
    1     |  a  a [b] b  b  b 
    10    |  a  a  b [b] b  b 
    10    |  a  a  b  b [b] b 
    10    |  a  a  b  b  b [b]
    10    |  a  a  b  b  b  b [.]
    11    |  a  a  b  b  b  b  . [.]
    11    |  a  a  b  b  b  b [.] . 
    11    |  a  a  b  b  b [b] .  . 
    11    |  a  a  b  b [b] b  .  . 
    11    |  a  a  b [b] b  b  .  . 
    11    |  a  a [b] b  b  b  .  . 
    11    |  a [a] b  b  b  b  .  . 
    11    | [a] a  b  b  b  b  .  . 
    11   [|] a  a  b  b  b  b  .  . 
    12    | [a] a  b  b  b  b  .  . 
    13    |  X [a] b  b  b  b  .  . 
    13    |  X  a [b] b  b  b  .  . 
    11    |  X  a  X [b] b  b  .  . 
    11    |  X  a [X] b  b  b  .  . 
    11    |  X [a] X  b  b  b  .  . 
    11    | [X] a  X  b  b  b  .  . 
    11   [|] X  a  X  b  b  b  .  . 
    12    | [X] a  X  b  b  b  .  . 
    12    |  X [a] X  b  b  b  .  . 
    13    |  X  X [X] b  b  b  .  . 
    13    |  X  X  X [b] b  b  .  . 
    11    |  X  X  X  X [b] b  .  . 
    11    |  X  X  X [X] b  b  .  . 
    11    |  X  X [X] X  b  b  .  . 
    11    |  X [X] X  X  b  b  .  . 
    11    | [X] X  X  X  b  b  .  . 
    11   [|] X  X  X  X  b  b  .  . 
    12    | [X] X  X  X  b  b  .  . 
    12    |  X [X] X  X  b  b  .  . 
    12    |  X  X [X] X  b  b  .  . 
    12    |  X  X  X [X] b  b  .  . 
    12    |  X  X  X  X [b] b  .  . 
    666   |  X  X  X  X  b [b] .  . 
    - : bool = false

You don't have to match this output exactly. The only important thing really is the final Boolean result. 


* * *


## Question 2: Constructing Turing Machines

In this question and the next, you will construct Turing machines. You
can test your Turing machines with the code you wrote in Question
1. 

For submission purposes, I ask you to define those Turing machines as
constants using `let` with a fixed name given below in each
question. There is already a placeholder in `homework4.ml` for those
answers. Just replace the placeholder with your definition.



### (A)

Construct a total Turing machine **`tm_q2_ab3`** that accepts the language
consisting of all strings over the alphabet `{a,b}` of the form
`a`<sup>n</sup>`b`<sup>3n</sup> for any n&ge;0.

Sample output (without showing the printed configurations):

    # run tm_q2_ab3 "";;
    start  [>]
    - : bool = true
    
    # run tm_q2_ab3 "abbb";;
    start  [>] a  b  b  b
    - : bool = true
    
    # run tm_q2_ab3 "aabbbbbb";;
    start  [>] a  a  b  b  b  b  b  b
    - : bool = true
    
    # run tm_q2_ab3 "aaabbbbbbbbb";;
    start  [>] a  a  a  b  b  b  b  b  b  b  b  b
    - : bool = true
    
    # run tm_q2_ab3 "a";;
    start  [>] a
    - : bool = false
    
    # run tm_q2_ab3 "ab";;
    start  [>] a  b 
    - : bool = false
    
    # run tm_q2_ab3 "abb";;
    start  [>] a  b  b
    - : bool = false
    
    # run tm_q2_ab3 "aabbb";;
    start  [>] a  a  b  b  b
    - : bool = false
    
    # run tm_q2_ab3 "b";;
    start  [>] b   
    - : bool = false
    
    # run tm_q2_ab3 "bb";;
    start  [>] b  b
    - : bool = false
    
    # run tm_q2_ab3 "bbba";;
    start  [>] b  b  b  a
    - : bool = false


### (B)

Construct a total Turing machine **`tm_q2_not`** that accepts the
language consisting of all strings over the alphabet `{0,1,#}` of the
form _u_`#`_v_, where _u_ and _v_ are nonempty strings over `{0,1}`
all of the same length such that _v_ is the pointwise NOT of _u_: if
the _i_th bit of _u_ is _b_ then the _i_th bit of _v_ is _¬b_, where
¬0 is 1 and ¬1 is 0.

Sample output (without showing configurations):

    # run tm_q2_not "0#1";;
    start  [>] 0  #  1
    - : bool = true
    
    # run tm_q2_not "000#111";;
    start  [>] 0  0  0  #  1  1  1
    - : bool = true
    
    # run tm_q2_not "010#101";;
    start  [>] 0  1  0  #  1  0  1
    - : bool = true
    
    # run tm_q2_not "010101#101010";;
    start  [>] 0  1  0  1  0  1  #  1  0  1  0  1  0
    - : bool = true
    
    # run tm_q2_not "000111000#111000111";;
    start  [>] 0  0  0  1  1  1  0  0  0  #  1  1  1  0  0  0  1  1  1
    - : bool = true
    
    # run tm_q2_not "0#1#01";;
    start  [>] 0  #  1  #  0  1
    - : bool = false
    
    # run tm_q2_not "0#11";;
    start  [>] 0  #  1  1
    - : bool = false
    
    # run tm_q2_not "00#1";;
    start  [>] 0  0  #  1
    - : bool = false
    
    # run tm_q2_not "1#1";;
    start  [>] 1  #  1
    - : bool = false
    
    # run tm_q2_not "0#0";;
    start  [>] 0  #  0
    - : bool = false
    
    # run tm_q2_not "010#100";;
    start  [>] 0  1  0  #  1  0  0
    - : bool = false
    
    # run tm_q2_not "010#001";;
    start  [>] 0  1  0  #  0  0  1
    - : bool = false


### (C)

Construct a total Turing machine **`tm_q2_and`** that accepts the
language consisting of all strings over the alphabet `{0,1,#}` of the
form _u_`#`_v_`#`_w_ where _u_, _v_, and _w_ are nonempty strings over
`{0,1}` all of the same length such that _w_ is the pointwise AND of
_u_ and _v_: if the _i_th bit of _u_ is _b1_ and the _i_th bit of _v_
is _b2_ then the _i_th bit of _w_ is _b1 ∧ b2_, where _x ∧ y_ is 1
when both _x_ and _y_ are 1, and 0 otherwise.

Sample output (without showing configurations):

    # run tm_q2_and "0#0#0";;
    start  [>] 0  #  0  #  0
    - : bool = true
    
    # run tm_q2_and "0#1#0";;
    start  [>] 0  #  1  #  0
    - : bool = true
    
    # run tm_q2_and "1#0#0";;
    start  [>] 1  #  0  #  0
    - : bool = true
    
    # run tm_q2_and "1#1#1";;
    start  [>] 1  #  1  #  1
    - : bool = true
    
    # run tm_q2_and "101#010#000";;
    start  [>] 1  0  1  #  0  1  0  #  0  0  0
    - : bool = true
    
    # run tm_q2_and "1110#0111#0110";;
    start  [>] 1  1  1  0  #  0  1  1  1  #  0  1  1  0
    - : bool = true
    
    # run tm_q2_and "0#0"
    start  [>] 0  #  0
    - : bool = false
    
    # run tm_q2_and "0#01#1"
    start  [>] 0  #  0  1  #  1
    - : bool = false
    
    # run tm_q2_and "00#11#11"
    start  [>] 0  0  #  1  1  #  1  1
    - : bool = false
    
    # run tm_q2_and "001#001#000"
    start  [>] 0  0  1  #  0  0  1  #  0  0  0
    - : bool = false
    
    # run tm_q2_and "001#001#011"
    start  [>] 0  0  1  #  0  0  1  #  0  1  1
    - : bool = false


### (D)

Construct a total Turing machine **`tm_q2_plus1`** that accepts the
language consisting of all strings over the alphabet `{0,1,#}` of the
form _u_`#`_v_ where _u_ and _v_ are nonempty
strings over `{0,1}` all of the same length such that _u + 1 = v_ when
viewed as binary numbers.

(Brownie points if you can construct the Turing machine so that _u_
and _v_ need not be of the same length, but that's not required.)

Sample output (without showing configurations):

    # run tm_q2_plus1 "0#1";;
    start  [>] 0  #  1
    - : bool = true
    
    # run tm_q2_plus1 "00#01";;
    start  [>] 0  0  #  0  1
    - : bool = true
    
    # run tm_q2_plus1 "000#001";;
    start  [>] 0  0  0  #  0  0  1
    - : bool = true
    
    # run tm_q2_plus1 "010#011";;
    start  [>] 0  1  0  #  0  1  1
    - : bool = true
    
    # run tm_q2_plus1 "011#100";;
    start  [>] 0  1  1  #  1  0  0
    - : bool = true
    
    # run tm_q2_plus1 "100#101";;
    start  [>] 1  0  0  #  1  0  1
    - : bool = true
    
    # run tm_q2_plus1 "101#110";;
    start  [>] 1  0  1  #  1  1  0
    - : bool = true
    
    # run tm_q2_plus1 "110#111";;
    start  [>] 1  1  0  #  1  1  1
    - : bool = true
    
    # run tm_q2_plus1 "101010#101011";;
    start  [>] 1  0  1  0  1  0 #  1  0  1  0  1  1
    - : bool = true
    
    # run tm_q2_plus1 "101111#110000";;
    start  [>] 1  0  1  1  1  1 #  1  1  0  0  0  0
    - : bool = true
    
    # run tm_q2_plus1 "0#1#01";;
    start  [>] 0  #  1  #  0  1
    - : bool = false
    
    # run tm_q2_plus1 "0#11";;
    start  [>] 0  #  1  1
    - : bool = false
    
    # run tm_q2_plus1 "00#1";;
    start  [>] 0  0  #  1
    - : bool = false
    
    # run tm_q2_plus1 "1#1";;
    start  [>] 1  #  1
    - : bool = false
    
    # run tm_q2_plus1 "0#0";;
    start  [>] 0  #  0
    - : bool = false
    
    # run tm_q2_plus1 "010#101";;
    start  [>] 0  1  0  #  1  0  1
    - : bool = false


### (E) (Slight more challenging)

Construct a total Turing machine **`tm_q2_duplicate`** that accepts the
language consisting of all strings over the alphabet `{a,b}` of the
form _ww_ for any string _w_. In other words, `tm_q2_duplicate`
accepts any string that is made up of two consecutive copies of the same sequence of characters.

**Hint**: One way to do this is in two multi-pass steps: (1) replace
every `a` and `b` in the first half of the input string by two new
symbols (for instance, `A` and `B`) and every `a` and `b` in the
second half of the input string by two new symbols (for instance, `C`
and `D`) -- can you do this without needing the find the middle of the
string?; (2) match every `A` and `C` and every `B` and `D`, in order.

Sample output (without showing configurations):

    # run tm_q2_duplicate "";;
    start  [>]
    - : bool = true
    
    # run tm_q2_duplicate "aa";;
    start  [>] a  a
    - : bool = true
    
    # run tm_q2_duplicate "bb";;
    start  [>] b  b
    - : bool = true
    
    # run tm_q2_duplicate "aaaa";;
    start  [>] a  a  a  a
    - : bool = true
    
    # run tm_q2_duplicate "abab";;
    start  [>] a  b  a  b
    - : bool = true
    
    # run tm_q2_duplicate "abaaba";;
    start  [>] a  b  a  a  b  a
    - : bool = true
    
    # run tm_q2_duplicate "aabaaaba";;
    start  [>] a  a  b  a  a  a  b  a
    - : bool = true
    
    # run tm_q2_duplicate "baabbbaaaabaabbbaaaa";;
    start  [>] b  a  a  b  b  b  a  a  a  a  b  a  a  b  b  b  a  a  a  a
    - : bool = true
    
    # run tm_q2_duplicate "a";;
    start  [>] a
    - : bool = false
    
    # run tm_q2_duplicate "b";;
    start  [>] b
    - : bool = false
    
    # run tm_q2_duplicate "aba";;
    start  [>] a  b  a
    - : bool = false
    
    # run tm_q2_duplicate "abbab";;
    start  [>] a  b  b  a  b
    - : bool = false
    
    # run tm_q2_duplicate "abba";;
    start  [>] a  b  b  a
    - : bool = false
    
    # run tm_q2_duplicate "ababa";;
    start  [>] a  b  a  b  a
    - : bool = false
    
    # run tm_q2_duplicate "ababb";;
    start  [>] a  b  a  b  b
    - : bool = false
    
    
    # run tm_q2_duplicate "abbaba";;
    start  [>] a  b  b  a  b  a
    - : bool = false
