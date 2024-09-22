<script>
  document.title = 'Homework 1'
</script>

# Homework 1

## Due Date: Sunday, Sep 29, 2024 (23h59)

- This homework is to be done individually. You may discuss problems
with fellow students, but all submitted work must be entirely your
own, and should not be from any other course, present, past, or
future. If you use a solution from another source you must cite it
&mdash; this includes when that source is someone else helping
you.

- Please do not post your solutions on a public website or a public repository (including GitHub).

- All programming is to be done in Python 3.

- Code your answers by modifying the file [`homework1.py`](homework1.py) provided. Add your **name**, your **email address**, and any **remarks** that you
wish to make to the instructor to the block comment at the head of the file.

- Please do not change the "signature" (number and interpretation of arguments) of the
functions. Doing so will make it impossible to load your code into the testing infrastructure, and
make me unhappy.

- Feel free to define helper functions if you need them.


**Electronic Submission Instructions**

- Make your your file `homework1.py` loads without error in a fresh Python 3 shell.

- Submission instructions following shortly, probably via Canvas.

* * *

## Deterministic Finite Automata

Consider a structure for finite automata in Python, as a dictionary:

    { 
       "states": ...,
       "alphabet": ...
       "delta": ...,
       "start": ...,
       "final": ...
    }

A finite automaton is described by a dictionary with fields holding the various components of the
automaton: a set of states, a set of symbols (the alphabet), a transition function &delta;, a start state, and
a set of final states. Lists are used to represent sets. The transition function is represented
using list of triples (_p_, _a_, _q_), stating that in state _p_, reading symbol _a_ makes the machine
transition to state _q_. States can be represented by any value that support equality checking.

A finite automaton is _deterministic_ if for every state _p_ and every symbol _a_ there is
exactly one transition out of _p_ labelled _a_ in the automaton. 

Here is a deterministic finite automaton that accepts the set of all strings over
{`a`, `b`, `c`} that contains a number of `a`s that is a multiple of 3:

    DFA_MOD_3 = {
        "states": [1, 2, 3],
        "alphabet": ["a", "b", "c"],
        "delta": [
            (1, "a", 2),
            (2, "a", 3),
            (3, "a", 1),
            (1, "b", 1),
            (2, "b", 2),
            (3, "b", 3),
            (1, "c", 1),
            (2, "c", 2),
            (3, "c", 3)
        ],
        "start": 1,
        "final": [1]
    }

Here is a deterministic finite automaton that accepts the language of all strings over {`a`, `b`, `c`} that start with an `a` and end with a `c` — notice how 99 is a sink state:

    DFA_START_END = {
        "states": [0, 1, 2, 99],
        "alphabet": ["a", "b", "c"],
        "delta": [
            (0, "a", 1),
            (0, "b", 99),
            (0, "c", 99),
            (1, "a", 1),
            (1, "b", 1),
            (1, "c", 2),
            (2, "a", 1),
            (2, "b", 1),
            (2, "c", 2),
            (99, "a", 99),
            (99, "b", 99),
            (99, "c", 99),
        ],
        "start": 0,
        "final": [2]
    }


* * *


## Question 1: Simulating Deterministic Finite Automata

We are going to code up a simulator for deterministic finite automata. Deterministic finite automata
are really easy to simulate: start at the start state, and follow transitions over
symbols from the input string until you reach the end of the string. Accept if you are on a final
state. 

Code a function **`accept_dfa(m, input)`** where `accept(m, s)` returns `True` if if deterministic
finite automaton `m` accepts the string `s`, and `False` otherwise. That is, it returns true exactly
if there is a way to follow the transitions of `m` labeled by the symbols in `s` from the start
state to a final state.

If you ever discover that the automaton is not deterministic (that is, at some point, we
either have no transitions to follow or more than one transition to follow), throw an exception.

For testing purposes, I've provided you with a function `language` where
`language(m, k, acc)` prints out all the strings of length up to `k` accepted by
finite automaton `m` via acceptance function `acc`. 

