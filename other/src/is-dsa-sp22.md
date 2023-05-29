
# IS Data Structures & Algorithms (Spring 2022)


In readings, CLR is Cormen, Leiserson, Rivest, _Introduction to Algorithms, Third Edition_, available
as an [e-book from Olin Library](http://olin.idm.oclc.org/login?url=https://search.ebscohost.com/login.aspx?direct=true&db=nlebk&AN=343613&site=eds-live).

***

## Task 1: Balanced binary search trees (February 28)

### Red-black trees

**Readings:** CLR, Chapter 12 (Binary Search Trees) and 13 (Red-Black Trees)

Code a Python class `RedBlackTree` implementing red-black trees where nodes hold an arbitrary
value. 

The constructor of the tree should take an optional `key` parameter which is a function that can be
used by the tree to extract from each value stored in the tree the "key" that it uses to order the
entries. If `key` is not given, then the value as a whole should be as a key. 

For example, 

    RedBlackTree()

should create an empty red-black tree which can store arbitrary values and for which the value as a whole is the key used to determine where the value is stored in the tree. In contrast,

    RedBlackTree(key=lambda obj: obj.name)

creates an empty red-black tree which can store arbitrary objects, and where the value of field `name`
in each object is used as the key to determine where the object is stored in the tree.

Red-black trees should support the following operations:

- `search(k)`  returning the value stored in the node at key `k`, or `None` if no such key is found
- `insert(v)`  inserting value `v` in the tree using the key computed from `v` as the key, overwriting any existing value at the same key
- `delete(k)`  deleting the node with key `k` if it exists


### AVL trees

**Readings:** [_An Algorithm for the Organization of Information_](./avl.pdf); Pfenning's [lecture notes on AVL trees](https://www.cs.cmu.edu/~fp/courses/15122-f15/lectures/16-avl.pdf); [Wikipedia](https://en.wikipedia.org/wiki/AVL_tree#Delete) for deletion from an AVL tree

Code a Python class `AVLTree` implementing AVL trees where nodes hold an arbitrary value. 

AVL trees should work exactly like red-black trees. In other words, the constructor for AVL trees
should take an optional `key` parameter used to describe how the key of each value is computed, and
AVL trees should support operations `search(k)`, `insert(v)`, and `delete(k)` operations.


***

## Task 2: Splay trees (March 7)

**Readings:** [_Self-Adjusting Binary Search Trees_](./self-adjusting.pdf), Sections 1&ndash;4. You can skim the proofs to understand why the trees support efficient access and update.

Code a Python class `SplayTree` implement splay trees as described in the Sleator-Tarjan paper.

Let's try to simplify the whole value/key thing from last time. 

Every node in the tree has a key (which is what you order by) and a stored value.

You store values in the tree by inserting them using a key. So when you insert, you provide a key
and a value. When you search, you search by key.

Splay trees should support the following operations:

- `search(k)`  returning the value stored in the node at key `k`, or `None` if no such key is found
- `insert(k, v)`  inserting value `v` in the tree at key `k`, overwriting any existing value at the same key
- `delete(k)`  deleting the node with key `k` if it exists

In other words, if you create an empty tree:

    t = SplayTree()
    
and insert value `{"a": 10, "b": 20, "c": 30}` in the tree at key `test`:

    t.insert("test", {"a": 10, "b": 20, "c": 30})
    
and then search for the value stored at key `test`, you should get back the value you stored:

    >>> t.search("test")
    {"a": 10, "b": 20, "c": 30}
    

***

## Task 3: B-trees (March 28)

Readings: CLR, Chapter 18

Code a Python class `BTree` implementing B-trees as described in CLR. 

Parameterize your implementaton with a parameter _t_ that controls how many keys the nodes in the B-tree contains. Don't worry about the `Disk-Write` and `Disk-Read` operations.

Every node in the tree has between _t_ and _2t_ keys (keys are what you order by) and associates with
each key in the node a stored value. You store values in the tree by inserting them using a key: you
insert, you provide a key and a value. When you search, you search by key and recover the associated
value.

The constructor for B-trees should take the parameter _t_ as an argument.

B-trees should support the following operations:

- `search(k)`  returning the value stored in the tree at key `k`, or `None` if no such key is found
- `insert(k, v)`  inserting value `v` in the tree at key `k`, overwriting any existing value at the same key
- `delete(k)`  deleting the key `k` if it exists

In other words, if you create an empty tree (taking _t_ = 2, which gives you a so-called 2-3-4 tree) :

    tr = BTree(2)
    
and insert value `{"a": 10, "b": 20, "c": 30}` in the tree at key `test`: 

    tr.insert("test", {"a": 10, "b": 20, "c": 30})
    
and then search for the value stored at key `test`, you should get back the value you stored:

    >>> tr.search("test")
    {"a": 10, "b": 20, "c": 30}

***

## Task 4: Hash Tables (April 14)

Readings: CLR, Chapter 11

### (1) HashTable class

Code a Python class `HashTable` implementing a hash table with chaining. 

Use a Python array to implement the hash table.

Parameterize the constructor `HashTable(size)` with a initial size for the hash table, that is, the initial number of cells in the array.

As with search trees, you want to associate keys with values, so that you store values in the hashtable using a given key, and search for values by searching for a given key. So that you can store value `hello` at key 1 and value `world` at key 2, and search for the value associated with key 1, etc. The hash is computed on the key.

Implement a method `insert(k, v)` that inserts value `v` at key `k` in the hash table. If there is already an entry with that key in the hash table, the new value should now be associated with the key, overwriting the old one.

Implement a method `search(k)` that returns the value associated with key `k` in the hash table (`None` if there is none.)

In other words, if you create a hash table of size 10:

    ht = HashTable(10)
    
and insert value `hello` in the table at key 1 and value `world` in the table at key 2:

    ht.insert(1, "hello")
    ht.insert(2, "world")
    
and then search for the value stored at key 1, you should get back the value you stored:

    >>> ht.search(1)
    hello

The hash table should implement chaining, that is, associated with each cell of the hash table is a sorted list (sorted by key) of all the entries with the same hash for the key.

You can use Python's built-in `hash()` function to obtain a hash corresponding to a value. That should work for most of your uses.


### (2) Growing tables

Modify your implementation of hash tables in (1) so that the table "grows" when it gets too full.

In particular, you can track how many items you have added to the hash table, and when after you insert a value the ratio of items in the table over the size of the table is > 0.75, you create a new array that is twice the size of the original array, and rehash everything you stored in the original array into the new array. This should distribute the items differently in the new array.

***

## Task 5: Hash Tables, Continued (April 25)

Readings: CLR, Chapter 11

Continuing from last week. 

### (3) Closed Hashing

Code a Python class `OpenHashTable` implementing a hash table with closed hashing (also known as open addressing).

Use a Python array to implement the hash table.

Parameterize the constructor `OpenHashTable(size)` with a initial size for the hash table, that is, the initial number of cells in the array.

As with search trees, you want to associate keys with values, so that you store values in the hashtable using a given key, and search for values by searching for a given key. So that you can store value `hello` at key 1 and value `world` at key 2, and search for the value associated with key 1, etc. The hash is computed on the key.

Implement a method `insert(k, v)` that inserts value `v` at key `k` in the hash table. If there is already an entry with that key in the hash table, the new value should now be associated with the key, overwriting the old one.

Implement a method `search(k)` that returns the value associated with key `k` in the hash table (`None` if there is none.)

In other words, if you create a hash table of size 10:

    ht = OpenHashTable(10)
    
and insert value `hello` in the table at key 1 and value `world` in the table at key 2:

    ht.insert(1, "hello")
    ht.insert(2, "world")
    
and then search for the value stored at key 1, you should get back the value you stored:

    >>> ht.search(1)
    hello

The hash table should implement closed hashing, that is, all the items are stored in the array proper. See <https://opendsa-server.cs.vt.edu/ODSA/Books/CS3/html/HashCSimple.html> for an overview.

Use a linear probe function to find a new position in the array if the position where the hash function tells you to put the item is already taken. 


### (4) Growing tables

Modify your implementation of hash tables in (2) so that the table "grows" when it gets too full.

In particular, you can track how many items you have added to the hash table, and when after you insert a value the ratio of items in the table over the size of the table is > 0.75, you create a new array that is twice the size of the original array, and rehash everything you stored in the original array into the new array. This should distribute the items differently in the new array.


### (5) Alternative probe functions

Parameterize your `OpenHashTable` constructor so that it takes as an optional parameter the probe function to use. (By default, it should be linear.) Try it with a quadratic probe function, and with double hashing. See <https://opendsa-server.cs.vt.edu/ODSA/Books/CS3/html/HashCImproved.html> for an overview.


### (6) Timings

Run some experiments to see how your hash tables from Task 4 and from Task 5 perform. 

One way is to first generate a random sequence of maybe a thousand or so keys with an associated value (the value is not particularly important), and then for each of your hash table implementations (open hashing, and closed hashing with different probe functions), do the following a hundred or so times: initialize the hash table with a small size (say, 100), measure the time taken to insert all keys, and take the average time taken over all runs. Compare each of the hash table implementations, and modify the sizes of the sequence of keys and the initial table sizes, etc. 

