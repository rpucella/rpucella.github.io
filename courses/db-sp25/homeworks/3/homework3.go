// --------------------------------------------------
//
// HOMEWORK 3
//
// Due: Sunday, Mar 16, 2025 (23h59)
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
	"strings"
)

type Record []string

type Table struct {
	fields []string
	rows []Record
	primaryKey string
}


func newTable(fields []string) Table {
	rows := make([]Record, 0, 10)
	return Table{fields, rows, ""}
}


func CreateTable(fields []string, primaryKey string) Table {
	t := newTable(fields)
	t.primaryKey = primaryKey
	return t
}


func getWidths(t Table) ([]int, int) {
	// Returns the width of the largest value in each field, in order.
	// Also return the total width.
	result := make([]int, len(t.fields))
	total := 0
	for i, f := range t.fields {
		currMax := len(f)
		for _, r := range t.rows {
			l := len(r[i])
			if l > currMax {
				currMax = l
			}
		}
		result[i] = currMax
		total += currMax
	}
	return result, total
}


func PrintTable(t Table) {
	widths, total := getWidths(t)
	line := strings.Repeat("-", total + len(t.fields) * 3 - 1)
	fmt.Printf("+%s+\n", line)
	for i, f := range t.fields {
		fmt.Printf("| %*s ", -widths[i], f)
	}
	fmt.Printf("|\n")
	fmt.Printf("+%s+\n", line)
	for _, r := range t.rows {
		for i, _ := range t.fields {
			fmt.Printf("| %*s ", -widths[i], r[i])
		}
		fmt.Printf("|\n")
	}
	fmt.Printf("+%s+\n", line)
}


func InsertRow(t *Table, rec Record) {
	fmt.Println("Not implemented")
}


func Project(t Table, fields []string) Table {
	fmt.Println("Not implemented")
	return CreateTable([]string{"dummy"}, "")
}


func Filter(t Table, cond func(Record)bool) Table {
	fmt.Println("Not implemented")
	return CreateTable([]string{"dummy"}, "")
}


func Join(t1 Table, t2 Table) Table {
	fmt.Println("Not implemented")
	return CreateTable([]string{"dummy"}, "")
}


func InnerJoin(t1 Table, t2 Table, f1 string, f2 string) Table {
	fmt.Println("Not implemented")
	return CreateTable([]string{"dummy"}, "")
}


func LeftOuterJoin(t1 Table, t2 Table, f1 string, f2 string) Table {
	fmt.Println("Not implemented")
	return CreateTable([]string{"dummy"}, "")
}


func RightOuterJoin(t1 Table, t2 Table, f1 string, f2 string) Table {
	fmt.Println("Not implemented")
	return CreateTable([]string{"dummy"}, "")
}


func FullOuterJoin(t1 Table, t2 Table, f1 string, f2 string) Table {
	fmt.Println("Not implemented")
	return CreateTable([]string{"dummy"}, "")
}


func Aggregate(t Table, groupBy string, concat []string) Table {
	fmt.Println("Not implemented")
	return CreateTable([]string{"dummy"}, "")
}


func main() {
	// Sample outputs.
	t := CreateTable([]string{"n", "square"}, "n")
	for i := 0; i < 20; i++ {
		InsertRow(&t, Record{fmt.Sprintf("%d", i), fmt.Sprintf("%d", i * i)})
	}
	PrintTable(t)
	PrintTable(Project(t, []string{"square"}))
	PrintTable(Filter(t, func(r Record) bool { return strings.HasPrefix(r[0], "1") }))
	t2 := CreateTable([]string{"number", "text"}, "number")
	for i := 0; i < 15; i++ {
		InsertRow(&t2, Record{fmt.Sprintf("%d", i * 2), fmt.Sprintf("value_%d", i * 2)})
	}
	PrintTable(t2)
	PrintTable(Join(t, t2))
	PrintTable(Filter(Join(t, t2), func(r Record) bool { return r[0] == r[2] }))
	PrintTable(InnerJoin(t, t2, "n", "number"))
	PrintTable(LeftOuterJoin(t, t2, "n", "number"))
	PrintTable(RightOuterJoin(t, t2, "n", "number"))
	PrintTable(FullOuterJoin(t, t2, "n", "number"))
	PrintTable(LeftOuterJoin(FullOuterJoin(t, t2, "n", "number"), t, "2.number", "square"))
}
