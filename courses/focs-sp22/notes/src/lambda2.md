
# Notes on the &lambda;-calculus (Part 2)

## Recursion

With conditionals and basic data types, we are very close to having
enough to code up a simulator for Turing machines &mdash; basically code
that replicates the Turing machine simulator that we implemented in
OCaml. 

All that is missing is a way to do loops. It turns out we can write
recursive functions in the &lambda;-calculus, which as we know is
sufficient to give us loops.

Consider factorial. Intuitively, we would like to define a term __fact__
by 

> __fact__ = &#x27e8; n &rarr; __if__ (__iszero?__ n) __1__ (__times__ n (__fact__ (__pred__ n))) &#x27e9;

The problem is that this is not a valid definition: the right-hand
side refers to the term being defined. It is really an *equation*, the
same way *x = 3x + 1* is an equation. 

Consider that equation, *x = 3x + 1*. Using algebraic manipulations,
we can derive a solution to this equations, *x = -1/2*. So the
equation *x = 3x + 1* indirectly yields a definition of *x = -1/2*
that satisfies the equation.

Intuitively, we want to do the same with equation involving __fact__
above &mdash; let's call it Equation (1).  Equation (1) describes a
property we need __fact__ to have. A solution of that equation will
give us a function that satisfies that property, that is, a factorial
function. So how can we derive a definition of a function that
satisfies Equation (1)?

Unfortunately, it is not really possible to perform algebraic
manipulations the way we can to solve arithmetic equations. Instead,
we'll take a slightly different tack.

Let's return to equation *x = 3x + 1*. Define a function *F* to
capture the right hand side, so that we can write *x = F(x)* where *F*
is a function such that *F(x) = 3x + 1*. Call this *F* the *generating
function* of the equation. I claim that a value *x<sub>0</sub>* is a
solution of *x = 3x + 1* exactly when *x<sub>0</sub>* is a *fixed
point* of *F*. Recall that a fixed point of a function f: A &rarr; A
is a value a &isin; A such that f(a) = a.

>  **Proof**: *x<sub>0</sub>* is a solution of *x = 3x + 1* exactly when *x<sub>0</sub> = 3x<sub>0</sub> + 1*
>  which is just *x<sub>0</sub> = F(x<sub>0</sub>)* which is just *x<sub>0</sub>* being a fixed point of *F*.

We are going to find a solution to Equation (1) by finding a fixed
point of its generating function. What is the generating function of
Equation (1)? Well, we can write Equation (1) as

> __fact__ = F<sub>fact</sub> __fact__

where F<sub>fact</sub> is:

> F<sub>fact</sub> = &#x27e8; f &rarr; &#x27e8; n &rarr; __if__ (__iszero?__ n) __1__ (__times__ n (f (__pred__ n))) &#x27e9; &#x27e9;

We can check that a fixed point of F<sub>fact</sub> could be used as a
factorial function. Let __f__ be a fixed point of F<sub>fact</sub>,
that is, F<sub>fact</sub> __f__ = __f__. In fact, we'll show that
__f__ &xrArr; F<sub>fact</sub> __f__, which is compatible with our use
of &xrArr; for simplifications.

> __f__ __3__  
>  &xrArr; F<sub>fact</sub> __f__ __3__  
>  &xrArr; &#x27e8; f &rarr; &#x27e8; n &rarr; __if__ (__iszero?__ n) __1__ (__times__ n (f (__pred__ n))) &#x27e9; &#x27e9; __f__ __3__  
>  &xrArr; &#x27e8; n &rarr; __if__ (__iszero?__ n) __1__ (__times__ n (__f__ (__pred__ n))) &#x27e9; __3__  
>  &xrArr; __if__ (__iszero?__ __3__) __1__ (__times__ __3__ (__f__ (__pred__ __3__)  
>  &xrArr; __times__ __3__ (__f__ (__pred__ __3__))  
>  &xrArr; __times__ __3__ (__f__ __2__)  
>  &xrArr; __times__ __3__ (F<sub>fact</sub> __f__ __2__)  
>  &xrArr; __times__ __3__ (__times__ __2__ (__f__ __1__))  
>  &xrArr; __times__ __3__ (__times__ __2__ (F<sub>fact</sub> __f__ __1__))  
>  &xrArr; __times__ __3__ (__times__ __2__ (__times__ __1__ (__f__ __1__)))  
>  &xrArr; __times__ __3__ (__times__ __2__ (__times__ __1__ (F<sub>fact</sub> __f__ __1__)))  
>  &xrArr; __times__ __3__ (__times__ __2__ (__times__ __1__ __1__))  
>  &xrArr; __6__

I coalesced together several sequences of simplification steps in the above,
for the sake of space.

It's not hard to see that (__f__ n) simplifies to the
factorial of n, and therefore a fixed point of F<sub>fact</sub>
indeed can be used as the factorial function. 

The only thing we need now is to figure out how to find fixed points in the
&lambda;-calculus. The following term &Theta; does just that, for *any* term of the &lambda;-calculus:

> &Theta;<sub>0</sub> = &#x27e8; x &rarr; &#x27e8; y &rarr; y ((x x) y) &#x27e9; &#x27e9;

> &Theta; = &Theta;<sub>0</sub> &Theta;<sub>0</sub>

**Theorem:** &Theta; G is a fixed point of G for any term G.

More specifically (and more usefully for us), we get that &Theta; G &xrArr; F (&Theta; G):

> &Theta; G  
> &xrArr; (&Theta;<sub>0</sub> &Theta;<sub>0</sub>) G  
> &xrArr; (&#x27e8; x &rarr; &#x27e8; y &rarr; y ((x x) y) &#x27e9; &#x27e9; &Theta;<sub>0</sub>) G  
> &xrArr; &#x27e8; y &rarr; y ((&Theta;<sub>0</sub> &Theta;<sub>0</sub>) y) &#x27e9; G  
> &xrArr; G ((&Theta;<sub>0</sub> &Theta;<sub>0</sub>) G)  
> &xrArr; G (&Theta; G)

Therefore, &Theta; G &xrArr; G (&Theta; G) &mdash; exactly the simplification
that we used above to show that &Theta; F<sub>fact</sub> could be used
as the factorial function.

We can therefore define factorial as:

> __fact__ = &Theta; F<sub>fact</sub>

And we can check directly that

> __fact__ __3__ &xrArr; __6__

> __fact__ __4__ &xrArr; __24__

Once we have natural numbers, conditionals, pairing, and recursion, it
is a simple matter to implement a Turing machine simulator (similarly
to how we implemented a Turing machine simulator in OCaml) showing the
&lambda;-calculus Turing-complete, and therefore able to express all
computable decision problems.
