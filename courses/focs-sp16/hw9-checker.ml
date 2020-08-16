
(* To use this file:
 * 
 * (1) start a _new_ OCaml shell
 * (2) load your homework submission #use "homework9.ml";;
 * (3) load this file  #use "hw9-checker.ml";;
 * 
 * If there are _any_ errors, go back and fix your homework. 
 *
 * The only errors this will check for are:
 * - function not defined or with syntax or type errors
 * - functions not having the required type
 *
 *)


(* Q1 *)
let a:int stream = try scale 0 (cst 0) with Failure _ -> (cst 0) in let _ = a in 
let a:int stream = try mult (cst 0) (cst 0) with Failure _ -> (cst 0) in let _ = a in 
let a:(int stream * int stream) = try unzip (zip (cst 0) (cst 0)) with Failure _ -> (cst 0, cst 0) in let _ = a in 
let a:(string stream * int stream) = try unzip (zip (cst "") (cst 0)) with Failure _ -> (cst "", cst 0) in let _ = a in 
let a:int stream = try fold (fun a b -> b) (cst 0) (cst "") with Failure _ -> (cst 0) in let _ = a in 
let a:string stream = try fold (fun a b -> b) (cst "") (cst 0) with Failure _ -> (cst "") in let _ = a in
let a:int stream = try running_max (cst 0) with Failure _ -> (cst 0) in let _ = a in
let a:int stream = try stutter (cst 0) with Failure _ -> (cst 0) in let _ = a in
let a:string stream = try stutter (cst "") with Failure _ -> (cst "") in let _ = a in
(* Q2 *)
let a:float stream = try arctan 0.0 with Failure _ -> (cst 0.0) in let _ = a in
let a:float stream = try pi with Failure _ -> (cst 0.0) in let _ = a in
let a:float stream = try newton (fun x -> x) (fun x -> x) 0.0 with Failure _ -> (cst 0.0) in let _ = a in
let a:float stream = try derivative (fun x -> x) 0.0 with Failure _ -> (cst 0.0) in let _ = a in
let a:float stream = try limit 1.0 (cst 0.0) with Failure _ -> (cst 0.0) in let _ = a in
(* Q3 *)
let a:int list stream = try rev_prefixes (cst 0) with Failure _ -> (cst [0]) in let _ = a in
let a:string list stream = try rev_prefixes (cst "") with Failure _ -> (cst [""]) in let _ = a in
let a:int list stream = try prefixes (cst 0) with Failure _ -> (cst [0]) in let _ = a in
let a:string list stream = try prefixes (cst "") with Failure _ -> (cst [""]) in let _ = a in
let a:(int * string) list stream = try stripes (cst 0) (cst "") with Failure _ -> (cst [(0,"")]) in let _ = a in
let a:int stream = try flatten (cst [0]) with Failure _ -> (cst 0) in let _ = a in 
let a:string stream = try flatten (cst [""]) with Failure _ -> (cst "") in let _ = a in 
let a:(int * string) stream = try pairs (cst 0) (cst "") with Failure _ -> (cst (0,"")) in let _ = a in
  print_string "Types all OK.\n"

