# Homework 2: JavaScript

* * * 



## Due Date: Monday, February 22, 2021 (23h59)


- This homework is to be done in teams of two. You're welcome to
discuss with other students, but all submitted work must be original
and your own. If you use a solution from another source you must cite
it &mdash; this includes when that source is someone else helping you.

- Link to a [homework2.zip](./homework2.zip) file containing support files to use as a starting point.


## Electronic Submission Instructions

- Zip all required files into a file `homework2.zip`.

- Make sure one of the files is a `README` text file listing the members of your team, as well as any remarks you want to make about your code.

- Send an email with your file `homework2.zip` as an attachment to `olin.submissions@gmail.com` with subject _Homework 2 submission_.


* * * 

In this homework, we'll practice working on simple JavaScript functions. We'll worry about using functions to modify an HTML document next time. For now, we'll just work on functions in isolation.

I've given you a file `homework2.js` that contains stubs for the functions I want you to write. You should add your code to that file, it's the one I'll be looking at. To develop and run the code in this homework, you have two ways:

**_Browser_**

You can use a web browser to run functions. I've given you a file `homework2.html` whose sole purpose is to load `homework2.js` (which should remain in the same folder as `homework2.html`) into the browser. Open `homework2.html` in your browser, and you should then be able to [open the developer's console](https://balsamiq.com/support/faqs/browserconsole/) and call those functions. 

Don't use Internet Explorer. That browser should be killed with fire.


**_NodeJS_**

You can install [NodeJS](https://nodejs.org/). NodeJS is a runtime environment and shell for JavaScript that doesn't require a browser. It is mostly compatible with JavaScript as implemented by browsers. Once you install NodeJS, you can start the shell and load `homework2.js` and start evaluating functions:

        $ node
        Welcome to Node.js v14.11.0.
        Type ".help" for more information.
        > .load homework2.js
        [...]
        > roundN(123, 1)
        120
        > .exit

If you use NodeJS, you don't need `homework2.html`. Note that you may have to quit the shell and restart it to reload a new version of your file homework2.js if you define `const` values in your file.


* * * 


## Question 1: Basic Functions

### (A)

Code a function `roundN(n, i)` which takes a number `n` and an integer `i` &ge; 0 and returns `n` rounded to the nearest 10<sup><code>i</code></sup>.

    > roundN(293847.6, 0)
    roundN(293847.6, 0)
    293848
    > roundN(293847.6, 1)
    roundN(293847.6, 1)
    293850
    > roundN(293847.6, 2)
    roundN(293847.6, 2)
    293800
    > roundN(293847.6, 3)
    roundN(293847.6, 3)
    294000
    > roundN(293847.6, 4)
    roundN(293847.6, 4)
    290000
    > roundN(293847.6, 5)
    roundN(293847.6, 5)
    300000
    > roundN(293847.6, 6)
    roundN(293847.6, 6)
    0


### (B)

Code a function `range(n, m)` which takes a number `n` and a number `m` and returns the array of all numbers between `n` (inclusive) and `m` (exclusive). The array should be empty if `n` &ge; `m`.

    > range(0, 10)
    [
      0, 1, 2, 3, 4,
      5, 6, 7, 8, 9
    ]
    > range(10, 20)
    [
      10, 11, 12, 13, 14,
      15, 16, 17, 18, 19
    ]
    > range(33, 34)
    [ 33 ]
    > range(33, 33)
    []
    > range(100, 0)
    []


### (C)

Code a function `positive(arr)` which takes an array of numbers `arr` and returns the array of numbers in `arr` that are positive (&ge; 0).

    > positive([])
    []
    > positive([1, 2, 3, 4])
    [ 1, 2, 3, 4 ]
    > positive([-1, -2, -3, -4])
    []
    > positive([-1, 2, -3, 4, -5, 6])
    [ 2, 4, 6 ]


### (D)

Code a function `positiveStr(s)` which takes a string `s` made up of integers separated by `;` and returns a string made up of all integers in `s` that are positive (&ge; 0) separated by `;`.

    > positiveStr('')
    ''
    > positiveStr('1')
    '1'
    > positiveStr('-1')
    ''
    > positiveStr('1;2;3;4')
    '1;2;3;4'
    > positiveStr('-1;-2;-3;-4')
    ''
    > positiveStr('-1;2;-3;4;-5;6')
    '2;4;6'


### (E)

Code a function `mapStr(s, sep, f)` which takes a string `s` made up of integers separated by a separating character `sep`, and a function `f`, and returns the string made up of the result of applying function `f` to every integer in `s`, all separated by `sep`.

    > mapStr('', ';', (x) => x * x)
    ''
    > mapStr('1', ';', (x) => x * x)
    '1'
    > mapStr('1;2;3', ';', (x) => x * x)
    '1;4;9'
    > mapStr('1|2|3|4|5', '|', (x) => x * x)
    '1|4|9|16|25'


* * *


## Question 2: Objects as Dictionaries

There is a sample array of objects `sample` and a sample object `sample_obj` provided in `homework2.js` that I used to generate sample outputs.


### (A)

Code a function `distinct(objs, fld)` which takes an array `objs` of objects and a string `fld` and returns the array of all _distinct_ values assigned to field `fld` in obejcts in `objs`.

    > distinct([], 'a')
    []
    > distinct(sample, 'a')
    [ 1, 10, 99 ]
    > distinct(sample, 'b')
    [ 2, 20, 66 ]
    > distinct(sample, 'c')
    [ 3, 30, 33 ]


### (B)

Code a function `sort(objs, fld)` which takes an array `objs` of objects and a string `fld` and returns the sorted array of all objects in `objs`, sorted by increasing value of their field `fld`.

Calling this function should not modify the original array or its content, so he resulting array should be a fresh array.

    > sort([], 'a')
    []
    > sort(sample, 'a')
    [
      { a: 1, b: 2, c: 3 },
      { a: 1, b: 20, c: 33 },
      { a: 10, b: 20, c: 30 },
      { a: 10, b: 66, c: 3 },
      { a: 99, b: 66, c: 33 },
      { a: 99, b: 2, c: 30 }
    ]
    > sort(sample, 'b')
    [
      { a: 1, b: 2, c: 3 },
      { a: 99, b: 2, c: 30 },
      { a: 10, b: 20, c: 30 },
      { a: 1, b: 20, c: 33 },
      { a: 99, b: 66, c: 33 },
      { a: 10, b: 66, c: 3 }
    ]
    > sort(sample, 'c')
    [
      { a: 1, b: 2, c: 3 },
      { a: 10, b: 66, c: 3 },
      { a: 10, b: 20, c: 30 },
      { a: 99, b: 2, c: 30 },
      { a: 99, b: 66, c: 33 },
      { a: 1, b: 20, c: 33 }
    ]


### (C)

Code a function `sum(objs, fld1, fld2)` which takes an array `objs` of objects and two strings `fld1` and `fld2` and returns an array of objects in which each object is an object of `objs` with an additional field `sum` whose value is the sum of the values of field `fld1` and `fld2` of the original object. 

Calling this function should not modify the original array or its content, so the resulting array should be a fresh array of fresh objects.

    > sum([], 'a', 'b')
    []
    > sum(sample, 'a', 'b')
    [
      { a: 1, b: 2, c: 3, sum: 3 },
      { a: 10, b: 20, c: 30, sum: 30 },
      { a: 99, b: 66, c: 33, sum: 165 },
      { a: 1, b: 20, c: 33, sum: 21 },
      { a: 10, b: 66, c: 3, sum: 76 },
      { a: 99, b: 2, c: 30, sum: 101 }
    ]
    > sum(sample, 'a', 'c')
    [
      { a: 1, b: 2, c: 3, sum: 4 },
      { a: 10, b: 20, c: 30, sum: 40 },
      { a: 99, b: 66, c: 33, sum: 132 },
      { a: 1, b: 20, c: 33, sum: 34 },
      { a: 10, b: 66, c: 3, sum: 13 },
      { a: 99, b: 2, c: 30, sum: 129 }
    ]
    > sum(sample, 'a', 'a')
    [
      { a: 1, b: 2, c: 3, sum: 2 },
      { a: 10, b: 20, c: 30, sum: 20 },
      { a: 99, b: 66, c: 33, sum: 198 },
      { a: 1, b: 20, c: 33, sum: 2 },
      { a: 10, b: 66, c: 3, sum: 20 },
      { a: 99, b: 2, c: 30, sum: 198 }
    ]


### (D)

Code a function `group(objs, fld)` which takes an array `objs` of objects and a string `fld` and returns a single object in which the keys are the distinct values associated to field `fld` in the objects in `objs`, and associated to each key `v` is the array of all objects in `objs` which have `v` as a value associated with field `fld`.

    > group([], 'a')
    {}
    > group(sample, 'a')
    {
      '1': [ { a: 1, b: 2, c: 3 }, { a: 1, b: 20, c: 33 } ],
      '10': [ { a: 10, b: 20, c: 30 }, { a: 10, b: 66, c: 3 } ],
      '99': [ { a: 99, b: 66, c: 33 }, { a: 99, b: 2, c: 30 } ]
    }
    > group(sample, 'b')
    {
      '2': [ { a: 1, b: 2, c: 3 }, { a: 99, b: 2, c: 30 } ],
      '20': [ { a: 10, b: 20, c: 30 }, { a: 1, b: 20, c: 33 } ],
      '66': [ { a: 99, b: 66, c: 33 }, { a: 10, b: 66, c: 3 } ]
    }
    > group(sample, 'c')
    {
      '3': [ { a: 1, b: 2, c: 3 }, { a: 10, b: 66, c: 3 } ],
      '30': [ { a: 10, b: 20, c: 30 }, { a: 99, b: 2, c: 30 } ],
      '33': [ { a: 99, b: 66, c: 33 }, { a: 1, b: 20, c: 33 } ]
    }


### (E)

Code a function `expand(obj, fld)` which takes an object `obj` and a string `fld` and returns an array of objects. You can assume that the value associated with field `fld` in `obj` is an array (call it `arr`). Each object of the resulting array is a copy of the original object, but with one of the values in `arr` associated with field `fld`.

    > expand(sample_obj, 'x')
    [
      { a: 33, b: 66, c: 99, x: 'this', y: [ 1, 2, 3 ], z: [] },
      { a: 33, b: 66, c: 99, x: 'is', y: [ 1, 2, 3 ], z: [] },
      { a: 33, b: 66, c: 99, x: 'a', y: [ 1, 2, 3 ], z: [] },
      { a: 33, b: 66, c: 99, x: 'string', y: [ 1, 2, 3 ], z: [] }
    ]
    > expand(sample_obj, 'y')
    [
      {
        a: 33,
        b: 66,
        c: 99,
        x: [ 'this', 'is', 'a', 'string' ],
        y: 1,
        z: []
      },
      {
        a: 33,
        b: 66,
        c: 99,
        x: [ 'this', 'is', 'a', 'string' ],
        y: 2,
        z: []
      },
      {
        a: 33,
        b: 66,
        c: 99,
        x: [ 'this', 'is', 'a', 'string' ],
        y: 3,
        z: []
      }
    ]
    > expand(sample_obj, 'z')
    []



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

There is a sample binary tree `sample_tree` provided in `homework2.js` that I used to generate sample outputs:

<center style="padding:20px;"><img style="width: 50%; padding: 10px;" src="tree.png"></center>


**Hint**: for pretty much all of these questions, the answer will use recursion. Why? Because trees are recursive structures. (A tree is either empty or a node with two trees hanging off of it.) If you're looking for a challenge, try to implement the methods below without using recursion. You'll probably need to introduce the aforementioned superclass `BinTree` if you go that route.



### (A)

Code a method `height()` in binary trees which returns the number of nodes in the longest path in the tree.

    > new Empty().height()
    0
    > new Node(10, new Empty(), new Empty()).height()
    1
    > sample_tree.height()
    4


### (B)

Code a method `fringe()` in binary trees which returns the list of values at the leaf nodes in the tree. A node is a leaf node if its left and right subtrees are both empty. The order of the resulting list is not important, but repetitions should be allowed.

    > new Empty().fringe()
    []
    > new Node(10, new Empty(), new Empty()).fringe()
    [ 10 ]
    > sample_tree.fringe()
    [ 80, 90, 100, 110, 60, 70 ]


### (C)

A preorder traversal of a tree is a way to touch every value in a tree in such a way that you touch the value at a node before you traverse the left subtree and then the right subtree of the node. What "touching" the value means depends on the context.

Code a method `preorder(f)` in binary trees which does a preorder traversal of the tree, and calls function `f` on the value at a node whenever the traversal touches the value.

    > const test = (v) => { console.log('value = ', v) }
    undefined
    > new Empty().preorder(test)
    undefined
    > new Node(10, new Empty(), new Empty()).preorder(test)
    value =  10
    undefined
    > sample_tree.preorder(test)
    value =  10
    value =  20
    value =  40
    value =  80
    value =  90
    value =  50
    value =  100
    value =  110
    value =  30
    value =  60
    value =  70
    undefined


### (D)

Code a method `map(f)` in binary trees which returns a new tree with the exact same shape as the original tree, but where the value at each node is the result of applying `f` to the value in the corresponding node in the original tree.

    > new Empty().map((v) => v * v)
    Empty {}
    > new Node(10, new Empty(), new Empty()).map((v) => v * v)
    Node { value: 100, left: Empty {}, right: Empty {} }
    > sample_tree.map((v) => v * v)
    Node {
      value: 100,
      left: Node {
        value: 400,
        left: Node { value: 1600, left: [Node], right: [Node] },
        right: Node { value: 2500, left: [Node], right: [Node] }
      },
      right: Node {
        value: 900,
        left: Node { value: 3600, left: Empty {}, right: Empty {} },
        right: Node { value: 4900, left: Empty {}, right: Empty {} }
      }
    }


### (E)

Code a method `trim()` in binary trees which return a new tree in which every leaf of the original tree has been removed. For the sake of simplicity, trimming an empty tree should return an empty tree.

    > new Empty().trim()
    Empty {}
    > new Node(10, new Empty(), new Empty()).trim()
    Empty {}
    > sample_tree.trim()
    Node {
      value: 10,
      left: Node {
        value: 20,
        left: Node { value: 40, left: Empty {}, right: Empty {} },
        right: Node { value: 50, left: Empty {}, right: Empty {} }
      },
      right: Node { value: 30, left: Empty {}, right: Empty {} }
    }


* * *

## Question 4: Additional question for teams of 3

### (A)

Another representation for a tree is as a JSON object. 

A JSON object representation of a tree is as a node

    {
       value: ...,
       left: ...,
       right: ...
    }

where `value` holds the value at the node, `left` (if present) holds the left subtree (as a JSON object), and `right` (if present) holds the right subtree (as a JSON object). Note that `left` or `right` is only present if there is a corresponding subtree. A leaf therefore would be represented as:

    {
       value: ...
    }
    
while a tree with only a right subtree would be represented as:

    {
       value: ...,
       right: ...
    }
    
and so on.

Code a method `toJSON()` in the `Empty` and `Subtree` class that returns the tree in the JSON representation above. The JSON representation of the empty tree should be `{}`.

    > new Empty().toJSON()
    {}
    > new Node(10, new Empty(), new Empty()).toJSON()
    { value: 10 }
    > sample_tree.toJSON()
    {
      value: 10,
      left: {
        value: 20,
        left: { value: 40, left: [Object], right: [Object] },
        right: { value: 50, left: [Object], right: [Object] }
      },
      right: { value: 30, left: { value: 60 }, right: { value: 70 } }
    }
    > console.log(JSON.stringify(sample_tree.toJSON(), null, 2))
    {
      "value": 10,
      "left": {
        "value": 20,
        "left": {
          "value": 40,
          "left": {
            "value": 80
          },
          "right": {
            "value": 90
          }
        },
        "right": {
          "value": 50,
          "left": {
            "value": 100
          },
          "right": {
            "value": 110
          }
        }
      },
      "right": {
        "value": 30,
        "left": {
          "value": 60
        },
        "right": {
          "value": 70
        }
      }
    }
    undefined


### (B)

Code a function `fromJSON(j)` taking a JSON object `j` in the JSON format described in question (A) and constructs the corresponding tree using `Empty` and `Node` classes.

    > fromJSON({value: 1})
    Node { value: 1, left: Empty {}, right: Empty {} }
    > fromJSON({value: 1, left: {value: 2, left: { value: 3}}, right: {value: 4, right: { value: 5}}})
    Node {
      value: 1,
      left: Node {
        value: 2,
        left: Node { value: 3, left: Empty {}, right: Empty {} },
        right: Empty {}
      },
      right: Node {
        value: 4,
        left: Empty {},
        right: Node { value: 5, left: Empty {}, right: Empty {} }
      }
    }


### (C)

Another representation for a tree is as an array of nodes, where each node is described by an object:

    { 
       id: ...,
       value: ...,
       left: ...,
       right: ...
    }
    
where `id` is the name of the node, `value` is the value at the node, `left` (if present) is the name of the node making up the left child, and `right` (if present) is the name of the node making up the right child. The root is whichever node does not appear as the child of any other node in the array.

For instance, here is the array representation for a tree with 10 at the root, 20 in the left child, and 30 in the right child:

    [
       {
          id: 'root',
          value: 10,
          left: 'left-child',
          right: 'right-child'
       },
       {
          id: 'left-child',
          value: 20
       },
       {
          id: 'right-child',
          value: 30
       }
    ]
    
Names don't have to be meaninful, and the root need not be the first element of the array.

Code a function `fromArray(arr)` taking an array in the above representation and constructs the corresponding tree using `Empty` and `Node` classes.

Using the sample arrays:

    const sample_arr_1 = [
        {id: 'a', value: 1, left: 'b', right: 'c'},
        {id: 'b', value: 2, left: 'd', right: 'e'},
        {id: 'c', value: 3, left: 'f', right: 'g'},
        {id: 'd', value: 4, left: 'h'},
        {id: 'e', value: 5, right: 'i'},
        {id: 'f', value: 6},
        {id: 'g', value: 7},
        {id: 'h', value: 8},
        {id: 'i', value: 9}
    ]
    
    const sample_arr_2 = [
        {id: 'john', value: 'L'},
        {id: 'paul', value: 'M'},
        {id: 'george', value: 'H', left: 'john', right: 'paul'},
        {id: 'ringo', value: 'S', left: 'george', right: 'george'},
    ]

here are some sample outputs:

    > fromArray(sample_arr_1)
    Node {
      value: 1,
      left: Node {
        value: 2,
        left: Node { value: 4, left: [Node], right: Empty {} },
        right: Node { value: 5, left: Empty {}, right: [Node] }
      },
      right: Node {
        value: 3,
        left: Node { value: 6, left: Empty {}, right: Empty {} },
        right: Node { value: 7, left: Empty {}, right: Empty {} }
      }
    }
    > fromArray(sample_arr_2)
    Node {
      value: 'S',
      left: Node {
        value: 'H',
        left: Node { value: 'L', left: Empty {}, right: Empty {} },
        right: Node { value: 'M', left: Empty {}, right: Empty {} }
      },
      right: Node {
        value: 'H',
        left: Node { value: 'L', left: Empty {}, right: Empty {} },
        right: Node { value: 'M', left: Empty {}, right: Empty {} }
      }
    }

          


