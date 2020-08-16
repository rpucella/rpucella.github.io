# Homework 2

## Due Date: Tuesday, February 11, 2020 (23h59)

- This homework is to be done in teams of 2, or singly. You may
discuss problems with fellow students, but all submitted work must be
entirely your team's own, and should not be from any other course,
present, past, or future. If you use a solution from another source
you must cite it &mdash; this includes when that source is someone
else helping you.

- **Please do not post your solutions on a
public website or a public repository like GitHub.**

- All programming is to be done in Scala 2.13. 

- Code your answers by modifying the file [`homework2.scala`](homework2.scala) provided. Add your team members' **name**, **email address**, and any **remarks** that you
wish to make to the instructor to the block comment at the top of the file.

- **Please do not change the types in the signature of the
method stubs I provide**. Doing so will make it
impossible to load your code into the testing infrastructure,
which tends to make me unhappy.

- Feel free to define helper functions if you need them.


## Electronic Submission Instructions

- Run your homework code via `scala homework2.scala`

- If there are any error, do not submit. I can't test what will not load correctly.

- When you're ready to submit, send an email with your file `homework2.scala` as an attachment to `olin.submissions@gmail.com` with subject _Homework 2 submission_.


* * *

## Question 1 : Factions

In class, we saw some code that implements fractions as a
kind of value in class `Value`. This code is available
in the source file for thi s homework. In this question, we'll
add to the functionality.

The sample outputs below assume the following definitions:

    // empty environment
    val nullEnv = new Env(List())
    
    // useful expression constructors
    def _int (i:Int) : Exp = new EInteger(i)
    def _frac (n:Int, d:Int) : Exp = new EFraction(n, d)
    def _plus (e1:Exp, e2:Exp) : Exp = new EPlus(e1, e2)
    def _times (e1:Exp, e2:Exp) : Exp = new ETimes(e1, e2)
    def _div (e1:Exp, e2:Exp) : Exp = new EDiv(e1, e2)


### (A)

Code an expression class **`EDiv`**
which takes two expressions and evaluates to the fraction
number obtained by dividing the result of evaluating the
first to the result of evaluating the second. Division
should work with both integers and fractions.

Sample output:

    scala> _div(_int(1), _int(2)).eval(nullEnv)
    res0: Value = VFraction(1, 2)
    
    scala> _div(_int(2), _int(3)).eval(nullEnv)
    res1: Value = VFraction(2, 3)
    
    scala> _div(_div(_int(2), _int(3)), _int(4)).eval(nullEnv)
    res2: Value = VFraction(1, 6)
    
    scala> _div(_int(2), _div(_int(3), _int(4))).eval(nullEnv)
    res3: Value = VFraction(8, 3)
    
    scala> _div(_div(_int(1), _int(2)), _div(_int(3), _int(5))).eval(nullEnv)
    res4: Value = VFraction(5, 6)


### (B)

Implement evaluation for **`EPlus`** and **`ETimes`** so that they
work with both integers and fractions. The result need not be
simplified.

Sample output:

    scala> _plus(_int(3), _int(4)).eval(nullEnv)
    res6: Value = VInteger(7)
    
    scala> _plus(_frac(1, 2), _frac(1, 3)).eval(nullEnv)
    res7: Value = VFraction(5, 6)
    
    scala> _plus(_frac(1, 2), _int(1)).eval(nullEnv)
    res9: Value = VFraction(3, 2)
    
    scala> _plus(_int(1), _frac(1, 2)).eval(nullEnv)
    res10: Value = VFraction(3, 2)
    
    scala> _times(_int(3), _int(4)).eval(nullEnv)
    res11: Value = VInteger(12)
    
    scala> _times(_int(3), _frac(1, 2)).eval(nullEnv)
    res12: Value = VFraction(3, 2)
    
    scala> _times(_frac(1, 2), _int(3)).eval(nullEnv)
    res13: Value = VFraction(3, 2)
    
    scala> _times(_frac(1, 2), _frac(5, 6)).eval(nullEnv)
    res14: Value = VFraction(5, 12)


### (C)
	  
