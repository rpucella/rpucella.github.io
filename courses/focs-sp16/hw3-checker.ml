
(* To use this file:
 * 
 * (1) start a _new_ OCaml shell
 * (2) load your homework submission #use "homework3.ml";;
 * (3) load this file  #use "hw3-checker.ml";;
 * 
 * If there are _any_ errors, go back and fix your homework. 
 *
 * The only errors this will check for are:
 * - function not defined or with syntax or type errors
 * - functions not having the required type
 *
 *)

let d1 = {states=["q"];alphabet=['a'];delta=[];start="q";accepting=[]}  in
let d2 = {states=[1];alphabet=['a'];delta=[];start=1;accepting=[]}  in
(* Q1 *)
let a:(string*char*string) list = try findTransitions(d1,"q",'a') with Failure _ -> [("",'a',"")] in let _ = a in
let a:(int*char*int) list = try findTransitions(d2,1,'a') with Failure _ -> [(1,'a',1)] in let _ = a in
let a:bool = try isAccepting(d1,"q") with Failure _ -> true in let _ = a in
let a:bool = try isAccepting(d2,1) with Failure _ -> true in let _ = a in
let a:string = try step(d1,"q",'a') with Failure _ -> "" in let _ = a in
let a:int = try step(d2,1,'a') with Failure _ -> 1 in let _ = a in
let a:string = try steps(d1,"q",['a']) with Failure _ -> "" in let _ = a in
let a:int = try steps(d2,1,['a']) with Failure _ -> 1 in let _ = a in
let a:bool = try isDFA(d1) with Failure _ -> true in let _ = a in
let a:bool = try isDFA(d2) with Failure _ -> true in let _ = a in
let a:bool = try acceptDFA(d1,"") with Failure _ -> true in let _ = a in
let a:bool = try acceptDFA(d2,"") with Failure _ -> true in let _ = a in
(* Q2 *)
let a:'a fa = try dfa_q2_a with Failure _ -> let x = [] in {states=x;alphabet=[];delta=[];start=List.hd x;accepting=[]} in let _ = a in
let a:'b fa = try dfa_q2_b with Failure _ -> let x = [] in {states=x;alphabet=[];delta=[];start=List.hd x;accepting=[]} in let _ = a in
let a:'c fa = try dfa_q2_c with Failure _ -> let x = [] in {states=x;alphabet=[];delta=[];start=List.hd x;accepting=[]} in let _ = a in
let a:'d fa = try nfa_q2_d with Failure _ -> let x = [] in {states=x;alphabet=[];delta=[];start=List.hd x;accepting=[]} in let _ = a in
(* Q3 *)
let a:string list = try keepTarget([("q",'a',"q")]) with Failure _ -> [""] in let _ = a in
let a:int list = try keepTarget([(1,'a',1)]) with Failure _ -> [1] in let _ = a in
let a:bool = try isAcceptingAny(d1,["q"]) with Failure _ -> true in let _ = a in
let a:bool = try isAcceptingAny(d2,[1]) with Failure _ -> true in let _ = a in
let a:string list = try stepAll(d1,["q"],'a') with Failure _ -> [""] in let _ = a in
let a:int list = try stepAll(d2,[1],'a') with Failure _ -> [1] in let _ = a in
let a:string list = try stepsAll(d1,["q"],['a']) with Failure _ -> [""] in let _ = a in
let a:int list = try stepsAll(d2,[1],['a']) with Failure _ -> [1] in let _ = a in
let a:bool = try acceptNFA(d1,"") with Failure _ -> true in let _ = a in
let a:bool = try acceptNFA(d2,"") with Failure _ -> true in let _ = a in
  print_string "Types all OK.\n"

