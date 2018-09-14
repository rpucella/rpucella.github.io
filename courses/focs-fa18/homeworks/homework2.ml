(* 

HOMEWORK 2

Due: Thu 9/20/18 23h59.

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


(* types for flow diagrams *)

type location = string
type node = int
type action = string
            
type diagram = (node * location * action * node * node) list
             
type config = (location * int) list


(* helper function to print a configuration *)
            
let print_config (nd:node) (cfg:config):unit =
  let _ = Printf.printf "[%i]  " nd  in
  let rec loop cfg = 
    match cfg with
    | [] -> print_newline ()
    | (loc,v)::cfg' -> let _ = Printf.printf "%s %i  " loc v  in loop cfg'  in
loop cfg

  

(* some simple flow diagram tables *)
  
let p_empty : diagram = (* X -> 0 *)
  [ (0,"","",1,1);
    (1,"X","-",1,-1) ]

let p_transfer : diagram = (* X -> Y *)
  [ (0,"","",1,1);
    (1,"X","-",2,-1);
    (2,"Y","+",1,1) ]

  
let p_copy: diagram =  (* X -> X; Y -> X + Y via T1*)
  [ (0,"","",1,1);
    (1,"X","-",2,4);
    (2,"Y","+",3,3);
    (3,"T1","+",1,1);
    (4,"T1","-",5,-1);
    (5,"X","+",4,4) ]

let p_plus : diagram =   (* Z := X + Y via T1 *)
   [(0,"","",1,1);

      (* Z := X *)
    (1,"X","-",2,4);
    (2,"Z","+",3,3);
    (3,"T1","+",1,1);
    (4,"T1","-",5,10);
    (5,"X","+",4,4);

      (* Y -> T1 *)
    (10,"Y","-",11,20);
    (11,"T1","+",10,10);

    (20,"T1","-",21,-1);
    (21,"Y","+",22,22);
      (* Z -> Z + 1 *)
    (22,"Z","+",20,20) ]

  
let p_times : diagram =   (* Z := X * Y via T1,T2,T3 *) 
  [ (0,"","",1,1);

    (* Z := 0 *)

    (* Y -> T1 *)
    (1,"Y","-",2,10);
    (2,"T1","+",1,1);

    (10,"T1","-",11,-1);
    (11,"Y","+",12,12);
    (* Z -> T2 *)
    (12,"Z","-",13,20);
    (13,"T2","+",12,12);
    
    (* Z := T2 + X *)   (* p_plus with X -> T2,  Y -> X, Z -> Z, T1 -> T3 *)
    (20,"T2","-",21,23);
    (21,"Z","+",22,22);
    (22,"T3","+",20,20);
    (23,"T3","-",24,30);
    (24,"T2","+",23,23);
    (30,"X","-",31,40);
    (31,"T3","+",30,30);
    (40,"T3","-",41,50);
    (41,"X","+",42,42);
    (42,"Z","+",40,40);

    (* empty T2 *)
    (50,"T2","-",50,10) ]
    

  
                                                              
(* QUESTION 1 *)

  
let rec find (x:node) (p:diagram):(node * location * action * node * node) =
  failwith "not implemented"

  
let rec get_loc (cfg:config) (loc:location):int =
  failwith "not implemented"

  
let rec set_loc (cfg:config) (loc:location) (v:int):config =
  failwith "not implemented"
                                  

let next (prg:diagram) (cfg:config) (nd:node):(node * config) =
  failwith "not implemented"
  
  
let run (prg:diagram) (cfg:config):config =
  failwith "not implemented"
  

(* helper function to make testing a bit easier *)
  
let calculate (prg:diagram) (inputs:location list) (output:location) (temps:location list) (args:int list):int =
  let start_cfg = (List.map2 (fun x y -> (x,y)) inputs args)@[(output,0)]@(List.map (fun x -> (x,0)) temps)  in
  get_loc (run prg start_cfg) output



  
(* QUESTION 2 - SIMPLE FLOW DIAGRAMS *)

  
let p_sub : diagram = []

let p_max : diagram = []
  
let p_diff : diagram = []



(* QUESTION 3 *)

let rec rename (rename_list:(location * location) list) (name:location):location =
  failwith "not implemented"

  
let rec incl (diag:diagram) (offset:int) (stop:int) (loc_rename:(location * location) list):diagram =
  failwith "not implemented"

  
let p_div : diagram = []

