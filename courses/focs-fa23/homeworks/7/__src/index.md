<script>
  document.title = 'Homework 7 - FOCS FA23'
</script>

# Homework 7

## Due Date: Thurday, Nov 30, 2023 (23h59)

- This homework is to be done individually. You may discuss problems
with fellow students, but all submitted work must be entirely your
own, and should not be from any other course, present, past, or
future. If you use a solution from another source you must cite it
&mdash; this includes when that source is someone else helping
you.

- **Please do not post your solutions on a public website or a public repository like GitHub.**

- All programming is to be done in Haskell using GHC v9. 

- Code your answers by modifying the file [`homework7.hs`](homework7.hs) provided. Add your **name**, your **email address**, and any **remarks** that you
wish to make to the instructor to the block comment at the head of the file.

- **Please do not change the types in the signature of the
function stubs I provide**. Doing so will make it
impossible to load your code into the testing infrastructure,
and make me unhappy.

- Feel free to define as many helper functions as you need.


## Electronic Submission Instructions

- Start a _fresh_  `ghci` shell.

- Load your homework code via `:load` *path-to-file*`/homework7.hs` to make sure that there are no errors when I will load your code.

- If there are any error, do not submit. I can't test what I can't `:load`.

- When you're ready to submit, log into our <a href="https://chat.rpucella.net">class chat</a>
and click <b>Submit File</b> in the profile menu (your initials in the upper right corner).


***

This homework is about the lambda calculus. I've implemented
some code for you to simplify lambda-calculus expressions using the
rules we saw in class. The result is not fast, but it
works.

A lambda expression is just a string in Haskell. The syntax of expressions is
the one we saw in class, adapted to the ASCII character set and the constraints of Haskell:

- An identifier can be any sequence of alphanumeric characters (including `_`, and starting with a letter), such as `x` or `arg_2`
- A function is written `(/identifier -> M)`  where `M` is an expression; thus, `(/x -> x)` and `(/arg -> plus arg arg)` are functions &mdash; note that unlike in class, we use a forward slash / instead of a backslash, since backslashes are used as an escape character in Haskell strings
- An application is written `M N`, where `M` and `N` are expressions
- Expressions can be wrapped in parentheses, such as `((/x -> x))` or `(ident1 (/y -> y))`. Without parentheses, applications associate to the left, so that `M N O P` parses as `((M N) O) P`. 


