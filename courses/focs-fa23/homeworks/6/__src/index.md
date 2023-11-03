<script>
  document.title = 'Homework 6 - FOCS FA23'
</script>

# Homework 6

## Due Date: Sunday Nov 12, 2023 (23h59)

- This homework is to be done individually. You may discuss problems
with fellow students, but all submitted work must be entirely your
own, and should not be from any other course, present, past, or
future. If you use a solution from another source you must cite it
&mdash; this includes when that source is someone else helping
you.

- **Please do not post your solutions on a public website or a public repository like GitHub.**

- All programming is to be done in Haskell using GHC v9. 

- Code your answers by modifying the file [`homework6.hs`](homework6.hs) provided. Add your **name**, your **email address**, and any **remarks** that you
wish to make to the instructor to the block comment at the head of the file.

- **Please do not change the types in the signature of the
function stubs I provide**. Doing so will make it
impossible to load your code into the testing infrastructure,
and make me unhappy.

- Feel free to define as many helper functions as you need.


## Electronic Submission Instructions

- Start a _fresh_  `ghci` shell.

- Load your homework code via `:load` *path-to-file*`/homework6.hs` to make sure that there are no errors when I will load your code.

- If there are any error, do not submit. I can't test what I can't `:load`.

- When you're ready to submit, log into our <a href="https://chat.rpucella.net">class chat</a>
and click <b>Submit File</b> in the profile menu (your initials in the upper right corner).


***

## Question 1: Partial functions

In this question, we consider the problem of working with partial functions. Intuitively, a
partial function from _A_ to _B_ is a function from _A_ to _B_ that is only defined for some values of
_A_. For instance, consider a function _iroot_ from integers to integers that returns the integer square
root of its input, that is, if the input _m_ is of the form _n_<sup>2</sup> for some integer _n_, then
_iroot(m)_ = _n_, and _iroot_ is undefined otherwise. 

Often, you can deal with partial functions using errors or
exceptions. That's how division (another partial function since it is
not defined when the denominator is 0) is usually implemented. But
there are cases where reporting an error is not the most elegant way
of handling it. Many language will have the notion of a `null` value
to represent undefined. (Python, for example, uses `None` for that
value.) One problem though is that null values cause all sorts of problem
since they are basically ignored by type systems in languages such as
Java or Go. Haskell's approach is interesting.

How can we represent partial functions in Haskell? We can use the type

    a -> Maybe b

as the type of partial functions that take values of type `a` and yield values of type `b`. To account
for the fact that these functions are partial, the result type is `Maybe b`.

Type `Maybe b` is a built-in Haskell type, and is a way to represent a null value.
A value of type `Maybe b` is created with the constructors `Nothing` and `Just`: `Nothing` represents a "null" value, while `Just ` _v_ represents value `v` (where _v_
has type `b`). The `Just` constructor is needed for the type system to work correctly, instead of just using a naked value `v`. For example:

    ghci> v1 = Nothing
    ghci> :t v1
    v1 :: Maybe a
    
    ghci> v2 = Just "hello"
    ghci> :t v2
    v2 :: Maybe String
    
    ghci> v3 = Just (3 :: Int)
    ghci> :t v3
    v3 :: Maybe Int

How do you use values of type `Maybe`? You use a case analysis to check if the value is `Nothing` or it's `Just` _v_
for some _v_:

    case exp of
      Nothing ->  <result when exp evaluates to `Nothing`>
      Just v ->  <result when exp evaluates to `Just x` and bind `x` to identifier `v`>
    
For example, consider the following function that accepts a `Maybe String` and returns an empty
string if the input is `Nothing` and returns the string s contained in the `Just` otherwise:

    ghci> :{
    ghci| toString s =
    ghci|   case s of
    ghci|     Nothing -> ""
    ghci|     Just x -> x
    ghci| :}
    
    ghci> :t toString
    toString :: Maybe String -> String
    
    ghci> toString Nothing
    ""
    
    ghci> toString (Just "hello")
    "hello"

Makes sense? (The notation `:{` is one way to get multi-line inputs into ghci.)

For various reasons having to do with getting simple type errors, I'll focus on partial functions that take strings as input, and are _polymorphic_ (meaning that they can handle mulitple types) in the return type. To make things readable, I use the following type abbreviation in the questions below and file `homework6.hs`:

    type Partial b = String -> Maybe b

