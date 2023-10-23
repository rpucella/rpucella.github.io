<script>
  document.title = 'Homework 5 - FOCS FA23'
</script>

# Homework 5

## Due Date: Sunday Oct 29, 2023 (23h59)

- This homework is to be done individually. You may discuss problems
with fellow students, but all submitted work must be entirely your
own, and should not be from any other course, present, past, or
future. If you use a solution from another source you must cite it
&mdash; this includes when that source is someone else helping
you.

- **Please do not post your solutions on a public website or a public repository like GitHub.**

- All programming is to be done in Haskell using GHC v9. 

- Code your answers by modifying the file [`homework5.hs`](homework5.hs) provided. Add your **name**, your **email address**, and any **remarks** that you
wish to make to the instructor to the block comment at the head of the file.

- **Please do not change the types in the signature of the
function stubs I provide**. Doing so will make it
impossible to load your code into the testing infrastructure,
and make me unhappy.

- Feel free to define as many helper functions as you need.


## Electronic Submission Instructions

- Start a _fresh_  `ghci` shell.

- Load your homework code via `:load` *path-to-file*`/homework5.hs` to make sure that there are no errors when I will load your code.

- If there are any error, do not submit. I can't test what I can't `:load`.

- When you're ready to submit, log into our <a href="https://chat.rpucella.net">class chat</a>
and click <b>Submit File</b> in the profile menu (your initials in the upper right corner).


***

## Question 1: Arithmetic for Turing machines

In this question, you will construct Turing machines. You can test your Turing machines with the
code you wrote in last homework. you can copy and paste the implementation of your Turing machines functions (A)-(D) from Homework 4 into `homework5.hs`, replacing the stubs that I provided.

For submission purposes, I ask you to define those Turing machines as constants with a fixed name
given below in each question. They are initialized with a dummy Turing machine that you should
replace with your own definition.


### (A)

Construct a total Turing machine **`tm_not`** that accepts the
language consisting of all strings over the alphabet `{0, 1, #}` of the
form _`#`u_`#`_v_, where _u_ and _v_ are nonempty strings over `{0, 1}`
all of the same length such that _v_ is the pointwise NOT of _u:_ if
the _i <sup>th</sup>_ bit of _u_ is _b_ then the _i <sup>th</sup>_ bit of _v_ is _¬b_, where
¬0 is 1 and ¬1 is 0.

Sample output not showing printed configurations:

    > run tm_not "#0#1"
    "ACCEPT"
    
    > run tm_not "#000#111"
    "ACCEPT"
    
    > run tm_not "#010#101"
    "ACCEPT"
    
    > run tm_not "#010101#101010"
    "ACCEPT"
    
    > run tm_not "#000111000#111000111"
    "ACCEPT"
    
    > run tm_not "#0#1#01"
    "REJECT"
    
    > run tm_not "#0#11"
    "REJECT"
    
    > run tm_not "#00#1"
    "REJECT"
    
    > run tm_not "#1#1"
    "REJECT"
    
    > run tm_not "#0#0"
    "REJECT"
    
    > run tm_not "#010#100"
    "REJECT"
    
    > run tm_not "#010#001"
    "REJECT"


### (B)

Construct a total Turing machine **`tm_or`** that accepts the
language consisting of all strings over the alphabet `{0, 1, #}` of the
form _`#`u_`#`_v_`#`_w_ where _u_, _v_, and _w_ are nonempty strings over
`{0, 1}` all of the same length such that _w_ is the pointwise OR of
_u_ and _v:_ if the _i <sup>th</sup>_ bit of _u_ is _b1_ or the _i <sup>th</sup>_ bit of _v_
    is _b2_ then the _i <sup>th</sup>_ bit of _w_ is _b1 ∨ b2_, where _x ∨ y_ is 1
when either one of _x_ or _y_ is 1 (possibly both), and 0 otherwise.

Sample output not showing printed configurations:

    > run tm_or "#0#0#0"
    "ACCEPT"
    
    > run tm_or "#0#1#1"
    "ACCEPT"
    
    > run tm_or "#1#0#1"
    "ACCEPT"
    
    > run tm_or "#1#1#1"
    "ACCEPT"
    
    > run tm_or "#101#010#111"
    "ACCEPT"
    
    > run tm_or "#1010#0011#1011"
    "ACCEPT"
    
    > run tm_or "#0011#0000#0011"
    "ACCEPT"
    
    > run tm_or "#0101#0101#0101"
    "ACCEPT"
    
    > run tm_or "#0#0"
    "REJECT"
    
    > run tm_or "#0#0#1#1"
    "REJECT"
    
    > run tm_or "#00#11#00"
    "REJECT"
    
    > run tm_or "#001#001#000"
    "REJECT"
    
    > run tm_or "#001#001#011"
    "REJECT"


