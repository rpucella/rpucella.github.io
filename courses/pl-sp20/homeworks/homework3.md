# Homework 3

## Due Date: Tuesday, February 25, 2020 (23h59)

- This homework is to be done in teams of 2, or singly. You may
discuss problems with fellow students, but all submitted work must be
entirely your team's own, and should not be from any other course,
present, past, or future. If you use a solution from another source
you must cite it &mdash; this includes when that source is someone
else helping you.

- **Please do not post your solutions on a
public website or a public repository like GitHub.**

- All programming is to be done in Scala 2.13. 

- Code your answers by modifying the file [`homework3.scala`](homework3.scala) provided. Add your team members' **name**, **email address**, and any **remarks** that you
wish to make to the instructor to the block comment at the top of the file.

- **Please do not change the types in the signature of the
method stubs I provide**. Doing so will make it
impossible to load your code into the testing infrastructure,
which tends to make me unhappy.

- Feel free to define helper functions if you need them.


## Electronic Submission Instructions

- Run your homework code via `scala homework3.scala`

- If there are any error, do not submit. I can't test what will not load correctly.

- When you're ready to submit, send an email with your file `homework3.scala` as an attachment to `olin.submissions@gmail.com` with subject _Homework 3 submission_.


* * *

Look over the sample code to see what is offered. Basic values are
integers, booleans, and vectors. Expressions include integer literals
`EInteger`, Boolean literals `EBoolean`, vector expressions `EVector`,
conditional expressions `EIf`, and binding expressions
`Elet`. Operations include addition `EPlus`, multiplication `ETimes`,
equality checking `EEqual` (evaluating to a Boolean value), and
conjunction `EAnd`.

