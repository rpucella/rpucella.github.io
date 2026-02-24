
# Notes on the λ-calculus (Part 1)

A term of the λ-calculus is either:

> an identifier *x*, *y*, *z*, ...
>
> an abstraction *( x → M )*  where *x* is an identifier and *M* a term
>
> an application *M N*  where *M* and *N* are terms

Examples: 

> *x* 
>
> *( x → x )*
>
> *( y → ( x → x ))* 
>
> *( x → x ( y → y ))*

Intuitively, *( x → M )* represents a function with parameter *x*
and returning *M*, while *M N* represents an application of
function *M* to argument *N*. The simplification rules below will
enforce this interpretation.

The standard presentation of the λ-calculus uses notation *λx.M* for what I write *( x → M )*, hence the name *λ-calculus*. The notation with *(...)*, while not at all standard, is easier to disambiguate.

Just like elsewhere in mathematics, we will use parentheses freely to
group terms together to affect or just clarify the order of
applications. 
Application *M  N* is a binary operation that associates to the left,
so that writing *M N P* is the same as writing *(M N) P*. If you
want *M (N P)* (which means something different) then you need to
use parentheses explicitly. 

An *occurrence* of *x* in a term *M* is the appearance of identifier *x* in *M* in a non-parameter
position. The *scope* of a parameter *x* in *( x → M )* is all of *M*.  An occurence of
*x* is said to be *bound* if it occurs in the scope of an abstraction with that identifier *x* as a
parameter.  More precisely, it is bound to the nearest enclosing abstraction. An occurrence of an
identifier is said to be *free* if it is not bound.

*Examples*: *y* is free in *( x → y )*; the first
occurrence of *x* is
free in *( y → x ( x → x ) )* while the second is not; *z* is
bound in *( z → ( x → z ) )*.

Bound identifiers can be renamed without affecting the meaning of term. Intuitively, *( x →
x )* and *( y → y )* represent the same function, namely the identity function. That
we happen to call the parameter *x* in the first and *y* in the second is pretty irrelevant. Two
terms are α-equivalent when they are equal up to renaming of some bound identifiers. Thus,
*( x → x z )* and *( y → y z )* are α-equivalent. We can rename
parameters and bound identifiers freely, as long as we do so consistently. Be careful that your
renaming does not *capture* a free occurrence of an identifier. For example, *( x → x z
)* and *( z → z z )* are *not* α-equivalent. They represent different functions.

We will generally identify α-equivalent terms.


## Substitution

An important operation is that of substituting a term *N* for an identifier *x*
inside another term *M*, written *M {N / x}*. It is defined formally as

> *x {N / x} = N*
>
> *y {N / x} = y*   when *x* is not *y*
>
> *(M P) {N / x} = M {N / x} P {N / x}*
>
> *( y → M ) {N / x} = ( y → M {N / x} )*  when *y* is not *x* and *y* is not free in *N*

