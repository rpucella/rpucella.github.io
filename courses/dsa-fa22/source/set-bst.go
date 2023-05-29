package main

import (
	"fmt"
)

/* SET ADT via BINARY SEARCH TREES */

type Cell struct {
	value int
	left *Cell
	right *Cell
}

type Set struct {
	root *Cell
}

func NewCell(v int) *Cell {
	c := &Cell{v, nil, nil}
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
	var print func(*Cell, string)
	print = func (c *Cell, prefix string) {
		if c != nil {
			print(c.right, prefix + "  ")
			fmt.Printf("%s%d\n", prefix, c.value)
			print(c.left, prefix + "  ")
		} else {
			fmt.Printf("%s-\n", prefix)
		}
	}
	print(s.root, "  ")
}

func Search(s *Set, k int) *Cell {
	curr := s.root
	for curr != nil {
		if curr.value == k {
			return curr
		} else if curr.value < k {
			curr = curr.right
		} else if k < curr.value {
			curr = curr.left
		}
	}
	return nil
}

func Insert(s *Set, c *Cell) {
	parentLink := &s.root
	curr := s.root
	for curr != nil {
		if curr.value == c.value {
			return
		} else if curr.value < c.value {
			parentLink = &curr.right
			curr = curr.right
		} else if c.value < curr.value {
			parentLink = &curr.left
			curr = curr.left
		}
	}
	*parentLink = c
	c.left = nil
	c.right = nil
}

func Delete(s *Set, c *Cell) {
	fmt.Println("(delete not implemented)")
}

func Minimum(s *Set) *Cell {
	fmt.Println("(minimum not implemented)")
	return nil
}

func Maximum(s *Set) *Cell {
	fmt.Println("(maximum not implemented)")
	return nil
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
	
	fmt.Println("searching for 67")
	c67 := Search(s, 67)
	if c67 == nil {
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