To simplify expressions, I implemented a function `simplify`
that repeatedly simplifies redexes until the expression reaches a normal
form. (If a normal form is not reached after 5000
steps, you'll get an error.)

    ghci> simplify [] "(/x -> x)"
    ----------------------------------------
    (/x -> x)
    ----------------------------------------
    (/x -> x)
    
    ghci> simplify [] "(/x -> x) z"
    ----------------------------------------
    (/x -> x) z
     => z
    ----------------------------------------
    z
    
    ghci> simplify [] "(/x -> (/y -> y y) x) z"
    ----------------------------------------
    (/x -> (/y -> y y) x) z
     => (/y -> y y) z
     => z z
    ----------------------------------------
    z z

The simplifications are contained between the lines, and the result (the normal form) is returned.

The first argument to `simplify` is a list of abbreviations that can
be used in the expression you want to simplify. Recall that when I presented
the lambda calculus in class, I started defining expressions as
abbreviations, such as `true` and `false`, to help us when writing
down more interesting expressions. The abbreviations you pass to `simplify`
have the form _(name,def)_ where _name_ is the name of the expression being
defined, and _def_ is the definition.

    ghci> my_defs = [ ("plus3", "(/n -> succ (succ (succ n)))") ]
    
    ghci> simplify my_defs "plus3 _2"
    ----------------------------------------
    (/n -> (/n -> (/f -> (/x -> n f (f x)))) ((/n -> (/f -> (/x -> n f (f x)))) ((/n -> (/f -> (/x -> n f (f x)))) n))) (/f -> (/x -> f (f x)))
     => (/n -> (/f -> (/x -> n f (f x)))) ((/n -> (/f -> (/x -> n f (f x)))) ((/n -> (/f -> (/x -> n f (f x)))) (/f -> (/x -> f (f x)))))
     => (/f -> (/x -> (/n -> (/f -> (/x -> n f (f x)))) ((/n -> (/f -> (/x -> n f (f x)))) (/f -> (/x -> f (f x)))) f (f x)))
     => (/f -> (/x -> (/f -> (/x -> (/n -> (/f -> (/x -> n f (f x)))) (/f -> (/x -> f (f x))) f (f x))) f (f x)))
     => (/f -> (/x -> (/x -> (/n -> (/f -> (/x -> n f (f x)))) (/f -> (/x -> f (f x))) f (f x)) (f x)))
     => (/f -> (/x -> (/n -> (/f -> (/x -> n f (f x)))) (/f -> (/x -> f (f x))) f (f (f x))))
     => (/f -> (/x -> (/f -> (/x -> (/f -> (/x -> f (f x))) f (f x))) f (f (f x))))
     => (/f -> (/x -> (/x -> (/f -> (/x -> f (f x))) f (f x)) (f (f x))))
     => (/f -> (/x -> (/f -> (/x -> f (f x))) f (f (f (f x)))))
     => (/f -> (/x -> (/x -> f (f x)) (f (f (f x)))))
     => (/f -> (/x -> f (f (f (f (f x))))))
    ----------------------------------------
    (/f -> (/x -> f (f (f (f (f x))))))

To make your life easier, `simplify` already knows about some of the
abbreviations we saw in class, including natural number arithmetic,
complete with a predecessor function. These are are available to you
without you needing to do anything special, and your own abbreviations can use them:

    let default_defs = [
           ("true", "(/x -> (/y -> x))"),
           ("false", "(/x -> (/y -> y))"),
           ("if", "(/c -> (/x -> (/y -> c x y)))"),
           ("_0","(/f -> (/x -> x))"),
           ("_1","(/f -> (/x -> f x))"),
           ("_2","(/f -> (/x -> f (f x)))"),
           ("_3","(/f -> (/x -> f (f (f x))))"),
           ("_4","(/f -> (/x -> f (f (f (f x)))))"),
           ("_5","(/f -> (/x -> f (f (f (f (f x))))))"),
           ("succ","(/n -> (/f -> (/x -> (n f) (f x))))"),
           ("plus","(/m -> (/n -> (/f -> (/x -> (m f) (n f x)))))"),
           ("times","(/m -> (/n -> (/f -> (/x -> m (n f) x))))"),
           ("iszero","(/n -> n (/x -> false) true)"),
           ("pred","(/n -> (/f -> (/x -> n (/g -> (/h -> h (g f))) (/u -> x) (/u -> u))))"),
           ("pair","(/x -> (/y -> (/s -> s x y)))"),
           ("first", "(/p -> p (/x -> (/y -> x)))"),
           ("second", "(/p -> p (/x -> (/y -> y)))"),
           ("some", "(/a -> (/s -> (/n -> s a)))"),
           ("none", "(/s -> (/n -> n))"),
           ("empty", "(/p -> first p)"),
           ("cons", "(/x -> (/y -> (/p -> (second p) x y)))"),
           ("head", "(/p -> p (pair false true))"),
           ("tail", "(/p -> p (pair false false))"),
           ("Theta", "(/x -> (/y -> y ((x x) y))) (/x -> (/y -> y ((x x) y)))"),
           ("sumto","Theta (/f -> (/n -> (/iszero n) _0 (plus n (f (pred n)))))")
         ]   

**Please do not modify the default definitions in `simplify` or add to them.  I will be testing your code with my own definition of `simplify` and I will not see the changes you've made. This will make my tests fail on your code.**
    
You'll note that you have only the first 6 natural numbers defined as
literals. This means that if you want to use natural number 8
somewhere, for instance, you will either have to create it by hand, or
use the arithmetic operations:

    ghci> simplify [] "plus _4 _4"
    ----------------------------------------
    (/m -> (/n -> (/f -> (/x -> m f (n f x))))) (/f -> (/x -> f (f (f (f x))))) (/f -> (/x -> f (f (f (f x)))))
     => (/n -> (/f -> (/x -> (/f -> (/x -> f (f (f (f x))))) f (n f x)))) (/f -> (/x -> f (f (f (f x)))))
     => (/f -> (/x -> (/f -> (/x -> f (f (f (f x))))) f ((/f -> (/x -> f (f (f (f x))))) f x)))
     => (/f -> (/x -> (/x -> f (f (f (f x)))) ((/f -> (/x -> f (f (f (f x))))) f x)))
     => (/f -> (/x -> f (f (f (f ((/f -> (/x -> f (f (f (f x))))) f x))))))
     => (/f -> (/x -> f (f (f (f ((/x -> f (f (f (f x)))) x))))))
     => (/f -> (/x -> f (f (f (f (f (f (f (f x)))))))))
    ----------------------------------------
    (/f -> (/x -> f (f (f (f (f (f (f (f x)))))))))

When you simplify an expression, all abbreviations are expanded out using their definition
before simplification occurs. This means that when you get 
the normal form back, it will be expressed in
expressions of the expanded out definitions. Thus, if you want to check
that the number 8 you create above is zero or not, you'll get
the resulting `false` as `(/x -> (/y -> y))`:

    ghci> simplify [] "iszero (plus _4 _4)"
    ----------------------------------------
    (/n -> n (/x -> (/x -> (/y -> y))) (/x -> (/y -> x))) ((/m -> (/n -> (/f -> (/x -> m f (n f x))))) (/f -> (/x -> f (f (f (f x))))) (/f -> (/x -> f (f (f (f x))))))
     => (/m -> (/n -> (/f -> (/x -> m f (n f x))))) (/f -> (/x -> f (f (f (f x))))) (/f -> (/x -> f (f (f (f x))))) (/x -> (/x -> (/y -> y))) (/x -> (/y -> x))
     => (/n -> (/f -> (/x -> (/f -> (/x -> f (f (f (f x))))) f (n f x)))) (/f -> (/x -> f (f (f (f x))))) (/x -> (/x -> (/y -> y))) (/x -> (/y -> x))
     => (/f -> (/x -> (/f -> (/x -> f (f (f (f x))))) f ((/f -> (/x -> f (f (f (f x))))) f x))) (/x -> (/x -> (/y -> y))) (/x -> (/y -> x))
     => (/x -> (/f -> (/x -> f (f (f (f x))))) (/x -> (/x -> (/y -> y))) ((/f -> (/x -> f (f (f (f x))))) (/x -> (/x -> (/y -> y))) x)) (/x -> (/y -> x))
     => (/f -> (/x -> f (f (f (f x))))) (/x -> (/x -> (/y -> y))) ((/f -> (/x -> f (f (f (f x))))) (/x -> (/x -> (/y -> y))) (/x -> (/y -> x)))
     => (/x -> (/x -> (/x -> (/y -> y))) ((/x -> (/x -> (/y -> y))) ((/x -> (/x -> (/y -> y))) ((/x -> (/x -> (/y -> y))) x)))) ((/f -> (/x -> f (f (f (f x))))) (/x -> (/x -> (/y -> y))) (/x -> (/y -> x)))
     => (/x -> (/x -> (/y -> y))) ((/x -> (/x -> (/y -> y))) ((/x -> (/x -> (/y -> y))) ((/x -> (/x -> (/y -> y))) ((/f -> (/x -> f (f (f (f x))))) (/x -> (/x -> (/y -> y))) (/x -> (/y -> x))))))
     => (/x -> (/y -> y))
    ----------------------------------------
    (/x -> (/y -> y))


By the time you're done with this homework, your brain will be rewired so that you'll read `(/x -> (/y -> y))` as `false` and `(/x -> (/y -> x))` as `true`. You're welcome.

A tricky point about definitions: every definition in the list of
definitions can only refer to previously defined expressions. This means two
things. First, that you can't write recursive definitions. (Recursion
in the lambda calculus is achieved via fixed point combinators.) It
also means that the order of definitions is important. What's tricky
is that if you mess it up, the simplification process will not
complain. An undefined identifier is just that, an identifier, and it
is passed around as an identifier. This makes debugging somewhat
painful. For instance, suppose you mistakenly wrote `pluss` instead of
`plus` in a previous example:

    ghci> simplify [] "iszero (pluss _4 _4)"
    ----------------------------------------
    (/n -> n (/x -> (/x -> (/y -> y))) (/x -> (/y -> x))) (pluss (/f -> (/x -> f (f (f (f x))))) (/f -> (/x -> f (f (f (f x))))))
     => pluss (/f -> (/x -> f (f (f (f x))))) (/f -> (/x -> f (f (f (f x))))) (/x -> (/x -> (/y -> y))) (/x -> (/y -> x))
    ----------------------------------------
    pluss (/f -> (/x -> f (f (f (f x))))) (/f -> (/x -> f (f (f (f x))))) (/x -> (/x -> (/y -> y))) (/x -> (/y -> x))
    
