<script>
  document.title = 'Homework 3 - DB SP25'
</script>

# Homework 3

## Due Date: Sunday, March 16, 2025 (23h59)

- This homework is to be done *in teams of two*. You may discuss problems with fellow students, but
all submitted work must be entirely your team's, and should not be from any other course, present,
past, or future. If you use a solution from another source you must cite it.  That includes when
that source is someone else helping you.

- **Please do not post your solutions on a public website or a public repository like GitHub.**

- All programming is to be done in Go (version at least 20).

- Code your answers by modifying the file [`homework3.go`](homework3.go) provided. Add your **name**, your **email address**, and any **remarks** that you
wish to make to the instructor to the block comment at the head of the file.

- **Please do not change the types in the signature of the function stubs I provide**. Doing so will make it
impossible to load your code into the testing infrastructure, and generally make me grumpy.

- Feel free to define and use helper functions if you need them.


## Electronic Submission Instructions

- Make sure your code compiles without errors. Run 

        go run homework3.go

- If there are any error, do not submit. I can't test what I can't compile.

- Submission done via the course <a href="https://canvas.olin.edu">Canvas</a> site. One submission per team is sufficient, but please make sure to indicate who is in your team in the the header of your `homework2.go` submitted file.


* * *

## The Relational Model

In this homework, we're going to implement a simple form of the relational model that powers relational databases, and in particular the basic operations that provide the foundations of the SQL query language. 

Recall that the relational model is a model based on relations and tuples, usually expressed as tables and records stored in those tables. Intuitively, a table is a collection of records. We saw several ways of representing collections of records in the last homework. In this homework, we are going to pick the simplest one: arrays with capacity. In homework 2, we did arrays with capacity from scratch, but Go actually provides arrays with capacity natively, called _slices_. 

