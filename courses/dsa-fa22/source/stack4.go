package main

import (
    "fmt"
)

type cell struct {
    value int
    next *cell
}

type Stack struct {
    head *cell
}

// Note -
// We could use type Stack *struct {...}
// But then we would need to use &struct{content []int}{c} for constructing.

func NewStack() *Stack {
    s := &Stack{nil}
    return s
}

func Push(s *Stack, v int) {
     c := &cell{v, s.head}
     s.head = c
}

func Pop(s *Stack) {
    if IsEmpty(s) {
        panic("stack is empty!")
    }
    s.head = s.head.next
}

func Top(s *Stack) int {
    if IsEmpty(s) {
        panic("stack is empty!")
    }
    return s.head.value
}

func IsEmpty(s *Stack) bool {
    return s.head == nil
}

func main() {
    s := NewStack()
    fmt.Println(IsEmpty(s))
    Push(s, 10)
    Push(s, 20)
    Push(s, 30)
    fmt.Println(s)
    Pop(s)
    fmt.Println(s)
    fmt.Println(Top(s))
    fmt.Println(IsEmpty(s))
}
