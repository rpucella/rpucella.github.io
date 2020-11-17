# Homework 7

## Due Date: Sunday, November 29, 2020 (23h59)

- This homework is to be done individually. You may discuss problems
with fellow students, but all submitted work must be entirely your
own, and should not be from any other course, present, past, or
future. If you use a solution from another source you must cite it
&mdash; this includes when that source is someone else helping
you.

- **Please do not post your solutions on a
public website or a public repository like GitHub.**

- All programming is to be done in OCaml v4.

- Code your answers by modifying the file
[`homework7.ml`](homework7.ml) provided. Add your **name**, your
**email address**, and any **remarks** that you wish to make to the
instructor to the block comment at the head of the file.

- **Please do not change the types in the signature of the
function stubs I provide**. Doing so will make it
impossible to load your code into the testing infrastructure,
and make me unhappy.

- Feel free to define helper functions if you need them.


## Electronic Submission Instructions

- Start a _fresh_  OCaml shell.

- Load your homework code via `#use "homework7.ml";;` to make sure that there will be no errors when I load your code.

- If there are any error, do not submit. I can't test what I can't `#use`.

- When you're ready to submit, send an email with your file
`homework7.ml` as an attachment to `olin.submissions@gmail.com` with
subject _Homework 7 submission_.


* * *

This homework is about the lambda calculus. I've implemented
some code for you to simplify lambda-calculus terms using the
rules we saw in class. The result is not fast, but it
works.

A term is just a string in OCaml. The syntax of terms is
the one we saw in class, adapted to the ASCII character set:

- An identifier can be any sequence of alphanumeric
  characters (including `_`, and starting with a
  letter), such as `x` or `arg_2`
- An abstraction is written `<identifier -> M>`
  where `M` is a term, such
  as `<x -> x>` or `<arg -> plus arg arg>`
- An application is written `M N`, where `M`
  and `N` are terms</li>
- Terms can be wrapped in parentheses, such
  as `(<x -> x>)` or `(ident1 <y -> y>)`. Without parentheses, applications associate to the left, so that `M N O P` parses as `((M N) O) P`. 


OCaml lets you write multi-line strings, which is super convenient if you want to structure a longer term such as :

    # let t = "<x -> 
                 <y -> 
                   <z -> (iszero x)
                           z 
                           y>>>";;
    val t : string =
      "<x -> \n           <y -> \n             <z -> (iszero x)\n                   z \n                     y>>>"

The extra whitespace won't affect the simplification rules.
      
To simplify terms, I implemented a function `simplify`
that repeatedly simplifies redexes until the term reaches a normal
form. (If a normal form is not reached after 5000
steps, you'll get an error.)

    # simplify [] "<x -> x>";;
    Term already in normal form
    - : string = "<x -> x>"
    # simplify [] "<x -> x> z";;
    - : string = "z"
    # simplify [] "<x -> <y -> y y> x> z";;
    - : string = "z z"

A variant of `simplify` called `simplify_verbose`
reports all intermediate terms that arise during a
simplification. That can be quite helpful for debugging.

    # simplify_verbose [] "<x -> <y -> y y> x> z";;
       <x -> <y -> y y> x> z
     = <y -> y y> z
     = z z
    - : string = "z z"

The first argument to `simplify`
and `simplify_verbose` is a list of abbreviations that
can be used in the term you want to simplify.?Recall that when
I presented the lambda calculus in class, I started defining
terms as abbreviations, such as `true`
and `false`, to help us when writing down more
interesting terms. The first argument to `simplify`
and `simplify_verbose` is a sequence of definitions that
you can use in your term being simplified, each of the
form _(name,def)_ where _name_ is the name of the term
being defined, and _def_ is the definition. For example,
here are some definitions:

    # let my_defs = [
        ("plus2", "<n -> succ (succ n)>");
        ("plus4", "<n -> plus2 (plus2 n)>")
      ];;
    val my_defs : (string * string) list =
      [("plus2", "<n -> succ (succ n)>"); ("plus4", "<n -> plus2 (plus2 n)>")]
    # simplify my_defs "plus4 _1";;
    - : string = "<f -> <x -> f (f (f (f (f x))))>>"