Sample output:

    >>> accept_dfa(DFA_MOD_3, "")
    True
    >>> accept_dfa(DFA_MOD_3, "a")
    False
    >>> accept_dfa(DFA_MOD_3, "aa")
    False
    >>> accept_dfa(DFA_MOD_3, "aaa")
    True
    >>> accept_dfa(DFA_MOD_3, "aaaa")
    False
    >>> accept_dfa(DFA_MOD_3, "aaaaa")
    False
    >>> accept_dfa(DFA_MOD_3, "aaaaaa")
    True

    >>> accept_dfa(DFA_MOD_3, "bacab")
    False
    >>> accept_dfa(DFA_MOD_3, "bacaba")
    True
    >>> accept_dfa(DFA_MOD_3, "bacabac")
    True
    >>> accept_dfa(DFA_MOD_3, "bacabaca")
    False
    
    >>> accept_dfa(DFA_START_END, "")
    False
    >>> accept_dfa(DFA_START_END, "a")
    False
    >>> accept_dfa(DFA_START_END, "ab")
    False
    >>> accept_dfa(DFA_START_END, "abc")
    True
    >>> accept_dfa(DFA_START_END, "abca")
    False
    >>> accept_dfa(DFA_START_END, "abcb")
    False
    >>> accept_dfa(DFA_START_END, "abcc")
    True
    >>> accept_dfa(DFA_START_END, "cabcc")
    False
    >>> accept_dfa(DFA_START_END, "babcc")
    False
    >>> accept_dfa(DFA_START_END, "aabcc")
    True
    
    >>> language(DFA_MOD_3, 6, accept_dfa)
    <empty string>
    b
    c
    bb
    bc
    cb
    cc
    aaa
    bbb
    bbc
    bcb
    bcc
    cbb
    cbc
    ccb
    ccc
    aaab
    aaac
    aaba
    aaca
    abaa
    acaa
    baaa
    bbbb
    bbbc
    bbcb
    bbcc
    bcbb
    bcbc
    bccb
    bccc
    caaa
    cbbb
    cbbc
    cbcb
    cbcc
    ccbb
    ccbc
    cccb
    cccc
    aaabb
    aaabc
    aaacb
    aaacc
    aabab
    aabac
    aabba
    aabca
    aacab
    aacac
    aacba
    aacca
    abaab
    abaac
    ababa
    abaca
    abbaa
    abcaa
    acaab
    acaac
    acaba
    acaca
    acbaa
    accaa
    baaab
    baaac
    baaba
    baaca
    babaa
    bacaa
    bbaaa
    bbbbb
    bbbbc
    bbbcb
    bbbcc
    bbcbb
    bbcbc
    bbccb
    bbccc
    bcaaa
    bcbbb
    bcbbc
    bcbcb
    bcbcc
    bccbb
    bccbc
    bcccb
    bcccc
    caaab
    caaac
    caaba
    caaca
    cabaa
    cacaa
    cbaaa
    cbbbb
    cbbbc
    cbbcb
    cbbcc
    cbcbb
    cbcbc
    cbccb
    cbccc
    ccaaa
    ccbbb
    ccbbc
    ccbcb
    ccbcc
    cccbb
    cccbc
    ccccb
    ccccc
    aaaaaa
    aaabbb
    aaabbc
    aaabcb
    aaabcc
    aaacbb
    aaacbc
    aaaccb
    aaaccc
    aababb
    aababc
    aabacb
    aabacc
    aabbab
    aabbac
    aabbba
    aabbca
    aabcab
    aabcac
    aabcba
    aabcca
    aacabb
    aacabc
    aacacb
    aacacc
    aacbab
    aacbac
    aacbba
    aacbca
    aaccab
    aaccac
    aaccba
    aaccca
    abaabb
    abaabc
    abaacb
    abaacc
    ababab
    ababac
    ababba
    ababca
    abacab
    abacac
    abacba
    abacca
    abbaab
    abbaac
    abbaba
    abbaca
    abbbaa
    abbcaa
    abcaab
    abcaac
    abcaba
    abcaca
    abcbaa
    abccaa
    acaabb
    acaabc
    acaacb
    acaacc
    acabab
    acabac
    acabba
    acabca
    acacab
    acacac
    acacba
    acacca
    acbaab
    acbaac
    acbaba
    acbaca
    acbbaa
    acbcaa
    accaab
    accaac
    accaba
    accaca
    accbaa
    acccaa
    baaabb
    baaabc
    baaacb
    baaacc
    baabab
    baabac
    baabba
    baabca
    baacab
    baacac
    baacba
    baacca
    babaab
    babaac
    bababa
    babaca
    babbaa
    babcaa
    bacaab
    bacaac
    bacaba
    bacaca
    bacbaa
    baccaa
    bbaaab
    bbaaac
    bbaaba
    bbaaca
    bbabaa
    bbacaa
    bbbaaa
    bbbbbb
    bbbbbc
    bbbbcb
    bbbbcc
    bbbcbb
    bbbcbc
    bbbccb
    bbbccc
    bbcaaa
    bbcbbb
    bbcbbc
    bbcbcb
    bbcbcc
    bbccbb
    bbccbc
    bbcccb
    bbcccc
    bcaaab
    bcaaac
    bcaaba
    bcaaca
    bcabaa
    bcacaa
    bcbaaa
    bcbbbb
    bcbbbc
    bcbbcb
    bcbbcc
    bcbcbb
    bcbcbc
    bcbccb
    bcbccc
    bccaaa
    bccbbb
    bccbbc
    bccbcb
    bccbcc
    bcccbb
    bcccbc
    bccccb
    bccccc
    caaabb
    caaabc
    caaacb
    caaacc
    caabab
    caabac
    caabba
    caabca
    caacab
    caacac
    caacba
    caacca
    cabaab
    cabaac
    cababa
    cabaca
    cabbaa
    cabcaa
    cacaab
    cacaac
    cacaba
    cacaca
    cacbaa
    caccaa
    cbaaab
    cbaaac
    cbaaba
    cbaaca
    cbabaa
    cbacaa
    cbbaaa
    cbbbbb
    cbbbbc
    cbbbcb
    cbbbcc
    cbbcbb
    cbbcbc
    cbbccb
    cbbccc
    cbcaaa
    cbcbbb
    cbcbbc
    cbcbcb
    cbcbcc
    cbccbb
    cbccbc
    cbcccb
    cbcccc
    ccaaab
    ccaaac
    ccaaba
    ccaaca
    ccabaa
    ccacaa
    ccbaaa
    ccbbbb
    ccbbbc
    ccbbcb
    ccbbcc
    ccbcbb
    ccbcbc
    ccbccb
    ccbccc
    cccaaa
    cccbbb
    cccbbc
    cccbcb
    cccbcc
    ccccbb
    ccccbc
    cccccb
    cccccc
    
    >>> language(DFA_START_END, 6, accept_dfa)
    ac
    aac
    abc
    acc
    aaac
    aabc
    aacc
    abac
    abbc
    abcc
    acac
    acbc
    accc
    aaaac
    aaabc
    aaacc
    aabac
    aabbc
    aabcc
    aacac
    aacbc
    aaccc
    abaac
    ababc
    abacc
    abbac
    abbbc
    abbcc
    abcac
    abcbc
    abccc
    acaac
    acabc
    acacc
    acbac
    acbbc
    acbcc
    accac
    accbc
    acccc
    aaaaac
    aaaabc
    aaaacc
    aaabac
    aaabbc
    aaabcc
    aaacac
    aaacbc
    aaaccc
    aabaac
    aababc
    aabacc
    aabbac
    aabbbc
    aabbcc
    aabcac
    aabcbc
    aabccc
    aacaac
    aacabc
    aacacc
    aacbac
    aacbbc
    aacbcc
    aaccac
    aaccbc
    aacccc
    abaaac
    abaabc
    abaacc
    ababac
    ababbc
    ababcc
    abacac
    abacbc
    abaccc
    abbaac
    abbabc
    abbacc
    abbbac
    abbbbc
    abbbcc
    abbcac
    abbcbc
    abbccc
    abcaac
    abcabc
    abcacc
    abcbac
    abcbbc
    abcbcc
    abccac
    abccbc
    abcccc
    acaaac
    acaabc
    acaacc
    acabac
    acabbc
    acabcc
    acacac
    acacbc
    acaccc
    acbaac
    acbabc
    acbacc
    acbbac
    acbbbc
    acbbcc
    acbcac
    acbcbc
    acbccc
    accaac
    accabc
    accacc
    accbac
    accbbc
    accbcc
    acccac
    acccbc
    accccc


