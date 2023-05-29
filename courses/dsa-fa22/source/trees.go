
package main

import (
	"fmt"
)

type Stack struct {
	content []*Node
}

func NewStack() *Stack {
	c := make([]*Node, 0)
	s := &Stack{c}
	return s
}

func Push(s *Stack, v *Node) {
	newContent := make([]*Node, len(s.content) + 1)
	for i := range(s.content) {
		newContent[i] = s.content[i]
	}
	newContent[len(s.content)] = v
	s.content = newContent
}

func Pop(s *Stack) *Node {
	if IsEmptyStack(s) {
		panic("stack is empty!")
	}
	result := s.content[len(s.content) - 1]
	newContent := make([]*Node, len(s.content) - 1)
	for i := range(newContent) {
		newContent[i] = s.content[i]
	}
	s.content = newContent
	return result
}

func IsEmptyStack(s *Stack) bool {
	return len(s.content) == 0
}

type cellQ struct {
	value int
	next  *cellQ
}

type Queue struct {
	head *cellQ
	tail *cellQ
}

func NewQueue() *Queue {
	q := &Queue{nil, nil}
	return q
}

func Enqueue(q *Queue, v int) {
	c := &cellQ{v, nil}
	if IsEmptyQueue(q) {
		q.head = c
		q.tail = c
	} else {
		c.next = q.head
		q.head = c
	}
}

func Dequeue(q *Queue) int {
	if IsEmptyQueue(q) {
		panic("queue is empty!")
	}
	curr := q.head
	result := q.tail.value
	if curr.next == nil {
		q.head = nil
	} else {
		for curr.next.next != nil {
			curr = curr.next
		}
		curr.next = nil
		q.tail = curr
	}
	return result
}

func IsEmptyQueue(q *Queue) bool {
	return q.head == nil
}

/* ************************************************** */

type Node struct {
	value int
	left *Node
	right *Node
}

func max(a int, b int) int {
	if a < b {
		return b
	}
	return a
}

func NewNode(v int, left *Node, right *Node) *Node {
	c := &Node{v, left, right}
	return c
}

func PrintTree(t *Node) {
	var print func(*Node, string)
	print = func (c *Node, prefix string) {
		if c != nil {
			print(c.right, prefix + "   ")
			fmt.Printf("%s%d\n", prefix, c.value)
			print(c.left, prefix + "   ")
		} else {
			fmt.Printf("%s-\n", prefix)
		}
	}
	fmt.Println(" +-----")
	print(t, " |  ")
	fmt.Println(" +-----")
}

func SizeRec(t *Node) int {
	// Recursive function to compute # nodes in a tree.
	if t == nil {
		return 0
	} else {
		s1 := SizeRec(t.left)
		s2 := SizeRec(t.right)
		return 1 + s1 + s2
	}
}

func HeightRec(t *Node) int {
	// Recursive function to compute height of a tree.
	if t == nil {
		return 0
	} else {
		h1 := HeightRec(t.left)
		h2 := HeightRec(t.right)
		return 1 + max(h1, h2)
	}
}



func ReflectRec(t *Node) {
	// Recursive function to "mirror reflect" all nodes in a tree (swap left/right subtrees).
	if t == nil {
		return 
	} else {
		temp := t.left
		t.left = t.right
		t.right = temp
		ReflectRec(t.left)
		ReflectRec(t.right)
	}
}

func MapRec(t *Node, f func(int)int) {
	// Recursive function to transform all values in a tree according to function f.
	if t == nil {
		return
	} else {
		t.value = f(t.value)
		MapRec(t.left, f)
		MapRec(t.right, f)
	}
}

func MapIRec(t *Node, f func(int)int) *Node {
	// Version of MapI that does not modify the original tree and creates a
	// completely new transformed tree.
	if t == nil {
		return nil
	} else {
		new_left := MapIRec(t.left, f)
		new_right := MapIRec(t.right, f)
		new_value := f(t.value)
		new_t := NewNode(new_value, new_left, new_right)
		return new_t
	}
}

func Map(t *Node, f func(int)int) {
	// Non-recursive version of Map using a stack
	working := NewStack()
	Push(working, t)
	for !IsEmptyStack(working) {
		t1 := Pop(working)
		if t1 != nil {
			t1.value = f(t1.value)
			Push(working, t1.left)
			Push(working, t1.right)
		}
	}
}

//
// Challenge: come up with non-recursive Size, Height, and MapI functions
//

func main() {
	lt := NewNode(20, NewNode (30, nil, nil), NewNode(40, nil, NewNode(50, nil, nil)))
	rt := NewNode(60, NewNode(70, nil, nil), NewNode(80, nil, nil))
	t := NewNode(10, lt, rt)
	PrintTree(t)
	h := HeightRec(t)
	fmt.Printf("Height = %d\n", h)
	Map(t, func(n int) int { return n + 3 })
	PrintTree(t)
}