By default, and to make your life easier, `simplify` and `simplify_verbose` 
always add the definitions of stuff we did in class (including natural number
arithmetic, complete with a predecessor function) to whatever definitions you provide, so that they are available to you without you needing to do anything special:

    let default_defs = [ 
       ("true", "<x -> <y -> x>>");
       ("false", "<x -> <y -> y>>");
       ("if", "<c -> <x -> <y -> c x y>>>");
       ("and", "<a -> <b -> if a b false>>");
       ("or", "<a -> <b -> if a true b>>");
       ("not", "<a -> if a false true>");
       
       ("_0","<f -> <x -> x>>");
       ("_1","<f -> <x -> f x>>");
       ("_2","<f -> <x -> f (f x)>>");
       ("_3","<f -> <x -> f (f (f x))>>");
       ("_4","<f -> <x -> f (f (f (f x)))>>");
       ("_5","<f -> <x -> f (f (f (f (f x))))>>");
       ("succ","<n -> <f -> <x -> (n f) (f x)>>>");
       ("plus","<m -> <n -> <f -> <x -> (m f) (n f x)>>>>");
       ("times","<m -> <n -> <f -> <x -> m (n f) x>>>>");
       ("iszero","<n -> n <x -> false> true>");
       ("pred","<n -> <f -> <x -> n <g -> <h -> h (g f)>> <u -> x> <u -> u>>>>");
    
       ("pair","<x -> <y -> <s -> s x y>>>");
       ("first", "<p -> p <x -> <y -> x>>>");
       ("second", "<p -> p <x -> <y -> y>>>");
       
       ("empty", "<p -> first p>");
       ("cons","<x -> <y -> <p -> (second p) x y>>>");

       ("Theta", "<x -> <y -> y ((x x) y)>> <x -> <y -> y ((x x) y)>>");
       ("sumto","Theta <f -> <n -> (iszero n) _0 (plus n (f (pred n)))>>")
    ]

**Please do not modify `default_defs`. I will be testing your code with my own definition of `default_defs`, and I will not see the changes you've made. This will probably make my tests fail on your code.**
    
You'll note that you have only the first 6 natural numbers defined
as literals. This means that if you want to use natural number 8 somewhere, for instance, you will either
have to create it by hand, or use the arithmetic operations:

    # simplify_verbose [] "plus _4 _4";;
       <m -> <n -> <f -> <x -> m f (n f x)>>>> <f -> <x -> f (f (f (f x)))>> <f -> <x -> f (f (f (f x)))>>
     = <n -> <f -> <x -> <f -> <x -> f (f (f (f x)))>> f (n f x)>>> <f -> <x -> f (f (f (f x)))>>
     = <f -> <x -> <f -> <x -> f (f (f (f x)))>> f (<f -> <x -> f (f (f (f x)))>> f x)>>
     = <f -> <x -> <x -> f (f (f (f x)))> (<f -> <x -> f (f (f (f x)))>> f x)>>
     = <f -> <x -> f (f (f (f (<f -> <x -> f (f (f (f x)))>> f x))))>>
     = <f -> <x -> f (f (f (f (<x -> f (f (f (f x)))> x))))>>
     = <f -> <x -> f (f (f (f (f (f (f (f x)))))))>>
    - : string = "<f -> <x -> f (f (f (f (f (f (f (f x)))))))>>"