* * *

## Question 2: Constructing Deterministic Finite Automata

In this question, you will come up with deterministic finite automata. You can test
your automata with the code you wrote in Question 1.

For submission purposes, I ask you to define those finite automata as
constants with a fixed name given below in each
question. There are already placeholders in `homework1.py` for those
answers. Just replace each placeholder with your definition.


### (A)

Construct a finite automaton **`DFA_Q2A`** for the language consisting of
all strings over the alphabet `{a,b,c}` of length 3n+1 or 3n+2 for any n
(i.e., of length 1, 2, 4, 5, 7, 8, 10, 11, ...)

Sample output:

    >>> language(DFA_Q2A, 6, accept_dfa)
    a
    b
    c
    aa
    ab
    ac
    ba
    bb
    bc
    ca
    cb
    cc
    aaaa
    aaab
    aaac
    aaba
    aabb
    aabc
    aaca
    aacb
    aacc
    abaa
    abab
    abac
    abba
    abbb
    abbc
    abca
    abcb
    abcc
    acaa
    acab
    acac
    acba
    acbb
    acbc
    acca
    accb
    accc
    baaa
    baab
    baac
    baba
    babb
    babc
    baca
    bacb
    bacc
    bbaa
    bbab
    bbac
    bbba
    bbbb
    bbbc
    bbca
    bbcb
    bbcc
    bcaa
    bcab
    bcac
    bcba
    bcbb
    bcbc
    bcca
    bccb
    bccc
    caaa
    caab
    caac
    caba
    cabb
    cabc
    caca
    cacb
    cacc
    cbaa
    cbab
    cbac
    cbba
    cbbb
    cbbc
    cbca
    cbcb
    cbcc
    ccaa
    ccab
    ccac
    ccba
    ccbb
    ccbc
    ccca
    cccb
    cccc
    aaaaa
    aaaab
    aaaac
    aaaba
    aaabb
    aaabc
    aaaca
    aaacb
    aaacc
    aabaa
    aabab
    aabac
    aabba
    aabbb
    aabbc
    aabca
    aabcb
    aabcc
    aacaa
    aacab
    aacac
    aacba
    aacbb
    aacbc
    aacca
    aaccb
    aaccc
    abaaa
    abaab
    abaac
    ababa
    ababb
    ababc
    abaca
    abacb
    abacc
    abbaa
    abbab
    abbac
    abbba
    abbbb
    abbbc
    abbca
    abbcb
    abbcc
    abcaa
    abcab
    abcac
    abcba
    abcbb
    abcbc
    abcca
    abccb
    abccc
    acaaa
    acaab
    acaac
    acaba
    acabb
    acabc
    acaca
    acacb
    acacc
    acbaa
    acbab
    acbac
    acbba
    acbbb
    acbbc
    acbca
    acbcb
    acbcc
    accaa
    accab
    accac
    accba
    accbb
    accbc
    accca
    acccb
    acccc
    baaaa
    baaab
    baaac
    baaba
    baabb
    baabc
    baaca
    baacb
    baacc
    babaa
    babab
    babac
    babba
    babbb
    babbc
    babca
    babcb
    babcc
    bacaa
    bacab
    bacac
    bacba
    bacbb
    bacbc
    bacca
    baccb
    baccc
    bbaaa
    bbaab
    bbaac
    bbaba
    bbabb
    bbabc
    bbaca
    bbacb
    bbacc
    bbbaa
    bbbab
    bbbac
    bbbba
    bbbbb
    bbbbc
    bbbca
    bbbcb
    bbbcc
    bbcaa
    bbcab
    bbcac
    bbcba
    bbcbb
    bbcbc
    bbcca
    bbccb
    bbccc
    bcaaa
    bcaab
    bcaac
    bcaba
    bcabb
    bcabc
    bcaca
    bcacb
    bcacc
    bcbaa
    bcbab
    bcbac
    bcbba
    bcbbb
    bcbbc
    bcbca
    bcbcb
    bcbcc
    bccaa
    bccab
    bccac
    bccba
    bccbb
    bccbc
    bccca
    bcccb
    bcccc
    caaaa
    caaab
    caaac
    caaba
    caabb
    caabc
    caaca
    caacb
    caacc
    cabaa
    cabab
    cabac
    cabba
    cabbb
    cabbc
    cabca
    cabcb
    cabcc
    cacaa
    cacab
    cacac
    cacba
    cacbb
    cacbc
    cacca
    caccb
    caccc
    cbaaa
    cbaab
    cbaac
    cbaba
    cbabb
    cbabc
    cbaca
    cbacb
    cbacc
    cbbaa
    cbbab
    cbbac
    cbbba
    cbbbb
    cbbbc
    cbbca
    cbbcb
    cbbcc
    cbcaa
    cbcab
    cbcac
    cbcba
    cbcbb
    cbcbc
    cbcca
    cbccb
    cbccc
    ccaaa
    ccaab
    ccaac
    ccaba
    ccabb
    ccabc
    ccaca
    ccacb
    ccacc
    ccbaa
    ccbab
    ccbac
    ccbba
    ccbbb
    ccbbc
    ccbca
    ccbcb
    ccbcc
    cccaa
    cccab
    cccac
    cccba
    cccbb
    cccbc
    cccca
    ccccb
    ccccc


