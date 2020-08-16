# Homework 1

## Due Date: Sunday, February 2, 2020 (23h59)

- This homework is to be done in teams of 2. You may discuss problems
with fellow students, but all submitted work must be entirely your
team's own, and should not be from any other course, present, past, or
future. If you use a solution from another source you must cite it
&mdash; this includes when that source is someone else helping
you.

- **Please do not post your solutions on a
public website or a public repository like GitHub.**

- All programming is to be done in Scala 2.13. 

- Code your answers by modifying the file [`homework1.scala`](homework1.scala) provided. Add your team members' **name**, **email address**, and any **remarks** that you
wish to make to the instructor to the block comment at the top of the file.

- **Please do not change the types in the signature of the
method stubs I provide**. Doing so will make it
impossible to load your code into the testing infrastructure,
which tends to make me unhappy.

- Feel free to define helper functions if you need them.


## Electronic Submission Instructions

- Run your homework code via `scala homework1.scala`

- If there are any error, do not submit. I can't test what will not load correctly.

- When you're ready to submit, send an email with your file `homework1.scala` as an attachment to `olin.submissions@gmail.com` with subject _Homework 1 submission_.


* * *

## Question 1: Scala Exercises

This question asks you to complete the implementation of a class
`Coordinates` with parameters `(val x : Double, val y : Double)` to
represent coordinates in the Cartesian plane. 

An instance `new Coordinates(x,y)` creates a point with coordinates
(`x`, `y`).

I have already implemented two accessor methods `xCoord () : Double`
and `yCoord () : Double` to access the x and y components of a
`Coordinates` instance. (They are not strictly necessary, as you may
discover later.)


### (A)

Define a method **`toString () : String`** that returns a string
representation of the coordinates as a pair, such as `"(1.2, 3.4)"`.

Sample output:

    scala> val c1 = new Coordinates(0, 0)
    c1: Coordinates = (0.0, 0.0)
    
    scala> c1.toString()
    res0: String = (0.0, 0.0)
    
    scala> val c2 = new Coordinates(3, -2)
    c2: Coordinates = (3.0, -2.0)
    
    scala> c2.toString()
    res1: String = (3.0, -2.0)



### (B)

Define a method **`isOrigin () : Boolean`** that returns true exactly
when the instance represents the origin of the Cartesian plane, that
is, (0, 0).

Define a method **`isSame (c : Coordinates) : Boolean`** that returns
true exactly when the instane and argument `c` represent the same
coordinates (same x components and same y components).

Sample output:

    scala> val c1 = new Coordinates(0, 0)
    c1: Coordinates = (0.0, 0.0)
    
    scala> val c2 = new Coordinates(3, -2)
    c2: Coordinates = (3.0, -2.0)
    
    scala> c1.isOrigin()
    res1: Boolean = true
    
    scala> c2.isOrigin()
    res2: Boolean = false
    
    scala> c1.isSame(c2)
    res3: Boolean = false
    
    scala> c1.isSame(c1)
    res4: Boolean = true
    
    scala> c2.isSame(c1)
    res5: Boolean = false
    
    scala> c2.isSame(c2)
    res6: Boolean = true
    
    scala> c2.isSame(new Coordinates(3, -2))
    res7: Boolean = true
    
    scala> c2.isSame(new Coordinates(-3, 2))
    res8: Boolean = false


### (C) 

Define a method **`translate (dx : Double, dy : Double) : Coordinates`** that takes arguments `dx` and `dy` and returns a new
`Coordinates` instance that represents the original coordinates but
shifted by `dx` on the x-axis and shifted by `dy` on the y-axis. Of
course, either of `dx` or `dy` could be negative. 

Note that
`translate` does not change the instance &mdash; it returns a new
instance representing the translated coordinates.

Sample output:

    scala> val c = new Coordinates(10, 20)
    c: Coordinates = (10.0, 20.0)
    
    scala> c.translate(100, 200)
    res9: Coordinates = (110.0, 220.0)
    
    scala> c
    res10: Coordinates = (10.0, 20.0)
    
    scala> c.translate(-100, 200)
    res11: Coordinates = (-90.0, 220.0)
    
    scala> c.translate(-100, -200)
    res12: Coordinates = (-90.0, -180.0)
    
    scala> c.translate(-c.xCoord(), -c.yCoord())
    res13: Coordinates = (0.0, 0.0)
    

### (D) 