When you simplify a term, all abbreviations are expanded out using their definition
before simplification occurs. This means that when you get 
the simplified term back, it will be expressed in
terms of the expanded out definitions. Thus, if you want to check
that the number 8 you create above is zero or not, you'll get
the resulting `false` as `<x -> <y ->y>>`:

    # simplify_verbose [] "iszero (plus _4 _4)";;
       <n -> n <x -> <x -> <y -> y>>> <x -> <y -> x>>> (<m -> <n -> <f -> <x -> m f (n f x)>>>> <f -> <x -> f (f (f (f x)))>> <f -> <x -> f (f (f (f x)))>>)
     = <m -> <n -> <f -> <x -> m f (n f x)>>>> <f -> <x -> f (f (f (f x)))>> <f -> <x -> f (f (f (f x)))>> <x -> <x -> <y -> y>>> <x -> <y -> x>>
     = <n -> <f -> <x -> <f -> <x -> f (f (f (f x)))>> f (n f x)>>> <f -> <x -> f (f (f (f x)))>> <x -> <x -> <y -> y>>> <x -> <y -> x>>
     = <f -> <x -> <f -> <x -> f (f (f (f x)))>> f (<f -> <x -> f (f (f (f x)))>> f x)>> <x -> <x -> <y -> y>>> <x -> <y -> x>>
     = <x -> <f -> <x -> f (f (f (f x)))>> <x -> <x -> <y -> y>>> (<f -> <x -> f (f (f (f x)))>> <x -> <x -> <y -> y>>> x)> <x -> <y -> x>>
     = <f -> <x -> f (f (f (f x)))>> <x -> <x -> <y -> y>>> (<f -> <x -> f (f (f (f x)))>> <x -> <x -> <y -> y>>> <x -> <y -> x>>)
     = <x -> <x -> <x -> <y -> y>>> (<x -> <x -> <y -> y>>> (<x -> <x -> <y -> y>>> (<x -> <x -> <y -> y>>> x)))> (<f -> <x -> f (f (f (f x)))>> <x -> <x -> <y -> y>>> <x -> <y -> x>>)
     = <x -> <x -> <y -> y>>> (<x -> <x -> <y -> y>>> (<x -> <x -> <y -> y>>> (<x -> <x -> <y -> y>>> (<f -> <x -> f (f (f (f x)))>> <x -> <x -> <y -> y>>> <x -> <y -> x>>))))
     = <x -> <y -> y>>
    - : string = "<x -> <y -> y>>"  

Don't worry, by the time you're done with this homework, your brain will be rewired so that you'll read `<x -> <y -> y>>` as `false` and `<x -> <y -> x>>` as `true`. You're welcome.


A tricky point about definitions: every definition in the list of
definitions can only refer to previously defined terms. This means two things. First, that you can't write recursive definitions.
If you want recursion, you need to use the &Theta; combinator &mdash; see `sumto` in the default definitions for an example. It also means that the order
of definitions is important, just like in OCaml. What's tricky is that
if you mess it up, the simplification process will not complain. An undefined identifier
is just that, an identifier, and it is passed around as an identifier. This makes debugging
somewhat painful. For instance, suppose you mistakenly wrote `pluss` instead of `plus` in the previous example:</p>

    # simplify_verbose [] "iszero (pluss _4 _4)";;
       <n -> n <x -> <x -> <y -> y>>> <x -> <y -> x>>> (pluss <f -> <x -> f (f (f (f x)))>> <f -> <x -> f (f (f (f x)))>>)
     = pluss <f -> <x -> f (f (f (f x)))>> <f -> <x -> f (f (f (f x)))>> <x -> <x -> <y -> y>>> <x -> <y -> x>>
    - : string =
    "pluss <f -> <x -> f (f (f (f x)))>> <f -> <x -> f (f (f (f x)))>> <x -> <x -> <y -> y>>> <x -> <y -> x>>"   
    
Yeah, that's not great. The clue that something went wrong is the `pluss` there at the beginning.

All the questions below ask you to create definitions. **You will add those definitions to
the list `q123_defs`** &mdash; there are already placeholders that you will need to replace. That's the list I will test during grading. 
All the helper functions needed by your definitions should also be in
that list. Feel free to add as many helper functions as you need.


* * *


## Question 1: Encodings


### (A)

