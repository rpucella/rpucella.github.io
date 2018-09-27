
(* Function to check if a string is in the language
   described by a regular expression *)


type sr_re = 
    SREmpty 
  | SRUnit 
  | SRLetter of string 
  | SRPlus of sr_re * sr_re 
  | SRTimes of sr_re * sr_re 
  | SRStar of sr_re 


let string_in_regexp (str:string) (re:string):bool =

  let mkSRTimes = function
    | (SREmpty,_) -> SREmpty
    | (_,SREmpty) -> SREmpty
    | (SRUnit,r) -> r
    | (r,SRUnit) -> r
    | (r1,r2) -> SRTimes(r1,r2)   in
  
  let mkSRPlus = function
    | (SREmpty,r) -> r
    | (r,SREmpty) -> r
    | (SRUnit,SRUnit) -> SRUnit
    | (r1,r2) -> SRPlus(r1,r2)   in
  
  let rec delta = function
    | SREmpty -> SREmpty
    | SRUnit -> SRUnit
    | SRLetter _ -> SREmpty
    | SRPlus (r1,r2) -> mkSRPlus(delta(r1),delta(r2))
    | SRTimes (r1,r2) -> mkSRTimes(delta(r1),delta(r2))
    | SRStar (r) -> SRUnit  in
  
  let rec deriv c = function
    | SREmpty -> SREmpty
    | SRUnit -> SREmpty
    | SRLetter x when x.[0] = c -> SRUnit
    | SRLetter x -> SREmpty
    | SRPlus (r1,r2) -> mkSRPlus(deriv c r1,
  			 deriv c r2)
    | SRTimes (r1,r2) -> mkSRPlus(mkSRTimes(delta r1,
  				deriv c r2),
  			  mkSRTimes(deriv c r1,
  				r2))
    | SRStar (r) -> mkSRTimes(deriv c r,
  		        SRStar(r))  in
      
  let fromChar c = String.make 1 c in
  let explode s = 
    let rec loop i result = 
      if i < 0 then result
      else loop (i-1) (s.[i]::result) in
    loop (String.length s - 1) []  in
  (* Grammar: 
   *
   * R ::= R1 + R
   *       R1
   * 
   * R1 ::= R2 R1
   *        R2
   * 
   * R2 ::= R3*
   *        R3
   * 
   * R3 ::= a
   *        1
   *        0 
   *        ( R )
   *)
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
              | Some (r2,cs) -> Some (SRPlus(r1,r2),cs)))
  and parse_R1 cs = 
    match parse_R2 cs with
      None -> None
    | Some (r1,cs) -> 
        (match parse_R1 cs with
           None -> Some (r1,cs)
         | Some (r2,cs) -> Some (SRTimes(r1,r2),cs))  
  and parse_R2 cs = 
    match parse_R3 cs with
      None -> None
    | Some (r1,cs) -> 
        (match expect '*' cs with
           None -> Some (r1,cs)
         | Some cs -> Some (SRStar(r1),cs))
  and parse_R3 cs = 
    match expect_alpha cs with
      Some (a,cs) -> Some (SRLetter(fromChar(a)),cs)
    | None -> 
        (match expect '1' cs with
           Some cs -> Some (SRUnit, cs)
         | None -> 
             (match expect '0' cs with
                Some cs -> Some (SREmpty,cs)
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
  let rec m re = function 
    | [] -> (match delta re with SRUnit -> true | _ -> false)
    | c::cs -> m (deriv c re) cs  in
  m (parse re) (explode str)
  


(*
 
# string_in_regexp "" regexp_d;;
- : bool = false
# string_in_regexp "d" regexp_d;;
- : bool = true
# string_in_regexp "dd" regexp_d;;
- : bool = false
# string_in_regexp "ddd" regexp_d;;
- : bool = true
# string_in_regexp "dddd" regexp_d;;
- : bool = false
# string_in_regexp "ddddd" regexp_d;;
- : bool = true
# string_in_regexp "eeded" regexp_d;;
- : bool = false
# string_in_regexp "eededd" regexp_d;;
- : bool = true
# string_in_regexp "eededdee" regexp_d;;
- : bool = true
# string_in_regexp "dededededee" regexp_d;;
- : bool = true
# string_in_regexp "dededededeed" regexp_d;;
- : bool = false
# string_in_regexp "eededeeeeeedee" regexp_d;;
- : bool = true
# string_in_regexp "eededeeeeeedede" regexp_d;;
- : bool = false
# string_in_regexp "eededeeeeeededde" regexp_d;;
- : bool = true
# string_in_regexp "eededeeeeeededdde" regexp_d;;
- : bool = false

*)