That's not great. The clue that something went wrong is the `pluss` there at the beginning of the normal form.

All the questions below ask you to create definitions. **You will add
those definitions to the list `qdefs`** I give you in
`homework7.hs`. There are already placeholders that you will need to
replace. That's the list I will test when grading.  All the helper
functions needed by your definitions should also go into that list,
since I will need to simplify your expressions. Feel free to add as
many helper functions as you need.

Sample outputs in the questions below skip the simplifications for all
the obvious reasons, and only gie you the normal forms.

* * *


## Question 1: Encodings


### (A)

Code a function **`not`** which takes a Boolean argument _a_ and return
`true` when _a_ is false and returns `false` when _a_ is true.
        
Code a function **`and`** which takes two Boolean arguments _a_ and
_b_ and returns `true` when both _a_ and _b_
are true, and returns `false` otherwise.

Code a function **`or`** which takes two Boolean arguments _a_ and _b_ and
returns `true` when at least one of _a_ and _b_ is true, and returns
`false` otherwise.

    > simplify qdefs "not true"
    (/x -> (/y -> y))
    
    > simplify qdefs "not false"
    (/x -> (/y -> x))


    > simplify qdefs "and true true"
    (/x -> (/y -> x))
    
    > simplify qdefs "and true false"
    (/x -> (/y -> y))
    
    > simplify qdefs "and false true"
    (/x -> (/y -> y))
    
    > simplify qdefs "and false false"
    (/x -> (/y -> y))


    > simplify qdefs "or true true"
    (/x -> (/y -> x))
    
    > simplify qdefs "or true false"
    (/x -> (/y -> x))
    
    > simplify qdefs "or false true"
    (/x -> (/y -> x))
    
    > simplify qdefs "or false false"
    (/x -> (/y -> y))

