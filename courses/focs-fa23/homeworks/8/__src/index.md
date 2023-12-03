<script>
  document.title = 'Homework 8 - FOCS FA23'
</script>

# Homework 8

## Due Date: Sunday, Dec 10, 2023 (23h59)

- This homework is to be done individually. You may discuss problems
with fellow students, but all submitted work must be entirely your
own, and should not be from any other course, present, past, or
future. If you use a solution from another source you must cite it
&mdash; this includes when that source is someone else helping
you.

- **Please do not post your solutions on a public website or a public repository like GitHub.**

- All programming is to be done in Haskell using GHC v9. 

- Code your answers by modifying the file [`homework8.hs`](homework8.hs) provided. Add your **name**, 
your **email address**, and any **remarks** that you wish to make to the instructor 
to the block comment at the head of the file.

- **Please do not change the types in the signature of the
function stubs I provide**. Doing so will make it
impossible to load your code into the testing infrastructure,
and make me unhappy.

- Feel free to define as many helper functions as you need.


## Electronic Submission Instructions

- Start a _fresh_  `ghci` shell.

- Load your homework code via `:load` *path-to-file*`/homework8.hs` to make sure that there are no errors when I will load your code.

- If there are any error, do not submit. I can't test what I can't `:load`.

- When you're ready to submit, log into our <a href="https://chat.rpucella.net">class chat</a>
and click <b>Submit File</b> in the profile menu (your initials in the upper right corner).

***

In this homework, we are going to program with streams via infinite lists in Haskell, the way we saw
in class. (See the [second part of the notes](../../streams.pdf).)

To help you get started, I added the primitives we saw in class to `homework8.hs`:

    cst :: a -> [a]
    cst k = k : (cst k)
    
    fby :: [a] -> [a] -> [a]
    fby s t = (head s) : t
    
    map2 :: (a -> b -> c) -> [a] -> [b] -> [c]
    map2 f s t = f (head s) (head t) : map2 f (tail s) (tail t)
    
    drop :: [a] -> [a]
    drop s = tail s


I also added the streams we built in class, as well as some helpful stream transformers:

    from :: Int -> [Int]
    from k = k : from (k + 1)
    
    add1 :: [Int] -> [Int]
    add1 s = map (\x -> x + 1) s

    add :: [Int] -> [Int] -> [Int]
    add s t = map2 (\x y -> x + y) s t

    nats :: [Int]
    nats = from 0
    
    evens :: [Int]
    evens = map (\x -> x * 2) nats
    
    odds :: [Int]
    odds = add1 evens
    
    triangles :: [Int]
    triangles = 0 : add (add1 nats) triangles

    fib :: [Int]
    fib = 0 : 1 : add fib (tail fib)
    
    psums :: [Int] -> [Int]
    psums s = (head s) : add (tail s) (psums s)
    
    primes :: [Int]
    primes = 
      let notdivides k n = n `mod` k /= 0
          sieve s = head s : sieve (filter (notdivides (head s)) (tail s))
      in sieve (from 2)
    

