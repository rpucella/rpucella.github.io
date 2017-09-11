(* 

HOMEWORK 2

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



(* Q1: Set operations *)


let rec set_in (e:'a) (xs:'a list):bool = 
  failwith "not implemented"


let rec set_sub (xs:'a list) (ys:'a list):bool = 
  failwith "not implemented"


let rec set_eq (xs:'a list) (ys:'a list):bool = 
  failwith "not implemented"


let rec set_union (xs:'a list) (ys:'a list):'a list = 
  failwith "not implemented"


let rec set_inter (xs:'a list) (ys:'a list):'a list = 
  failwith "not implemented"


let rec set_size (xs:'a list):int = 
  failwith "not implemented"




(* Q2: Language operations *)


let rec lang_union (xs:string list) (ys:string list):string list = 
  failwith "not implemented"


let rec lang_concat (xs:string list) (ys:string list):string list = 
  failwith "not implemented"


let rec lang_nstar (n:int) (xs:string list):string list = 
  failwith "not implemented"




(* Q3: regular expressions *)

type re = 
    Empty 
  | Unit 
  | Letter of string 
  | Plus of re * re 
  | Times of re * re 
  | Star of re

let compute_lang n s = 
  let fromChar c = String.make 1 c in
  let explode s = 
    let rec loop i result = 
      if i < 0 then result
      else loop (i-1) (s.[i]::result) in
    loop (String.length s - 1) []  in
  let isalpha = function 'A'..'Z'|'a'..'z' -> true | _ -> false in
  let expect c cs = 
    match cs with 
      f::cs when f = c -> Some cs
    | _ -> None in
  let expect_alpha cs = 
    match cs with
      f::cs when isalpha f -> Some (f,cs)
    | _ -> None  in
  let rec parse_R cs = 
    match parse_R1 cs with
      None -> None
    | Some (r1,cs) -> 
        (match expect '+' cs with
           None -> Some (r1,cs)
         | Some cs -> 
             (match parse_R cs with
                None -> None
              | Some (r2,cs) -> Some (Plus(r1,r2),cs)))
  and parse_R1 cs = 
    match parse_R2 cs with
      None -> None
    | Some (r1,cs) -> 
        (match parse_R1 cs with
           None -> Some (r1,cs)
         | Some (r2,cs) -> Some (Times(r1,r2),cs))  
  and parse_R2 cs = 
    match parse_R3 cs with
      None -> None
    | Some (r1,cs) -> 
        (match expect '*' cs with
           None -> Some (r1,cs)
         | Some cs -> Some (Star(r1),cs))
  and parse_R3 cs = 
    match expect_alpha cs with
      Some (a,cs) -> Some (Letter(fromChar(a)),cs)
    | None -> 
        (match expect '1' cs with
           Some cs -> Some (Unit, cs)
         | None -> 
             (match expect '0' cs with
                Some cs -> Some (Empty,cs)
              | None -> parse_parens cs))
  and parse_parens cs = 
    match expect '(' cs with
      None -> None
    | Some cs -> 
        (match parse_R cs with
           None -> None
         | Some (r,cs) -> 
             (match expect ')' cs with
                None -> None
              | Some cs -> Some (r,cs)))  in
  let parse s = 
    let cs = explode s in
    match parse_R cs with
      Some (re,[]) -> re
    | _ -> failwith ("Cannot parse "^s)  in
  let rec eval re k = 
    match re with
      Empty -> k []
    | Unit -> k [""]
    | Letter (a) -> k [a]
    | Plus (r1,r2) -> eval r1 (fun l1 -> eval r2 (fun l2 -> k (lang_union l1 l2)))
    | Times (r1,r2) -> eval r1 (fun l1 -> eval r2 (fun l2 -> k (lang_concat l1 l2)))
    | Star r -> eval r (fun l -> k (lang_nstar n l))  in
  eval (parse s) (fun x -> x)




let lang r n ml = 

  let l = List.filter (fun s -> String.length s <= ml) (compute_lang n r)  in

  let cmp a b = if String.length a < String.length b then -1
                else if String.length b < String.length a then 1
                else String.compare a b   in

  let sl = List.sort cmp l in

  let rec loop l seen = 
    match l with
    | [] -> ()
    | s::rest -> if List.mem s seen 
                   then loop rest seen 
                 else (match s with 
                       | "" -> let _ = print_string "  <epsilon>\n" in loop rest (""::seen)
                       | s -> let _ = print_string ("  "^s^"\n") in loop rest (s::seen)) in
  loop sl []
    



(* QUESTION 3 *)

let regexp_a : string = ""
let regexp_b : string = ""
let regexp_c : string = ""
let regexp_d : string = ""
let regexp_e : string = ""