So values of type `Partial b` are just values of type `String -> Maybe b`, that is, partial functions
from strings to values of type `b`.


### (A)

Code a value **`null_partial`** of type **`Partial b`** where `null_partial` represents a partial function that is defined nowhere, that is, that returns `Nothing` for all inputs. 

    > f = null_partial
    
    > f ""
    Nothing
    
    > f "one"
    Nothing
    
    > f "two"
    Nothing
    
    > f "ten"
    Nothing
    
    > f "hundred"
    Nothing


### (B)

Code a function **`extend_partial`** of type **`String -> b -> Partial b -> Partial b`** where `extend_partial s b p` return a new partial function that returns `b` when given input `s` and otherwise returns whatever the partial function `p` returns.

    > f = null_partial
    
    > f "zero"
    Nothing
    
    > f "ten"
    Nothing
    
    > f "hundred"
    Nothing
    
    > g = extend_partial "ten" 10 f
    
    > g "zero"
    Nothing
    
    > g "ten"
    Just 10
    
    > g "hundred"
    Nothing
    
    > h = extend_partial "hundred" 100 g
    
    > h "zero"
    Nothing
    
    > h "ten"
    Just 10
    
    > h "hundred"
    Just 100


### (C)

Code a function **`join_partial`** of type **`Partial b -> Partial b -> Partial b`** where `join_partial p1 p2` return a new partial function that on any given input returns what `p1` returns on that input if `p1` is defined on that input, otherwise returns what `p2` returns on that input if `p2` is defined on that input, and is undefined in all other cases.

    > f = extend_partial "one" 1 null_partial
    
    > f "one"
    Just 1
    
    > f "two"
    Nothing
    
    > f "three"
    Nothing
    
    > g = extend_partial "two" 2 null_partial
    
    > g "one"
    Nothing
    
    > g "two"
    Just 2
    
    > g "three"
    Nothing
    
    > h = join_partial f g
    
    > h "one"
    Just 1
    
    > h "two"
    Just 2
    
    > h "three"
    Nothing
    
    > i = join_partial h (extend_partial "three" 3 null_partial)
    
    > i "one"
    Just 1
    
    > i "two"
    Just 2
    
    > i "three"
    Just 3


### (D)

Here are two obvious alternative to "complete" a partial function and turn it into a _total function_ (a function defined on all inputs).

Code a function **`default_partial`** of type **`b -> Partial b -> (String -> b)`** where `default_partial d p` return a _total function_ that on any given input returns the value that partial function `p` returns on that input if `p` is defined on that input, and returns the default value `d` otherwise. 

Code a function **`fail_partial`** of type **`Partial b -> (String -> b)`** where `fail_partial p` return a _total function_ that on any given input returns the value that partial function `p` returns on that input, and otherwise causes an error `"undefined"`.

    > f = extend_partial "one" 1 (extend_partial "two" 2 null_partial)
    
    > f "zero"
    Nothing
    
    > f "one"
    Just 1
    
    > f "two"
    Just 2
    
    > f "three"
    Nothing
    
    > g = default_partial (-1) f
    
    > g "zero"
    -1
    
    > g "one"
    1
    
    > g "two"
    2
    
    > g "three"
    -1
    
    > h = fail_partial f
    
    > h "zero"
    *** Exception: undefined
    
    > h "one"
    1
    
    > h "two"
    2
    
    > h "three"
    *** Exception: undefined


### (E)

What can you do with partial functions? Well, among other things, you can use them to implement dictionaries. A dictionary associates keys to arbitrary values, so that you can look up the value corresponding to a key. More specifically, we can represent a dictionary mapping keys of type `String` to values of type `b` as a function from strings to values of type `b` that throws an error when the value is not a key in the dictionary. We get this almost for free by using partial functions.

Code a function **`dict`** of type **`[(String, b)] -> (String -> b)`** where `dict ps` takes a list of pairs `ps` describing the dictionary content (where a pair `(k, v)` indicates that key `k` should be associated with value `v`) and returns a function that takes a key and returns the value associated with that key in the dictionary, throwing an exception if there is no such key.