Because streams are infinite, you cannot just evaluate a steams at the shell and print it. (Try it, you'll see what I mean.)

You have two approaches. Either you use function `take n s` that returns the first `n` elements of stream `s` as a list:

    ghci> take 20 fib
    [0,1,1,2,3,5,8,13,21,34,55,89,144,233,377,610,987,1597,2584,4181]

or you use function `nth n s` that returns the `n`th element (0-indexed) of stream `s`:

    ghci> nth 20 fib
    6765

* * *

## Question 1: Basic Stream Manipulation


### (A)

Code a stream function **`mult`** with type **`[Int] -> [Int] -> [Int]`** where `mult` produces the stream obtained by multiplying the corresponding elements of the input streams:

> `mult < a1 a2 a3 ... > < b1 b2 b3 ... > = < a1*b1 a2*b2 a3*b3 ... >`


    > take 20 (mult nats nats)
    [0,1,4,9,16,25,36,49,64,81,100,121,144,169,196,225,256,289,324,361]

    > take 20 (mult nats evens)
    [0,2,8,18,32,50,72,98,128,162,200,242,288,338,392,450,512,578,648,722]

    > take 20 (mult evens evens)
    [0,4,16,36,64,100,144,196,256,324,400,484,576,676,784,900,1024,1156,1296,1444]

    > take 20 (mult evens mod7)
    [0,2,8,18,32,50,72,0,16,36,60,88,120,156,0,30,64,102,144,190]


### (B)

Code a stream function **`mult2`** with
type **`[Int] -> [Int] -> [Int]`** where `mult2` produces the
stream obtained by multiplying each element of the first input stream with the _first_ and _second_ elements of the second input stream in alternance:

> `mult2 < a1 a2 a3 a4 a5 ... > < b1 b2 b3 ... > = < a1*b1 a2*b2 a3*b1 a4*b2 a5*b1 ... >`


    > take 20 (mult2 nats mod7)
    [0,1,0,3,0,5,0,7,0,9,0,11,0,13,0,15,0,17,0,19]

    > take 20 (mult2 mod7 nats)
    [0,1,0,3,0,5,0,0,0,2,0,4,0,6,0,1,0,3,0,5]

    > take 20 (mult2 odds nats)
    [0,3,0,7,0,11,0,15,0,19,0,23,0,27,0,31,0,35,0,39]

    > take 20 (mult2 nats odds)
    [0,3,2,9,4,15,6,21,8,27,10,33,12,39,14,45,16,51,18,57]


**Hint:** Create a stream containing the first two elements of the second stream repeated infinitely.


### (C)

Code a stream function **`stutter`** with
type **`[Int] -> [Int]`** where `stutter` produces the
stream obtained by repeating twice every element of the input :

> `stutter < a1 a2 a3 a4 a5 ... > < a1 a1 a2 a2 a3 a3 a4 a4 a5 a5 ... >`


    > take 20 (stutter nats)
    [0,0,1,1,2,2,3,3,4,4,5,5,6,6,7,7,8,8,9,9]

    > take 20 (stutter evens)
    [0,0,2,2,4,4,6,6,8,8,10,10,12,12,14,14,16,16,18,18]

    > take 20 (stutter mod7)
    [0,0,1,1,2,2,3,3,4,4,5,5,6,6,0,0,1,1,2,2]



### (D)

Code a stream function **`stairs`** with
type **`[Int] -> [Int]`** where `stairs` produces the
stream obtained by repeating the first element of the input stream once, the second element of the input stream twice, the third element of the input stream three times, and more generally the nth element of the input stream n times:

> `stairs < a1 a2 a3 a4 a5 ... > = < a1 a2 a2 a3 a3 a3 a4 a4 a4 a4 a5 a5 a5 a5 a5 ... >`


    > take 30 (stairs nats)
    [0,1,1,2,2,2,3,3,3,3,4,4,4,4,4,5,5,5,5,5,5,6,6,6,6,6,6,6,7,7]

    > take 30 (stairs evens)
    [0,2,2,4,4,4,6,6,6,6,8,8,8,8,8,10,10,10,10,10,10,12,12,12,12,12,12,12,14,14]

    > take 30 (stairs primes)
    [2,3,3,5,5,5,7,7,7,7,11,11,11,11,11,13,13,13,13,13,13,17,17,17,17,17,17,17,19,19]



**Hint:** You may need a helper function to create a list of `n` copies of a given value.


### (E)

Code a stream function **`running_max`** with
type **`[Int] -> [Int]`** which produces
the stream consisting of the maximum value in the input stream up to the current point in the output stream:
	    
> `running_max < a1 a2 a3 ... > = < max(a1) max(a1,a2) max(a1,a2,a3) ... >`

    > take 20 (running_max nats)
    [0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19]

    > take 20 (running_max mod7)
    [0,1,2,3,4,5,6,6,6,6,6,6,6,6,6,6,6,6,6,6]

    > take 20 (running_max (add mod7 nats))
    [0,2,4,6,8,10,12,12,12,12,13,15,17,19,19,19,19,20,22,24]


* * * 

## Question 2: Numerical Analysis


Many problems in numerical analysis involve finding better and better
approximations to a desired value (such as &pi; or _e_,
or the solution to a differential equation) until the difference
between successive approximations gets small enough that we decide
that we have converged and report that we have found the value we're
looking for. 


Infinite streams help in this context because we can represent the 
successive approximations to a value by a stream of those
approximations.

In this question, we will mostly be dealing with streams of floats (aka, floating point numbers) of
type `[Double]`. You may have to recreate functions such as `add` that work on floats. Haskell treats
integers and floats differently, and while some functions can work on both, and sometimes Haskell
will implicitly treat an integer as a float (especially if it's a literal like `1`), functions such
as division `/` will fail to type check if both arguments are not floats. This will often happen
when using an identifier with type `Int` in an expression expecting a `Double`. 

If you need to convert an integer to a float explicitly, you can use the built-in function
`fromIntegral`.

### (A)

How do you compute the value of &pi;? One way is to
use [trigonometry](https://en.wikipedia.org/wiki/Approximations_of_%CF%80). An old approach uses the fact that tan (<sup>&pi;</sup>&frasl;<sub>6</sub>) = <sup>1</sup>&frasl;<sub>&Sqrt;3</sub>, that is:

> &pi; = 6 arctan (<sup>1</sup>&frasl;<sub>&Sqrt;3</sub>)

and using the Taylor expansion of arctan and some algebra, you can derive:

> &pi; = &Sqrt;12 (1 - <sup>1</sup>&frasl;<sub>(3 &centerdot; 3)</sub> + (<sup>1</sup>&frasl;<sub>5 &centerdot; 3<sup>2</sup></sub>) - (<sup>1</sup>&frasl;<sub>7 &centerdot; 3<sup>3</sup></sub>) + (<sup>1</sup>&frasl;<sub>9 &centerdot; 3<sup>4</sup></sub>) - ...)

This is basically infinite sum, but it can be approximated by
the stream of partial sums

> < &Sqrt;12 (1) &nbsp;&nbsp;&nbsp; &Sqrt;12 (1 - (<sup>1</sup>&frasl;<sub>3 &centerdot; 3</sub>)) &nbsp;&nbsp;&nbsp;  &Sqrt;12 (1 - (<sup>1</sup>&frasl;<sub>3 &centerdot; 3</sub>) + (<sup>1</sup>&frasl;<sub>5 &centerdot; 3<sup>2</sup></sub>)) &nbsp;&nbsp;&nbsp;  &Sqrt;12 (1 - (<sup>1</sup>&frasl;<sub>3 &centerdot; 3</sub>) + (<sup>1</sup>&frasl;<sub>5 &centerdot; 3<sup>2</sup></sub>) - (<sup>1</sup>&frasl;<sub>7 &centerdot; 3<sup>3</sup></sub>)) &nbsp;&nbsp;&nbsp; &Sqrt;12 (1 - (<sup>1</sup>&frasl;<sub>3 &centerdot; 3</sub>) + (<sup>1</sup>&frasl;<sub>5 &centerdot; 3<sup>2</sup></sub>) - (<sup>1</sup>&frasl;<sub>7 &centerdot; 3<sup>3</sup></sub>) + (<sup>1</sup>&frasl;<sub>9 &centerdot; 3<sup>4</sup></sub>)) ... >

which gets closer and closer to &pi;.

Code a stream **`spi`** with type **`[Double]`**
which is the of 
approximations to &pi; above. (I'm using `spi` since `pi` is already a built-in constant.)

    > take 10 spi
    [3.4641016151377544,3.0792014356780038,3.156181471569954,3.1378528915956805,3.1426047456630846,3.141308785462
    883,3.1416743126988376,3.141568715941784,3.141599773811506,3.1415905109380797]

    > nth 1000 spi
    3.1415926535897944



### (B)

Newton's method is a way to find a zero of a polynomial with one
unknown, such as 10x+20. In fact, it works for any differentiable function of one argument.
Recall that a zero of a function f(x) is a
value v that makes f(v)=0. To compute &radic;10,
for instance, we can use Newton's method to find a zero of
x<sup>2</sup>-10. To compute &radic;20 we find a zero of
x<sup>3</sup>-20, and so on.

What is Newton's method? It says that to find a zero of f(x), we
need the derivative of f, written f'(x), as well as an initial
guess x<sub>0</sub>. The guess doesn't have to be a good guess. Once we have a
guess x<sub>0</sub>, we can improve the guess by computing x<sub>1</sub>, x<sub>2</sub>, x<sub>3</sub>, ..., as follows:

> x<sub>n+1</sub> = x<sub>n</sub> - <sup>f(x<sub>n</sub>)</sup>&frasl;<sub>f'(x<sub>n</sub>)</sub>  &nbsp;&nbsp;&nbsp;&nbsp;  (*)

where x<sub>i</sub> is the i<sup>th</sup> guess. Each
guess gets closer and closer to a zero of f(x).

Code a stream function **`newton`** with type
**`(Double -> Double) -> (Double -> Double) -> Double -> [Double]`** where `newton f df guess` produces
the stream of approximations of guesses given by 
(*) for function `f` with its
derivative `df` and an initial
guess `guess`.

Functions `psqrt` and `dsqrt` in the examples below are defined in `homework8.hs`, where `psqrt v`
is the polynomial x<sup>2</sup>-v (for which a zero if &Sqrt;v and `dsqrt v` is the derivative of
`psqrt v`, namely the polynomial 2x.


    > take 10 (newton (\x -> 3 * x - 2) (\x -> 3) 1)
    [1.0,0.6666666666666667,0.6666666666666667,0.6666666666666667,0.6666666666666667,0.6666666666666667,0.6666666666666667,0.6666666666666667,0.6666666666666667,0.6666666666666667]

    > take 10 (newton (psqrt 9) (dsqrt 9) 1)
    [1.0,5.0,3.4,3.023529411764706,3.00009155413138,3.000000001396984,3.0,3.0,3.0,3.0]

    > take 10 (newton (psqrt 2) (dsqrt 2) 1)
    [1.0,1.5,1.4166666666666667,1.4142156862745099,1.4142135623746899,1.4142135623730951,1.414213562373095,1.4142135623730951,1.414213562373095,1.4142135623730951]

    > take 10 (newton (psqrt 144) (dsqrt 144) 1)
    [1.0,72.5,37.24310344827586,20.554795555442038,13.7802299905638,12.11499150672641,12.000545730742438,12.000000012408687,12.0,12.0]

    > take 10 (newton (psqrt 123) (dsqrt 123) 1)
    [1.0,62.0,31.991935483870968,17.91832720752661,12.391403638657124,11.158819944876512,11.090745427603231,11.090536508377186,11.090536506409418,11.090536506409418]

        -- verification: good enough!
    > 11.090536506409418 * 11.090536506409418
    123.00000000000001



### (C)

Our function `newton` in (B) requires not only the function for which we want to find a zero, but also
the derivative. How do we compute derivative?

Computing derivatives is itself an approximate process. More
specifically, the value of the derivative of a function f at a
point x<sub>0</sub> can be approximated by the stream:

> < f(x<sub>0</sub> + 1) - f(x<sub>0</sub>)  &nbsp;&nbsp;&nbsp;
<sup>f(x<sub>0</sub> + &frac12;) - f(x<sub>0</sub>)</sup>&frasl; <sub>&frac12;</sub>   &nbsp;&nbsp;&nbsp;
<sup>f(x<sub>0</sub> + &frac13;) - f(x<sub>0</sub>)</sup>&frasl; <sub>&frac13;</sub>   &nbsp;&nbsp;&nbsp;
... >  &nbsp;&nbsp;&nbsp;&nbsp;  (**)

where the n<sup>th</sup> term in the sequence is:

> <sup>f(x<sub>0</sub> + 1/n) - f(x<sub>0</sub>)</sup>&frasl; <sub>1/n</sub>

Code a stream function **`derivative`** with type
**`(Double -> Double) -> Double -> [Double]`**
where `derivative f x` produces the  stream of approximations of the
derivative of `f` at `x` given by (**).


        -- Derivative of x^2 is 2x.
    > take 10 (derivative (\x -> x * x) 1)
    [3.0,2.5,2.333333333333333,2.25,2.1999999999999997,2.1666666666666683,2.1428571428571423,2.125,2.1111111111111125,2.100000000000002]

    > nth 1000 (derivative (\x -> x * x) 1)
    2.000999000999173

    > take 10 (derivative (\x -> x * x) 5)
    [11.0,10.5,10.333333333333329,10.25,10.200000000000014,10.166666666666679,10.142857142857174,10.125,10.111111111111068,10.09999999999998]

    > nth 1000 (derivative (\x -> x * x) 5)
    10.000999001001347

        -- Convergence is pretty bad.
    > nth 2000 (derivative (\x -> x * x) 5)
    10.000499750128029

        -- Derivative of x^3 + x is 3x^2 + 1
    > take 10 (derivative (\x -> x * x * x + x) 3)
    [38.0,32.75,31.111111111111143,30.3125,29.840000000000053,29.527777777777743,29.306122448979586,29.140625,29.012345679012398,28.910000000000053]

    > nth 1000 (derivative (\x -> x * x * x + x) 3)
    28.008992006997033


     
**For you to think about:** If you had `derivative`, how could you modify function `newton` so that it doesn't require you to pass the derivative of the function for which you want to find a zero? Note that function `derivative` computes the derivative at a given point, how could you turn it into a function that computes the *derivative function*?


### (D)

All of the above questions return streams yielding better and
better approximations to a desired value. By picking out an
element of the stream far enough down, we can find a good
approximation to the value we want.

But how far do we go? Approximations get closer and closer to the
value they approximate, which means that the difference between
successive approximations gets smaller and smaller. So we can look into
the stream and try to find the first approximation which differs from
the next approximation by a small enough margin to decide
that we have converged to the desired value, and take that as the
desired approximation. Since we can make the margin as small as we
want, we can get approximation that are as close as we want to the
actual value we seek. (All of this assuming that the
stream actually converges to the desired value. What
happens if the stream does _not_ converge?)

Code a stream function **`limit`** with type **`Double -> [Double]`**
where `limit epsilon s` produces the stream 
of elements of `s` that differ from their subsequent
element by less than `epsilon` (in absolute value).

    > take 10 (limit 0.00001 (derivative (\x -> x * x) 5))
    [10.00316455696084,10.003154574132097,10.003144654087876,10.003134796237806,10.0031249999995,10.003115264797547,10.003105590062432,10.003095975231844,10.00308641975225,10.003076923078602]

    > take 10 (limit 0.0000001 (derivative (\x -> x * x) 5))
    [10.000316255522684,10.00031615556123,10.00031605562522,10.000315955766794,10.000315855968275,10.000315756220818,10.000315656569455,10.000315556966818,10.000315457408142,10.000315357942604]

    > take 10 (limit 0.00000001 (derivative (\x -> x * x) 5))
    [10.000100240525654,10.000100190334681,10.000100120108897,10.000100110111749,10.000100059999866,10.000100050004175,10.000100040006174,10.000100030030438,10.000100009959798,10.000099999984968]

    > take 10 (limit 0.000000001 (derivative (\x -> x * x) 5))
    [10.000035395554434,10.000035254607944,10.000035217323742,10.000035173984365,10.000034968585913,10.0000348467442,10.000034810302157,10.000034676417672,10.00003457928949,10.000034555412874]

    > take 10 (limit 0.0000000001 (derivative (\x -> x * x) 5))
    [10.000022545623082,10.00002238874023,10.000022349691456,10.000022280409333,10.00002210025059,10.000022074364473,10.000022038315404,10.000021931556276,10.000021879262366,10.000021819614275]




