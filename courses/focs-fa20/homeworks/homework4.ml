(* 

HOMEWORK 4

Due: Thu Oct 22, 2020 (23h59)

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
 * Type for deterministic Turing machines
 *
 * States are integers
 *
 *)

type tm = {
    states : int list;
    input_alphabet : char list;
    tape_alphabet : char list;
    left_marker : char;
    blank : char;
    delta : (int * char) -> (int * char * int);   (* 0 = Left, 1 = Right *)
    start : int;
    accept : int;
    reject : int
  }

type config = {
    state : int;
    tape: char list;
    position: int
  }


(* 
 * Some sample deterministic Turing machines
 *
 * asbs is the regular language {a^m b^n | m,n >= 0}
 * anbn is the non-regular language {a^n b^n | n >= 0}
 * anbncn is the non-regular language {a^n b^n c^n | n >= 0}
 *
 *)

let asbs =
  let d inp = (match inp with
               | (1, 'a') -> (1, 'a', 1)
               | (1, 'b') -> (10, 'b', 1)
               | (1, '>') -> (1, '>', 1)
               | (1, '_') -> (777, '_', 1)
               | (10, 'b') -> (10, 'b', 1)
               | (10, '_') -> (777, '_', 1)
               | (777, 'a') -> (777, 'a', 1)
               | (777, 'b') -> (777, 'b', 1)
               | (777, '>') -> (777, '>', 1)
               | (777, '_') -> (777, '_', 1)
               | (_,c) -> (666,c,1))
  in { states = [1; 10; 777; 666];
       input_alphabet = ['a';'b'];
       tape_alphabet = ['a';'b';'_';'>'];
       blank = '_';
       left_marker = '>';
       start = 1;
       accept = 777;
       reject = 666;
       delta = d }

let anbn =
  let d inp = (match inp with
	       | (1, 'a') -> (1, 'a', 1)
     	       | (1, 'b') -> (10, 'b', 1)
	       | (1, '|') -> (1, '|', 1)
	       | (1, '-') -> (11, '-', 1)
	       | (10, 'b') -> (10, 'b', 1)
	       | (10, '.') -> (11, '.', 1)
	       | (11, '|') -> (12, '|', 1)
	       | (11, 'a') -> (11, 'a', 0)
	       | (11, 'b') -> (11, 'b', 0)
	       | (11, 'X') -> (11, 'X', 0)
	       | (11, '.') -> (11, '.', 0)
	       | (12, 'X') -> (12, 'X', 1)
	       | (12, '.') -> (777, '.', 1)
	       | (12, 'a') -> (13, 'X', 1)
	       | (13, 'a') -> (13, 'a', 1)
	       | (13, 'X') -> (13, 'X', 1)
	       | (13, 'b') -> (11, 'X', 1)
	       | (777, 'a') -> (777, 'a', 1)
	       | (777, 'b') -> (777, 'b', 1)
	       | (777, '|') -> (777, '|', 1)
	       | (777, 'X') -> (777, 'X', 1)
	       | (777, '.') -> (777, '.', 1)
	       | (_,c) -> (666,c,1))
  in { states = [1; 10; 11; 12; 13; 777; 666];
       input_alphabet = ['a';'b'];
       tape_alphabet = ['a';'b';'X';'.';'|'];
       blank = '.';
       left_marker = '|';
       start = 1;
       accept = 777;
       reject = 666;
       delta = d }
   

