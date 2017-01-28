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
 * Always make sure you can #use this file before submitting it.
 * It has to load without any errors.
 *
 *)





   (* Q1: Set functions *)

let rec inS e xs = 
  failwith "inS not implemented"


let rec subsetS xs ys = 
  failwith "subsetS not implemented"


let rec equalS xs ys = 
  failwith "equalS not implemented"


let rec unionS xs ys = 
  failwith "unionS not implemented"


let rec interS xs ys = 
  failwith "interS not implemented"


let rec sizeS xs = 
  failwith "sizeS not implemented"





   (* Q2: Language functions *)


let rec atMost n xs = 
  failwith "atMost not implemented"


let rec unionL n xs ys = 
  failwith "unionL not implemented"


let rec concatL n xs ys = 
  failwith "concatL not implemented"


let rec starL n xs = 
  failwith "starL not implemented"





   (* Q3: regular expressions *)

type re = 
    Empty 
  | Unit 
  | Letter of string 
  | Plus of re * re 
  | Times of re * re 
  | Star of re

let lang n s = 
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
  let rec eval re = 
    match re with
      Empty -> []
    | Unit -> [""]
    | Letter (a) -> [a]
    | Plus (r1,r2) -> unionL n (eval r1) (eval r2)
    | Times (r1,r2) -> concatL n (eval r1) (eval r2)
    | Star r -> starL n (eval r)  in
  eval (parse s)

let show l = 
  let rec loop l seen = 
    match l with
    | [] -> ()
    | s::rest -> if List.mem s seen 
                    then loop rest seen 
                  else (match s with 
		        | "" -> (print_string "  <empty string>\n"; 
				 loop rest (""::seen))
			| s -> (print_string ("  "^s^"\n"); 
				loop rest (s::seen))) in
  loop l []



let regexp_a = "???"

let regexp_b = "???"

let regexp_c = "???"

let regexp_d = "???"

let regexp_e = "???"