In the last case, if *x = y* or if *y* is free in *N*, we can
always find a term *( z → M' )* that is α-equivalent to
*( y → M )* and such that *z* is not *x* and *z* is not free in *N* to
perform the sustitution. 

Because it avoids capturing free identifiers, this form of substitution
is called a *capture-avoiding substitution*.


## Simplification Rules


The main simplification rule is: 

>  *( x → M )  N ⇒ M {N / x}*

A term of the form *( x → M )  N* is called a *redex*.

Simplification can occur within the context of a larger term, of course, leading to the following
three derived simplification rules:

> *M P ⇒ N P*  when *M ⇒ N*
>
> *P M ⇒ P N*  when *M ⇒ N*
>
> *( x → M ) ⇒ ( x → N )*   when   *M ⇒ N*


Examples:

> *( x → x ) ( y → y )*  
>   ⇒ *x {( y → y ) / x}* = *( y → y )*

> *(( x → ( y → x ) ) v) w*  
> ⇒ *(( y → x {v / x} )) w* = *( y → v ) w*  
> ⇒ *v {w / y}* = *v*

> *(( x → ( y → y ) ) ( z → z )) ( x → ( y → x ) )*  
> ⇒ *( y → y ) {( z → z ) / x} ( x → ( y → x ) )* =  *( y → y ) ( x → ( y → x ) )*  
> ⇒ *y {( x → ( y → x ) ) / y}* = *( x → ( y → x ) )*

From now on, I will skip the explicit substitution step when
showing simplifications.

A term is in *normal form* if it has no redex. A term is normal form cannot be simplified.

Not every term can be simplified to a normal form: 

> *( x → x x ) ( x → x x )*  
>  ⇒ *( x → x x ) ( x → x x )*  
>  ⇒ *( x → x x ) ( x → x x )*  
>  ⇒ ...

There can be more than one redex in a term, meaning that
there may be more than one applicable simplification. For instance, in the term
*(( x → x ) ( y → x )) (( x → ( y → x ) ) w)*.
A property of the λ-calculus is that all the
ways to simplify a term down to a normal form yield the same normal form (up
to renaming of bound identifiers). This is called the
*Church-Rosser property*. It says that the order in which we
perform simplifications to reach a normal form is not important.

In practice, one often imposes an order in which to apply simplifications
to avoid nondeterminisn. The *normal-order strategy*, which
always simplifies the leftmost and outermost redex, is guaranteed to
find a normal form if one exists.


## Encoding Booleans

Even though the λ-calculus only
has identifiers and functions, that's enough to encode all traditional data types.

Here's one way to encode Boolean values, due to Alonzo Church:

> *__true__* = *( x → ( y → x ) )*
>
> *__false__* = *( x → ( y → y ) )*

In what sense are these encodings of Boolean values? Booleans are
useful because they allow you to select one
branch or the other of a conditional expression. 

> *__if__* = *( c → ( x → ( y → c x y ) ) )*

The trick is that
when *B* simplifies to either *__true__* or *__false__*, then *__if__ B M N*
simplifies either to *M* or to *N*, respectively:

If *B = __true__*, then:

> *__if__ B M N*  
>  ⇒ *B M N*  
>  ⇒ *__true__ M N*  
>  ⇒ *( x → ( y → x ) ) M N*  
>  ⇒ *( y → M ) N*  
>  ⇒ *M*
 
while if *B = __false__*, then:

> *__if__ B M N*  
> ⇒ *B M N*  
> ⇒ *__false__ M N*  
> ⇒ *( x → ( y → y ) ) M N*  
> ⇒ *( y → y )  N*  
> ⇒ *N*

Of course, these show that *__if__* is not strictly necessary. You
should convince yourself that *__true__ M N ⇒ M* and that
*__false__ M N ⇒ N*.

We can easily define logical operators *__and__*, *__or__*, and *__not__* using simple conditional expressions.


## Encoding Natural Numbers

Here is an encoding of natural numbers, again due to Alonzo Church, hence their name: *Church numerals*.

> *__0__* = *( f → ( x → x ) )*  
> *__1__* = *( f → ( x → f x ) )*  
> *__2__* = *( f → ( x → f (f x) ) )*  
> *__3__* = *( f → ( x → f (f (f x)) ) )*  
> *__4__* = ...

In general, natural number *n* is encoded as *( f → ( x → f (f (f (...
(f x) ... ))) ) )*, with *n* applications of *f* to *x*.

Successor operation:

> *__succ__* = *( n → ( f → ( x → n f (f x) ) ) )*

> *__succ__ __1__*  
> ⇒  *( n → ( f → ( x → n f (f x) ) ) ) ( f → ( x → f x ) )*  
> ⇒  *( f → ( x → ( f → ( x → f x ) ) f (f x) ) )*  
> ⇒  *( f → ( x → ( x → f x ) (f x) ) )*  
> ⇒  *( f → ( x → f (f x) ) )*  
> ⇒  *__2__*

Other operations:

> *__plus__* = *( m → ( n → m __succ__ n ) )*  
>
> *__times__* = *( m → ( n → ( f → ( x → m (n f) x ) ) ) )*  
>
> *__iszero?__* = *( n → n ( x → __false__ ) __true__ )*

> *__plus__ __2__ __1__*  
> ⇒  *( m → ( n → m __succ__ n ) ) __2__ __1__*  
> ⇒  *( n → __2__ __succ__ n ) __1__*  
> ⇒  *__2__ __succ__ __1__*  
> ⇒  *( f → ( x → f (f x) ) ) __succ__ __1__*  
> ⇒  *( x → __succ__ (__succ__ x) ) __1__*  
> ⇒  *__succ__ (__succ__ __1__)*  
> ⇒  *__succ__ (( n → ( f → ( x → n f (f x) ) ) ) __1__)*  
> ⇒  *__succ__ ( f → ( x → __1__ f (f x) ) )*  
> ⇒  *__succ__ ( f → ( x → ( f → ( x → f x ) ) f (f x) ) )*  
> ⇒  *__succ__ ( f → ( x → ( x → f x ) (f x) ) )*  
> ⇒  *__succ__ ( f → ( x → f (f x) ) )*  
> ⇒  *( n → ( f → ( x → n f (f x) ) ) ) ( f → ( x → f (f x) ) )*  
> ⇒  *( f → ( x → ( f → ( x → f (f x) ) ) f (f x) ) )*  
> ⇒  *( f → ( x → ( x → f (f x) ) (f x) ) )*  
> ⇒  *( f → ( x → f (f (f x)) ) )*  
> ⇒  *__3__*

> *__times__ __2__ __3__*  
> ⇒  *( m → ( n → ( f → ( x → m (n f) x ) ) ) ) __2__ __3__*  
> ⇒  *( n → ( f → ( x → __2__ (n f) x ) ) ) __3__*  
> ⇒  *( f → ( x → __2__ (__3__ f) x ) )*  
> ⇒  *( f → ( x → __2__ (( f → ( x → f (f (f x)) ) ) f) x ) )*  
> ⇒  *( f → ( x → __2__ ( x → f (f (f x)) ) x ) )*  
> ⇒  *( f → ( x → ( f → ( x → f (f x) ) ) ( x → f (f (f x)) ) x ) )*  
> ⇒  *( f → ( x → ( x → ( x → f (f (f x)) ) (( x → f (f (f x)) ) x) ) x ) )*  
> ⇒  *( f → ( x → ( x → ( x → f (f (f x)) ) (f (f (f x))) ) x ) )*  
> ⇒  *( f → ( x → ( x → f (f (f (f (f (f x))))) ) x ) )*  
> ⇒  *( f → ( x → f (f (f (f (f (f x))))) ) )*  
> ⇒  *__6__*

> *__iszero?__ __0__*  
> ⇒  *( n → n ( x → __false__ ) __true__ ) ( f → ( x → x ) )*  
> ⇒  *( f → ( x → x ) ) ( x → __false__ ) __true__*  
> ⇒  *( x → x ) __true__*  
> ⇒  *__true__*

> *__iszero?__ __2__*  
> ⇒  *( n → n ( x → __false__ ) __true__ ) ( f → ( x → f (f x) ) )*  
> ⇒  *( f → ( x → f (f x) ) ) ( x → __false__ ) __true__*  
> ⇒  *( x → ( x → __false__ ) (( x → __false__ ) x) ) __true__*  
> ⇒  *( x → ( x → __false__ ) __false__ ) __true__*  
> ⇒  *( x → __false__ ) __true__*  
> ⇒  *__false__*

An alternative way to define *__times__* is as *( m → ( n → m (__plus__ n) __0__ ) )*. Check that *__times__ __2__ __3__ ⇒ __6__*
with this definition.

Defining a predecessor function is a bit more challenging. Predecessor takes a nonzero
natural number *n* and returns *n - 1*. There are several ways of
defining such a function, all challenging. Here is probably the simplest, and it's still a doozy:

> *__pred__* = *( n → ( f → ( x → n ( g → ( h → h (g f) ) ) ( u → x ) ( u → u ) ) ) )*

(This definition under the hood uses an encoding of pairs of the kind described in the next section.)

> *__pred__ __2__*  
> ⇒  *( n → ( f → ( x → n ( g → ( h → h (g f) ) ) ( u → x ) ( u → u ) ) ) ) ( f → ( x → f (f x) ) )*  
> ⇒  *( f → ( x → ( f → ( x → f (f x) ) ) ( g → ( h → h (g f) ) ) ( u → x ) ( u → u ) ) )*  
> ⇒  *( f → ( x → ( x → ( g → ( h → h (g f) ) ) (( g → ( h → h (g f) ) ) x) ) ( u → x ) ( u → u ) ) )*  
> ⇒  *( f → ( x → ( g → ( h → h (g f) ) ) (( g → ( h → h (g f) ) ) ( u → x )) ( u → u ) ) )*  
> ⇒  *( f → ( x → ( g → ( h → h (g f) ) ) ( h → h (( u → x ) f) ) ( u → u ) ) )*  
> ⇒  *( f → ( x → ( g → ( h → h (g f) ) ) ( h → h x ) ( u → u ) ) )*  
> ⇒  *( f → ( x → ( h → h (( h → h x ) f) ) ( u → u ) ) )*  
> ⇒  *( f → ( x → ( h → h (f x) ) ( u → u ) ) )*  
> ⇒  *( f → ( x → ( u → u ) (f x) ) )*  
> ⇒  *( f → ( x → f x ) )*  
>  ⇒ *__1__*

Note that under this definition *__pred__ __0__* is just *__0__* :

> *__pred__ __0__*  
> ⇒  *( n → ( f → ( x → n ( g → ( h → h (g f) ) ) ( u → x ) ( u → u ) ) ) ) ( f → ( x → x ) )*  
> ⇒  *( f → ( x → ( f → ( x → x ) ) ( g → ( h → h (g f) ) ) ( u → x ) ( u → u ) ) )*  
> ⇒  *( f → ( x → ( x → x ) ( u → x ) ( u → u ) ) )*  
> ⇒  *( f → ( x → ( u → x ) ( u → u ) ) )*  
> ⇒  *( f → ( x → x ) )*  
> ⇒  *__0__*


## Encoding Pairs

A pair is just a packaging up of two terms in such a way that we can recover the two terms later on.

> *__pair__* =  *( x → ( y → ( s → s x y ) ) )*  
>
> *__first__* =  *( p → p ( x → ( y → x ) ) )*  
>
> *__second__* =  *( p → p ( x → ( y → y ) ) )*

Intuitively, a pair "object" obtained by pairing an *x* and a *y* is a function that accepts a selector *s* and can apply that selector *s* to both *x* and *y*. Passing the right selector will return *x*, and passing the right selector will return *y*. Definitions *__first__* and *__second__* pass the appropriate selector to the pair to extract the first (resp., second) component of the pair.

It is easy to check that this works as advertised:

> *__first__ (__pair__ a b)*  
> ⇒  *( p → p ( x → ( y → x ) ) ) (( x → ( y → ( s → s x y ) ) ) a b)*  
> ⇒  *( p → p ( x → ( y → x ) ) ) (( y → ( s → s a y ) ) b)*  
> ⇒  *( p → p ( x → ( y → x ) ) ) ( s → s a b )*  
> ⇒  *( s → s a b ) ( x → ( y → x ) )*  
> ⇒  *( x → ( y → x ) ) a b*  
> ⇒  *( y → a ) b*  
> ⇒  *a*

> *__second__ (__pair__ a b)*  
> ⇒  *( p → p ( x → ( y → y ) ) ) (( x → ( y → ( s → s x y ) ) ) a b)*  
> ⇒  *( p → p ( x → ( y → y ) ) ) (( y → ( s → s a y ) ) b)*  
> ⇒  *( p → p ( x → ( y → y ) ) ) ( s → s a b )*  
> ⇒  *( s → s a b ) ( x → ( y → y ) )*  
> ⇒  *( x → ( y → y ) ) a b*  
> ⇒  *( y → y ) b*  
> ⇒  *b*

It is an easy exercise to extend this encoding to *n*-tuples for any *n*. It is a slightly more
interesting exercise to extend this encoding to lists, that is, structures that can record an
arbitrary number of elements.


## A convenient abbreviation

To simplify the presentation of more complex terms, we can introduce a convenient abbreviation. We
write

> *( x y → M )  = ( x → ( y → M ) )*  
> *( x y z → M )  = ( x → ( y → ( z → M ) ) )*  
> *( x y z w → M ) = ( x → ( y → ( z → ( w → M ) ) ) )*  
> ...

Working through the abbreviations, this means that we have simplifications:

> *( x y → M ) N ⇒ ( y → M {N / x} )*  
> *( x y z → M ) N ⇒ ( y z → M {N / x} )*  
> ...

This, of course, is just like currying in OCaml. I may or may not use this abbreviation.
