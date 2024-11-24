
<script>
  document.title = 'Final Project - FOCS FA24'
</script>

# Final Project

## Due Date for deliverables: Friday, Dec 20, 2024

Think of the final project as a more open-ended last homework with an associated short presentation.

The task: 

- Create teams of 2.
- Propose a topic or pick one from the list below.
- Once you've selected a topic (or want to propose your own), send me an email to let me know your team composition and your topic, and we can discuss scope and references if you need any.
- Most of these projects have an implementation component — you should feel free to use whatever language you want for that.

Deliverables:

- A 10-15 minutes presentation during final events period
- Code for your implementation (when relevant) and instructions for how to run the code
- A write up describing the work and walking through the code

***

## Possible Topics

### 2-Tapes Turing Machines Simulation

I defined two 2-tapes Turing machines in class, and mentioned the fact that you can transform any
2-tapes Turing machine into a single tape Turing machine that accepts and rejects the same strings,
by simulating the two tapes into a single tape. Write that transformation and use it to simulate
2-tapes Turing machines.

### Universal Turing Machines

A [Universal Turing machine](https://en.wikipedia.org/wiki/Universal_Turing_machine) is a Turing
machine that takes (the encoding of) a Turing machine as input as well as a string, and simulates
the input Turing machine running on that input string. Implement a Universal Turing machine, and use
it to simulate other Turing machines.


### (NEW) Modular Turing Machines

Constructing Turing machines is a bit of a pain. It needs to be built all at once, and if it
requires functionality that you've already implemented for another Turing machine, you cannot reuse
the "code" in any meaninful way. When I talked about building a Turing machine corresponding to a
Register Machine program, I introduced the notion of "gadgets" that you can combine together to
create a Turing machine, but didn't formalize the notion. This project would be to create a notion
of "modular" Turing machines that you can build by putting together pre-created (possibly
parameterized) components


### Post Correspondence Problem

We saw the Post Correspondence Problem in class as an example of a non-computable problem that is
not Turing-machine related. We can show it is non-computable by showing that if we could solve
the PCP, then we could solve the halting problem for Turing machines. The proof of this works by
showing that given a Turing machine M and an input w, we can construct a PCP instance (a set of
dominoes) with the property that the set of dominoes has a "solution" exactly when the Turing
machine M accepts input w. Implement the transformation from Turing machines to dominoes, and for a
Turing machine M and input w that Turing machine M accepts, construct the "solution" for the PCP
instance that shows that M accepts w. (I gave you a PDF with the construction on Canvas.)


### Context-Free Grammars Parsing

Context-free grammars *generate* strings. To determine if a string is in the language of a context-free grammar, we did it the hard way: we searchde through the space of all string generations to see if we can generate the string – if we can, we say yes, and if we can't or reach a search threshold, we say no. That's not great because the threshold can be high, and the search can be expensive. An alternative is *parsing*: taking the string, and trying to work backwards to see if we "simplify" the string back down to the starting symbol. There are many algorithms for parsing context-free grammars. Implement one from scratch.


### L-systems

Read about [L-systems](https://en.wikipedia.org/wiki/L-system), which are a form of rewrite system
not dissimilar to context-free grammars, but used for different goals. Implement a renderer for
L-systems and use it to visualize interesting examples.


### The Game of Life

The [Game of Life](https://en.wikipedia.org/wiki/The_Game_of_Life) is a cellular automaton that
exhibits an impressive array of interesting and counter-intuitive (maybe?) behaviors. You can
simulate Turing machines with it. That's too hard though. Easier (but still tricky) is to implement
logic gates and build logic "circuits". Do that. Yes, yes, I will send you references.


### Extended regular expressions

Regular expressions can express exactly the regular languages. People have also developed extended versions of regular expressions that can express more languages. Learn about [backreferences](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Regular_expressions/Backreference). Implement an extended regular expression matcher that supports backreferences.


### Turing machine visual simulator

Implement a visual simulator for a Turing machine as a web app.
