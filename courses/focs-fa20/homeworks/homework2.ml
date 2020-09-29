(* 

HOMEWORK 2

Due: Thu Oct 8, 2020 (23h59)

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



(* 
 * String <-> characters utility functions:
 *
 *   explode : string -> char list
 *      returns the list of characters making up a string
 *
 *   implode : char list -> string
 *      concatenates the list of characters into a string
 *
 *)

let explode (str) = 
  let rec acc (index,result) = 
    if (index<0) then
      result
    else
      acc(index-1, (String.get str index)::result)
  in
    acc(String.length(str)-1, [])

let implode cs = 
  List.fold_right (fun a r -> (String.make 1 a)^r) cs ""



(*
 *  The type of a finite automaton
 * 
 *)

type fa = { states: int list;
     	    alphabet: char list;
            delta: (int * char * int) list;
            start : int;
            final : int list }


(* 
 * Sample finite automata
 *
 * The first accepts the language of all strings over {x,y} 
 * with a multiple-of-3 number of x's.
 *
 * The second accepts the language of all strings over {x,y,z}
 * whose last three symbols are y's.
 *
 *)

let faThreeX = { 
  states = [1; 2; 3];
  alphabet = ['x'; 'y'];
  delta = [ (1,'x',2);
	    (2,'x',3);
	    (3,'x',1);
	    (1,'y',1);
	    (2,'y',2);
	    (3,'y',3) ];
  start = 1;
  final = [1]
} 

let faLastThreeY = {
  states = [0; 1; 2; 3];
  alphabet = ['x'; 'y'; 'z'];
  delta = [ (0,'x',0);
	    (0,'y',0);
	    (0,'z',0);
	    (0,'y',1);
	    (1,'y',2);
	    (2,'y',3); ];
  start = 0;
  final = [3]
} 



(* Question 1 *)


let rec hasFinal (m:fa) (qs:int list):bool =
  failwith "not implemented"


let rec followSymbol (m:fa) (q:int) (a:char):int list = 
  failwith "not implemented"


let rec followSymbolFromSet (m:fa) (qs:int list) (a:char):int list =
  failwith "not implemented"


let rec followStringFromSet (m:fa) (qs:int list) (syms:char list):int list = 
  failwith "not implemented"


let rec followString (m:fa) (q:int) (syms: char list) : int list =
  failwith "not implemented"


let rec accept (m:fa) (input:string):bool = 
  failwith "not implemented"



(* Question 2 *)


(* Right now, these are dummy finite automata that always rejects 
   replace by your own *)

let dummy : fa = { states = [0];
                   alphabet = [];
                   delta = [];
                   start = 0;
                   final = []}

let fa_q2_a : fa = dummy
                    

let fa_q2_b : fa = dummy
                        

let fa_q2_c : fa = dummy
                    

let fa_q2_d : fa = dummy


let fa_q2_e : fa = dummy




(* Question 3 *)

let fa1 = { states = [0; 1; 2];
	    alphabet = ['x'; 'y'; 'z'];
	    delta = [ (0,'x',1);
		      (1,'x',2);
		      (2,'x',0) ];
	    start = 0;
	    final = [0] }

let fa2 = { states = [10; 11; 12];
	    alphabet = ['x'; 'y'; 'z'];
	    delta = [ (10,'y',11);
		      (11,'z',12);
                      (12,'y',11) ];
	    start = 10;
	    final = [12] }


(* Functions to compute a fresh state for one or two finite automata *)

let rec maxState (states:int list): int =
  match states with
  | [] -> failwith "no states"
  | [a] -> a
  | state::states' -> max state (maxState states')

let freshState (m:fa): int =
  (maxState m.states) + 1

let freshState2 (m1:fa) (m2:fa): int =
  (max (maxState m1.states) (maxState m2.states)) + 1


(* Helper functions to check for disjoint states and same alphabets *)

let rec subset (s : 'a list) (t : 'a list) : bool = 
  match s with
  | [] -> true
  | x::s' -> if List.mem x t then subset s' t else false

let equal (s : 'a list) (t : 'a list) : bool =
  (subset s t) && (subset t s)

let rec intersect (s : 'a list) (t : 'a list) : 'a list =
  match s with
  | [] -> []
  | x::s' -> if List.mem x t
               then x :: (intersect s' t)
	     else intersect s' t

let rec empty (s : 'a list) : bool =
  match s with
  | [] -> true
  | _ -> false




let prependM (m : fa) (a : char) : fa =
  failwith "not implemented"


let prependStringM (m : fa) (s : string) : fa =
  failwith "not implemented"


let rec replaceSource delta state =
  match delta with
  | [] -> []
  | (p, a, q)::delta' -> (state, a, q)::(replaceSource delta' state)

let rec transitionsFromState state delta =
  match delta with
  | [] -> []
  | (p, a, q)::delta' -> if p = state then (p, a, q)::(transitionsFromState state delta') else transitionsFromState state delta'
  
                     
let unionM (m1:fa) (m2:fa):fa = 
  if not (empty (intersect m1.states m2.states))
    then failwith "States not disjoint"
  else if not (equal m1.alphabet m2.alphabet)
    then failwith "Alphabets not equal"
  else 
    let new_start = freshState2 m1 m2  in
    let new_states = new_start :: (m1.states @ m2.states)  in
    let alphabet = m1.alphabet  in
    let new_delta = m1.delta @ m2.delta @ 
      (replaceSource (transitionsFromState m1.start m1.delta) new_start) @
      (replaceSource (transitionsFromState m2.start m2.delta) new_start)  in
    let new_final = if (List.mem m1.start m1.final || List.mem m2.start m2.final) 
                      then m1.final @ m2.final @ [new_start] 
                    else m1.final @ m2.final  in
    { states = new_states;
      alphabet = alphabet;
      delta = new_delta;
      start = new_start;
      final = new_final }




  
(* This function is the base function that basically loops through all
 * strings  of length up to n, and prints those that are accepted by the
 * finite automaton.
 *
 * This is being way too clever to try to not blow the stack 
 * while enumerating all strings up to a given length. Basically.
 * we enumerate all integer, convert them to base K (where K is the
 * size of the alphabet) and then replace every digit base K by the
 * letter of the alphabet at the corresponding index in the alphabet. 
 *
 * The key is that we can enumerate integers super easily
 *
 *)

let lang m n = 

  let rec expt a n = if n <= 0 then 1 else a*(expt a (n-1)) in
  
  let rec take n default l = 
    if n <= 0 then []
    else (match l with
          | [] -> default::(take (n-1) default l)
          | x::xs -> x::(take (n-1) default xs)) in
  
  let to_base_n base size n = 
    let rec loop n = 
      if n <= 0 then []
      else if n mod base = 0 then 0::(loop (n / base))
      else (n mod base)::(loop ((n - n mod base) / base))  in
    take size 0 (loop n)  in
  
  let to_string alphabet size n = 
    let base = List.length alphabet in
    let num_base = to_base_n base size n in
    implode (List.map (fun i -> List.nth alphabet i) num_base) in
  
    if n < 0 then ()
    else
      let print_str s = if s = "" then print_string "  <epsilon>\n"
  	              else print_string ("  "^s^"\n")  in
      let rec loop i = 
        if i <= n then 
  	  let ts = to_string m.alphabet i  in
  	  let bound = expt (List.length m.alphabet) i in
  	  let rec loop2 j = 
  	    if j < bound then (if accept m (ts j) 
                                 then print_str (ts j)
                               else ();
  			       loop2 (j+1))
  	    else ()  in
  	  (loop2 0; loop (i+1))
        else ()  in
      loop 0

