
# Notes on the &lambda;-calculus (Part 1)

A term of the &lambda;-calculus is either:

> an identifier *x*, *y*, *z*, \dots
>
> an abstraction *&#x27e8; x &rarr; M &#x27e9;*  where *x* is an identifier and *M* a term
>
> an application *M N*  where *M* and *N* are terms

Examples: 

> *x* 
>
> *&#x27e8; x &rarr; x &#x27e9;*
>
> *&#x27e8; y &rarr; &#x27e8; x &rarr; x &#x27e9;&#x27e9;* 
>
> *&#x27e8; x &rarr; x &#x27e8; y &rarr; y &#x27e9;&#x27e9;*

Intuitively, *&#x27e8; x &rarr; M &#x27e9;* represents a function with parameter *x*
and returning *M*, while *M N* represents an application of
function *M* to argument *N*. The simplification rules below will
enforce this interpretation.

The standard presentation of the &lambda;-calculus uses notation *\lambda x.M* for what I write *&#x27e8; x &rarr; M &#x27e9;*, hence the name *&lambda;-calculus*. The notation with *&#x27e8;...&#x27e9;*, while not at all standard, is easier to disambiguate.

Just like elsewhere in mathematics, we will use parentheses freely to
group terms together to affect or just clarify the order of
applications. 
Application *M  N* is a binary operation that associates to the left,
so that writing *M N P* is the same as writing *(M N) P*. If you
want *M (N P)* (which means something different) then you need to
use parentheses explicitly. 

An *occurrence* of *x* in a term *M* is the appearance of identifier *x* in *M* in a non-parameter
position. The *scope* of a parameter *x* in *&#x27e8; x &rarr; M &#x27e9;* is all of *M*.  An occurence of
*x* is said to be *bound* if it occurs in the scope of an abstraction with that identifier *x* as a
parameter.  More precisely, it is bound to the nearest enclosing abstraction. An occurrence of an
identifier is said to be *free* if it is not bound.

*Examples: *y* is free in *&#x27e8; x &rarr; y &#x27e9;*; the first
occurrence of *x* is
free in *&#x27e8; y &rarr; x &#x27e8; x &rarr; x &#x27e9; &#x27e9;* while the second is not; *z* is
bound in *&#x27e8; z &rarr; &#x27e8; x &rarr; z &#x27e9; &#x27e9;*.

Bound identifiers can be renamed without affecting the meaning of term. Intuitively, *&#x27e8; x &rarr;
x &#x27e9;* and *&#x27e8; y &rarr; y &#x27e9;* represent the same function, namely the identity function. That
we happen to call the parameter *x* in the first and *y* in the second is pretty irrelevant. Two
terms are &alpha;-equivalent when they are equal up to renaming of some bound identifiers. Thus,
*&#x27e8; x &rarr; x z &#x27e9;* and *&#x27e8; y &rarr; y z &#x27e9;* are &alpha;-equivalent. We can rename
parameters and bound identifiers freely, as long as we do so consistently. Be careful that your
renaming does not *capture* a free occurrence of an identifier. For example, *&#x27e8; x &rarr; x z
&#x27e9;* and *&#x27e8; z &rarr; z z &#x27e9;* are *not* &alpha;-equivalent. They represent different functions.

We will generally identify &alpha;-equivalent terms.


## Substitution

An important operation is that of substituting a term *N* for an identifier *x*
inside another term *M*, written *M [N / x]*. It is defined formally as

> *x [N / x] = N*
>
> *y [N / x] = y*   when *x* is not *y*
>
> *(M P) [N / x] = M [N / x] P [N / x]*
>
> *&#x27e8; y &rarr; M &#x27e9; [N / x] = &#x27e8; y &rarr; M [N / x] &#x27e9;*  when *y* is not *x* and *y* is not free in *N*

