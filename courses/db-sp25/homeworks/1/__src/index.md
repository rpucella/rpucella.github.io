<script>
  document.title = 'Homework 1 - DB SP25'
</script>

# Homework 1

## Due Date: Friday, February 7, 2025 (23h59)


- This homework is to be done individually. You may discuss problems
with fellow students, but all submitted work must be entirely your
own, and should not be from any other course, present, past, or
future. If you use a solution from another source you must cite it. 
That includes when that source is someone else helping you.

- **Please do not post your solutions on a public website or a public repository like GitHub.**

- All programming is to be done in Go (version at least 20).

- Code your answers by modifying the file [`homework1.go`](homework1.go) provided. Add your **name**, your **email address**, and any **remarks** that you
wish to make to the instructor to the block comment at the head of the file.

- **Please do not change the types in the signature of the function stubs I provide**. Doing so will make it
impossible to load your code into the testing infrastructure, and generall make me grumpy.

- Feel free to define and use helper functions if you need them.


## Electronic Submission Instructions

- Make sure your code compiles without errors. Run 

        go run homework1.go

- If there are any error, do not submit. I can't test what I can't compile.

- Submission done via the course <a href="https://canvas.olin.edu">Canvas</a> site.


* * *


## Question 1: Basic Exercises

For the functions in the section and the following ones, Please use `panic` if you need to
report an error. It causes a runtime failure. Its usage is simple:

    panic("some string representing a panic message")

That'll do for now. Go's `panic` primitive is a big hammer, and is basically an uncatchable
exception. Idiomatic Go code does not use `panic` but instead uses error return values to indicate and
propagate errors. (Go does not have an exception mechanism as a specific design feature.) We'll
return to errors in future homeworks.

