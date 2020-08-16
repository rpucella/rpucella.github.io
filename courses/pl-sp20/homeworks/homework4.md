# Homework 4

## Due Date: Friday, April 17, 2020 (23h59)

- This homework is to be done in teams of 2, or singly. You may
discuss problems with fellow students, but all submitted work must be
entirely your team's own, and should not be from any other course,
present, past, or future. If you use a solution from another source
you must cite it &mdash; this includes when that source is someone
else helping you.

- **Please do not post your solutions on a
public website or a public repository like GitHub.**

- All programming is to be done in Scala 2.13. 

- Code your answers by modifying the file [`homework4.scala`](homework4.scala) provided. Add your team members' **name**, **email address**, and any **remarks** that you
wish to make to the instructor to the block comment at the top of the file.

- **Please do not change the types in the signature of the
method stubs I provide**. Doing so will make it
impossible to load your code into the testing infrastructure,
which tends to make me unhappy.

- Feel free to define helper functions if you need them.


## Electronic Submission Instructions

- Run your homework code via `scala -classpath scala-parser-combinators_2.13-1.1.2.jar homework4.scala` (assuming you have the parser combinators library in the same directory -- see below).

- If there are any error, do not submit. I can't test what will not load correctly.

- When you're ready to submit, send an email with your file `homework4.scala` as an attachment to `olin.submissions@gmail.com` with subject _Homework 4 submission_.


* * *

## Updates

- 3/27/20: Added Question 5


* * *

Included with this homework is sample code that defines the most
recent version of a language we can call FUNC, with integers,
fractions, vectors, first-class functions, and reference cells, with the following `Value` subclasses:

    class VInteger (val i:Int) extends Value
    class VBoolean (val b:Boolean) extends Value
    class VFraction (val numerator:Int, val denominator:Int) extends Value
    class VVector (val l : List[Value]) extends Value
    class VRefCell (val init : Value) extends Value
    class VPrimitive (val oper : (List[Value]) => Value) extends Value
    class VRecClosure (val self: String, val params: List[String], val body:Exp, val env:Env) extends Value

It is all as we have seen before. A `VPrimitive` primitive operation
value is simply a wrapper around an operation that takes a list of
`Value`s and returns a `Value`. All the built-in functions in the
initial environment are implemented as primitives.

Some things to note when you look at the code: 

- `Value` subclasses have
a normal `toString` method to show a value in the form `VInteger[10]`
(for example). 

- Additionally, there is a method `toDisplay` that shows
the value in a more user-friendly way, such as `10`. The shell uses
`toDisplay` to show the result of evaluation. 

- Also, take a look at
`VInteger` and `VFraction` to see how I treat working with fractions
and integers: an integer can be treated as a fraction (because it
defines the `getNumerator` and `getDenominator` methods), and when you
create a fraction, it gets simplified and even converted to `VInteger`
if needed. That leads to a particularly simple implementation of `+`,
`*`, `-`, and `/`: just treat the arguments as fractions, and create a
resulting fraction, If you happen to use integers, it works
transparently and returns an integer if appropriate.

Expressions are defined via the following `Exp` subclasses:

    class EInteger (val i:Int) extends Exp
    class EBoolean (val b:Boolean) extends Exp
    class EPrimitive (val oper : (List[Value]) => Value) extends Exp 
    class EIf (val ec : Exp, val et : Exp, val ee : Exp) extends Exp
    class EId (val id : String) extends Exp
    class EApply (val fn : Exp, val args : List[Exp]) extends Exp
    class EFunction (val recName : String, val params : List[String], val body : Exp) extends Exp
    class ELet (val bindings : List[(String, Exp)], val body : Exp) extends Exp

It's all pretty much as we've seen before. Note that `EFunction` and
`EApply` take multiple arguments, so you can see how I implemented
what I asked you to do in Homework 3. `EFunction` can be recursive.

One difference that may appear weird is that I added an expression
`EPrimitive` that simply evaluates to the corresponding
`VPrimitive`. This is useful when writing parser transformations that
needs to use a primitive operation in the result of the
transformation: you can simply add an appropriate `new
EPrimitive(oper)` where you need it. (It's unsafe to rely on the name
of the operation in the initial environment, because it may be
shadowed by another binding of the same name.)