In the last case, if *x = y* or if *y* is free in *N*, we can
always find a term *&#x27e8; z &rarr; M' &#x27e9;* that is &alpha;-equivalent to
*&#x27e8; y &rarr; M &#x27e9;* and such that *z* is not *x* and *z* is not free in *N* to
perform the sustitution. 

Because it avoids capturing free identifiers, this form of substitution
is called a *capture-avoiding substitution*.


## Simplification Rules


The main simplification rule is: 

>  *&#x27e8; x &rarr; M &#x27e9;  N &xrArr; M [N / x]*

A term of the form *&#x27e8; x &rarr; M &#x27e9;  N* is called a *redex*.

Simplification can occur within the context of a larger term, of course, leading to the following
three derived simplification rules:

> *M P &xrArr; N P*  when *M &xrArr; N*
>
> *P M &xrArr; P N*  when *M &xrArr; N*
>
> *&#x27e8; x &rarr; M &#x27e9; &xrArr; &#x27e8; x &rarr; N &#x27e9;*   when   *M &xrArr; N*


Examples:

> *&#x27e8; x &rarr; x &#x27e9; &#x27e8; y &rarr; y &#x27e9;*  
>   &xrArr; *x [&#x27e8; y &rarr; y &#x27e9; / x]* = *&#x27e8; y &rarr; y &#x27e9;*

> *(&#x27e8; x &rarr; &#x27e8; y &rarr; x &#x27e9; &#x27e9; v) w*  
> &xrArr; *(&#x27e8; y &rarr; x [v / x] &#x27e9;) w* = *&#x27e8; y &rarr; v &#x27e9; w*  
> &xrArr; *v [w / y]* = *v*

> *(&#x27e8; x &rarr; &#x27e8; y &rarr; y &#x27e9; &#x27e9; &#x27e8; z &rarr; z &#x27e9;) &#x27e8; x &rarr; &#x27e8; y &rarr; x &#x27e9; &#x27e9;*  
> &xrArr; *&#x27e8; y &rarr; y &#x27e9; [&#x27e8; z &rarr; z &#x27e9; / x] &#x27e8; x &rarr; &#x27e8; y &rarr; x &#x27e9; &#x27e9;* =  *&#x27e8; y &rarr; y &#x27e9; &#x27e8; x &rarr; &#x27e8; y &rarr; x &#x27e9; &#x27e9;*  
> &xrArr; *y [&#x27e8; x &rarr; &#x27e8; y &rarr; x &#x27e9; &#x27e9; / y]* = *&#x27e8; x &rarr; &#x27e8; y &rarr; x &#x27e9; &#x27e9;*

From now on, I will skip the explicit substitution step when
showing simplifications.

A term is in *normal form* if it has no redex. A term is normal form cannot be simplified.

Not every term can be simplified to a normal form: 

> *&#x27e8; x &rarr; x x &#x27e9; &#x27e8; x &rarr; x x &#x27e9;*  
>  &xrArr; *&#x27e8; x &rarr; x x &#x27e9; &#x27e8; x &rarr; x x &#x27e9;*  
>  &xrArr; *&#x27e8; x &rarr; x x &#x27e9; &#x27e8; x &rarr; x x &#x27e9;*  
>  &xrArr; ...

There can be more than one redex in a term, meaning that
there may be more than one applicable simplification. For instance, in the term
*(&#x27e8; x &rarr; x &#x27e9; &#x27e8; y &rarr; x &#x27e9;) (&#x27e8; x &rarr; &#x27e8; y &rarr; x &#x27e9; &#x27e9; w)*.
A property of the &lambda;-calculus is that all the
ways to simplify a term down to a normal form yield the same normal form (up
to renaming of bound identifiers). This is called the
*Church-Rosser property*. It says that the order in which we
perform simplifications to reach a normal form is not important.

In practice, one often imposes an order in which to apply simplifications
to avoid nondeterminisn. The *normal-order strategy*, which
always simplifies the leftmost and outermost redex, is guaranteed to
find a normal form if one exists.


## Encoding Booleans

Even though the &lambda;-calculus only
has identifiers and functions, that's enough to encode all traditional data types.

Here's one way to encode Boolean values, due to Alonzo Church:

> *__true__* = *&#x27e8; x &rarr; &#x27e8; y &rarr; x &#x27e9; &#x27e9;*
>
> *__false__* = *&#x27e8; x &rarr; &#x27e8; y &rarr; y &#x27e9; &#x27e9;*

In what sense are these encodings of Boolean values? Booleans are
useful because they allow you to select one
branch or the other of a conditional expression. 

> *__if__* = *&#x27e8; c &rarr; &#x27e8; x &rarr; &#x27e8; y &rarr; c x y &#x27e9; &#x27e9; &#x27e9;*

The trick is that
when *B* simplifies to either *__true__* or *__false__*, then *__if__ B M N*
simplifies either to *M* or to *N*, respectively:

If *B = __true__*, then:

> *__if__ B M N*  
>  &xrArr; *B M N*  
>  &xrArr; *__true__ M N*  
>  &xrArr; *&#x27e8; x &rarr; &#x27e8; y &rarr; x &#x27e9; &#x27e9; M N*  
>  &xrArr; *&#x27e8; y &rarr; M &#x27e9; N*  
>  &xrArr; *M*
 
while if *B = __false__*, then:

> *__if__ B M N*  
> &xrArr; *B M N*  
> &xrArr; *__false__ M N*  
> &xrArr; *&#x27e8; x &rarr; &#x27e8; y &rarr; y &#x27e9; &#x27e9; M N*  
> &xrArr; *&#x27e8; y &rarr; y &#x27e9;  N*  
> &xrArr; *N*

Of course, these show that *__if__* is not strictly necessary. You
should convince yourself that *__true__ M N &xrArr; M* and that
*__false__ M N &xrArr; N*.

We can easily define logical operators *__and__*, *__or__*, and *__not__* using simple conditional expressions.


## Encoding Natural Numbers

Here is an encoding of natural numbers, again due to Alonzo Church, hence their name: *Church numerals*.

> *__0__* = *&#x27e8; f &rarr; &#x27e8; x &rarr; x &#x27e9; &#x27e9;*  
> *__1__* = *&#x27e8; f &rarr; &#x27e8; x &rarr; f x &#x27e9; &#x27e9;*  
> *__2__* = *&#x27e8; f &rarr; &#x27e8; x &rarr; f (f x) &#x27e9; &#x27e9;*  
> *__3__* = *&#x27e8; f &rarr; &#x27e8; x &rarr; f (f (f x)) &#x27e9; &#x27e9;*  
> *__4__* = ...

In general, natural number *n* is encoded as *&#x27e8; f &rarr; &#x27e8; x &rarr; f (f (f (...
(f x) ... ))) &#x27e9; &#x27e9;*, with *n* applications of *f* to *x*.

Successor operation:

> *__succ__* = *&#x27e8; n &rarr; &#x27e8; f &rarr; &#x27e8; x &rarr; n f (f x) &#x27e9; &#x27e9; &#x27e9;*

> *__succ__ __1__*  
> &xrArr;  *&#x27e8; n &rarr; &#x27e8; f &rarr; &#x27e8; x &rarr; n f (f x) &#x27e9; &#x27e9; &#x27e9; &#x27e8; f &rarr; &#x27e8; x &rarr; f x &#x27e9; &#x27e9;*  
> &xrArr;  *&#x27e8; f &rarr; &#x27e8; x &rarr; &#x27e8; f &rarr; &#x27e8; x &rarr; f x &#x27e9; &#x27e9; f (f x) &#x27e9; &#x27e9;*  
> &xrArr;  *&#x27e8; f &rarr; &#x27e8; x &rarr; &#x27e8; x &rarr; f x &#x27e9; (f x) &#x27e9; &#x27e9;*  
> &xrArr;  *&#x27e8; f &rarr; &#x27e8; x &rarr; f (f x) &#x27e9; &#x27e9;*  
> &xrArr;  *__2__*

Other operations:

> *__plus__* = *&#x27e8; m &rarr; &#x27e8; n &rarr; m __succ__ n &#x27e9; &#x27e9;*  
>
> *__times__* = *&#x27e8; m &rarr; &#x27e8; n &rarr; &#x27e8; f &rarr; &#x27e8; x &rarr; m (n f) x &#x27e9; &#x27e9; &#x27e9; &#x27e9;*  
>
> *__iszero?__* = *&#x27e8; n &rarr; n &#x27e8; x &rarr; __false__ &#x27e9; __true__ &#x27e9;*

> *__plus__ __2__ __1__*  
> &xrArr;  *&#x27e8; m &rarr; &#x27e8; n &rarr; m __succ__ n &#x27e9; &#x27e9; __2__ __1__*  
> &xrArr;  *&#x27e8; n &rarr; __2__ __succ__ n &#x27e9; __1__*  
> &xrArr;  *__2__ __succ__ __1__*  
> &xrArr;  *&#x27e8; f &rarr; &#x27e8; x &rarr; f (f x) &#x27e9; &#x27e9; __succ__ __1__*  
> &xrArr;  *&#x27e8; x &rarr; __succ__ (__succ__ x) &#x27e9; __1__*  
> &xrArr;  *__succ__ (__succ__ __1__)*  
> &xrArr;  *__succ__ (&#x27e8; n &rarr; &#x27e8; f &rarr; &#x27e8; x &rarr; n f (f x) &#x27e9; &#x27e9; &#x27e9; __1__)*  
> &xrArr;  *__succ__ &#x27e8; f &rarr; &#x27e8; x &rarr; __1__ f (f x) &#x27e9; &#x27e9;*  
> &xrArr;  *__succ__ &#x27e8; f &rarr; &#x27e8; x &rarr; &#x27e8; f &rarr; &#x27e8; x &rarr; f x &#x27e9; &#x27e9; f (f x) &#x27e9; &#x27e9;*  
> &xrArr;  *__succ__ &#x27e8; f &rarr; &#x27e8; x &rarr; &#x27e8; x &rarr; f x &#x27e9; (f x) &#x27e9; &#x27e9;*  
> &xrArr;  *__succ__ &#x27e8; f &rarr; &#x27e8; x &rarr; f (f x) &#x27e9; &#x27e9;*  
> &xrArr;  *&#x27e8; n &rarr; &#x27e8; f &rarr; &#x27e8; x &rarr; n f (f x) &#x27e9; &#x27e9; &#x27e9; &#x27e8; f &rarr; &#x27e8; x &rarr; f (f x) &#x27e9; &#x27e9;*  
> &xrArr;  *&#x27e8; f &rarr; &#x27e8; x &rarr; &#x27e8; f &rarr; &#x27e8; x &rarr; f (f x) &#x27e9; &#x27e9; f (f x) &#x27e9; &#x27e9;*  
> &xrArr;  *&#x27e8; f &rarr; &#x27e8; x &rarr; &#x27e8; x &rarr; f (f x) &#x27e9; (f x) &#x27e9; &#x27e9;*  
> &xrArr;  *&#x27e8; f &rarr; &#x27e8; x &rarr; f (f (f x)) &#x27e9; &#x27e9;*  
> &xrArr;  *__3__*

> *__times__ __2__ __3__*  
> &xrArr;  *&#x27e8; m &rarr; &#x27e8; n &rarr; &#x27e8; f &rarr; &#x27e8; x &rarr; m (n f) x &#x27e9; &#x27e9; &#x27e9; &#x27e9; __2__ __3__*  
> &xrArr;  *&#x27e8; n &rarr; &#x27e8; f &rarr; &#x27e8; x &rarr; __2__ (n f) x &#x27e9; &#x27e9; &#x27e9; __3__*  
> &xrArr;  *&#x27e8; f &rarr; &#x27e8; x &rarr; __2__ (__3__ f) x &#x27e9; &#x27e9;*  
> &xrArr;  *&#x27e8; f &rarr; &#x27e8; x &rarr; __2__ (&#x27e8; f &rarr; &#x27e8; x &rarr; f (f (f x)) &#x27e9; &#x27e9; f) x &#x27e9; &#x27e9;*  
> &xrArr;  *&#x27e8; f &rarr; &#x27e8; x &rarr; __2__ &#x27e8; x &rarr; f (f (f x)) &#x27e9; x &#x27e9; &#x27e9;*  
> &xrArr;  *&#x27e8; f &rarr; &#x27e8; x &rarr; &#x27e8; f &rarr; &#x27e8; x &rarr; f (f x) &#x27e9; &#x27e9; &#x27e8; x &rarr; f (f (f x)) &#x27e9; x &#x27e9; &#x27e9;*  
> &xrArr;  *&#x27e8; f &rarr; &#x27e8; x &rarr; &#x27e8; x &rarr; &#x27e8; x &rarr; f (f (f x)) &#x27e9; (&#x27e8; x &rarr; f (f (f x)) &#x27e9; x) &#x27e9; x &#x27e9; &#x27e9;*  
> &xrArr;  *&#x27e8; f &rarr; &#x27e8; x &rarr; &#x27e8; x &rarr; &#x27e8; x &rarr; f (f (f x)) &#x27e9; (f (f (f x))) &#x27e9; x &#x27e9; &#x27e9;*  
> &xrArr;  *&#x27e8; f &rarr; &#x27e8; x &rarr; &#x27e8; x &rarr; f (f (f (f (f (f x))))) &#x27e9; x &#x27e9; &#x27e9;*  
> &xrArr;  *&#x27e8; f &rarr; &#x27e8; x &rarr; f (f (f (f (f (f x))))) &#x27e9; &#x27e9;*  
> &xrArr;  *__6__*

> *__iszero?__ __0__*  
> &xrArr;  *&#x27e8; n &rarr; n &#x27e8; x &rarr; __false__ &#x27e9; __true__ &#x27e9; &#x27e8; f &rarr; &#x27e8; x &rarr; x &#x27e9; &#x27e9;*  
> &xrArr;  *&#x27e8; f &rarr; &#x27e8; x &rarr; x &#x27e9; &#x27e9; &#x27e8; x &rarr; __false__ &#x27e9; __true__*  
> &xrArr;  *&#x27e8; x &rarr; x &#x27e9; __true__*  
> &xrArr;  *__true__*

> *__iszero?__ __2__*  
> &xrArr;  *&#x27e8; n &rarr; n &#x27e8; x &rarr; __false__ &#x27e9; __true__ &#x27e9; &#x27e8; f &rarr; &#x27e8; x &rarr; f (f x) &#x27e9; &#x27e9;*  
> &xrArr;  *&#x27e8; f &rarr; &#x27e8; x &rarr; f (f x) &#x27e9; &#x27e9; &#x27e8; x &rarr; __false__ &#x27e9; __true__*  
> &xrArr;  *&#x27e8; x &rarr; &#x27e8; x &rarr; __false__ &#x27e9; (&#x27e8; x &rarr; __false__ &#x27e9; x) &#x27e9; __true__*  
> &xrArr;  *&#x27e8; x &rarr; &#x27e8; x &rarr; __false__ &#x27e9; __false__ &#x27e9; __true__*  
> &xrArr;  *&#x27e8; x &rarr; __false__ &#x27e9; __true__*  
> &xrArr;  *__false__*

An alternative way to define *__times__* is as *&#x27e8; m &rarr; &#x27e8; n &rarr; m (__plus__ n) __0__ &#x27e9; &#x27e9;*. Check that *__times__ __2__ __3__ &xrArr; __6__*
with this definition.

Defining a predecessor function is a bit more challenging. Predecessor takes a nonzero
natural number *n* and returns *n - 1*. There are several ways of
defining such a function, all challenging. Here is probably the simplest, and it's still a doozy:

> *__pred__* = *&#x27e8; n &rarr; &#x27e8; f &rarr; &#x27e8; x &rarr; n &#x27e8; g &rarr; &#x27e8; h &rarr; h (g f) &#x27e9; &#x27e9; &#x27e8; u &rarr; x &#x27e9; &#x27e8; u &rarr; u &#x27e9; &#x27e9; &#x27e9; &#x27e9;*

(This definition under the hood uses an encoding of pairs of the kind described in the next section.)

> *__pred__ __2__*  
> &xrArr;  *&#x27e8; n &rarr; &#x27e8; f &rarr; &#x27e8; x &rarr; n &#x27e8; g &rarr; &#x27e8; h &rarr; h (g f) &#x27e9; &#x27e9; &#x27e8; u &rarr; x &#x27e9; &#x27e8; u &rarr; u &#x27e9; &#x27e9; &#x27e9; &#x27e9; &#x27e8; f &rarr; &#x27e8; x &rarr; f (f x) &#x27e9; &#x27e9;*  
> &xrArr;  *&#x27e8; f &rarr; &#x27e8; x &rarr; &#x27e8; f &rarr; &#x27e8; x &rarr; f (f x) &#x27e9; &#x27e9; &#x27e8; g &rarr; &#x27e8; h &rarr; h (g f) &#x27e9; &#x27e9; &#x27e8; u &rarr; x &#x27e9; &#x27e8; u &rarr; u &#x27e9; &#x27e9; &#x27e9;*  
> &xrArr;  *&#x27e8; f &rarr; &#x27e8; x &rarr; &#x27e8; x &rarr; &#x27e8; g &rarr; &#x27e8; h &rarr; h (g f) &#x27e9; &#x27e9; (&#x27e8; g &rarr; &#x27e8; h &rarr; h (g f) &#x27e9; &#x27e9; x) &#x27e9; &#x27e8; u &rarr; x &#x27e9; &#x27e8; u &rarr; u &#x27e9; &#x27e9; &#x27e9;*  
> &xrArr;  *&#x27e8; f &rarr; &#x27e8; x &rarr; &#x27e8; g &rarr; &#x27e8; h &rarr; h (g f) &#x27e9; &#x27e9; (&#x27e8; g &rarr; &#x27e8; h &rarr; h (g f) &#x27e9; &#x27e9; &#x27e8; u &rarr; x &#x27e9;) &#x27e8; u &rarr; u &#x27e9; &#x27e9; &#x27e9;*  
> &xrArr;  *&#x27e8; f &rarr; &#x27e8; x &rarr; &#x27e8; g &rarr; &#x27e8; h &rarr; h (g f) &#x27e9; &#x27e9; &#x27e8; h &rarr; h (&#x27e8; u &rarr; x &#x27e9; f) &#x27e9; &#x27e8; u &rarr; u &#x27e9; &#x27e9; &#x27e9;*  
> &xrArr;  *&#x27e8; f &rarr; &#x27e8; x &rarr; &#x27e8; g &rarr; &#x27e8; h &rarr; h (g f) &#x27e9; &#x27e9; &#x27e8; h &rarr; h x &#x27e9; &#x27e8; u &rarr; u &#x27e9; &#x27e9; &#x27e9;*  
> &xrArr;  *&#x27e8; f &rarr; &#x27e8; x &rarr; &#x27e8; h &rarr; h (&#x27e8; h &rarr; h x &#x27e9; f) &#x27e9; &#x27e8; u &rarr; u &#x27e9; &#x27e9; &#x27e9;*  
> &xrArr;  *&#x27e8; f &rarr; &#x27e8; x &rarr; &#x27e8; h &rarr; h (f x) &#x27e9; &#x27e8; u &rarr; u &#x27e9; &#x27e9; &#x27e9;*  
> &xrArr;  *&#x27e8; f &rarr; &#x27e8; x &rarr; &#x27e8; u &rarr; u &#x27e9; (f x) &#x27e9; &#x27e9;*  
> &xrArr;  *&#x27e8; f &rarr; &#x27e8; x &rarr; f x &#x27e9; &#x27e9;*  
>  &xrArr; *__1__*

Note that under this definition *__pred__ __0__* is just *__0__* :

> *__pred__ __0__*  
> &xrArr;  *&#x27e8; n &rarr; &#x27e8; f &rarr; &#x27e8; x &rarr; n &#x27e8; g &rarr; &#x27e8; h &rarr; h (g f) &#x27e9; &#x27e9; &#x27e8; u &rarr; x &#x27e9; &#x27e8; u &rarr; u &#x27e9; &#x27e9; &#x27e9; &#x27e9; &#x27e8; f &rarr; &#x27e8; x &rarr; x &#x27e9; &#x27e9;*  
> &xrArr;  *&#x27e8; f &rarr; &#x27e8; x &rarr; &#x27e8; f &rarr; &#x27e8; x &rarr; x &#x27e9; &#x27e9; &#x27e8; g &rarr; &#x27e8; h &rarr; h (g f) &#x27e9; &#x27e9; &#x27e8; u &rarr; x &#x27e9; &#x27e8; u &rarr; u &#x27e9; &#x27e9; &#x27e9;*  
> &xrArr;  *&#x27e8; f &rarr; &#x27e8; x &rarr; &#x27e8; x &rarr; x &#x27e9; &#x27e8; u &rarr; x &#x27e9; &#x27e8; u &rarr; u &#x27e9; &#x27e9; &#x27e9;*  
> &xrArr;  *&#x27e8; f &rarr; &#x27e8; x &rarr; &#x27e8; u &rarr; x &#x27e9; &#x27e8; u &rarr; u &#x27e9; &#x27e9; &#x27e9;*  
> &xrArr;  *&#x27e8; f &rarr; &#x27e8; x &rarr; x &#x27e9; &#x27e9;*  
> &xrArr;  *__0__*


## Encoding Pairs

A pair is just a packaging up of two terms in such a way that we can recover the two terms later on.

> *__pair__* =  *&#x27e8; x &rarr; &#x27e8; y &rarr; &#x27e8; s &rarr; s x y &#x27e9; &#x27e9; &#x27e9;*  
>
> *__first__* =  *&#x27e8; p &rarr; p &#x27e8; x &rarr; &#x27e8; y &rarr; x &#x27e9; &#x27e9; &#x27e9;*  
>
> *__second__* =  *&#x27e8; p &rarr; p &#x27e8; x &rarr; &#x27e8; y &rarr; y &#x27e9; &#x27e9; &#x27e9;*

Intuitively, a pair "object" obtained by pairing an *x* and a *y* is a function that accepts a selector *s* and can apply that selector *s* to both *x* and *y*. Passing the right selector will return *x*, and passing the right selector will return *y*. Definitions *__first__* and *__second__* pass the appropriate selector to the pair to extract the first (resp., second) component of the pair.

It is easy to check that this works as advertised:

> *__first__ (__pair__ a b)*  
> &xrArr;  *&#x27e8; p &rarr; p &#x27e8; x &rarr; &#x27e8; y &rarr; x &#x27e9; &#x27e9; &#x27e9; (&#x27e8; x &rarr; &#x27e8; y &rarr; &#x27e8; s &rarr; s x y &#x27e9; &#x27e9; &#x27e9; a b)*  
> &xrArr;  *&#x27e8; p &rarr; p &#x27e8; x &rarr; &#x27e8; y &rarr; x &#x27e9; &#x27e9; &#x27e9; (&#x27e8; y &rarr; &#x27e8; s &rarr; s a y &#x27e9; &#x27e9; b)*  
> &xrArr;  *&#x27e8; p &rarr; p &#x27e8; x &rarr; &#x27e8; y &rarr; x &#x27e9; &#x27e9; &#x27e9; &#x27e8; s &rarr; s a b &#x27e9;*  
> &xrArr;  *&#x27e8; s &rarr; s a b &#x27e9; &#x27e8; x &rarr; &#x27e8; y &rarr; x &#x27e9; &#x27e9;*  
> &xrArr;  *&#x27e8; x &rarr; &#x27e8; y &rarr; x &#x27e9; &#x27e9; a b*  
> &xrArr;  *&#x27e8; y &rarr; a &#x27e9; b*  
> &xrArr;  *a*

> *__second__ (__pair__ a b)*  
> &xrArr;  *&#x27e8; p &rarr; p &#x27e8; x &rarr; &#x27e8; y &rarr; y &#x27e9; &#x27e9; &#x27e9; (&#x27e8; x &rarr; &#x27e8; y &rarr; &#x27e8; s &rarr; s x y &#x27e9; &#x27e9; &#x27e9; a b)*  
> &xrArr;  *&#x27e8; p &rarr; p &#x27e8; x &rarr; &#x27e8; y &rarr; y &#x27e9; &#x27e9; &#x27e9; (&#x27e8; y &rarr; &#x27e8; s &rarr; s a y &#x27e9; &#x27e9; b)*  
> &xrArr;  *&#x27e8; p &rarr; p &#x27e8; x &rarr; &#x27e8; y &rarr; y &#x27e9; &#x27e9; &#x27e9; &#x27e8; s &rarr; s a b &#x27e9;*  
> &xrArr;  *&#x27e8; s &rarr; s a b &#x27e9; &#x27e8; x &rarr; &#x27e8; y &rarr; y &#x27e9; &#x27e9;*  
> &xrArr;  *&#x27e8; x &rarr; &#x27e8; y &rarr; y &#x27e9; &#x27e9; a b*  
> &xrArr;  *&#x27e8; y &rarr; y &#x27e9; b*  
> &xrArr;  *b*

It is an easy exercise to extend this encoding to *n*-tuples for any *n*. It is a slightly more
interesting exercise to extend this encoding to lists, that is, structures that can record an
arbitrary number of elements.


## A convenient abbreviation

To simplify the presentation of more complex terms, we can introduce a convenient abbreviation. We
write

> *&#x27e8; x y &rarr; M &#x27e9;  = &#x27e8; x &rarr; &#x27e8; y &rarr; M &#x27e9; &#x27e9;*  
> *&#x27e8; x y z &rarr; M &#x27e9;  = &#x27e8; x &rarr; &#x27e8; y &rarr; &#x27e8; z &rarr; M &#x27e9; &#x27e9; &#x27e9;*  
> *&#x27e8; x y z w &rarr; M &#x27e9; = &#x27e8; x &rarr; &#x27e8; y &rarr; &#x27e8; z &rarr; &#x27e8; w &rarr; M &#x27e9; &#x27e9; &#x27e9; &#x27e9;*  
> ...

Working through the abbreviations, this means that we have simplifications:

> *&#x27e8; x y &rarr; M &#x27e9; N &xrArr; &#x27e8; y &rarr; M [N / x] &#x27e9;*  
> *&#x27e8; x y z &rarr; M &#x27e9; N &xrArr; &#x27e8; y z &rarr; M [N / x] &#x27e9;*  
> ...

This, of course, is just like currying in OCaml. I may or may not use this abbreviation.


