//
// GO DEMO
//
// To run:
//
//   go run demo.go
//
// To build:
//
//   go build demo.go
//

// Every Go source file belongs to a package. Unless you're writing a module, use package main.
package main

// List all the library packages you're importing.
import (
	// The fmt library package controls printing to the console.
	"fmt"
)

// This is a function that takes an integer as argument and returns an integer result.
func square(n int) int {
	return n * n
}

// This is a function that returns two values - not a tuple of values, but TWO values.
func next(n int) (int, int) {
	return n, n + 1
}

// This is a function returning a string, using a good old conditional in its body.
func parity(n int) string {
	if n % 2 == 0 {
		return "even"
	} else {
		return "odd"
	}
}

// Function main (no arguments, no return value) is the main entry point of your program
// and is called when you do `go run` or when you execute the executable created
// by `go build`. It is required.
func main() {
	// Print a string to the screen with a newline.
	fmt.Println("Hello world!")
	// Local variable declaration and initialization.
	var i int = 0
	// An "unbounded" for loop: iterate until the condition is true.
	for i < 20 {
		// Function invocation returning an integer.
		var result int = square(i)
		// Function invocation returning a string.
		var p string = parity(i)
		// Formatted output - pass a format string and values to fill in the "blanks".
		fmt.Printf("The square of %d is %d - is %s\n", i, result, p)
		i = i + 1
	}

	// Declare an array of 20 integers initialized with zeros.
	var squares [20]int
	printArray(squares)
	var index int
	// Loop over the elements of array squares.
	// Two variables are assigned at each iteration: the index of the elemt and the element itself.
	for index, _ = range squares {
		squares[index] = square(index)
	}
	printArray(squares)
	fmt.Printf("first element before update = %d\n", squares[0])
	// Call a function to (incorrectly) update the first element of the array.
	modifyArray(squares)
	fmt.Printf("first element after (incorrect) update = %d\n", squares[0])
	// Call a function to (correctly) update the first element of the array.
	// The function is passed a _pointer_ to the array (obtained via the & operator).
	modifyArray2(&squares)
	fmt.Printf("first element after (correct) update = %d\n", squares[0])
	printArray(squares)
}

// Function to print the content of an array of 20 integers.
func printArray(arr [20]int) {
	fmt.Printf("Array = %v\n", arr)
}

// Function to (incorrectly) update the first element of an array of 20 integers.
func modifyArray(arr [20]int) {
	// Update the first element of the array (0-indexed).
	arr[0] = 99
}

// Function to (correctly) update the first element of an array of 20 integers passed as a pointer.
func modifyArray2(arr *[20]int) {
	// Dereference the pointer to get the array then update its first element.
	(*arr)[0] = 99
}