**Hint:** Use cascaded `if`s.


### (B)

Code a function **`minus`** which takes two Church numeral arguments
_m_ and _n_ and returns _m-n_ if _m_ is greater
than _n_, and 0 otherwise. 
        
    > simplify qdefs "minus _3 _0"
    (/f -> (/x -> f (f (f x))))
    
    > simplify qdefs "minus _3 _2"
    (/f -> (/x -> f x))
    
    > simplify qdefs "minus _3 _3"
    (/f -> (/x -> x))
    
    > simplify qdefs "minus _3 _4"
    (/f -> (/x -> x))
    
    > simplify qdefs "minus _0 _0"
    (/f -> (/x -> x))
    
    > simplify qdefs "minus _0 _2"
    (/f -> (/x -> x))
    

**Hint:** `pred 0` is `0`.
        

### (C)
      
Code a function **`ge`** which takes two Church numeral arguments _m_ and _n_ and returns `true` when _m_ is greater than or equal to _n_, and `false` otherwise.

Code a function **`gt`** which takes two Church numeral arguments _m_ and _n_ and returns `true` when _m_ is greater than (but not equal to) _n_, and `false` otherwise.

    > simplify qdefs "ge _3 _0"
    (/x -> (/y -> x))
    
    > simplify qdefs "ge _3 _2"
    (/x -> (/y -> x))
    
    > simplify qdefs "ge _3 _3"
    (/x -> (/y -> x))
    
    > simplify qdefs "ge _3 _4"
    (/x -> (/y -> y))
    
    > simplify qdefs "ge _0 _0"
    (/x -> (/y -> x))
    
    > simplify qdefs "ge _0 _1"
    (/x -> (/y -> y))


    > simplify qdefs "gt _3 _0"
    (/x -> (/y -> x))
    
    > simplify qdefs "gt _3 _2"
    (/x -> (/y -> x))
    
    > simplify qdefs "gt _3 _3"
    (/x -> (/y -> y))
    
    > simplify qdefs "gt _3 _4"
    (/x -> (/y -> y))
    
    > simplify qdefs "gt _0 _0"
    (/x -> (/y -> y))
    
    > simplify qdefs "gt _0 _1"
    (/x -> (/y -> y))
    
            