For full credit, implement `dict` using by constructing a partial function from the pairs
in `ps` using only functions in (A)-(D). 

    > d1 = dict []
    
    > d1 "one"
    *** Exception: undefined
    
    > d1 "two"
    *** Exception: undefined
    
    > d2 = dict [("one", 1), ("two", 2), ("three", 3)]
    
    > d2 "zero"
    *** Exception: undefined
    
    > d2 "one"
    1
    
    > d2 "two"
    2
    
    > d2 "three"
    3
    
    > d2 "four"
    *** Exception: undefined


***

## Question 2: Context-Free Grammars


Here is a type for context-free grammars:

    data CFG = CFG {
      cfg_nterms :: [Char],
      cfg_terms  :: [Char],
      cfg_rules :: [(Char, String)],
      cfg_start :: Char
    }

As I mentioned in the past, a `String` is the same as a `[Char]`. When I want to emphasize a list of characters as a _set_ of characters, I will tend to use `[Char]`, while when I want to emphasize it as a _sequence_ of characters (i.e., a string), I will use `String`.

Recall that a context-free grammar is described by a set of
nonterminal symbols, a set of terminal symbols, a set of rules, and a
starting symbol. The following grammar with starting symbol S and
terminals `a` and `b` generates the language
{`a`<sup>n</sup>`b`<sup>n</sup> | n &ge; 0}:

   S &rarr; &epsilon;<br>
   S &rarr; `a` S `b` 

Here is how you represent that grammar with the type above:

    anbn = CFG {
      cfg_nterms = ['S'],
      cfg_terms = ['a', 'b'],
      cfg_rules = [('S', ""),
                   ('S', "aSb") ],
      cfg_start = 'S'
    }

Note that a context-free grammar rule P &rarr; Q is represented by a pair (P, Q)
where P is a nonterminal symbol and Q is a sequence of terminal and nonterminal
symbols.

Here is a context-free grammar generating the language {`a`<sup>n</sup>`b`<sup>m</sup> | m &ge; n &ge; 0}:

    anbm = CFG {
      cfg_nterms = ['S', 'T', 'U'],
      cfg_terms = ['a', 'b'],
      cfg_rules = [('S', "TU"),
                   ('T', ""),
                   ('T', "aTb"),
                   ('U', ""),
                   ('U', "Ub")],
      cfg_start = 'S'
    }

To check whether a string can be generated from a context-free
grammar, I provided you with a function

    generate_cfg: CFG -> String -> Int -> [String] -> IO Bool

Essentially, `generate_cfg g str n init` checks whether string `str`
can be generated by context-free grammar `g`, returning true if it can, and false otherwise. If
true, the function also prints a sequence of rewrites that shows how
the string can be generated. For example:

    ghci> generate_cfg anbm "aabbb" 10 []
    Searching (depth 01, max width 01)
    Searching (depth 02, max width 02)
    Searching (depth 03, max width 03)
    Searching (depth 04, max width 04)
    Searching (depth 05, max width 05)
       S
    -> TU
    -> aTbU
    -> aaTbbU
    -> aabbU
    -> aabbUb
    -> aabbb
    True

What about arguments `n` and `init`? Well, checking if a string
can be generated by a grammar is done by taking the start symbol,
and repeatedly applying all possible rules until the string is
generated. That's expensive. Also, if the string cannot be generated,
then this process never terminates.  To control the process and
prevent searching forever, rules are applied only up
to _depth_ `n` (that is, up to `n` rules can be
applied in any sequence of rewrites &mdash; moreover, the maximum
width of string that can be obtained during any of those rewrites is
also `n`). This means, in particular, that if you know that a
string requires K rewrites to be generated, then you need to supply a
value of at least K to function `generate_cfg`. The tricky bit is
that the largest the parameter `n`, the slowest the generation
process, because the tree of rewrites being searched gets bigger
as `n` increases. If your grammar gets too complicated, don't
be surprised if it gets difficult to generate strings you _know_
can be generated.

Here's a sample generation that fails because the search depth
is too low:

    ghci> generate_cfg anbm "aaaaabbbbbbb" 10 []
    Searching (depth 01, max width 01)
    Searching (depth 02, max width 02)
    Searching (depth 03, max width 03)
    Searching (depth 04, max width 04)
    Searching (depth 05, max width 05)
    Searching (depth 06, max width 06)
    Searching (depth 07, max width 07)
    Searching (depth 08, max width 08)
    Searching (depth 09, max width 09)
    Searching (depth 10, max width 10)
    Searching (depth 11, max width 11)
    False
    