Note that `Exp` subclasses include not only a `toString` method to
return a representation of the given expression, but also a method
`pp` (for pretty-print) that prints a nicely indented form of the
expression. The pretty-printing code is slightly twisted, so take a
look. Pretty-printing is a generally difficult problem, and people
have developed [interesting algorithms](http://belle.sourceforge.net/doc/hughes95design.pdf) for it. **When writing new
expressions classes, you do not have to implement method `pp` -- by
default, it will use `toString()` which isn't as pretty, but is quite
workable. So your sample outputs may look slightly different from
mine.**

The code includes a parser written using the [Scala
Standard Parser Combinator Library](https://github.com/scala/scala-parser-combinators) which implements the following
grammar:

<pre>
     <i>atomic</i> ::= <span style="color: blue;"><i>integer</i></span>
                <span style="color: blue;"><i>identifier</i></span>
                <span style="color: blue;">true</span>
                <span style="color: blue;">false</span>
                 
     <i>expr</i> ::= <i>atomic</i>
              <span style="color: blue;">(</span> <span style="color: blue;">if</span> <i>expr</i> <i>expr</i> <i>expr</i> <span style="color: blue;">)</span>
              <span style="color: blue;">(</span> <span style="color: blue;">let</span> <span style="color: blue;">(</span> <span style="color: blue;">(</span> <span style="color: blue;"><i>identifier</i></span> <i>expr</i> <span style="color: blue;">)</span> <span style="color: blue;">)</span> <i>expr</i> <span style="color: blue;">)</span>
              <span style="color: blue;">(</span> <span style="color: blue;">do</span> <i>expr</i> ... <span style="color: blue;">)</span>
              <span style="color: blue;">(</span> <span style="color: blue;">fun</span> <span style="color: blue;"><i>identifier</i></span> <span style="color: blue;">(</span> <span style="color: blue;"><i>identifier</i></span> ...  <span style="color: blue;">)</span>  <i>expr</i> <span style="color: blue;">)</span>  
              <span style="color: blue;">(</span> <span style="color: blue;">fun</span> <span style="color: blue;">(</span> <span style="color: blue;"><i>identifier</i></span> ...  <span style="color: blue;">)</span>  <i>expr</i> <span style="color: blue;">)</span>  
              <span style="color: blue;">(</span> <i>expr</i> ...<span style="color: blue;">)</span>
</pre>

Tokens are in blue and stand for themselves, expect for token
`integer` which represents any sequence of digits (including a leading
negation sign) and token `identfier` which represents any sequence of
alphanumeric characters starting with an alphabetical character. The
surface syntax is pretty much exactly as before, except supporting
multiple arguments to functions.

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

You can run the shell via `shell()`, or you can evaluate the content
of a file containing an expression using `evalFile(fname)`.  Here's a
file [`test.func`](test.func) that you can use with `evalFile`.
    
    
* * * 


## Question 1 : Strings

### (A)

Add a new value subclass `VString` representing strings.

    class VString (val str : String) extends Value 


Sample output:

    scala> new VString("")
    res3: VString = VString[]

    scala> new VString("hello world")
    res4: VString = VString[hello world]


### (B)

Add a corresponding expression subcass `EString` representing string literals.

    class EString (val s:String) extends Exp
    
Sample output:

    scala> new EString("")
    res5: EString = EString[]
    
    scala> new EString("hello world")
    res6: EString = EString[hello world]
    
    scala> (new EString("hello world")).eval(initEnv)
    res7: Value = VString[hello world]


### (C)

Extend the parser to add surface syntax letting you parse an expression `"foo"` as `EString("foo")`. 

So as not to send you down the regular expressions rabbit hole, this
is the definition of the string token that I used in my own code: it
parses a sequence of characters starting with `"` and ending with `"`
into a string with value the sequence of characters between the `"`

    def STRING : Parser[String] = """\"[^"]*\"""".r ^^ { s => s.substring(1, s.length - 1) }

Sample output:

    scala> shell()
    Type #quit to quit
    FUNC> ""
    ;; Parse: 
    ;;  EString[]
    ""
    FUNC> "hello world"
    ;; Parse: 
    ;;  EString[hello world]
    "hello world"
    FUNC> (let ((a "this is a string")) a)
    ;; Parse: 
    ;;  EApply[EFunction[ _gs_0
    ;;                   (a)
    ;;                   EId[a]]
    ;;         EString[this is a string]]
    "this is a string"
    

### (D)

Code some primitive operations to work with strings:

    def operConcat (vs : List[Value]) : Value = {
       ...
    }
    
    def operLower (vs : List[Value]) : Value = { 
       ...
    }
    
    def operUpper (vs : List[Value]) : Value = { 
       ...
    }
    
    def operSubstring (vs : List[Value]) : Value = { 
       ...
    }

Primitive `operConcat` should take an arbitrary number of strings and concatenate them together into a resulting string that's returned. Primitive `operLower` and `operUpper` should take a single string and return a new string consisting of the original string's characters all in lowercase or uppwecase, respectively. Primitive `operSubstring` should take three arguments, a string, an integer start index, and an integer end index, and return the part of the string between the start index (inclusive) and the end index (exclusive).

Bind them respectively to the following symbols in the initial environment:

    concat
    lower
    upper
    substring
    
Sample output:

    scala> shell()
    Type #quit to quit
    FUNC> (concat "hello " "world")
    ;; Parse: 
    ;;  EApply[EId[concat]
    ;;         EString[hello ]
    ;;         EString[world]]
    "hello world"
    FUNC> (concat "hello " "world " "and goodbye")
    ;; Parse: 
    ;;  EApply[EId[concat]
    ;;         EString[hello ]
    ;;         EString[world ]
    ;;         EString[and goodbye]]
    "hello world and goodbye"
    FUNC> (concat "foo")
    ;; Parse: 
    ;;  EApply[EId[concat]
    ;;         EString[foo]]
    "foo"
    FUNC> (concat)
    ;; Parse: 
    ;;  EApply[EId[concat]]
    ""
    FUNC> (upper "hello world")
    ;; Parse: 
    ;;  EApply[EId[upper]
    ;;         EString[hello world]]
    "HELLO WORLD"
    FUNC> (lower "Hello World")
    ;; Parse: 
    ;;  EApply[EId[lower]
    ;;         EString[Hello World]]
    "hello world"
    FUNC> (substring "hello world" 0 3)
    ;; Parse: 
    ;;  EApply[EId[substring]
    ;;         EString[hello world]
    ;;         EInteger[0]
    ;;         EInteger[3]]
    "hel"
    FUNC> (substring "hello world" 3 4)
    ;; Parse: 
    ;;  EApply[EId[substring]
    ;;         EString[hello world]
    ;;         EInteger[3]
    ;;         EInteger[4]]
    "l"



* * * 

## Question 2 : Parser Transformations

We saw that we extend the language by adding surface syntax
that gets parsed into existing abstract representation,
without the need to introduce new abstract representation.


### (A)

The short-circuiting conjunction `(and a b)` is true when both `a` and `b`
are true, and false otherwise. Moreover, if `a` is false, `b` need not be
evaluated. Similarly, the short-circuiting disjunction `(or a b)` is
true when either `a` or `b` is true, and false otherwise. If `a` if true,
then `b` need not be evaluated.

We saw in class that we can parse `(and a b)` as `(if a b false)`, and
`(or a b)` as `(if a true b)`, so we don't actually need to introduce
abstract representations for `and` and `or`. This of course
generalizes to multiple arguments.

Extend the surface syntax via a parser transformation so you can support conjunctions and
disjunctions with an arbitrary number of arguments. The surface syntax expression

    (and e1 e2 e3 ...)
    
should transform into:

    (if e1 (if e2 (if e3 ...) false) false)
    
And 

    (or e1 e2 e3 ...)
    
should transform into:

    (if e1 true (if e2 true (if e3 true ...)))

Note that for consistency you can have `(and)` be equivalent to `true` and `(or)` be equivalent to `false`. (Why?)

Sample output:

    scala> shell()
    Type #quit to quit
    FUNC> (and)
    ;; Parse: 
    ;;  EBoolean[true]
    true
    FUNC> (and true)
    ;; Parse: 
    ;;  EIf[EId[true]
    ;;      EBoolean[true]
    ;;      EBoolean[false]]
    true
    FUNC> (and true false false)
    ;; Parse: 
    ;;  EIf[EId[true]
    ;;      EIf[EId[false]
    ;;          EIf[EId[false]
    ;;              EBoolean[true]
    ;;              EBoolean[false]]
    ;;          EBoolean[false]]
    ;;      EBoolean[false]]
    false
    FUNC> (and true true true)
    ;; Parse: 
    ;;  EIf[EId[true]
    ;;      EIf[EId[true]
    ;;          EIf[EId[true]
    ;;              EBoolean[true]
    ;;              EBoolean[false]]
    ;;          EBoolean[false]]
    ;;      EBoolean[false]]
    true
    FUNC> (and false true true)
    ;; Parse: 
    ;;  EIf[EId[false]
    ;;      EIf[EId[true]
    ;;          EIf[EId[true]
    ;;              EBoolean[true]
    ;;              EBoolean[false]]
    ;;          EBoolean[false]]
    ;;      EBoolean[false]]
    false
    FUNC> (and false error)
    ;; Parse: 
    ;;  EIf[EId[false]
    ;;      EIf[EId[error]
    ;;          EBoolean[true]
    ;;          EBoolean[false]]
    ;;      EBoolean[false]]
    false
    FUNC> (or)
    ;; Parse: 
    ;;  EBoolean[false]
    false
    FUNC> (or true)
    ;; Parse: 
    ;;  EIf[EId[true]
    ;;      EBoolean[true]
    ;;      EBoolean[false]]
    true
    FUNC> (or true false)
    ;; Parse: 
    ;;  EIf[EId[true]
    ;;      EBoolean[true]
    ;;      EIf[EId[false]
    ;;          EBoolean[true]
    ;;          EBoolean[false]]]
    true
    FUNC> (or false false true)
    ;; Parse: 
    ;;  EIf[EId[false]
    ;;      EBoolean[true]
    ;;      EIf[EId[false]
    ;;          EBoolean[true]
    ;;          EIf[EId[true]
    ;;              EBoolean[true]
    ;;              EBoolean[false]]]]
    true
    FUNC> (or false false false)
    ;; Parse: 
    ;;  EIf[EId[false]
    ;;      EBoolean[true]
    ;;      EIf[EId[false]
    ;;          EBoolean[true]
    ;;          EIf[EId[false]
    ;;              EBoolean[true]
    ;;              EBoolean[false]]]]
    false
    FUNC> (or false true error)
    ;; Parse: 
    ;;  EIf[EId[false]
    ;;      EBoolean[true]
    ;;      EIf[EId[true]
    ;;          EBoolean[true]
    ;;          EIf[EId[error]
    ;;              EBoolean[true]
    ;;              EBoolean[false]]]]
    true


### (B)

We have `let` in our language. It is implemented as an abstract
representation expression `ELet` that defines simultaneous bindings.

It turns out we can implement (simultaneous) let as a straightforward
parser transformation. If you think about it, an expression 

    (let ((x1 e1) (x2 e2) ...)
      e)
      
is equivalent to 

    ((fun (x1 x2 ...) e) e1 e2 ...)
    
Modify the parser so that `let` is parser using this parser transformation, instead of using `ELet`. (I.e., after you do this change, you should be able to remove `ELet` from the code and everything should work as before.)

Sample output:

    scala> shell()
    Type #quit to quit
    FUNC> (let ((a 10)) a)
    ;; Parse: 
    ;;  EApply[EFunction[ _gs_4
    ;;                   (a)
    ;;                   EId[a]]
    ;;         EInteger[10]]
    10
    FUNC> (let ((a 10) (b 20) (c 30)) (+ a (+ b c)))
    ;; Parse: 
    ;;  EApply[EFunction[ _gs_2
    ;;                   (a, b, c)
    ;;                   EApply[EId[+]
    ;;                          EId[a]
    ;;                          EApply[EId[+]
    ;;                                 EId[b]
    ;;                                 EId[c]]]]
    ;;         EInteger[10]
n    ;;         EInteger[20]
    ;;         EInteger[30]]
    60
    FUNC> (let ((a (+ 1 2)) (b (* 2 3))) b)
    ;; Parse: 
    ;;  EApply[EFunction[ _gs_5
    ;;                   (a, b)
    ;;                   EId[b]]
    ;;         EApply[EId[+]
    ;;                EInteger[1]
    ;;                EInteger[2]]
    ;;         EApply[EId[*]
    ;;                EInteger[2]
    ;;                EInteger[3]]]
    6


### (C)

In homework 2, we saw `let*` as a variant of `let` that instead of binding all the names at once, binds them sequentially, each binding defined in an environment in which the previous bindings are available. Recall that we want an expression

    (let* ((x1 e1) (x2 e2) ...)
      e)
      
to first evaluate `e_1`, binding it to `x_1`; then evaluate `e_2` in an environment containing that binding for `x_1, and binding the result to `x_2`; and so on. Finally, it evaluates `e` in an environment containing all the bindings for `x_1`, `x_2`, ....

Extend the surface syntax via a parser transformation so you can support `let*` as above. Specifically, the parser transformation should take

    (let* ((x1 e1) (x2 e2) (x3 e3) ...) 
      e)
      
and transform it to the equivalent:

    (let ((x1 e1))
      (let ((x2 e2)) 
        (let ((x3 e3)) 
          ...
            e))

For full credit, you should rely on the `let` parser transformation
from question (B), and your result should not use the `ELet` abstract
representation.

Sample output:

    scala> shell()
    Type #quit to quit
    FUNC> (let* ((a 1)) a)
    ;; Parse: 
    ;;  EApply[EFunction[ _gs_13
    ;;                   (a)
    ;;                   EId[a]]
    ;;         EInteger[1]]
    1
    FUNC> (let* ((a 1) (b (+ a 1))) b)
    ;; Parse: 
    ;;  EApply[EFunction[ _gs_15
    ;;                   (a)
    ;;                   EApply[EFunction[ _gs_14
    ;;                                    (b)
    ;;                                    EId[b]]
    ;;                          EApply[EId[+]
    ;;                                 EId[a]
    ;;                                 EInteger[1]]]]
    ;;         EInteger[1]]
    2
    FUNC> (let* ((a 1) (b (+ a 1)) (c (+ b 1))) c)
    ;; Parse: 
    ;;  EApply[EFunction[ _gs_18
    ;;                   (a)
    ;;                   EApply[EFunction[ _gs_17
    ;;                                    (b)
    ;;                                    EApply[EFunction[ _gs_16
    ;;                                                     (c)
    ;;                                                     EId[c]]
    ;;                                           EApply[EId[+]
    ;;                                                  EId[b]
    ;;                                                  EInteger[1]]]]
    ;;                          EApply[EId[+]
    ;;                                 EId[a]
    ;;                                 EInteger[1]]]]
    ;;         EInteger[1]]
    3
    FUNC> (let* ((a 1) (b (+ a 1)) (c (+ b 1)) (d (+ a (+ b c)))) d)
    ;; Parse: 
    ;;  EApply[EFunction[ _gs_22
    ;;                   (a)
    ;;                   EApply[EFunction[ _gs_21
    ;;                                    (b)
    ;;                                    EApply[EFunction[ _gs_20
    ;;                                                     (c)
    ;;                                                     EApply[EFunction[ _gs_19
    ;;                                                                      (d)
    ;;                                                                      EId[d]]
    ;;                                                            EApply[EId[+]
    ;;                                                                   EId[a]
    ;;                                                                   EApply[EId[+]
    ;;                                                                          EId[b]
    ;;                                                                          EId[c]]]]]
    ;;                                           EApply[EId[+]
    ;;                                                  EId[b]
    ;;                                                  EInteger[1]]]]
    ;;                          EApply[EId[+]
    ;;                                 EId[a]
    ;;                                 EInteger[1]]]]
    ;;         EInteger[1]]
    6


### (D)

The `if` conditional is nice but there are other useful forms of
conditionals. For instance:

    (cond ((= x 0) 10)
          ((= x 1) 20)
          ((= x 2) 30)
          (true 40))

evaluates to 10 if `x` is equal to 0, 20 if `x` is equal to 1, 30 if `x` is
equal to 2, and 40 otherwise. Intuitively, each clause of the form `(t e)`
is checked, from the first to the last, in order: if the test expression `t` evaluates to
true, then the result of evaluating the whole cond is the corresponding `e`, otherwise, the next
clause is checked. If no clause matches, the result is `false`. 

Extend the surface syntax via a parser transformation so you can
support `cond` as above. Specifically, the parser transformation
should take

    (cond (c1 e1)
          (c2 e2)
          ...)
          
and transform it into an expression made of cascaded ifs:

    (if c1
      e1
      (if c2
        e2 
        ...))
        

Sample output:

    scala> shell()
    Type #quit to quit
    FUNC> (cond (true 1))
    ;; Parse: 
    ;;  EIf[EId[true]
    ;;      EInteger[1]
    ;;      EBoolean[false]]
    1
    FUNC> (cond (false 1) (true 2))
    ;; Parse: 
    ;;  EIf[EId[false]
    ;;      EInteger[1]
    ;;      EIf[EId[true]
    ;;          EInteger[2]
    ;;          EBoolean[false]]]
    2
    FUNC> (cond (true 1) (false 2))
    ;; Parse: 
    ;;  EIf[EId[true]
    ;;      EInteger[1]
    ;;      EIf[EId[false]
    ;;          EInteger[2]
    ;;          EBoolean[false]]]
    1
    FUNC> (let ((x 10)) (cond ((= x 10) 99) ((= x 0) 66) (true 33)))
    ;; Parse: 
    ;;  EApply[EFunction[ _gs_23
    ;;                   (x)
    ;;                   EIf[EApply[EId[=]
    ;;                              EId[x]
    ;;                              EInteger[10]]
    ;;                       EInteger[99]
    ;;                       EIf[EApply[EId[=]
    ;;                                  EId[x]
    ;;                                  EInteger[0]]
    ;;                           EInteger[66]
    ;;                           EIf[EId[true]
    ;;                               EInteger[33]
    ;;                               EBoolean[false]]]]]
    ;;         EInteger[10]]
    99
    FUNC> (let ((x 0)) (cond ((= x 10) 99) ((= x 0) 66) (true 33)))
    ;; Parse: 
    ;;  EApply[EFunction[ _gs_24
    ;;                   (x)
    ;;                   EIf[EApply[EId[=]
    ;;                              EId[x]
    ;;                              EInteger[10]]
    ;;                       EInteger[99]
    ;;                       EIf[EApply[EId[=]
    ;;                                  EId[x]
    ;;                                  EInteger[0]]
    ;;                           EInteger[66]
    ;;                           EIf[EId[true]
    ;;                               EInteger[33]
    ;;                               EBoolean[false]]]]]
    ;;         EInteger[0]]
    66
    FUNC> (let ((x 5)) (cond ((= x 10) 99) ((= x 0) 66) (true 33)))
    ;; Parse: 
    ;;  EApply[EFunction[ _gs_25
    ;;                   (x)
    ;;                   EIf[EApply[EId[=]
    ;;                              EId[x]
    ;;                              EInteger[10]]
    ;;                       EInteger[99]
    ;;                       EIf[EApply[EId[=]
    ;;                                  EId[x]
    ;;                                  EInteger[0]]
    ;;                           EInteger[66]
    ;;                           EIf[EId[true]
    ;;                               EInteger[33]
    ;;                               EBoolean[false]]]]]
    ;;         EInteger[5]]
    33

* * * 

## Question 3 : Vector Comprehension

In class, we saw two higher-order functions that are useful when
working with sequences such as lists, arrays, or vectors in Python or
other languages: `map` and `filter`. Recall that `map` takes a
function to apply to every element of a list, returning a list of the
results, while `filter` takes a Boolean-valued function (a predicate)
and returns all the elements in a list for which the predicate is
true. We'll basically implement `map` and `filter` for vectors in FUNC.


### (A)

First, let's make it more convenient to create vectors. Right now, the
only facilities for creating vectors are the values `empty` (which
represents an empty vector) and the primitive operation `cons` taking
an item `a` and a vector `v` and creating a new vector starting with
`a` and whose subsequent elements are those from `v`.

Code a primitive operation 

    def operVector (vs : List[Value]) : Value = {
       ...
    }
    
taking an arbitrary number of values, and returning a new vector made up of the supplied values, in the same order.

Bind this primitive operation to the name `vector` in the initial environment.

Sample output:

    scala> shell()
    Type #quit to quit
    FUNC> (vector)
    ;; Parse: 
    ;;  EApply[EId[vector]]
    ()
    FUNC> (vector 1)
    ;; Parse: 
    ;;  EApply[EId[vector]
    ;;         EInteger[1]]
    (1)
    FUNC> (vector 1 2 3 4 5)
    ;; Parse: 
    ;;  EApply[EId[vector]
    ;;         EInteger[1]
    ;;         EInteger[2]
    ;;         EInteger[3]
    ;;         EInteger[4]
    ;;         EInteger[5]]
    (1 2 3 4 5)
    FUNC> (vector (+ 1 2) (+ 3 4) (+ 4 5))
    ;; Parse: 
    ;;  EApply[EId[vector]
    ;;         EApply[EId[+]
    ;;                EInteger[1]
    ;;                EInteger[2]]
    ;;         EApply[EId[+]
    ;;                EInteger[3]
    ;;                EInteger[4]]
    ;;         EApply[EId[+]
    ;;                EInteger[4]
    ;;                EInteger[5]]]
    (3 7 9)
    FUNC> (first (vector 1 2 3 4))
    ;; Parse: 
    ;;  EApply[EId[first]
    ;;         EApply[EId[vector]
    ;;                EInteger[1]
    ;;                EInteger[2]
    ;;                EInteger[3]
    ;;                EInteger[4]]]
    1
    FUNC> (rest (vector 1 2 3 4))
    ;; Parse: 
    ;;  EApply[EId[rest]
    ;;         EApply[EId[vector]
    ;;                EInteger[1]
    ;;                EInteger[2]
    ;;                EInteger[3]
    ;;                EInteger[4]]]
    (2 3 4)


### (B)

Code a primitive operation 

    def operMap (vs : List[Value]) : Value = {
       ...
    }
    
taking two values, a function (either a primitive operation or a
closure) and a vector, and returning a new vector with the function
value applied to every element of the vector.

Bind this primitive operation to the name `map` in the initial environment.

Sample output:

    scala> shell()
    Type #quit to quit
    FUNC> (map (fun (a) (* a a)) empty)
    ;; Parse: 
    ;;  EApply[EId[map]
    ;;         EFunction[ _gs_28
    ;;                   (a)
    ;;                   EApply[EId[*]
    ;;                          EId[a]
    ;;                          EId[a]]]
    ;;         EId[empty]]
    ()
    FUNC> (map (fun (a) (* a a)) (cons 10 empty))
    ;; Parse: 
    ;;  EApply[EId[map]
    ;;         EFunction[ _gs_29
    ;;                   (a)
    ;;                   EApply[EId[*]
    ;;                          EId[a]
    ;;                          EId[a]]]
    ;;         EApply[EId[cons]
    ;;                EInteger[10]
    ;;                EId[empty]]]
    (100)
    FUNC> (map (fun (a) (* a a)) (vector 1 2 3 4))
    ;; Parse: 
    ;;  EApply[EId[map]
    ;;         EFunction[ _gs_26
    ;;                   (a)
    ;;                   EApply[EId[*]
    ;;                          EId[a]
    ;;                          EId[a]]]
    ;;         EApply[EId[vector]
    ;;                EInteger[1]
    ;;                EInteger[2]
    ;;                EInteger[3]
    ;;                EInteger[4]]]
    (1 4 9 16)
    	  
    
### (C) 

Code a primitive operation 

    def operFilter (vs : List[Value]) : Value = {
       ...
    }
    
taking two values, a function (either a
primitive operation or a closure) and a vector, and
returning a new vector with all the element of the
original vector (in order) for which the function value
returns the Boolean value true.

Bind this primitive operation to the name `filter` in the initial environment.

Sample output:

    scala> shell()
    Type #quit to quit
    FUNC> (not true)
    ;; Parse: 
    ;;  EApply[EId[not]
    ;;         EId[true]]
    false
    FUNC> (filter (fun (a) (not (< a 0))) empty)
    ;; Parse: 
    ;;  EApply[EId[filter]
    ;;         EFunction[ _gs_30
    ;;                   (a)
    ;;                   EApply[EId[not]
    ;;                          EApply[EId[<]
    ;;                                 EId[a]
    ;;                                 EInteger[0]]]]
    ;;         EId[empty]]
    ()
    FUNC> (filter (fun (a) (not (< a 0))) (cons 1 empty))
    ;; Parse: 
    ;;  EApply[EId[filter]
    ;;         EFunction[ _gs_31
    ;;                   (a)
    ;;                   EApply[EId[not]
    ;;                          EApply[EId[<]
    ;;                                 EId[a]
    ;;                                 EInteger[0]]]]
    ;;         EApply[EId[cons]
    ;;                EInteger[1]
    ;;                EId[empty]]]
    (1)
    FUNC> (filter (fun (a) (not (< a 0))) (cons -1 empty))
    ;; Parse: 
    ;;  EApply[EId[filter]
    ;;         EFunction[ _gs_32
    ;;                   (a)
    ;;                   EApply[EId[not]
    ;;                          EApply[EId[<]
    ;;                                 EId[a]
    ;;                                 EInteger[0]]]]
    ;;         EApply[EId[cons]
    ;;                EInteger[-1]
    ;;                EId[empty]]]
    ()
    FUNC> (filter (fun (a) (not (< a 0))) (vector 1 -2 3 -4 5 -6 7))
    ;; Parse: 
    ;;  EApply[EId[filter]
    ;;         EFunction[ _gs_33
    ;;                   (a)
    ;;                   EApply[EId[not]
    ;;                          EApply[EId[<]
    ;;                                 EId[a]
    ;;                                 EInteger[0]]]]
    ;;         EApply[EId[vector]
    ;;                EInteger[1]
    ;;                EInteger[-2]
    ;;                EInteger[3]
    ;;                EInteger[-4]
    ;;                EInteger[5]
    ;;                EInteger[-6]
    ;;                EInteger[7]]]
    (1 3 5 7)


### (D) 

After Parts (A), (B), (C), you can write expressions such as:

    (filter (fun (x) (not (< x 0))) (map (fun (v) (* v 2)) (vector 1 -2 3 -4 5 -6)))
    
which evaluates to the vector `(2 6 10)`.

That's all well and good. But Python --- and before that
[Haskell](https://www.haskell.org) --- uses a more convenient
notation for applying `map` and `filter` called _comprehension_,
inspired by [set comprehension](https://en.wikipedia.org/wiki/Set-builder_notation)
 from naive set theory.

In Python, an expression such as `[ e1 for id in e2 ]` where `e2`
evaluates to a list `v` returns a new list where every element is
obtained by successively binding each element of `v` to `id` before
evaluating `e1` to obtain the new element. Thus, `[ x * 2 for x in [1, 2, 3, 4] ]` in Python would return `[2, 4, 6, 8]`.

Similarly, an expression `[ e1 for id in e2 if e3 ]` returns a new list where `e1` is used to obtain the new elements, except that additionally `e3` is evaluated after binding `id` and only the values for which `e3` evaluates to true are kept. Thus, `[ x * 3 for x in [1, 2, 3, 4] if even(x) ]` in Python would return `[6, 12]` assuming a suitable definition of `even`, since only 2 and 4 make `even` return
true.

Extend the surface syntax via a parser transformation so you can support an expression

    (comp e1 for id in e2)
    
which should transform into an expression

    (map (fun (id) e1) e2)
    
and an expression 

    (comp e1 for id in e2 when e3)
    
which should transform into an expression

    (map (fun (id) e1) (filter (fun (id) e3) e2))


Sample output:

    scala> shell()
    Type #quit to quit
    FUNC> (comp (* x x) for x in empty)
    ;; Parse: 
    ;;  EApply[EPrimitive[457af059]
    ;;         EFunction[
    ;;                   (x)
    ;;                   EApply[EId[*]
    ;;                          EId[x]
    ;;                          EId[x]]]
    ;;         EId[empty]]
    ()
    FUNC> (comp (* x x) for x in (cons 10 empty))
    ;; Parse: 
    ;;  EApply[EPrimitive[457af059]
    ;;         EFunction[
    ;;                   (x)
    ;;                   EApply[EId[*]
    ;;                          EId[x]
    ;;                          EId[x]]]
    ;;         EApply[EId[cons]
    ;;                EInteger[10]
    ;;                EId[empty]]]
    (100)
    FUNC> (comp (* x x) for x in (vector 1 2 3 4 5))
    ;; Parse: 
    ;;  EApply[EPrimitive[457af059]
    ;;         EFunction[
    ;;                   (x)
    ;;                   EApply[EId[*]
    ;;                          EId[x]
    ;;                          EId[x]]]
    ;;         EApply[EId[vector]
    ;;                EInteger[1]
    ;;                EInteger[2]
    ;;                EInteger[3]
    ;;                EInteger[4]
    ;;                EInteger[5]]]
    (1 4 9 16 25)
    FUNC> (comp (* x x) for x in (vector 1 -2 3 -4 5 -6 7 -8))
    ;; Parse: 
    ;;  EApply[EPrimitive[457af059]
    ;;         EFunction[
    ;;                   (x)
    ;;                   EApply[EId[*]
    ;;                          EId[x]
    ;;                          EId[x]]]
    ;;         EApply[EId[vector]
    ;;                EInteger[1]
    ;;                EInteger[-2]
    ;;                EInteger[3]
    ;;                EInteger[-4]
    ;;                EInteger[5]
    ;;                EInteger[-6]
    ;;                EInteger[7]
    ;;                EInteger[-8]]]
    (1 4 9 16 25 36 49 64)
    FUNC> (comp (* x x) for x in (vector 1 -2 3 -4 5 -6 7 -8) when (not (< x 0)))
    ;; Parse: 
    ;;  EApply[EPrimitive[1d9643e6]
    ;;         EFunction[
    ;;                   (x)
    ;;                   EApply[EId[*]
    ;;                          EId[x]
    ;;                          EId[x]]]
    ;;         EApply[EPrimitive[7f487086]
    ;;                EFunction[
    ;;                          (x)
    ;;                          EApply[EId[not]
    ;;                                 EApply[EId[<]
    ;;                                        EId[x]
    ;;                                        EInteger[0]]]]
    ;;                EApply[EId[vector]
    ;;                       EInteger[1]
    ;;                       EInteger[-2]
    ;;                       EInteger[3]
    ;;                       EInteger[-4]
    ;;                       EInteger[5]
    ;;                       EInteger[-6]
    ;;                       EInteger[7]
    ;;                       EInteger[-8]]]]
    (1 9 25 49)



* * * 


## Qestion 4: Dictionaries

A dictionary is a structure that maps values (keys) to values. In
Python, a dictionary is defined like this:

     { 1: [1,2,3],  3: True, 5: 8, "hello": "world" }

and items in dictionary `d` are accessed via `d[1]`  and updated via `d[1] = "foo"`.

### (A)

Add a new value subclass `VDictionary` representing dictionaries.

    class VDictionary (val entries : List[(Value, Value)]) extends Value

Dictionary should support methods `dget` and `dput` to get the entry corresponding to a given key value, and update the entry corresponding to a key value, respectively. In the case of `dput`, if the key already exists in the dictinoary, the previous value associated with the key is overwritten. If the key does not exist in the dictionary, it should get added.

Two challenges:

(1) Allowing keys to be any kind of value means that when you look up
keys in the dictionary with `dget` and `dput` you'll have to figure
out how to compare values for equality. Many ways of doing that, some
cleaner than others.

(2) Adding the ability to update the entry corresponding to a key
value is the thorniest bit of this question, so you may want to
postpone it until the end, in part (E). There are many ways of doing
it. I personally created a class `UpdatableValue` in Scala to play the
role of a reference cell for values at the Scala level, but I don't
want to constrain you to do it that way.


Sample output:

    // empty dictionary
    scala> val d = new VDictionary(List())
    d: VDictionary = VDictionary[]
    
    scala> d.dget(new VInteger(1))
    java.lang.Exception: Runtime error: Cannot find key VInteger[1] in dictionary
      at .runtimeError(solution4.scala:43)
      at VDictionary.dget(solution4.scala:319)
      ... 28 elided
    
    // dictionary (1 10) ("hello" 20) (true "30")
    scala> val d2 = new VDictionary(List((new VInteger(1), new VInteger(10)), (new VString("hello"), new VInteger(20)), (new VBoolean(true), new VString("30"))))
    d2: VDictionary = VDictionary[(VInteger[1], VInteger[10]), (VString[hello], VInteger[20]), (VBoolean[true], VString[30])]

    scala> d2.dget(new VInteger(1))
    res20: Value = VInteger[10]
    
    scala> d2.dget(new VBoolean(true))
    res21: Value = VString[30]
    
    scala> d2.dget(new VString("hello"))
    res22: Value = VInteger[20]

    // updating field "hello"
    scala> d2.dput(new VString("hello"), new VString("world"))
    res24: Value = VString[world]
    
    scala> d2
    res25: VDictionary = VDictionary[(VInteger[1], VInteger[10]), (VString[hello], VString[world]), (VBoolean[true], VString[30])]

    scala> d2.dget(new VString("hello"))
    res26: Value = VString[world]

    // adding field 4
    scala> d2.dput(new VInteger(4), new VInteger(400))
    res27: Value = VInteger[400]
    
    scala> d2
    res28: VDictionary = VDictionary[(VInteger[4], VInteger[400]), (VInteger[1], VInteger[10]), (VString[hello], VString[world]), (VBoolean[true], VString[30])]
    
    scala> d2.dget(new VInteger(4))
    res30: Value = VInteger[400]
    


### (B)

Add an expression subcass `EDictionary` that evaluates to a dictionary by evaluating the keys and the items associated with the keys:

    class EDictionary (val entries : List[(Exp, Exp)]) extends Exp

Sample output:

    scala> new EDictionary(List((new EInteger(1), new EInteger(10)), (new EString("hello"), new EInteger(20)), (new EBoolean(true), new EString("30"))))
    res33: EDictionary = EDictionary[(EInteger[1], EInteger[10]), (EString[hello], EInteger[20]), (EBoolean[true], EString[30])]
    
    scala> new EDictionary(List((new EInteger(1), new EInteger(10)), (new EString("hello"), new EInteger(20)), (new EBoolean(true), new EString("30")))).eval(initEnv)
    res34: Value = VDictionary[(VInteger[1], VInteger[10]), (VString[hello], VInteger[20]), (VBoolean[true], VString[30])]
    
    scala> new EDictionary(List((new EApply(new EId("+"), List(new EInteger(1), new EInteger(2))), new EApply(new EId("*"), List(new EInteger(10), new EInteger(20)))))).eval(initEnv)
    res36: Value = VDictionary[(VInteger[3], VInteger[200])]


### (C)

Extend the parser to add surface syntax letting you parse an expression

    (dict (k1 e1) (k2 e2) ...)
    
into an `EDictionary` with entries `(k1, e1)`, `(k2, e2)`, and so on. Each k1, k2, ... and e1, e2, ... should be arbitrary expressions.

    scala> shell()
    Type #quit to quit
    FUNC> (dict)
    ;; Parse: 
    ;;  EDictionary[]
    ;; #DICT[]
    FUNC> (dict ("hello" "world"))
    ;; Parse: 
    ;;  EDictionary[(EString[hello],
    ;;               EString[world])
    #DICT[("hello", "world")]
    FUNC> (dict (1 10) ("hello" 20) (true "30"))
    ;; Parse: 
    ;;  EDictionary[(EInteger[1],
    ;;               EInteger[10])
    ;;               EString[hello],
    ;;               EInteger[20])
    ;;               EId[true],
    ;;               EString[30]]
    #DICT[(1, 10), ("hello", 20), (true, "30")]
    FUNC> (dict ((+ 1 2) (* 10 20)))
    ;; Parse: 
    ;;  EDictionary[(EApply[EId[+]
    ;;                      EInteger[1]
    ;;                      EInteger[2]],
    ;;               EApply[EId[*]
    ;;                      EInteger[10]
    ;;                      EInteger[20]])
    #DICT[(3, 200)]

### (D)

Code a primitive operation `operDictGet`

    def operDictGet(vs : List[Value]) : Value = {
       ...
    }
    
taking two values, a dictionary `d` and a key value `k`, and returning
the item associated with key `k` in dictionary `d`. It should raise a
runtime error if the key is not present.

Bind this primitive operation to name `dget` in the initial environment.

    scala> shell()
    Type #quit to quit
    FUNC> (let ((d (dict (1 10) ("hello" 20) (true "30")))) (dget d 1))
    ;; Parse: 
    ;;  EApply[EFunction[ _gs_40
    ;;                   (d)
    ;;                   EApply[EId[dget]
    ;;                          EId[d]
    ;;                          EInteger[1]]]
    ;;         EDictionary[(EInteger[1],
    ;;                      EInteger[10])
    ;;                      EString[hello],
    ;;                      EInteger[20])
    ;;                      EId[true],
    ;;                      EString[30]]
    10
    FUNC> (let ((d (dict (1 10) ("hello" 20) (true "30")))) (dget d "hello"))
    ;; Parse: 
    ;;  EApply[EFunction[ _gs_41
    ;;                   (d)
    ;;                   EApply[EId[dget]
    ;;                          EId[d]
    ;;                          EString[hello]]]
    ;;         EDictionary[(EInteger[1],
    ;;                      EInteger[10])
    ;;                      EString[hello],
    ;;                      EInteger[20])
    ;;                      EId[true],
    ;;                      EString[30]]
    20
    FUNC> (let ((d (dict (1 10) ("hello" 20) (true "30")))) (dget d true))
    ;; Parse: 
    ;;  EApply[EFunction[ _gs_42
    ;;                   (d)
    ;;                   EApply[EId[dget]
    ;;                          EId[d]
    ;;                          EId[true]]]
    ;;         EDictionary[(EInteger[1],
    ;;                      EInteger[10])
    ;;                      EString[hello],
    ;;                      EInteger[20])
    ;;                      EId[true],
    ;;                      EString[30]]
    "30"




### (E) 


Code a primitive operation `operDictPut`

    def operDictPut(vs : List[Value]) : Value = {
       ...
    }
    
taking three values, a dictionary `d`, a key value `k`, and a value `v`, and updating or adding the item associated with key `k` in dictionary `d` to be value `v`. 

Bind this primitive operation to name `dput` in the initial environment.

    scala> shell()
    Type #quit to quit
    FUNC> (let ((d (dict (1 10) ("hello" 20) (true "30")))) (do (dput d "hello" "world") d))
    ;; Parse: 
    ;;  EApply[EFunction[ _gs_75
    ;;                   (d)
    ;;                   EApply[EFunction[ _gs_74
    ;;                                    ( _gs_73)
    ;;                                    EId[d]]
    ;;                          EApply[EId[dput]
    ;;                                 EId[d]
    ;;                                 EString[hello]
    ;;                                 EString[world]]]]
    ;;         EDictionary[(EInteger[1],
    ;;                      EInteger[10])
    ;;                      EString[hello],
    ;;                      EInteger[20])
    ;;                      EId[true],
    ;;                      EString[30]]
    #DICT[(1, 10), ("hello", "world"), (true, "30")]
    FUNC> (let ((d (dict (1 10) ("hello" 20) (true "30")))) (do (print "before:" (dget d "hello")) (dput d "hello" "world") (print "after:" (dget d "hello")) d))
    ;; Parse: 
    ;;  EApply[EFunction[ _gs_82
    ;;                   (d)
    ;;                   EApply[EFunction[ _gs_81
    ;;                                    ( _gs_80)
    ;;                                    EApply[EFunction[ _gs_79
    ;;                                                     ( _gs_78)
    ;;                                                     EApply[EFunction[ _gs_77
    ;;                                                                      ( _gs_76)
    ;;                                                                      EId[d]]
    ;;                                                            EApply[EId[print]
    ;;                                                                   EString[after:]
    ;;                                                                   EApply[EId[dget]
    ;;                                                                          EId[d]
    ;;                                                                          EString[hello]]]]]
    ;;                                           EApply[EId[dput]
    ;;                                                  EId[d]
    ;;                                                  EString[hello]
    ;;                                                  EString[world]]]]
    ;;                          EApply[EId[print]
    ;;                                 EString[before:]
    ;;                                 EApply[EId[dget]
    ;;                                        EId[d]
    ;;                                        EString[hello]]]]]
    ;;         EDictionary[(EInteger[1],
    ;;                      EInteger[10])
    ;;                      EString[hello],
    ;;                      EInteger[20])
    ;;                      EId[true],
    ;;                      EString[30]]
    "before:" 20  
    "after:" "world"  
    #DICT[(1, 10), ("hello", "world"), (true, "30")]
    FUNC> (let ((d (dict (1 10)))) (do (dput d 2 20) d))
    ;; Parse: 
    ;;  EApply[EFunction[ _gs_85
    ;;                   (d)
    ;;                   EApply[EFunction[ _gs_84
    ;;                                    ( _gs_83)
    ;;                                    EId[d]]
    ;;                          EApply[EId[dput]
    ;;                                 EId[d]
    ;;                                 EInteger[2]
    ;;                                 EInteger[20]]]]
    ;;         EDictionary[(EInteger[1],
    ;;                      EInteger[10])
    #DICT[(2, 20), (1, 10)]
    FUNC> (let ((d (dict (1 10)))) (do (dput d 2 20) (dget d 2)))
    ;; Parse: 
    ;;  EApply[EFunction[ _gs_88
    ;;                   (d)
    ;;                   EApply[EFunction[ _gs_87
    ;;                                    ( _gs_86)
    ;;                                    EApply[EId[dget]
    ;;                                           EId[d]
    ;;                                           EInteger[2]]]
    ;;                          EApply[EId[dput]
    ;;                                 EId[d]
    ;;                                 EInteger[2]
    ;;                                 EInteger[20]]]]
    ;;         EDictionary[(EInteger[1],
    ;;                      EInteger[10])
    20


* * * 

## Question 5: Hacking the Shell

We've been using a rather simple shell for our object language FUNC. All it really does is let you enter expressions, and evaluate them. Unfortunately, each and every expression is evaluated independently within the initial environment that's defined. There is no way to add new entries to the environment so that you can use them later in other expressions you evaluate. That's an annoying limitation. There are others. 

The purpose of this question is to play around with the shell, seeing what is involved in adding features to it. It is a bit more open ended, in that I won't really guide you in terms of how to do it. I also don't care so much whether what you do looks exactly what I have below. Take my outputs as a guide. All I care about is functionality.


### (A)

Right now, when you evaluate an expression in the shell, it prints the abstract representation of the expression before evaluating it, which is useful for debugging, but is distracting for daily usage. An alternative is to have expressions evaluate to their value when you type them in without extraneous output, and to provide a shell command for examining the the abstract representation produced for an expression if the user wants to see it.

Your task: first, remove the part that prints the abstract representation when evaluating an expression in the shell, and second, add a shell command `#parse` that takes an expression and prints its abstract representation. Here's what that looks like in my implementation -- yours doesn't have to look exactly like this, as long as it lets you see the abstract representation:

    scala> shell()
    Type #quit to quit
    FUNC> (let ((a 1)) (let ((b 2) (c 3)) (+ a b)))
    3
    FUNC> #parse (let ((a 1)) (let ((b 2) (c 3)) (+ a b)))
    ;;  EApply[EFunction[ _gs_11
    ;;                   (a)
    ;;                   EApply[EFunction[ _gs_10
    ;;                                    (b, c)
    ;;                                    EApply[EId[+]
    ;;                                           EId[a]
    ;;                                           EId[b]]]
    ;;                          EInteger[2]
    ;;                          EInteger[3]]]
    ;;         EInteger[1]]
    FUNC> #parse (fun (a) (+ a 1))
    ;;  EFunction[ _gs_12
    ;;            (a)
    ;;            EApply[EId[+]
    ;;                   EId[a]
    ;;                   EInteger[1]]]
    FUNC> #parse (do (print 1) (print 2) (print 3))
    ;;  EApply[EFunction[ _gs_16
    ;;                   ( _gs_15)
    ;;                   EApply[EFunction[ _gs_14
    ;;                                    ( _gs_13)
    ;;                                    EApply[EId[print]
    ;;                                           EInteger[3]]]
    ;;                          EApply[EId[print]
    ;;                                 EInteger[2]]]]
    ;;         EApply[EId[print]
    ;;                EInteger[1]]]



### (B)

As I mentioned above, there is no way to add new entries to the environment of the shell. Let's remedy that. 

Add a shell command `#def` that takes a name and an expression, and adds a new entry to the shell's environment that binds the name to the value obtained by evaluating the expression supplied. That name, being in the environment, should be available for expressions next evaluated in that same shell.

Here's what that looks like in my implementation -- yours doesn't have to look exactly like this, as long as it lets you add bindings to the shell's environmment:

    scala> shell()
    Type #quit to quit
    FUNC> a
    java.lang.Exception: Runtime error: Runtime error : unbound identifier a
    FUNC> #def a 10
    a
    FUNC> a
    10
    FUNC> #def b (+ 1 2)
    b
    FUNC> b
    3
    FUNC> #def square (fun (a) (* a a))
    square
    FUNC> (square 99)
    9801
    FUNC> #def length (fun ln (a) (if (empty? a) 0 (+ 1 (ln (rest a)))))
    length
    FUNC> #def t (cons "hello" (cons "world" (cons "!" empty)))
    t
    FUNC> t
    ("hello" "world" "!")
    FUNC> (length t)
    3
    FUNC> #quit


### (C)

Add a shell command `#file` that takes a file name and reads the content of the file (which should be an expression) before evaluating it. 

For instance, here is a sample file, `test.func`:

    (do (print "Hello world!")
        (print "The next line should return the value 3")
        (let ((a 1)
              (b 2))
           (+ a b)))

Here's what that looks like in my implementation -- yours doesn't have to look exactly like this, as long as it lets you evaluate the content of a file.

    scala> shell()
    Type #quit to quit
    FUNC> #file test.func
    "Hello world!"  
    "The next line should return the value 3"  
    3


### (D)

Entering expressions on a single line in the shell gets old fast. 

Add a shell command `#multi` that lets you type a multi-line expression that doesn't get evaluated until you enter an empty line.

Here's what that looks like in my implementation -- yours doesn't have to look exactly like this, as long as it lets you evaluate multi-line expressions:

    scala> shell()
    Type #quit to quit
    FUNC> #multi
    ..... (+ 1
    .....    (+ 2
    .....       3))
    ..... 
    6
    FUNC> #multi
    ..... (let ((a 1)
    .....       (b 2))
    .....   (let ((c (+ a b)))
    .....     (* c c)))
    ..... 
    9
    FUNC> #multi
    ..... (cons
    ..... "hello"
    ..... (cons
    ..... "world"
    ..... empty)
    ..... )
    ..... 
    ("hello" "world")