### (C)

Construct a total Turing machine **`tm_increment`** that accepts the
language consisting of all strings over the alphabet `{0, 1, #}` of the
form _`#`u_`#`_v_ where _u_ and _v_ are nonempty
strings over `{0, 1}` all of the same length such that _u + 1 = v_ when
viewed as _binary numbers_.

(Brownie points if you can construct the Turing machine so that _u_
and _v_ need not be of the same length, but that's not required.)

Sample output not showing printed configurations:

    > run tm_increment "#0#1"
    "ACCEPT"
    
    > run tm_increment "#00#01"
    "ACCEPT"
    
    > run tm_increment "#000#001"
    "ACCEPT"
    
    > run tm_increment "#010#011"
    "ACCEPT"
    
    > run tm_increment "#011#100"
    "ACCEPT"
    
    > run tm_increment "#100#101"
    "ACCEPT"
    
    > run tm_increment "#101#110"
    "ACCEPT"
    
    > run tm_increment "#110#111"
    "ACCEPT"
    
    > run tm_increment "#101010#101011"
    "ACCEPT"
    
    > run tm_increment "#101111#110000"
    "ACCEPT"
    
    > run tm_increment "#0"
    "REJECT"
    
    > run tm_increment "#0#1#1"
    "REJECT"
    
    > run tm_increment "#0#11"
    "REJECT"
    
    > run tm_increment "#01#0"
    "REJECT"
    
    > run tm_increment "#1#1"
    "REJECT"
    
    > run tm_increment "#0#0"
    "REJECT"
    
    > run tm_increment "#010#101"
    "REJECT"

**Hint**: how would you do 00101010101011 + 1 by hand, say? 

      00101010101011
    +              1
      --------------

Go from right to left, and distinguish situations where you have a carry from situations where
you don't using different states.


### (D)

Construct a total Turing machine **`tm_add`** that accepts the
language consisting of all strings over the alphabet `{0, 1, #}` of the
form _`#`u_`#`_v_`#`_w_ where _u_, _v_, and _w_ are nonempty strings over
`{0, 1}` all of the same length such that _u + v = w_ when viewed as _binary numbers_. 

(Brownie points if you can construct the Turing machine so that _u_, _v_, and _w_ need not be of the
same length, but that's not required.)

Sample output not showing printed configurations:

    > run tm_add "#0#0#0"
    "ACCEPT"
    
    > run tm_add "#0#1#1"
    "ACCEPT"
    
    > run tm_add "#1#0#1"
    "ACCEPT"
    
    > run tm_add "#01#01#10"
    "ACCEPT"
    
    > run tm_add "#101#010#111"
    "ACCEPT"
    
    > run tm_add "#01110#00111#10101"
    "ACCEPT"
    
    > run tm_add "#0#0"
    "REJECT"
    
    > run tm_add "#0#0#0#0"
    "REJECT"
    
    > run tm_add "#0#0#1"
    "REJECT"
    
    > run tm_add "#00#11#10"
    "REJECT"
    
    > run tm_add "#001#001#011"
    "REJECT"
    
    > run tm_add "#001#001#100"
    "REJECT"



***

## Question 2: Basic register machine programs

In class, I showed how you could translate a program written in some assembly language for a very simple register machine into a Turing machine. In this question and the next, we're going to play a little with that assembly language.

A program is a sequence of instructions. Execution proceeds by starting from the first instruction, executing it, then going to the following instructions, etc. The one exception to this is when the instruction is a `JUMP` instruction which sends execution to another instruction, or the `DEC` instruction when the value in the register is zero.

During execution, the program has access to a set of _registers_, each of which can hold a natural number (integer greater than or equal to 0). Registers have an initial value of 0, except for registers that are given a value when a program is run.

Fundamentally, registers are numbered -- the first register is 0, the second register is 1, and so on. For easy of writing programs, you can name those registers that you are going to use, and refer to them using strings.

The instructions available to you are:

<table style="margin-left: 24px;">
<tr><td><tt>INC r</tt></td><td>&nbsp;&nbsp;&nbsp;increment the content of register <tt>r</tt> and go to the next instruction</td></tr>
<tr><td><tt>DEC (r, addr)</tt></td><td> &nbsp;&nbsp;&nbsp;if the content of register <tt>r</tt> is zero, go to the instruction at address <tt>addr</tt>; otherwise, decrement the content of register <tt>r</tt> and go to the next instruction</td></tr>
<tr><td><tt>JUMP addr</tt></td><td> &nbsp;&nbsp;&nbsp;go to the instruction at address <tt>addr</tt></td></tr>
<tr><td><tt>TRUE</tt></td><td> &nbsp;&nbsp;&nbsp;stop execution and return true</td></tr>
<tr><td><tt>FALSE</tt></td><td> &nbsp;&nbsp;&nbsp;stop execution and return false</td></tr>
<tr><td><tt>EQUAL (r1, r2)</tt></td><td> &nbsp;&nbsp;&nbsp;stop execution and return true when the content of register <tt>r1</tt> and <tt>r2</tt> are equal and false otherwise.</td></tr>
</table>

These are the instructions we saw in class, except that to simplify your life I added an instruction `EQUAL (r1, r2)` to check for equality of values in two registers. (It's easy enough to write a sequence of instructions to do the same task if you think using `EQUAL` is cheating.) Registers are strings, and addresses are natural numbers, and refer to the position of the instruction in the program.

Instructions are defined as a Haskell _algebraic datatype_. We haven't seen those yet, but all you need to know is the instructions above are _constructors_ for the `Instruction` type. Thus, `INC "X"` is a value of the `Instruction` type, as is `TRUE` and `EQUAL ("X", "Y")`. 

A register machine program is created by calling `RMP` with two arguments, a list of register names (the first name becomes the name of register 0, the second name the name of register 1, etc), and a list of instructions, each of type `Instruction`. The address of an instruction is simply the index of the instruction in the list.

Here is a program that gives name `X` to the first register, and decrements it until it reaches zero, at which point the program stops and returns true:

    p_reset :: RMP
    p_reset = RMP ["X"] [
        -- 0 loop
        DEC ("X", 2),
        JUMP 0,
        -- 2 done
        TRUE
      ]

(Recall that `--` is a line comment in Haskell. I find it useful to comment where instructions live to calculate jumps. Of course, those need to be updated if I add instructions. Can you think of a better way of doing this?) 

To run the program, you call `runRMP` passing in the program and also a list with an initial values for the first N registers. For instance:

    ghci> runRMP p_reset [10]
    0000: DEC (X, 0002)        10 
    0001: JUMP 0000             9 
    0000: DEC (X, 0002)         9 
    0001: JUMP 0000             8 
    0000: DEC (X, 0002)         8 
    0001: JUMP 0000             7 
    0000: DEC (X, 0002)         7 
    0001: JUMP 0000             6 
    0000: DEC (X, 0002)         6 
    0001: JUMP 0000             5 
    0000: DEC (X, 0002)         5 
    0001: JUMP 0000             4 
    0000: DEC (X, 0002)         4 
    0001: JUMP 0000             3 
    0000: DEC (X, 0002)         3 
    0001: JUMP 0000             2 
    0000: DEC (X, 0002)         2 
    0001: JUMP 0000             1 
    0000: DEC (X, 0002)         1 
    0001: JUMP 0000             0 
    0000: DEC (X, 0002)         0 
    0002: TRUE                  0 
    True

As a program executes, you see the address of the instruction currently being executed, the instruction itself, and the content of the registers, in order.

As a second example, consider the following program that takes the content of the first register and transfers it to the second register, adding to what's already there:

    p_transfer :: RMP
    p_transfer = RMP ["X", "Y"] [
        -- 0 loop
        DEC ("X", 3),
        INC "Y",
        JUMP 0,
        -- 3 done
        TRUE
      ]

with sample execution:

    ghci> runRMP p_transfer [7, 10]
    0000: DEC (X, 0003)         7  10 
    0001: INC Y                 6  10 
    0002: JUMP 0000             6  11 
    0000: DEC (X, 0003)         6  11 
    0001: INC Y                 5  11 
    0002: JUMP 0000             5  12 
    0000: DEC (X, 0003)         5  12 
    0001: INC Y                 4  12 
    0002: JUMP 0000             4  13 
    0000: DEC (X, 0003)         4  13 
    0001: INC Y                 3  13 
    0002: JUMP 0000             3  14 
    0000: DEC (X, 0003)         3  14 
    0001: INC Y                 2  14 
    0002: JUMP 0000             2  15 
    0000: DEC (X, 0003)         2  15 
    0001: INC Y                 1  15 
    0002: JUMP 0000             1  16 
    0000: DEC (X, 0003)         1  16 
    0001: INC Y                 0  16 
    0002: JUMP 0000             0  17 
    0000: DEC (X, 0003)         0  17 
    0003: TRUE                  0  17 
    True

As a third example, consider the following program, which checks if the content of the second register is equal to the content of the first register + 1. If you call the second register Z and the first register X, then this program checks that Z = X + 1:

    p_succ :: RMP
    p_succ = RMP ["X", "Z"] [
         INC "X",
         EQUAL ("X", "Z")
      ]

with sample executions:

    ghci> runRMP p_succ [4, 5]
    0000: INC X                 4   5 
    0001: EQUAL (X, Z)          5   5 
    True
    ghci> runRMP p_succ [4, 6]
    0000: INC X                 4   6 
    0001: EQUAL (X, Z)          5   6 
    False
    
Note you can declare more registers than you provide values when calling `runRMP`. Those registers are temporary registers (or helper registers), initialized to value 0, which you can used for whatever you want. Some of the more interesting programs cannot be written without using temporary registers.

This register machine is based on the abacus model described in the first chapter of Lambek's [_Programs, Grammars, Arguments_](http://www.math.mcgill.ca/barr/papers/pga.pdf). You should definitely read it for inspiration in how to address the problems below.

To practice, you may want to first write two programs whose structure will come in handy in later
programs: (1) one that copies the content of the first register into the second register (no matter
what is in the second register), so that at the end of execution both the first register and the
second register have the same value; and (2) one that swaps the content of the first and second
registers, so that at the end of execution, the first register holds the value that was in the
second register, and the second register holds the value that was in the first register (hint: move
the content of the first register to a temporary register first.) In both cases, the programs can
just return true.

The sample outputs below only show you the final true/false result, because obviously.

### (A)

Code a program **`p_add`** that returns true if the sum of the values in the first two registers is equal to the value in the third register.

    > runRMP p_add [0, 3, 3]
    True
    
    > runRMP p_add [3, 0, 3]
    True
    
    > runRMP p_add [3, 2, 5]
    True
    
    > runRMP p_add [2, 3, 5]
    True
    
    > runRMP p_add [2, 3, 4]
    False
    
    > runRMP p_add [2, 3, 6]
    False
    
    > runRMP p_add [2, 3, 0]
    False



### (B)

Code a program **`p_sub`** that returns true if the "positive difference" between the first register and the second register is equal to the value in the third register.

The positive difference of X and Y, written X &dotminus; Y, is defined to be X - Y if X &ge; Y, and 0 otherwise.

    > runRMP p_sub [3, 0, 3]
    True
    
    > runRMP p_sub [3, 1, 2]
    True
    
    > runRMP p_sub [3, 2, 1]
    True
    
    > runRMP p_sub [3, 3, 0]
    True
    
    > runRMP p_sub [3, 4, 0]
    True
    
    > runRMP p_sub [15, 2, 13]
    True
    
    > runRMP p_sub [3, 1, 3]
    False
    
    > runRMP p_sub [3, 1, 1]
    False
    
    > runRMP p_sub [3, 1, 0]
    False
    
    > runRMP p_sub [3, 5, 2]
    False
    
    > runRMP p_sub [3, 5, 3]
    False


### (C)

Code a program **`p_max`** that returns true if the value in the third register is equal to the maximum of the values in the first two registers.

    > runRMP p_max [3, 5, 5]
    True
    
    > runRMP p_max [5, 5, 5]
    True
    
    > runRMP p_max [5, 3, 5]
    True
    
    > runRMP p_max [123, 100, 123]
    True
    
    > runRMP p_max [5, 3, 3]
    False
    
    > runRMP p_max [5, 3, 0]
    False
    
    > runRMP p_max [5, 3, 4]
    False
    
    > runRMP p_max [5, 3, 6]
    False




### (D)

Code a program **`p_diff`** that returns true if the value in the third register is the absolute difference between the values in the first two registers. 

The absolute difference between X and Y is the absolute value |X - Y| of the difference between X and Y, and is obviously always non-negative.

    > runRMP p_diff [5, 3, 2]
    True
    
    > runRMP p_diff [3, 5, 2]
    True
    
    > runRMP p_diff [5, 5, 0]
    True
    
    > runRMP p_diff [5, 15, 10]
    True
    
    > runRMP p_diff [15, 5, 10]
    True
    
    > runRMP p_diff [15, 5, 5]
    False
    
    > runRMP p_diff [15, 5, 15]
    False
    
    > runRMP p_diff [15, 5, 6]
    False
    
    > runRMP p_diff [15, 5, 4]
    False
    
    > runRMP p_diff [15, 5, 0]
    False

**Hint**: |X - Y| = X &dotminus; Y if X &ge; Y and = Y &dotminus; X if X < Y. Equivalently, |X - Y| = (X &dotminus; Y) + (Y &dotminus; X).


* * *

## Question 3: Multiplication in register machines

This question asks you to work with multiplication. You can do it the brute force way, by replicating the code for multiplicaton in each of these questions and adapting it appopriately, or you can be a little bit clever and figure out how to write the code for multiplication once and use that in your programs below. Remember that the instructions in a program is just a Haskell list of values of type `Instruction`, and you can manipulate that list however you want to produce the final program.

Again, the sample outputs below only show you the final true/false result.


### (A)

Code a program **`p_mult`** that returns true if the value of the third register is equal to the product of the values in the first two registers.

    > runRMP p_mult [0, 5, 0]
    True
    
    > runRMP p_mult [1, 5, 5]
    True
    
    > runRMP p_mult [2, 5, 10]
    True
    
    > runRMP p_mult [3, 5, 15]
    True
    
    > runRMP p_mult [4, 5, 20]
    True
    
    > runRMP p_mult [5, 5, 25]
    True
    
    > runRMP p_mult [5, 4, 20]
    True
    
    > runRMP p_mult [5, 3, 15]
    True
    
    > runRMP p_mult [5, 2, 10]
    True
    
    > runRMP p_mult [5, 1, 5]
    True
    
    > runRMP p_mult [5, 0, 0]
    True
    
    > runRMP p_mult [3, 5, 3]
    False
    
    > runRMP p_mult [3, 5, 5]
    False
    
    > runRMP p_mult [3, 5, 14]
    False
    
    > runRMP p_mult [3, 5, 16]
    False

**Hint**: X * Y = Y + ... + Y  (X times).


### (B) 

Code a program **`p_double`** that returns true if the value of the second register is equal to double the value in the first register.

    > runRMP p_double [0, 0]
    True
    
    > runRMP p_double [1, 2]
    True
    
    > runRMP p_double [2, 4]
    True
    
    > runRMP p_double [5, 10]
    True
    
    > runRMP p_double [5, 9]
    False
    
    > runRMP p_double [5, 11]
    False
    
    > runRMP p_double [0, 2]
    False
    
    > runRMP p_double [2, 0]
    False
    
    > runRMP p_double [2, 2]
    False


**Hint**: take a temporary register, put 2 in it, and use multiplication.




### (C)

Code a program **`p_square`** that returns true if the value of the second register is equal to the square of the value of the first register.

    > runRMP p_square [0, 0]
    True
    
    > runRMP p_square [1, 1]
    True
    
    > runRMP p_square [2, 4]
    True
    
    > runRMP p_square [3, 9]
    True
    
    > runRMP p_square [11, 121]
    True
    
    > runRMP p_square [11, 120]
    False
    
    > runRMP p_square [11, 122]
    False

**Hint**: take a temporary register, put X in it, and use multiplication.


### (D)

Code a program **`p_cube`** that returns true if the value of the second register is _n<sup>3</sup>_ when the value of the first register is _n_.

    > runRMP p_cube [0, 0]
    True
    
    > runRMP p_cube [1, 1]
    True
    
    > runRMP p_cube [2, 7]
    False
    
    > runRMP p_cube [2, 8]
    True
    
    > runRMP p_cube [3, 27]
    True
    
    > runRMP p_cube [5, 125]
    True
    
    > runRMP p_cube [5, 120]
    False
    
    > runRMP p_cube [5, 130]
    False
    
    > runRMP p_cube [5, 5]
    False

**Hint**: take two temporary registers, put X in both, and use multiplication twice.

