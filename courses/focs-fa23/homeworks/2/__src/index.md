<script>
  document.title = 'Homework 2 - FOCS FA23'
</script>

# Homework 2

## Due Date: Thursday, Oct 5, 2023 (23h59)

- This homework is to be done individually. You may discuss problems
with fellow students, but all submitted work must be entirely your
own, and should not be from any other course, present, past, or
future. If you use a solution from another source you must cite it
&mdash; this includes when that source is someone else helping
you.

- **Please do not post your solutions on a public website or a public repository like GitHub.**

- All programming is to be done in Haskell using GHC v9. 

- Code your answers by modifying the file [`homework2.hs`](homework2.hs) provided. Add your **name**, your **email address**, and any **remarks** that you
wish to make to the instructor to the block comment at the head of the file.

- **Please do not change the types in the signature of the
function stubs I provide**. Doing so will make it
impossible to load your code into the testing infrastructure,
and make me unhappy.

- Feel free to define as many helper functions as you need.


## Electronic Submission Instructions

- Start a _fresh_  `ghci` shell.

- Load your homework code via `:load` *path-to-file*`/homework2.hs` to make sure that there are no errors when I will load your code.

- If there are any error, do not submit. I can't test what I can't `:load`.

- When you're ready to submit, log into our <a href="https://chat.rpucella.net">class chat</a>
and click <b>Submit File</b> in the profile menu (your initials in the upper right corner).


* * *

Consider the following Haskell type for finite automata, or finite state machines:

    data FA = FA {
      states :: [Int],
      alphabet :: [Char],
      delta :: [(Int, Char, Int)],
      start :: Int,
      final :: [Int]
    }

A finite automaton is described by a _record_ with fields holding the various components of the
automaton: the set of states, the alphabet, the transition relation &Delta;, the start state, and
the set of final states. Lists are used to represent sets. The transition relation is represented
using list of triples (_p_, _a_, _q_), stating that in state _p_, reading symbol _a_ makes the machine
transition to state _q_. For simplicity, we're going to represent states using integers. (It's easy enough to
modify this declaration to make finite state machines generic over the type of the state.)

Haskell uses the type `Char` for characters, with literals written `'a'`, `'b'`, `'c'`, .... A string is defined as just a list of characters, so that `String` and `[Char]` are in fact exactly the same type. This means that you can iterate over the characters in a string using recursion, in the usual way.

Recall that a finite automaton is **deterministic** if for every state _p_ and every symbol _a_ there is
exactly one transition out of _p_ labelled _a_ in the automaton. Nondeterministic finite automata have
no restrictions: there can be any number of transitions labelled _a_ out of any state, including none. This homework is all about deterministic finite automata.

