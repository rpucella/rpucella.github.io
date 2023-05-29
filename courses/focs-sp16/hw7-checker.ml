
(* To use this file:
 * 
 * (1) start a _new_ OCaml shell
 * (2) load your homework submission #use "homework7.ml";;
 * (3) load this file  #use "hw7-checker.ml";;
 * 
 * If there are _any_ errors, go back and fix your homework. 
 *
 * The only errors this will check for are:
 * - function not defined or with syntax or type errors
 * - functions not having the required type
 *
 *)

let g = {nonterminals=[]; terminals=[]; rules=[]; startsym=""} in
(* Q1 *)
let a:grammar = try palindromes with Failure _ -> g in let _ = a in 
let a:grammar = try ambncmn with Failure _ -> g in let _ = a in 
let a:grammar = try amcmnbn with Failure _ -> g in let _ = a in 
let a:grammar = try ambncm with Failure _ -> g in let _ = a in 
let a:grammar = try eqnum with Failure _ -> g in let _ = a in 
(* Q2 *)
let a:grammar = try dfaGrammar { states=[""]; alphabet=[]; delta = (fun x y -> x); start=""; accepting=[]} with Failure _ -> g in let _ = a in
(* Q3 *)
let a:grammar = try addition with Failure _ -> g in let _ = a in
let a:grammar = try powers2 with Failure _ -> g in let _ = a in
  print_string "Types all OK.\n"

