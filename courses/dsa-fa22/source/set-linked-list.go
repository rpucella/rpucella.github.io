package main

import (
	"fmt"
)

/* SET ADT via LINKED LISTS */

type Cell struct {
	value int
	next *Cell
}

type Set struct {
	head *Cell
}

func NewCell(v int) *Cell {
	c := &Cell{v, nil}
	return c
}

func CellValue(c *Cell) int {
	return c.value
}

func NewSet() *Set {
	s := &Set{nil}
	return s
}

func PrintSet(s *Set) {
	fmt.Printf("  ")
	curr := s.head
	for curr != nil {
		fmt.Printf("%d ", curr.value)
		curr = curr.next
	}
	fmt.Println()
}

func Search(s *Set, k int) *Cell {
	curr := s.head
	for curr != nil {
		if curr.value == k {
			return curr
		}
		curr = curr.next
	}
	return nil
}

func Insert(s *Set, c *Cell) {
	c.next = s.head
	s.head = c
}

func Delete(s *Set, c *Cell) {
	var prev *Cell = nil
	curr := s.head
	for curr != nil {
		if curr == c {
			if prev == nil {
				s.head = curr.next
				curr.next = nil
				return
			} else {
				prev.next = curr.next
				curr.next = nil
				return
			}
		} else {
			prev = curr
			curr = curr.next
		}
	}
}

func Minimum(s *Set) *Cell {
	curr := s.head
	var currMin *Cell = nil
	for curr != nil {
		if currMin == nil {
			currMin = curr
		} else if curr.value < currMin.value {
			currMin = curr
		}
		curr = curr.next
	}
	return currMin
}
	
func Maximum(s *Set) *Cell {
	curr := s.head
	var currMax *Cell = nil
	for curr != nil {
		if currMax == nil {
			currMax = curr
		} else if curr.value > currMax.value {
			currMax = curr
		}
		curr = curr.next
	}
	return currMax
}
	
func main() {
	c1 := NewCell(33)
	c2 := NewCell(66)
	c3 := NewCell(99)

	fmt.Println("creating empty set")
	s := NewSet()

	fmt.Println("inserting 33, 66, 99")
	Insert(s, c1)
	Insert(s, c2)
	Insert(s, c3)
	PrintSet(s)

	fmt.Println("searching for 66")
	c66 := Search(s, 66)
	if c66 == nil {
		fmt.Println("  not found")
	} else {
		fmt.Println("  found")
	}
	
	fmt.Println("deleting 33")
	Delete(s, c1)
	PrintSet(s)

	fmt.Println("inserting -99, 99999")
	cmin := NewCell(-99)
	cmax := NewCell(99999)
	Insert(s, cmin)
	Insert(s, cmax)
	PrintSet(s)

	fmt.Println("deleting minimum")
	cn := Minimum(s)
	Delete(s, cn)
	PrintSet(s)

	fmt.Println("deleting maximum")
	cx := Maximum(s)
	Delete(s, cx)
	PrintSet(s)
}