Code a function **`mkFraction`**
type **`(Int, Int) : Value`** which takes a numerator _n_
and a denoninator _d_ and creates a `Value`
representing the simplified form of fraction _n / d_ &mdash; including the case where the simplified
form of the fraction is an integer, such as _4/2_.

Sample output:

    scala> mkFraction(1, 2)
    res15: Value = VFraction(1, 2)
    
    scala> mkFraction(2, 4)
    res16: Value = VFraction(1, 2)
    
    scala> mkFraction(6, 8)
    res17: Value = VFraction(3, 4)
    
    scala> mkFraction(2, 1)
    res18: Value = VInteger(2)
    
    scala> mkFraction(4, 2)
    res19: Value = VInteger(2)
    
    scala> mkFraction(40, 30)
    res20: Value = VFraction(4, 3)


Use `mkFraction` to improve the evaluation method for `EFraction`,
`EPlus`, `ETimes`, and `EDiv` so that the results are as simplified as
possible.
	      
Sample output:

    scala> _frac(4, 2).eval(nullEnv)
    res21: Value = VInteger(2)
    
    scala> _frac(40, 30).eval(nullEnv)
    res22: Value = VFraction(4, 3)
    
    scala> _plus(_frac(1, 2), _frac(1, 2)).eval(nullEnv)
    res23: Value = VInteger(1)
    
    scala> _times(_frac(2, 4), _int(5)).eval(nullEnv)
    res24: Value = VFraction(5, 2)
    
    scala> _times(_frac(2, 4), _int(10)).eval(nullEnv)
    res25: Value = VInteger(5)
    
    scala> _div(_int(2), _int(4)).eval(nullEnv)
    res26: Value = VFraction(1, 2)
    
    scala> _div(_int(10), _int(4)).eval(nullEnv)
    res27: Value = VFraction(5, 2)
    
    scala> _div(_int(10), _int(2)).eval(nullEnv)
    res28: Value = VInteger(5)


* * * 


## Question 2: Vectors

In this question, you will implement vectors of
values. Vectors are heterogeneous, meaning that you can
have one vector containing values of various types.

Vectors are represented
as <a href="https://www.scala-lang.org/api/2.13.1/scala/collection/immutable/List.html">Scala
lists</a>. You'll want to read up on
them. This <a href="https://alvinalexander.com/scala/scala-list-class-examples">tutorial
on using Scala lists</a> might be useful.

The sample outputs below assume the following definitions:

    // empty environment
    val nullEnv = new Env(List())
    
    // useful expression constructors
    def _int (i:Int) : Exp = new EInteger(i)
    def _bool (b:Boolean) : Exp = new EBoolean(b)
    def _plus (e1:Exp, e2:Exp) : Exp = new EPlus(e1,e2)
    def _times (e1:Exp, e2:Exp) : Exp = new ETimes(e1,e2)
    def _and (e1:Exp, e2:Exp) : Exp = new EAnd(e1,e2)
    def _vector (es:List[Exp]) : Exp = new EVector(es)
	

### (A)

Code a value
class **`VVector`** which adds vectors to
our little language. A vector is represented as a list of
values of type `List[Value]`. Vectors implement a
method `getList()` which returns that list of values.

Sample output:

    scala> new VVector(List())
    res32: VVector = VVector()
    
    scala> new VVector(List(new VInteger(1)))
    res33: VVector = VVector(VInteger(1))
    
    scala> new VVector(List(new VInteger(1), new VInteger(1)))
    res34: VVector = VVector(VInteger(1), VInteger(1))
    
    scala> new VVector(List(new VInteger(1), new VInteger(2), new VInteger(3)))
    res36: VVector = VVector(VInteger(1), VInteger(2), VInteger(3))
    
    scala> new VVector(List(new VInteger(1), new VBoolean(true)))
    res37: VVector = VVector(VInteger(1), VBoolean(true))
    
    scala> new VVector(List(new VInteger(1), new VBoolean(true), new VFraction(1,2)))
    res31: VVector = VVector(VInteger(1), VBoolean(true), VFraction(1, 2))


### (B)

Code an expression class **`EVector`** which takes a list of
expressions _e<sub>1</sub>_, ..., _e<sub>n</sub>_ and evaluates to the vector
made up of the result of evaluating each of _e<sub>1</sub>_, ..., _e<sub>n</sub>_.