Increasing the depth gets us the string:

    ghci> generate_cfg anbm "aaaaabbbbbbb" 13 []
    Searching (depth 01, max width 01)
    Searching (depth 02, max width 02)
    Searching (depth 03, max width 03)
    Searching (depth 04, max width 04)
    Searching (depth 05, max width 05)
    Searching (depth 06, max width 06)
    Searching (depth 07, max width 07)
    Searching (depth 08, max width 08)
    Searching (depth 09, max width 09)
    Searching (depth 10, max width 10)
    Searching (depth 11, max width 11)
    Searching (depth 12, max width 12)
       S
    -> TU
    -> aTbU
    -> aaTbbU
    -> aaaTbbbU
    -> aaaaTbbbbU
    -> aaaaaTbbbbbU
    -> aaaaabbbbbU
    -> aaaaabbbbbUb
    -> aaaaabbbbbUbb
    -> aaaaabbbbbbb
    True

To help, you can also give an initial sequence `init` of rewrites to
get you to a point where you want to start the derivation search. If
you specify `[]`, the search will start from the start symbol, the way
we saw in class. If you specify a sequence of strings, `generate_cfg`
checks that the sequence describes an possible sequence of rewrites
(in particular, the first string in the sequence should be the
starting symbol) and then starts the search from the last string in
the sequence, which will be depth 1 for the purpose of this search. If
the sequence of strings does not describe a possible sequence of
rewrites, you will get an exception.

Here is an example of the search above that failed at depth 10 which succeeds at depth 10 if you give the first few rewrites:

    ghci> generate_cfg anbm "aaaaabbbbbbb" 10 ["S", "TU", "aTbU", "aaTbbU"]
    Searching (depth 01, max width 01)
    Searching (depth 02, max width 02)
    Searching (depth 03, max width 03)
    Searching (depth 04, max width 04)
    Searching (depth 05, max width 05)
    Searching (depth 06, max width 06)
    Searching (depth 07, max width 07)
       aaTbbU
    -> aaaTbbbU
    -> aaaaTbbbbU
    -> aaaaaTbbbbbU
    -> aaaaabbbbbU
    -> aaaaabbbbbUb
    -> aaaaabbbbbUbb
    -> aaaaabbbbbbb
    True

This is mostly going to be needed in Question 3. Thought you can use this to test a complete derivation:

    ghci> generate_cfg anbm "aaaaabbbbbbb" 1 ["S", "TU", "TUb", "TUbb", "Tbb", "aTbbb", "aaTbbbb", "aaaTbbbbb",
            "aaaaTbbbbbb", "aaaaaTbbbbbbb", "aaaaabbbbbbb"]
    Searching (depth 01, max width 01)
       aaaaabbbbbbb
    True

You'll need a depth of at least 1 to get the function to recognize your sequence.

In this question, I ask you to define context-free grammars as
constants with a fixed name given below in each
question. There is already a placeholder in `homework6.hs` for those
answers. Just replace the placeholder with your definition.

Note that the grammars in this question should all be **context-free**.

### (A)

Construct a context-free grammar **`q2_part_a`** of type **`CFG`**
that generates the language of all strings of the form
`a`<sup>m</sup>`b`<sup>m+n</sup>`c`<sup>n</sup> for m, n &ge; 0, i.e.,
the set of all strings of `a`s followed by `b`s followed by `c`s,
where there are as many `b`s as `a`s and `c`s combined.

Sample strings in the generated language:

    (the empty string)   [m=0, n=0]
    abbc                 [m=1, n=1]
    aabb                 [m=2, n=0]
    bbcc                 [m=0, n=2]
    abbbcc               [m=1, n=2]
    aabbbc               [m=2, n=1]
    aabbbbcc             [m=2, n=2]
    aabbbbbccc           [m=2, n=3]
    aaabbbbbcc           [m=3, n=2]

Sample strings _not_ in the generated language:

    cb
    ba
    ccba
    a
    b
    c
    ac
    abc
    abbcc
    abbbbcc
    aabbbcc
    aaabbcc


### (B)