Code a term **`xor`** which takes two Boolean arguments _a_ and
_b_ and returns `true` when exactly one of _a_ or _b_
is true, and returns `false` otherwise.
        
    # simplify q123_defs "xor true true";;
    - : string = "<x -> <y -> y>>"
    # simplify q123_defs "xor true false";;
    - : string = "<x -> <y -> x>>"
    # simplify q123_defs "xor false true";;
    - : string = "<x -> <y -> x>>"
    # simplify q123_defs "xor false false";;
    - : string = "<x -> <y -> y>>"


### (B)

Code a term **`minus`** which takes two Church numeral arguments
_m_ and _n_ and returns _m-n_ if _m_ is greater
than _n_, and 0 otherwise. 
        
    # simplify q123_defs "minus _3 _0";;
    - : string = "<f -> <x -> f (f (f x))>>"
    # simplify q123_defs "minus _3 _2";;
    - : string = "<f -> <x -> f x>>"
    # simplify q123_defs "minus _3 _3";;
    - : string = "<f -> <x -> x>>"
    # simplify q123_defs "minus _3 _4";;
    - : string = "<f -> <x -> x>>"
    # simplify q123_defs "minus _0 _0";;
    - : string = "<f -> <x -> x>>"
    # simplify q123_defs "minus _0 _2";;
    - : string = "<f -> <x -> x>>"

**Hint:** Observe that `pred 0` is `0`.
        

### (C)
      
Code a term **`ge`** which takes two Church numeral arguments _m_ and _n_ and returns `true` when _m_ is greater than or equal to _n_, and `false` otherwise.

Code a term **`gt`** which takes two Church numeral
arguments _m_ and _n_ and returns `true` when
_m_ is greater than (but not equal to) _n_, and `false` otherwise.

    # simplify q123_defs "ge _3 _0";;
    - : string = "<x -> <y -> x>>"
    # simplify q123_defs "ge _3 _2";;
    - : string = "<x -> <y -> x>>"
    # simplify q123_defs "ge _3 _3";;
    - : string = "<x -> <y -> x>>"
    # simplify q123_defs "ge _3 _4";;
    - : string = "<x -> <y -> y>>"
    # simplify q123_defs "ge _3 _5";;
    - : string = "<x -> <y -> y>>"
    # simplify q123_defs "ge _0 _0";;
    - : string = "<x -> <y -> x>>"
    # simplify q123_defs "ge _0 _1";;
    - : string = "<x -> <y -> y>>"
            
    # simplify q123_defs "gt _3 _0";;
    - : string = "<x -> <y -> x>>"
    # simplify q123_defs "gt _3 _2";;
    - : string = "<x -> <y -> x>>"
    # simplify q123_defs "gt _3 _3";;
    - : string = "<x -> <y -> y>>"
    # simplify q123_defs "gt _3 _4";;
    - : string = "<x -> <y -> y>>"
    # simplify q123_defs "gt _3 _5";;
    - : string = "<x -> <y -> y>>"
    # simplify q123_defs "gt _0 _0";;
    - : string = "<x -> <y -> y>>"
    # simplify q123_defs "gt _0 _1";;
    - : string = "<x -> <y -> y>>"
    # simplify q123_defs "gt _1 _0";;
    - : string = "<x -> <y -> x>>"


### (D)

Code a term **`max`** which takes two Church numeral arguments _m_ and _n_ and returns the larger of the two.