**Hint** there is one approach that uses `minus`.


### (D)

Code a function **`max`** which takes two Church numeral arguments _m_ and _n_ and returns the larger of the two.

Code a function **`min`** which takes two Church numeral arguments _m_ and _n_ and returns the smaller of the two.

    > simplify qdefs "max _3 _0"
    (/f -> (/x -> f (f (f x))))
    
    > simplify qdefs "max _3 _2"
    (/f -> (/x -> f (f (f x))))
    
    > simplify qdefs "max _3 _3"
    (/f -> (/x -> f (f (f x))))
    
    > simplify qdefs "max _3 _4"
    (/f -> (/x -> f (f (f (f x)))))
    
    > simplify qdefs "max _0 _0"
    (/f -> (/x -> x))
    
    > simplify qdefs "max _0 _1"
    (/f -> (/x -> f x))


    > simplify qdefs "min _3 _0"
    (/f -> (/x -> x))
    
    > simplify qdefs "min _3 _2"
    (/f -> (/x -> f (f x)))
    
    > simplify qdefs "min _3 _3"
    (/f -> (/x -> f (f (f x))))
    
    > simplify qdefs "min _3 _4"
    (/f -> (/x -> f (f (f x))))
    
    > simplify qdefs "min _0 _0"
    (/f -> (/x -> x))
    
    > simplify qdefs "min _0 _1"
    (/f -> (/x -> x))
    

* * *


## Question 2: Integers

We saw in class an encoding of natural numbers
0, 1, 2, 3, ... In this question, we consider an encoding of
integers, ..., -3, -2, -1, 0, 1, 2, 3,...

The encoding is simple. An integer is a pair of a
sign and a natural number. The sign will be represented by a
Boolean value: `true` for positive integers,
and `false` for negative integers. Thus,
_(true,3)_ is the representation of integer 3,
and _(false,2)_ the representation of integer
-2. **Integer 0 is always represented as (true,0).**

Use the implementation of pairs in the lambda calculus described in class.

### (A)

Code a function **`int`** which
takes a natural number _n_ and returns a (positive) integer
_n_ in the encoding described above. 

    > simplify qdefs "first (int _0)"
    (/x -> (/y -> x))
    
    > simplify qdefs "second (int _0)"
    (/f -> (/x -> x))


    > simplify qdefs "first (int _2)"
    (/x -> (/y -> x))
    
    > simplify qdefs "second (int _2)"
    (/f -> (/x -> f (f x)))


    > simplify qdefs "first (int _4)"
    (/x -> (/y -> x))
    
    > simplify qdefs "second (int _4)"
    (/f -> (/x -> f (f (f (f x)))))
    

### (B)

Code a function **`neg_int`** which takes an integer _n_ in the encoding
described above, and returns its negation. Recall that the negation of
2 is -2, and the negation of -3 is 3.

