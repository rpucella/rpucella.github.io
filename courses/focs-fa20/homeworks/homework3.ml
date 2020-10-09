(* 

HOMEWORK 3

Due: Thu Oct 15, 2020 (23h59)

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
 *  The type of a deterministic finite automaton
 * 
 *)

type dfa = { states: int list;
     	     alphabet: char list;
             delta: int -> char -> int;
             start : int;
             final : int list }


(* 
 * Sample Deterministic Finite Automata
 *
 * The first accepts the language of all strings over {x,y} 
 * with a multiple-of-3 number of x's.
 *
 * The second accepts the language of all strings over {x,y} 
 * starting and ending with an x.
 *
 *)

let dfa1 = { 
  states = [1; 2; 3];
  alphabet = ['x'; 'y'];
  delta = (fun q x -> match (q, x) with
                      | (1, 'x') -> 2
	              | (2, 'x') -> 3
	              | (3, 'x') -> 1
	              | (1, 'y') -> 1
	              | (2, 'y') -> 2
	              | (3, 'y') -> 3);
  start = 1;
  final = [1]
} 


let dfa2 = { 
  states = [10; 11; 12; 13];
  alphabet = ['x'; 'y'];
  delta = (fun q x -> match (q, x) with
                      | (10, 'x') -> 11
	              | (11, 'x') -> 12
	              | (11, 'y') -> 11
	              | (12, 'x') -> 12
	              | (12, 'y') -> 11
	              | (_, _) -> 13);
  start = 10;
  final = [12]
} 


(* QUESTION 1 *)

let absNonZero (xs: int list): int list =
  failwith "Not implemented"

  
let map_functions (fs: ('a -> 'b) list) (x: 'a): 'b list =
  failwith "Not implemented"

  
let compose_all (fs: ('a -> 'a) list) (x: 'a): 'a =
  failwith "Not implemented"


let pairs1 (x: 'a) (ys: 'b list): ('a * 'b) list =
  failwith "Not implemented"

  
let pairs (xs: 'a list) (ys: 'b list): ('a * 'b) list =
  failwith "Not implemented"



(* Question 2 *)

let cons_all (x: 'a) (xss: 'a list list): 'a list list = 
  failwith "Not implemented"
    

let prefixes (xs: 'a list): 'a list list = 
  failwith "Not implemented"

  
let suffixes (xs: 'a list): 'a list list =
  failwith "Not implemented"


let splits (xs: 'a list): ('a list * 'a list) list =
  failwith "Not implemented"

  
let inject (x: 'a) (xs: 'a list): 'a list list =
  failwith "Not implemented"

  
(* BONUS *)

let permutations (xs: 'a list): 'a list list = 
  failwith "Not implemented"



   
(* Question 3 *)

let accept (m: dfa) (input: string): bool =
  failwith "Not implemented"



  

  
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

let lang (m: dfa) (n: int) = 

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