Code a term **`min`** which takes two Church numeral arguments _m_ and _n_ and returns the smaller of the two.

    # simplify q123_defs "max _3 _0";;
    - : string = "<f -> <x -> f (f (f x))>>"
    # simplify q123_defs "max _3 _2";;
    - : string = "<f -> <x -> f (f (f x))>>"
    # simplify q123_defs "max _3 _3";;
    - : string = "<f -> <x -> f (f (f x))>>"
    # simplify q123_defs "max _3 _4";;
    - : string = "<f -> <x -> f (f (f (f x)))>>"
    # simplify q123_defs "max _3 _5";;
    - : string = "<f -> <x -> f (f (f (f (f x))))>>"
    # simplify q123_defs "max _0 _0";;
    - : string = "<f -> <x -> x>>"
    # simplify q123_defs "max _0 _1";;
    - : string = "<f -> <x -> f x>>"
    # simplify q123_defs "max _1 _0";;
    - : string = "<f -> <x -> f x>>"
    
    # simplify q123_defs "min _3 _0";;
    - : string = "<f -> <x -> x>>"
    # simplify q123_defs "min _3 _2";;
    - : string = "<f -> <x -> f (f x)>>"
    # simplify q123_defs "min _3 _3";;
    - : string = "<f -> <x -> f (f (f x))>>"
    # simplify q123_defs "min _3 _4";;
    - : string = "<f -> <x -> f (f (f x))>>"
    # simplify q123_defs "min _3 _5";;
    - : string = "<f -> <x -> f (f (f x))>>"
    # simplify q123_defs "min _0 _0";;
    - : string = "<f -> <x -> x>>"
    # simplify q123_defs "min _0 _1";;
    - : string = "<f -> <x -> x>>"
    # simplify q123_defs "min _1 _0";;
    - : string = "<f -> <x -> x>>"


### (E) 

We saw an encoding of pairs in class. A triple is a package containing three values,
_(x,y,z)_. Your task is to create an encoding for triples.

Code a term **`triple`** which takes three terms and returns a triple
of those three terms using your encoding. Code terms **`firstT`**,
**`secondT`**, and **`thirdT`** each taking a triple (created by
function `triple`) as an argument and returning the first, second, and
third element of the triple, respectively.

Your terms should satisfy the following properties:

    firstT (triple a b c) = a
    secondT (triple a b c) = b
    thirdT (triple a b c) = c
        

    # simplify q123_defs "firstT (triple a b c)";;
    - : string = "a"
    # simplify q123_defs "secondT (triple a b c)";;
    - : string = "b"
    # simplify q123_defs "thirdT (triple a b c)";;
    - : string = "c"


## Question 2: Integers

We saw in class an encoding of natural numbers
0,1,2,3,... In this question, we consider an encoding of
integers, ...,-3,-2,-1,0,1,2,3,...

The encoding is dead simple. An integer is a pair of a
sign and a natural number. The sign will be represented by a
Boolean value: `true` for positive integers,
and `false` for negative integers. Thus,
_(true,3)_ is the representation of integer 3,
and _(false,2)_ the representation of integer
-2. **Integer 0 is always represented as (true,0).**

### (A)

Code a term **`int`** which
takes a natural number _n_ and returns a (positive) integer
_n_ in the encoding described above. 

    # simplify q123_defs "first (int _0)";;
    - : string = "<x -> <y -> x>>"
    # simplify q123_defs "second (int _0)";;
    - : string = "<f -> <x -> x>>"
    
    # simplify q123_defs "first (int _1)";;
    - : string = "<x -> <y -> x>>"
    # simplify q123_defs "second (int _1)";;
    - : string = "<f -> <x -> f x>>"
    
    # simplify q123_defs "first (int _2)";;
    - : string = "<x -> <y -> x>>"
    # simplify q123_defs "second (int _2)";;
    - : string = "<f -> <x -> f (f x)>>"   
    

### (B)

Code a term **`neg_int`** which
takes an integer _n_ in the encoding described
above, and returns its negation. Recall that the negation of 2 is
-2, and the negation of -3 is 3. 

    # simplify q123_defs "first (neg_int (int _3))";;
    - : string = "<x -> <y -> y>>"
    # simplify q123_defs "second (neg_int (int _3))";;
    - : string = "<f -> <x -> f (f (f x))>>"
    
    # simplify q123_defs "first (neg_int (neg_int (int _3)))";;
    - : string = "<x -> <y -> x>>"
    # simplify q123_defs "second (neg_int (neg_int (int _3)))";;
    - : string = "<f -> <x -> f (f (f x))>>"	  