let anbncn =
  let d inp = (match inp with
	       | (1, 'a') -> (1, 'a', 1)
     	       | (1, 'b') -> (10, 'b', 1)
	       | (1, 'c') -> (15, 'c', 1)
	       | (1, '>') -> (1, '>', 1)
	       | (1, '_') -> (11, '_', 1)
	       | (10, 'b') -> (10, 'b', 1)
	       | (10, 'c') -> (15, 'c', 1)
	       | (10, '_') -> (11, '_', 1)
	       | (11, '>') -> (12, '>', 1)
	       | (11, 'a') -> (11, 'a', 0)
	       | (11, 'b') -> (11, 'b', 0)
	       | (11, 'c') -> (11, 'c', 0)
	       | (11, '_') -> (11, '_', 0)
	       | (11, 'X') -> (11, 'X', 0)
	       | (12, 'X') -> (12, 'X', 1)
	       | (12, '_') -> (777, '_', 1)
	       | (12, 'a') -> (13, 'X', 1)
	       | (13, 'a') -> (13, 'a', 1)
	       | (13, 'X') -> (13, 'X', 1)
	       | (13, 'b') -> (14, 'X', 1)
	       | (14, 'b') -> (14, 'b', 1)
	       | (14, 'X') -> (14, 'X', 1)
	       | (14, 'c') -> (11, 'X', 1)
	       | (15, 'c') -> (15, 'c', 1)
	       | (15, '_') -> (11, '_', 1)
	       | (777, 'a') -> (777, 'a', 1)
	       | (777, 'b') -> (777, 'b', 1)
	       | (777, 'c') -> (777, 'c', 1)
	       | (777, '>') -> (777, '>', 1)
	       | (777, 'X') -> (777, 'X', 1)
	       | (777, '_') -> (777, '_', 1)
	       | (_,c) -> (666, c,1))
  in { states = [1;10;11;12;13;14;15;777;666];
       input_alphabet = ['a';'b';'c'];
       tape_alphabet = ['a';'b';'c';'X';'_';'>'];
       blank = '_';
       left_marker = '>';
       start = 1;
       accept = 777;
       reject = 666;
       delta = d }


(*
 * Helper functions
 *
 *   explode : string -> char list
 *      return the list of characters making up a string
 *
 *   printConfig: tm -> config -> 'a -> 'a
 *      print a configuration (including newline) to standard output
 *      and return a value
 *
 *   validateStates : tm -> bool
 *      check that all the states reachable from the start state
 *      appear in the set of states of the Turing machine
 *
 *)

let explode (str: string): char list = 
  let rec acc (index, result) = 
    if (index < 0) then
      result
    else
      acc (index - 1, (String.get str index) :: result)
  in
  acc (String.length (str) - 1, [])

let printConfig (m: tm) (c: config): unit =
  let maxStateLength a r = max (String.length (string_of_int a)) r in
  let mw = List.fold_right maxStateLength m.states 0 in
  let padding = max 0 (c.position + 1 - List.length c.tape) in
  let rec mkBlank k =
    match k with
    | 0 -> []
    | _ -> m.blank :: (mkBlank (k - 1)) in
  let tape' : char list = c.tape @ (mkBlank padding) in
  let str_state = string_of_int c.state in
  let _ = print_string (String.sub (str_state ^ (String.make mw ' ')) 0 mw) in
  let _ = print_string "  "  in
  let _ = List.iteri (fun i sym -> 
              if (i = c.position) then
                Printf.printf "[%c]" sym
	      else
                Printf.printf " %c " sym) tape'  in
  print_newline ()


let validateStates (m: tm):bool =
  let rec addTransitions q syms queue =
    match syms with
    | [] -> queue
    | sym::syms' -> let (p, _, _) = m.delta (q, sym) in
                    addTransitions q syms' (p :: queue) in
  let rec loop seen queue =
    match queue with
    | [] -> true
    | q::qs -> if not (List.mem q m.states) then
                 let str_q = string_of_int q in
                 failwith ("Reachable state " ^ str_q ^ " not defined in states list")
               else if not (List.mem q seen) then
                 let str_q = string_of_int q in
                 let _ = print_string ("Following state " ^ str_q ^ "\n") in
                 loop (q :: seen) (addTransitions q m.tape_alphabet qs)
               else
                 loop seen qs  in
  loop [] [m.start]
  



(* QUESTION 1 *)


let startConfig (m:tm) (w:string): config =
  failwith "Not implemented"


let acceptConfig (m: tm) (c: config): bool = 
  failwith "Not implemented"


let rejectConfig (m: tm) (c: config): bool = 
  failwith "Not implemented"


let rec replace_nth (lst: 'a list) (n: int) (s: 'a): 'a list =
  failwith "Not implemented"


let step (m: tm) (c: config): config =
  failwith "Not implemented"


let run (m: tm) (w: string): bool = 
  failwith "Not implemented"
  


(* QUESTION 2 *)


let dummyTM = { states = [0; 1];
		input_alphabet = ['x'];
		tape_alphabet = ['x'; '_'; '>'];
		blank = '_';
		left_marker = '>';
		start = 0;
		accept = 0;
		reject = 1;
		delta = (fun (x,y) -> (x, y, 0))}
            

let tm_q2_ab3 : tm = dummyTM


let tm_q2_not : tm = dummyTM


let tm_q2_and : tm = dummyTM


let tm_q2_plus1 : tm = dummyTM

                     
let tm_q2_duplicate : tm = dummyTM