Be careful about the negation of 0. 

    > simplify qdefs "first (neg_int (int _3))"
    (/x -> (/y -> y))
    
    > simplify qdefs "second (neg_int (int _3))"
    (/f -> (/x -> f (f (f x))))


    > simplify qdefs "first (neg_int (neg_int (int _3)))"
    (/x -> (/y -> x))
    
    > simplify qdefs "second (neg_int (neg_int (int _3)))"
    (/f -> (/x -> f (f (f x))))



### (C)

Code a function **`abs_int`** which
takes an integer _n_ in the encoding described
above, and returns its absolute value _as an integer_.

    > simplify qdefs "first (abs_int (int _3))"
    (/x -> (/y -> x))
    
    > simplify qdefs "second (abs_int (int _3))"
    (/f -> (/x -> f (f (f x))))


    > simplify qdefs "first (abs_int (neg_int (int _3)))"
    (/x -> (/y -> x))
    
    > simplify qdefs "second (abs_int (neg_int (int _3)))"
    (/f -> (/x -> f (f (f x))))
    


### (D)

Code a function **`plus_int`** which
takes two integers _m_ and _n_ in the encoding
described above and returns integer _m+n_. Of course, _m_ and _n_ may be negative.

    > simplify qdefs "first (plus_int (int _3) (int _2))"
    (/x -> (/y -> x))
    
    > simplify qdefs "second (plus_int (int _3) (int _2))"
    (/f -> (/x -> f (f (f (f (f x))))))


    > simplify qdefs "first (plus_int (int _3) (neg_int (int _2)))"
    (/x -> (/y -> x))
    
    > simplify qdefs "second (plus_int (int _3) (neg_int (int _2)))"
    (/f -> (/x -> f x))


    > simplify qdefs "first (plus_int (neg_int (int _3)) (int _2))"
    (/x -> (/y -> y))
    
    > simplify qdefs "second (plus_int (neg_int (int _3)) (int _2))"
    (/f -> (/x -> f x))


    > simplify qdefs "first (plus_int (neg_int (int _3)) (neg_int (int _2)))"
    (/x -> (/y -> y))
    
    > simplify qdefs "second (plus_int (neg_int (int _3)) (neg_int (int _2)))"
    (/f -> (/x -> f (f (f (f (f x))))))
    

**Hint:** Just consider all possibilities. You will find functions `ge` and `minus` from Question 1 useful here.


* * *

## Question 3: Optional Values and Lists

In this question, we are going to emulate the optional type from
Haskell we saw in [Homework 6](../6/). Recall that type `Maybe b` is a
built-in Haskell type that gives a way to represent an optional value.
A value of type `Maybe b` is either `Nothing` or `Just` _v_, where _v_
has type `b`. To use a value of this type in Haskell, you do a case
analysis to check if the value is `Nothing` or it's `Just` _v_ for
some _v_:

    case exp of
      Nothing ->  <result when exp evaluates to `Nothing`
      Just v ->  <result when exp evaluates to `Just x` and bind `x` to identifier `v`>

Consider the following encoding for optional values in the lambda
calculus. The intuition here is that an optional value is constructed in such a way that it is either easy to recover the content, or we get an indication that the optional value is "nothing":

    just = (/x -> (/s -> (/n -> s x)))
    nothing = (/s -> (/n -> n))

As you can see, an optional value is a function of the form `(/s -> (/n -> ...))`: an optional value is a function that expects two arguments, `s` and `n`, and the idea is that when given a value for `s` and for `n`, if the the optional value was built with `just` then it applies `s` to the value given in the `just`, and if the optional value was build with `nothing` then it simply returns `n`. The optional value is constructed differently via the
`just` constructor and via the `nothing` constructor, but the shape of
the resulting optional value is the same. Case analysis is done simply by calling the optional value with a function to apply in the case of `just` and something to return in the case of `nothing`.

