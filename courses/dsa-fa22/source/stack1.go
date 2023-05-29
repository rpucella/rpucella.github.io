package main

import (
    "fmt"
)

type Stack struct {
    content []int
}

// Note -
// We could use type Stack *struct {...}
// But then we would need to use &struct{content []int}{c} for constructing.

func NewStack() *Stack {
    c := make([]int, 0)
    s := &Stack{c}
    return s
}

func Push(s *Stack, v int) {
    newContent := make([]int, len(s.content) + 1)
    for i := range(s.content) {
        newContent[i] = s.content[i]
    }
    newContent[len(s.content)] = v
    s.content = newContent
}

func Pop(s *Stack) {
    if IsEmpty(s) {
        panic("stack is empty!")
    }
    newContent := make([]int, len(s.content) - 1)
    for i := range(newContent) {
        newContent[i] = s.content[i]
    }
    s.content = newContent
}

func Top(s *Stack) int {
    if IsEmpty(s) {
        panic("stack is empty!")
    }
    return s.content[len(s.content) - 1]
}

func IsEmpty(s *Stack) bool {
    // return s == nil || len(s.content) == 0
    return len(s.content) == 0
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
