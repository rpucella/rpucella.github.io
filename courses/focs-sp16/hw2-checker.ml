
(* To use this file:
 * 
 * (1) start a _new_ OCaml shell
 * (2) load your homework submission #use "homework2.ml";;
 * (3) load this file  #use "hw2-checker.ml";;
 * 
 * If there are _any_ errors, go back and fix your homework. 
 *
 * The only errors this will check for are:
 * - function not defined or with syntax or type errors
 * - functions not having the required type
 *
 *)

(* Q1 *)
let a:string list = try prepend("a",["a"]) with Failure _ -> [""] in let _ = a in
let a:string list = try concatenate(["a"],["a"]) with Failure _ -> [""] in let _ = a in
let a:string list = try all_strings(["a"],1) with Failure _ -> [""] in let _ = a in
(* Q2 *)
let a:string list = try restrict(["a"],1) with Failure _ -> [""] in let _ = a in
let a:string list = try langUnion(["a"],["a"],1) with Failure _ -> [""] in let _ = a in
let a:string list = try langConcat(["a"],["a"],1) with Failure _ -> [""] in let _ = a in
let a:string list = try langStar(["a"],1) with Failure _ -> [""] in let _ = a in
(* Q3 *)
let a:string = try regexp_a with Failure _ -> "" in let _ = a in
let a:string = try regexp_b with Failure _ -> "" in let _ = a in
let a:string = try regexp_c with Failure _ -> "" in let _ = a in
let a:string = try regexp_d with Failure _ -> "" in let _ = a in
let a:string = try regexp_e with Failure _ -> "" in let _ = a in
  print_string "Types all OK.\n"

