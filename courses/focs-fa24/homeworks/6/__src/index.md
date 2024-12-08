# Homework 6

## Due Date: Thursday, December 12, 2024 (23h59)

- This homework is to be done individually. You may discuss problems
with fellow students, but all submitted work must be entirely your
own, and should not be from any other course, present, past, or
future. If you use a solution from another source you must cite it
&mdash; this includes when that source is someone else helping
you.

- Please do not post your solutions on a public website or a public repository (including GitHub).

- All programming is to be done in Python 3.

- Code your answers by modifying the file [`homework6.py`](homework6.py) provided. Add your **name**, your **email address**, and any **remarks** that you
wish to make to the instructor to the block comment at the head of the file.

- Please do not change the "signature" (number and interpretation of arguments) of the
functions. Doing so will make it impossible to load your code into the testing infrastructure, and
make me unhappy.

- Feel free to define helper functions if you need them.


**Electronic Submission Instructions**

- Make your your file `homework6.py` loads without error in a fresh Python 3 shell.

- Submit `homework6.py` on [Canvas](https://canvas.olin.edu).

***

## Register Machines (Part 2)

In [last homework](../5/) we developed the register machines we saw in class. 

Recall that a program for a register machine is a sequence of instructions, where instructions
include:

- `INC reg` : increment the content of register `reg` and go to the next instruction
- `DEC reg addr` : if the content of register `reg` is zero, go to the instruction at address `addr`; otherwise, decrement the content of register `reg` and go to the next instruction
- `JUMP addr` : go to the instruction at address `addr`
- `STOP reg` : stop execution and return value of register `reg`
- `NOP`: do nothing go to the next instruction

An instruction is represented in Python by a tuple with the first
element a string representing the instruction, and any subsequent item
the arguments to the instruction, for example:

    ("inc", 0)
    ("dec", 1, 37)
    ("jump", 47)
    ("stop", 2)
    
A program is represented by a list of instruction tuples. The address of each instruction is just
the index of the instruction in the list, so that the address of the first instruction is 0, the
second instruction 1, etc. Execution always starts at address 0.

Here is a program that copies the content of register 0 into register 1 without changing the content
of register 0, using a temporary register 2:

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

In Question 2 of last homework, we introduced _symbolic_ programs,
which let you use register _names_ instead of numbers, and _labels_
instead of addresses, via the following pseudo-instructions:

- `REGISTER name reg`: allow using `name` to refer to register number `reg`
- `TEMPORARY name`: allow using `name` to refer to a temporary register whose number is not known in advance
- `LABEL name`: indicate that the address of the next instruction can be referred to by `name`

Before execution, those names are resolved into
actual register numbers and instruction addresses so that execution
proceeds as previously defined.

Here is program `P_COPY` from above rewritten as a symbolic program:

    P_COPY = [
        # Copy register 0 to register 1.
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
    
Here is program `P_MOVE` to move the content of register 0 into register 1:

    P_MOVE = [
        # Move register 0 to register 1.
        ("register", "X", 0),
        ("register", "Y", 1),
        
        ("label", "start"),
        ("dec", "X", "done"),
        ("inc", "Y"),
        ("jump", "start"),
        
        ("label", "done"),
        ("stop", "Y")
    ]

I've given you a class `RM` that includes a method for running a (symbolic) program in two steps:
`RM().run()` takes a symbolic program, resolves all register names and label addresses, and then
executes the resulting non-symbolic program.

***

## Question 1: Subprogram Execution

We are going to add a new pseudo-instruction to symbolic programs:

    EXECUTE prg regs

where `prg` is a previously defined symbolic program, and `regs` is a list of registers, for example:

    EXECUTE P_COPY X,T

Intuitively, this instruction means "execute program `P_COPY` as a subroutine, using registers `X` and `T`
as input and output registesr. When the subroutine hits a `STOP`, execution of the current program
resumes at the instruction following the `EXECUTE` pseudo-instruction. In Python, this instruction is
represented by a tuple such as:

    ("execute", P_COPY, ["X", "T"])

We are NOT going to call `run()` recursively in the execution of a program to execute
subprogram. Instead, we are transform symbolic programs with `EXECUTE` instructions in them and
replace each `EXECUTE` instruction by a copy of the subprogram that `EXECUTE` refers to, performing
surgery on the subprogram so that it uses the specified register names instead of the registers it
normally uses.

Here is a program to add registers 0 and 1 into register 2, leaving registers 0 and 1 untouched:

    P_PLUS = [
        # Z = X + Y.
        ("register", "X", 0),
        ("register", "Y", 1),
        ("register", "Z", 2),
        ("temporary", "T"),
        ("temporary", "U"),
        ("execute", P_COPY, ["X", "T"]),
        ("execute", P_COPY, ["Y", "U"]),
        ("execute", P_MOVE, ["T", "Z"]),
        ("execute", P_MOVE, ["U", "Z"]),
        ("stop", "Z")
    ]

Here's how are going to work out the replacement of an `EXECUTE` instruction by a sequence of
instructions. We are going to replace every `("execute", p, regs)` in a symbolic program by a sequence
of symbolic instructions taken from program `p`, and we are going to:

- rename all the registers in program `p` so that the register names are all fresh
- change the program so that any references to registers 0, 1, ..., N-1 (where N is the length of `regs`) are turned into references to registers `regs[0]`, `regs[1]`, ..., `regs[N-1]`
- rename all the labels in program `p` so that the labels are all fresh
- replace all `STOP` instructions in program `p` by a `JUMP` to the address AFTER the EXECUTE instruction being replaced

Here is the result of doing this replacement in `P_PLUS` — I have indicated where the replaced subprograms occur:

    [
        ('register', 'X', 0),
        ('register', 'Y', 1),
        ('register', 'Z', 2),
        ('temporary', 'T'),
        ('temporary', 'U'),
      
        # EXECUTE P_COPY X,T
        ('register', '_R100', 'X'),
        ('register', '_R101', 'T'),
        ('temporary', '_R102'),
        ('label', '_L103'),
        ('dec', '_R100', '_L104'),
        ('inc', '_R101'),
        ('inc', '_R102'),
        ('jump', '_L103'),
        ('label', '_L104'),
        ('dec', '_R102', '_L105'),
        ('inc', '_R100'),
        ('jump', '_L104'),
        ('label', '_L105'),
        ('jump', '_L106'),
        ('label', '_L106'),
      
        # EXECUTE P_COPY Y,U
        ('register', '_R107', 'Y'),
        ('register', '_R108', 'U'),
        ('temporary', '_R109'),
        ('label', '_L110'),
        ('dec', '_R107', '_L111'),
        ('inc', '_R108'),
        ('inc', '_R109'),
        ('jump', '_L110'),
        ('label', '_L111'),
        ('dec', '_R109', '_L112'),
        ('inc', '_R107'),
        ('jump', '_L111'),
        ('label', '_L112'),
        ('jump', '_L113'),
        ('label', '_L113'),
      
        # EXECUTE P_MOVE T,Z
        ('register', '_R114', 'T'),
        ('register', '_R115', 'Z'),
        ('label', '_L116'),
        ('dec', '_R114', '_L117'),
        ('inc', '_R115'),
        ('jump', '_L116'),
        ('label', '_L117'),
        ('jump', '_L118'),
        ('label', '_L118'),
      
        # EXECUTE P_MOVE U,Z
        ('register', '_R119', 'U'),
        ('register', '_R120', 'Z'),
        ('label', '_L121'),
        ('dec', '_R119', '_L122'),
        ('inc', '_R120'),
        ('jump', '_L121'),
        ('label', '_L122'),
        ('jump', '_L123'),
        ('label', '_L123'),
      
        ('stop', 'Z')
    ]

We're going to implement the various steps of this transformations first, then pull them all
together in part (D).
    
### (A) 

Code a method `rename_register()` in class `RM` that takes a symbolic
program `P`, a source register name `S`, and a target register name
`T`, and returns a new symbolic program that's just like `P` but in
which every occurrence of register name `S` has been replaced by
register name `T`.

Sample outputs:

    >>> RM().rename_register(P_COPY, "X", "H")
    [
        ('register', 'H', 0), 
        ('register', 'Y', 1), 
        ('temporary', 'T'), 
        ('label', 'start'), 
        ('dec', 'H', 'reset-x'), 
        ('inc', 'Y'), 
        ('inc', 'T'), 
        ('jump', 'start'), 
        ('label', 'reset-x'), 
        ('dec', 'T', 'done'), 
        ('inc', 'H'), 
        ('jump', 'reset-x'), 
        ('label', 'done'), 
        ('stop', 'Y')
    ]

    >>> RM().rename_register(P_COPY, "Y", "H")
    [
        ('register', 'X', 0), 
        ('register', 'H', 1), 
        ('temporary', 'T'), 
        ('label', 'start'), 
        ('dec', 'X', 'reset-x'), 
        ('inc', 'H'), 
        ('inc', 'T'), 
        ('jump', 'start'), 
        ('label', 'reset-x'), 
        ('dec', 'T', 'done'), 
        ('inc', 'X'), 
        ('jump', 'reset-x'), 
        ('label', 'done'), 
        ('stop', 'H')
    ]

    >>> RM().rename_register(P_COPY, "T", "H")
    [
        ('register', 'X', 0), 
        ('register', 'Y', 1), 
        ('temporary', 'H'), 
        ('label', 'start'), 
        ('dec', 'X', 'reset-x'), 
        ('inc', 'Y'), 
        ('inc', 'H'), 
        ('jump', 'start'), 
        ('label', 'reset-x'), 
        ('dec', 'H', 'done'), 
        ('inc', 'X'), 
        ('jump', 'reset-x'), 
        ('label', 'done'), 
        ('stop', 'Y')
    ]


### (B)

Code a method `retarget_register()` in class `RM` that take a symbolic
program `P`, a register _number_ `N`, and a register name `T`, and
returns a new symbolic program that's just like `P` but in which every
occurence of register number `N` has been replaced by register name
`T`.

Sample outputs:

    >>> RM().retarget_register(P_COPY, 0, "H")
    [
        ('register', 'X', 'H'), 
        ('register', 'Y', 1), 
        ('temporary', 'T'), 
        ('label', 'start'), 
        ('dec', 'X', 'reset-x'), 
        ('inc', 'Y'), 
        ('inc', 'T'), 
        ('jump', 'start'), 
        ('label', 'reset-x'), 
        ('dec', 'T', 'done'), 
        ('inc', 'X'), 
        ('jump', 'reset-x'), 
        ('label', 'done'), 
        ('stop', 'Y')
    ]

    >>> RM().retarget_register(P_COPY, 1, "H")
    [
        ('register', 'X', 0), 
        ('register', 'Y', 'H'), 
        ('temporary', 'T'), 
        ('label', 'start'), 
        ('dec', 'X', 'reset-x'), 
        ('inc', 'Y'), 
        ('inc', 'T'), 
        ('jump', 'start'), 
        ('label', 'reset-x'), 
        ('dec', 'T', 'done'), 
        ('inc', 'X'), 
        ('jump', 'reset-x'), 
        ('label', 'done'), 
        ('stop', 'Y')
    ]

    >>> RM().retarget_register(P_COPY, 1, 10)
    [
        ('register', 'X', 0), 
        ('register', 'Y', 10), 
        ('temporary', 'T'), 
        ('label', 'start'), 
        ('dec', 'X', 'reset-x'), 
        ('inc', 'Y'), 
        ('inc', 'T'), 
        ('jump', 'start'), 
        ('label', 'reset-x'), 
        ('dec', 'T', 'done'), 
        ('inc', 'X'), 
        ('jump', 'reset-x'), 
        ('label', 'done'), 
        ('stop', 'Y')
    ]



### (C)

Code a method `rename_label()` in class `RM` that takes a symbolic
program `P`, a source label name `S`, and a target label name
`T`, and returns a new symbolic program that's just like `P` but in
which every occurrence of label `S` has been replaced by
label `T`.

Sample outputs:

    >>> RM().rename_label(P_COPY, "reset-x", "new_label")
    [
        ('register', 'X', 0), 
        ('register', 'Y', 1), 
        ('temporary', 'T'), 
        ('label', 'start'), 
        ('dec', 'X', 'new_label'), 
        ('inc', 'Y'), 
        ('inc', 'T'), 
        ('jump', 'start'), 
        ('label', 'new_label'), 
        ('dec', 'T', 'done'), 
        ('inc', 'X'), 
        ('jump', 'new_label'), 
        ('label', 'done'), 
        ('stop', 'Y')
    ]

    >>> RM().rename_label(P_COPY, "done", "new_label")
    [
        ('register', 'X', 0), 
        ('register', 'Y', 1), 
        ('temporary', 'T'), 
        ('label', 'start'), 
        ('dec', 'X', 'reset-x'), 
        ('inc', 'Y'), 
        ('inc', 'T'), 
        ('jump', 'start'), 
        ('label', 'reset-x'), 
        ('dec', 'T', 'new_label'), 
        ('inc', 'X'), 
        ('jump', 'reset-x'), 
        ('label', 'new_label'), 
        ('stop', 'Y')
    ]


### (D)

Code a method `replace_stop()` in class `RM` that takes a symbolic
program `P` and a label name `T`, and returns a new symbolic program that's just like `P` but in 
which every occurrence of `STOP` has been replaced by a `JUMP` to label name `T`.

Sample outputs:

    >>> RM().replace_stop(P_COPY, "new_label")
    [
        ('register', 'X', 0), 
        ('register', 'Y', 1), 
        ('temporary', 'T'), 
        ('label', 'start'), 
        ('dec', 'X', 'reset-x'), 
        ('inc', 'Y'), 
        ('inc', 'T'), 
        ('jump', 'start'), 
        ('label', 'reset-x'), 
        ('dec', 'T', 'done'), 
        ('inc', 'X'), 
        ('jump', 'reset-x'), 
        ('label', 'done'), 
        ('jump', 'new_label')
    ]


### (E)

Let's put all the pieces together. 

Code a method `resolve_executes()` in class `RM` that takes a symbolic program `P` and returns a new
symbolic program that looks just like `P` but in which every occurrences of 

    EXECUTE Q R0,R1,...,Rk 
    
has been replaced by the code of program `Q` using all fresh register names and fresh label names, in
which any references to registers 0, 1, ..., k have been replaced by references to `R0`, `R1`, ..., `Rk`,
and in which every `STOP` instruction has been replaced by a `JUMP` to the address after the
`EXECUTE`.

To help you out, `RM` has a bunch of useful helper methods:

- `register_names()` returns a list of all register names appearing in a program
- `fresh_register()` returns a "fresh" register name
- `label_names()` returns a list of all label names appearing in a program
- `fresh_label()` returns a "fresh" label name

Sample outputs (register and label names may differ due to fresh name allocation differences):

    >>> RM().resolve_executes(P_PLUS)
    [
        ('register', 'X', 0), 
        ('register', 'Y', 1), 
        ('register', 'Z', 2), 
        ('temporary', 'T'), 
        ('temporary', 'U'), 
        ('register', '_R100', 'X'), 
        ('register', '_R101', 'T'), 
        ('temporary', '_R102'), 
        ('label', '_L103'), 
        ('dec', '_R100', '_L104'), 
        ('inc', '_R101'), 
        ('inc', '_R102'), 
        ('jump', '_L103'), 
        ('label', '_L104'), 
        ('dec', '_R102', '_L105'), 
        ('inc', '_R100'), 
        ('jump', '_L104'), 
        ('label', '_L105'), 
        ('jump', '_L106'), 
        ('label', '_L106'), 
        ('register', '_R107', 'Y'), 
        ('register', '_R108', 'U'), 
        ('temporary', '_R109'), 
        ('label', '_L110'), 
        ('dec', '_R107', '_L111'), 
        ('inc', '_R108'), 
        ('inc', '_R109'), 
        ('jump', '_L110'), 
        ('label', '_L111'), 
        ('dec', '_R109', '_L112'), 
        ('inc', '_R107'), 
        ('jump', '_L111'), 
        ('label', '_L112'), 
        ('jump', '_L113'), 
        ('label', '_L113'), 
        ('register', '_R114', 'T'), 
        ('register', '_R115', 'Z'), 
        ('label', '_L116'), 
        ('dec', '_R114', '_L117'), 
        ('inc', '_R115'), 
        ('jump', '_L116'), 
        ('label', '_L117'), 
        ('jump', '_L118'), 
        ('label', '_L118'), 
        ('register', '_R119', 'U'), 
        ('register', '_R120', 'Z'), 
        ('label', '_L121'), 
        ('dec', '_R119', '_L122'), 
        ('inc', '_R120'), 
        ('jump', '_L121'), 
        ('label', '_L122'), 
        ('jump', '_L123'), 
        ('label', '_L123'), 
        ('stop', 'Z')
    ]

    >>> RM().resolve_executes(P_TIMES)
    [
        ('register', 'X', 0), 
        ('register', 'Y', 1), 
        ('register', 'Z', 2), 
        ('temporary', 'T'), 
        ('temporary', 'U'), 
        ('register', '_R100', 'X'), 
        ('register', '_R101', 'T'), 
        ('temporary', '_R102'), 
        ('label', '_L103'), 
        ('dec', '_R100', '_L104'), 
        ('inc', '_R101'), 
        ('inc', '_R102'), 
        ('jump', '_L103'), 
        ('label', '_L104'), 
        ('dec', '_R102', '_L105'), 
        ('inc', '_R100'), 
        ('jump', '_L104'), 
        ('label', '_L105'), 
        ('jump', '_L106'), 
        ('label', '_L106'), 
        ('label', 'loop'), 
        ('dec', 'T', 'done'), 
        ('register', '_R107', 'Y'), 
        ('register', '_R108', 'U'), 
        ('temporary', '_R109'), 
        ('label', '_L110'), 
        ('dec', '_R107', '_L111'), 
        ('inc', '_R108'), 
        ('inc', '_R109'), 
        ('jump', '_L110'), 
        ('label', '_L111'), 
        ('dec', '_R109', '_L112'), 
        ('inc', '_R107'), 
        ('jump', '_L111'), 
        ('label', '_L112'), 
        ('jump', '_L113'), 
        ('label', '_L113'), 
        ('register', '_R114', 'U'), 
        ('register', '_R115', 'Z'), 
        ('label', '_L116'), 
        ('dec', '_R114', '_L117'), 
        ('inc', '_R115'), 
        ('jump', '_L116'), 
        ('label', '_L117'), 
        ('jump', '_L118'), 
        ('label', '_L118'), 
        ('jump', 'loop'), 
        ('label', 'done'), 
        ('stop', 'Z')
    ]

Note that method `run()` already calls `self.resolve_executes()` to resolve `EXECUTE` instructions before
running a program. So once you complete `resolve_executes()` you should be able to run `P_TIMES`, say:

    >>> RM().run(P_TIMES, [2, 3])
    0000  2  3
    0001  1  3
    0002  1  3  0  1
    0003  1  3  0  1  0  1
    0000  1  3  0  1  0  1
    0001  0  3  0  1  0  1
    0002  0  3  0  2  0  1
    0003  0  3  0  2  0  2
    0000  0  3  0  2  0  2
    0004  0  3  0  2  0  2
    0005  0  3  0  2  0  1
    0006  1  3  0  2  0  1
    0004  1  3  0  2  0  1
    0005  1  3  0  2  0  0
    0006  2  3  0  2  0  0
    0004  2  3  0  2  0  0
    0007  2  3  0  2  0  0
    0008  2  3  0  2  0  0
    0009  2  3  0  1  0  0
    0010  2  2  0  1  0  0
    0011  2  2  0  1  1  0
    0012  2  2  0  1  1  0  1
    0009  2  2  0  1  1  0  1
    0010  2  1  0  1  1  0  1
    0011  2  1  0  1  2  0  1
    0012  2  1  0  1  2  0  2
    0009  2  1  0  1  2  0  2
    0010  2  0  0  1  2  0  2
    0011  2  0  0  1  3  0  2
    0012  2  0  0  1  3  0  3
    0009  2  0  0  1  3  0  3
    0013  2  0  0  1  3  0  3
    0014  2  0  0  1  3  0  2
    0015  2  1  0  1  3  0  2
    0013  2  1  0  1  3  0  2
    0014  2  1  0  1  3  0  1
    0015  2  2  0  1  3  0  1
    0013  2  2  0  1  3  0  1
    0014  2  2  0  1  3  0  0
    0015  2  3  0  1  3  0  0
    0013  2  3  0  1  3  0  0
    0016  2  3  0  1  3  0  0
    0017  2  3  0  1  3  0  0
    0018  2  3  0  1  2  0  0
    0019  2  3  1  1  2  0  0
    0017  2  3  1  1  2  0  0
    0018  2  3  1  1  1  0  0
    0019  2  3  2  1  1  0  0
    0017  2  3  2  1  1  0  0
    0018  2  3  2  1  0  0  0
    0019  2  3  3  1  0  0  0
    0017  2  3  3  1  0  0  0
    0020  2  3  3  1  0  0  0
    0021  2  3  3  1  0  0  0
    0008  2  3  3  1  0  0  0
    0009  2  3  3  0  0  0  0
    0010  2  2  3  0  0  0  0
    0011  2  2  3  0  1  0  0
    0012  2  2  3  0  1  0  1
    0009  2  2  3  0  1  0  1
    0010  2  1  3  0  1  0  1
    0011  2  1  3  0  2  0  1
    0012  2  1  3  0  2  0  2
    0009  2  1  3  0  2  0  2
    0010  2  0  3  0  2  0  2
    0011  2  0  3  0  3  0  2
    0012  2  0  3  0  3  0  3
    0009  2  0  3  0  3  0  3
    0013  2  0  3  0  3  0  3
    0014  2  0  3  0  3  0  2
    0015  2  1  3  0  3  0  2
    0013  2  1  3  0  3  0  2
    0014  2  1  3  0  3  0  1
    0015  2  2  3  0  3  0  1
    0013  2  2  3  0  3  0  1
    0014  2  2  3  0  3  0  0
    0015  2  3  3  0  3  0  0
    0013  2  3  3  0  3  0  0
    0016  2  3  3  0  3  0  0
    0017  2  3  3  0  3  0  0
    0018  2  3  3  0  2  0  0
    0019  2  3  4  0  2  0  0
    0017  2  3  4  0  2  0  0
    0018  2  3  4  0  1  0  0
    0019  2  3  5  0  1  0  0
    0017  2  3  5  0  1  0  0
    0018  2  3  5  0  0  0  0
    0019  2  3  6  0  0  0  0
    0017  2  3  6  0  0  0  0
    0020  2  3  6  0  0  0  0
    0021  2  3  6  0  0  0  0
    0008  2  3  6  0  0  0  0
    0022  2  3  6  0  0  0  0
    ----  2  3  6  0  0  0  0
    6


## To think about

Not part of the homework, but if you worked through the above, you may want to contemplate the following questions:

- Can you `EXECUTE` a program that itself has `EXECUTE` pseudo-instructions in it? If not, what changes would you need to make to your code to support that functionality?
- If you could handle the `EXECUTE` of a program with `EXECUTE` pseudo-instructions in it, what happens if program `A` executes a program `B` that itself executes program `A`? Can you `EXECUTE` mutually recursive programs? What would you need to do to support this feature?
