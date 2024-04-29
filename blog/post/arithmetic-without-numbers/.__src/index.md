
---

title: Arithmetic Without Numbers
date: 2023-11-20
reading: The Book of Ebenezer Le Page (by G. E. Edwards)

---

The [lambda calculus](https://en.wikipedia.org/wiki/Lambda_calculus) is a simple rewrite system often used to study computation. Despite having very
few rules, it is Turing-complete, and it has a reasonably immediate interpretation as a programming
language model (specifically, for functional programming languages).

Lambda calculus expressions are of the form:

    E, F ::= x
             λx.E
             E F
             (E)
          
where `x` is one of a (countably infinite) set of identifiers. There is only one rewrite rule, that
lets you rewrite (*reduce*) a sub-expression `(λx.E)` into expression `E` in which every (free) occurrence of identifier
`x` is replaced by `F`. Formally,

    (λx.E) F ⇒ E[F/x]
    
I'm skipping over a lot of details here — the definition of free occurrence of identifiers, the fact
that substitution should be capture-avoiding and free occurrences of identifiers in `F` should
remain free even after substitution, etc. You can find all of the information in any [reference](https://en.wikipedia.org/wiki/Lambda_calculus).

Computing with the lambda calculus consists of taking an expression and reducing it using the
rewrite rule above until you end up with an expression that you can no longer reduce. (It is
possible to write expressions that never reach that point — the lambda calculus equivalent of
infinite loops.) For example, the expression

    (λx.λy.λz.x (y z)) (λw.w) (λ v.v v) u
    
reduces as follows. It is useful to know that application associates *to the left*, so that an
expression `A B C D` is interpreted as (`(A B) C) D`. I will parenthesize fully here, for clarity, but will
soon drop unnecessary parentheses and rely on left-associativity for application:

    (((λx.λy.λz.x (y z)) (λw.w)) (λ v.v v)) u
        ⇒  ((λy.λz.(λw.w) (y z)) (λ v.v v)) u
        ⇒  (λz.(λw.w) ((λ v.v v) z)) u
        ⇒  (λw.w) ((λ v.v v) u)
        ⇒  (λw.w) (u u)
        ⇒  u u

At that point, no more reduction is possible, so `u u` is the result of the "computation"
`(λx.λy.λz.x (y z)) (λw.w) (λ v.v v) u`.

The lambda calculus as defined, though, doesn't have any data types. There's identifiers and
functions. What are those identifiers referring to? Nothing, or other identifiers, or functions. And
what are functions acting on? Identifiers, and other functions. That's it. It's a mathematical
ouroboros.

You can *encode* other data types though. Church, who first defined the lambda calculus, presented
encodings for several data types, including natural numbers. What do I mean by encoding here? I
simply mean that we can *represent* values like natural numbers using functions, in such a way that
they *behave* like natural numbers: you can define operations like addition and multiplication acting
on natural numbers represented using that encoding, and they will return the expected result, again
represented in that encoding.

Here is Church's encoding of the first few natural numbers:

    0 = λf.λx.x
    
    1 = λf.λx.f x
    
    2 = λf.λx.f (f x)
    
    3 = λf.λx.f (f (f x))
    
    ...
    
You can spot the pattern. In general, natural number `n` is encoded as `λf.λx.f (f (f ... x))`, that is,
as a function that expects an argument `f` and an argument `x` and applies `f` to `x` `n` times. There is no
assumption made on what `f` or `x` stand for. A natural number `n` is just a machine to apply any function
to any argument `n` times. What is perhaps surprising is that this is a starting point to define
operations on natural numbers. 

Here is one definition the successor function which takes (the encoding of) natural *n* and returns
(the encoding of) natural number *n + 1*:

    succ = λn.λf.λx.(n f) (f x)
    
Here's the reduction showing that the successor of 2 is 3:

    succ 2 =  (λn.λf.λx.(n f) (f x)) (λf.λx.f (f x))
           ⇒  λf.λx.((λf.λx.f (f x)) f) (f x)
           ⇒  λf.λx.(λx.f (f x)) (f x)
           ⇒  λf.λx.(f (f (f x)))
           =  3

Similarly, addition and multiplication are comparatively easy to define:

     plus = λn.λm.λf.λx.(n f) (m f x)
     
    times = λn.λm.λf.λx.n (m f) x
    
Subtraction is more interesting. The encoding of Church is optimized for taking successors — it is
easy to apply an extra `f` — but not so much for going the other direction. It is an interesting
challenge to define a predecessor operation. Pause now if you want to solve this tricky puzzle.

The following definition is due to Kleene:

    pred = λn.λf.λx.n (λg.λh.h (g f)) (λu.x) (λu.u)
    
(One intuition is that this builds a sequence of pairs *(0, 1)*, *(1, 2)*, ..., *(n - 1, n)*
using the function `n`, and then takes the first component of the resulting pair. Kleene's definition
is an optimized form of this intuition.)

It is straightforward to verify that `pred` *n* `⇒` *n - 1* when *n* is non-zero, with
`pred 0 ⇒ 0`. Subtraction can then be implemented by repeatedly taking predecessors:

    minus = λn.λm.m pred n

Indeed, `minus n m ⇒ pred (pred (pred (pred ... (pred n))))` where `pred` is taken `m` times. And
because `pred 0 ⇒ 0`, `minus n m ⇒ 0` when `m` is greater or equal than `n`.

It is also possible to define predicates over the natural numbers, such as equality. To do so, we
need a representation of the Booleans true and false. The following definition is also due to Church:

     true = λx.(λy.x)
    
    false = λx.(λy.y)
    
Just like the encoding of a natural number `n` is a function that can apply some `f` to some `x` `n` times,
the encoding of a Boolean is a function that takes two arguments and returns one of them — the first
or the second, based on whether the Boolean is true or false. We can confirm easily:

     true A B  =  (λx.(λy.x)) A B
               ⇒  (λy.A) B
               ⇒  A

    false A B  =  (λx.(λy.y)) A B
               ⇒  (λy.y) B
               ⇒  B

(It's an easy exercise to define operations such as NOT, AND, and OR based on this encoding.)

Once we have Booleans, we can write predicates on natural numbers such as *is zero*, *greater than or equal*, and *equal*:

    iszero = λn.n (λx.false) true
    
        ge = λn.λm.not (iszero (minus (succ m) n))
        
        eq = λn.λm.and (ge m n) (ge n m)

All of the above is well covered in any introduction to the lambda calculus. One thing that is often
left out though is how to go about encoding integers, that is both positive and negative
numbers. The most naive encoding is to encoding an integer as a pair of a sign (positive or
negative, as a Boolean), and a natural number representing the absolute value of the integer. Thus
`(true, 2)` would be the representation of integer 2, and `(false, 3)` the representation of integer -3.

In order to get this encoding, we need a way to encode pairs. That encoding is also well known:

    pair = λx.λy.λs.s x y 
    
     fst = λp.p (λx.λy.x)
     
     snd = λp.p (λx.λy.y)
     
Intuitively, a pair is a function that expects a *selector* and calls that selector on the two
expressions used to create the pair. Extracting the first and second element of the pair (functions
`fst` and `snd`) is then simply a matter of passing the right selector to the pair.

This gives us an implementation of integers. First, to create an integer out of a natural number:

    int = λn.pair true n

Then, operations. For example, `neg_int` negates an integer:

    neg_int = λi.(ifzero (snd i)) (pair true 0) (pair (not (fst i)) (snd i))

Operations `plus_int`, `minus_int`, and `times_int` are left as exercise to the reader. They are
mostly case analysis on the sign of the arguments, and work with the natural numbers accordingly. 


## Integers via Grothendieck Completion

The encoding of integers as a pair of a sign and a natural number is somewhat inelegant. A
symptom of that inelegance is the case analysis that has to go into the addition operation. 

A more elegant approach is to replicate the 
classical construction of the ring of integers from the natural numbers as 
[equivalence classes of pairs of natural numbers](https://en.wikipedia.org/wiki/Integer#Equivalence_classes_of_ordered_pairs). 
It is well covered in any elementary abstract algebra textbook. (The original construction goes back
at least to [Kronecker](Kronecker.), and it was generalized
by [Grothendieck](https://en.wikipedia.org/wiki/Alexander_Grothendieck) — one of the most fascinating mathematicians of the twentieth century —
who used the general construction in his proof of the [Grothendieck-Riemann-Roch theorem](https://en.wikipedia.org/wiki/Grothendieck%E2%80%93Riemann%E2%80%93Roch_theorem).)

The idea is simple: given a semigroup (*S*, +, *, *0*, *1*), we can form a commutative ring *R* by taking the
elements of *R* to be pairs (*s*, *t*) of *S x S* subject to the equivalence that (*s*, *t*) and
(*s'*, *t'*) are
equivalent when *s + t'* = *t + s'*. In this way, the semigroup *S* embeds into *R* by mapping an element
*s* of *S* to (*s*, *0*). 

For integers, this means that a pair (*n*, *m*) is used to encode integer *n* - *m*. When *n* > *m*, (*n*, *m*)
represents a
positive integer, and when *n* < *m*, (*n*, *m*) represents a negative integer. The equivalence relation
identifies the infinite number of pairs of natural number whose difference is *n - m*.  For example, the integer -2 can be
represented in infinitely many ways, such as (0, 2), (1, 3), (2, 4), (3, 5), (4, 6), (5, 7),
... Indeed, (0, 2) and (5, 7) are equivalent because 0 + 7 = 2 + 5.

Addition and multiplication then get a delightfully uniform definition:

    (n, m) + (n', m') = (n + n', m + m')
    
    (n, m) * (n', m') = (nn' + mm', n'm + nm')
    
The algebra-minded among you can check the commutative ring properties and see that they hold, and
that addition and multiplication are well defined with respect to the equivalence relation.

This gives us the following encoding of integers in the lambda calculus:

         int = λn.pair n 0
         
     neg_int = λi.pair (snd i) (fst i)
     
    plus_int = λi.λj.pair (plus (fst i) (fst j)) (plus (snd i) (snd j))
    
    minus_int = λi.λj.pair (plus (fst i) (snd j)) (plus (snd i) (fst j))
    
    times_int = λi.λj.pair (plus (times (fst i) (fst j)) (times (snd i) (snd j))) 
                           (plus (times (fst j) (snd i)) (times (fst i) (snd j)))

       eq_int = λi.λj.eq (plus (fst i) (snd j)) (plus (snd i) (fst j))
       
You can verify, for instance, that 2 - 3 = -1, that is:

    eq_int (plus_int (int 2) (neg_int (int 3))) (neg_int (int 1)) ⇒ true
    
Functions `is_zero_int` (an extension of `is_zero` to integers) and `ge_int` (an extension of `ge` to
integers) are easy exercises. As are predicates such as `is_pos` (taking an integer and returning true
exactly when that integer is greater than or equal to 0) and functions such as `abs` (taking an
integer and returning its absolute value as an integer).


## A Javascript Implementation


We can try out the above encodings with any untyped programming language that supports anonymous
functions. (We need the language to be untyped because it turns out that assigning types to the
Church encoding of natural numbers is tricky. Try it, and see where you get stuck. You basically
need a form of recursive types.)

Here is an implementation of these encodings in Javascript — I used an underscore prefix to
highlight the fact that we are working with encodings of numbers. Thus, `_3` is defined to be the
encoding of `3` as a natural number, `_plus` is addition over encoded natural numbers, and `_int` takes
the encoding of a natural number and returns the encoding of it as an integer.

    // Booleans
    const _true = x => y => x
    const _false = x => y => y
    const _not = b => b(_false)(_true)
    const _and = b => c => b(c)(_false)

    // Natural numbers
    const _0 = f => x => x
    const _1 = f => x => f(x)
    const _2 = f => x => f(f(x))
    const _3 = f => x => f(f(f(x)))
    const _4 = f => x => f(f(f(f(x))))
    const _succ = n => f => x => n(f)(f(x))
    const _plus = n => m => f => x => n(f)(m(f)(x))
    const _times = n => m => f => x => n(m(f))(x)
    const _pred = n => f => x => n(g => (h => h(g(f))))(u => x)(u => u)
    const _minus = n => m => m(_pred)(n)
    const _iszero = n => n(x => _false)(_true)
    const _ge = n => m => _not(_iszero(_minus(_succ(n))(m)))
    const _eq = n => m => _and(_ge(n)(m))(_ge(m)(n))

    // Pairs
    const _pair = x => y => s => s(x)(y)
    const _fst = p => p(x => y => x)
    const _snd = p => p(x => y => y)

    // Integers
    const _int = n => _pair(n)(_0)
    const _neg_int = i => _pair(_snd(i))(_fst(i))
    const _plus_int = i => j => _pair(_plus(_fst(i))(_fst(j)))(_plus(_snd(i))(_snd(j)))
    const _minus_int = i => j => _plus_int(i)(_neg_int(j))
    const _times_int = i => j => _pair(_plus(_times(_fst(i))(_fst(j)))(_times(_snd(i))(_snd(j))))(_plus(_times(_fst(j))(_snd(i)))(_times(_fst(i))(_snd(j))))
    const _eq_int = i => j => _eq(_plus(_fst(i))(_snd(j)))(_plus(_snd(i))(_fst(j)))

I freely admit that Javascript makes using the encoding messy, because it requires parentheses
around function calls arguments. Languages such as ML or Haskell where application is written by
simple juxtaposition would not have that problem, but there the type systems would get in the way.

Implementing these encodings in Javascript means that we should be able to convert a value in one of
these encodings into the corresponding primitive value in Javascript. 

The following functions let us do just that:

    // Convert an encoded Boolean into a primitive Boolean 
    function evalB(b) {
        return b(true)(false)
    }

    // Convert an encoded natural number into a primitive number
    function evalN(n) {
        return n(a => a+1)(0)
    }

    // Convert an encoded integer into a primitive number
    function evalI(i) {
        const t = i(x => y => [x, y])
        return evalN(t[0]) - evalN(t[1])
    }

We can now use the encodings and validate the results by converting to primitive values. Here's an
interaction with a [Node.js](https://nodejs.org) shell and the above code.

    // x = 3 + (2 x 4)
    
    > x = _plus(_3)(_times(_2)(_4))
    [Function (anonymous)]

    > evalN(x)
    11


    // y = 2 + (-3)
    
    > y = _plus_int(_int(_2))(_neg_int(_int(_3)))
    [Function (anonymous)]

    > evalI(y)
    -1


    // y == 1 ?
    
    > evalB(_eq_int(y)(_int(_1)))
    false

    // y == -1 ?
    
    > evalB(_eq_int(y)(_neg_int(_int(_1))))
    true

The code above is [available](./grothendieck.js).
