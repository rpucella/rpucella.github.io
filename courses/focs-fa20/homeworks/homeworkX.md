# Homework X


## Question 1: Higher-Order Functions

In this quesion, you will be working with [higher order functions](../notes-functional.html).

For each function I ask you to code, full points will be awarded if
the function and its helpers do not use explicit recursion. In other
words, the function and its helpers should not use the `rec` keyword
for function definitions.

This means that you will have to use `List.map`, `List.filter`,
`List.fold_right`, or some other higher-order functions in the [`List`
module](https://caml.inria.fr/pub/docs/manual-ocaml/libref/List.html). One
additional function you may find useful is `List.map2`, which acts
like `List.map` but maps over two lists, passing elements of both
lists to the mapping function and producing a single list as result.

To illustrate, here is the `squares` function defined using explicit recursion:

    let rec squares (xs: int list): int list =
      match xs with
      | [] -> []
      | x::xs' -> (x * x) :: (squares xs')

Here is the `squares` function defined without using explicit recursion:

    let squares (xs: int list): int list =
      List.map (fun x -> x * x) xs

For another illustration, here is the `length` function defined using explicit recursion:

    let rec length (xs: 'a list): int =
      match xs with
      | [] -> 0
      | x::xs' -> 1 + length xs

Here is the length function defined without using explicit recursion:

    let length (xs: 'a list): int =
      List.fold_right (fun x r -> 1 + r) xs 0

If you can't get a function to work without explicit recursion, please
go ahead and implement a version with explicit recursion.


### (A)

Code a function **`long`** of type **`string list -> string list`** where `long xs` returns the list obtained by removing all strings of length strictly less than 2 from `xs`.

Sample output:

    # long [];;
    - : string list = []
    # long [""];;
    - : string list = []
    # long [""; "a"];;
    - : string list = []
    # long [""; "a"; "ab"];;
    - : string list = ["ab"]
    # long [""; "a"; "ab"; "abc"];;
    - : string list = ["ab"; "abc"]
    # long [""; "a"; "ab"; "abc"; "abcd"];;
    - : string list = ["ab"; "abc"; "abcd"]
    # long [""; "a"; "ab"; "abc"; "abcd"; "abc"; "ab"; "b"; ""];;
    - : string list = ["ab"; "abc"; "abcd"; "abc"; "ab"]


### (B)

Code a function **`words1`** of type **`int list -> string list`** where `words1 xs` returns the list obtained by transforming every 1 in `xs` into the string `one`, every 2 in `xs` into the string `two`, every 3 in `xs` into the string `three`, and every other integer into the string `other`.

Sample output:

    # words1 [];;
    - : string list = []
    # words1 [1];;
    - : string list = ["one"]
    # words1 [2];;
    - : string list = ["two"]
    # words1 [3];;
    - : string list = ["three"]
    # words1 [99];;
    - : string list = ["other"]
    # words1 [1; 2; 3; 2; 1; 2; 3];;
    - : string list = ["one"; "two"; "three"; "two"; "one"; "two"; "three"]
    # words1 [1; 99; 2; 99; 3; 99; 2; 99; 1; 99; 2; 99; 3];;
    - : string list =
    ["one"; "other"; "two"; "other"; "three"; "other"; "two"; "other"; "one"; "other"; "two"; "other"; "three"]


### (C)

Code a function **`words2`** of type **`int list -> string`** where `words2 xs` returns the string obtained by concatenating a "zero-" for every 0 in `xs` and a "one-" for every ` in `xs`, ignoring every other integer.

Sample output:

    # words2 [];;
    - : string = ""
    # words2 [0];;
    - : string = "zero-"
    # words2 [1];;
    - : string = "one-"
    # words2 [99];;
    - : string = ""
    # words2 [0; 1];;
    - : string = "zero-one-"
    # words2 [0; 1; 0; 1; 0];;
    - : string = "zero-one-zero-one-zero-"
    # words2 [0; 99; 1; 99; 0; 99; 1; 99; 0];;
    - : string = "zero-one-zero-one-zero-"


### (D)

Code a function **`unique`** of type **`string list -> string list`** where `unique xs` returns the list of unique strings in `xs`. 

Sample output:

    # unique [];;
    - : string list = []
    # unique [""];;
    - : string list = [""]
    # unique ["a"];;
    - : string list = ["a"]
    # unique ["b"];;
    - : string list = ["b"]
    # unique [""; "a"; "b"];;
    - : string list = [""; "a"; "b"]
    # unique [""; "a"; "b"; ""; "a"; "b"];;
    - : string list = [""; "a"; "b"]
    # unique [""; "a"; "b"; ""; "a"; "b"; "ab"];;
    - : string list = [""; "a"; "b"; "ab"]
    # unique [""; "a"; "b"; ""; "a"; "b"; "ab"; "ab"];;
    - : string list = [""; "a"; "b"; "ab"]
    # unique ["ab"; "ba"; ""; "a"; "b"; ""; "a"; "b"; "ab"; "ab"];;
    - : string list = ["ba"; ""; "a"; "b"; "ab"]



### (E)

Code a function **`mix`** of type **`'a list -> 'a list -> 'a list`** where `mix xs ys` return the list obtained by taking every element of `xs` and following each such element by the elements from `ys`.

Sample output:

    # mix [] [];;
    - : 'a list = []
    # mix [] [1; 2; 3];;
    - : int list = []
    # mix [100; 200; 300] [1; 2; 3];;
    - : int list = [100; 1; 2; 3; 200; 1; 2; 3; 300; 1; 2; 3]
    # mix [100; 200; 300] [];;
    - : int list = [100; 200; 300]
    # mix [100; 200; 300] [1];;
    - : int list = [100; 1; 200; 1; 300; 1]
    # mix [100; 200; 300] [1; 2];;
    - : int list = [100; 1; 2; 200; 1; 2; 300; 1; 2]


* * *

## Question 2: Turing Machines

For questions 2 and 3, use the implementation of Turing machines provided (and taken from Homework 4).


### (A)

Code a Turing machine **`same`** which accepts all strings over
{0, 1} that contain the same number of 0s and 1s.

Thus, the machine should accept the empty string as well as the following
strings:

    01
    10
    0011
    0101
    1001
    10001011

and reject the following strings:

    0
    1
    001
    101
    1000111


### (B)

Code a Turing machine **`count`** which accepts all strings of the form `0`<sup>n</sup>`#`w where w is the binary representation of n, and rejects every other string. (Notation `0`<sup>n</sup> represents the sequence of n `0`s, with `0`<sup>0</sup> being the empty string.)

The machine should accept the following strings:

    #0
    0#1
    00#10
    000#11
    0000#100
    00000#101
    000000#110
    0000000#111
    00000000#1000

and reject the following strings:

    #
    0#
    00
    01
    #1
    0#10
    0#11
    0#111
    00#1
    00#11


**Hint**: modify `plus1` from homework 4.


* * *

## Question 3: Generating Turing Machines

Code a function **`copies`** of type **`int -> tm`** where `copies n` (for n &gt; 0) creates a Turing machine that accepts all strings of the form w`#`w`#`w`#`...`#`w  whith n copies of w, for any w over {0, 1}.

Thus, `copies 1` should create a Turing machine which accepts only and all strings over {0, 1}, `copies 2` should create a Turing machine which accepts only and all strings of the form w`#`w (for any w over {0, 1}), `copies 3` should create a Turing machine which accepts only and all strings of the form w`#`w`#`w (for any w over {0, 1}), and so on.

**Hint**: figure out the TMs that `copies 1`, `copies 2`, `copies 3`, `copies 4`, etc should create, then spot the pattern, then implement the pattern.
