// --------------------------------------------------
//
// HOMEWORK 4
//
// Due: Sunday, Apr 20, 2025 (23h59)
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

const recordsPerBlock = 5

type Block = [recordsPerBlock]string

type File struct  {
	blocks []Block
	overflow *Block
}

func NewBlock() Block {
	return [recordsPerBlock]string{}
}

func NewSeqFile() *File {
	return &File{make([]Block, 0), nil}
}

func NewOrdFile() *File {
	b := NewBlock()
	return &File{make([]Block, 0), &b}
}

func ReadBlock(file *File, idx int) Block {
	if idx < 0 {
		fmt.Printf("[Reading overflow block]\n")
		return *file.overflow
	}
	fmt.Printf("[Reading block %d]\n", idx)
	return file.blocks[idx]
}

func WriteBlock(file *File, idx int, block Block) {
	if idx < 0 {
		fmt.Printf("[Writing overflow block]\n")
		*file.overflow = block
		return
	}
	fmt.Printf("[Writing block %d]\n", idx)
	file.blocks[idx] = block
}

func CreateBlock(file *File) int {
	b := NewBlock()
	file.blocks = append(file.blocks, b)
	return FileSize(file) - 1
}

func FileSize(file *File) int {
	return len(file.blocks)
}

func PrintFile(file *File) {
	fmt.Println("-- BLOCKS ------------------------------")
	for j, b := range file.blocks {
		fmt.Printf("Block %d\n", j)
		for i, r := range b {
			fmt.Printf(" %2d %v\n", i, r)
		}
	}
	if file.overflow != nil {
		fmt.Println("-- OVERFLOW ----------------------------")
		for i, r := range file.overflow {
			fmt.Printf(" %2d %v\n", i, r)
		}
	}
}

// Question 1

func AppendRecord(block Block, rec string) (Block, bool) {
	panic("AppendRecord not implemented")
}

func FirstRecord(block Block) string {
	panic("FirstRecord not implemented")
}

func LastRecord(block Block) string {
	panic("LastRecord not implemented")
}

func FindRecord(block Block, rec string) bool {
	panic("FindRecord not implemented")
}

func FreeSize(block Block) int {
	panic("FreeSize not implemented")
}


// Question 2: sequential files

func SF_Find(file *File, record string) bool {
	panic("SF_Find not implemented")
}

func SF_Insert(file *File, record string) {
	panic("SF_Insert not implemented")
}


// Question 3: ordered files

func OF_Find(f *File, rec string) bool {
	panic("OF_Find not implemented")
}

func OF_Insert(f *File, rec string) {
	panic("OF_Insert not implemented")
}


func main() {
	fmt.Println("=== SEQ FILE =====================================")
	f := NewSeqFile()
	for i := 1; i < 25; i++ {
		rec := fmt.Sprintf("test-%d", i)
		SF_Insert(f, rec)
	}
	PrintFile(f)
	fmt.Printf("Looking for test-14: %v\n", SF_Find(f, "test-14"))
	fmt.Printf("looking for test-99: %v\n", SF_Find(f, "test-99"))

	fmt.Println("=== ORD FILE =====================================")
	of := NewOrdFile()
	for i := 1; i < 25; i++ {
		rec := fmt.Sprintf("test-%d", i)
		OF_Insert(of, rec)
	}
	PrintFile(of)
	fmt.Printf("Looking for test-14: %v\n", OF_Find(f, "test-14"))
	fmt.Printf("looking for test-99: %v\n", OF_Find(f, "test-99"))
}
