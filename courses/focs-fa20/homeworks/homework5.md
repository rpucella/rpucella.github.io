# Homework 5

## Due Date: Thursday, November 5, 2020 (23h59)

- This homework is to be done individually. You may discuss problems
with fellow students, but all submitted work must be entirely your
own, and should not be from any other course, present, past, or
future. If you use a solution from another source you must cite it
&mdash; this includes when that source is someone else helping
you.

- **Please do not post your solutions on a
public website or a public repository like GitHub.**

- All programming is to be done in OCaml v4.

- Code your answers by modifying the file [`homework5.ml`](homework5.ml) provided. Add your **name**, your **email address**, and any **remarks** that you
wish to make to the instructor to the block comment at the head of the file.

- **Please do not change the types in the signature of the
function stubs I provide**. Doing so will make it
impossible to load your code into the testing infrastructure,
and make me unhappy.

- Feel free to define helper functions if you need them.


## Electronic Submission Instructions

- Start a _fresh_  OCaml shell.

- Load your homework code via `#use "homework5.ml";;` to make sure that there are no errors when I will load your code.

- If there are any error, do not submit. I can't test what I can't `#use`.

- When you're ready to submit, send an email with your file
`homework5.ml` as an attachment to `olin.submissions@gmail.com` with
subject _Homework 5 submission_.

* * *

In class, I showed how you could translate a program written in some assembly language for a very simple CPU into a Turing machine. This shows that we can roughly build up standard programming languages atop a Turing machine by doing the same kind of compilation down to assembly.

In this homework, we're going to play a little bit with that assembly language.

A program is a sequence of instructions. Execution proceeds by starting from the first instruction, executing it, then going to the following instructions, etc. The one exception to this is when the instruction is a `JUMP` instruction which sends execution to another instruction, or the `DEC` instruction when the value in the register is zero.

During execution, the program has access to a set of _registers_, each of which can hold a natural number. Registers have an initial value of 0, except for registers that are given a value when the program is run.

Fundamentally, registers are numbered -- the first register is 0, the second register is 1, and so on. Working with registers as numbers is a pain, so you are allowed to give a name to a register via the `REGISTER` instructions.

The instructions available to you are:

- `INC rgstr` : increment the content of register `rgstr` and go to the next instruction
- `DEC (rgstr, lbl)` : if the content of register `rgstr` is zero, go to the instruction at label `lbl`; otherwise, decrement the content of register `rgstr` and go to the next instruction
- `JUMP lbl` : go to the instruction at label `lbl`
- `TRUE` : stop execution and return true
- `FALSE` : stop execution and return false
- `EQUAL (rgstr1, rgstr2)` : stop execution and return true when the content of register `rgstr1` and `rgstr2` are equal
- `PRINT rgstr` : print the content of register `rgstr`   (useful for debugging)
- `LABEL lbl` : declare that the instruction following this one can be referred to by label `lbl`
- `REGISTER (rgstr, pos)` : declare that the name `rgstr` can be used to refer to the register at position `pos` (0, 1, 2, ...)

Instructions are defined as an OCaml _algebraic datatype_. We haven't seen those yet, but all you need to know is the instructions above are _constructors_ for the `instruction` type. Thus, `INC "X"` is a value of the `instruction` type, as is `TRUE` and `LABEL "done"`. You can [find a bit more information about algebraic datatypes here](https://www.cs.cornell.edu/courses/cs3110/2019sp/textbook/data/algebraic_data_types.html).

A program is just an OCaml list of values of type `instruction`, where each instruction is one of the above, with strings naming registers and labels, and integers naming positions.

Here is a program that gives name `X` to the first register, and decrements it until it reaches zero, at which point the program stops and returns true:

    let p_reset = [REGISTER ("X", 0);
                   LABEL "loop";
                   DEC ("X", "done");
                   JMP "loop";
                   LABEL "done";
                   TRUE]

To run the program, you call `run` passing in the program and also a list with an initial values for the first N registers. For instance:

    # run p_reset [10];;
    ----------------------------------------------------------------------
    0000 DEC (0, 2)
    0001 JUMP 0
    0002 TRUE
    ----------------------------------------------------------------------
    0000: 10
    0001: 9
    0000: 9
    0001: 8
    0000: 8
    0001: 7
    0000: 7
    0001: 6
    0000: 6
    0001: 5
    0000: 5
    0001: 4
    0000: 4
    0001: 3
    0000: 3
    0001: 2
    0000: 2
    0001: 1
    0000: 1
    0001: 0
    0000: 0
    0002: 0
    - : bool = true