### (C)

Code a term **`abs`** which
takes an integer _n_ in the encoding described
above, and returns its absolute value (as an integer still).

    # simplify q123_defs "first (abs (int _2))";;
    - : string = "<x -> <y -> x>>"
    # simplify q123_defs "second (abs (int _2))";;
    - : string = "<f -> <x -> f (f x)>>"
    
    # simplify q123_defs "first (abs (neg_int (int _2)))";;
    - : string = "<x -> <y -> x>>"
    # simplify q123_defs "second (abs (neg_int (int _2)))";;
    - : string = "<f -> <x -> f (f x)>>"


### (D)

Code a term **`plus_int`** which
takes two integers _m_ and _n_ in the encoding
described above and returns integer _m+n_. Of course, _m_ and _n_ may be negative.

    # simplify q123_defs "first (plus_int (int _3) (int _2))";;
    - : string = "<x -> <y -> x>>"
    # simplify q123_defs "second (plus_int (int _3) (int _2))";;
    - : string = "<f -> <x -> f (f (f (f (f x))))>>"
    
    # simplify q123_defs "first (plus_int (int _3) (neg_int (int _2)))";;
    - : string = "<x -> <y -> x>>"
    # simplify q123_defs "second (plus_int (int _3) (neg_int (int _2)))";;
    - : string = "<f -> <x -> f x>>"
    
    # simplify q123_defs "first (plus_int (neg_int (int _3)) (int _2))";;
    - : string = "<x -> <y -> y>>"
    # simplify q123_defs "second (plus_int (neg_int (int _3)) (int _2))";;
    - : string = "<f -> <x -> f x>>"
    
    # simplify q123_defs "first (plus_int (neg_int (int _3)) (neg_int (int _2)))";;
    - : string = "<x -> <y -> y>>"
    # simplify q123_defs "second (plus_int (neg_int (int _3)) (neg_int (int _2)))";;
    - : string = "<f -> <x -> f (f (f (f (f x))))>>"	  

**Hint:** Just consider all possibilities. You will find functions `ge` and `minus` from Question 1 useful here.


### (E)

Code a term **`times_int`** which
takes two integers _m_ and _n_ in the encoding
described above and returns integer _m &times; n_. Of course, _m_ and _n_ may be negative.

    # simplify q123_defs "first (times_int (int _3) (int _2))";;
    - : string = "<x -> <y -> x>>"
    # simplify q123_defs "second (times_int (int _3) (int _2))";;
    - : string = "<f -> <x -> f (f (f (f (f (f x)))))>>"
    
    # simplify q123_defs "first (times_int (int _3) (neg_int (int _2)))";;
    - : string = "<x -> <y -> y>>"
    # simplify q123_defs "second (times_int (int _3) (neg_int (int _2)))";;
    - : string = "<f -> <x -> f (f (f (f (f (f x)))))>>"
    
    # simplify q123_defs "first (times_int (neg_int (int _3)) (int _2))";;
    - : string = "<x -> <y -> y>>"
    # simplify q123_defs "second (times_int (neg_int (int _3)) (int _2))";;
    - : string = "<f -> <x -> f (f (f (f (f (f x)))))>>"
    
    # simplify q123_defs "first (times_int (neg_int (int _3)) (neg_int (int _2)))";;
    - : string = "<x -> <y -> x>>"
    # simplify q123_defs "second (times_int (neg_int (int _3)) (neg_int (int _2)))";;
    - : string = "<f -> <x -> f (f (f (f (f (f x)))))>>"	  
      


## Question 3: Lists and Recursion


### (A)

Let's encode lists. The encoding is a variant of that 
for pairs, with a twist. The encoding of pairs we saw encodes a pair as a 
function that takes a selector function _s_ as an argument and applies that selector function to the two components of the pair.