### (B)

Construct a finite automaton **`DFA_Q2B`** for the language
consisting of all strings over the alphabet `{a,b,c}` with exactly two `a`
and no `b` in them (and any number of `c`).

Sample output:

    >>> language(DFA_Q2B, 6, accept_dfa)
    aa
    aac
    aca
    caa
    aacc
    acac
    acca
    caac
    caca
    ccaa
    aaccc
    acacc
    accac
    accca
    caacc
    cacac
    cacca
    ccaac
    ccaca
    cccaa
    aacccc
    acaccc
    accacc
    acccac
    acccca
    caaccc
    cacacc
    caccac
    caccca
    ccaacc
    ccacac
    ccacca
    cccaac
    cccaca
    ccccaa


### (C)

Construct a finite automaton **`DFA_Q2C`** for the language
consisting of all strings over the alphabet `{a,b,c}` with exactly two `a`
and one `b` in them (and any number of `c`).

Sample output:

    >>> language(DFA_Q2C, 6, accept_dfa)
    aab
    aba
    baa
    aabc
    aacb
    abac
    abca
    acab
    acba
    baac
    baca
    bcaa
    caab
    caba
    cbaa
    aabcc
    aacbc
    aaccb
    abacc
    abcac
    abcca
    acabc
    acacb
    acbac
    acbca
    accab
    accba
    baacc
    bacac
    bacca
    bcaac
    bcaca
    bccaa
    caabc
    caacb
    cabac
    cabca
    cacab
    cacba
    cbaac
    cbaca
    cbcaa
    ccaab
    ccaba
    ccbaa
    aabccc
    aacbcc
    aaccbc
    aacccb
    abaccc
    abcacc
    abccac
    abccca
    acabcc
    acacbc
    acaccb
    acbacc
    acbcac
    acbcca
    accabc
    accacb
    accbac
    accbca
    acccab
    acccba
    baaccc
    bacacc
    baccac
    baccca
    bcaacc
    bcacac
    bcacca
    bccaac
    bccaca
    bcccaa
    caabcc
    caacbc
    caaccb
    cabacc
    cabcac
    cabcca
    cacabc
    cacacb
    cacbac
    cacbca
    caccab
    caccba
    cbaacc
    cbacac
    cbacca
    cbcaac
    cbcaca
    cbccaa
    ccaabc
    ccaacb
    ccabac
    ccabca
    ccacab
    ccacba
    ccbaac
    ccbaca
    ccbcaa
    cccaab
    cccaba
    cccbaa