Construct a context-free grammar **`q2_part_b`**
of type **`CFG`**
that generates the language of all strings of the form
`a`<sup>m</sup>`b`<sup>n</sup>`c`<sup>m+n</sup> for m, n &ge; 0, i.e., the set of all strings made up
of `a`s followed by `b`s 
followed by `c`s, where there are as many `c`s as `a`s and `b`s combined.

Sample strings in the generated language:

    (the empty string)
    abcc                 [m=1, n=1]
    aacc                 [m=2, n=0]
    bbcc                 [m=0, n=2]
    abbccc               [m=1, n=2]
    aabccc               [m=2, n=1]
    aabbcccc             [m=2, n=2]
    aabbbccccc           [m=2, n=3]
    aaabbccccc           [m=3, n=2]

Sample strings _not_ in the generated language:

    cb
    ba
    cbba
    a
    b
    c
    ab
    abc
    aabbcc
    aabcc
    abbcc
    aabbccc
    aabcccc
    


### (C)

Construct a context-free grammar **`q2_part_c`** of type
**`CFG`** that generates the language of all strings of the form
`a`<sup>2m</sup>`b`<sup>m+n</sup>`c`<sup>n</sup> for m, n &ge; 0, i.e.,
the set of all strings made up of `a`s followed by `b`s followed by
`c`s, where there are 2m `a`s when there are m+n `b`s and n `c`s. 

Sample strings in the generated language:

    (the empty string)   [m=0, n=0]
    aabbc                [m=1, n=1]
    aaaabb               [m=2, n=0]
    bbcc                 [m=0, n=2]
    aabbbcc              [m=1, n=2]
    aaaabbbc             [m=2, n=1]
    aaaabbbbcc           [m=2, n=2]
    aaaabbbbbccc         [m=2, n=3]
    aaaaaabbbbbcc        [m=3, n=2]

Sample strings _not_ in the generated language:

    ba
    ccbaaa
    bccaaa
    aabccc
    aaaabbc
    aabb
    aabbcc
    abbc
    aaabbc
    

### (D)

Construct a context-free grammar **`q2_part_d`**
of type **`CFG`**
that
generates all strings over {`a`,`b`} that have the same number of `a`s and `b`s.

Sample strings in the generated language:

    (the empty string)
    ab
    ba
    aabb
    abab
    baab
    baba
    aaabbb
    ababab
    bbaaba
    abaabbabbaba

Sample strings _not_ in the generated language:

    a
    b
    abb
    aba
    babab
    bbaab
    aaabb
    abababa
    aaaaaab
    abbbbbb
    

### (E)

Construct a context-free grammar **`q2_part_e`** of type **`CFG`** that generates the language
of all strings of the form `1`<sup>m</sup>`+1`<sup>n</sup>`=1`<sup>m+n</sup> for m, n &ge; 0
representing **unary** addition. For instance, `111+11=11111` represents 3 + 2 = 5, while `11+=11`
represent 2 + 0 = 2.

Sample strings in the generated language:

    +=
    1+=1
    +1=1
    1+1=11
    11+1111=111111
    111+11111=11111111
    11111+111=11111111
    1111+=1111

Sample strings _not_ in the generated language:

    1+1=1
    11+=1
    11+=111
    11+11=111
    11+11=11111
    1=1+1
    1+1+1=1
    1=1=1

**Hint**: Do not get distracted by the meaning of the symbols.

* * *


## Question 3: Unrestricted Grammars

An unrestricted grammar is a grammar like the context-free grammars in Question 2 _except_ without the restrictions that rules can only rewrite a single nonterminal symbol: rules are of the form P &rarr; Q where both P and Q are sequences of terminal and nonterminal symbols.

There is a dedicated type for unrestricted grammars, different from the context-free grammars in Question 2, but very similar:

    data Grammar = Grammar {
      nterms :: [Char],
      terms :: [Char],
      rules :: [(String, String)],
      start :: Char
    }

Here is the example of the unrestricted grammar with terminals `a`, `b`, and `c` that generates the language
{`a`<sup>n</sup>`b`<sup>n</sup>`c`<sup>n</sup> | n &ge; 0} that I showed in class and that cannot actually be generated by a context-free grammar:

    anbncn = Grammar {
      nterms = ['S', 'A', 'B', 'C', 'X'],
      terms = ['a', 'b', 'c'],
      rules = [ ("S", ""),
                ("S", "AB"),
                ("B", "XbBc"),
                ("B", ""),
                ("A", "AA"),
                ("AX", "a"),
                ("aX", "Xa"),
                ("bX", "Xb") ],
      start = 'S'
    }