The first thing `run` does is print out the program it is executing, expanded out so that you see the address of every instructions, and labels and registers are replaced by their addresses and their position, respectively.

The execution proceeds, and `run` gives you the address of each instruction it is executing, along with the content of the registers before the instruction executes.

As a second example, consider the following program that takes the content of the first register and transfers it to the second register, adding to what's there:

    let p_transfer = [REGISTER ("X", 0);
                      REGISTER ("Y", 1);
                      LABEL "loop";
                      DEC ("X", "done");
                      INC "Y";
                      JUMP "loop";
                      LABEL "done";
                      TRUE]

with sample execution:

    # run p_transfer [7; 10];;
    ----------------------------------------------------------------------
    0000 DEC (0, 3)
    0001 INC 1
    0002 JUMP 0
    0003 TRUE
    ----------------------------------------------------------------------
    0000: 7 10
    0001: 6 10
    0002: 6 11
    0000: 6 11
    0001: 5 11
    0002: 5 12
    0000: 5 12
    0001: 4 12
    0002: 4 13
    0000: 4 13
    0001: 3 13
    0002: 3 14
    0000: 3 14
    0001: 2 14
    0002: 2 15
    0000: 2 15
    0001: 1 15
    0002: 1 16
    0000: 1 16
    0001: 0 16
    0002: 0 17
    0000: 0 17
    0003: 0 17
    - : bool = true


As a third example, consider the following program, which checks if the content of the second register is equal to the content of the first register + 1. If you call the second register Z and the first register X, then this program checks that Z = X + 1:

    let p_succ = [REGISTER ("X", 0);
                  REGISTER ("Z", 1);
                  INC "X";
                  EQUAL ("X", "Z")]

with sample executions:

    # run p_succ [4; 5];;
    ----------------------------------------------------------------------
    0000 INC 0
    0001 DEC (0, 4)
    0002 DEC (1, 5)
    0003 JUMP 1
    0004 DEC (1, 6)
    0005 FALSE
    0006 TRUE
    ----------------------------------------------------------------------
    0000: 4 5
    0001: 5 5
    0002: 4 5
    0003: 4 4
    0001: 4 4
    0002: 3 4
    0003: 3 3
    0001: 3 3
    0002: 2 3
    0003: 2 2
    0001: 2 2
    0002: 1 2
    0003: 1 1
    0001: 1 1
    0002: 0 1
    0003: 0 0
    0001: 0 0
    0004: 0 0
    0006: 0 0
    - : bool = true
    
    # run p_succ [4; 6];;
    ----------------------------------------------------------------------
    0000 INC 0
    0001 DEC (0, 4)
    0002 DEC (1, 5)
    0003 JUMP 1
    0004 DEC (1, 6)
    0005 FALSE
    0006 TRUE
    ----------------------------------------------------------------------
    0000: 4 6
    0001: 5 6
    0002: 4 6
    0003: 4 5
    0001: 4 5
    0002: 3 5
    0003: 3 4
    0001: 3 4
    0002: 2 4
    0003: 2 3
    0001: 2 3
    0002: 1 3
    0003: 1 2
    0001: 1 2
    0002: 0 2
    0003: 0 1
    0001: 0 1
    0004: 0 1
    0005: 0 0
    - : bool = false
    
Note that `EQUAL ("X", "Z")` gets expanded out into simpler primitives before execution. I'll let you figure out what that transformation is. (You don't need to know the transformation in order to use `run`, obviously.)