Sample output:

    scala> _vector(List()).eval(nullEnv)
    res38: Value = VVector()
    
    scala> _vector(List(_int(1))).eval(nullEnv)
    res39: Value = VVector(VInteger(1))
    
    scala> _vector(List(_int(1), _int(2), _int(3))).eval(nullEnv)
    res40: Value = VVector(VInteger(1), VInteger(2), VInteger(3))
    
    scala> _vector(List(_int(1), _int(2), _int(3)))
    res41: Exp = EVector(EInteger(1), EInteger(2), EInteger(3))
    
    scala> _vector(List(_int(1), _int(2), _int(3))).eval(nullEnv)
    res42: Value = VVector(VInteger(1), VInteger(2), VInteger(3))
    
    scala> _vector(List(_plus(_int(1), _int(2)), _and(_bool(true), _bool(false)))).eval(nullEnv)
    res43: Value = VVector(VInteger(3), VBoolean(false))
    
    scala> _vector(List(_vector(List(_int(1), _int(2))), _vector(List(_bool(true), _bool(false))))).eval(nullEnv)
    res44: Value = VVector(VVector(VInteger(1), VInteger(2)), VVector(VBoolean(true), VBoolean(false)))


### (C)

Extend the evaluation of **`EPlus`** so that
it adds vectors pointwise. Thus, `EPlus`
will compute vector addition when given vectors.

For full points, make sure you correctly handle vectors of vectors

**Hint:** you may want to define a function `operPlus` of type
`(Value, Value) : Value` that encapsulates the addition of two values
no matter their type, so that you can invoke it recursively.
	      
Sample output:

    scala> _plus(_vector(List()),_vector(List())).eval(nullEnv)
    res45: Value = VVector()
    
    scala> _plus(_vector(List(_int(1))),_vector(List(_int(2)))).eval(nullEnv)
    res46: Value = VVector(VInteger(3))
    
    scala> val e1 = _vector(List(_int(1),_int(2)))
    e1: Exp = EVector(EInteger(1), EInteger(2))
    
    scala> val e2 = _vector(List(_int(3),_int(4)))
    e2: Exp = EVector(EInteger(3), EInteger(4))
    
    scala> _plus(e1,e2).eval(nullEnv)
    res47: Value = VVector(VInteger(4), VInteger(6))
    
    scala> _plus(_vector(List(e1,e1)),_vector(List(e2,e2))).eval(nullEnv)
    res48: Value = VVector(VVector(VInteger(4), VInteger(6)), VVector(VInteger(4), VInteger(6)))
    
    scala> _plus(_vector(List(e1,e2)),_vector(List(e1,e2))).eval(nullEnv)
    res49: Value = VVector(VVector(VInteger(2), VInteger(4)), VVector(VInteger(6), VInteger(8)))

Brownie points if you can also handle vectors of fractions.


### (D) 