To check whether a string can be generated from an unrestricted grammar, I provided you with a different version of `generate_cfg` that works on unrestricted grammars, but works in _exactly_ the same way:

    generate: Grammar -> String -> Int -> [String] -> IO Bool

Again, `generate g str n init` checks whether string `str`
can be generated by unrestricted grammar `g`, returning true if it can, and false otherwise. If
true, the function also prints a sequence of rewrites that shows how
the string can be generated. As with `generate_cfg`, argument `n` controls how deep the search goes (how long the rewrite sequence), and `init` is an initial sequence of rewrites that we can use to start the search "deeper".

Argument `init` is almost always needed for unrestricted grammars,
because the search space gets very large very quickly.  For example,
if you want to check that `anbncn` can generate `aaaaaabbbbbbcccccc` -
this requires depth > 38 starting from the start symbol, and I didn't
have the patience to run it to completion on my machine. It takes a
_long_ while. But we can get it under a minute if we're willing to
provide the first few rewrites:

    ghci> generate anbncn "aaaaaabbbbbbcccccc" 35 ["S", "AB", "AAB", "AAAB", "AAAAB", "AAAAAB", "AAAAAAB", "AAAAAAXbBc",
            "AAAAAAXbXbBcc", "AAAAAAXbXbXbBccc", "AAAAAAXbXbXbXbBcccc", "AAAAAAXbXbXbXbXbBccccc", "AAAAAAXbXbXbXbXbXbBcccccc",
	    "AAAAAAXbXbXbXbXbXbcccccc"]
    Searching (depth 01, max width 01)
    Searching (depth 02, max width 02)
    Searching (depth 03, max width 03)
    Searching (depth 04, max width 04)
    Searching (depth 05, max width 05)
    Searching (depth 06, max width 06)
    Searching (depth 07, max width 07)
    Searching (depth 08, max width 08)
    Searching (depth 09, max width 09)
    Searching (depth 10, max width 10)
    Searching (depth 11, max width 11)
    Searching (depth 12, max width 12)
    Searching (depth 13, max width 13)
    Searching (depth 14, max width 14)
    Searching (depth 15, max width 15)
    Searching (depth 16, max width 16)
    Searching (depth 17, max width 17)
    Searching (depth 18, max width 18)
    Searching (depth 19, max width 19)
    Searching (depth 20, max width 20)
    Searching (depth 21, max width 21)
    Searching (depth 22, max width 22)
    Searching (depth 23, max width 23)
    Searching (depth 24, max width 24)
    Searching (depth 25, max width 25)
    Searching (depth 26, max width 26)
    Searching (depth 27, max width 27)
    Searching (depth 28, max width 28)
    Searching (depth 29, max width 29)
    Searching (depth 30, max width 30)
    Searching (depth 31, max width 31)
    Searching (depth 32, max width 32)
    Searching (depth 33, max width 33)
    Searching (depth 34, max width 34)
    Searching (depth 35, max width 35)
       AAAAAAXbXbXbXbXbXbcccccc
    -> AAAAAabXbXbXbXbXbcccccc
    -> AAAAAaXbbXbXbXbXbcccccc
    -> AAAAAXabbXbXbXbXbcccccc
    -> AAAAaabbXbXbXbXbcccccc
    -> AAAAaabXbbXbXbXbcccccc
    -> AAAAaaXbbbXbXbXbcccccc
    -> AAAAaXabbbXbXbXbcccccc
    -> AAAAXaabbbXbXbXbcccccc
    -> AAAaaabbbXbXbXbcccccc
    -> AAAaaabbXbbXbXbcccccc
    -> AAAaaabXbbbXbXbcccccc
    -> AAAaaaXbbbbXbXbcccccc
    -> AAAaaXabbbbXbXbcccccc
    -> AAAaXaabbbbXbXbcccccc
    -> AAAXaaabbbbXbXbcccccc
    -> AAaaaabbbbXbXbcccccc
    -> AAaaaabbbXbbXbcccccc
    -> AAaaaabbXbbbXbcccccc
    -> AAaaaabXbbbbXbcccccc
    -> AAaaaaXbbbbbXbcccccc
    -> AAaaaXabbbbbXbcccccc
    -> AAaaXaabbbbbXbcccccc
    -> AAaXaaabbbbbXbcccccc
    -> AAXaaaabbbbbXbcccccc
    -> AaaaaabbbbbXbcccccc
    -> AaaaaabbbbXbbcccccc
    -> AaaaaabbbXbbbcccccc
    -> AaaaaabbXbbbbcccccc
    -> AaaaaabXbbbbbcccccc
    -> AaaaaaXbbbbbbcccccc
    -> AaaaaXabbbbbbcccccc
    -> AaaaXaabbbbbbcccccc
    -> AaaXaaabbbbbbcccccc
    -> AaXaaaabbbbbbcccccc
    -> AXaaaaabbbbbbcccccc
    -> aaaaaabbbbbbcccccc
    True


