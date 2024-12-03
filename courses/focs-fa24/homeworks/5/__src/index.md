<script>
  document.title = 'Homework 5'
</script>

# Homework 5

## Due Date: Tuesday, November 19, 2024 (23h59)

- This homework is to be done individually. You may discuss problems
with fellow students, but all submitted work must be entirely your
own, and should not be from any other course, present, past, or
future. If you use a solution from another source you must cite it
&mdash; this includes when that source is someone else helping
you.

- Please do not post your solutions on a public website or a public repository (including GitHub).

- All programming is to be done in Python 3.

- Code your answers by modifying the file [`homework5.py`](homework5.py) provided. Add your **name**, your **email address**, and any **remarks** that you
wish to make to the instructor to the block comment at the head of the file.

- Please do not change the "signature" (number and interpretation of arguments) of the
functions. Doing so will make it impossible to load your code into the testing infrastructure, and
make me unhappy.

- Feel free to define helper functions if you need them.


**Electronic Submission Instructions**

- Make your your file `homework5.py` loads without error in a fresh Python 3 shell.

- Submit `homework5.py` on [Canvas](https://canvas.olin.edu).

* * *

## Register Machines


In class, we talked about the abacus model, and programs written as diagrams where you have
locations containing pebbles and "instructions" such as adding a pebble to a location and removing a
pebble from a location.  This is described in the first two section of Chapter 1 of Lambek's
[_Programs, Grammars, Arguments_](https://www.math.mcgill.ca/barr/papers/pga.pdf). You should
definitely read it for inspiration in how to address the problems below.


I also described how you can turn that into a simple "assembly language" for a simple register
machine with an arbitrary number of registers that can hold natural numbers, and simple instructions
like "add one to a register", or "remove one from a register", along with some simple branching
instructions.

In this homework, we're going to play a little with that assembly language.

A program for a register machine is a sequence of instructions. Execution proceeds by starting from
the first instruction, executing it, then going to the following instructions, etc. The one
exception to this is when the instruction is a `JUMP` instruction which sends execution to another
instruction, or the `DEC` instruction when the value in the register is zero.

During execution, the program has access to a set of _registers_, each of which can hold a natural
number. Registers have an initial value of 0 if they're not given a value when the program is run.program is run.

Registers are numbered -- there is a register at 0, at 1, etc. You refer to a register by its number. 

The instructions available to you are:

- `INC` *`reg`* : increment the content of register *`reg`* and go to the next instruction
- `DEC` *`reg`* *`addr`* : if the content of register *`reg`* is zero, go to the instruction at address *`addr`*; otherwise, decrement the content of register *`reg`* and go to the next instruction
- `JUMP` *`addr`* : go to the instruction at address  *`addr`*
- `STOP` *`reg`* : stop execution and return value of register *`reg`*

An instruction is represented in python by a tuple with the first element a string representing the instruction, and any subsequent item the arguments to the instruction, for example:

    ("inc", 0)
    ("dec", 1, 37)
    ("jump", 47)
    ("stop", 2)
    
Instructions are case insensitive.

A program is represented by a list of instruction tuples. The address of each instruction is just
the index of the instruction in the list, so that the address of the first instruction is 0, the
second instruction 1, etc. Execution always starts at address 0.

Here is a simple program that empties out the content of register 0. It returns the final value of
register 0, which should of course always be 0:

    P_RESET = [
    
       # address 0
       ("dec", 0, 2),
       ("jump", 0),
       
       # address 2
       ("stop", 0)
    ]

Here is another program that copies the content of register 0 into register 1, without changing the final content of register 0. It uses a temporary register 2 for doing this.

    P_COPY = [
    
        # address 0
        ("dec", 0, 4),
        ("inc", 1),
        ("inc", 2),
        ("jump", 0),
        
        # address 4
        ("dec", 2, 7),
        ("inc", 1),
        ("jump", 4),
        
        # address 7
        ("stop", 1)
    ]

To run a program, you call `RM().run()` passing in the program and also a list with an initial values for the first N registers. For instance:

    >>> RM().run(P_RESET, [5])
    0000  5
    0001  4
    0000  4
    0001  3
    0000  3
    0001  2
    0000  2
    0001  1
    0000  1
    0001  0
    0000  0
    0002  0
    ----  0
    0

Execution proceeds by showing you the address of every instruction being executed and the state of registers before the instruction executes, step by step until the final `STOP`, with the final state of registers being shown preceded by `----` and the content of the register specified by the `STOP` returned.

Here are two sample executions of `P_COPY`:

    >>> RM().run(P_COPY, [6])
    0000  6
    0001  5
    0002  5  1
    0003  5  1  1
    0000  5  1  1
    0001  4  1  1
    0002  4  2  1
    0003  4  2  2
    0000  4  2  2
    0001  3  2  2
    0002  3  3  2
    0003  3  3  3
    0000  3  3  3
    0001  2  3  3
    0002  2  4  3
    0003  2  4  4
    0000  2  4  4
    0001  1  4  4
    0002  1  5  4
    0003  1  5  5
    0000  1  5  5
    0001  0  5  5
    0002  0  6  5
    0003  0  6  6
    0000  0  6  6
    0004  0  6  6
    0005  0  6  5
    0006  1  6  5
    0004  1  6  5
    0005  1  6  4
    0006  2  6  4
    0004  2  6  4
    0005  2  6  3
    0006  3  6  3
    0004  3  6  3
    0005  3  6  2
    0006  4  6  2
    0004  4  6  2
    0005  4  6  1
    0006  5  6  1
    0004  5  6  1
    0005  5  6  0
    0006  6  6  0
    0004  6  6  0
    0007  6  6  0
    ----  6  6  0
    6
    
    >>> RM().run(P_COPY, [6, 2])
    0000  6  2
    0001  5  2
    0002  5  3
    0003  5  3  1
    0000  5  3  1
    0001  4  3  1
    0002  4  4  1
    0003  4  4  2
    0000  4  4  2
    0001  3  4  2
    0002  3  5  2
    0003  3  5  3
    0000  3  5  3
    0001  2  5  3
    0002  2  6  3
    0003  2  6  4
    0000  2  6  4
    0001  1  6  4
    0002  1  7  4
    0003  1  7  5
    0000  1  7  5
    0001  0  7  5
    0002  0  8  5
    0003  0  8  6
    0000  0  8  6
    0004  0  8  6
    0005  0  8  5
    0006  1  8  5
    0004  1  8  5
    0005  1  8  4
    0006  2  8  4
    0004  2  8  4
    0005  2  8  3
    0006  3  8  3
    0004  3  8  3
    0005  3  8  2
    0006  4  8  2
    0004  4  8  2
    0005  4  8  1
    0006  5  8  1
    0004  5  8  1
    0005  5  8  0
    0006  6  8  0
    0004  6  8  0
    0007  6  8  0
    ----  6  8  0
    8



***


## Question 1: Basic Register Machine programs


The sample outputs below show only the final register values and the returned value. You can use as
many temporary as you need, and you don't need to use the same number I do.

For all of these programs, please ensure that the input registers are restored to their initial
value, and that any temporary registers have 0 in them at the end of execution.

### (A)

Code a program **`P_PLUS`** that computes the sum of the values in registers 0 and 1 into register 2
and returns that sum.

    >>> rm = RM()

    >>> rm.run(P_PLUS, [0, 0])
    ----  0  0  0  0  0
    0
    
    >>> rm.run(P_PLUS, [0, 3])
    ----  0  3  3  0  0
    3
    
    >>> rm.run(P_PLUS, [3, 0])
    ----  3  0  3  0  0
    3
    
    >>> rm.run(P_PLUS, [3, 1])
    ----  3  1  4  0  0
    4
    
    >>> rm.run(P_PLUS, [3, 2])
    ----  3  2  5  0  0
    5
    
    >>> rm.run(P_PLUS, [3, 3])
    ----  3  3  6  0  0
    6
    
    >>> rm.run(P_PLUS, [3, 4])
    ----  3  4  7  0  0
    7
    
    >>> rm.run(P_PLUS, [3, 5])
    ----  3  5  8  0  0
    8
    
    >>> rm.run(P_PLUS, [8, 9])
    ----  8  9  17  0  0
    17


### (B)

Code a program **`P_MINUS`** that computes the "positive difference" between registers 0 and 1 into
register 2 and returns that difference.

The positive difference of X and Y, written X &dotminus; Y, is defined to be X - Y if X &ge; Y, and 0 otherwise.

    >>> rm = RM()

    >>> rm.run(P_MINUS, [0, 0])
    ----  0  0  0  0  0
    0

    >>> rm.run(P_MINUS, [0, 3])
    ----  0  3  0  0  0
    0
    
    >>> rm.run(P_MINUS, [3, 0])
    ----  3  0  3  0  0
    3
    
    >>> rm.run(P_MINUS, [3, 1])
    ----  3  1  2  0  0
    2
    
    >>> rm.run(P_MINUS, [3, 2])
    ----  3  2  1  0  0
    1
    
    >>> rm.run(P_MINUS, [3, 3])
    ----  3  3  0  0  0
    0
    
    >>> rm.run(P_MINUS, [3, 0])
    ----  3  0  3  0  0
    3
    
    >>> rm.run(P_MINUS, [3, 4])
    ----  3  4  0  0  0
    0
    
    >>> rm.run(P_MINUS, [3, 5])
    ----  3  5  0  0  0
    0
    
    >>> rm.run(P_MINUS, [9, 8])
    ----  9  8  1  0  0
    1

    >>> rm.run(P_MINUS, [8, 9])
    ----  8  9  0  0  0
    0



### (C)

Code a program **`P_MAX`** that computes the maximum value stored in registers 0 and 1 into register 2 and returns that maximum.

    >>> rm = RM()

    >>> rm.run(P_MAX, [0, 0])
    ----  0  0  0  0  0
    0
    
    >>> rm.run(P_MAX, [0, 3])
    ----  0  3  3  0  0
    3

    >>> rm.run(P_MAX, [3, 0])
    ----  3  0  3  0  0
    3
    
    >>> rm.run(P_MAX, [3, 1])
    ----  3  1  3  0  0
    3
    
    >>> rm.run(P_MAX, [3, 2])
    ----  3  2  3  0  0
    3
    
    >>> rm.run(P_MAX, [3, 3])
    ----  3  3  3  0  0
    3
    
    >>> rm.run(P_MAX, [3, 4])
    ----  3  4  4  0  0
    4
    
    >>> rm.run(P_MAX, [3, 5])
    ----  3  5  5  0  0
    5
    
    >>> rm.run(P_MAX, [9, 8])
    ----  9  8  9  0  0
    9
    
    >>> rm.run(P_MAX, [8, 9])
    ----  8  9  9  0  0
    9




* * *

## Question 2: Symbolic Register Machine programs

Register Machine programs are a bit of a pain to write because 

- you have to address registers by numbers which is error prone and difficult to read, and 
- you need to explicitly manage the address of the various jumps (in the `DEC` instruction and the
  `JUMP` instruction) which means that you whenever you add a new instruction in the middle of a
  program you may need to change bunch of jump target addresses. 

How did you approach writing programs in Question 1? Personally, I use placeholders for addresses,
and only when then program is written do I replace those placeholders by the addresses that I can
read off the final program. Similarly, instead of explicitly using register numbers, I use
register names, and changes those names to numbers right before execution.

Let's formalize that. We'll introduce the notion of a *symbolic program* that uses register names
instead of numbers, and that uses symbolic labels instead of addresses. Of course, before execution,
those names will be resolved into actual register numbers and instruction addresses so that execution
proceeds as previously defined. We're not changing how programs execute. We're only making it easier
for us to write programs.

For writing symbolic programs, we'll introduce three new *pseudo-instructions*: 

- `REGISTER` *`name`* *`reg`*
- `TEMPORARY` *`name`*
- `LABEL` *`name`*

These are not executable instructions for our Register Machines. They are a kind of annotation that
get used to transform the program before running it.

Pseudo-instruction `REGISTER name reg` says that you can use `name` to refer to register number
`reg`. So that `REGISTER X 0` indicates that you can write `INC X` to mean the same as `INC 0`.

Pseudo-instruction `TEMPORARY name` says that you can use `name` to refer to a temporary register,
whose number if not known in advance but guaranteed to be different than any other register. (Since
we do not assign initial values to temporary register, we don't actually care what number they
are. If we did, we'd use `REGISTER` to give the register's name a specific number.)

Pseudo-instruction `LABEL name` indicates that the address of the next (real) instruction can be
refered to by `name`. So we can write `JUMP foo` to jump to the address of the instruction following
`LABEL foo`.

Here is program `P_COPY` from above rewritten as a symbolic program:

    P_COPY_sym = [
        ("register", "X", 0),
        ("register", "Y", 1),
        ("temporary", "T")
        
        ("label", "empty_x"),
        ("dec", "X", "refresh_x"),
        ("inc", "Y"),
        ("inc", "T"),
        ("jump", "empty_x"),
        
        ("label", "refresh_x")
        ("dec", "T", "done"),
        ("inc", "X"),
        ("jump", "refresh_x"),
        
        ("label", "done"),
        ("stop", "Y")
    ]

(If we had a dedicated syntax, we could write this more legibly as:

    register X 0
    register Y 1
    temporary T
    
    empty_x:
       dec X refresh_x
       inc Y
       inc T
       jump empty_x
       
    refresh_x:
       dec T done
       inc X
       jump refresh_x
       
    done:
       stop Y
       
Keep this in mind for next time.)

    
### (A) 

Code a method `resolve_registers()` in class `RM` that takes a symbolic program and returns a *new*
(possibly symbolic) program in which:

1. For every `REGISTER name reg` in the program, all uses of register name `name` in `INC`, `DEC`,
   and `STOP` instructions are replaced by register number `reg`.
2. For every `TEMPORARY name` in the program, all uses of register name `name` in `INC`, `DEC`, and
   `STOP` instructions are replaced by some register number that is different from all other.
   register numbers used in the program (including other numbers assigned to other `TEMPORARY` registers)
3. Every occurrence of `REGISTER name reg` or `TEMPORARY name` is removed.

I've given you a method `max_register()` that takes a program and returns the highest numbered
register used in the program. You know, in case it's useful?

Sample outputs (not unique due to `TEMPORARY`):

    >>> p = [("register", "X", 0), ("inc", "X"), ("stop", "X")]
    >>> RM().resolve_registers(p)
    [('inc', 0), ('stop', 0)]

    >>> p = [("register", "X", 0), ("register", "Y", 1), ("inc", "X"), ("dec", "Y", 3), ("stop", "X"), ("stop", "Y")]
    >>> RM().resolve_registers(p)
    [('inc', 0), ('dec', 1, 3), ('stop', 0), ('stop', 1)]

    >>> p = [("register", "X", 2), ("temporary", "T"), ("temporary", "U"), ("inc", "T"), ("dec", "U", 3), ("stop", "T"), ("stop", "U")]
    >>> RM().resolve_registers(p)
    [('inc', 3), ('dec', 4, 3), ('stop', 3), ('stop', 4)]

    >>> p = [("temporary", "T1"), ("temporary", "T2"), ("temporary", "T3"), ("temporary", "T4"), ("temporary", "T5"), ("inc", "T1"), ("inc", "T2"), ("inc", "T3"), ("inc", "T4"), ("inc", "T5"), ("stop", "T1")]
    >>> RM().resolve_registers(p)
    [('inc', 1), ('inc', 2), ('inc', 3), ('inc', 4), ('inc', 5), ('stop', 1)]

    >>> p = [("register", "X", 0), ("temporary", "T"), ("label", "L1"), ("dec", "X", "L1"), ("stop", "T")]
    >>> RM().resolve_registers(p)
    [('label', 'L1'), ('dec', 0, 'L1'), ('stop', 1)]


### (B)

Code a method `resolve_labels()` in class `RM` that takes a symbolic program and returns a *new*
(possibly symbolic) program in which:

1. For every `LABEL name` in the program, all uses of address `name` in `DEC` and `JUMP`
   instructions are replaced by *the address of the instruction following `LABEL name`*.
2. Every occurrence of `LABEL name` is removed.

Note that for the purpose of determining the address of instructions in a program,
pseudo-instructions like `REGISTER`, `TEMPORARY`, and `LABEL` do not count (since they do not appear
in programs that are actually executed).

    >>> p = [("label", "L"), ("dec", 0, "L"), ("jump", "L")]
    >>> RM().resolve_labels(p)
    [('dec', 0, 0), ('jump', 0)]
    
    >>> p = [("label", "L"), ("dec", 0, "L"), ("jump", "L"), ("label", "M"), ("dec", 0, "M"), ("jump", "M")]
    >>> RM().resolve_labels(p)
    [('dec', 0, 0), ('jump', 0), ('dec', 0, 2), ('jump', 2)]
    
    >>> p = [("label", "L"), ("label", "M"), ("dec", 0, "L"), ("jump", "L"), ("dec", 0, "M"), ("jump", "M")]
    >>> RM().resolve_labels(p)
    [('dec', 0, 0), ('jump', 0), ('dec', 0, 0), ('jump', 0)]
    
    >>> p = [("label", "L"), ("dec", 0, "L"), ("label", "M"), ("jump", "L"), ("dec", 0, "M"), ("jump", "M")]
    >>> RM().resolve_labels(p)
    [('dec', 0, 0), ('jump', 0), ('dec', 0, 1), ('jump', 1)]
    
    >>> p = [("register", "X", 0), ("label", "L"), ("dec", "X", "L"), ("jump", "L"), ("label", "M"), ("dec", "X", "M"), ("jump", "M")]
    >>> RM().resolve_labels(p)
    [('register', 'X', 0), ('dec', 'X', 0), ('jump', 0), ('dec', 'X', 2), ('jump', 2)]
    
    >>> p = [("register", "X", 0), ("temporary", "T"), ("label", "L"), ("dec", "X", "L"), ("jump", "L"), ("label", "M"), ("dec", "X", "M"), ("jump", "M")]
    >>> RM().resolve_labels(p)
    [('register', 'X', 0), ('temporary', 'T'), ('dec', 'X', 0), ('jump', 0), ('dec', 'X', 2), ('jump', 2)]
    
    >>> p = [("label", "L"), ("register", "X", 0), ("temporary", "T"), ("dec", "X", "L"), ("jump", "L"), ("label", "M"), ("dec", "X", "M"), ("jump", "M")]
    >>> RM().resolve_labels(p)
    [('register', 'X', 0), ('temporary', 'T'), ('dec', 'X', 0), ('jump', 0), ('dec', 'X', 2), ('jump', 2)]


### (C)

Code a symbolic program **`P_DIFF`** that computes the absolute difference between the values in
registers 0 and 1 into register 2, and returns that difference. The absolute difference between X
and Y is the absolute value |X - Y| of the difference between X and Y, and is obviously always
positive.

The sample outputs below show only the final register values and the returned value. You can use as
many temporary as you need, and you don't need to use the same number I do.

Please ensure that the input registers are restored to their initial value, and any temporary
registers have 0 in them at the end of execution.


    >>> rm = RM()

    >>> rm.run(P_DIFF, [0, 0])
    ----  0  0  0  0  0  0  0  0  0  0  0
    0
    
    >>> rm.run(P_DIFF, [3, 0])
    ----  3  0  3  0  0  0  0  0  0  0  0
    3
    
    >>> rm.run(P_DIFF, [3, 1])
    ----  3  1  2  0  0  0  0  0  0  0  0
    2
    
    >>> rm.run(P_DIFF, [3, 2])
    ----  3  2  1  0  0  0  0  0  0  0  0
    1
    
    >>> rm.run(P_DIFF, [3, 3])
    ----  3  3  0  0  0  0  0  0  0  0  0
    0
    
    >>> rm.run(P_DIFF, [3, 4])
    ----  3  4  1  0  0  0  0  0  0  0  0
    1
    
    >>> rm.run(P_DIFF, [3, 5])
    ----  3  5  2  0  0  0  0  0  0  0  0
    2
    
    >>> rm.run(P_DIFF, [3, 6])
    ----  3  6  3  0  0  0  0  0  0  0  0
    3
    
    >>> rm.run(P_DIFF, [0, 3])
    ----  0  3  3  0  0  0  0  0  0  0  0
    3

**Hint**: |X - Y| = X &dotminus; Y if X &ge; Y and = Y &dotminus; X if X < Y. Equivalently, |X - Y|
= (X &dotminus; Y) + (Y &dotminus; X).
