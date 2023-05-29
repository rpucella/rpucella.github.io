
(* To use this file:
 * 
 * (1) start a _new_ OCaml shell
 * (2) load your homework submission #use "homework6.ml";;
 * (3) load this file  #use "hw6-checker.ml";;
 * 
 * If there are _any_ errors, go back and fix your homework. 
 *
 * The only errors this will check for are:
 * - function not defined or with syntax or type errors
 * - functions not having the required type
 *
 *)

let tString = {states=["q"];input_alphabet=["a"];tape_alphabet=["a"];left_marker="a";blank="a";delta=(fun (a,s) -> ("q",s,1));start="q";accept="q";reject="r"}  in
let tInt = {states=[0];input_alphabet=["a"];tape_alphabet=["a"];left_marker="a";blank="a";delta=(fun (a,s) -> (0,s,1));start=0;accept=0;reject=0}  in
let deltaInt x = match x with (0,"") -> (0,"",0) | _ -> (0,"",0) in
let deltaString x = match x with ("","") -> ("","",0) | _ -> ("","",0) in
(* Q1 *)
let a:(bool * int * string) list = try triples [true] [0] [""] with Failure _ -> [(true,0,"")] in let _ = a in 
let a:(bool * int * string * bool) list = try quads [true] [0] [""] [true] with Failure _ -> [(true,0,"",true)] in let _ = a in 
let a:int list = try range 1 with Failure _ -> [0] in let _ = a in 
(* Q2 *)
let a:int list = try transformStates [""] int_of_string with Failure _ -> [0] in let _ = a in 
let a:string list = try transformStates [0] string_of_int with Failure _ -> [""] in let _ = a in 
let a:int = try find_original [0] string_of_int "" with Failure _ -> 0 in let _ = a in 
let a:string = try find_original [""] int_of_string 0 with Failure _ -> "" in let _ = a in 
let a:(int * string) -> (int * string * int) = try transformDelta [""] deltaString int_of_string with Failure _ -> deltaInt in let _ = a in 
let a:(string * string) -> (string * string * int) = try transformDelta [0] deltaInt string_of_int with Failure _ -> deltaString in let _ = a in 
let a:(int tm) = try transform tString int_of_string with Failure _ -> tInt in let _ = a in 
let a:(string tm) = try transform tInt string_of_int with Failure _ -> tString in let _ = a in 
(* Q3 *)
let a:string tm = try permutation with Failure _ -> tString in let _ = a in
let a:string tm = try copies 1 with Failure _ -> tString in let _ = a in
  print_string "Types all OK.\n"

