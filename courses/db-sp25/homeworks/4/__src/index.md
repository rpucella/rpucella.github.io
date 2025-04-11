<script>
  document.title = 'Homework 4 - DB SP25'
</script>

# Homework 4

## Due Date: Sunday, April 20, 2025 (23h59)

- This homework is to be done *in teams of two*. You may discuss problems with fellow students, but
all submitted work must be entirely your team's, and should not be from any other course, present,
past, or future. If you use a solution from another source you must cite it.  That includes when
that source is someone else helping you.

- **Please do not post your solutions on a public website or a public repository like GitHub.**

- All programming is to be done in Go (version at least 20).

- Code your answers by modifying the file [`homework4.go`](homework4.go) provided. Add your **name**, your **email address**, and any **remarks** that you
wish to make to the instructor to the block comment at the head of the file.

- **Please do not change the types in the signature of the function stubs I provide**. Doing so will make it
impossible to load your code into the testing infrastructure, and generall make me grumpy.

- Feel free to define and use helper functions if you need them.


## Electronic Submission Instructions

- Make sure your code compiles without errors. Run 

        go run homework4.go

- If there are any error, do not submit. I can't test what I can't compile.

- Submission done via the course <a href="https://canvas.olin.edu">Canvas</a> site. One submission per team is sufficient, but please make sure to indicate who is in your team in the the header of your `homework2.go` submitted file.


* * *

## Physical Model

In this homework, we're going to play around with how data is stored on disk, with a focus on blocks. This is going to be pretty schematic, enough to give you a sense of the kind of manipulations that are needed. We're also going to implement B+ trees, because fun. 

As we saw in the Physical Model lecture, a file is made up of blocks, where each block contains a sequence of records. A block here corresponding to an operating system _disk block_, which is a predefined unit at the operating system level. We're not going to go to that level, we're going to _model_ blocks in software. We are still going to work purely in memory.

A Block is simply a sequence of records, with a fixed number of record per block, given by constant `recordsPerBlock`. In Question 1, we will create functions to work on records in blocks.

    const recordsPerBlock = 5

    type Block = [recordsPerBlock]string

We're going to keep things super simple by taking a record to simply be a string. An empty string represents an empty record slot in a block. You create a new block using provided function `NewBlock()` that returns a new block with no records in it (i.e., all strings are empty).

A File is basically a list of blocks, with an optional overflow block used in the context of ordered files.

    type File struct  {
        blocks []Block
        overflow *Block
    }

You can create a new (empty) sequential file (for Question 1) and ordered file (for Question 2) using:

    func NewSeqFile() *File
    func NewOrdFile() *File

I will ask you _not_ to access the field of File directly. Instead, I've given you three functions to interact with a file:

    func ReadBlock(f *File, idx int) Block
    func WriteBlock(f *File, idx int, block Block)
    func CreateBlock(f *File) int

Function `ReadBlock(f, idx)` takes a file `f` and a block index `idx` (0 is the first block) and returns (a copy of) the block in the file at that index. When the index is -1, this returns the overflow block.

Function `WriteBlock(f, idx, b)` is similar, taking a file `f` and a block index `idx` (0 is the first) as well as a block `b`, and write the content of block `b` at index `idx` of the file. When the index is -1, this writes `b` into the overflow block.

Function `CreateBlock(f)` adds a new (empty) block to the file, and returns the index of that newly created block.

