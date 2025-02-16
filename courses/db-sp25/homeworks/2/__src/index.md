<script>
  document.title = 'Homework 1 - DB SP25'
</script>

# Homework 2

## Due Date: Friday, February 23, 2025 (23h59)

- This homework is to be done *in teams of two*. You may discuss problems with fellow students, but
all submitted work must be entirely your team's, and should not be from any other course, present,
past, or future. If you use a solution from another source you must cite it.  That includes when
that source is someone else helping you.

- **Please do not post your solutions on a public website or a public repository like GitHub.**

- All programming is to be done in Go (version at least 20).

- Code your answers by modifying the file [`homework2.go`](homework2.go) provided. Add your **name**, your **email address**, and any **remarks** that you
wish to make to the instructor to the block comment at the head of the file.

- **Please do not change the types in the signature of the function stubs I provide**. Doing so will make it
impossible to load your code into the testing infrastructure, and generall make me grumpy.

- Feel free to define and use helper functions if you need them.


## Electronic Submission Instructions

- Make sure your code compiles without errors. Run 

        go run homework2.go

- If there are any error, do not submit. I can't test what I can't compile.

- Submission done via the course <a href="canvas.olin.edu">Canvas</a> site. One submission per team is sufficient, but please make sure to indicate who is in your team in the the header of your `homework2.go` submitted file.


* * *

In this homework, we're going to dig into in-memory data structures
for storing records, as we saw in class. We're going to cover arrays
and linked structures (lists and trees) over primitive types
(integers) and then handle records.

For each data structure, we're going to have the following:

- a type `X` describing the structure
- a `NewX` function for creating an empty structure
- a `InsertX` function for inserting a new value in the structure &mdash; the function modifies the structure in-place
- a `FindX` function for looking up a value in the structure &mdash; it returns two values, the value it found (if any), and a boolean indicating whether the value was found

To illustrate the above, here's a simple implementation of an array structure that simply reallocates an array when you insert a new value:

    type ArrayDefault struct {
        content []int
        size int
    }
    
    func NewArrayDefault() *ArrayDefault {
        c := make([]int, 0)
        return &ArrayDefault{c, 0}
    }
    
    func FindArrayDefault(arr *ArrayDefault, target int) (int, bool) {
        for _, c := range arr.content {
            if c == target {
                return c, true
            }
        }
        return 0, false
    }
    
    func InsertArrayDefault(arr *ArrayDefault, new int) {
        new_content := make([]int, arr.size + 1)
        for i, c := range arr.content {
            new_content[i] = c
        }
        new_content[arr.size] = new
        arr.content = new_content
        arr.size += 1
    }

A few of things to note in the code above:

