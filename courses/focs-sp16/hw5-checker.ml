
(* To use this file:
 * 
 * (1) start a _new_ OCaml shell
 * (2) load your homework submission #use "homework5.ml";;
 * (3) load this file  #use "hw5-checker.ml";;
 * 
 * If there are _any_ errors, go back and fix your homework. 
 *
 * The only errors this will check for are:
 * - function not defined or with syntax or type errors
 * - functions not having the required type
 *
 *)

let t1 = {states=["q"];input_alphabet=["a"];tape_alphabet=["a"];left_marker="a";blank="a";delta=(fun (a,s) -> ("q",s,1));start="q";accept="q";reject="r"}  in
let c1 = {state="";before=[""];after=[""]} in
(* Q1 *)
let a:string config = try startConfig t1 "" with Failure _ -> c1 in let _ = a in 
let a:bool = try acceptConfig t1 c1 with Failure _ -> true in let _ = a in
let a:bool = try rejectConfig t1 c1 with Failure _ -> true in let _ = a in
let a:bool = try haltConfig t1 c1 with Failure _ -> true in let _ = a in
let a:string config = try step t1 c1 with Failure _ -> c1 in let _ = a in 
let a:bool = try run t1 "" with Failure _ -> true in let _ = a in
(* Q2 *)
let a:'a tm = try tm_q2_a with Failure _ -> t1 in let _ = a in
let a:'b tm = try tm_q2_b with Failure _ -> t1 in let _ = a in 
(* Q3 *)
let a:'a tm = try binaryAddition with Failure _ -> t1 in let _ = a in
  print_string "Types all OK.\n"