Extend the evaluation of **`ETimes`** so that
if given expressions evaluating to vectors of
integers as arguments, it computes the [inner product](http://mathworld.wolfram.com/DotProduct.html)
of those vectors.
	      
Sample output:

    scala> _times(_vector(List()), _vector(List())).eval(nullEnv)
    res51: Value = VInteger(0)
    
    scala> _times(_vector(List(_int(2))), _vector(List(_int(100)))).eval(nullEnv)
    res52: Value = VInteger(200)
    
    scala> val e1 = _vector(List(_int(2), _int(3)))
    e1: Exp = EVector(EInteger(2),  EInteger(3))
    
    scala> val e2 = _vector(List(_int(33), _int(66)))
    e2: Exp = EVector(EInteger(33), EInteger(66))
    
    scala> _times(e1, e2).eval(nullEnv)
    res53: Value = VInteger(264)
    
    scala> _times(e1, _plus(e2, e2)).eval(nullEnv)
    res54: Value = VInteger(528)

Brownie points if you can also handle vectors of fractions.


### (E)

Extend the evaluation of **`EPlus`** and **`ETimes`** so that when given expressions evaluating to
a scalar (i.e., not a vector) and a vector of integers  
order, the corresponding operation (addition or multiplication) is
applied to the scalar and every element of the vector, yielding a
vector of the results.

Thus, for example, 2 + &lt; 10, 20, 30 &gt; should yield the vector &lt; 12, 22, 32 &gt; and 2 * &lt; 10, 20, 30 &gt; should yield the vector &lt; 20, 40, 60 &gt;.

Sample output:

    scala> _plus(_int(2), _vector(List())).eval(nullEnv)
    res55: Value = VVector()
    
    scala> _plus(_int(2), _vector(List(_int(1)))).eval(nullEnv)
    res56: Value = VVector(VInteger(3))
    
    scala> _plus(_int(2), _vector(List(_int(1), _int(2)))).eval(nullEnv)
    res57: Value = VVector(VInteger(3), VInteger(4))
    
    scala> _plus(_int(2), _vector(List(_int(1), _int(2), _int(3)))).eval(nullEnv)
    res59: Value = VVector(VInteger(3), VInteger(4), VInteger(5))
    
    scala> _times(_int(2), _vector(List())).eval(nullEnv)
    res60: Value = VVector()
    
    scala> _times(_int(2), _vector(List(_int(1)))).eval(nullEnv)
    res61: Value = VVector(VInteger(2))
    
    scala> _times(_int(2), _vector(List(_int(1), _int(2)))).eval(nullEnv)
    res62: Value = VVector(VInteger(2), VInteger(4))
    
    scala> _times(_int(2), _vector(List(_int(1), _int(2), _int(3)))).eval(nullEnv)
    res63: Value = VVector(VInteger(2), VInteger(4), VInteger(6))

Brownie points if you can also handle fractions.


* * * 


## Question 3: Simultaneous Bindings

The sample outputs below assume the following definitions:

    // empty environment
    val nullEnv = new Env(List())
    
    // useful expression constructors
    def _int (i:Int) : Exp = new EInteger(i)
    def _plus (e1:Exp, e2:Exp) : Exp = new EPlus(e1, e2)
    def _times (e1:Exp, e2:Exp) : Exp = new ETimes(e1, e2)
    def _id (i:String) : Exp = new EId(i)
    def _let (i:String, e1:Exp, e2:Exp) : Exp = new ELet(i, e1, e2)
    def _letsim (b:List[(String, Exp)], e:Exp) : Exp = new ELetSim(b, e)
    def _letseq (b:List[(String, Exp)], e:Exp) : Exp = new ELetSeq(b, e)

	
### (A)


Code an alternate to `ELet` called **`ELetSim`** that can
handle multiple _simultaneous bindings_ instead of a
single one. Using a similar sample surface syntax that I used in class,

    let (x_1 = e_1, ..., x_n = e_n)
      e
      
first evaluates all the `e_1`, ..., `e_n` down to values and _then_
binds them to the identifiers before evaluating `e`. Thus, if any of
the `e_1`, ..., `e_n` refer any of the identifiers `x_1`, ..., `x_n`,
the values used for those identifiers come from the
environment at the _beginning_ of evaluation.  

For instance, we could write

    let (x = 10, y = 20, z = 30)
      (x+y)*z

which should evaluate to 900, and

    let (a = 5, b = 20)
      let (a = b, b = a)
        a + b

which should evaluate to 25. This last expression is different from

    let (a = 5)
      let (b = 20)
       let (a = b)
          let (b = a)
            a + b

(Why?) In particular, this last one evaluates to 40.

Expression `ELetSim`  take two arguments:

- A list of pairs representing the bindings, each pair of the
  form `(id,exp)` where `id` is a string
  representing the name of an identifier being bound,
  and `exp` the expression that identifier is bound to

- The expression to evaluate in the context of those bindings,
  and that will be the result of evaluating the `ELetSim`
  expression as a whole.

The two examples above would be written:

    new ELetSim(List(("x", new ELiteral(new VInteger(10))),
                     ("y", new ELiteral(new VInteger(20))),
                     ("z", new ELiteral(new VInteger(30)))),
                new ETimes(new EPlus(new EId("x"), new EId("y")),
                           new EId("z")))

and

    new ELetSim(List(("a", new ELiteral(new VInteger(5))),
                     ("b", new ELiteral(new VInteger(20)))),
                new ELetSim(List(("a", new EId("b")),
                                 ("b", new EId("a"))),
                            new EPlus(new EId("a"), new EId("b"))))

	    
Sample output:

    scala> _letsim(List(("a", _int(99))), _id("a")).eval(nullEnv)
    res64: Value = VInteger(99)
    
    scala> _letsim(List(("a", _int(99)), ("b", _int(66))), _id("a")).eval(nullEnv)
    res65: Value = VInteger(99)
    
    scala> _letsim(List(("a", _int(99)), ("b", _int(66))), _id("b")).eval(nullEnv)
    res66: Value = VInteger(66)
    
    scala> _letsim(List(("a", _int(99))), _letsim(List(("a", _int(66)), ("b", _id("a"))), _id("a"))).eval(nullEnv)
    res67: Value = VInteger(66)
    
    scala> _letsim(List(("a", _int(99))), _letsim(List(("a", _int(66)), ("b", _id("a"))), _id("b"))).eval(nullEnv)
    res68: Value = VInteger(99)
    
    scala> _letsim(List(("a", _int(5)), ("b", _int(20))), _letsim(List(("a", _id("b")), ("b", _id("a"))), _plus(_id("a"), _id("b")))).eval(nullEnv)
    res69: Value = VInteger(25)



### (B)


Another common local binding special form is sequential bindings,
which we will write (in our sample surface syntax) `let*`.
Intuitively, it lets you create simultaneous bindings just like in
(A), except that instead of all the expressions being interpreted "at
the same time", they are interpreted one after the other, in an
environment containing all previous bindings. Thus, to evaluate 

    let* (x_1 = e_1, ..., x_n = e_n)
      e
      
first evaluate `e_1`, binding it to `x_1`; then evaluate `e_2` in an environment containing that binding for `x_1, and binding the result to `x_2`; then evaluate `e_3` in an environment containing the binding for `x_1` and `x_2` and binding the result to `x_3`, etc. Finally, evaluate `e` in an environment containing all the bindings for `x_1`, ..., `x_n`.  

In other words, 

    let* (x_1 = e_1, ..., x_n = e_n)
      e
      
should evaluate to the same result as 

    let (x_1 = e_1)
      let (x_2 = e_2)
        ...
          let (x_n = e_n)
            e

For instance, again using surface syntax similar to
what we used in class, we could write

    let* (x = 10, y = 20, z = 30)
      (x+y)*z

which would evaluate the same as if we had written

    let (x = 10)
      let (y = 20)
        let (z = 30)
          (x+y)*z

which of course still evaluates to 900. The expression

    let (a = 5)
      let (b = 20)
        let* (a = b, b = a)
          a + b

would evaluate to 40, since it should be equivalent to writing

    let (a = 5)
      let (b = 20)
        let (a = b)
          let (b = a)
            a + b

Code an expression class `ELetSeq` that takes
two arguments, just like `ELetSim` from
last question:

- A list of pairs representing the bindings, each pair of the
  form `(id,exp)` where `id` is a string
  representing the name of an identifier being bound,
  and `exp` the expression that identifier is bound
  to
    
- The expression to evaluate in the context of those bindings,
  and that will be the result of evaluating the `ELetSeq`
  expression as a whole


Sample output:

    scala> _letseq(List(("a",_int(99))),_id("a")).eval(nullEnv)
    res70: Value = VInteger(99)
    
    scala> _letseq(List(("a",_int(99)),("b",_int(66))),_id("a")).eval(nullEnv)
    res71: Value = VInteger(99)
    
    scala> _letseq(List(("a",_int(99)),("b",_int(66))),_id("b")).eval(nullEnv)
    res72: Value = VInteger(66)
    
    scala> _letseq(List(("a",_int(99))),_letseq(List(("a",_int(66)),("b",_id("a"))),_id("a"))).eval(nullEnv)
    res73: Value = VInteger(66)
    
    scala> _letseq(List(("a",_int(99))),_letseq(List(("a",_int(66)),("b",_id("a"))),_id("b"))).eval(nullEnv)
    res74: Value = VInteger(66)
    
    scala> _letseq(List(("a",_int(5)),("b",_int(20))),_letseq(List(("a",_id("b")),("b",_id("a"))),_plus(_id("a"),_id("b")))).eval(nullEnv)
    res75: Value = VInteger(40)