1. I use [structs](https://gobyexample.com/structs) where in Python you'd use a dictionary and in Javascript you'd use an object &mdash; a struct is a compound value that lets you access its components (fields) by name. You declare a struct with `type ... struct ...` which gives a name to the type (so that you can use it in function and variable declarations). To create a new struct value, you use the struct name, passing the values of the fields between braces. Fields are initialized by the zero value of their type, unless specified.

1. I use [slices](https://gobyexample.com/slices) as more convenient
form of arrays. Slices are many things, but to a first approximation
you can think of them as arrays with no explicit size. The type of a
slice containing integers is `[]int` as opposed to an array of
integers which requires a size, such as `[3]int`. Please treat slices
exactly like you do arrays. Do not use `append` in this
homework. (This restriction is not because `append` is bad, but
because `append` does some of things that I want you to do manually in
Question 1. After this homework, you will be free to use `append`.) To
create a slice, you use `make`. It initializes a slice of a given size
with the zero element of the type of the slice.

3. Because I will want to modify the structure in place, `NewX` returns a [pointer](https://gobyexample.com/pointers) to the struct representing the structure. To work with that, `InsertX` and `FindX` expect a pointer to the structure as well. Since you create a structure by calling `NewX` and insert into it with `InsertX`, you generally do not have to know that this is all using pointers to use these functions.

4. I use the `:=` notation for [assignment](https://gobyexample.com/variables), which is a convenient abbreviation that does type inference. A declarative assignemnt such as `x := 10` is just shorthand for `var x int = 10`, where the `int` is inferred from the initializer `10`. In cases where the type cannot be inferred from the initializer (for instance, if the initializer is `nil`, which can be any pointer type), a declarative assignment such as `x := nil` will fail and you will need to use the more explicit `var x *int = nil` (or whatever).

In the questions below, I will ask you to create new structures. In all cases, I will give you the structure type and the `NewX` function, and it will be up to you to implement the `InsertX` and `FindX` functions.

* * *

## Question 1: Arrays with Capacity

We're going to start with implementing Arrays with capacity (like we saw in class). Here's the type and constructor:

    type ArrayCapacity struct {
        content []int
        size int
        waterline int
    }
	
    func NewArrayCapacity() *ArrayCapacity {
        c := make([]int, 1)
        return &ArrayCapacity{c, 1, 0}
    }

Basically, an array with capacity is like a normal array (content and
size) but the array itself is larger than it strictly needs to be. It
has more space than values in it. Field `waterline` holds an index
into the first "unused" cell in the array. Arrays with capacity are
created with capacity 1.

You can print such an array, for debugging purposes, with `fmt.Printf("%v\n", arr)`

### (A)

Code a function

    InsertArrayCapacity(arr *ArrayCapacity, item int)
	
that takes a pointer to an array with capacity and a new item and adds the item to content. 

To insert an item into an array with capacity, you see if there's are
unused cells left. If there are, you add the new item at the first
unused cell, and bump the waterline. If the array is full, then you
allocate a new array with double the capacity of the original, and use
that new array as the new content, making sure to copy to earlier
content.


### (B)

Code a function

    FindArrayCapacity(arr *ArrayCapacity, item int) (int, bool)

that takes a pointer to an array with capacity and an item to
find, and returns two values, the item if found (or zero if not), and
a Boolean value indicating whether the item was found.

(We basically need to distinguish between the function returning 0
because we were looking for 0 and it is in the structure, versus
returning 0 because we were looking for 0 but it was not there.)



## Question 2: Sorted Arrays with Capacity

Let's implemented sorted arrays with capacity, like we saw in
class. The advantage here is that they enable binary search to find an
element quickly in the array.

    type ArraySorted struct {
        content []int
        size int
        waterline int
    }
    
    func NewArraySorted() *ArraySorted {
        c := make([]int, 1)
        return &ArraySorted{c, 1, 0}
    }

A sorted array with capacity looks *exactly* like an array with capacity. The fact that it's sorted doesn't reflect itself in the type.

You can print such an array, for debugging purposes, with `fmt.Printf("%v\n", arr)`

### 
### (A)

Code a function

    InsertArraySorted(arr *ArrayCapacity, item int)

that takes a pointer to a sorted array with capacity and a new item and
adds the item to content.

Insertion is a bit like with an array with capacity: if there is room for the item, you add it to the content. Otherwise, you need to allocate an array with twice the current capacity because adding the item. In either cases, you need to add the item in the proper place in the array to preserve the order of the elements. 

### (B)

Code a function

    FindArraySorted(arr *ArrayCapacity, item int) (int, bool)

that takes a pointer to a sorted array with capacity and an item to
find, and returns two values, the item if found (or zero if not), and
a Boolean value indicating whether the item was found.

Use *binary search* like we saw in class to find the element in the array.

***

## Question 3: Binary Search Trees

In class, we saw that while sorted arrays with capacity are excellent for finding items because of binary search, insertions are expensive because inserting in the middle of an array requires shifting array elements do make room for the new item. Linked lists (unlike arrays) make it easy to add into the middle of a sequence, but we lose the benefits of binary search. For the sake of completeness, I included an implementation of linked lists in `homework2.go`.

We can restore binary search by switching from linked lists to [binary search trees](https://en.wikipedia.org/wiki/Binary_search_tree):

    type Cell_BST struct {
        value int
        left *Cell_BST
        right *Cell_BST
    }
    
    type BSTree struct {
        root *Cell_BST
    }
    
    func NewBSTree() *BSTree {
        s := &BSTree{nil}
        return s
    }
    
    func PrintBSTree(t *BSTree) {
        var print func(*Cell_BST, string)
        print = func (c *Cell_BST, prefix string) {
            if c != nil {
                print(c.right, prefix + "    ")
                fmt.Printf("%s%d\n", prefix, c.value)
                print(c.left, prefix + "    ")
            } else {
                fmt.Printf("%s.\n", prefix)
            }
        }
        print(t.root, "  ")
        fmt.Println()
    }

A binary search tree is made of *cells*, where each cell has a
value associated with it and two pointers to cells making up the left
and right subtree of the cell. The *binary search tree property*
mandates that all the values in the cells connected via the "left"
pointer are less than the value in the current cell, and all the
values in the cells connected via the "right" pointer are more than
(or equal) to the value in the current cell.

To print a binary search tree, we cannot simply delegate to the `Printf` function, because it doesn't follow pointers. I gave you a `PrintBSTree` function that shows the tree "on its side".

### (A)

Code a function

    InsertBSTree(t *BSTree, item int)

that takes a pointer to a binary search tree and a new item and adds
the item to content.

Insert into a binary search is like finding in a binary search tree. You go down the tree, following left or right pointer depending on the value of the cell you're in and the value you're looking for. When you reach a leaf (a nil pointer), then that is where the value you want to insert should go, so you should allocate a cell and put the new value in that cell. You also need to hook up that new cell to its parent.

Note that our representation does *not* have parent pointers. You do not need them. You can simply track which cell was the one that got you were you are, so that you can know which cell's left/right pointer to update. Take care of what happens when you insert into an empty tree. If you're clever and understand pointers well, you can almost trivially keep track of which cell pointer you need to update to point to the new cell you create. But you do not have to be clever if you're willing to do some bookkeeping.

### (B)

Code a function

    FindBSTree(t *BSTree, item int) (int, bool)

that takes a pointer to a binary search tree and an item to
find, and returns two values, the item if found (or zero if not), and
a Boolean value indicating whether the item was found.

Use the binary search tree property to find the item quickly, starting
at the root and following left or right pointer depending on the value
of the cell you're in and the value you're looking for.

***

## Question 4: Records

Let's extend our binary search trees from Question 3 to handle records (here, book records).

We're going to keep book records very simple:

    type Book struct {
        isbn string
        title string
    }

Obviously, we could add authors, publishers, publication years, etc, without changing what we're going to do below. For us, ISBNs will be of the form `BNnnnnnnnnn` (unlike real ISBNs).

Here's what binary search trees look like for book records:

    type Cell_BookTree struct {
        value *Book
        left *Cell_BookTree
        right *Cell_BookTree
    }
    
    type BookTree struct {
        root *Cell_BookTree
    }
    
    func NewBookTree() *BookTree {
        return &BookTree{nil}
    }
    
    func PrintBookTree(t *BookTree) {
        var print func(*Cell_BookTree, string)
        print = func (c *Cell_BookTree, prefix string) {
            if c != nil {
                print(c.right, prefix + "    ")
                fmt.Printf("%s%s (%s)\n", prefix, c.value.title, c.value.isbn)
                print(c.left, prefix + "    ")
            } else {
                fmt.Printf("%s.\n", prefix)
            }
        }
        print(t.root, "  ")
        fmt.Println()
    }
    
The only difference is that here a cell holds a pointer to a book structure instead of an integer. We use a pointer to enable sharing of books in the next question.

Another difference, not reflected in the types, is that a book tree does not specify what field of `Book` it is ordered by. We specify this ordering in the insert and lookup function.

### (A)

Code a function

    InsertBookTree(t *BookTree, key func(*Book)string, item *Book)

that takes a pointer to a book tree, a *function* that returns a key out of a book structure, and a new book, and adds the book
to the tree using standard binary search tree insertion, using the key field described by the `key` function for the ordering.

The second argument to `InsertBookTree` is a function argument. You can [pass functions around as values to other functions and return them from other functions](https://gobyexample.com/closures) either by using the `func` expression form, or simply by supplying the name of an existing function as the argument.

Here are two obvious key functions that will come in handy:

    func keyIsbn(b *Book) string {
        return b.isbn
    }
    
    func keyTitle(b *Book) string {
        return b.title
    }

If you want to insert by ISBN, pass `keyIsbn` to `InsertBookTree`, and if you want to insert by title, pass `keyTitle` to `InsertBookTree`. Note that you'll want to **always** use the same key function for a given book tree, otherwise you will get weird and incorrect results!

### (B)

Code a function

    FindBookTree(t *BookTree, key func(*Book)string, k string) (*Book, bool)

that takes a pointer to a book tree, a *function* that returns a key out of a book structure, and a string representing a value of the key you're looking for, and returns a pointer to the Book corresponding to the key you're looking for (`nil` if not found) as well as a Boolean flag indicated whether the key has been found. Here, the flag is redundant (why?) but for consistency of interface, we'll keep it.

The function should use binary search to find the book.

The `key` function works exactly as in `InsertBookTree`. Clearly, you should use the same `key` function to search a book tree that was used to insert books in the tree.

For testing, I have given you a function `CreateRandomBook` that returns a random ISBN and a random title.

### Stuff to think about

Having to provide a key function when inserting and searching in a book tree is annoying and error prone &mdash; there is no way to guarantee that you search with the same key that was used to insert in the tree in the first place. Can you think of a way to specify the key to use for a given book tree, while allowing you the flexibility of using different keys for different book trees?

***

## Question 5: Records with Multiple Search Keys

Let's extend what we did in Question 4 to support a book collection that you can search efficiently by both title and ISBN, as opposed to a single field. The idea is trivial: use multiple trees to represent a collection.

    type BookCollection struct {
        byIsbn *BookTree
        byTitle *BookTree
    }
    
    func NewBookCollection() *BookCollection {
        return &BookCollection{NewBookTree(), NewBookTree()}
    }

    func PrintBookCollection(books *BookCollection) {
        fmt.Println("BY ISBN:")
        PrintBookTree(books.byIsbn)
        fmt.Println("BY TITLE:")
        PrintBookTree(books.byTitle)
    }
    
A book collection is two book trees, one ordered by ISBN, one ordered by title.


### (A)

Code a function

    InsertBookCollection(t *BookCollection, item *Book)

that takes a pointer to a book collection  and a new book, and adds the book to the collection, that is, adds the book to BOTH book trees in the collection, using the appropriate key for each book tree.


### (B)

Code two functions

    FindBookCollectionByIsbn(t *BookCollection, k string) (*Book, bool)
    FindBookCollectionByTitle(t *BookCollection, k string) (*Book, bool)

that both take a pointer to a book collection and a string representing an ISBN or a title (respectively), and returns a pointer to the book with that ISBN or title (or `nil` if not found), as as well as a Boolean flag indicated whether the key has been found.

The function should use binary search to find the book in the appropriate book tree.

