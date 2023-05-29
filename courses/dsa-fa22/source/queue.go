package main

import (
    "fmt"
)

type cell struct {
    value int
    next *cell
}

type Queue struct {
    head *cell
    tail *cell
}

func NewQueue() *Queue {
    q := &Queue{nil, nil}
    return q
}

func Enqueue(q *Queue, v int) {
     c := &cell{v, nil}
     if IsEmpty(q) {
        q.head = c
     } else {
        q.tail.next = c
     }
     q.tail = c
}

func Dequeue(q *Queue) {
    if IsEmpty(q) {
        panic("queue is empty!")
    }
    q.head = q.head.next
}

func Front(q *Queue) int {
    if IsEmpty(q) {
        panic("queue is empty!")
    }
    return q.head.value
}

func IsEmpty(q *Queue) bool {
    return q.head == nil
}

func main() {
    q := NewQueue()
    fmt.Println(IsEmpty(q))
    Enqueue(q, 10)
    Enqueue(q, 20)
    Enqueue(q, 30)
    Enqueue(q, 40)
    Enqueue(q, 50)
    fmt.Println(q)
    for !IsEmpty(q) { 
      fmt.Println(Front(q))
      Dequeue(q)
    }
}