First step: please read about slices in the [tutorial](https://go.dev/tour/moretypes/7) and via [examples](https://gobyexample.com/slices). You should think of a slice as a dynamic array of type `[]T`, which you can initialize with `make([]T, L)` where `L` is the initial length of the array (the array is initialized with zero values for the type `T`) and which you can extend by calling the built-in function `append` which gives you back a new slice. (This is unlike the `append()` method in Python which modifies its array in place, so beware.) I will usually just use the term _array_ for a slice, because honestly fixed-sized arrays are rarely used in Go, unlike slices.

Given this, a _table_ has type

    type Table struct {
    	fields []string
    	rows []Record
    	primaryKey string
    }

where `fields` is an array of fields names in the table, in order, and `rows` is an array of records. A _record_ (that is, a row of a table) has type

    type Record []string

That's right, a record is just an array of values. (For the purpose of this homework, the values we store in tables are string. Obviously, this is limiting. We could generalize this, but it would be a distraction from the main point I want to make.) How do you know which value correspond to which field? Well, the values in a record appear in the order in which the fields are listed in the `fields` array of the table. 

The final element of a table is `primaryKey`, which is the name of the field that is used as a primary key for the table. Recall that a primary key has the property that it uniquely identifies a record in the table, so that there cannot be two records in the table with the same value for their primary key.

The primary key is optional. Why optional? Tables that _we_ create to store the entities and relationships in our data should have a primary key. However, as we'll see, the result of queries also form tables, and those tables that are (temporarily) created by way of querying need not have primary keys. For instance, we may project and ditch the primary key field of a table.

The fact that the values in records are strings does mean that we cannot represent `null` in these records. For the purpose of this homework, we can treat the empty string as a null value. (That's not great, because an empty string is a perfectly reasonable value to put in a string field, and that means something different than there being no value in the field. We could make that better, but again it would be a distraction from the point I want to make.)

As usual, you can print a table (for debugging purposes) with `fmt.Printf("%v\n", s)` though if you have pointers in the structure this will _not_ follow them.

I have given you two pre-defined functions in `homework3.go` to work with tables:

- `CreateTable(fields []string, primaryKey string) Table` which creates a new (empty) table with the given fields and a given primary key without any records
- `PrintTable(t Table)` which prints a table in a way similar to the SQLite command-line client.

We will code functions to insert records in a table, and query tables below. 


* * *

## Question 1: Inserting

Code a function

    InsertRow(t *Table, rec Record) 
    
that takes a pointer to a table, and a record, and inserts the record into the table. Notice there is no return type, so the table should be modified in place. (That's why we use a pointer.)

Please make sure that the record you're trying to insert is of the right size for the table (you can `panic` if not), and that when the table has a primary key defined, then the record value for the primary key field does not already exist in the table (you can `panic` if it does).

**Hint**: in this questions and others below, you will benefit from writing a function `indexField(t Table, f string) int` that takes a table and a field name and returns the index of the field in the records stored in the table. For example, if the `fields` are `a`, `b`, and `c` (in that order), then index of `a` is 0, index of `b` is 1, and index of `c` is 2, indicating the index of the values for those field in each record of the table.

Sample output:

    t := CreateTable([]string{"n", "square"}, "n")
    for i := 0; i < 20; i++ {
        InsertRow(&t, Record{fmt.Sprintf("%d", i), fmt.Sprintf("%d", i * i)})
    }
    PrintTable(t)
    
    +-------------+
    | n  | square |
    +-------------+
    | 0  | 0      |
    | 1  | 1      |
    | 2  | 4      |
    | 3  | 9      |
    | 4  | 16     |
    | 5  | 25     |
    | 6  | 36     |
    | 7  | 49     |
    | 8  | 64     |
    | 9  | 81     |
    | 10 | 100    |
    | 11 | 121    |
    | 12 | 144    |
    | 13 | 169    |
    | 14 | 196    |
    | 15 | 225    |
    | 16 | 256    |
    | 17 | 289    |
    | 18 | 324    |
    | 19 | 361    |
    +-------------+

Table `t` in this sample output will be reused in all examples below.


***

## Question 2: Projecting

Code a function

    Project(t Table, fields []strings) Table
    
that takes a table `t` and a sequence of field names and returns a new table with the same rows as `t` but where each record only has the fields defined in `fields`.

Make sure to check that the fields in `fields` are fields of the table, and that there are no repeats.

Sample output (using `t` from Question 1):


    PrintTable(Project(t, []string{"square"}))
    
    +--------+
    | square |
    +--------+
    | 0      |
    | 1      |
    | 4      |
    | 9      |
    | 16     |
    | 25     |
    | 36     |
    | 49     |
    | 64     |
    | 81     |
    | 100    |
    | 121    |
    | 144    |
    | 169    |
    | 196    |
    | 225    |
    | 256    |
    | 289    |
    | 324    |
    | 361    |
    +--------+



***

## Question 3: Filtering


Code a function

    Filter(t Table, pred func(Record)bool) Table
    
that takes a table `t` and a _predicate_ `pred` and returns a new table with the same fields as `t` but keep only the records for which `pred` returns `true` 

A predicate is just a function that takes a record and returns Boolean value (true or false). You can either define that predicate function like any other function and pass the name of the function as an argument to `Filter`, or you can define an [anonymous function](https://gobyexample.com/closures) to pass to `Filter` using the expression `func (r Record) bool { ... }`. (See the sample output below for such an example.)

Sample output (using `t` from Question 1):

    PrintTable(Filter(t, func(r Record) bool { return strings.HasPrefix(r[0], "1") }))
    
    +-------------+
    | n  | square |
    +-------------+
    | 1  | 1      |
    | 10 | 100    |
    | 11 | 121    |
    | 12 | 144    |
    | 13 | 169    |
    | 14 | 196    |
    | 15 | 225    |
    | 16 | 256    |
    | 17 | 289    |
    | 18 | 324    |
    | 19 | 361    |
    +-------------+

***

## Question 4: Joining

Code a function

    Join(t1 Table, t2 Table) Table
    
that takes two tables `t1` and `t2` and creates a new table with the fields of `t1` and the fields of `t2` and every record in the resulting table is a concatenation of a record from `t1` and a record from `t2`, in every possible combination.

Please prefix the field names in the resulting table with an indication of which table they came from: the fields from `t1` should be prefixed with `1.` and the fields from `t2` should be prefixed with `2.`. This guarantees that even when `t1` and `t2` have field names in common the resulting table will still have distinct field names.

**Hint**: you may want to create a function `concatRecords(r1 Record, r2 Record) Record` that creates a new record as the concatenation of two existing records.

Sample output (using `t` from Question 1):

    t2 := CreateTable([]string{"number", "text"}, "number")
    for i := 0; i < 15; i++ {
        InsertRow(&t2, Record{fmt.Sprintf("%d", i * 2), fmt.Sprintf("value_%d", i * 2)})
    }
    
    PrintTable(t2)
    
    +-------------------+
    | number | text     |
    +-------------------+
    | 0      | value_0  |
    | 2      | value_2  |
    | 4      | value_4  |
    | 6      | value_6  |
    | 8      | value_8  |
    | 10     | value_10 |
    | 12     | value_12 |
    | 14     | value_14 |
    | 16     | value_16 |
    | 18     | value_18 |
    | 20     | value_20 |
    | 22     | value_22 |
    | 24     | value_24 |
    | 26     | value_26 |
    | 28     | value_28 |
    +-------------------+


    PrintTable(Join(t, t2))

    +--------------------------------------+
    | 1.n | 1.square | 2.number | 2.text   |
    +--------------------------------------+
    | 0   | 0        | 0        | value_0  |
    | 0   | 0        | 2        | value_2  |
    | 0   | 0        | 4        | value_4  |
    | 0   | 0        | 6        | value_6  |
    | 0   | 0        | 8        | value_8  |
    | 0   | 0        | 10       | value_10 |
    | 0   | 0        | 12       | value_12 |
    | 0   | 0        | 14       | value_14 |
    | 0   | 0        | 16       | value_16 |
    | 0   | 0        | 18       | value_18 |
    | 0   | 0        | 20       | value_20 |
    | 0   | 0        | 22       | value_22 |
    | 0   | 0        | 24       | value_24 |
    | 0   | 0        | 26       | value_26 |
    | 0   | 0        | 28       | value_28 |
    | 1   | 1        | 0        | value_0  |
    | 1   | 1        | 2        | value_2  |
    | 1   | 1        | 4        | value_4  |
    | 1   | 1        | 6        | value_6  |
    | 1   | 1        | 8        | value_8  |
    | 1   | 1        | 10       | value_10 |
    | 1   | 1        | 12       | value_12 |
    | 1   | 1        | 14       | value_14 |
    | 1   | 1        | 16       | value_16 |
    | 1   | 1        | 18       | value_18 |
    | 1   | 1        | 20       | value_20 |
    | 1   | 1        | 22       | value_22 |
    | 1   | 1        | 24       | value_24 |
    | 1   | 1        | 26       | value_26 |
    | 1   | 1        | 28       | value_28 |
    | 2   | 4        | 0        | value_0  |
    | 2   | 4        | 2        | value_2  |
    | 2   | 4        | 4        | value_4  |
    | 2   | 4        | 6        | value_6  |
    | 2   | 4        | 8        | value_8  |
    | 2   | 4        | 10       | value_10 |
    | 2   | 4        | 12       | value_12 |
    | 2   | 4        | 14       | value_14 |
    | 2   | 4        | 16       | value_16 |
    | 2   | 4        | 18       | value_18 |
    | 2   | 4        | 20       | value_20 |
    | 2   | 4        | 22       | value_22 |
    | 2   | 4        | 24       | value_24 |
    | 2   | 4        | 26       | value_26 |
    | 2   | 4        | 28       | value_28 |
    | 3   | 9        | 0        | value_0  |
    | 3   | 9        | 2        | value_2  |
    | 3   | 9        | 4        | value_4  |
    | 3   | 9        | 6        | value_6  |
    | 3   | 9        | 8        | value_8  |
    | 3   | 9        | 10       | value_10 |
    | 3   | 9        | 12       | value_12 |
    | 3   | 9        | 14       | value_14 |
    | 3   | 9        | 16       | value_16 |
    | 3   | 9        | 18       | value_18 |
    | 3   | 9        | 20       | value_20 |
    | 3   | 9        | 22       | value_22 |
    | 3   | 9        | 24       | value_24 |
    | 3   | 9        | 26       | value_26 |
    | 3   | 9        | 28       | value_28 |
    | 4   | 16       | 0        | value_0  |
    | 4   | 16       | 2        | value_2  |
    | 4   | 16       | 4        | value_4  |
    | 4   | 16       | 6        | value_6  |
    | 4   | 16       | 8        | value_8  |
    | 4   | 16       | 10       | value_10 |
    | 4   | 16       | 12       | value_12 |
    | 4   | 16       | 14       | value_14 |
    | 4   | 16       | 16       | value_16 |
    | 4   | 16       | 18       | value_18 |
    | 4   | 16       | 20       | value_20 |
    | 4   | 16       | 22       | value_22 |
    | 4   | 16       | 24       | value_24 |
    | 4   | 16       | 26       | value_26 |
    | 4   | 16       | 28       | value_28 |
    | 5   | 25       | 0        | value_0  |
    | 5   | 25       | 2        | value_2  |
    | 5   | 25       | 4        | value_4  |
    | 5   | 25       | 6        | value_6  |
    | 5   | 25       | 8        | value_8  |
    | 5   | 25       | 10       | value_10 |
    | 5   | 25       | 12       | value_12 |
    | 5   | 25       | 14       | value_14 |
    | 5   | 25       | 16       | value_16 |
    | 5   | 25       | 18       | value_18 |
    | 5   | 25       | 20       | value_20 |
    | 5   | 25       | 22       | value_22 |
    | 5   | 25       | 24       | value_24 |
    | 5   | 25       | 26       | value_26 |
    | 5   | 25       | 28       | value_28 |
    | 6   | 36       | 0        | value_0  |
    | 6   | 36       | 2        | value_2  |
    | 6   | 36       | 4        | value_4  |
    | 6   | 36       | 6        | value_6  |
    | 6   | 36       | 8        | value_8  |
    | 6   | 36       | 10       | value_10 |
    | 6   | 36       | 12       | value_12 |
    | 6   | 36       | 14       | value_14 |
    | 6   | 36       | 16       | value_16 |
    | 6   | 36       | 18       | value_18 |
    | 6   | 36       | 20       | value_20 |
    | 6   | 36       | 22       | value_22 |
    | 6   | 36       | 24       | value_24 |
    | 6   | 36       | 26       | value_26 |
    | 6   | 36       | 28       | value_28 |
    | 7   | 49       | 0        | value_0  |
    | 7   | 49       | 2        | value_2  |
    | 7   | 49       | 4        | value_4  |
    | 7   | 49       | 6        | value_6  |
    | 7   | 49       | 8        | value_8  |
    | 7   | 49       | 10       | value_10 |
    | 7   | 49       | 12       | value_12 |
    | 7   | 49       | 14       | value_14 |
    | 7   | 49       | 16       | value_16 |
    | 7   | 49       | 18       | value_18 |
    | 7   | 49       | 20       | value_20 |
    | 7   | 49       | 22       | value_22 |
    | 7   | 49       | 24       | value_24 |
    | 7   | 49       | 26       | value_26 |
    | 7   | 49       | 28       | value_28 |
    | 8   | 64       | 0        | value_0  |
    | 8   | 64       | 2        | value_2  |
    | 8   | 64       | 4        | value_4  |
    | 8   | 64       | 6        | value_6  |
    | 8   | 64       | 8        | value_8  |
    | 8   | 64       | 10       | value_10 |
    | 8   | 64       | 12       | value_12 |
    | 8   | 64       | 14       | value_14 |
    | 8   | 64       | 16       | value_16 |
    | 8   | 64       | 18       | value_18 |
    | 8   | 64       | 20       | value_20 |
    | 8   | 64       | 22       | value_22 |
    | 8   | 64       | 24       | value_24 |
    | 8   | 64       | 26       | value_26 |
    | 8   | 64       | 28       | value_28 |
    | 9   | 81       | 0        | value_0  |
    | 9   | 81       | 2        | value_2  |
    | 9   | 81       | 4        | value_4  |
    | 9   | 81       | 6        | value_6  |
    | 9   | 81       | 8        | value_8  |
    | 9   | 81       | 10       | value_10 |
    | 9   | 81       | 12       | value_12 |
    | 9   | 81       | 14       | value_14 |
    | 9   | 81       | 16       | value_16 |
    | 9   | 81       | 18       | value_18 |
    | 9   | 81       | 20       | value_20 |
    | 9   | 81       | 22       | value_22 |
    | 9   | 81       | 24       | value_24 |
    | 9   | 81       | 26       | value_26 |
    | 9   | 81       | 28       | value_28 |
    | 10  | 100      | 0        | value_0  |
    | 10  | 100      | 2        | value_2  |
    | 10  | 100      | 4        | value_4  |
    | 10  | 100      | 6        | value_6  |
    | 10  | 100      | 8        | value_8  |
    | 10  | 100      | 10       | value_10 |
    | 10  | 100      | 12       | value_12 |
    | 10  | 100      | 14       | value_14 |
    | 10  | 100      | 16       | value_16 |
    | 10  | 100      | 18       | value_18 |
    | 10  | 100      | 20       | value_20 |
    | 10  | 100      | 22       | value_22 |
    | 10  | 100      | 24       | value_24 |
    | 10  | 100      | 26       | value_26 |
    | 10  | 100      | 28       | value_28 |
    | 11  | 121      | 0        | value_0  |
    | 11  | 121      | 2        | value_2  |
    | 11  | 121      | 4        | value_4  |
    | 11  | 121      | 6        | value_6  |
    | 11  | 121      | 8        | value_8  |
    | 11  | 121      | 10       | value_10 |
    | 11  | 121      | 12       | value_12 |
    | 11  | 121      | 14       | value_14 |
    | 11  | 121      | 16       | value_16 |
    | 11  | 121      | 18       | value_18 |
    | 11  | 121      | 20       | value_20 |
    | 11  | 121      | 22       | value_22 |
    | 11  | 121      | 24       | value_24 |
    | 11  | 121      | 26       | value_26 |
    | 11  | 121      | 28       | value_28 |
    | 12  | 144      | 0        | value_0  |
    | 12  | 144      | 2        | value_2  |
    | 12  | 144      | 4        | value_4  |
    | 12  | 144      | 6        | value_6  |
    | 12  | 144      | 8        | value_8  |
    | 12  | 144      | 10       | value_10 |
    | 12  | 144      | 12       | value_12 |
    | 12  | 144      | 14       | value_14 |
    | 12  | 144      | 16       | value_16 |
    | 12  | 144      | 18       | value_18 |
    | 12  | 144      | 20       | value_20 |
    | 12  | 144      | 22       | value_22 |
    | 12  | 144      | 24       | value_24 |
    | 12  | 144      | 26       | value_26 |
    | 12  | 144      | 28       | value_28 |
    | 13  | 169      | 0        | value_0  |
    | 13  | 169      | 2        | value_2  |
    | 13  | 169      | 4        | value_4  |
    | 13  | 169      | 6        | value_6  |
    | 13  | 169      | 8        | value_8  |
    | 13  | 169      | 10       | value_10 |
    | 13  | 169      | 12       | value_12 |
    | 13  | 169      | 14       | value_14 |
    | 13  | 169      | 16       | value_16 |
    | 13  | 169      | 18       | value_18 |
    | 13  | 169      | 20       | value_20 |
    | 13  | 169      | 22       | value_22 |
    | 13  | 169      | 24       | value_24 |
    | 13  | 169      | 26       | value_26 |
    | 13  | 169      | 28       | value_28 |
    | 14  | 196      | 0        | value_0  |
    | 14  | 196      | 2        | value_2  |
    | 14  | 196      | 4        | value_4  |
    | 14  | 196      | 6        | value_6  |
    | 14  | 196      | 8        | value_8  |
    | 14  | 196      | 10       | value_10 |
    | 14  | 196      | 12       | value_12 |
    | 14  | 196      | 14       | value_14 |
    | 14  | 196      | 16       | value_16 |
    | 14  | 196      | 18       | value_18 |
    | 14  | 196      | 20       | value_20 |
    | 14  | 196      | 22       | value_22 |
    | 14  | 196      | 24       | value_24 |
    | 14  | 196      | 26       | value_26 |
    | 14  | 196      | 28       | value_28 |
    | 15  | 225      | 0        | value_0  |
    | 15  | 225      | 2        | value_2  |
    | 15  | 225      | 4        | value_4  |
    | 15  | 225      | 6        | value_6  |
    | 15  | 225      | 8        | value_8  |
    | 15  | 225      | 10       | value_10 |
    | 15  | 225      | 12       | value_12 |
    | 15  | 225      | 14       | value_14 |
    | 15  | 225      | 16       | value_16 |
    | 15  | 225      | 18       | value_18 |
    | 15  | 225      | 20       | value_20 |
    | 15  | 225      | 22       | value_22 |
    | 15  | 225      | 24       | value_24 |
    | 15  | 225      | 26       | value_26 |
    | 15  | 225      | 28       | value_28 |
    | 16  | 256      | 0        | value_0  |
    | 16  | 256      | 2        | value_2  |
    | 16  | 256      | 4        | value_4  |
    | 16  | 256      | 6        | value_6  |
    | 16  | 256      | 8        | value_8  |
    | 16  | 256      | 10       | value_10 |
    | 16  | 256      | 12       | value_12 |
    | 16  | 256      | 14       | value_14 |
    | 16  | 256      | 16       | value_16 |
    | 16  | 256      | 18       | value_18 |
    | 16  | 256      | 20       | value_20 |
    | 16  | 256      | 22       | value_22 |
    | 16  | 256      | 24       | value_24 |
    | 16  | 256      | 26       | value_26 |
    | 16  | 256      | 28       | value_28 |
    | 17  | 289      | 0        | value_0  |
    | 17  | 289      | 2        | value_2  |
    | 17  | 289      | 4        | value_4  |
    | 17  | 289      | 6        | value_6  |
    | 17  | 289      | 8        | value_8  |
    | 17  | 289      | 10       | value_10 |
    | 17  | 289      | 12       | value_12 |
    | 17  | 289      | 14       | value_14 |
    | 17  | 289      | 16       | value_16 |
    | 17  | 289      | 18       | value_18 |
    | 17  | 289      | 20       | value_20 |
    | 17  | 289      | 22       | value_22 |
    | 17  | 289      | 24       | value_24 |
    | 17  | 289      | 26       | value_26 |
    | 17  | 289      | 28       | value_28 |
    | 18  | 324      | 0        | value_0  |
    | 18  | 324      | 2        | value_2  |
    | 18  | 324      | 4        | value_4  |
    | 18  | 324      | 6        | value_6  |
    | 18  | 324      | 8        | value_8  |
    | 18  | 324      | 10       | value_10 |
    | 18  | 324      | 12       | value_12 |
    | 18  | 324      | 14       | value_14 |
    | 18  | 324      | 16       | value_16 |
    | 18  | 324      | 18       | value_18 |
    | 18  | 324      | 20       | value_20 |
    | 18  | 324      | 22       | value_22 |
    | 18  | 324      | 24       | value_24 |
    | 18  | 324      | 26       | value_26 |
    | 18  | 324      | 28       | value_28 |
    | 19  | 361      | 0        | value_0  |
    | 19  | 361      | 2        | value_2  |
    | 19  | 361      | 4        | value_4  |
    | 19  | 361      | 6        | value_6  |
    | 19  | 361      | 8        | value_8  |
    | 19  | 361      | 10       | value_10 |
    | 19  | 361      | 12       | value_12 |
    | 19  | 361      | 14       | value_14 |
    | 19  | 361      | 16       | value_16 |
    | 19  | 361      | 18       | value_18 |
    | 19  | 361      | 20       | value_20 |
    | 19  | 361      | 22       | value_22 |
    | 19  | 361      | 24       | value_24 |
    | 19  | 361      | 26       | value_26 |
    | 19  | 361      | 28       | value_28 |
    +--------------------------------------+


***

## Question 5: Inner and Outer Joins

Code functions

    InnerJoin(t1 Table, t2 Table, f1 string, f2 string) Table
    
    LeftOuterJoin(t1 Table, t2 Table, f1 string, f2 string) Table
    
    RightOuterJoin(t1 Table, t2 Table, f1 string, f2 string) Table
    
    FullOuterJoin(t1 Table, t2 Table, f1 string, f2 string) Table

that take two tables `t1` and `t2` and a field `f1` in `t1` and a field `f2` in `t2` and returns a new table with the fields of `t1` and the fields of `t2` and the records in the resulting table are

- for `InnerJoin`: all records formed by concatenating a record from `t1` and a record from `t2` where the values of `f1` and `f2` are the same
- for `LeftOuterJoin`: all records formed by concatenating a record from `t1` and a record from `t2` where the values of `f1` and `f2` are the same, as well as all records from `t1` (concatenated with an "empty" record from `t2`) where the value of `f1` is not the value of an `f2` field in any record in `t2`
- for `LeftOuterJoin`: all records formed by concatenating a record from `t1` and a record from `t2` where the values of `f1` and `f2` are the same, as well as all records from `t2` (concatenated with an "empty" record from `t1`) where the value of `f2` is not the value of an `f1` field in any record in `t1` 
- for `FullOuterJoin`: all records formed by concatenating a record from `t1` and a record from `t2` where the values of `f1` and `f2` are the same, as well as all records from `t1` (concatenated with an "empty" record from `t2`) where the value of `f1` is not the value of an `f2` field in any record in `t2`, and all records from `t2` (concatenated with an "empty" record from `t1`) where the value of `f2` is not the value of an `f1` field in any record in `t1`.

An "empty" record here is a record where all fields are the empty string. (Not great, but we don't have null values.)

As in the `Join` case, please prefix the fields from `t1` with `1.` and the fields from `t2` with `2.`.

**Hint**: you may want to define an "empty" `t1` record and an "empty" `t2` record that you can concatenate to a `t1` or `t2` record. You also may want to factor out what's in common among all of these join functions so that you can simply have a core "join" function that you can call variously to obtain the different join flavors being asked for.

Sample output (using `t` from Question 1 and `t2` from Question 4):

    PrintTable(InnerJoin(t, t2, "n", "number"))
        
    +--------------------------------------+
    | 1.n | 1.square | 2.number | 2.text   |
    +--------------------------------------+
    | 0   | 0        | 0        | value_0  |
    | 2   | 4        | 2        | value_2  |
    | 4   | 16       | 4        | value_4  |
    | 6   | 36       | 6        | value_6  |
    | 8   | 64       | 8        | value_8  |
    | 10  | 100      | 10       | value_10 |
    | 12  | 144      | 12       | value_12 |
    | 14  | 196      | 14       | value_14 |
    | 16  | 256      | 16       | value_16 |
    | 18  | 324      | 18       | value_18 |
    +--------------------------------------+


    PrintTable(LeftOuterJoin(t, t2, "n", "number"))
        
    +--------------------------------------+
    | 1.n | 1.square | 2.number | 2.text   |
    +--------------------------------------+
    | 0   | 0        | 0        | value_0  |
    | 1   | 1        |          |          |
    | 2   | 4        | 2        | value_2  |
    | 3   | 9        |          |          |
    | 4   | 16       | 4        | value_4  |
    | 5   | 25       |          |          |
    | 6   | 36       | 6        | value_6  |
    | 7   | 49       |          |          |
    | 8   | 64       | 8        | value_8  |
    | 9   | 81       |          |          |
    | 10  | 100      | 10       | value_10 |
    | 11  | 121      |          |          |
    | 12  | 144      | 12       | value_12 |
    | 13  | 169      |          |          |
    | 14  | 196      | 14       | value_14 |
    | 15  | 225      |          |          |
    | 16  | 256      | 16       | value_16 |
    | 17  | 289      |          |          |
    | 18  | 324      | 18       | value_18 |
    | 19  | 361      |          |          |
    +--------------------------------------+


    PrintTable(RightOuterJoin(t, t2, "n", "number"))
        
    +--------------------------------------+
    | 1.n | 1.square | 2.number | 2.text   |
    +--------------------------------------+
    | 0   | 0        | 0        | value_0  |
    | 2   | 4        | 2        | value_2  |
    | 4   | 16       | 4        | value_4  |
    | 6   | 36       | 6        | value_6  |
    | 8   | 64       | 8        | value_8  |
    | 10  | 100      | 10       | value_10 |
    | 12  | 144      | 12       | value_12 |
    | 14  | 196      | 14       | value_14 |
    | 16  | 256      | 16       | value_16 |
    | 18  | 324      | 18       | value_18 |
    |     |          | 20       | value_20 |
    |     |          | 22       | value_22 |
    |     |          | 24       | value_24 |
    |     |          | 26       | value_26 |
    |     |          | 28       | value_28 |
    +--------------------------------------+


    PrintTable(FullOuterJoin(t, t2, "n", "number"))
        
    +--------------------------------------+
    | 1.n | 1.square | 2.number | 2.text   |
    +--------------------------------------+
    | 0   | 0        | 0        | value_0  |
    | 1   | 1        |          |          |
    | 2   | 4        | 2        | value_2  |
    | 3   | 9        |          |          |
    | 4   | 16       | 4        | value_4  |
    | 5   | 25       |          |          |
    | 6   | 36       | 6        | value_6  |
    | 7   | 49       |          |          |
    | 8   | 64       | 8        | value_8  |
    | 9   | 81       |          |          |
    | 10  | 100      | 10       | value_10 |
    | 11  | 121      |          |          |
    | 12  | 144      | 12       | value_12 |
    | 13  | 169      |          |          |
    | 14  | 196      | 14       | value_14 |
    | 15  | 225      |          |          |
    | 16  | 256      | 16       | value_16 |
    | 17  | 289      |          |          |
    | 18  | 324      | 18       | value_18 |
    | 19  | 361      |          |          |
    |     |          | 20       | value_20 |
    |     |          | 22       | value_22 |
    |     |          | 24       | value_24 |
    |     |          | 26       | value_26 |
    |     |          | 28       | value_28 |
    +--------------------------------------+

        
    PrintTable(LeftOuterJoin(FullOuterJoin(t, t2, "n", "number"), t, "2.number", "square"))

    +-------------------------------------------------------------+
    | 1.1.n | 1.1.square | 1.2.number | 1.2.text | 2.n | 2.square |
    +-------------------------------------------------------------+
    | 0     | 0          | 0          | value_0  | 0   | 0        |
    | 1     | 1          |            |          |     |          |
    | 2     | 4          | 2          | value_2  |     |          |
    | 3     | 9          |            |          |     |          |
    | 4     | 16         | 4          | value_4  | 2   | 4        |
    | 5     | 25         |            |          |     |          |
    | 6     | 36         | 6          | value_6  |     |          |
    | 7     | 49         |            |          |     |          |
    | 8     | 64         | 8          | value_8  |     |          |
    | 9     | 81         |            |          |     |          |
    | 10    | 100        | 10         | value_10 |     |          |
    | 11    | 121        |            |          |     |          |
    | 12    | 144        | 12         | value_12 |     |          |
    | 13    | 169        |            |          |     |          |
    | 14    | 196        | 14         | value_14 |     |          |
    | 15    | 225        |            |          |     |          |
    | 16    | 256        | 16         | value_16 | 4   | 16       |
    | 17    | 289        |            |          |     |          |
    | 18    | 324        | 18         | value_18 |     |          |
    | 19    | 361        |            |          |     |          |
    |       |            | 20         | value_20 |     |          |
    |       |            | 22         | value_22 |     |          |
    |       |            | 24         | value_24 |     |          |
    |       |            | 26         | value_26 |     |          |
    |       |            | 28         | value_28 |     |          |
    +-------------------------------------------------------------+

***

## Question 6: Aggregation

Code a function

    Aggregate(t Table, groupBy string, concat []string) Table
    
that takes a table `t`, a field name `groupBy` of `t`, and an array of field names of `t` (different from `groupBy`) and returns a new table with both `groupBy` and the fileds in `concat` as fields, and in which every row is the aggregation of the rows of `t` with the same value for `groupBy`: each aggregated row gets the common value of `groupBy` as the value of `groupBy` and gets the values of `f` concatenated together (separated by a comma) as the value of field `f`, for every `f` in `concat`.

(Obviously, this is a restricted form of aggregation. A more general aggregation would let you decide *for each field* how to aggregate the values in that field. Thinking how about you could generalize this function is left as an open exercise.)

**Hint**: use function `strings.Join` from the standard library.

Sample output (using `t` from Question 1):

	t3 := CreateTable([]string{"n", "first", "last"}, "n")
	for i := 0; i < 20; i++ {
		v := fmt.Sprintf("%d", i)
		InsertRow(&t3, Record{v, v[:1], v[len(v) - 1:]})
	}
    
    PrintTable(t3)
    
    +-------------------+
    | n  | first | last |
    +-------------------+
    | 0  | 0     | 0    |
    | 1  | 1     | 1    |
    | 2  | 2     | 2    |
    | 3  | 3     | 3    |
    | 4  | 4     | 4    |
    | 5  | 5     | 5    |
    | 6  | 6     | 6    |
    | 7  | 7     | 7    |
    | 8  | 8     | 8    |
    | 9  | 9     | 9    |
    | 10 | 1     | 0    |
    | 11 | 1     | 1    |
    | 12 | 1     | 2    |
    | 13 | 1     | 3    |
    | 14 | 1     | 4    |
    | 15 | 1     | 5    |
    | 16 | 1     | 6    |
    | 17 | 1     | 7    |
    | 18 | 1     | 8    |
    | 19 | 1     | 9    |
    +-------------------+

    PrintTable(Aggregate(t3, "first", []string{"n", "last"}))

    +-------------------------------------------------------------------------------------+
    | first | n                                         | last                            |
    +-------------------------------------------------------------------------------------+
    | 0     | 0                                         | 0                               |
    | 1     | 1, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19 | 1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 |
    | 2     | 2                                         | 2                               |
    | 3     | 3                                         | 3                               |
    | 4     | 4                                         | 4                               |
    | 5     | 5                                         | 5                               |
    | 6     | 6                                         | 6                               |
    | 7     | 7                                         | 7                               |
    | 8     | 8                                         | 8                               |
    | 9     | 9                                         | 9                               |
    +-------------------------------------------------------------------------------------+

	PrintTable(Aggregate(t3, "last", []string{"n", "first"}))
    
    +----------------------+
    | last | n     | first |
    +----------------------+
    | 0    | 0, 10 | 0, 1  |
    | 1    | 1, 11 | 1, 1  |
    | 2    | 2, 12 | 2, 1  |
    | 3    | 3, 13 | 3, 1  |
    | 4    | 4, 14 | 4, 1  |
    | 5    | 5, 15 | 5, 1  |
    | 6    | 6, 16 | 6, 1  |
    | 7    | 7, 17 | 7, 1  |
    | 8    | 8, 18 | 8, 1  |
    | 9    | 9, 19 | 9, 1  |
    +----------------------+

	PrintTable(Aggregate(InnerJoin(t, t3, "n", "n"), "2.first", []string{"1.square", "1.n"}))

    +-----------------------------------------------------------------------------------------------------------+
    | 2.first | 1.square                                            | 1.n                                       |
    +-----------------------------------------------------------------------------------------------------------+
    | 0       | 0                                                   | 0                                         |
    | 1       | 1, 100, 121, 144, 169, 196, 225, 256, 289, 324, 361 | 1, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19 |
    | 2       | 4                                                   | 2                                         |
    | 3       | 9                                                   | 3                                         |
    | 4       | 16                                                  | 4                                         |
    | 5       | 25                                                  | 5                                         |
    | 6       | 36                                                  | 6                                         |
    | 7       | 49                                                  | 7                                         |
    | 8       | 64                                                  | 8                                         |
    | 9       | 81                                                  | 9                                         |
    +-----------------------------------------------------------------------------------------------------------+