Records are basically dictionaries, except that they are statically typed, and the field names
making up a record are part of its type. For information how to work with records in Haskell, [check
out this link](https://devtut.github.io/haskell/record-syntax.html). It's pretty straightforward. You create a record by calling the constructor (here, `FA`) and passing a value for each field. To access the fields of a record, you treat the field names as _accessor functions_, so that, for example, the alphabet of a finite automata `fm` in the above representation can be accessed with `alphabet fm`. You can also use pattern matching if you wish, but I'll let you figure that one out on your own. 

Here is an example of a deterministic finite automaton that accepts the set of all strings over
`{a, b}` that contains a number of `a`s that is a multiple of 3.

    faMod3a = FA { states = [1, 2, 3],
                   alphabet = ['a', 'b'],
                   delta = [ (1, 'a', 2),
                             (2, 'a', 3),
                             (3, 'a', 1),
                             (1, 'b', 1),
                             (2, 'b', 2),
                             (3, 'b', 3) ],
                   start = 1,
                   final = [1]
                 }

Here is an example of a deterministic finite automaton that accepts the language of all strings over `{a, b, c}` that start with an `a` and end with a `c`. 

    faStartaEndc = FA { states = [0, 1, 2, 99],
                        alphabet = ['a', 'b', 'c'],
                        delta = [ (0, 'a', 1),
                                  (1, 'a', 1),
                                  (2, 'a', 1),
                                  (99, 'a', 99),
                                  (0, 'b', 99),
                                  (1, 'b', 1),
                                  (2, 'b', 1),
                                  (99, 'b', 99),
                                  (0, 'c', 99),
                                  (1, 'c', 2),
                                  (2, 'c', 2),
                                  (99, 'c', 99) ],
                        start = 0,
                        final = [2]
                      }


* * *


## Question 1: Simulating Deterministic Finite Automata

We are going to code up a simulator for deterministic finite automata. Deterministic finite automata
are pretty easy to simulate. Basically, start at the start state, and follow symbols from the input
string until you reach the end of the string. Accept if you are on a final state. We're going to
build up the code in steps. (If the automaton is in fact not deterministic, we will treat it as deterministic by ignoring extra transitions out of a state with the same label.)


### (A)

Code a function **`isFinal`** of type **`[Int] -> Int -> Bool`** where `isFinal fs q` returns True if `q` is a final state of automaton included in the final states `fs`, and False otherwise.

    > isFinal (final faMod3a) 1
    True
    > isFinal (final faMod3a) 2
    False
    > isFinal (final faMod3a) 3
    False
    
    > isFinal (final faStartaEndc) 0
    False
    > isFinal (final faStartaEndc) 1
    False
    > isFinal (final faStartaEndc) 2
    True


### (B)

Code a function **`followSymbol`** of type **`[(Int, Char, Int)] -> Int -> Char -> Int`** where `followSymbol delta q sym` returns the state that can be reached by a transition in `delta` labeled `sym` from state `q`.

If there is more than one such transition, you should use the first transition you find, and ignore all others.

If there is no transition, use `error "incomplete automaton"` to abort execution.

    > followSymbol (delta faMod3a) 1 'a'
    2
    > followSymbol (delta faMod3a) 1 'b'
    1
    > followSymbol (delta faMod3a) 2 'a'
    3
    > followSymbol (delta faMod3a) 2 'b'
    2
    > followSymbol (delta faMod3a) 3 'a'
    1
    > followSymbol (delta faMod3a) 3 'b'
    3
    > followSymbol (delta faStartaEndc) 0 'a'
    1
    > followSymbol (delta faStartaEndc) 1 'a'
    1
    > followSymbol (delta faStartaEndc) 1 'b'
    1
    > followSymbol (delta faStartaEndc) 1 'c'
    2
    > followSymbol (delta faStartaEndc) 2 'a'
    1
    > followSymbol (delta faStartaEndc) 0 'b'
    99
    > followSymbol (delta faStartaEndc) 0 'c'
    99
    


### (C)

Code a function **`followString`** of type **`[(Int, Char, Int)] -> Int -> [Char] -> Int`** where `followString delta q syms`
returns the state that is reachable by following a sequence of transitions in `delta` labeled by the
symbols in `syms` (in order) starting from state `q`.

    > followString (delta faMod3a) 1 []
    1
    > followString (delta faMod3a) 1 ['a']
    2
    > followString (delta faMod3a) 1 ['a', 'b']
    2
    > followString (delta faMod3a) 1 ['a', 'b', 'a', 'b', 'a']
    1
    > followString (delta faMod3a) 2 ['a']
    3
    > followString (delta faMod3a) 2 ['a', 'b']
    3
    > followString (delta faMod3a) 2 ['a', 'b', 'a']
    1

    > followString (delta faStartaEndc) 0 []
    0
    > followString (delta faStartaEndc) 0 ['a']
    1
    > followString (delta faStartaEndc) 0 ['a', 'c']
    2
    > followString (delta faStartaEndc) 0 ['a', 'b', 'a', 'c']
    2
    > followString (delta faStartaEndc) 0 ['a', 'b', 'a', 'b']
    1
    > followString (delta faStartaEndc) 1 ['c']
    2
    > followString (delta faStartaEndc) 1 ['c', 'a']
    1
    > followString (delta faStartaEndc) 0 ['b']
    99
    > followString (delta faStartaEndc) 0 ['c', 'c']
    99

If at any point there is no transition to follow, use `error "incomplete automaton"` to abort execution.

**Hint**: Use `followSymbol`.

### (D)

Code a function **`accept`** of type **`FA -> String -> Bool`** where
`accept m s` returns True if and only if deterministic finite
automaton `m` accepts the string `s`. That is, it returns True exactly
when following the transitions of `m` labeled by the symbols in `s`
from the start state leads to a final state.

**Hint**: Use `followString` and `isFinal`.

For testing purposes, I've provided you with a function `lang` where
`lang m k` prints out all the strings of length up to `k` accepted by
finite automaton `m`. Note that this function assumes that you have a working ``accept``, so it will not work correctly until you have completed `accept`.

    > accept faMod3a ""
    True
    > accept faMod3a "babab"
    False
    > accept faMod3a "bababa"
    True
    > accept faMod3a "bababab"
    True
    > accept faMod3a "babababa"
    False

    > accept faStartaEndc ""
    False
    > accept faStartaEndc "ac"
    True
    > accept faStartaEndc "aabcabcabcc"
    True
    > accept faStartaEndc "aabcabcabcb"
    False
    > accept faStartaEndc "bbbcabcabcc"
    False
    
    > lang faMod3a 6
      <empty-string>
      b
      bb
      aaa
      bbb
      baaa
      abaa
      aaba
      aaab
      bbbb
      bbaaa
      babaa
      abbaa
      baaba
      ababa
      aabba
      baaab
      abaab
      aabab
      aaabb
      bbbbb
      aaaaaa
      bbbaaa
      bbabaa
      babbaa
      abbbaa
      bbaaba
      bababa
      abbaba
      baabba
      ababba
      aabbba
      bbaaab
      babaab
      abbaab
      baabab
      ababab
      aabbab
      baaabb
      abaabb
      aababb
      aaabbb
      bbbbbb
    
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


* * *

## Question 2: Constructing Deterministic Finite Automata

In this question, you will come up with deterministic finite
automata. Test your automata with the code you wrote in Question 1.

For submission purposes, I ask you to define those finite automata as
constants of type `FA` with a fixed name given below in each
question. There is already a dummy placeholder in `homework2.hs` for those
answers. Just replace the placeholder with your definition.



### (A)

Construct a finite automaton **`fa_part_a`** for the language consisting of
all strings over the alphabet `{a,b,c}` of length 3n or 3n+1 for any n
(i.e., of length 0, 1, 3, 4, 6, 7, 9, 10, 12, 13, ...)

    > lang fa_part_a 6
      <empty-string>
      a
      b
      c
      aaa
      baa
      caa
      aba
      bba
      cba
      aca
      bca
      cca
      aab
      bab
      cab
      abb
      bbb
      cbb
      acb
      bcb
      ccb
      aac
      bac
      cac
      abc
      bbc
      cbc
      acc
      bcc
      ccc
      aaaa
      baaa
      caaa
      abaa
      bbaa
      cbaa
      acaa
      bcaa
      ccaa
      aaba
      baba
      caba
      abba
      bbba
      cbba
      acba
      bcba
      ccba
      aaca
      baca
      caca
      abca
      bbca
      cbca
      acca
      bcca
      ccca
      aaab
      baab
      caab
      abab
      bbab
      cbab
      acab
      bcab
      ccab
      aabb
      babb
      cabb
      abbb
      bbbb
      cbbb
      acbb
      bcbb
      ccbb
      aacb
      bacb
      cacb
      abcb
      bbcb
      cbcb
      accb
      bccb
      cccb
      aaac
      baac
      caac
      abac
      bbac
      cbac
      acac
      bcac
      ccac
      aabc
      babc
      cabc
      abbc
      bbbc
      cbbc
      acbc
      bcbc
      ccbc
      aacc
      bacc
      cacc
      abcc
      bbcc
      cbcc
      accc
      bccc
      cccc
      aaaaaa
      baaaaa
      caaaaa
      abaaaa
      bbaaaa
      cbaaaa
      acaaaa
      bcaaaa
      ccaaaa
      aabaaa
      babaaa
      cabaaa
      abbaaa
      bbbaaa
      cbbaaa
      acbaaa
      bcbaaa
      ccbaaa
      aacaaa
      bacaaa
      cacaaa
      abcaaa
      bbcaaa
      cbcaaa
      accaaa
      bccaaa
      cccaaa
      aaabaa
      baabaa
      caabaa
      ababaa
      bbabaa
      cbabaa
      acabaa
      bcabaa
      ccabaa
      aabbaa
      babbaa
      cabbaa
      abbbaa
      bbbbaa
      cbbbaa
      acbbaa
      bcbbaa
      ccbbaa
      aacbaa
      bacbaa
      cacbaa
      abcbaa
      bbcbaa
      cbcbaa
      accbaa
      bccbaa
      cccbaa
      aaacaa
      baacaa
      caacaa
      abacaa
      bbacaa
      cbacaa
      acacaa
      bcacaa
      ccacaa
      aabcaa
      babcaa
      cabcaa
      abbcaa
      bbbcaa
      cbbcaa
      acbcaa
      bcbcaa
      ccbcaa
      aaccaa
      baccaa
      caccaa
      abccaa
      bbccaa
      cbccaa
      acccaa
      bcccaa
      ccccaa
      aaaaba
      baaaba
      caaaba
      abaaba
      bbaaba
      cbaaba
      acaaba
      bcaaba
      ccaaba
      aababa
      bababa
      cababa
      abbaba
      bbbaba
      cbbaba
      acbaba
      bcbaba
      ccbaba
      aacaba
      bacaba
      cacaba
      abcaba
      bbcaba
      cbcaba
      accaba
      bccaba
      cccaba
      aaabba
      baabba
      caabba
      ababba
      bbabba
      cbabba
      acabba
      bcabba
      ccabba
      aabbba
      babbba
      cabbba
      abbbba
      bbbbba
      cbbbba
      acbbba
      bcbbba
      ccbbba
      aacbba
      bacbba
      cacbba
      abcbba
      bbcbba
      cbcbba
      accbba
      bccbba
      cccbba
      aaacba
      baacba
      caacba
      abacba
      bbacba
      cbacba
      acacba
      bcacba
      ccacba
      aabcba
      babcba
      cabcba
      abbcba
      bbbcba
      cbbcba
      acbcba
      bcbcba
      ccbcba
      aaccba
      baccba
      caccba
      abccba
      bbccba
      cbccba
      acccba
      bcccba
      ccccba
      aaaaca
      baaaca
      caaaca
      abaaca
      bbaaca
      cbaaca
      acaaca
      bcaaca
      ccaaca
      aabaca
      babaca
      cabaca
      abbaca
      bbbaca
      cbbaca
      acbaca
      bcbaca
      ccbaca
      aacaca
      bacaca
      cacaca
      abcaca
      bbcaca
      cbcaca
      accaca
      bccaca
      cccaca
      aaabca
      baabca
      caabca
      ababca
      bbabca
      cbabca
      acabca
      bcabca
      ccabca
      aabbca
      babbca
      cabbca
      abbbca
      bbbbca
      cbbbca
      acbbca
      bcbbca
      ccbbca
      aacbca
      bacbca
      cacbca
      abcbca
      bbcbca
      cbcbca
      accbca
      bccbca
      cccbca
      aaacca
      baacca
      caacca
      abacca
      bbacca
      cbacca
      acacca
      bcacca
      ccacca
      aabcca
      babcca
      cabcca
      abbcca
      bbbcca
      cbbcca
      acbcca
      bcbcca
      ccbcca
      aaccca
      baccca
      caccca
      abccca
      bbccca
      cbccca
      acccca
      bcccca
      ccccca
      aaaaab
      baaaab
      caaaab
      abaaab
      bbaaab
      cbaaab
      acaaab
      bcaaab
      ccaaab
      aabaab
      babaab
      cabaab
      abbaab
      bbbaab
      cbbaab
      acbaab
      bcbaab
      ccbaab
      aacaab
      bacaab
      cacaab
      abcaab
      bbcaab
      cbcaab
      accaab
      bccaab
      cccaab
      aaabab
      baabab
      caabab
      ababab
      bbabab
      cbabab
      acabab
      bcabab
      ccabab
      aabbab
      babbab
      cabbab
      abbbab
      bbbbab
      cbbbab
      acbbab
      bcbbab
      ccbbab
      aacbab
      bacbab
      cacbab
      abcbab
      bbcbab
      cbcbab
      accbab
      bccbab
      cccbab
      aaacab
      baacab
      caacab
      abacab
      bbacab
      cbacab
      acacab
      bcacab
      ccacab
      aabcab
      babcab
      cabcab
      abbcab
      bbbcab
      cbbcab
      acbcab
      bcbcab
      ccbcab
      aaccab
      baccab
      caccab
      abccab
      bbccab
      cbccab
      acccab
      bcccab
      ccccab
      aaaabb
      baaabb
      caaabb
      abaabb
      bbaabb
      cbaabb
      acaabb
      bcaabb
      ccaabb
      aababb
      bababb
      cababb
      abbabb
      bbbabb
      cbbabb
      acbabb
      bcbabb
      ccbabb
      aacabb
      bacabb
      cacabb
      abcabb
      bbcabb
      cbcabb
      accabb
      bccabb
      cccabb
      aaabbb
      baabbb
      caabbb
      ababbb
      bbabbb
      cbabbb
      acabbb
      bcabbb
      ccabbb
      aabbbb
      babbbb
      cabbbb
      abbbbb
      bbbbbb
      cbbbbb
      acbbbb
      bcbbbb
      ccbbbb
      aacbbb
      bacbbb
      cacbbb
      abcbbb
      bbcbbb
      cbcbbb
      accbbb
      bccbbb
      cccbbb
      aaacbb
      baacbb
      caacbb
      abacbb
      bbacbb
      cbacbb
      acacbb
      bcacbb
      ccacbb
      aabcbb
      babcbb
      cabcbb
      abbcbb
      bbbcbb
      cbbcbb
      acbcbb
      bcbcbb
      ccbcbb
      aaccbb
      baccbb
      caccbb
      abccbb
      bbccbb
      cbccbb
      acccbb
      bcccbb
      ccccbb
      aaaacb
      baaacb
      caaacb
      abaacb
      bbaacb
      cbaacb
      acaacb
      bcaacb
      ccaacb
      aabacb
      babacb
      cabacb
      abbacb
      bbbacb
      cbbacb
      acbacb
      bcbacb
      ccbacb
      aacacb
      bacacb
      cacacb
      abcacb
      bbcacb
      cbcacb
      accacb
      bccacb
      cccacb
      aaabcb
      baabcb
      caabcb
      ababcb
      bbabcb
      cbabcb
      acabcb
      bcabcb
      ccabcb
      aabbcb
      babbcb
      cabbcb
      abbbcb
      bbbbcb
      cbbbcb
      acbbcb
      bcbbcb
      ccbbcb
      aacbcb
      bacbcb
      cacbcb
      abcbcb
      bbcbcb
      cbcbcb
      accbcb
      bccbcb
      cccbcb
      aaaccb
      baaccb
      caaccb
      abaccb
      bbaccb
      cbaccb
      acaccb
      bcaccb
      ccaccb
      aabccb
      babccb
      cabccb
      abbccb
      bbbccb
      cbbccb
      acbccb
      bcbccb
      ccbccb
      aacccb
      bacccb
      cacccb
      abcccb
      bbcccb
      cbcccb
      accccb
      bccccb
      cccccb
      aaaaac
      baaaac
      caaaac
      abaaac
      bbaaac
      cbaaac
      acaaac
      bcaaac
      ccaaac
      aabaac
      babaac
      cabaac
      abbaac
      bbbaac
      cbbaac
      acbaac
      bcbaac
      ccbaac
      aacaac
      bacaac
      cacaac
      abcaac
      bbcaac
      cbcaac
      accaac
      bccaac
      cccaac
      aaabac
      baabac
      caabac
      ababac
      bbabac
      cbabac
      acabac
      bcabac
      ccabac
      aabbac
      babbac
      cabbac
      abbbac
      bbbbac
      cbbbac
      acbbac
      bcbbac
      ccbbac
      aacbac
      bacbac
      cacbac
      abcbac
      bbcbac
      cbcbac
      accbac
      bccbac
      cccbac
      aaacac
      baacac
      caacac
      abacac
      bbacac
      cbacac
      acacac
      bcacac
      ccacac
      aabcac
      babcac
      cabcac
      abbcac
      bbbcac
      cbbcac
      acbcac
      bcbcac
      ccbcac
      aaccac
      baccac
      caccac
      abccac
      bbccac
      cbccac
      acccac
      bcccac
      ccccac
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
      aaabbc
      baabbc
      caabbc
      ababbc
      bbabbc
      cbabbc
      acabbc
      bcabbc
      ccabbc
      aabbbc
      babbbc
      cabbbc
      abbbbc
      bbbbbc
      cbbbbc
      acbbbc
      bcbbbc
      ccbbbc
      aacbbc
      bacbbc
      cacbbc
      abcbbc
      bbcbbc
      cbcbbc
      accbbc
      bccbbc
      cccbbc
      aaacbc
      baacbc
      caacbc
      abacbc
      bbacbc
      cbacbc
      acacbc
      bcacbc
      ccacbc
      aabcbc
      babcbc
      cabcbc
      abbcbc
      bbbcbc
      cbbcbc
      acbcbc
      bcbcbc
      ccbcbc
      aaccbc
      baccbc
      caccbc
      abccbc
      bbccbc
      cbccbc
      acccbc
      bcccbc
      ccccbc
      aaaacc
      baaacc
      caaacc
      abaacc
      bbaacc
      cbaacc
      acaacc
      bcaacc
      ccaacc
      aabacc
      babacc
      cabacc
      abbacc
      bbbacc
      cbbacc
      acbacc
      bcbacc
      ccbacc
      aacacc
      bacacc
      cacacc
      abcacc
      bbcacc
      cbcacc
      accacc
      bccacc
      cccacc
      aaabcc
      baabcc
      caabcc
      ababcc
      bbabcc
      cbabcc
      acabcc
      bcabcc
      ccabcc
      aabbcc
      babbcc
      cabbcc
      abbbcc
      bbbbcc
      cbbbcc
      acbbcc
      bcbbcc
      ccbbcc
      aacbcc
      bacbcc
      cacbcc
      abcbcc
      bbcbcc
      cbcbcc
      accbcc
      bccbcc
      cccbcc
      aaaccc
      baaccc
      caaccc
      abaccc
      bbaccc
      cbaccc
      acaccc
      bcaccc
      ccaccc
      aabccc
      babccc
      cabccc
      abbccc
      bbbccc
      cbbccc
      acbccc
      bcbccc
      ccbccc
      aacccc
      bacccc
      cacccc
      abcccc
      bbcccc
      cbcccc
      accccc
      bccccc
      cccccc


### (B)

Construct a finite automaton **`fa_part_b`** for the language
consisting of all strings over the alphabet `{a,b,c}` with exactly four `a`
and no `b` in them (and any number of `c`).

    > lang fa_part_b 6
      aaaa
      caaaa
      acaaa
      aacaa
      aaaca
      aaaac
      ccaaaa
      cacaaa
      accaaa
      caacaa
      acacaa
      aaccaa
      caaaca
      acaaca
      aacaca
      aaacca
      caaaac
      acaaac
      aacaac
      aaacac
      aaaacc


### (C)

Construct a finite automaton **`fa_part_c`** for the language
consisting of all strings over the alphabet `{a,b,c}` with exactly three `a`
and one `b` in them (and any number of `c`).

    > lang fa_part_c 6
      baaa
      abaa
      aaba
      aaab
      cbaaa
      bcaaa
      cabaa
      acbaa
      bacaa
      abcaa
      caaba
      acaba
      aacba
      baaca
      abaca
      aabca
      caaab
      acaab
      aacab
      aaacb
      baaac
      abaac
      aabac
      aaabc
      ccbaaa
      cbcaaa
      bccaaa
      ccabaa
      cacbaa
      accbaa
      cbacaa
      bcacaa
      cabcaa
      acbcaa
      baccaa
      abccaa
      ccaaba
      cacaba
      accaba
      caacba
      acacba
      aaccba
      cbaaca
      bcaaca
      cabaca
      acbaca
      bacaca
      abcaca
      caabca
      acabca
      aacbca
      baacca
      abacca
      aabcca
      ccaaab
      cacaab
      accaab
      caacab
      acacab
      aaccab
      caaacb
      acaacb
      aacacb
      aaaccb
      cbaaac
      bcaaac
      cabaac
      acbaac
      bacaac
      abcaac
      caabac
      acabac
      aacbac
      baacac
      abacac
      aabcac
      caaabc
      acaabc
      aacabc
      aaacbc
      baaacc
      abaacc
      aabacc
      aaabcc


### (D)

Construct a finite automaton **`fa_part_d`** for
the language consisting of all strings over the alphabet
`{a,b,c}` in which there are an odd number
of `a`, an even number of `b`, and any number of `c`.

    > lang fa_part_d 6
      a
      ca
      ac
      aaa
      bba
      cca
      bab
      abb
      cac
      acc
      caaa
      acaa
      cbba
      bcba
      aaca
      bbca
      ccca
      cbab
      bcab
      cabb
      acbb
      bacb
      abcb
      aaac
      bbac
      ccac
      babc
      abbc
      cacc
      accc
      aaaaa
      bbaaa
      ccaaa
      babaa
      abbaa
      cacaa
      accaa
      baaba
      ababa
      aabba
      bbbba
      ccbba
      cbcba
      bccba
      caaca
      acaca
      cbbca
      bcbca
      aacca
      bbcca
      cccca
      baaab
      abaab
      aabab
      bbbab
      ccbab
      cbcab
      bccab
      aaabb
      bbabb
      ccabb
      babbb
      abbbb
      cacbb
      accbb
      cbacb
      bcacb
      cabcb
      acbcb
      baccb
      abccb
      caaac
      acaac
      cbbac
      bcbac
      aacac
      bbcac
      cccac
      cbabc
      bcabc
      cabbc
      acbbc
      bacbc
      abcbc
      aaacc
      bbacc
      ccacc
      babcc
      abbcc
      caccc
      acccc
      caaaaa
      acaaaa
      cbbaaa
      bcbaaa
      aacaaa
      bbcaaa
      cccaaa
      cbabaa
      bcabaa
      cabbaa
      acbbaa
      bacbaa
      abcbaa
      aaacaa
      bbacaa
      ccacaa
      babcaa
      abbcaa
      caccaa
      acccaa
      cbaaba
      bcaaba
      cababa
      acbaba
      bacaba
      abcaba
      caabba
      acabba
      cbbbba
      bcbbba
      aacbba
      bbcbba
      cccbba
      baacba
      abacba
      aabcba
      bbbcba
      ccbcba
      cbccba
      bcccba
      aaaaca
      bbaaca
      ccaaca
      babaca
      abbaca
      cacaca
      accaca
      baabca
      ababca
      aabbca
      bbbbca
      ccbbca
      cbcbca
      bccbca
      caacca
      acacca
      cbbcca
      bcbcca
      aaccca
      bbccca
      ccccca
      cbaaab
      bcaaab
      cabaab
      acbaab
      bacaab
      abcaab
      caabab
      acabab
      cbbbab
      bcbbab
      aacbab
      bbcbab
      cccbab
      baacab
      abacab
      aabcab
      bbbcab
      ccbcab
      cbccab
      bcccab
      caaabb
      acaabb
      cbbabb
      bcbabb
      aacabb
      bbcabb
      cccabb
      cbabbb
      bcabbb
      cabbbb
      acbbbb
      bacbbb
      abcbbb
      aaacbb
      bbacbb
      ccacbb
      babcbb
      abbcbb
      caccbb
      acccbb
      baaacb
      abaacb
      aabacb
      bbbacb
      ccbacb
      cbcacb
      bccacb
      aaabcb
      bbabcb
      ccabcb
      babbcb
      abbbcb
      cacbcb
      accbcb
      cbaccb
      bcaccb
      cabccb
      acbccb
      bacccb
      abcccb
      aaaaac
      bbaaac
      ccaaac
      babaac
      abbaac
      cacaac
      accaac
      baabac
      ababac
      aabbac
      bbbbac
      ccbbac
      cbcbac
      bccbac
      caacac
      acacac
      cbbcac
      bcbcac
      aaccac
      bbccac
      ccccac
      baaabc
      abaabc
      aababc
      bbbabc
      ccbabc
      cbcabc
      bccabc
      aaabbc
      bbabbc
      ccabbc
      babbbc
      abbbbc
      cacbbc
      accbbc
      cbacbc
      bcacbc
      cabcbc
      acbcbc
      baccbc
      abccbc
      caaacc
      acaacc
      cbbacc
      bcbacc
      aacacc
      bbcacc
      cccacc
      cbabcc
      bcabcc
      cabbcc
      acbbcc
      bacbcc
      abcbcc
      aaaccc
      bbaccc
      ccaccc
      babccc
      abbccc
      cacccc
      accccc


### (E)

Construct a finite automaton **`fa_part_e`** for
the language consisting of all strings over the alphabet
`{a,b,c}` in which there are an even number
of `a`, a multiple-of-3 number of `b`, and no `c`.

    > lang fa_part_e 7
      <empty-string>
      aa
      bbb
      aaaa
      bbbaa
      bbaba
      babba
      abbba
      bbaab
      babab
      abbab
      baabb
      ababb
      aabbb
      aaaaaa
      bbbbbb
      bbbaaaa
      bbabaaa
      babbaaa
      abbbaaa
      bbaabaa
      bababaa
      abbabaa
      baabbaa
      ababbaa
      aabbbaa
      bbaaaba
      babaaba
      abbaaba
      baababa
      abababa
      aabbaba
      baaabba
      abaabba
      aababba
      aaabbba
      bbaaaab
      babaaab
      abbaaab
      baabaab
      ababaab
      aabbaab
      baaabab
      abaabab
      aababab
      aaabbab
      baaaabb
      abaaabb
      aabaabb
      aaababb
      aaaabbb


***

## Question 3: Functions as First-Class Values

We saw in class that functions are first-class values in Haskell: they can be passed to other functions as arguments, and returned from functions as results. They can also be stored in lists, tuples, or records.


### (A)

Code a function **`makeFunction`** of type **`[(Int, a)] -> a -> (Int -> a)`** which takes a list `gr` of tuples of the form (_input_, _output_) and a default, and create a function of one parameter that takes a value and returns the output corresponding to that value in list `gr`, or the default if the value is not an input in `gr`.

    > f = makeFunction [] "nothing"
    > f 0
    "nothing"
    > f 1
    "nothing"
    > f 100
    "nothing"
    
    > g = makeFunction [(0, "zero"), (1, "one"), (2, "two"), (3, "three")] "unknown"
    > g 0
    "zero"
    > g 1
    "one"
    > g 2
    "two"
    > g 3
    "three"
    > g 4
    "unknown"
    > g (-2)
    "unknown"


### (B)

Code a function **`functionGraph`** of type **`(Int -> a) -> [Int] -> [(Int, a)]`** which takes a function `f` and a _domain of definition_ and returns the graph of the function over the domain, namely the list of tuple `(v, f v)` for every `v` in the domain of definition.

    > functionGraph (\x -> 2 * x) []
    []
    
    > functionGraph (\x -> 2 * x) [1, 2, 3, 4]
    [(1,2),(2,4),(3,6),(4,8)]
    
    > functionGraph (\x -> abs x) [-1, -2, -3, 1, 2, 3]
    [(-1,1),(-2,2),(-3,3),(1,1),(2,2),(3,3)]