Define a method **`scale (s : Double) : Coordinates`** that takes a
scaling factor as argument and return a new `Coordinates` instance
representing the current point scaled by that scaling factor. 

Note
that `scale` does not change the instance &mdash; it returns a new
instance representing the scaled coordinates.

Sample output:

    scala> val c = new Coordinates(10, 20)
    c: Coordinates = (10.0, 20.0)
    
    scala> c.scale(2)
    res14: Coordinates = (20.0, 40.0)
    
    scala> c
    res15: Coordinates = (10.0, 20.0)
    
    scala> c.scale(-2)
    res16: Coordinates = (-20.0, -40.0)
    
    scala> c.scale(0.5)
    res17: Coordinates = (5.0, 10.0)
    
    scala> c.scale(-0.5)
    res18: Coordinates = (-5.0, -10.0)
    
    scala> c.scale(0)
    res19: Coordinates = (0.0, 0.0)

    scala> c.scale(1)
    res20: Coordinates = (10.0, 20.0)


### (E) 

Define a method **`rotate (angle : Double) : Coordinates`** that takes
an angle in _degrees_ and returns a new `Coordinates` instance
representing the current point rotated counterclockwise about the
origin by the specified angle. 

Note that `rotate` does not change the
instance &mdash; it returns a new instance representing the rotated
coordinates.

You will find functions `math.sin` and `math.cos` useful, as well as
the constant `math.Pi`. You'll want to remind yourself how to perform
a rotation in the plane.

Sample output:

    scala> val c = new Coordinates(10, 20)
    c: Coordinates = (10.0, 20.0)
    
    scala> c.rotate(90)
    res21: Coordinates = (-20.0, 10.000000000000002)
    
    scala> c
    res22: Coordinates = (10.0, 20.0)
    
    scala> c.rotate(180)
    res23: Coordinates = (-10.000000000000002, -20.0)
    
    scala> c.rotate(270)
    res24: Coordinates = (19.999999999999996, -10.000000000000004)
    
    scala> c.rotate(360)
    res25: Coordinates = (10.000000000000005, 19.999999999999996)
    
    scala> c.rotate(0)
    res26: Coordinates = (10.0, 20.0)
    
    scala> c.rotate(45)
    res27: Coordinates = (-7.071067811865474, 21.213203435596427)


* * * 

## Question 2: New Abstract Representation Expressions

Consider the abstract representation for the small language of
arithmetic expressions we saw in class.  The sample code I gave you
implements abstract class `Exp` and expressions
`EInteger`, `EPlus`, `ETimes`, and `EIf`. 

In this question we will add new expressions to the language.


### (A)

Define a new subclass of expression **`ENeg (val e : Exp)`**
that represents the operation of negating an expression, that is,
`ENeg(e)` is meant to represent operation `-e`. Make
sure you supply an appropriate `toString()` method as well as
an `eval()` method.

Sample output:

    scala> def i (x : Int) : Exp = new EInteger(x)
    i: (x: Int)Exp
    
    scala> val e = new ENeg(i(1))
    e: ENeg = ENeg(EInteger(1))
    
    scala> e.eval()
    res28: Int = -1
    
    scala> (new EPlus(i(1), new ENeg(i(1)))).eval()
    res29: Int = 0
    
    scala> (new ENeg(new EPlus(i(1), i(2)))).eval()
    res30: Int = -3
    
    scala> (new ENeg(i(0))).eval()
    res31: Int = 0


### (B)


Define a new subclass of expression **`EMax (val e1 : Exp, val e2 :
Exp)`** which represents the operation of taking the maximum of two
expressions, that is, `EMax(e1,e2)` represents operation
`max(e1,e2)`. Make sure you supply an appropriate `toString()` method
as well as an appropriate `eval()` method.

Sample output:

    scala> def i (x : Int) : Exp = new EInteger(x)
    i: (x: Int)Exp
    
    scala> (new EMax(i(1), i(2))).eval()
    res32: Int = 2
    
    scala> (new EMax(i(1), new EMax(i(2), i(3)))).eval()
    res33: Int = 3
    
    scala> (new EMax(new EPlus(i(1), i(2)), new EPlus(i(2), i(3)))).eval()
    res34: Int = 5
    
    scala> (new EMax(new EPlus(i(1), i(2)), new ETimes(i(2), i(3)))).eval()
    res35: Int = 6
    
    scala> (new EMax(i(2), i(1))).eval()
    res36: Int = 2
    
    scala> (new EMax(i(3), i(3))).eval()
    res37: Int = 3
    
    scala> (new EMax(i(2), i(-2))).eval()
    res38: Int = 2
    
    scala> (new EMax(i(-3), i(-2))).eval()
    res39: Int = -2