For lists, the twist is that there are two kinds of lists that need to
be handled differently: empty lists, and non-empty lists. Here is the
encoding for an empty list `empty`, and the encoding for a nonempty
list `cons x y ` with first element `x` and rest `y` (essentially
`x::y` in OCaml):

    empty = <p -> first p>
    cons  = <x -> <y -> <p -> (second p) x y>>>
        
Your goal is to understand this encoding, and to code a term
**`match_list`** which takes a list _L_ (encoded as above) and two
other arguments _a_ and _f_ and returns _a_ if _L_ is the empty list,
and otherwise calls _f_ with the head of _L_ and the tail of _L_ as
arguments.

Roughly speaking, `match_list lst a <h -> <t -> M>>`
behaves like `match lst with [] -> a | h::t -> M` in OCaml.</p>

Your function `match_list` should satisfy the following properties:

    match_list empty a f = a
    match_list (cons h t) a f = f h t

    # simplify q123_defs "match_list empty a f";;
    - : string = "a"
    # simplify q123_defs "match_list (cons h t) a f";;
    - : string = "f h t"
    # simplify q123_defs "match_list (cons A empty) a <h -> <t -> h>>";;
    - : string = "A"
    # simplify q123_defs "match_list (cons A (cons B empty)) a <h -> <t -> h>>";;
    - : string = "A"
    # simplify q123_defs "match_list (cons A (cons B empty)) a <h -> <t -> 
                           match_list t a <h -> <t -> h>>>>";;
    - : string = "B"	    


### (B)

Code a recursive term **`sum`** which
takes a list of natural numbers in the encoding described above and returns
a natural number (in the encoding we saw in class) representing the sum of its elements.

    # simplify q123_defs "sum empty";;
    - : string = "<f -> <x -> x>>"
    # simplify q123_defs "sum (cons _1 empty)";;
    - : string = "<f -> <x -> f x>>"
    # simplify q123_defs "sum (cons _1 (cons _3 empty))";;
    - : string = "<f -> <x -> f (f (f (f x)))>>"
    # simplify q123_defs "sum (cons _1 (cons _3 (cons _5 empty)))";;
    - : string = "<f -> <x -> f (f (f (f (f (f (f (f (f x))))))))>>"	  

**Hint:** Use &Theta; for the recursion.


### (C)

Code a recursive term **`fold_right`** which
takes a function _f_, a list _L_ (in the encoding described above), and a base value _b_, and
returns the result of doing a `fold_right` on list _L_ with folding function _f_ and base value _b_. It basically should behave exactly like `fold_right` in OCaml.

    # simplify q123_defs "fold_right <a -> <r -> succ r>> empty _0";;
    - : string = "<f -> <x -> x>>"
    # simplify q123_defs "fold_right <a -> <r -> succ r>> (cons _1 empty) _0";;
    - : string = "<f -> <x -> f x>>"
    # simplify q123_defs "fold_right <a -> <r -> succ r>> (cons _1 (cons _3 empty)) _0";;
    - : string = "<f -> <x -> f (f x)>>"
    # simplify q123_defs "fold_right <a -> <r -> succ r>> (cons _1 (cons _3 (cons _5 empty))) _0";;
    - : string = "<f -> <x -> f (f (f x))>>"


### (D)

Code a term **`sum_fold`** that computes the sum of the natural numbers in a list just like `sum` does but using `fold_right` from question (C) instead of using &Theta;.

    # simplify q123_defs "sum_fold empty";;
    - : string = "<f -> <x -> x>>"
    # simplify q123_defs "sum_fold (cons _1 empty)";;
    - : string = "<f -> <x -> f x>>"
    # simplify q123_defs "sum_fold (cons _1 (cons _3 empty))";;
    - : string = "<f -> <x -> f (f (f (f x)))>>"
    # simplify q123_defs "sum_fold (cons _1 (cons _3 (cons _5 empty)))";;
    - : string = "<f -> <x -> f (f (f (f (f (f (f (f (f x))))))))>>"