(There are ways to _hide_ the implementation of `File` — basically put the code in its own package — but I don't want to complicate the homework code. So I will instead ask you to not access the fields directly.)

Function

    func FileSize(f *File) int
    
returns the number of blocks in file `f`. 

To print a file, use function

    func PrintFile(f *File)

It will show the context of each block, with the index of the each record, as well as the overflow block for ordered files.


* * *

## Question 1: Manipulating Blocks

### (A)

Code a function

    func AppendRecord(block Block, rec string) (Block, bool)
    
which takes a block and a record (a string) and appends the record to the block at the next available slot, if there is one. If there is room for the record, then the function returns the updated block and the value `true`, and if there is no room for the record, then the function returns the unchanged block and the value `false.

To maintain the invariant that the empty string represents "no record", we disallowing appending the empty string as a record. So panic if `rec == ""`. 

### (B)

Code a function

    func FirstRecord(block Block) string
    
which takes a block and returns the first record in the block (or the empty string if the block is empty).

### (C)

Code a function

    func LastRecord(block Block) string
    
which takes a block and returns the last record in the block (or the empty string if the block is empty).

### (D)

Code a function

    func FindRecord(block Block, rec string) bool

which true if record `rec` can be found in the block and `false` otherwise.
    
    
### (E)

Code a function

    func FreeSize(block Block) int
    
which returns the number of free spots for records in the block.


* * *

## Question 2: Sequential Files

Recall that a sequential file is a file made up of a sequence of blocks where records are added
sequentially, one after the one, block after block, in no specific order.

### (A)

Code a function 

    SF_Find(file *File, rec string) bool

which returns `true` if the record `rec` can be found in file `f`, and `false` otherwise

### (B)

Code a function 

    SF_Insert(file *File, rec string)
    
which adds record `rec` to file `f`. The record should be appended at the end of the last block of the file. If the last block of the file is full, then a new block should be created in the file to hold the record.

If record `rec` already appears in file `f`, `SF_Insert` has no effect.

* * *

## Question 3: Ordered Files

Recall that an ordered file is a file made up of a sequence of blocks where records are kept in increasing order in the file. 
The file also has an overflow block that holds unordered records. The idea is that to add a record, we add it to the overflow block. When the overflow block is full, the file as a whole is _rebuilt_, ensuring increasing order for all records, and leaving the overflow block empty.

### (A)

Code a function 

    func OF_Find(f *File, rec string) bool 
    
which returns `true` if the record `rec` can be found in file `f`, and `false` otherwise.

Use binary search in the file to find the block in which record `rec` should be found if were in the file. Start with the middle block, and use `FirstRecord` and `LastRecord` to determine if record should appear in the block. If it does, look for it in the block, otherwise, use binary search to find another block candidate based on whether the record is less than the `FirstRecord()` of the block, or mroe than the `LastRecord()` of the block.

Remember to look in the overflow block as well!


### (B)

Code a function

    func OF_Insert(f *File, rec string) 
    
which adds record `rec` to file `f`. Add the record to the overflow block of the file unless the
overflow block is full, in which case you first rebuild the file before adding record `rec`.

To do this rebuilding, recreate the blocks of the file, appending records one after the other and
"inserting" all the records in the overflow block in the appropriate spots.

If record `rec` already appears in file `f`, `OF_Insert` has no effect.

**Hint**: for the rebuilding step, loop through every record in every block, and add them to new
blocks while at the same time checking against ever record in the overflow block to see if one of
them should be added first.

***

## Sample run of the code

Here's a sample run of the code for my implementation. It uses the `main()` function defined in `homework4.go`, which of course you should feel free to change. 

```
=== SEQ FILE =====================================
[Reading block 0]
[Writing block 0]
[Reading block 0]
[Writing block 0]
[Reading block 0]
[Writing block 0]
[Reading block 0]
[Writing block 0]
[Reading block 0]
[Writing block 0]
[Reading block 0]
[Reading block 1]
[Writing block 1]
[Reading block 1]
[Writing block 1]
[Reading block 1]
[Writing block 1]
[Reading block 1]
[Writing block 1]
[Reading block 1]
[Writing block 1]
[Reading block 1]
[Reading block 2]
[Writing block 2]
[Reading block 2]
[Writing block 2]
[Reading block 2]
[Writing block 2]
[Reading block 2]
[Writing block 2]
[Reading block 2]
[Writing block 2]
[Reading block 2]
[Reading block 3]
[Writing block 3]
[Reading block 3]
[Writing block 3]
[Reading block 3]
[Writing block 3]
[Reading block 3]
[Writing block 3]
[Reading block 3]
[Writing block 3]
[Reading block 3]
[Reading block 4]
[Writing block 4]
[Reading block 4]
[Writing block 4]
[Reading block 4]
[Writing block 4]
[Reading block 4]
[Writing block 4]
-- BLOCKS ------------------------------
Block 0
  0 test-1
  1 test-2
  2 test-3
  3 test-4
  4 test-5
Block 1
  0 test-6
  1 test-7
  2 test-8
  3 test-9
  4 test-10
Block 2
  0 test-11
  1 test-12
  2 test-13
  3 test-14
  4 test-15
Block 3
  0 test-16
  1 test-17
  2 test-18
  3 test-19
  4 test-20
Block 4
  0 test-21
  1 test-22
  2 test-23
  3 test-24
  4 
[Reading block 0]
[Reading block 1]
[Reading block 2]
Looking for test-14: true
[Reading block 0]
[Reading block 1]
[Reading block 2]
[Reading block 3]
[Reading block 4]
looking for test-99: false
=== ORD FILE =====================================
[Reading overflow block]
[Writing overflow block]
[Reading overflow block]
[Writing overflow block]
[Reading overflow block]
[Writing overflow block]
[Reading overflow block]
[Writing overflow block]
[Reading overflow block]
[Writing overflow block]
[Reading overflow block]
[Reading block 0]
[Reading overflow block]
[Writing overflow block]
[Reading block 0]
[Reading overflow block]
[Writing overflow block]
[Reading block 0]
[Reading overflow block]
[Writing overflow block]
[Reading block 0]
[Reading overflow block]
[Writing overflow block]
[Reading block 0]
[Reading overflow block]
[Writing overflow block]
[Reading block 0]
[Reading overflow block]
[Reading block 1]
[Reading block 0]
[Reading overflow block]
[Writing overflow block]
[Reading block 1]
[Reading block 0]
[Reading overflow block]
[Writing overflow block]
[Reading block 1]
[Reading block 0]
[Reading overflow block]
[Writing overflow block]
[Reading block 1]
[Reading block 0]
[Reading overflow block]
[Writing overflow block]
[Reading block 1]
[Reading block 0]
[Reading overflow block]
[Writing overflow block]
[Reading block 1]
[Reading block 0]
[Reading overflow block]
[Reading block 1]
[Reading overflow block]
[Writing overflow block]
[Reading block 1]
[Reading overflow block]
[Writing overflow block]
[Reading block 1]
[Reading overflow block]
[Writing overflow block]
[Reading block 1]
[Reading overflow block]
[Writing overflow block]
[Reading block 1]
[Reading overflow block]
[Writing overflow block]
[Reading block 1]
[Reading overflow block]
[Reading block 2]
[Reading overflow block]
[Writing overflow block]
[Reading block 2]
[Reading overflow block]
[Writing overflow block]
[Reading block 2]
[Reading overflow block]
[Writing overflow block]
[Reading block 2]
[Reading overflow block]
[Writing overflow block]
-- BLOCKS ------------------------------
Block 0
  0 test-1
  1 test-10
  2 test-11
  3 test-12
  4 test-13
Block 1
  0 test-14
  1 test-15
  2 test-16
  3 test-17
  4 test-18
Block 2
  0 test-19
  1 test-2
  2 test-20
  3 test-3
  4 test-4
Block 3
  0 test-5
  1 test-6
  2 test-7
  3 test-8
  4 test-9
-- OVERFLOW ----------------------------
  0 test-21
  1 test-22
  2 test-23
  3 test-24
  4 
[Reading block 2]
Looking for test-14: true
[Reading block 2]
[Reading block 4]
looking for test-99: false
```