### (C)

Define a new subclass of expression **`EWithin (val e1 : Exp, val e2 :
Exp, val e3 : Exp)`** which represents an operation that takes three
arguments (x, y, z) and returns the closest value to x (possibly x
itself) that lies between y and z (both inclusive). Do not assume that
y is less than z. Make sure you supply an appropriate `toString`
method as well as an appropriate `eval` method.

Sample output:

    scala> def i (x:Int) : Exp = new EInteger(x)
    i: (x: Int)Exp
    
    scala> (new EWithin(i(2), i(1), i(3))).eval()
    res40: Int = 2
    
    scala> (new EWithin(i(0), i(1), i(3))).eval()
    res41: Int = 1
    
    scala> (new EWithin(i(4), i(1), i(3))).eval()
    res42: Int = 3
    
    scala> (new EWithin(new EPlus(i(1), i(2)), new EPlus(i(3), i(4)), new EPlus(i(5), i(6)))).eval()
    res44: Int = 7


* * * 

## Question 3: New Abstract Representation Functionality

We continue with the abstract representation for the small language of
arithmetic expressions we saw in class and in Question 2 above.

Method `toString` in class `Exp` does give us a
string corresponding to the expression, but it's a bit unreadable. I
mean, it is an accurate representation of the tree making up the the
abstract representation, but it would be nice if we could get a more
readable output that is closer to how we usually write arithmetical
expressions. For instance, the abstract representation given by
`new EPlus(new EInteger(3), new EInteger(4))` could be output
as `"3 + 4"`, which would be more readable than the default
`"EPlus(EInteger(3), EInteger(4))"` returned by method
`toString`.

To get that, we first add an abstract method

    def readable () : String

to the `Exp` abstract class, then write an implementation of
method `readable` in every subclass of `Exp`. Don't
forget the subclasses you had to write for Question 2. I'm not going
to be super specific about what `readable` returns, but it
should be something that is readable by someone that knows basic
arithmetic. For example, `max(3 + 4, - (5 * 6))`.

Feel feel to over-parenthesize &mdash; it is a suprisingly difficult
problem to write down an arithmetic expression with a minimal number
of parentheses.
	  
Sample output:

    scala> def i (x:Int) : Exp = new EInteger(x)
    i: (x: Int)Exp
    
    scala> i(5).readable()
    res45: String = 5
    
    scala> val e1 = new EPlus(i(3), i(5))
    e1: EPlus = EPlus(EInteger(3), EInteger(5))
    
    scala> e1.readable()
    res47: String = 3 + 5
    
    scala> val e2 = new ETimes(i(4), e1)
    e2: ETimes = ETimes(EInteger(4), EPlus(EInteger(3), EInteger(5)))
    
    scala> e2.readable()
    res48: String = (4) * (3 + 5)
    
    scala> val e3 = new EMax(e1, e2)
    e3: EMax = EMax(EPlus(EInteger(3), EInteger(5)), ETimes(EInteger(4), EPlus(EInteger(3), EInteger(5))))
    
    scala> e3.readable()
    res50: String = max(3 + 5, (4) * (3 + 5))
    
    scala> val e4 = new EWithin(e1, e2, e3)
    e4: EWithin = EWithin(EPlus(EInteger(3), EInteger(5)), ETimes(EInteger(4), EPlus(EInteger(3), EInteger(5))), EMax(EPlus(EInteger(3), EInteger(5)), ETimes(EInteger(4), EPlus(EInteger(3), EInteger(5)))))

    scala> e4.readable()
    res51: String = within(3 + 5, (4) * (3 + 5), max(3 + 5, (4) * (3 + 5)))
    
    scala> val e5 = new ENeg(e4)
    e5: ENeg = ENeg(EWithin(EPlus(EInteger(3), EInteger(5)), ETimes(EInteger(4), EPlus(EInteger(3), EInteger(5))), EMax(EPlus(EInteger(3), EInteger(5)), ETimes(EInteger(4), EPlus(EInteger(3), EInteger(5))))))
    
    scala> e5.readable()
    res52: String = - (within(3 + 5, (4) * (3 + 5), max(3 + 5, (4) * (3 + 5))))
