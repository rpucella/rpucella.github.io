<script>
  document.title = 'Homework 2'
</script>

# Homework 2

## Due Date: Tuesday Oct 8, 2024 (23h59)

- This homework is to be done individually. You may discuss problems
with fellow students, but all submitted work must be entirely your
own, and should not be from any other course, present, past, or
future. If you use a solution from another source you must cite it
&mdash; this includes when that source is someone else helping
you.

- Please do not post your solutions on a public website or a public repository (including GitHub).

- All programming is to be done in Python 3.

- Code your answers by modifying the file [`homework2.py`](homework2.py) provided. Add your **name**, your **email address**, and any **remarks** that you
wish to make to the instructor to the block comment at the head of the file.

- Please do not change the "signature" (number and interpretation of arguments) of the
functions. Doing so will make it impossible to load your code into the testing infrastructure, and
make me unhappy.

- Feel free to define helper functions if you need them.


**Electronic Submission Instructions**

- Make your your file `homework2.py` loads without error in a fresh Python 3 shell.

- Submission via [Canvas](https://canvas.olin.edu).

* * *

## Nondeterministic Finite Automata

Consider the structure for finite automata that we saw last time, as a Python dictionary:

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

In the last homework, we focused on deterministic finite automata, where for every state _p_ and every
symbol _a_ there is exactly one transition out of _p_ labelled _a_ in the automaton.

Here, we drop this restriction, and consider nondeterministic finite automata (NFA), where there can be no transition labeled for some symbols out of a given state, or more than one. Clearly, every DFA can be considered an NFA. (Why?)

Here are some NFAs that are not deterministic. The first is an NFA that accepts all strings over
`{a, b, c}` that end with `a`:

    NFA_LAST = {
        "states": [0, 1],
        "alphabet": ["a", "b", "c"],
        "delta": [
            (0, "a", 0),
            (0, "b", 0),
            (0, "c", 0),
            (0, "a", 1)
        ],
        "start": 0,
        "final": [1]
    }

Here is a generalization, _function_ that takes a number `n > 0` and returns an NFA that accepts all
strings over `{a, b, c}` where then `n`th position from the end has an `a`:

    def NFA_FIXED(n):
        if n > 0:
            return {
                "states": [-1] + list(range(n)),
                "alphabet": ["a", "b", "c"],
                "delta": [
                    (-1, "a", -1),
                    (-1, "b", -1),
                    (-1, "c", -1),
                    (-1, "a", 0)
                ] + [(i - 1, "a", i) for i in range(1, n)]
                + [(i - 1, "b", i) for i in range(1, n)]
                + [(i - 1, "c", i) for i in range(1, n)],
                "start": -1,
                "final": [n - 1]
            }
        raise Exception(f"Number {n} not supported")

So `NFA_FIXED(1)` is just `NFA_LAST`, but `NFA_FIXED(7)` is the set of all strings with an `a` in the 7th position from the end of the string. 


* * *


## Question 1: Simulating Nondeterministic Finite Automata

We are going to code up a simulator for nondeterministic finite automata. Nondeterministic finite automata
are harder to simulate than deterministic ones because we have to find ONE possible path in the automaton that brings us from the start state to a final state. We can either do that by searching, or by basically attempting to follow all possible paths at once. It's this second approach I want you to implement.

Code a function **`accept_nfa(m, input)`** where `accept_nfa(m, s)` returns `True` if if finite
automaton `m` accepts the string `s`, and `False` otherwise. That is, it returns true exactly if
there is *some* way to follow the transitions of `m` labeled by the symbols in `s` from the start
state to a final state.

**Hint 1**: Use the same process that we used as to accept a string with a DFA. That is, loop over all
the symbols in the input string in order, and keep track of your current position in the automaton
after each symbol. The initial position is just the start state of the automaton. The one difference
here is that instead of the current position being represented as a single state (like for a DFA),
the current position is represented by a *set* of states, like we saw in class. The current position
(which is a set of states) is updated to the next position after reading symbol `a` from the input
string by following all transitions labeled `a` from a state in the current position — the set of
all states that can be reached following an `a`-transition from a state in the current position
forms the next position.

**Hint 2**: You will want to use [Python's `set` or `frozenset` structures](https://docs.python.org/3/library/stdtypes.html#set-types-set-frozenset) to represent sets of states. It will make your life *much* simpler. (Two sets compare with `==` the way you want sets to compare, with no regard to repetition or order of elements.)

**Hint 3**: I strongly suggest you write a helper function `next_states(delta, current, sym)` that takes the `delta`
transition relation of an NFA, a set of states of that NFA, and a symbol `sym`, and returns the set of
all states that can be reached by following an `a`-transition out of a state in `current`. 

An input string should be accepted if the set of states you get after following all the symbols in
the input string contains a final state of the NFA. 

You can use function `language(m, length, accept)` I introduced last time (and included in `homework2.py` to print all strings accepted by NFA `m` of length at most `length` by passing `accept_nfa` as the acceptance function.)


Sample output:

    >>> accept_nfa(NFA_LAST, "")
    False
    >>> accept_nfa(NFA_LAST, "a")
    True
    >>> accept_nfa(NFA_LAST, "ba")
    True
    >>> accept_nfa(NFA_LAST, "bba")
    True
    >>> accept_nfa(NFA_LAST, "bbba")
    True
    >>> accept_nfa(NFA_LAST, "bbbab")
    False
    >>> accept_nfa(NFA_LAST, "bbbabb")
    False
    >>> accept_nfa(NFA_LAST, "aaaab")
    False
    
    >>> accept_nfa(NFA_FIXED(3), "bbbb")
    False
    >>> accept_nfa(NFA_FIXED(3), "aa")
    False
    >>> accept_nfa(NFA_FIXED(3), "aaa")
    True
    >>> accept_nfa(NFA_FIXED(3), "abb")
    True
    >>> accept_nfa(NFA_FIXED(3), "babb")
    True
    
    >>> language(NFA_LAST, 4, accept_nfa)
    a
    aa
    ba
    ca
    aaa
    aba
    aca
    baa
    bba
    bca
    caa
    cba
    cca
    aaaa
    aaba
    aaca
    abaa
    abba
    abca
    acaa
    acba
    acca
    baaa
    baba
    baca
    bbaa
    bbba
    bbca
    bcaa
    bcba
    bcca
    caaa
    caba
    caca
    cbaa
    cbba
    cbca
    ccaa
    ccba
    ccca
    
    >>> language(NFA_FIXED(2), 4, accept_nfa)
    aa
    ab
    ac
    aaa
    aab
    aac
    baa
    bab
    bac
    caa
    cab
    cac
    aaaa
    aaab
    aaac
    abaa
    abab
    abac
    acaa
    acab
    acac
    baaa
    baab
    baac
    bbaa
    bbab
    bbac
    bcaa
    bcab
    bcac
    caaa
    caab
    caac
    cbaa
    cbab
    cbac
    ccaa
    ccab
    ccac
    
    >>> language(NFA_FIXED(3), 4, accept_nfa)
    aaa
    aab
    aac
    aba
    abb
    abc
    aca
    acb
    acc
    aaaa
    aaab
    aaac
    aaba
    aabb
    aabc
    aaca
    aacb
    aacc
    baaa
    baab
    baac
    baba
    babb
    babc
    baca
    bacb
    bacc
    caaa
    caab
    caac
    caba
    cabb
    cabc
    caca
    cacb
    cacc


* * *

## Question 2: Converting NFAs to DFAs

In Question 1, we simulated nondeterministic finite state machines directly. In this question, we
convert nondeterministic finite automat to deterministic finite automata using the *subset construction* we saw in class and which is described in the notes.

To that end, code a function `to_dfa(m)` taking an NFA `m` and returning a new DFA that accepts exactly the same
language as `m`. The resulting automaton should be deterministic.

Recall that the subset construction creates a DFA from an NFA by taking as states of the new DFA the
*subsets* of states of the NFA. There is an `a`-transition from set of NFA-states P to set of NFA-states
Q in the new DFA if Q is the set of all states reachable via `a`-transitions from a state in
P. (Observe that this is **exactly** what function `next_states` I suggested you write in Question 1 gives
you! Accident? I think not.) 

**Hint 1**: I *strongly* suggest you use `set` or `frozenset` to represent states of the new DFA, so that your DFAs
can be simulated with the code you wrote in Homework 1.

**Hint 2**: You will want to write a function `subsets(qs)` that takes a set of states `qs` and returns
the list of all subsets of states from `qs`. That's a fun function to write. I have some sample outputs below.

The starting state of the resulting DFA is the set containing only the starting state of the NFA,
and a set of states is final if it contains at least one final state of the NFA.

You can test your `to_nfa` function by converting some NFAs and running the result through `language`
using `accept_dfa` from Homework 1.

The DFAs you produce **do not need to look exactly like the DFAs that I produce in my sample outputs below**. As long as your DFAs accept the same language as the original NFA, that's good enough for me.

Sample outputs:

    >>> subsets([])
    [frozenset()]
    
    >>> subsets([1])
    [frozenset(), frozenset({1})]
    
    >>> subsets([1, 2])
    [frozenset(), frozenset({2}), frozenset({1}), frozenset({1, 2})]
    
    >>> subsets([1, 2, 3])
    [frozenset(), frozenset({3}), frozenset({2}), frozenset({2, 3}), frozenset({1}), frozenset({1, 3}), frozenset({1, 2}), frozenset({1, 2, 3})]
    
    >>> subsets([1, 2, 3, 4])
    [frozenset(), frozenset({4}), frozenset({3}), frozenset({3, 4}), frozenset({2}), frozenset({2, 4}), frozenset({2, 3}), frozenset({2, 3, 4}), frozenset({1}), frozenset({1, 4}), frozenset({1, 3}), frozenset({1, 3, 4}), frozenset({1, 2}), frozenset({1, 2, 4}), frozenset({1, 2, 3}), frozenset({1, 2, 3, 4})]
    
    >>> subsets([1, 2, 3, 4, 5])
    [frozenset(), frozenset({5}), frozenset({4}), frozenset({4, 5}), frozenset({3}), frozenset({3, 5}), frozenset({3, 4}), frozenset({3, 4, 5}), frozenset({2}), frozenset({2, 5}), frozenset({2, 4}), frozenset({2, 4, 5}), frozenset({2, 3}), frozenset({2, 3, 5}), frozenset({2, 3, 4}), frozenset({2, 3, 4, 5}), frozenset({1}), frozenset({1, 5}), frozenset({1, 4}), frozenset({1, 4, 5}), frozenset({1, 3}), frozenset({1, 3, 5}), frozenset({1, 3, 4}), frozenset({1, 3, 4, 5}), frozenset({1, 2}), frozenset({1, 2, 5}), frozenset({1, 2, 4}), frozenset({1, 2, 4, 5}), frozenset({1, 2, 3}), frozenset({1, 2, 3, 5}), frozenset({1, 2, 3, 4}), frozenset({1, 2, 3, 4, 5})]

    >>> to_dfa(NFA_LAST)
    {'states': [frozenset(), frozenset({1}), frozenset({0}), frozenset({0, 1})], 'alphabet': ['a', 'b', 'c'], 'delta': [(frozenset(), 'a', set()), (frozenset(), 'b', set()), (frozenset(), 'c', set()), (frozenset({1}), 'a', set()), (frozenset({1}), 'b', set()), (frozenset({1}), 'c', set()), (frozenset({0}), 'a', {0, 1}), (frozenset({0}), 'b', {0}), (frozenset({0}), 'c', {0}), (frozenset({0, 1}), 'a', {0, 1}), (frozenset({0, 1}), 'b', {0}), (frozenset({0, 1}), 'c', {0})], 'start': frozenset({0}), 'final': [frozenset({1}), frozenset({0, 1})]}

    >>> to_dfa(NFA_FIXED(1))
    {'states': [frozenset(), frozenset({0}), frozenset({-1}), frozenset({0, -1})], 'alphabet': ['a', 'b', 'c'], 'delta': [(frozenset(), 'a', set()), (frozenset(), 'b', set()), (frozenset(), 'c', set()), (frozenset({0}), 'a', set()), (frozenset({0}), 'b', set()), (frozenset({0}), 'c', set()), (frozenset({-1}), 'a', {0, -1}), (frozenset({-1}), 'b', {-1}), (frozenset({-1}), 'c', {-1}), (frozenset({0, -1}), 'a', {0, -1}), (frozenset({0, -1}), 'b', {-1}), (frozenset({0, -1}), 'c', {-1})], 'start': frozenset({-1}), 'final': [frozenset({0}), frozenset({0, -1})]}

    >>> to_dfa(NFA_FIXED(2))
    {'states': [frozenset(), frozenset({1}), frozenset({0}), frozenset({0, 1}), frozenset({-1}), frozenset({1, -1}), frozenset({0, -1}), frozenset({0, 1, -1})], 'alphabet': ['a', 'b', 'c'], 'delta': [(frozenset(), 'a', set()), (frozenset(), 'b', set()), (frozenset(), 'c', set()), (frozenset({1}), 'a', set()), (frozenset({1}), 'b', set()), (frozenset({1}), 'c', set()), (frozenset({0}), 'a', {1}), (frozenset({0}), 'b', {1}), (frozenset({0}), 'c', {1}), (frozenset({0, 1}), 'a', {1}), (frozenset({0, 1}), 'b', {1}), (frozenset({0, 1}), 'c', {1}), (frozenset({-1}), 'a', {0, -1}), (frozenset({-1}), 'b', {-1}), (frozenset({-1}), 'c', {-1}), (frozenset({1, -1}), 'a', {0, -1}), (frozenset({1, -1}), 'b', {-1}), (frozenset({1, -1}), 'c', {-1}), (frozenset({0, -1}), 'a', {0, 1, -1}), (frozenset({0, -1}), 'b', {1, -1}), (frozenset({0, -1}), 'c', {1, -1}), (frozenset({0, 1, -1}), 'a', {0, 1, -1}), (frozenset({0, 1, -1}), 'b', {1, -1}), (frozenset({0, 1, -1}), 'c', {1, -1})], 'start': frozenset({-1}), 'final': [frozenset({1}), frozenset({0, 1}), frozenset({1, -1}), frozenset({0, 1, -1})]}

    >>> to_dfa(NFA_FIXED(3))
    {'states': [frozenset(), frozenset({2}), frozenset({1}), frozenset({1, 2}), frozenset({0}), frozenset({0, 2}), frozenset({0, 1}), frozenset({0, 1, 2}), frozenset({-1}), frozenset({2, -1}), frozenset({1, -1}), frozenset({1, 2, -1}), frozenset({0, -1}), frozenset({0, 2, -1}), frozenset({0, 1, -1}), frozenset({0, 1, 2, -1})], 'alphabet': ['a', 'b', 'c'], 'delta': [(frozenset(), 'a', set()), (frozenset(), 'b', set()), (frozenset(), 'c', set()), (frozenset({2}), 'a', set()), (frozenset({2}), 'b', set()), (frozenset({2}), 'c', set()), (frozenset({1}), 'a', {2}), (frozenset({1}), 'b', {2}), (frozenset({1}), 'c', {2}), (frozenset({1, 2}), 'a', {2}), (frozenset({1, 2}), 'b', {2}), (frozenset({1, 2}), 'c', {2}), (frozenset({0}), 'a', {1}), (frozenset({0}), 'b', {1}), (frozenset({0}), 'c', {1}), (frozenset({0, 2}), 'a', {1}), (frozenset({0, 2}), 'b', {1}), (frozenset({0, 2}), 'c', {1}), (frozenset({0, 1}), 'a', {1, 2}), (frozenset({0, 1}), 'b', {1, 2}), (frozenset({0, 1}), 'c', {1, 2}), (frozenset({0, 1, 2}), 'a', {1, 2}), (frozenset({0, 1, 2}), 'b', {1, 2}), (frozenset({0, 1, 2}), 'c', {1, 2}), (frozenset({-1}), 'a', {0, -1}), (frozenset({-1}), 'b', {-1}), (frozenset({-1}), 'c', {-1}), (frozenset({2, -1}), 'a', {0, -1}), (frozenset({2, -1}), 'b', {-1}), (frozenset({2, -1}), 'c', {-1}), (frozenset({1, -1}), 'a', {0, 2, -1}), (frozenset({1, -1}), 'b', {2, -1}), (frozenset({1, -1}), 'c', {2, -1}), (frozenset({1, 2, -1}), 'a', {0, 2, -1}), (frozenset({1, 2, -1}), 'b', {2, -1}), (frozenset({1, 2, -1}), 'c', {2, -1}), (frozenset({0, -1}), 'a', {0, 1, -1}), (frozenset({0, -1}), 'b', {1, -1}), (frozenset({0, -1}), 'c', {1, -1}), (frozenset({0, 2, -1}), 'a', {0, 1, -1}), (frozenset({0, 2, -1}), 'b', {1, -1}), (frozenset({0, 2, -1}), 'c', {1, -1}), (frozenset({0, 1, -1}), 'a', {0, 1, 2, -1}), (frozenset({0, 1, -1}), 'b', {1, 2, -1}), (frozenset({0, 1, -1}), 'c', {1, 2, -1}), (frozenset({0, 1, 2, -1}), 'a', {0, 1, 2, -1}), (frozenset({0, 1, 2, -1}), 'b', {1, 2, -1}), (frozenset({0, 1, 2, -1}), 'c', {1, 2, -1})], 'start': frozenset({-1}), 'final': [frozenset({2}), frozenset({1, 2}), frozenset({0, 2}), frozenset({0, 1, 2}), frozenset({2, -1}), frozenset({1, 2, -1}), frozenset({0, 2, -1}), frozenset({0, 1, 2, -1})]}

    >>> language(to_dfa(NFA_LAST), 4, accept_dfa)
    a
    aa
    ba
    ca
    aaa
    aba
    aca
    baa
    bba
    bca
    caa
    cba
    cca
    aaaa
    aaba
    aaca
    abaa
    abba
    abca
    acaa
    acba
    acca
    baaa
    baba
    baca
    bbaa
    bbba
    bbca
    bcaa
    bcba
    bcca
    caaa
    caba
    caca
    cbaa
    cbba
    cbca
    ccaa
    ccba
    ccca

    >>> language(to_dfa(NFA_FIXED(1)), 4, accept_dfa)
    a
    aa
    ba
    ca
    aaa
    aba
    aca
    baa
    bba
    bca
    caa
    cba
    cca
    aaaa
    aaba
    aaca
    abaa
    abba
    abca
    acaa
    acba
    acca
    baaa
    baba
    baca
    bbaa
    bbba
    bbca
    bcaa
    bcba
    bcca
    caaa
    caba
    caca
    cbaa
    cbba
    cbca
    ccaa
    ccba
    ccca

    >>> language(to_dfa(NFA_FIXED(2)), 4, accept_dfa)
    aa
    ab
    ac
    aaa
    aab
    aac
    baa
    bab
    bac
    caa
    cab
    cac
    aaaa
    aaab
    aaac
    abaa
    abab
    abac
    acaa
    acab
    acac
    baaa
    baab
    baac
    bbaa
    bbab
    bbac
    bcaa
    bcab
    bcac
    caaa
    caab
    caac
    cbaa
    cbab
    cbac
    ccaa
    ccab
    ccac

    >>> language(to_dfa(NFA_FIXED(3)), 4, accept_dfa)
    aaa
    aab
    aac
    aba
    abb
    abc
    aca
    acb
    acc
    aaaa
    aaab
    aaac
    aaba
    aabb
    aabc
    aaca
    aacb
    aacc
    baaa
    baab
    baac
    baba
    babb
    babc
    baca
    bacb
    bacc
    caaa
    caab
    caac
    caba
    cabb
    cabc
    caca
    cacb
    cacc

    >>> language(to_dfa(NFA_FIXED(4)), 4, accept_dfa)
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


* * *

## Question 3: Regular Expressions

I introduced regular expressions in class as a notation for regular languages, completely equivalent
to finite automata. A regular expression (over a given alphabet) is an expression using symbols from
the alphabet, and operations `+` and `*` along with sequencing (represented by putting two regular
expressions next to each other). A regular expression has the associated concept of the set of
strings that match the regular expression. 

For example, regular expression `abc` is matched by a single string, `abc`. Regular expression `ab*c` is
matched by all strings that start with an `a` followed by zero or more `b`s and ending with a `c`. Regular
expression `ab+ac` is matched by two strings, `ab` and `ac`, as is regular expression `a(b+c)`. Note the
priority of operations: sequencing binds tighter than `*` which binds tighter than `+`. 

The point of this question is to transform a regular expression into an NFA that accepts the exact
same language matched by the regular expression. The full conversion is tricky because we need to
parse the regular expression (a string in Python) into some structure that is more amenable to the
conversion, and moreover the conversion we saw in class was in term of ϵ-NFAs, and not NFAs. We
haven't dealt with  ϵ-NFAs in this homework. So I'm going to give you pieces of the conversion, and
you're going to complete it.

Overall, code a function `to_nfa(s, alphabet)` that takes a string denoting a regular expression
over a given alphabet, and returns an NFA (over the same alphabet) that accepts exactly the language
of strings matched by the regular expression.

The function `to_nfa()` works in three steps:  (1) it turns the regular expression string into
a structured representation that is easier to transform; (2) it takes the regular expression
representation and transforms it into an ϵ-NFA using the constructions we saw in class (and
described in the notes); (3) it transforms the ϵ-NFA into an NFA by eliminating the null transitions. 

I have written functions to do steps (1) and (3) for you. All you need to provide is step
(2). You're welcome.

First, the representation of regular expressions. I'm going to use tuples to represent regular
expressions in a structured form. A regular expression is going to be represented by a tuple of the
form `(type, ...)` where `type` is one of `"symbol"`, `"sequence"`, `"plus"`, or `"star"`,
representing the different kind of regular expressions we have, and the `...` part are values that
depend on the type of regular expression we are representing:

- The representation of a symbol is `("symbol", sym)` where `sym` is the symbol being represented. For
   instance, regular expression `a` is represented by `("symbol", "a")`. 

- The representation of sequencing is `("sequence", r1, r2)` where `r1` and `r2` are themselves
representations of the regular expressions being sequenced. For instance, regular expression `ab` is
represented by `("sequence", ("symbol", "a"), ("symbol", "b"))`. 

- The representation of choice is `("plus", r1, r2)` where `r1` and `r2` are themselves representations of
the regular expressions being chosen between. For instance, regular expressions `a+b` is represented
by `("plus", ("symbol", "a"), ("symbol", "b"))`, and regular expression `ab+ac` is represented by
`("plus", ("sequence", ("symbol", "a"), ("symbol", "b")), ("sequence", ("symbol", "a"), ("symbol", "c")))`. 

- The representation of a star operation is `("star", r)` where `r` is the representation of the
regular expression being starred. For instance, regular expression `(ab)*` is represented by
`("star", ("sequence", ("symbol", "a"), ("symbol", "b")))`.

To create these representations from a string, I gave you a function `parse(s)` that takes a string
`s` denoting a regular expression and returns a representation of the regular expression in the
above representation. (Note that this function is restricted to alphabets over the symbols from `a`
to `z`, so you are restricted to those alphabets for this question.) This takes care of step (1).

Step (2) is what you have to provide. I suggest you write a `convert_regexp(r)` function that takes
a regular expression in the above representation and create an ϵ-NFA corresponding to the regular
expression. That function will very likely need to be recursive - for instance, when converting a
`("star", r)` you will need to first transform `r` into an ϵ-NFA (you will do that by invoking the
`convert_regexp` function on `r`, that's the recursion!) and then perform some surgery on that ϵ-NFA
like we saw in class before returning the result. You may want to read up on recursive
functions. Note that during the construction you will need to merge ϵ-NFAs into new ϵ-NFAs. The
challenge there will be to make sure that you rename states in the ϵ-NFAs in such a way that merging
them does not lead to states clashing. In my code, I added unique prefixes to all state names when
creating an ϵ-NFA, but there are several other ways of achieving the same effect. This is the one challenge in this question.

An ϵ-NFA is just a NFA except it allows *null transitions*. A null transition is represented by a
tuple of form `(p, None, q)` in the `delta` field of the automaton, where `p` and `q` are
states. Now, the NFA simulator in Question 1 or the subset construction in Question 2 does not know
how to handle those transitions, so you cannot just give an NFA with null transitions to the
functions you wrote in those questions and expect it to work. Instead of changing those functions, I
gave you a function to transform an ϵ-NFA (where null transitions are represented as I just told
you) and turn it into an NFA that accepts the same language without any null transitions, and that
you can feed to `accept_nfa` from Question 1 or `to_dfa` from Question 2.

You should be able to write your function `to_nfa(r)` by first calling `parse()` then calling your
`convert_regexp()` function and then calling `remove_null()` on the result. And you can test with what
you wrote in Question 1 or Question 2. 

Just like in Question 2, the NFAs you produce **do not need to look exactly like the NFAs that I
produce in my sample outputs below**. As long as your NFAs accept the same language as the original
regular expression, that's good enough for me.

Sample outputs:

    >>> parse("ab")
    ('sequence', ('symbol', 'a'), ('symbol', 'b'))
    
    >>> parse("(a+b)*a")
    ('sequence', ('star', ('plus', ('symbol', 'a'), ('symbol', 'b'))), ('symbol', 'a'))
    
    >>> parse("(a+b)*aaa")
    ('sequence', ('sequence', ('sequence', ('star', ('plus', ('symbol', 'a'), ('symbol', 'b'))), ('symbol', 'a')), ('symbol', 'a')), ('symbol', 'a'))
    
    >>> convert_regexp(parse("ab"), ["a", "b"])
    {'states': ['8-6-0', '8-6-1', '8-7-0', '8-7-1'], 'alphabet': ['a', 'b'], 'delta': [('8-6-1', None, '8-7-0'), ('8-6-0', 'a', '8-6-1'), ('8-7-0', 'b', '8-7-1')], 'start': '8-6-0', 'final': ['8-7-1']}
    
    >>> convert_regexp(parse("(a+b)*aaa"), ["a", "b"])
    {'states': ['18-16-14-12-0', '18-16-14-12-11-0', '18-16-14-12-11-1', '18-16-14-12-11-9-0', '18-16-14-12-11-9-1', '18-16-14-12-11-10-0', '18-16-14-12-11-10-1', '18-16-14-13-0', '18-16-14-13-1', '18-16-15-0', '18-16-15-1', '18-17-0', '18-17-1'], 'alphabet': ['a', 'b'], 'delta': [('18-16-15-1', None, '18-17-0'), ('18-16-14-13-1', None, '18-16-15-0'), ('18-16-14-12-0', None, '18-16-14-13-0'), ('18-16-14-12-0', None, '18-16-14-12-11-0'), ('18-16-14-12-11-1', None, '18-16-14-12-0'), ('18-16-14-12-11-0', None, '18-16-14-12-11-9-0'), ('18-16-14-12-11-0', None, '18-16-14-12-11-10-0'), ('18-16-14-12-11-9-1', None, '18-16-14-12-11-1'), ('18-16-14-12-11-10-1', None, '18-16-14-12-11-1'), ('18-16-14-12-11-9-0', 'a', '18-16-14-12-11-9-1'), ('18-16-14-12-11-10-0', 'b', '18-16-14-12-11-10-1'), ('18-16-14-13-0', 'a', '18-16-14-13-1'), ('18-16-15-0', 'a', '18-16-15-1'), ('18-17-0', 'a', '18-17-1')], 'start': '18-16-14-12-0', 'final': ['18-17-1']}
    
    >>> to_nfa("(a+b)*aaa", ["a", "b"])
    {'states': ['28-26-24-22-0', '28-26-24-22-21-0', '28-26-24-22-21-1', '28-26-24-22-21-19-0', '28-26-24-22-21-19-1', '28-26-24-22-21-20-0', '28-26-24-22-21-20-1', '28-26-24-23-0', '28-26-24-23-1', '28-26-25-0', '28-26-25-1', '28-27-0', '28-27-1'], 'alphabet': ['a', 'b'], 'delta': [('28-26-24-22-21-1', 'b', '28-26-24-22-21-20-1'), ('28-27-0', 'a', '28-27-1'), ('28-26-24-22-21-19-0', 'a', '28-26-24-22-21-19-1'), ('28-26-24-23-0', 'a', '28-26-24-23-1'), ('28-26-24-22-21-19-1', 'a', '28-26-24-22-21-19-1'), ('28-26-24-22-21-19-1', 'a', '28-26-24-23-1'), ('28-26-24-22-21-0', 'a', '28-26-24-22-21-19-1'), ('28-26-24-22-21-19-1', 'b', '28-26-24-22-21-20-1'), ('28-26-24-22-0', 'a', '28-26-24-22-21-19-1'), ('28-26-24-22-21-0', 'b', '28-26-24-22-21-20-1'), ('28-26-24-22-21-1', 'a', '28-26-24-22-21-19-1'), ('28-26-24-22-0', 'a', '28-26-24-23-1'), ('28-26-25-0', 'a', '28-26-25-1'), ('28-26-24-22-21-20-1', 'a', '28-26-24-22-21-19-1'), ('28-26-24-23-1', 'a', '28-26-25-1'), ('28-26-24-22-21-20-0', 'b', '28-26-24-22-21-20-1'), ('28-26-25-1', 'a', '28-27-1'), ('28-26-24-22-21-20-1', 'a', '28-26-24-23-1'), ('28-26-24-22-21-1', 'a', '28-26-24-23-1'), ('28-26-24-22-21-20-1', 'b', '28-26-24-22-21-20-1'), ('28-26-24-22-0', 'b', '28-26-24-22-21-20-1')], 'start': '28-26-24-22-21-19-1', 'final': ['28-27-1']}
    
    >>> language(to_nfa("(a+b)*aaa", ["a", "b"]), 5, accept_nfa)
    aaa
    aaaa
    baaa
    aaaaa
    abaaa
    baaaa
    bbaaa
    
    >>> language(to_nfa("(a+b)*aaa", ["a", "b"]), 6, accept_nfa)
    aaa
    aaaa
    baaa
    aaaaa
    abaaa
    baaaa
    bbaaa
    aaaaaa
    aabaaa
    abaaaa
    abbaaa
    baaaaa
    babaaa
    bbaaaa
    bbbaaa