Note you can declare more registers than you provide values when calling `run. Those registers are temporary registers, initialized to value 0, which you can used for whatever you want.

This abstract CPU is based on the abacus model described in the first chapter of Lambek's [_Programs, Grammars, Arguments_](http://www.math.mcgill.ca/~barr/papers/pga.pdf). You should definitely read it for inspiration in how to address the problems below.

* * *


## Question 1: Basic Programs

Note that except for (A)-(B), the sample outputs below only show you the final true/false result.

### (A)

Code a program **`p_copy`** that copies the content of the first register into the second register (no matter what is in the second register) and then returns true. Thus, at the end of execution, both the first register and the second register should have the same value.

Sample output:

    # run p_copy [2; 3];;
    0000: 2 3 0
    ...
    0009: 2 2 0
    - : bool = true
    
    # run p_copy [5; 1];;
    0000: 5 1 0
    ...
    0009: 5 5 0
    - : bool = true
    
    # run p_copy [0; 7];;
    0000: 0 7 0
    ...
    0009: 0 0 0
    - : bool = true

The sample output above only shows you the registers content at the first instruction and the last. Obviously, your programs will be different from mine, so both addresses and the number of registers you use will be different. I only care about the first two registers in your last output line.

### (B)

Code a program **`p_swap`** that swaps the content of the first and second registers and then returns true. Thus, at the end of execution, the first register should hold the value that was in the second register, and the second register should hold the value that was in the first register. 

Sample output:

    # run p_swap [3; 5];;
    0000: 3 5 0
    ...
    0011: 5 3 0
    - : bool = true
    
    # run p_swap [5; 3];;
    0000: 5 3 0
    ...
    0011: 3 5 0
    - : bool = true
    
    # run p_swap [0; 3];;
    0000: 0 3 0
    ...
    0011: 3 0 0
    - : bool = true
    
    # run p_swap [3; 0];;
    0000: 3 0 0
    ...
    0011: 0 3 0
    - : bool = true
    
    # run p_swap [5; 5];;
    0000: 5 5 0
    ...
    0011: 5 5 0
    - : bool = true

The sample output above only shows you the registers content at the first instruction and the last. Obviously, your programs will be different from mine, so both addresses and the number of registers you use will be different. I only care about the first two registers in your last output line.

**Hint**: move the content of the first register to a temporary register first.


### (C)

Code a program **`p_plus`** that returns true if the sum of the values in the first two registers is equal to the value in the third register.

Sample output:

    # run p_plus [0; 3; 3];;
    - : bool = true
    
    # run p_plus [3; 0; 3];;
    - : bool = true
    
    # run p_plus [3; 2; 5];;
    - : bool = true
    
    # run p_plus [2; 3; 5];;
    - : bool = true
    
    # run p_plus [2; 3; 4];;
    - : bool = false
    
    # run p_plus [2; 3; 6];;
    - : bool = false
    
    # run p_plus [2; 3; 0];;
    - : bool = false



### (D)

Code a program **`p_sub`** that returns true if the "positive difference" between the first register and the second register is equal to the value in the third register.

The positive difference of X and Y, written X &dotminus; Y, is defined to be X - Y if X &ge; Y, and 0 otherwise.

Sample output:

    # run p_sub [3; 0; 3];;
    - : bool = true
    
    # run p_sub [3; 1; 2];;
    - : bool = true
    
    # run p_sub [3; 2; 1];;
    - : bool = true
    
    # run p_sub [3; 3; 0];;
    - : bool = true
    
    # run p_sub [3; 4; 0];;
    - : bool = true
    
    # run p_sub [15; 2; 13];;
    - : bool = true
    
    # run p_sub [3; 1; 3];;
    - : bool = false
    
    # run p_sub [3; 1; 1];;
    - : bool = false
    
    # run p_sub [3; 1; 0];;
    - : bool = false
    
    # run p_sub [3; 5; 2];;
    - : bool = false
    
    # run p_sub [3; 5; 3];;
    - : bool = false


### (E)

Code a program **`p_max`** that returns true if the value in the third register is equal to the maximum of the values in the first two registers.

Sample output:

    # run p_max [3; 5; 5];;
    - : bool = true
    
    # run p_max [5; 5; 5];;
    - : bool = true
    
    # run p_max [5; 3; 5];;
    - : bool = true
    
    # run p_max [123; 100; 123];;
    - : bool = true
    
    # run p_max [5; 3; 3];;
    - : bool = false
    
    # run p_max [5; 3; 0];;
    - : bool = false
    
    # run p_max [5; 3; 4];;
    - : bool = false
    
    # run p_max [5; 3; 6];;
    - : bool = false




### (F)

Code a program **`p_diff`** that returns true if the value in the third register is the absolute difference between the values in the first two registers. The absolute difference between X and Y is simply |X - Y|.

Sample output:

    # run p_diff [5; 3; 2];;
    - : bool = true
    
    # run p_diff [3; 5; 2];;
    - : bool = true
    
    # run p_diff [5; 5; 0];;
    - : bool = true
    
    # run p_diff [5; 15; 10];;
    - : bool = true
    
    # run p_diff [15; 5; 10];;
    - : bool = true
    
    # run p_diff [15; 5; 5];;
    - : bool = false
    
    # run p_diff [15; 5; 15];;
    - : bool = false
    
    # run p_diff [15; 5; 6];;
    - : bool = false
    
    # run p_diff [15; 5; 4];;
    - : bool = false
    
    # run p_diff [15; 5; 0];;
    - : bool = false

**Hint**: |X - Y| = X &dotminus; Y if X &ge; Y and = Y &dotminus; X if X < Y. Equivalently, |X - Y| = (X &dotminus; Y) + (Y &dotminus; X).


* * *

## Question 2: Multiplication

This question asks you to work with multiplication. You can do it the brute force way, by replicating the code for multiplicaton in each of these questions and adapting it appopriately, or you can be a little bit clever and figure out how to write the code for multiplication once and use that in each of the programs in (A) - (C). Remember that a program is just an OCaml list of values of type `instruction`, and you can manipulate these lists however you want to produce the final program.

Note that the sample outputs below only show you the final true/false result.


### (A)

Code a program **`p_times`** that returns true if the value of the third register is equal to the product of the values in the first two registers.

Sample output:

    # run p_times [0; 5; 0];;
    - : bool = true
    
    # run p_times [1; 5; 5];;
    - : bool = true
    
    # run p_times [2; 5; 10];;
    - : bool = true
    
    # run p_times [3; 5; 15];;
    - : bool = true
    
    # run p_times [4; 5; 20];;
    - : bool = true
    
    # run p_times [5; 5; 25];;
    - : bool = true
    
    # run p_times [5; 4; 20];;
    - : bool = true
    
    # run p_times [5; 3; 15];;
    - : bool = true
    
    # run p_times [5; 2; 10];;
    - : bool = true
    
    # run p_times [5; 1; 5];;
    - : bool = true
    
    # run p_times [5; 0; 0];;
    - : bool = true
    
    # run p_times [3; 5; 3];;
    - : bool = false
    
    # run p_times [3; 5; 5];;
    - : bool = false
    
    # run p_times [3; 5; 14];;
    - : bool = false
    
    # run p_times [3; 5; 16];;
    - : bool = false



**Hint**: X * Y = Y + ... + Y  (X times).


### (B)

Code a program **`p_square`** that returns true if the value of the second register is equal to the square of the value of the first register.

Sample output:

    # run p_square [0; 0];;
    - : bool = true
    
    # run p_square [1; 1];;
    - : bool = true
    
    # run p_square [2; 4];;
    - : bool = true
    
    # run p_square [3; 9];;
    - : bool = true
    
    # run p_square [11; 121];;
    - : bool = true
    
    # run p_square [11; 120];;
    - : bool = false
    
    # run p_square [11; 122];;
    - : bool = false



### (C)

Code a program **`p_square_x_2`** that returns true if the value of the second register is _n<sup>2</sup> + n + 2_ when the value of the first register is _n_.

Sample output:

    # run p_square_x_2 [0; 2];;
    - : bool = true
    
    # run p_square_x_2 [1; 4];;
    - : bool = true
    
    # run p_square_x_2 [2; 7];;
    - : bool = false
    
    # run p_square_x_2 [2; 8];;
    - : bool = true
    
    # run p_square_x_2 [3; 14];;
    - : bool = true
    
    # run p_square_x_2 [11; 134];;
    - : bool = true
    
    # run p_square_x_2 [11; 133];;
    - : bool = false
    
    # run p_square_x_2 [11; 135];;
    - : bool = false
    
    # run p_square_x_2 [11; 11];;
    - : bool = false



* * *


## Question 3: Division

Code a program **`p_half`** that returns true if the second register holds value &lfloor; _n/2_ &rfloor; when the first register holds value _n_. Remember that &lfloor; _x_ &rfloor; is the largest integer less than or equal to _x_ -- for instance, &lfloor; 2 &rfloor; = 2 and &lfloor; 2.5 &rfloor; = 2.

Sample output (only showing the final true/false result):

    # run p_half [2; 1];;
    - : bool = true
    
    # run p_half [3; 1];;
    - : bool = true
    
    # run p_half [4; 2];;
    - : bool = true
    
    # run p_half [5; 2];;
    - : bool = true
    
    # run p_half [6; 3];;
    - : bool = true
    
    # run p_half [7; 3];;
    - : bool = true
    
    # run p_half [8; 4];;
    - : bool = true
    
    # run p_half [150; 75];;
    - : bool = true
    
    # run p_half [10; 4];;
    - : bool = false
    
    # run p_half [10; 6];;
    - : bool = false
    
    # run p_half [10; 10];;
    - : bool = false
    
**Hint**: This is the trickiest one. Observe that &lfloor; _n/2_ &rfloor; is the largest number _k_ such that _k_ &le; _n/2_, that is, the largest number _k_ such that _2k_ &le; _n_. It is therefore the _smallest_ number _k_ such that _2(k + 1)_ > _n_. So how do you find the smallest number _k_ such that _2(k + 1)_ > _n_? You start with _k_ = 0 and increase _k_ by one repeatedly, testing to see if _2(k + 1)_ > _n_ until you hit the first _k_ that does. It's not fast, but the goal is not speed here.