Some [information on panic](https://gobyexample.com/panic).


### (A)

Code a function

    func Clamp(a int, b int, v int) int

where `Clamp(a, b, v)` returns `v` if `v` is between `a` and `b` (inclusive), and otherwise returns the smallest of `a` and `b` if `v` is smaller, or the largest of `a` and `b` if `v` is larger. In most uses, `a` would be the smaller value and `b` would be the larger value.

    Clamp(10, 20, 15) ⟶ 15
    Clamp(10, 20, 10) ⟶ 10
    Clamp(10, 20, 20) ⟶ 20
    Clamp(10, 20, 5) ⟶ 10
    Clamp(10, 20, 25) ⟶ 20
    Clamp(20, 10, 25) ⟶ 20


### (B)

Code a function

    func Interpolate(a float32, b float32, v float32) float32 
    
where `Interpolate(a, b, v)` maps the interval `[0, 1]` to the interval `[a, b]`, and returns the
value corresponding to value `v` with respect `[0, 1]` to the corresponding value with respect to
`[a, b]`. Thus, `0.5` is in the middle of `[0, 1]`, so it should map to `15` with respect to `[10, 20]` (or `[20, 10]`). 

Note that `v` could be outside `[0, 1]`. So 1.5 with respect to `[0, 1]` is halfway past the end
point, so corresponds to 25 with respect to `[10, 20]`, and corresponds to 30 with respect to `[0, 20]`. It also corresponds to 5 with respect to `[20, 10]`. 

This is all about ratios.

	Interpolate(10.0, 20.0, 0) ⟶ 10
	Interpolate(10.0, 20.0, 1) ⟶ 20
	Interpolate(10.0, 20.0, 0.5) ⟶ 15
	Interpolate(10.0, 20.0, 0.25) ⟶ 12.5
	Interpolate(10.0, 20.0, 1.5) ⟶ 25
	Interpolate(20.0, 10.0, 1.5) ⟶ 5


### (C)

Code a function

    func Spaces(n int) string
    
which a string made up exclusively of `n` spaces (when `n` is at least 0).

(In these sample outputs, I'm using ␣ to represent a space)

	Spaces(0) ⟶ 
	Spaces(3) ⟶ ␣␣␣

**Hint**: + is used for string concatenation.

### (D)

Code functions

    func PadLeft(s string, n int) string 
    
    func PadRight(s string, n int) string

which return the string `s` padded to the left or to the right (respectively) with spaces up to total
length `n`. 

If the string is already at lenth `n` or longer, the result is just the original string.

(In these sample outputs, I'm using ␣ to represent a space)

	PadLeft("test", 10) ⟶ ␣␣␣␣␣␣test
	PadLeft("this is a longer test", 10) ⟶ this is a longer test
	PadRight("test", 10) ⟶ test␣␣␣␣␣␣
	PadRight("this is a longer test", 10) ⟶ this is a longer test

**Hint**: Use `Spaces`

### (E)

Code a function

    func PadBoth(s string, n int) string 
    
which returns the string `s` padded to the left and to the right (equally) with spaces up to a total
length `n`. (It's a form of centering.) If the number of spaces to the left and to the right of the
string cannot be equal to make up length `n`, then favor adding one more space to the right than to
the left. 

If the string is already at length `n` or longer, the result is just the original string.

	PadBoth("test", 10) ⟶ ␣␣␣test␣␣␣
	PadBoth("this is a longer test", 10) ⟶ this is a longer test
	PadBoth("a", 3) ⟶ ␣a␣
	PadBoth("a", 4) ⟶ ␣a␣␣
	PadBoth("ab", 3) ⟶ ab␣
	PadBoth("ab", 4) ⟶ ␣ab␣

**Hint**: Use `Spaces`


* * *


## Question 2: 3D Vectors

A 3D vector is a sequence of three floating point numbers denoting a spatial position in 3D
space. (Yeah, I know, that's the defintion of a vector in an 3D Euclidean space, as opposed to a vector
in a general vector space. Bear with me.)

We represent a 3D vector as an array of three `float32` values.

### (A)

Code a function

    func NewVec(n1 float32, n2 float32 n3 float32) [3]float32 

which returns a new vector where `n1`, `n2`, and `n3` represent the three coordinates of the vector.

	NewVec(42, 42, 42) ⟶ [42 42 42]]
	NewVec(1, 2, 3) ⟶ [1 2 3]


### (B)

Code a function

    func ScaleVec(sc float32, v1 [3]float32) [3]float32 
    
which returns a *new* vector in which every coordinate of `v1` is multiplied by
`sc`.

	var v1 [3]float32 = [3]float32{1.0, 2.0, 3.0}
	ScaleVec(1.0, v1) ⟶ [1 2 3]
	ScaleVec(2.0, v1) ⟶ [2 4 6]
	ScaleVec(-2.0, v1) ⟶ [-2 -4 -6]

### (C)

Code a function

    func AddVec(v1 [3]float32, v2 [3]float32) [3]float32 
    
which returns a *new* vector in which every coordinate is the sum of the
corresponding coordinates of `v1` and `v2`.

	var v1 [3]float32 = [3]float32{1.0, 2.0, 3.0}
	var v2 [3]float32 = [3]float32{-30.0, -20.0, -10.0}
	AddVec(v1, v1) ⟶ [2 4 6]
	AddVec(v1, v2) ⟶ [-29 -18 -7]
	AddVec(v2, v2) ⟶ [-60 -40 -20]


### (D)

Code a function

    func DotProd(v1 [3]float32, v2 [3]float32) float32

which returns the inner product or [dot product](https://en.wikipedia.org/wiki/Dot_product) of the two vectors `v1` and `v2`. 

	var v1 [3]float32 = [3]float32{1.0, 2.0, 3.0}
	var v2 [3]float32 = [3]float32{-30.0, -20.0, -10.0}
	InnerProduct(v1, v1) ⟶ 14
	InnerProduct(v1, v2) ⟶ -100
	InnerProduct(v2, v2) ⟶ 1400

* * * 

## Question 3: 3D Matrices

A 3D matrix is a 3 by 3 "square" of floating point numbers denoting a transformation of vectors in 3D Euclidean space.

We represent a 3D matrix as an array of nine `float32` values, where the
first three values represent the first row of the matrix, the next
three values represent the second row of the matrix, and the last
three values represent the third row of the matrix.


### (A)

Code a function

    func NewMat(r1 [3]float32, r2 [3]float32, r3 [3]float32) [9]float32

which returns a new matrix `m` where the elements of the matrix are taken from the three provided vectors which describe the content of the first row, second row, and third row of the matrix, respectively.

	NewMat([3]float{1, 0, 0}, [3]float{0, 1, 0}, [3]float{0, 0, 1}) ⟶ [1 0 0 0 1 0 0 0 1]
	NewMat([3]float{1, 2, 3}, [3]float{4, 5, 6}, [3]float{7, 8, 9}) ⟶ [1 2 3 4 5 6 7 8 9]


### (B)

Code a function

    func ScaleMat(sc float32, m [9]float32) [9]float32

which returns a *new* matrix in which every entry is the corresponding entry
of `m` multiplied by `sc`.

	var m1 [9]float32 = [9]float32{1, 2, 3, 4, 5, 6, 7, 8, 9}
	ScaleMatrix(1.0, m1) ⟶ [1 2 3 4 5 6 7 8 9]
	ScaleMatrix(2.0, m1) ⟶ [2 4 6 8 10 12 15 18]
	ScaleMatrix(-3.0, m1) ⟶ [-3 -6 -9 -12 -15 -18 -21 -24 -27]

### (C)

Code a function

    func TransposeMat(m [9]float32) [9]float32

which returns a *new* matrix that is the [transpose](https://en.wikipedia.org/wiki/Transpose) of `m`. 

	var m1 [9]float32 = [9]float32{1, 0, 0, 0, 1, 0, 0, 0, 1}
	var m2 [9]float32 = [9]float32{1, 2, 3, 4, 5, 6, 7, 8, 9}
	TransposeMat(m1) ⟶ [1 0 0 0 1 0 0 0 1]
	TransposeMat(m2) ⟶ [1 4 7 2 5 8 3 6 9]

**Hint**: Figure how to go from an index of array to a row number
  and column number for that index, and vice versa.


### (D)

Code a function

    func AddMat(m1 [9]float32, m2 [9]float32) [9]float32
    
which returns a *new* matrix in which every entry is the sum of the
corresponding entries in `m1` and `m2`.

	var m1 [9]float32 = [9]float32{1, 0, 0, 0, 1, 0, 0, 0, 1}
	var m2 [9]float32 = [9]float32{1, 2, 3, 4, 5, 6, 7, 8, 9}
	AddMat(m1, m1) ⟶ [2 0 0 0 2 0 0 0 2]
	AddMat(m1, m2) ⟶ [2 2 3 4 6 6 7 8 10]
	AddMat(m2, m2) ⟶ [2 4 6 8 10 12 14 16 18]


### (E)

Code a function

    func MultMat(m1 [9]float32, m2 [9]float32) [9]float32

which returns a *new* matrix which represents the
[product](https://en.wikipedia.org/wiki/Matrix_multiplication) of `m1` and `m2`. 

	var m1 [9]float32 = [9]float32{1, 0, 0, 0, 1, 0, 0, 0, 1}
	var m2 [9]float32 = [9]float32{1, 2, 3, 4, 5, 6, 7, 8, 9}
	MultMat(m1, m1) ⟶ [1 0 0 0 1 0 0 0 1]
	MultMat(m1, m2) ⟶ [1 2 3 4 5 6 7 8 9]
	MultMat(m2, m2) ⟶ [30 36 42 66 81 96 102 126 150]


### If you're bored

For no credit except bragging rights, code the following two functions.

First, a function

    func InvertMat(m [9]float32) [9]float32
    
which takes a matrix `m` and returns a *new* matrix that
is the inverse of `m`. Recall that the inverse of a matrix M is the
unique matrix M<sup>-1</sup> with the property that M M<sup>-1</sup> is the identity matrix.

Second, a function

    func MultMatVec(m [9]float32, v [3]float32) [3]float32

which takes a matrix `m` and a vector `v` and returns the product of
`m` and `v`, where `v` is interpreted as a column vector for the
purpose of the mulitplication, and the resulting column vector is
represented as a normal vector. This represents the application of the
transformation represented by the matrix to a vector to obtain the
transformed vector.