The code also includes a parser written using the [Scala
Standard Parser Combinator Library](https://github.com/scala/scala-parser-combinators) which implements the following
grammar:

<pre>
     <i>atomic</i> ::= <span style="color: blue;"><i>integer</i></span>
                <span style="color: blue;"><i>identifier</i></span>
                <span style="color: blue;">true</span>
                <span style="color: blue;">false</span>
                 
     <i>expr</i> ::= <i>atomic</i>
              <span style="color: blue;">(</span> <span style="color: blue;">+</span> <i>expr</i> <i>expr</i> <span style="color: blue;">)</span>
              <span style="color: blue;">(</span> <span style="color: blue;">*</span> <i>expr</i> <i>expr</i> <span style="color: blue;">)</span>
              <span style="color: blue;">(</span> <span style="color: blue;">if</span> <i>expr</i> <i>expr</i> <i>expr</i> <span style="color: blue;">)</span>
              <span style="color: blue;">(</span> <span style="color: blue;">let</span> <span style="color: blue;">(</span> <span style="color: blue;">(</span> <i>identifier</i> <i>expr</i> <span style="color: blue;">)</span> <span style="color: blue;">)</span> <i>expr</i> <span style="color: blue;">)</span>
</pre>

Tokens are in blue and stand for themselves, expect for token `integer` which represents any
sequence of digits (including a leading negation sign) and token `identfier`
which represents any sequence of alphanumeric characters starting with an
alphabetical character.

The parser is implemented by class `SExpParser`, and can be invoked by
calling the top-level function `parse` included in the code.  It takes
a string and returns an `Exp`, that is, a piece of source of code in
the abstract representation of our language.

Note that just like for the demo code for Lecture 4, using the
Standard Parser Combinator library requires you to have loaded the
library's `jar` file in your shell if you run your code in the
shell. The easiest way is probably to start the shell with:

    scala -classpath scala-parser-combinators_2.13-1.1.2.jar
    
assuming the `jar` file is in the same folder where you start your
shell.  (If not, you'll need to provide a path to the `jar` file.)
Alternatively, you can also use the command

    :require scala-parser-combinators_2.13-1.1.2.jar
    
from within the shell. Again, you may need to provide a path to the `jar` file.

You can find the `jar` file at the link above, and [I also
posted the version for Scala 2.13 here for
convenience](../scala-parser-combinators_2.13-1.1.2.jar).

    
* * * 


## Question 1 : Functions with Multiple Parameters


In class, we saw an implementation of one-argument functions by
introducing a `VClosure1` value representing a one-argument function
which records the environment in which it was created (this is what
makes it a closure) as well as expression `ELetFun` that evaluates an
expression to a closure and binds it to a name in the environment, and
`EApply1` that applies a closure in the environment to an argument.

We now replicate that work, but with multiple arguments.

Moreover, instead of using an expression such as `ELetFun` to create
functions, we simply use an expression `EFunction` that evaluates to a
closure. If we want to bind it to a name, we can simply use
`ELet`. This means that we can generalize `EApply` to take an
expression in its function position, as long as the expression evaluates to a closure. 


### (A)

Code a new kind of value **`VClosure`** to represent a multiargument
function taking three arguments: a list of strings representing the
parameters that will be bound to values when the function is called,
an expression representing the body of the function (which of course
can refer to the parameters), and an environment that represents the
environment existing at the moment when the function was created.

`VClosure` should implement a method `apply` that
takes a list of values (the values to which the function is applied)
and evaluate the body in the stored environment extended with a
binding of the parameters to the appropriate values. 

It's an error to apply a function with too few or too many arguments.

For example, `VClosure(List("a", "b"), EId("b"), new Env())` should
produce a function of two arguments that returns its second argument.

    scala> val v1 = new VClosure(List("a", "b"), new EId("b"), new Env())
    v1: VClosure = VClosure[[a, b], EId[b], {})
    
    scala> v1.apply(List(new VInteger(10), new VInteger(20)))
    res0: Value = VInteger[20]
    
    scala> v1.apply(List(new VInteger(100), new VInteger(200)))
    res1: Value = VInteger[200]
    
    scala> val v2 = new VClosure(List("x", "y", "z"), 
                                 new EIf(new EId("z"), new EId("x"), new EId("y")), 
                                 new Env())
    v2: VClosure = VClosure[[x, y, z], EIf[EId[z], EId[x], EId[y]], {})
    
    scala> v2.apply(List(new VInteger(10), new VInteger(20), new VBoolean(true)))
    res2: Value = VInteger[10]
    
    scala> v2.apply(List(new VInteger(10), new VInteger(20), new VBoolean(false)))
    res3: Value = VInteger[20]
    
    scala> val v3 = new VClosure(List("a"), new EId("z"), new Env(List(("z", new VInteger(99)))))
    v3: VClosure = VClosure[[a], EId[z], {z <- VInteger[99]})
    
    scala> v3.apply(List(new VInteger(10)))
    res4: Value = VInteger[99]


### (B)

Code a new kind of expression **`EFunction`** that takes a list of
strings representing parameters and an expression representing a body,
and evaluates to a closure representing the function and capturing the
environment in which `EFunction` is evaluated.

For example, `EFunction(List("a","b"), EId("b"))` should evaluate to
the sample closure in the previous question when evaluated in the
empty environment.

    scala> val e1 = new EFunction(List("a", "b"), new EId("b"))
    e1: EFunction = EFunction[[a, b], EId[b]]
    
    scala> e1.eval(new Env())
    res5: Value = VClosure[[a, b], EId[b], {})
    
    scala> e1.eval(new Env()).apply(List(new VInteger(10), new VInteger(20)))
    res6: Value = VInteger[20]
    
    scala> e1.eval(new Env()).apply(List(new VInteger(100), new VInteger(200)))
    res7: Value = VInteger[200]
    
    scala> val e2 = new EFunction(List("x", "y", "z"), new EIf(new EId("z"), new EId("x"), new EId("y")))
    e2: EFunction = EFunction[[x, y, z], EIf[EId[z], EId[x], EId[y]]]
    
    scala> e2.eval(new Env())
    res8: Value = VClosure[[x, y, z], EIf[EId[z], EId[x], EId[y]], {})
    
    scala> e2.eval(new Env()).apply(List(new VInteger(10), new VInteger(20), new VBoolean(true)))
    res9: Value = VInteger[10]
    
    scala> e2.eval(new Env()).apply(List(new VInteger(10), new VInteger(20), new VBoolean(false)))
    res10: Value = VInteger[20]
    
    scala> val e3 = new ELet("z", new EInteger(99), new EFunction(List("a"), new EId("z")))
    e3: ELet = ELet[z, EInteger[99], EFunction[[a], EId[z]]]
    
    scala> e3.eval(new Env())
    res11: Value = VClosure[[a], EId[z], {z <- VInteger[99]})
    
    scala> e3.eval(new Env()).apply(List(new VInteger(10)))
    res12: Value = VInteger[99]


### (C)

Code a new kind of expression **`EApply`** that takes an expression
(which should evaluate to a closure) and a sequence of expressions
(which should evaluate to the arguments of the function) and applies
the function to the arguments.

    scala> val e1 = new EFunction(List("a", "b"), new EId("b"))
    e1: EFunction = EFunction[[a, b], EId[b]]
    
    scala> new EApply(e1, List(new EInteger(10), new EInteger(20)))
    res13: EApply = EApply[EFunction[[a, b], EId[b]], [EInteger[10], EInteger[20]]]
    
    scala> new EApply(e1, List(new EInteger(10), new EInteger(20))).eval(new Env())
    res14: Value = VInteger[20]
    
    scala> new EApply(e1, List(new EInteger(100), new EInteger(200))).eval(new Env())
    res15: Value = VInteger[200]
    
    scala> val e2 = new EFunction(List("x", "y", "z"), new EIf(new EId("z"), new EId("x"), new EId("y")))
    e2: EFunction = EFunction[[x, y, z], EIf[EId[z], EId[x], EId[y]]]
    
    scala> new EApply(e2, List(new EInteger(10), new EInteger(20), new EBoolean(true)))
    res16: EApply = EApply[EFunction[[x, y, z], EIf[EId[z], EId[x], EId[y]]], [EInteger[10], EInteger[20], EBoolean[true]]]
    
    scala> new EApply(e2, List(new EInteger(10), new EInteger(20), new EBoolean(true))).eval(new Env())
    res18: Value = VInteger[10]
    
    scala> new EApply(e2, List(new EInteger(10), new EInteger(20), new EBoolean(false))).eval(new Env())
    res19: Value = VInteger[20]
    
    scala> val e3 = new ELet("z", new EInteger(99), new EFunction(List("a"), new EId("z")))
    e3: ELet = ELet[z, EInteger[99], EFunction[[a], EId[z]]]
    
    scala> new EApply(e3, List(new EInteger(10)))
    res20: EApply = EApply[ELet[z, EInteger[99], EFunction[[a], EId[z]]], [EInteger[10]]]
    
    scala> new EApply(e3, List(new EInteger(10))).eval(new Env())
    res21: Value = VInteger[99]



## Question 2: Recursive Functions


The one-argument functions we implemented in class and the
multi-argument functions you implemented in Question 1 have a serious
limitation: they don't support recursion. We cannot write a recursive
function, say, to sum all natural numbers up to a, something that
we could write in Scala as:

      def sum (a:Int) : Int =  {
        if (a == 0) {
          return 0
        } else {
          return a + sum(a - 1)
        }
      }

If we try to write the closest equivalent to `sum` and applying it to
sum the natural numbers up to 10 in our object language directly using the abstract representation 

    ELet[sum, EFunction[[a], EIf[EEqual[EId[a], EInteger[0]],
                                 EInteger[0], 
                                 EPlus[EId[a], EApply[EId[sum], [EPlus[EId[a], EInteger[-1]]]]]]], 
         EApply[EId[sum], [EInteger[10]]]]

or using the S-expressions surface syntax introduced in class and extended in Question 3:

    (let ((sum (fun (a) 
                  (if (= a 0) 
                    0 
                    (+ a (sum (+ a -1)))))))
      (sum 10))

then it faikls miserable. Why?

There are a few ways of implementing recursive functions. One of the simplest
is to add a notion of a recursive closure. A recursive closure is like
a normal closure -- parameters, a body, and an environment
-- except that it has an additional parameter that can be used in
the body of the function, and that gets bound **to the function
itself** when the function is called. We can call this the _self_ parameter. Using the self parameter, we can
refer to the function inside its own body, which is of course what
recursion is all about.


### (A)

Define a new kind of value **`VRecursiveClosure`** to represent a
_recursive_ multiargument function, and taking four arguments: a
string (the self parameter) representing the name to which the
recursive function itself should be bound when it is applied to arguments,
and then the same three arguments as a regular closure (the list of
parameters, the body, the creation-time environment). 

Like a regular closure, `VRecursiveClosure` should implement a method
`apply` that takes the list of values to which we want to apply the function
and evaluate the body in the stored environment extended
with a binding of parameters with the appropriate values _as well as a
binding of the self parameter to the recursive closure itself_. 

It's an error to call a function with too few or too many arguments.

    scala> val v1 = new VRecursiveClosure("self", List("a", "b"), new EId("self"), new Env())
    v1: VRecursiveClosure = VRecursiveClosure[self, [a, b], EId[self], {})
    
    scala> v1.apply(List(new VInteger(10), new VInteger(20)))
    res28: Value = VRecursiveClosure[self, [a, b], EId[self], {})
    
    scala> val v2 = new VRecursiveClosure("self", List("a"), 
                             new EIf(new EEqual(new EId("a"), new EInteger(0)), 
                                     new EInteger(0), 
                                     new EPlus(new EId("a"), new EApply(new EId("self"), 
                                                                        List(new EPlus(new EId("a"), new EInteger(-1)))))), 
                             new Env())
    v2: VRecursiveClosure = VRecursiveClosure[self, [a], EIf[EEqual[EId[a], EInteger[0]], EInteger[0], EPlus[EId[a], EApply[EId[self], [EPlus[EId[a], EInteger[-1]]]]]], {})
    
    scala> v2.apply(List(new VInteger(10)))
    res29: Value = VInteger[55]


### (B)

Code an expression **`ERecursiveFunction`** that takes a string representing
the self parameter, a list of strings representing the parameters of
the function, and an expression representing the body, and evaluates
to a recursive closure representing the function and capturing the environment in which `ERecursiveFunction` is evaluated. 

    scala> val e1 = new ERecursiveFunction("self", List("a", "b"), new EId("self"))
    e1: ERecursiveFunction = EFunction[self, [a, b], EId[self]]
    
    scala> e1.eval(new Env())
    res30: Value = VRecursiveClosure[self, [a, b], EId[self], {})
    
    scala> e1.eval(new Env()).apply(List(new VInteger(10), new VInteger(20)))
    res31: Value = VRecursiveClosure[self, [a, b], EId[self], {})
    
    scala> val e2 = new ERecursiveFunction("self", List("a"), 
                                 new EIf(new EEqual(new EId("a"), new EInteger(0)), 
                                         new EInteger(0), 
                                         new EPlus(new EId("a"), new EApply(new EId("self"), 
                                                                            List(new EPlus(new EId("a"), new EInteger(-1)))))))
    e2: ERecursiveFunction = EFunction[self, [a], EIf[EEqual[EId[a], EInteger[0]], EInteger[0], EPlus[EId[a], EApply[EId[self], [EPlus[EId[a], EInteger[-1]]]]]]]
    
    scala> e2.eval(new Env())
    res33: Value = VRecursiveClosure[self, [a], EIf[EEqual[EId[a], EInteger[0]], EInteger[0], EPlus[EId[a], EApply[EId[self], [EPlus[EId[a], EInteger[-1]]]]]], {})
    
    scala> e2.eval(new Env()).apply(List(new VInteger(10)))
    res34: Value = VInteger[55]


## Question 3: Parsing

This question requires you to dig a bit into the documentation of the
[Scala Standard Parser Combinator
Library](https://github.com/scala/scala-parser-combinators). You may
want to read the [Getting
Started](https://github.com/scala/scala-parser-combinators/blob/1.2.x/docs/Getting_Started.md)
section the library, which gives you the very basic concepts.

Unlike Questions 1 and 2, the parts of this questions are
independent of each other. But note that part (C) requires you to have
completed Questions 1 and 2 above.

I have already created all the tokens you need in `SExpParser`. I've
also put in a skeleton parser for each new rule that you need to add,
although those skeleton parser simply fail to parse (via the special
parser `failure` of the parsing library).


### (A)

Extend the parser so that it can parse the following
addition to the grammar above:

<pre>
      <i>expr</i> ::= ...
               <span style="color: blue;">(</span> <span style="color: blue;">and</span>  <i>expr</i> <i>expr</i> <span style="color: blue;">)</span>
</pre>

(Modify rule `expr_and` in the parser.)

An expression of the form `(and e1 e2)` should produce an abstract
representation `new EAnd(t1, t2)`,
where `t1` is the abstract representation of `e1` and `t2` is the
abstract representation of `e2`.
	    
    scala> parse(" (and true false) ")
    res35: Exp = EAnd[EBoolean[true], EBoolean[false]]
    
    scala> parse(" (and true (and true false)) ")
    res36: Exp = EAnd[EBoolean[true], EAnd[EBoolean[true], EBoolean[false]]]
    
    scala> parse(" (if (and true false) 1 2) ")
    res37: Exp = EIf[EAnd[EBoolean[true], EBoolean[false]], EInteger[1], EInteger[2]]
    
    scala> parse(" (if (and true false) 1 2) ").eval(new Env())
    res38: Value = VInteger[2]
    

### (B)

Extend the parser so that it can parse the following
addition to the grammar above:

<pre>
      <i>expr</i> ::= ...
               <span style="color: blue;">(</span> <span style="color: blue;">vector</span> <i>exprs</i> <span style="color: blue;">)</span>
    
      <i>exprs</i> ::= <i>expr</i> <i>exprs</i>
                <i>expr</i>
</pre>

(Modify rules `expr_vector` and `exprs` in the parser.)

An expression of the form `(vector e1 ...)` should produce an
abstract representation `new EVector(List(t1, ...))`, where `t1`
is the abstract representation of `e1`, and so on.

Note that the parser for _`exprs`_ should be of type
`Parser[List[Exp]]`, since it parses a list of expressions.
	    
    scala> parse("  (vector 1) ")
    res39: Exp = EVector[EInteger[1]]
    
    scala> parse("  (vector 1 2) ")
    res40: Exp = EVector[EInteger[1], EInteger[2]]
    
    scala> parse("  (vector 1 2 3) ")
    res41: Exp = EVector[EInteger[1], EInteger[2], EInteger[3]]
    
    scala> parse("  (vector 1 2 3 (+ 4 5)) ")
    res42: Exp = EVector[EInteger[1], EInteger[2], EInteger[3], EPlus[EInteger[4], EInteger[5]]]
    
    scala> parse("  (+ (vector 1 2) (vector (+ 3 4) 5))  ")
    res43: Exp = EPlus[EVector[EInteger[1], EInteger[2]], EVector[EPlus[EInteger[3], EInteger[4]], EInteger[5]]]
    
    scala> parse("  (+ (vector 1 2) (vector (+ 3 4) 5))  ").eval(new Env())
    res44: Value = VVector[VInteger[8], VInteger[7]]



### (C)

Extend the parser so that it can parse the functions, recursive functions, and application from Questions 1 and 2. More specifically, extend the parser to handle the following addition to the grammar above:

<pre>
      <i>expr</i> ::= ...
               <span style="color: blue;">(</span> <span style="color: blue;">fun</span> <span style="color: blue;">(</span> <i>params</i> <span style="color: blue;">)</span> <i>expr</i> <span style="color: blue;">)</span>
               <span style="color: blue;">(</span> <span style="color: blue;">fun</span> <span style="color: blue;"><i>identifier</i></span> <span style="color: blue;">(</span> <i>params</i> <span style="color: blue;">)</span> <i>expr</i> <span style="color: blue;">)</span>
               <span style="color: blue;">(</span> <i>expr</i> <i>exprs</i> <span style="color: blue;">)</span>
    
      <i>params</i> ::= <span style="color: blue;"><i>identifier</i></span> <i>params</i>
                 <span style="color: blue;"><i>identifier</i></span>
</pre>

where _`exprs`_ is in common with part (B).

(Modify rules `expr_fun`, `expr_rec_fun`, `params`, and `expr_apply` in the parser.)

An expression of the form `(fun (a ...) e)` should produce an abstract
representation `new EFunction(List("a", ...), t)` where `t` is the
abstraction representation of `e`.

An expression of the form `(fun self (a ...) e)` should produce an abstract
representation `new ERecursiveFunction("self", List("a", ...), t)` where `t` is the
abstraction representation of `e`.

An expression of the form `(e e1 ...)` should produce an abstract
representation `new EApply(t, List(t1, ...))` where `t` is the
abstract representation of `e`, `t1` is the abstraction representation
of `e1`, and so on.

    scala> parse("  (fun (a b) b)  ")
    res45: Exp = EFunction[[a, b], EId[b]]
    
    scala> parse("  (fun (x y z) (if z x y))  ")
    res46: Exp = EFunction[[x, y, z], EIf[EId[z], EId[x], EId[y]]]
    
    scala> parse("  (let ((square (fun (x) (* x x)))) (square 9))  ")
    res47: Exp = ELet[square, EFunction[[x], ETimes[EId[x], EId[x]]], EApply[EId[square], [EInteger[9]]]]
    
    scala> parse("  (let ((square (fun (x) (* x x)))) (square 9))  ").eval(new Env())
    res48: Value = VInteger[81]
    
    scala> parse("  ((fun (a b) (+ a b)) 10 20)  ")
    res49: Exp = EApply[EFunction[[a, b], EPlus[EId[a], EId[b]]], [EInteger[10], EInteger[20]]]
    
    scala> parse("  ((fun (a b) (+ a b)) 10 20)  ").eval(new Env())
    res50: Value = VInteger[30]
    
    scala> parse("  (fun s (a b) s)  ")
    res51: Exp = EFunction[s, [a, b], EId[s]]
    
    scala> parse("  (fun s (a b) s)  ").eval(new Env())
    res52: Value = VRecursiveClosure[s, [a, b], EId[s], {})

    scala> parse("  (let ((sum (fun s (a) (if (= a 0) 0 (+ a (s (+ a -1))))))) (sum 100))  ")
    res53: Exp = ELet[sum, EFunction[s, [a], EIf[EEqual[EId[a], EInteger[0]], EInteger[0], EPlus[EId[a], EApply[EId[s], [EPlus[EId[a], EInteger[-1]]]]]]], EApply[EId[sum], [EInteger[100]]]]

    scala> parse("  (let ((sum (fun s (a) (if (= a 0) 0 (+ a (s (+ a -1))))))) (sum 100))  ").eval(new Env())
    res54: Value = VInteger[5050]


### (D) 

As it stands, our expression language only supports giving exactly two
arguments to `+` and `*`. It's easy enough to modify the abstract
representation for `EPlus` and `ETimes` to support more than two
arguments. But there's another approach we can use.

Intuitively, we can express addition with more than two arguments as a
nested sequence of additions with only two arguments. For instance,
`(+ 1 2 3)` should be equivalent to `(+ (+ 1 2) 3)`, `(+ 1 2 3 4)` should be
equivalent to `(+ (+ (+ 1 2) 3) 4)`, and so on. Therefore, one way to
support addition with more than two arguments is to parse it into an
abstract representation that uses nested additions with two arguments.

Extend the parser so that it can parse expressions of the form `(+ e1 e2 e3 e4 ...)`
and for those returns the same
abstract representation as if we had written `(+ (+ (+ (+ e1 e2) e3) e4) ...)` or
something equivalent.

Do the same for `*`.

(Modify rules `expr_plus_multi` and `expr_times_multi` in the parser.)

    scala> parse("  (+ 1 2 3)  ")
    res59: Exp = EPlus[EPlus[EInteger[1], EInteger[2]], EInteger[3]]
    
    scala> parse("  (+ 1 2 3 4)  ")
    res60: Exp = EPlus[EPlus[EPlus[EInteger[1], EInteger[2]], EInteger[3]], EInteger[4]]
    
    scala> parse("  (+ 1 2 3 4 5)  ")
    res61: Exp = EPlus[EPlus[EPlus[EPlus[EInteger[1], EInteger[2]], EInteger[3]], EInteger[4]], EInteger[5]]
    
    scala> parse("  (+ 1 2 3 4 5)  ").eval(new Env())
    res62: Value = VInteger[15]
    
    scala> parse("  (* 1 2 3 4 5)  ")
    res63: Exp = ETimes[ETimes[ETimes[ETimes[EInteger[1], EInteger[2]], EInteger[3]], EInteger[4]], EInteger[5]]
