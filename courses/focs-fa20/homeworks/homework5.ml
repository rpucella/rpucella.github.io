(* 

HOMEWORK 5

Due: Thu Nov 5, 2020 (23h59)

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
 *  The type for instructions - each row is a constructor representing an instruction
 *
 *) 

type instruction =
  | INC of string
  | DEC of string * string
  | JUMP of string
  | TRUE
  | FALSE
  | PRINT of string
  | LABEL of string
  | REGISTER of string * int
  | EQUAL of string * string

let pp instructions =
  let instruction_to_string i instr =
    match instr with
    | INC n -> Printf.sprintf "%04d INC %s\n" i n
    | DEC (n, addr) -> Printf.sprintf "%04d DEC (%s, %s)\n" i n addr
    | JUMP addr -> Printf.sprintf "%04d JUMP %s\n" i addr
    | TRUE -> Printf.sprintf "%04d TRUE\n" i
    | FALSE -> Printf.sprintf "%04d FALSE\n" i
    | PRINT n -> Printf.sprintf "%04d PRINT %s\n" i n
    | LABEL addr -> Printf.sprintf "%04d LABEL %s\n" i addr
    | REGISTER (r, n) -> Printf.sprintf "%04d REGISTER (%s, %d)\n" i r n
    | EQUAL (r1, r2) -> Printf.sprintf "%04d EQUAL (%s, %s)\n" i r1 r2  in
  let program_to_string instructions =
    String.concat "" (List.mapi instruction_to_string instructions)  in
  print_string (program_to_string instructions)


(* 
 * Helper functions
 *
 * Create a map from registers name to positions
 * Create a map from label names to instruction indices
 * Lookup a name in a map
 * Get the largest register used in a program
 *
 *)
  
module SMap = Map.Make(String)
    
let registerMap (instrs: instruction list): int SMap.t =
  let rec loop instrs map =
    match instrs with
    | [] -> map
    | (REGISTER (name, r))::instrs' -> loop instrs' (SMap.add name r map)
    | _::instrs' -> loop instrs' map  in
  loop instrs SMap.empty
    
let labelMap (instrs: instruction list): int SMap.t = 
  let rec loop instrs addr map =
    match instrs with
    | [] -> map
    | (LABEL n)::instrs' -> loop instrs' (addr + 1) (SMap.add n addr map)
    | _::instrs' -> loop instrs' (addr + 1) map  in
  loop instrs 0 SMap.empty

let lookup sym map =
  try SMap.find sym map with
  | Not_found -> let _ = Printf.printf "Cannot find symbol %s\n" sym in
                 raise Not_found

let max_register (p : instruction list): int =
  let rec loop instrs curr_max =
    match instrs with
    | [] -> curr_max
    | (REGISTER (_, cntr))::instrs' -> loop instrs' (max curr_max cntr)
    | _::instrs' -> loop instrs' curr_max in
  loop p 0

  
(*
 * Run a CPU program directly
 *
 * Inputs are: program and list of initial values for the first few registers
 *
 *)
  
let run (p: instruction list) (nums: int list): bool =
  let _ = print_string "----------------------------------------------------------------------\n" in
  let _ = pp p in
  let _ = print_string "----------------------------------------------------------------------\n" in
  let rmap = registerMap p in
  let lmap = labelMap p in
  let p_array = Array.of_list p in
  let registers =  Array.make (max_register p + 1) 0 in
  let _ = List.iteri (fun i v -> Array.set registers i v) nums in
  let rec loop addr =
    let regs = String.concat " " (List.map string_of_int (Array.to_list registers)) in
    let _ = Printf.printf "%04d: %s\n" addr regs in
    match (Array.get p_array addr) with
    | INC (r) -> let r = lookup r rmap in
                 let _ = Array.set registers r ((Array.get registers r) + 1) in
                 loop (addr + 1) 
    | DEC (r, addr') -> let r = lookup r rmap in
                        let addr' = lookup addr' lmap in
                        let v = Array.get registers r in
                        if (v > 0) then
                          let _ = Array.set registers r (v - 1) in
                          loop (addr + 1)
                        else
                          loop addr'
    | JUMP addr' -> let addr' = lookup addr' lmap in
                    loop addr'
    | TRUE -> true
    | FALSE -> false
    | PRINT r -> let r' = lookup r rmap in
                 let _ = Printf.printf "      Register %s = %d\n" r (Array.get registers r') in
                 loop (addr + 1)
    | EQUAL (r1, r2) -> let r1 = lookup r1 rmap in
                        let r2 = lookup r2 rmap in
                        Array.get registers r1 = Array.get registers r2
    | _ -> loop (addr + 1)  in
  loop 0


(*
 *
 * Some very simple programs
 *
 *)
 

let p_reset = [REGISTER ("X", 0);
               LABEL "loop";
               DEC ("X", "done");
               JUMP "loop";
               LABEL "done";
               TRUE]

let p_transfer = [REGISTER ("X", 0);
                  REGISTER ("Y", 1);
                  LABEL "loop";
                  DEC ("X", "done");
                  INC "Y";
                  JUMP "loop";
                  LABEL "done";
                  TRUE]

let p_succ = [REGISTER ("X", 0);
              REGISTER ("Z", 1);
	      INC "X";
	      EQUAL ("X", "Z")]



(* QUESTION 1 *)


let p_copy: instruction list = [REGISTER ("X", 0);
                                REGISTER ("Y", 1)]


let p_swap: instruction list = [REGISTER ("X", 0);
                                REGISTER ("Y", 1)]


let p_plus: instruction list = [REGISTER ("X", 0);
                                REGISTER ("Y", 1);
                                REGISTER ("Z", 2)]


let p_sub: instruction list = [REGISTER ("X", 0);
                               REGISTER ("Y", 1);
                               REGISTER ("Z", 2)]


let p_max: instruction list = [REGISTER ("X", 0);
                               REGISTER ("Y", 1);
                  	     REGISTER ("Z", 2)]
             
           
let p_diff: instruction list = [REGISTER ("X", 0);
                                REGISTER ("Y", 1);
                                REGISTER ("Z", 2)]
               

(* QUESTION 2 *)


let p_times: instruction list = [REGISTER ("X", 0);
                                 REGISTER ("Y", 1);
                                 REGISTER ("Z", 2)]


let p_square: instruction list = [REGISTER ("X", 0);
                                  REGISTER ("Z", 1)]


let p_square_x_2: instruction list = [REGISTER ("X", 0);
                                      REGISTER ("Z", 1)]
            

(* QUESTION 3 *)

let p_half: instruction list = [REGISTER ("X", 0);
                                REGISTER ("Z", 1)]