In this question, I ask you to define unrestricted grammars as
constants with a fixed name given below in each question,
like you did in Question 2. There is already a placeholder in
`homework6.hs` for those answers. Just replace the placeholder with your
definition.

Testing these is difficult, depending on the complexity of the grammars you come up with. You will
definitely benefit from constructing initial sequences of rewrites by hand to give as a last argument
to `generate`. That's what I did when creating sample solutions to these myself.

To help me on my end, I will ask you not only to create the grammar, but also to give the sequence
of rewrites that generate a given string. That sequence of rewrites should be of the form that I can
feed into `generate` as described above to test that your sequence of rewrites correctly generates the
string. For instance, if I were to ask you to give a sequence of rewrites to show that `anbn` can
generate the string `aaaabbbb`, you could give me the list:

    test_anbn_4 = ["S", "aSb", "aaSbb", "aaaSbbb", "aaaaSbbbb", "aaaabbbb"]

which I will be able to test by running

    generate anbn "aaaabbbb" 1 test_anbn_4

You can define those rewrites as list constants with fixed
names given below for each grammar.  There is an empty placeholder for those
rewrite that you should replace with your lists.

### (A)

Construct a grammar **`q3_part_a`** of type **`Grammar`** that generates the language of all strings of the form `a`<sup>n</sup>`b`<sup>n</sup>`c`<sup>n</sup>`d`<sup>n</sup> for n &ge; 0, i.e., all strings with the same number of `a`s, `b`s, `c`s, and `d`s, in that order.

Sample strings in the generated language:

    (the empty string)
    abcd
    aabbccdd
    aaabbbcccddd
    aaaabbbbccccdddd

Sample strings _not_ in the generated language:

    a
    b
    c
    d
    abc
    acd
    abcdd
    aabcd
    abcda
    babcd
    aaabbccdd
    aabbccddd
    aaabbbccc
    

Please provide sequences of rewrites showing how to generate
`aabbccdd` and `aaabbbcccddd` with your grammar, bound
to the identifiers **`rewrites_part_a_2`** and
**`rewrites_part_a_3`**, respectively.

**Hint**: look at the grammar for the language {`a`<sup>n</sup>`b`<sup>n</sup>`c`<sup>n</sup> | n &ge; 0}.
    

### (B)

Construct a grammar **`q3_part_b`** of type **`Grammar`**
that generates strings of the form WW where W is an arbitrary string over alphabet {`a`, `b`}.

Sample strings in the generated language:

    (the empty string)
    aa
    bb
    abab
    baba
    abbabb
    aabbaabb
    abababababab
    
Sample strings _not_ in the generated language:

    aaa
    aba
    abba
    aaaaa
    ababab
    abbbabba
    ababbaba

Please provide sequences of rewrites showing how to generate
`abaaabaa` and `aabbbaabbb` with your grammar, bound
to the identifiers **`rewrites_part_b_abaa`** and
**`rewrites_part_b_aabbb`**, respectively.

**Hint**: Generate a string of `a`s and `b`s for W so that whenever you generate an `a` you also
generate an `X`, and when you generate a `b` you also generate a `Y`. Send the `X`s and the `Y`s to
the right to the end of the string, and when you reach the end of the string turn `X`s into `a`s and
`Y`s into `b`s. Don't cross the `X`s and the `Y`s. And you'll have to figure out how to know when
you're at the end of the string.

