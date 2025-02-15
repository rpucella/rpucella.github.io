// --------------------------------------------------
//
// HOMEWORK 1
//
// Due: Fri, Feb 7, 2025 (23h59)
//
// Name:
//
// Email:
//
// Remarks, if any:
//
// 
// --------------------------------------------------
//
// Please fill in this file with your solutions and submit it
//
// The functions below are stubs that you should replace with your
// own implementation.
//
// PLEASE DO NOT CHANGE THE SIGNATURE IN THE STUBS BELOW.
// Doing so makes it impossible for me to test your code.
//
// --------------------------------------------------

package main

import (
	"fmt"
)

func Clamp(min int, max int, v int) int {
        fmt.Println("Clamp not implemented")
	return 0
}

func Interpolate(min float32, max float32, v float32) float32 {
        fmt.Println("Interpolate not implemented")
	return 0
}

func Spaces(n int) string {
        fmt.Println("Spaces not implemented")
	return ""
}

func PadLeft(s string, n int) string {
        fmt.Println("PadLeft not implemented")
	return ""
}

func PadRight(s string, n int) string {
        fmt.Println("PadRight not implemented")
	return ""
}

func PadBoth(s string, n int) string {
        fmt.Println("PadBoth not implemented")
	return ""
}

func NewVec(n1 float32, n2 float32, n3 float32) [3]float32 {
        fmt.Println("NewVec not implemented")
	return [3]float32{0, 0, 0}
}

func ScaleVec(sc float32, v1 [3]float32) [3]float32 {
        fmt.Println("ScaleVec not implemented")
	return [3]float32{0, 0, 0}
}

func AddVec(v1 [3]float32, v2 [3]float32) [3]float32 {
        fmt.Println("AddVec not implemented")
	return [3]float32{0, 0, 0}
}

func DotProd(v1 [3]float32, v2 [3]float32) float32 {
        fmt.Println("DotProd not implemented")
	return 0
}

func NewMat(r1 [3]float32, r2 [3]float32, r3 [3]float32) [9]float32 {
        fmt.Println("NewMat not implemented")
	return [9]float32{0, 0, 0, 0, 0, 0, 0, 0, 0}
}

func ScaleMat(sc float32, m [9]float32) [9]float32 {
        fmt.Println("ScaleMat not implemented")
	return [9]float32{0, 0, 0, 0, 0, 0, 0, 0, 0}
}

func AddMat(m1 [9]float32, m2 [9]float32) [9]float32 {
        fmt.Println("AddMat not implemented")
	return [9]float32{0, 0, 0, 0, 0, 0, 0, 0, 0}
}

func TransposeMat(m [9]float32) [9]float32 {
        fmt.Println("TransposeMat not implemented")
	return [9]float32{0, 0, 0, 0, 0, 0, 0, 0, 0}
}

func MultMat(m1 [9]float32, m2 [9]float32) [9]float32 {
        fmt.Println("MultMat not implemented")
	return [9]float32{0, 0, 0, 0, 0, 0, 0, 0, 0}
}

func main() {
	var f string = "Expected = %v\n     Got = %v\n"
	var wrap func(string)string = func (s string) string { return "'" + s + "'" }
	fmt.Println("You can write some sample tests for yourself here. Here are some to get started.")
	
	fmt.Println("****** Clamp ***************************************")
	fmt.Printf(f, 10.0, Clamp(10.0, 20.0, 5.0))
	
	fmt.Println("****** Interpolate ***************************************")
	fmt.Printf(f, 10.0, Interpolate(10.0, 20.0, 0))

	fmt.Println("****** Spaces ***************************************")
	fmt.Printf(f, "'          '", wrap(Spaces(10)))
	
	fmt.Println("****** PadLeft ***************************************")
	fmt.Printf(f, "'      test'", wrap(PadLeft("test", 10)))
	
	fmt.Println("****** PadRight ***************************************")
	fmt.Printf(f, "'test      '", wrap(PadRight("test", 10)))
	
	fmt.Println("****** PadBoth ***************************************")
	fmt.Printf(f, "'   test    '", wrap(PadBoth("test", 10)))

	var v1 [3]float32 = [3]float32{1.0, 2.0, 3.0}
	
	fmt.Println("****** NewVec ***************************************")
	fmt.Printf(f, [3]float32{0, 10, 20}, NewVec(0, 10, 20))
	
	fmt.Println("****** ScaleVec ***************************************")
	fmt.Printf(f, [3]float32{2, 4, 6}, ScaleVec(2.0, v1))
	
	fmt.Println("****** AddVec ***************************************")
	fmt.Printf(f, [3]float32{2, 4, 6}, AddVec(v1, v1))
	
	fmt.Println("****** DotProd ***************************************")
	fmt.Printf(f, 14, DotProd(v1, v1))

	var v31 [3]float32 = [3]float32{1, 2, 3}
	var v32 [3]float32 = [3]float32{4, 5, 6}
	var v33 [3]float32 = [3]float32{7, 8, 9}
	var m1 [9]float32 = [9]float32{1, 2, 3, 4, 5, 6, 7, 8, 9}

	fmt.Println("****** NewMat ***************************************")
	fmt.Printf(f, [9]float32{1, 2, 3, 4, 5, 6, 7, 8, 9}, NewMat(v31, v32, v33))
	
	fmt.Println("****** ScaleMat ***************************************")
	fmt.Printf(f, [9]float32{2, 4, 6, 8, 10, 12, 14, 16, 18}, ScaleMat(2.0, m1))
	
	fmt.Println("****** TransposeMat ***************************************")
	fmt.Printf(f, [9]float32{1, 4, 7, 2, 5, 8, 3, 6, 9}, TransposeMat(m1))
	
	fmt.Println("****** AddMat ***************************************")
	fmt.Printf(f, [9]float32{2, 4, 6, 8, 10, 12, 14, 16, 18}, AddMat(m1, m1))
	
	fmt.Println("****** MultMat ***************************************")
	fmt.Printf(f, [9]float32{30, 36, 42, 66, 81, 96, 102, 126, 150}, MultMat(m1, m1))
}
