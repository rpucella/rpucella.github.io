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
 *
 * Two modules for implementing a simple assembly language
 *
 * You don't have to understand the details of this
 *
 *)

module CPU0 = struct

  type instruction =
    | INC of int
    | DEC of int * int
    | JUMP of int
    | TRUE
    | FALSE
    | PRINT of int

  type program = instruction list

  let instruction_to_string i instr =
    match instr with
    | INC n -> Printf.sprintf "%04d INC %d\n" i n
    | DEC (n, addr) -> Printf.sprintf "%04d DEC (%d, %d)\n" i n addr
    | JUMP addr -> Printf.sprintf "%04d JUMP %d\n" i addr
    | TRUE -> Printf.sprintf "%04d TRUE\n" i
    | FALSE -> Printf.sprintf "%04d FALSE\n" i
    | PRINT n -> Printf.sprintf "%04d PRINT %d\n" i n

  let program_to_string instructions =
    String.concat "" (List.mapi instruction_to_string instructions)

  let pp instructions =
    print_string (program_to_string instructions)

  let max_register (p : program): int =
    let rec loop instrs curr_max =
      match instrs with
      | [] -> curr_max
      | (INC cntr)::instrs' -> loop instrs' (max curr_max cntr)
      | (DEC (cntr, _))::instrs' -> loop instrs' (max curr_max cntr)
      | _::instrs' -> loop instrs' curr_max in
    loop p 0

end


module CPU1 = struct

  (* Adding register names and labels *)
  
  type instruction =
    | INC of string
    | DEC of string * string
    | JUMP of string
    | TRUE
    | FALSE
    | PRINT of string
    | LABEL of string
    | REGISTER of string * int

  type program = instruction list

  let instruction_to_string instr =
    match instr with
    | INC r -> Printf.sprintf " INC %s\n" r
    | DEC (r, label) -> Printf.sprintf " DEC (%s, %s)\n" r label
    | JUMP label -> Printf.sprintf " JUMP %s\n" label
    | TRUE -> " TRUE\n"
    | FALSE -> " FALSE\n"
    | PRINT r -> Printf.sprintf " PRINT %s\n" r
    | LABEL label -> Printf.sprintf "LABEL %s\n" label
    | REGISTER (name, r) -> Printf.sprintf "REGISTER (%s, %d)\n" name r

  let program_to_string instructions =
    String.concat "" (List.map instruction_to_string instructions)

  let pp instructions =
    print_string (program_to_string instructions)

  let fresh =
    let counter = ref 0 in
    fun () -> let c = !counter in
              let _ = counter := c + 1 in
              (string_of_int c)

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
      | (LABEL n)::instrs' -> loop instrs' addr (SMap.add n addr map)
      | (REGISTER _)::instrs' -> loop instrs' addr map
      | _::instrs' -> loop instrs' (addr + 1) map  in
    loop instrs 0 SMap.empty

  let lookup sym map =
    try SMap.find sym map with
    | Not_found -> let _ = Printf.printf "Cannot find symbol %s\n" sym in
                   raise Not_found

  let compile0 (p: program): CPU0.program =
    let rmap = registerMap p in
    let lmap = labelMap p in
    let rec translate instrs =
      match instrs with
      | [] -> []
      | (INC r)::instrs' -> (CPU0.INC (lookup r rmap)) :: (translate instrs')
      | (DEC (r, label))::instrs' -> (CPU0.DEC (lookup r rmap, lookup label lmap)) :: (translate instrs')
      | (JUMP label)::instrs' -> (CPU0.JUMP (lookup label lmap)) :: (translate instrs')
      | TRUE::instrs' -> CPU0.TRUE :: (translate instrs')
      | FALSE::instrs' -> CPU0.FALSE :: (translate instrs')
      | (PRINT r)::instrs' -> (CPU0.PRINT (lookup r rmap)) :: (translate instrs')
      | (LABEL label)::instrs' -> translate instrs'
      | (REGISTER (name, r))::instrs' -> translate instrs'  in
    translate p

end



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


(*
 *  Compile a program in the above assembly language
 *  to a program in the CPU0 assembly language
 *
 *)

let compile0 (p: instruction list): CPU0.program =
  let rec translate instrs = 
    match instrs with
    | [] -> []
    | (INC r)::instrs' -> (CPU1.INC r) :: translate instrs'
    | (DEC (r, addr))::instrs' -> (CPU1.DEC (r, addr)) :: translate instrs'
    | (JUMP addr)::instrs' -> (CPU1.JUMP addr) :: translate instrs'
    | TRUE::instrs' -> (CPU1.TRUE) :: translate instrs'
    | FALSE::instrs' -> (CPU1.FALSE) :: translate instrs'
    | (PRINT r)::instrs' -> (CPU1.PRINT r) :: translate instrs'
    | (LABEL arg)::instrs' -> (CPU1.LABEL arg) :: translate instrs'
    | (REGISTER (r, cntr))::instrs' -> (CPU1.REGISTER (r, cntr)) :: translate instrs'
    | (EQUAL (r1, r2))::instrs' ->
       let label_start = Printf.sprintf "::EQUAL_%s" (CPU1.fresh ()) in
       let label_empty = Printf.sprintf "::EQUAL_%s" (CPU1.fresh ()) in
       let label_acc = Printf.sprintf "::EQUAL_%s" (CPU1.fresh ()) in
       let label_rej = Printf.sprintf "::EQUAL_%s" (CPU1.fresh ()) in
       [CPU1.LABEL label_start;
        CPU1.DEC (r1, label_empty);
        CPU1.DEC (r2, label_rej);
        CPU1.JUMP label_start;
        CPU1.LABEL label_empty;
        CPU1.DEC (r2, label_acc);
        CPU1.LABEL label_rej;
        CPU1.FALSE;
        CPU1.LABEL label_acc;
        CPU1.TRUE] @ translate instrs'  in
  CPU1.compile0 (translate p)


(*
 * Run a CPU program directly
 * First compiling it down to a CPU0 program using compile0
 *
 * Inputs are: program and list of initial values for the first few registers
 *
 *)
  
let run (p: instruction list) (nums: int list): bool =
  let p0 = compile0 p in
  let _ = print_string "----------------------------------------------------------------------\n" in
  let _ = CPU0.pp p0 in
  let _ = print_string "----------------------------------------------------------------------\n" in
  let p0_array = Array.of_list p0 in
  let registers =  Array.make (CPU0.max_register p0 + 1) 0 in
  let _ = List.iteri (fun i v -> Array.set registers i v) nums in
  let rec loop addr =
    let regs = String.concat " " (List.map string_of_int (Array.to_list registers)) in
    let _ = Printf.printf "%04d: %s\n" addr regs in
    match (Array.get p0_array addr) with
    | CPU0.INC (r) -> let _ = Array.set registers r ((Array.get registers r) + 1) in
                      loop (addr + 1) 
    | CPU0.DEC (r, addr') -> let v = Array.get registers r in
                             if (v > 0) then
                               let _ = Array.set registers r (v - 1) in
                               loop (addr + 1)
                             else
                               loop addr'
    | CPU0.JUMP addr' -> loop addr'
    | CPU0.TRUE -> true
    | CPU0.FALSE -> false
    | CPU0.PRINT r -> let _ = Printf.printf "      Register %d = %d\n" r (Array.get registers r) in
                      loop (addr + 1)  in
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
