<script>
  document.title = 'Homework 3'
</script>

# Homework 3

## Due Date: Sunday Oct 13, 2024 (23h59)

- This homework is to be done individually. You may discuss problems
with fellow students, but all submitted work must be entirely your
own, and should not be from any other course, present, past, or
future. If you use a solution from another source you must cite it
&mdash; this includes when that source is someone else helping
you.

- Please do not post your solutions on a public website or a public repository (including GitHub).

- All programming is to be done in Python 3.

- Code your answers by modifying the file [`homework3.py`](homework3.py) provided. Add your **name**, your **email address**, and any **remarks** that you
wish to make to the instructor to the block comment at the head of the file.

- Please do not change the "signature" (number and interpretation of arguments) of the
functions. Doing so will make it impossible to load your code into the testing infrastructure, and
make me unhappy.

- Feel free to define helper functions if you need them.


**Electronic Submission Instructions**

- Make your your file `homework3.py` loads without error in a fresh Python 3 shell.

- Submit `homework3.py` on [Canvas](https://canvas.olin.edu).

* * *

## Pushdown Automata

We are going to describe pushdown automata in Python using a similar structure that we used for
finite automata, as a dictionary:

    {
      "states": ...,
      "alphabet": ...,
      "stack_alphabet":...,
      "delta": ...,
      "start": ...,
      "final": ...
     }

As usual, the dictionary fields hold the various components of the automaton: a set of states, a set
of input symbols (the alphabet), a set of stack symbols (the stack alphabet), a transition relation
&delta;, a start state, and a set of final states. Lists are used to represent sets. 

The transition relation is represented using list of tuples (_p_, _a_, _s_, _q_, _S_), stating that
in state _p_, reading symbol _a_ from the input string while _s_ is on top of the stack makes the
machine transition to state _q_, pop the stack, and push the symbols in _S_ onto the stack, _in
reverse order_ (last element of the list is pushed first). States can be represented by any value
that support equality checking. We also allow _null transitions_, where the _a_ part is `None`, representing a transition that in state _p_ when _s_ is on top of the stack can make the machine transition to state _q_, popping the stack and pushing the symbols in _S_ onto the stack in reverse order.

When a pushdown automaton is given a string to accept or reject, it uses a stack. The stack is
initialized with a special stack alphabet symbol -- we will use `.` in this homework. The automaton
is initially in the start state. It transitions to another state via an enabled transition. A
transition is enabled if it has the current state as the source state, its input symbol is the
current input symbol, and its stack symbol is the symbol sitting on top of the stack. A null
transition is enabled if it has the current state as the source state and its stack symbol is the
symbol sitting on top of the stack. Following an enabled transitions moves the current state to the
target state of the transition, pops the symbol from the top of the stack, and pushes the new stack
symbols on top of the stack, in reverse order than they appear in the list in the transition

A pushdown automata accepts string _w_ if there is a sequence of enabled transitions starting in the start state and following the symbols from the input string and ending up in any of the final states with only `.` in the stack. 

For example, here is a pushdown automaton that accepts all strings of the form
`a`<sup>_n_</sup>`b`<sup>_n_</sup> for all _n &ge; 0_:

    PDA_ANBN = {
        "states": [0, 1],
        "alphabet": ["a", "b"],
        "stack_alphabet": [".", "X"],
        "start": 0,
        "final": [1],
        "delta": [
            (0, "a", ".", 0, ["X", "."]),
            (0, "a", "X", 0, ["X", "X"]),
            (0, None, ".", 1, ["."]),
            (0, None, "X", 1, ["X"]),
            (1, "b", "X", 1, []),
        ]
    }

and here is a pushdown automaton that accepts all palindromes over {`a`, `b`}, of both even and odd length:

    PDA_PAL = {
        "states": [0, 1],
        "alphabet": ["a", "b"],
        "stack_alphabet": [".", "X", "Y"],
        "start": 0,
        "final": [1],
        "delta": [
            (0, "a", ".", 0, ["X", "."]),
            (0, "a", "X", 0, ["X", "X"]),
            (0, "a", "Y", 0, ["X", "Y"]),
            (0, "b", ".", 0, ["Y", "."]),
            (0, "b", "X", 0, ["Y", "X"]),
            (0, "b", "Y", 0, ["Y", "Y"]),
            (0, None, ".", 1, ["."]),
            (0, None, "X", 1, ["X"]),
            (0, None, "Y", 1, ["Y"]),
            (0, "a", ".", 1, ["."]),
            (0, "a", "X", 1, ["X"]),
            (0, "a", "Y", 1, ["Y"]),
            (0, "b", ".", 1, ["."]),
            (0, "b", "X", 1, ["X"]),
            (0, "b", "Y", 1, ["Y"]),
            (1, "a", "X", 1, []),
            (1, "b", "Y", 1, [])
        ]
    }

Pushdown automata are nondeterministic, and have null transitions.

***

## Question 1

In this question, we are going to build the tools to accepts strings via pushdown automata. 

Rather than asking you to write an `accept` function directly (how we did in the last two homeworks for finite automata), I'm going to do it a little bit differently, because I want to be able to reuse the code you write here in Question 2.

We are going to break down how to run a pushdown automaton into multiple functions. When running a
pushdown automaton, we need to keep track of multiple things: the current state,
what remains of the input string, and the stack. We are going to collect all of the information into
a triple called a *configuration*, of the form (_inp_, _q_, _S_) where `inp` is a string representing what
remains of the input string to be consumed, _q_ is a state of the automaton, and _S_ is a list of stack
symbols on the stack (where the stacks grows from the end of the list, so that the first element of
the list is on top of the stack).

At any point during execution, the pushdown automaton is in some
configuration, representing what input remains to be consumed, what the current state is, and the
current content of the stack. Running a pushdown automaton basically means stepping from
configuration to configuration, following enabled transitions.

### (A) 

Code a function `step_pda(m, config)` taking a pushdown automaton `m` and a configuration of the form
`(inp, state, stack)`, and returning a list of all configurations that can be reached by following an
enabled transition from the given configuration: that is, it should return a list of all
configurations that you can obtain by following a transition from state `state` with input symbol
`inp[0]` and stack symbol `stack[0]`, or following a null transition from state `state` with stack symbol
`stack[0]`.

Sample output (order of results not relevant):

    >>> step_pda(PDA_ANBN, ("aabb", 0, ["."]))
    [('abb', 0, ['X', '.']), ('aabb', 1, ['.'])]

    >>> step_pda(PDA_ANBN, ("aabb", 0, ["X", "."]))
    [('abb', 0, ['X', 'X', '.']), ('aabb', 1, ['X', '.'])]

    >>> step_pda(PDA_ANBN, ("bb", 1, ["X", "X", "."]))
    [('b', 1, ['X', '.'])]

    >>> step_pda(PDA_ANBN, ("", 1, ["."]))
    []

    >>> step_pda(PDA_PAL, ("aba", 0, ["."]))
    [('ba', 0, ['X', '.']), ('aba', 1, ['.']), ('ba', 1, ['.'])]

    >>> step_pda(PDA_PAL, ("ba", 0, ["X", "."]))
    [('a', 0, ['Y', 'X', '.']), ('ba', 1, ['X', '.']), ('a', 1, ['X', '.'])]

    >>> step_pda(PDA_PAL, ("a", 1, ["X", "."]))
    [('', 1, ['.'])]

    >>> step_pda(PDA_PAL, ("", 1, ["."]))
    []


### (B)

Code a function `start_pda(m, input)` taking a pushdown automaton `m` and an input string `input`
and returning the *starting configuration* for executing the machine, that is, the configuration
with the start state of the automaton, the input string provided, and an initial stack.

Sample output:

    >>> start_pda(PDA_ANBN, "aabb")
    ('aabb', 0, ['.'])

    >>> start_pda(PDA_ANBN, "aabbbb")
    ('aabbbb', 0, ['.'])

    >>> start_pda(PDA_PAL, "abba")
    ('abba', 0, ['.'])

    >>> start_pda(PDA_PAL, "abbab")
    ('abbab', 0, ['.'])


### (C)

Code a function `is_done_pda(m, config)` that takes a pushdown automaton `m` and a configuration
`config` and returns `True` exactly when the configuration is a configuration that has consumed all of
the input string.

Sample output:

    >>> is_done_pda(PDA_ANBN, ("ab", 0, ["X", "."]))
    False

    >>> is_done_pda(PDA_ANBN, ("ab", 1, ["X", "."]))
    False

    >>> is_done_pda(PDA_ANBN, ("", 0, ["X", "."]))
    True

    >>> is_done_pda(PDA_ANBN, ("", 1, ["X", "."]))
    True

    >>> is_done_pda(PDA_PAL, ("ab", 0, ["X", "."]))
    False

    >>> is_done_pda(PDA_PAL, ("ab", 1, ["X", "."]))
    False

    >>> is_done_pda(PDA_PAL, ("", 0, ["X", "."]))
    True

    >>> is_done_pda(PDA_PAL, ("", 1, ["X", "."]))
    True


### (D)

Code a function `is_accept_pda(m, config)` that takes a pushdown automaton `m` and a configuration
`config` and returns `True` exactly when the configuration is an *accepting configuration*, that is
one that has consumed all of the input string and whose stack is empty but for the initial stack
symbol `.` and whose state is a final state of the automaton.  stack is empty but for the initial
stack symbol `"."`.

Sample output:

    >>> is_accept_pda(PDA_ANBN, ("ab", 0, ["X", "."]))
    False

    >>> is_accept_pda(PDA_ANBN, ("ab", 1, ["X", "."]))
    False

    >>> is_accept_pda(PDA_ANBN, ("", 1, ["X", "."]))
    False

    >>> is_accept_pda(PDA_ANBN, ("", 1, ["."]))
    True

    >>> is_accept_pda(PDA_ANBN, ("", 0, ["."]))
    False

    >>> is_accept_pda(PDA_PAL, ("aa", 0, ["X", "."]))
    False

    >>> is_accept_pda(PDA_PAL, ("aa", 0, ["."]))
    False

    >>> is_accept_pda(PDA_PAL, ("aa", 1, ["."]))
    False

    >>> is_accept_pda(PDA_PAL, ("", 1, ["X", "."]))
    False

    >>> is_accept_pda(PDA_PAL, ("", 1, ["."]))
    True

    >>> is_accept_pda(PDA_PAL, ("", 0, ["."]))
    False


### (E)

We are going to put the pieces we built above together to create a simular for *deterministic
pushdown automata*.  A deterministic pushdown automaton is a pushdown automaton that has the
property that for every state, for every input symbol, and for every stack symbol, there is exactly
**one** transition with the state as source, that input symbol, and that stack symbol in the `delta`
relation. Deterministic pushdown automata moreover have no null transitions. 

The idea is that when the automaton is in a given state, the next state is completely determined by
the current state, the symbol on the string, and the symbol on top of the stack. Deterministic
pushdown automata are more limited than pushdown automata. For instance, there is no deterministic
pushdown automaton that can accept exactly the even-length palindromes over {a, b}. But they are a
good starting point for us.

Here a deterministic pushdown automaton that accepts all strings of the form
`a`<sup>_n_</sup>`b`<sup>_n_</sup> for all _n &ge; 0_:


    DPDA_ANBN = {
        "states": [0, 1, 2, 99],
        "alphabet": ["a", "b"],
        "stack_alphabet": [".", "X"],
        "start": 0,
        "final": [0, 2],
        "delta": [
            (0, "a", ".", 1, ["X", "."]),
            (1, "a", "X", 1, ["X", "X"]),
            (1, "b", "X", 2, []),
            (2, "b", "X", 2, []),
            # Sink transitions (for deterministism)
            (0, "a", "X", 99, []),
            (0, "b", ".", 99, ["."]),
            (0, "b", "X", 99, []),
            (1, "a", ".", 99, ["."]),
            (1, "b", ".", 99, ["."]),
            (2, "a", ".", 99, ["."]),
            (2, "a", "X", 99, []),
            (2, "b", ".", 99, ["."]),
            (99, "a", ".", 99, ["."]),
            (99, "a", "X", 99, []),
            (99, "b", ".", 99, ["."]),
            (99, "b", "X", 99, [])
        ]
    }

Notice that for every choice of state, input symbol, and stack symbol, there is exactly one transition in `delta` with those choices in the first three elements of the transition. Notice also that we had to add a "sink state" (here, 99).

Code a function `accept_dpda(m, input)` that takes a *deterministic* pushdown automaton `m` and an input string `input` and returns True exactly when `m` accepts input string `input`. Intuitively, this should work by starting with the starting configuration, loops by applying `step_pda` to the current configuration to obtain the next configuration until we have consumed the whole input string, and returns True when the final configuration is an accepting configuration. Make sure to check at every step that `step_pda()` returns exactly one next configuration — if not, raise an exception pointing out that the machine is nondeterministic. As in the last few homeworks, you can pass `accept_dpda` to `language` to see all strings accepted by a machine, up to a certain length.

Sample outputs:

    >>> accept_dpda(DPDA_ANBN, "")
    True

    >>> accept_dpda(DPDA_ANBN, "ab")
    True
    >>> accept_dpda(DPDA_ANBN, "aabb")
    True

    >>> accept_dpda(DPDA_ANBN, "aaabbb")
    True

    >>> accept_dpda(DPDA_ANBN, "aa")
    False

    >>> accept_dpda(DPDA_ANBN, "bb")
    False

    >>> accept_dpda(DPDA_ANBN, "aabbb")
    False

    >>> accept_dpda(DPDA_ANBN, "abab")
    False

    >>> language(DPDA_ANBN, 10, accept_dpda)
    <empty string>
    ab
    aabb
    aaabbb
    aaaabbbb
    aaaaabbbbb

    >>> accept_dpda(PDA_PAL, "abba")
    Traceback (most recent call last):
      File "<stdin>", line 1, in <module>
      File "/Users/riccardo/git/focs/homeworks/nondet.py", line 155, in acc
        raise Exception("Not deterministic!")
    Exception: Not deterministic!



***

## Question 2

Question 1 asked you to write a bunch of functions to help simulate pushdown automata, and you used
them to create an acceptance function for *deterministic* pushdown automata. In this question, I'm
giving you an acceptance function `accept_pda(m, input)` for general nondeterministic pushdown
automata, and ask you to create some interesting (nondeterministic) pushdown automata.

(Function `accept_pda` will only work once you have completed Question 1 parts (A)–(D), and
basically implements a breadth-first search over the graph of configurations. It's not deep, but it works.)

For submission purposes, I ask you to define those pushdown automata as constants with a fixed name
given below in each question. There are already placeholders in `homework3.py` for those
answers. Just replace each placeholder with your definition.


## (A)

Construct a nondeterministic pushdown automaton `PDA_AMBN_MGN` that accepts
exactly the strings over alphabet {`a`, `b`} of the form `a`<sup>_m_</sup>`b`<sup>_n_</sup> in which
_m_ &ge; _n_ &ge; 0 (at least as many `a`s as `b`s).

Sample output:

    >>> language(PDA_AMBN_MGN, 8, accept_pda)
    <empty string>
    a
    aa
    ab
    aaa
    aab
    aaaa
    aaab
    aabb
    aaaaa
    aaaab
    aaabb
    aaaaaa
    aaaaab
    aaaabb
    aaabbb
    aaaaaaa
    aaaaaab
    aaaaabb
    aaaabbb
    aaaaaaaa
    aaaaaaab
    aaaaaabb
    aaaaabbb
    aaaabbbb


## (B)

Construct a nondeterministic pushdown automaton `PDA_AMBN_NGM` that accepts exactly the strings over
alphabet {`a`, `b`} of the form `a`<sup>_m_</sup>`b`<sup>_n_</sup> in which _n_ &ge; _m_ &ge; 0 (at
least as many `b`s as `a`s).

Sample output:

    >>> language(PDA_AMBN_NGM, 8, accept_pda)
    <empty string>
    b
    ab
    bb
    abb
    bbb
    aabb
    abbb
    bbbb
    aabbb
    abbbb
    bbbbb
    aaabbb
    aabbbb
    abbbbb
    bbbbbb
    aaabbbb
    aabbbbb
    abbbbbb
    bbbbbbb
    aaaabbbb
    aaabbbbb
    aabbbbbb
    abbbbbbb
    bbbbbbbb


## (C)

Construct a nondeterministic pushdown automaton `PDA_AMBNCMN` that accepts exactly the strings over
alphabet {`a`, `b`, `c`} of the form `a`<sup>_m_</sup>`b`<sup>_n_</sup>`c`<sup>_m+n_</sup> in which _m_, _n_ &ge; 0 (the same number of `c`s as `a`s and `b`s combined).

Sample output:

    >>> language(PDA_AMBNCMN, 8, accept_pda)
    <empty string>
    ac
    bc
    aacc
    abcc
    bbcc
    aaaccc
    aabccc
    abbccc
    bbbccc
    aaaacccc
    aaabcccc
    aabbcccc
    abbbcccc
    bbbbcccc


## (D)

Construct a nondeterministic pushdown automaton `PDA_AMBMNCN` that accepts exactly the strings over
alphabet {`a`, `b`, `c`} of the form `a`<sup>_m_</sup>`b`<sup>_m+n_</sup>`c`<sup>_n_</sup> in which
_m_, _n_ &ge; 0 (the same number of `b`s as `a`s and `c`s combined).

Sample output:

    >>> language(PDA_AMBMNCN, 8, accept_pda)
    <empty string>
    ab
    bc
    aabb
    abbc
    bbcc
    aaabbb
    aabbbc
    abbbcc
    bbbccc
    aaaabbbb
    aaabbbbc
    aabbbbcc
    abbbbccc
    bbbbcccc