Lists are similar to optional values. There are two kinds of lists
that need to be handled differently: empty lists, and non-empty
lists. Here is the encoding for an empty list `empty`, and the
encoding for a nonempty list `cons x y ` with first element `x` and
rest `y` (essentially `x:y` in Haskell):

    empty = (/ne -> (/e -> e))
    cons  = (/x -> (/y -> (/ne -> (/e -> ne x y))))

A list is therefore a function of the form `(/ne -> (/e -> ...))`: a function that expects two arguments `ne` and `e`, and when provided values for those arguments, it either returns `e` if the list is empty, and it returns `ne` applied to both the head of the list and to the tail of the list when the list is non-empty. 

Roughly speaking, if `lst` is a list in the above encoding, then `lst (/h -> (/t -> M)) a` behaves just like the following in Haskell:

    case lst of
      [] -> a
      h:t -> M 

(where `M` can refer to `h` and to `t`).


### (A)

Code a function **`default`** which takes an optional value _opt_ and a
value _v_ and returns either the underlying "just" value of _opt_ when
it is a "just" option, or _v_ when _opt_ is a "nothing" option.

    > simplify qdefs "default (just a) b"
    a
    
    > simplify qdefs "default nothing b"
    b
    

### (B)

As in Homework 6, we can use optional values to represent "partial"
functions in the lambda calculus, that is, functions that return
values only under some circumstances. A function returns a "just"
option when it is defined for an argument, and a "nothing" option when it
is undefined for the argument.

Code a function **`either`** which takes two partial functions _f_ and _g_
that return option values and returns a new function _h_ that when
given an argument _x_ behaves as _f_ if _f_ is defined on _x_, behaves
as _g_ if _g_ is defined on _x_, and is undefined in all other
cases. _h_ should itself be a partial function that returns an
appropriate option value.

    > simplify qdefs "(either (/x -> just (F x)) (/y -> just (G y)) a) S N"
    S (F a)
    
    > simplify qdefs "(either (/x -> nothing) (/y -> just (G y)) a) S N"
    S (G a)
    
    > simplify qdefs "(either (/x -> just (F x)) (/y -> nothing) a) S N"
    S (F a)
    
    > simplify qdefs "(either (/x -> nothing) (/y -> nothing) a) S N"
    N
    

 

### (C)

Code a function **`sum`** which
takes a list of natural numbers in the encoding described above and
returns a natural number (in the encoding we saw in class)
representing the sum of its elements.

    > simplify qdefs "sum empty"
    (/f -> (/x -> x))
    
    > simplify qdefs "sum (cons _1 empty)"
    (/f -> (/x -> f x))
    
    > simplify qdefs "sum (cons _1 (cons _2 empty))"
    (/f -> (/x -> f (f (f x))))
    
    > simplify qdefs "sum (cons _1 (cons _2 (cons _3 empty)))"
    (/f -> (/x -> f (f (f (f (f (f x)))))))
    
    > simplify qdefs "sum (cons _1 (cons _2 (cons _3 (cons _4 empty))))"
    (/f -> (/x -> f (f (f (f (f (f (f (f (f (f x)))))))))))
    

**Hint:** Use &Theta; for the recursion.


### (D)

Code a function **`nth`** which takes a natural number _n_ (in the Church
encoding we saw in class) and a list _L_ (in the encoding above) and
returns the _n_th element of the list when _n_ is between 0 and one
less than the length of the list, or `false` when _n_ is greater than
or equal to the length of the list.

Interestingly, it is possible to write `nth` without using recursion,
using the fact that the natural number _n_ in the Church encoding can
apply a function _n_ times to an argument. But feel free to use
recursion if you want.

    > simplify qdefs "nth _0 (cons a (cons b (cons c empty)))"
    a
    
    > simplify qdefs "nth _1 (cons a (cons b (cons c empty)))"
    b
    
    > simplify qdefs "nth _2 (cons a (cons b (cons c empty)))"
    c
    
    > simplify qdefs "nth _3 (cons a (cons b (cons c empty)))"
    (/x -> (/y -> y))
    
    > simplify qdefs "nth _4 (cons a (cons b (cons c empty)))"
    (/x -> (/y -> y))
    
