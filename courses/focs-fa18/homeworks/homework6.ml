(* 

HOMEWORK 6

Due: Thu 11/1/18 23h59.

Name: 

Email:

Remarks, if any:

*)


(*
 *
 * Please fill in this file with your solutions and submit it
 *
 * The functions below are stubs that you should replace with your
 * own implementation.
 *
 * PLEASE DO NOT CHANGE THE TYPES IN THE STUBS BELOW.
 * Doing so risks making it impossible for me to test your code.
 *
 * Always make sure you can #use this file in a FRESH OCaml shell
 * before submitting it. It has to load without any errors.
 *
 *)




(* QUESTION 1 *)


let count (p:'a -> bool) (xs:'a list) : int = 
  failwith "not implemented"


let all_pairs (xs:'a list) (ys:'b list) : ('a * 'b) list = 
  failwith "not implemented"


let increasing_pairs (xs : int list) (ys : int list) : (int * int) list =
  failwith "not implemented"


let extent (xs: int list) : (int * int) =
  failwith "not implemented"


let all_but_last (xs : 'a list) : 'a list =
  failwith "not implemented"



(* QUESTION 2 *)


let graph (f: 'a -> 'b) (xs: 'a list): ('a * 'b) list =
  failwith "not implemented"


let mapf (fs:('a -> 'b) list) (xs:'a list) : 'b list = 
  failwith "not implemented"


let prepend (x:'a) (xss:'a list list) : 'a list list = 
  failwith "not implemented"


let prefixes (xs:'a list) : 'a list list = 
  failwith "not implemented"


let inject (x:'a) (xs:'a list) : 'a list list = 
  failwith "not implemented"




(* BONUS *)

let permutations (xs:'a list):'a list list = 
  failwith "not implemented"
