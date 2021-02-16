# Homework 2: JavaScript

* * * 



## Due Date: Monday, February 22, 2021 (23h59)


- This homework is to be done in teams of two. You're welcome to
discuss with other students, but all submitted work must be original
and your own. If you use a solution from another source you must cite
it &mdash; this includes when that source is someone else helping you.

- Link to a [homework1.zip](./homework2.zip) file containing support files to use as a starting point.


## Electronic Submission Instructions

- Zip all required files into a file `homework2.zip`.

- Make sure one of the files is a `README` text file listing the members of your team, as well as any remarks you want to make about your code.

- Send an email with your file `homework2.zip` as an attachment to `olin.submissions@gmail.com` with subject _Homework 2 submission_.


* * * 


## Question 1: Basic Functions

### (A)

Code a function `roundN(n, i)` which takes a number `n` and a non-zero integer `i` and returns n rounded to the nearest 10<sup><code>i</code></sup>.

### (B)

Code a function `range(n, m)` which takes a number `n` and a number `m` and returns the array of all numbers between `n` (inclusive) and `m` (exclusive). The array should be empty if `n` &ge; `m`.


### (C)

Code a function `positive(lst)` which takes an array of numbers `lst` and returns the array of numbers in `lst` that are positive (&ge; 0).


### (D)

Code a function `positiveStr(s)` which takes a string `s` made up of integers separated by `;` and returns a string made up of all integers in `s` that are positive (&ge; 0) separated by `;`.


### (E)

Code a function `mapStr(s, sep, f)` which takes a string `s` made up of integers separated by a separating character `sep`, and a function `f`, and returns the string made up of the result of applying function `f` to every integer in `s`, all separated by `sep`.




* * *


## Question 2: Objects as Dictionaries


### (A)

Code a function `distinct(objs, fld)` which takes an array `objs` of objects and a string `fld` and returns the array of all _distinct_ values assigned to field `fld` in obejcts in `objs`.


### (B)

Code a function `sort(objs, fld)` which takes an array `objs` of objects and a string `fld` and returns the sorted array of all objects in `objs`, sorted by increasing value of their field `fld`.

The resulting array of objects should not share any object with the original array (that is, modifying the value associated with a top-level key in any object in the original array should not affect the resulting array).


### (C)

Code a function `sum(objs, fld1, fld2)` which takes an array `objs` of objects and two strings `fld1` and `fld2` and returns an array of objects in which each object is an object of `objs` with an additional field `sum` whose value is the sum of the values of field `fld1` and `fld2` of the original object. 

The resulting array of objects should not share any object with the original array (that is, modifying the value associated with a top-level key in any object in the original array should not affect the resulting array).


### (D)

Code a function `group(objs, fld)` which takes an array `objs` of objects and a string `fld` and returns a single object in which the keys are the distinct values associated to field `fld` in the objects in `objs`, and associated to each key `v` is the array of all objects in `objs` which have `v` as a value associated with field `fld`.

It is fine if the resulting object shares objects with the original array. 


### (E)

Code a function `expand(obj, fld)` which takes an object `obj` and a string `fld` and returns an array of objects. You can assume that the value associated with field `fld` in `obj` is an array (call it `arr`). Each object of the resulting array is a copy of the original object, but with one of the values in `arr` associated with field `fld`.

The resulting array of objects should not share any top-level key value with the original object (that is, modifying the value associated with a top-level key in the original object should not affect the resulting array).


* * *


## Question 3: Classes

Consider the following two classes `Empty` and `Node` used to implement binary trees with a value stored at each node.
You can think of them as subclasses of an abstract `BinTree` class, which I didn't bother writing down.

    class Empty {
        
        isEmpty() {
            return true
        }

        size() {
            return 0
        }
    }


    class Node {
        
        constructor(value, left, right) {
            this.value = value
            this.left = left
            this.right = right
        }
    
        isEmpty() {
            return false
        }
        
        size() {
            return 1 + this.left.size() + this.right.size()
        }
    }

Naturally enough, `new Empty()` creates an empty tree, while `new Node(v, lft, rgt)` creates a tree with value `v` at the root and with left and right subtrees `lft` and `rgt` respectively.

Two methods are provided: `isEmpty()` checks if a tree is empty, while `size()` returns the total number of nodes in a tree.

**Hint**: for pretty much all of these questions, the answer will use recursion. Why? Because trees are recursive structures. (A tree is either empty or a node with two trees hanging off of it.) If you're looking for a challenge, try to implement the methods below without using recursion.


### (A)

Code a method `height()` in binary trees which returns the number of nodes in the longest path in the tree.


### (B)

Code a method `fringe()` in binary trees which returns the list of values at the leaf nodes in the tree. A node is a leaf node if its left and right subtrees are both empty. The order of the resulting list is not important, but repetitions should be allowed.


### (C)

A preorder traversal of a tree is a way to touch every value in a tree in such a way that you touch the value at a node before you traverse the left subtree and then the right subtree of the node. What "touching" the value means depends on the context.

Code a method `preorder(f)` in binary trees which does a preorder traversal of the tree, and calls function `f` on the value at a node whenever the traversal touches the value.


### (D)

Code a method `map(f)` in binary trees which returns a new tree with the exact same shape as the original tree, but where the value at each node is the result of applying `f` to the value in the corresponding node in the original tree.


### (E)

Code a method `trim()` in binary trees which return a new tree in which every leaf of the original tree has been removed. For the sake of simplicity, trimming an empty tree should return an empty tree.


* * *

## Question 4: Additional question for teams of 3

_Coming soon_