### (D)

Construct a finite automaton **`DFA_Q2D`** for
the language consisting of all strings over the alphabet
`{a,b,c}` in which there are an odd number
of `a`, an even number of `b`, and any number of `c`.

Sample output:

    >>> language(DFA_Q2D, 6, accept_dfa)
    a
    ac
    ca
    aaa
    abb
    acc
    bab
    bba
    cac
    cca
    aaac
    aaca
    abbc
    abcb
    acaa
    acbb
    accc
    babc
    bacb
    bbac
    bbca
    bcab
    bcba
    caaa
    cabb
    cacc
    cbab
    cbba
    ccac
    ccca
    aaaaa
    aaabb
    aaacc
    aabab
    aabba
    aacac
    aacca
    abaab
    ababa
    abbaa
    abbbb
    abbcc
    abcbc
    abccb
    acaac
    acaca
    acbbc
    acbcb
    accaa
    accbb
    acccc
    baaab
    baaba
    babaa
    babbb
    babcc
    bacbc
    baccb
    bbaaa
    bbabb
    bbacc
    bbbab
    bbbba
    bbcac
    bbcca
    bcabc
    bcacb
    bcbac
    bcbca
    bccab
    bccba
    caaac
    caaca
    cabbc
    cabcb
    cacaa
    cacbb
    caccc
    cbabc
    cbacb
    cbbac
    cbbca
    cbcab
    cbcba
    ccaaa
    ccabb
    ccacc
    ccbab
    ccbba
    cccac
    cccca
    aaaaac
    aaaaca
    aaabbc
    aaabcb
    aaacaa
    aaacbb
    aaaccc
    aababc
    aabacb
    aabbac
    aabbca
    aabcab
    aabcba
    aacaaa
    aacabb
    aacacc
    aacbab
    aacbba
    aaccac
    aaccca
    abaabc
    abaacb
    ababac
    ababca
    abacab
    abacba
    abbaac
    abbaca
    abbbbc
    abbbcb
    abbcaa
    abbcbb
    abbccc
    abcaab
    abcaba
    abcbaa
    abcbbb
    abcbcc
    abccbc
    abcccb
    acaaaa
    acaabb
    acaacc
    acabab
    acabba
    acacac
    acacca
    acbaab
    acbaba
    acbbaa
    acbbbb
    acbbcc
    acbcbc
    acbccb
    accaac
    accaca
    accbbc
    accbcb
    acccaa
    acccbb
    accccc
    baaabc
    baaacb
    baabac
    baabca
    baacab
    baacba
    babaac
    babaca
    babbbc
    babbcb
    babcaa
    babcbb
    babccc
    bacaab
    bacaba
    bacbaa
    bacbbb
    bacbcc
    baccbc
    bacccb
    bbaaac
    bbaaca
    bbabbc
    bbabcb
    bbacaa
    bbacbb
    bbaccc
    bbbabc
    bbbacb
    bbbbac
    bbbbca
    bbbcab
    bbbcba
    bbcaaa
    bbcabb
    bbcacc
    bbcbab
    bbcbba
    bbccac
    bbccca
    bcaaab
    bcaaba
    bcabaa
    bcabbb
    bcabcc
    bcacbc
    bcaccb
    bcbaaa
    bcbabb
    bcbacc
    bcbbab
    bcbbba
    bcbcac
    bcbcca
    bccabc
    bccacb
    bccbac
    bccbca
    bcccab
    bcccba
    caaaaa
    caaabb
    caaacc
    caabab
    caabba
    caacac
    caacca
    cabaab
    cababa
    cabbaa
    cabbbb
    cabbcc
    cabcbc
    cabccb
    cacaac
    cacaca
    cacbbc
    cacbcb
    caccaa
    caccbb
    cacccc
    cbaaab
    cbaaba
    cbabaa
    cbabbb
    cbabcc
    cbacbc
    cbaccb
    cbbaaa
    cbbabb
    cbbacc
    cbbbab
    cbbbba
    cbbcac
    cbbcca
    cbcabc
    cbcacb
    cbcbac
    cbcbca
    cbccab
    cbccba
    ccaaac
    ccaaca
    ccabbc
    ccabcb
    ccacaa
    ccacbb
    ccaccc
    ccbabc
    ccbacb
    ccbbac
    ccbbca
    ccbcab
    ccbcba
    cccaaa
    cccabb
    cccacc
    cccbab
    cccbba
    ccccac
    ccccca


