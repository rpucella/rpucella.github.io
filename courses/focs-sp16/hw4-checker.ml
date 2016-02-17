
(* To use this file:
 * 
 * (1) start a _new_ OCaml shell
 * (2) load your homework submission #use "homework4.ml";;
 * (3) load this file  #use "hw4-checker.ml";;
 * 
 * If there are _any_ errors, go back and fix your homework. 
 *
 * The only errors this will check for are:
 * - function not defined or with syntax or type errors
 * - functions not having the required type
 *
 *)

let d1 = {states=["q"];alphabet=['a'];delta=(fun x y -> x);start="q";accepting=[]}  in
let d2 = {states=[1];alphabet=['a'];delta=(fun x y -> x);start=1;accepting=[]}  in
(* Q1 *)
let a:bool = try isAccepting d1 "q" with Failure _ -> true in let _ = a in
let a:bool = try isAccepting d2 1 with Failure _ -> true in let _ = a in
let a:string = try steps d1 "q" ['a'] with Failure _ -> "" in let _ = a in
let a:int = try steps d2 1 ['a'] with Failure _ -> 1 in let _ = a in
let a:bool = try acceptDFA d1 ""  with Failure _ -> true in let _ = a in
let a:bool = try acceptDFA d2 ""  with Failure _ -> true in let _ = a in
(* Q2 *)
let a:bool = try at_least 0 (fun x -> x=0) [0] with Failure _ -> true in let _ = a in
let a:bool = try at_least 0 (fun x -> x="") [""] with Failure _ -> true in let _ = a in
let a:int = try max_positive [0] with Failure _ -> 0 in let _ = a in
let a:bool list = try map_funs [fun x -> x=0] 0 with Failure _ -> [true] in let _ = a in
let a:int list = try map_funs [fun x -> if x="" then 0 else 0] "" with Failure _ -> [0] in let _ = a in
let a:bool list = try map_cross [fun x -> x=0] [0] with Failure _ -> [true] in let _ = a in
let a:int list = try map_cross [fun x -> if x="" then 0 else 0] [""] with Failure _ -> [0] in let _ = a in
let a:(int * string) list = try all_pairings [0] [""] with Failure _ -> [(0,"")] in let _ = a in
let a:(string * int) list = try all_pairings [""] [0] with Failure _ -> [("",0)] in let _ = a in
(* Q3 *)
let a:int list list = try prefixes [0] with Failure _ -> [[0]] in let _ = a in
let a:int list list = try suffixes [0] with Failure _ -> [[0]] in let _ = a in
let a:int list list = try inject 0 [0] with Failure _ -> [[0]] in let _ = a in
let a:int list list = try permutations [0] with Failure _ -> [[0]] in let _ = a in
let a:string list list = try prefixes [""] with Failure _ -> [[""]] in let _ = a in
let a:string list list = try suffixes [""] with Failure _ -> [[""]] in let _ = a in
let a:string list list = try inject "" [""] with Failure _ -> [[""]] in let _ = a in
let a:string list list = try permutations [""] with Failure _ -> [[""]] in let _ = a in
  print_string "Types all OK.\n"

