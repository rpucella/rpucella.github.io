// To run this file:
//    go run demo.go
//
// You can also compile it:
//    go build demo.go
// After you build, you should have an executable demo in the folder.


// Every Go source file belongs to a package. Unless you're writing a module, use package main.

package main

// List all the library packages you're importing.
import (
	// The fmt library package controls printing to the console.
	"fmt"
)

// This is a global integer variable initialized with 42.
var fortyTwo int = 42

// This is a global string variable initialized with "fortyTwo"
var fortyTwoString string = "fortyTwo"

// This is a global uninitialized integer variable.
// It automatically gets initialized to the zero value for the type.
// The zero value for integers is 0, the zero value for strings is "", etc.
var uninitialized int

// This is a function that takes an integer as argument and returns an integer result.
func doubleInt(i int) int {
	return i * 2
}

// This is a function that takes a string as argument and returns a string result.
func doubleString(s string) string {
     // This is a conditional. Note the structure, and braces to indicates blocks for the
     // then part and for the else part.
     if s == "" {
        return "empty"
     } else { 
        return s + s
     }
}

// Function main (no arguments, no return value) is the main entry point of your program
// and is called when you do `go run` or when you execute the executable created
// by `go build`. It is required.
func main() {
	// Print a string to the screen with a newline (nl).
	fmt.Println("Hello world!")
	// Local variable declaration and initialization.
	var a int = doubleInt(fortyTwo)
	fmt.Println(a)
	// If you're initializing a variable and the value is unambiguous as to its type,
	// you can use a shortened declaration form.
	// This is exactly equivalent to
	//   var ab string = doubleString(fortyTwoString)
	ab := doubleString(fortyTwoString)
	fmt.Println(ab)

	// This is a C-style for loop, of the form:
        //   for <init declaration>; <test expr>; <step statement> { ... }
	for i := 10; i < 20; i++ {
		fmt.Println(i)
	}

	// This is how you allocate an array of 5 elements. 
	var arr [5]int

	// Such an array is initialized with the zero values of the type.
	// Array positions are 0-indexed.
	fmt.Println("first element =", arr[0])

	// You can set the values in the array. 
	arr[0] = 10
	arr[1] = 20
	arr[2] = 30
	arr[3] = 40
	arr[4] = 50

	// This is how you iterate over the values in an array.
	// Note that range is part of the syntax of the for loop here,
	// and you get two loop variables:
	// - the index
	// - the value at the index.
	for i, v := range arr {
		fmt.Println("index", i, "=", v)
	}

	// This is how you allocate and initialize an array of 4 integers.
	var arr2 [4]int = [4]int{100, 200, 300, 400}

	fmt.Println("first element =", arr2[0])
	arr2[3] = 99

	// The arrays above have a fixed static size. To create an array of
	// dynamic size, you have to use `make`.
	n := 10
	var arr3 []int = make([]int, n)

	for i, v := range arr3 {
		fmt.Println("index", i, "=", v)
	}
	// (Things are actually a bit more complicated than that - `make actually
	// creates a slice, which is a bit more complicated beast than an array, but
	// for the time being, we'll treat them as arrays.)

	// A pointer is basically an address in memory describing some location of memory.
	// If that address is the address where the value of another variable X is stored,
	// we say the address is a "pointer to variable X". 
	// If x is a variable, &x gives you a pointer to variable x -- it takes the address of x.
	// The type of a pointer to a variable of type T is *T.
	var str1 string = "Riccardo"
	var str2 string = str1
	// Let's define str_ptr1 to point to variable str1.
        var str1_ptr *string = &str1

	// Let's confirm that str1 and str2 both contain Riccardo, and
	// and that str1_ptr1 is an address -- it's a pointer to str1. 
	// What the address is is machine and execution dependent.
        fmt.Println("str1 =", str1)
        fmt.Println("str2 =", str2)
        fmt.Println("str1_ptr =", str1_ptr)
	// You can access the value stored at a location pointed to by a pointer by "dereferencing"
	// the pointer using notation *p.
        fmt.Println("*str1_ptr =", *str1_ptr)

	// Why are pointer useful? Well, if I change the value of str1, I can see the changes
	// to str1 via str1_ptr. Meanwhile, str2, even though it was initialized with the
	// value of str1, has its own copy of the string  Riccardo, and is unaffected.
        str1 = "Vicky"
        fmt.Println("str1 =", str1)
        fmt.Println("str2 =", str2)
        fmt.Println("*str1_ptr =", *str1_ptr)

	// Here's another interesting thing with pointers. You can pass a pointer to another
	// function, which makes it possible for that function to affect the memory
	// location pointed to, even if that location corresponding to a local variable in the calling
	// function.
	var local int = 42
	fmt.Println("before", local)
	// See definition below.
	interesting(&local)
	fmt.Println("after", local)
}

// An example of what you can do with pointers.
// You can pass a pointer to a variable to a function,
// modify the content of the variable by dereferencing the
// pointer, and return. The content of the variable in the
// calling function will be affected by side effect.
func interesting(i *int) {
     *i = 10
}