### (E)

Construct a finite automaton **`DFA_Q2E`** for
the language consisting of all strings over the alphabet
`{a,b,c}` in which there are an odd number
of `a`, an even number of `b`, and exactly one `c`.

Sample output:

    >>> language(DFA_Q2E, 6, accept_dfa)
    ac
    bc
    ca
    cb
    aaac
    aabc
    aaca
    aacb
    abac
    abbc
    abca
    abcb
    acaa
    acab
    acba
    acbb
    baac
    babc
    baca
    bacb
    bbac
    bbbc
    bbca
    bbcb
    bcaa
    bcab
    bcba
    bcbb
    caaa
    caab
    caba
    cabb
    cbaa
    cbab
    cbba
    cbbb
    aaaaac
    aaaabc
    aaaaca
    aaaacb
    aaabac
    aaabbc
    aaabca
    aaabcb
    aaacaa
    aaacab
    aaacba
    aaacbb
    aabaac
    aababc
    aabaca
    aabacb
    aabbac
    aabbbc
    aabbca
    aabbcb
    aabcaa
    aabcab
    aabcba
    aabcbb
    aacaaa
    aacaab
    aacaba
    aacabb
    aacbaa
    aacbab
    aacbba
    aacbbb
    abaaac
    abaabc
    abaaca
    abaacb
    ababac
    ababbc
    ababca
    ababcb
    abacaa
    abacab
    abacba
    abacbb
    abbaac
    abbabc
    abbaca
    abbacb
    abbbac
    abbbbc
    abbbca
    abbbcb
    abbcaa
    abbcab
    abbcba
    abbcbb
    abcaaa
    abcaab
    abcaba
    abcabb
    abcbaa
    abcbab
    abcbba
    abcbbb
    acaaaa
    acaaab
    acaaba
    acaabb
    acabaa
    acabab
    acabba
    acabbb
    acbaaa
    acbaab
    acbaba
    acbabb
    acbbaa
    acbbab
    acbbba
    acbbbb
    baaaac
    baaabc
    baaaca
    baaacb
    baabac
    baabbc
    baabca
    baabcb
    baacaa
    baacab
    baacba
    baacbb
    babaac
    bababc
    babaca
    babacb
    babbac
    babbbc
    babbca
    babbcb
    babcaa
    babcab
    babcba
    babcbb
    bacaaa
    bacaab
    bacaba
    bacabb
    bacbaa
    bacbab
    bacbba
    bacbbb
    bbaaac
    bbaabc
    bbaaca
    bbaacb
    bbabac
    bbabbc
    bbabca
    bbabcb
    bbacaa
    bbacab
    bbacba
    bbacbb
    bbbaac
    bbbabc
    bbbaca
    bbbacb
    bbbbac
    bbbbbc
    bbbbca
    bbbbcb
    bbbcaa
    bbbcab
    bbbcba
    bbbcbb
    bbcaaa
    bbcaab
    bbcaba
    bbcabb
    bbcbaa
    bbcbab
    bbcbba
    bbcbbb
    bcaaaa
    bcaaab
    bcaaba
    bcaabb
    bcabaa
    bcabab
    bcabba
    bcabbb
    bcbaaa
    bcbaab
    bcbaba
    bcbabb
    bcbbaa
    bcbbab
    bcbbba
    bcbbbb
    caaaaa
    caaaab
    caaaba
    caaabb
    caabaa
    caabab
    caabba
    caabbb
    cabaaa
    cabaab
    cababa
    cababb
    cabbaa
    cabbab
    cabbba
    cabbbb
    cbaaaa
    cbaaab
    cbaaba
    cbaabb
    cbabaa
    cbabab
    cbabba
    cbabbb
    cbbaaa
    cbbaab
    cbbaba
    cbbabb
    cbbbaa
    cbbbab
    cbbbba
    cbbbbb


