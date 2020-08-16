
(* To use this file:
 * 
 * (1) start a _new_ OCaml shell
 * (2) load your homework submission #use "homework1.ml";;
 * (3) load this file  #use "hw1-checker.ml";;
 * 
 * If there are _any_ errors, go back and fix your homework. 
 *
 * The only errors this will check for are:
 * - function not defined or with syntax or type errors
 * - functions not having the required type
 *
 *)

(* Q1 *)
let a:int = try gcd(1,1) with Failure _ -> 0 in let _ = a in
let a:bool = try is_coprime(1,1) with Failure _ -> true in let _ = a in
let a:int = try euler(2) with Failure _ -> 0 in let _ = a in
let a:int list = try coprimes(2) with Failure _ -> [] in let _ = a in
(* Q2 *)
let a:int list = try append([1],[2]) with Failure _ -> [] in let _ = a in
let a:string list = try append(["a"],["b"]) with Failure _ -> [] in let _ = a in
let a:int list = try flatten([[1]]) with Failure _ -> [] in let _ = a in
let a:string list = try flatten([["a"]]) with Failure _ -> [] in let _ = a in
let a:int = try nth(0,[1;2;3]) with Failure _ -> 0 in let _ = a in
let a:string = try nth(0,["a";"b";"c"]) with Failure _ -> "" in let _ = a in
let a:int = try last([1;2;3]) with Failure _ -> 0 in let _ = a in
let a:string = try last(["a";"b";"c"]) with Failure _ -> "" in let _ = a in
let a:(int list * int list) = try separate([(1,1)]) with Failure _ -> ([],[]) in let _ = a in
let a:(string list * string list) = try separate(["a","a"]) with Failure _ -> ([],[]) in let _ = a in
(* Q3 *)
let a:bool = try setIn(1,[1]) with Failure _ -> true in let _ = a in
let a:bool = try setIn("a",["a"])  with Failure _ -> true in let _ = a in
let a:bool = try setSub([1],[1]) with Failure _ -> true in let _ = a in
let a:bool = try setSub(["a"],["a"]) with Failure _ -> true in let _ = a in
let a:bool = try setEqual([1],[1]) with Failure _ -> true in let _ = a in
let a:bool = try setEqual(["a"],["a"]) with Failure _ -> true in let _ = a in
let a:int list = try setUnion([1],[1]) with Failure _ -> [] in let _ = a in
let a:string list = try setUnion(["a"],["a"]) with Failure _ -> [] in let _ = a in
let a:int list = try setInter([1],[1]) with Failure _ -> [] in let _ = a in
let a:string list = try setInter(["a"],["a"]) with Failure _ -> [] in let _ = a in
let a:int = try setSize([1]) with Failure _ -> 0 in let _ = a in
let a:int = try setSize(["a"]) with Failure _ -> 0 in let _ = a in
  print_string "Types all OK.\n"

