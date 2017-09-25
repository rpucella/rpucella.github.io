(* 

HOMEWORK 4

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
 * It is fine to add a 'rec' keyword though.
 *
 * Always make sure you can #use this file in a FRESH OCaml shell
 * before submitting it. It has to load without any errors.
 *
 *)



(* QUESTION 1 *)

let nfilter (p:'a -> bool) (xs:'a list) : 'a list = 
  failwith "not implemented"


let count (p:'a -> bool) (xs:'a list) : int = 
  failwith "not implemented"


let maxp (xs:int list) : int =
  failwith "not implemented"


let mapf (fs:('a -> 'b) list) (xs:'a list) : 'b list = 
  failwith "not implemented"


let pairs (xs:'a list) (ys:'b list) : ('a * 'b) list = 
  failwith "not implemented"



(* Question 2 *)

let prepend (x:'a) (xss:'a list list) : 'a list list = 
  failwith "not implemented"


let prefixes (xs:'a list) : 'a list list = 
  failwith "not implemented"


let augment_suffixes (xs:'a list) : ('a * 'a list) list = 
  failwith "not implemented"


let inject (x:'a) (xs:'a list) : 'a list list = 
  failwith "not implemented"


(* BONUS *)

let permutations (xs:'a list):'a list list = 
  failwith "not implemented"