* * *

## Question 3: Combining Deterministic Finite Automata

In this question, we will implement the construction we saw in class to compute the **intersection** of two languages. This construction is described in Chen 1.2.2 under the name _the product construction_. 

Code a function **`inter(m1, m2)`** where `inter(m1, m2)` takes two DFAs `m1` and `m2` and returns a **new** DFA with the property that if `m1` accepts language A and `m2` accepts language B, then `inter(m1, m2)` is a DFA that accepts language A ∩ B.

Notice that this is a function that takes two dictionaries and that returns a new dictionary. We're not "executing" any automaton here. We're just constructing a new automaton from two other automata.

Your final automata doesn't need to look exactly like the one I give you a sample output for below. As long as it accepts exactly A ∩ B, I'll be happy.

Sample output:

    >>> inter(DFA_MOD_3, DFA_START_END)
    {
       'states': [
         (1, 0), 
         (1, 1), 
         (1, 2), 
         (1, 99), 
         (2, 0), 
         (2, 1), 
         (2, 2), 
         (2, 99), 
         (3, 0), 
         (3, 1), 
         (3, 2), 
         (3, 99)
       ], 
       'alphabet': ['a', 'b', 'c'], 
       'delta': [
         ((1, 0), 'a', (2, 1)), 
         ((1, 0), 'b', (1, 99)), 
         ((1, 0), 'c', (1, 99)), 
         ((1, 1), 'a', (2, 1)), 
         ((1, 1), 'b', (1, 1)), 
         ((1, 1), 'c', (1, 2)), 
         ((1, 2), 'a', (2, 1)), 
         ((1, 2), 'b', (1, 1)), 
         ((1, 2), 'c', (1, 2)), 
         ((1, 99), 'a', (2, 99)), 
         ((1, 99), 'b', (1, 99)), 
         ((1, 99), 'c', (1, 99)), 
         ((2, 0), 'a', (3, 1)), 
         ((2, 0), 'b', (2, 99)), 
         ((2, 0), 'c', (2, 99)), 
         ((2, 1), 'a', (3, 1)), 
         ((2, 1), 'b', (2, 1)), 
         ((2, 1), 'c', (2, 2)), 
         ((2, 2), 'a', (3, 1)), 
         ((2, 2), 'b', (2, 1)), 
         ((2, 2), 'c', (2, 2)), 
         ((2, 99), 'a', (3, 99)), 
         ((2, 99), 'b', (2, 99)), 
         ((2, 99), 'c', (2, 99)), 
         ((3, 0), 'a', (1, 1)), 
         ((3, 0), 'b', (3, 99)), 
         ((3, 0), 'c', (3, 99)), 
         ((3, 1), 'a', (1, 1)), 
         ((3, 1), 'b', (3, 1)), 
         ((3, 1), 'c', (3, 2)), 
         ((3, 2), 'a', (1, 1)), 
         ((3, 2), 'b', (3, 1)), 
         ((3, 2), 'c', (3, 2)), 
         ((3, 99), 'a', (1, 99)), 
         ((3, 99), 'b', (3, 99)), 
         ((3, 99), 'c', (3, 99))], 
       'start': (1, 0), 
       'final': [(1, 2)]
     }

    >>> language(inter(DFA_MOD_3, DFA_START_END), 6, accept_dfa)
    # The language of strings starting with a and ending with c with mod 3 a's in them.
    aaac
    aaabc
    aaacc
    aabac
    aacac
    abaac
    acaac
    aaabbc
    aaabcc
    aaacbc
    aaaccc
    aababc
    aabacc
    aabbac
    aabcac
    aacabc
    aacacc
    aacbac
    aaccac
    abaabc
    abaacc
    ababac
    abacac
    abbaac
    abcaac
    acaabc
    acaacc
    acabac
    acacac
    acbaac
    accaac

