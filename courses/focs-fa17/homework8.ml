(* 

HOMEWORK 8

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
 * PLEASE DO NOT CHANGE THE TYPES IN THE STUBS I GIVE YOU. 
 * Doing so risks make it impossible to test your code.
 *
 * Always make sure you can #use this file before submitting it.
 * Do that in a _fresh_ OCaml shell 
 * It has to load without any errors.
 *
 *)


(* 
 * The internal implementation for streams 
 *
 * DO NOT MODIFY THIS CODE, INCLUDING ADDING FUNCTIONS TO THE INTERFACE
 *
 *)

module AbsStream :
  sig
      type 'a stream 
      val cst : 'a -> 'a stream
      val fby : 'a stream -> (unit -> 'a stream) -> 'a stream
      val map : ('a -> 'b) -> 'a stream -> 'b stream
      val map2 : ('a -> 'b -> 'c) -> 'a stream -> 'b stream -> 'c stream
      val filter : ('a -> 'b -> bool) -> 'a stream -> 'b stream -> 'b stream
      val split : 'a stream -> ('a stream * 'a stream)
      val print_stream : ('a -> string) -> int -> 'a stream -> unit
    end = 
  struct
    type 'a stream = R of 'a * (unit -> 'a stream)
    let memoize f = 
      let memoized = ref None in
      let new_f () = 
	match !memoized with
	| None -> let result = f () in memoized := Some result; result
	| Some v -> v   in
      new_f
    let f1 h t = R (h, memoize t) 
    let f2 s = let R (h,t) = s in h
    let f3 s = let R (h,t) = s in t ()
    let rec cst v = f1 v (fun () -> cst v)
    let fby s1 ps2 = f1 (f2 s1) ps2
    let rec map f s = f1 (f (f2 s)) (fun () -> map f (f3 s))
    let rec map2 f s1 s2 = f1 (f (f2 s1) (f2 s2)) (fun () -> map2 f (f3 s1) (f3 s2))
    let rec filter p ctl s = if p (f2 ctl) (f2 s) then f1 (f2 s) (fun () -> filter p (f3 ctl) (f3 s)) else filter p (f3 ctl) (f3 s)
    let split s = (cst (f2 s), f3 s)
    let rec zip s1 s2 = f1 (f2 s1, f2 s2) (fun () -> zip (f3 s1) (f3 s2))
    let rec prefix n s = if n > 0 then (f2 s)::(prefix (n-1) (f3 s)) else []
    let print_stream tr n s =
      let rec loop n s = 
        if n > 0 then (print_string ((tr (f2 s))^" "); loop (n-1) (f3 s))
        else (print_string "...>\n") in
      print_string "< " ; loop n s
  end


(*
 * 
 * THESE ARE THE FUNCTIONS YOU GET TO USE
 *
 *)

type 'a stream = 'a AbsStream.stream
let cst : 'a -> 'a stream = AbsStream.cst
let fby : 'a stream -> (unit -> 'a stream) -> 'a stream = AbsStream.fby
let map : ('a -> 'b) -> 'a stream -> 'b stream = AbsStream.map
let map2 : ('a -> 'b -> 'c) -> 'a stream -> 'b stream -> 'c stream = AbsStream.map2
let filter : ('a -> 'b -> bool) -> 'a stream -> 'b stream -> 'b stream = AbsStream.filter
let split : 'a stream -> ('a stream * 'a stream) = AbsStream.split
let print_stream : ('a -> string) -> int -> 'a stream -> unit = AbsStream.print_stream




(* 
 *  Some helper functions to print a stream:
 *
 *  They are simple wrapper around print_stream
 *
 *)

let pri s = print_stream string_of_int 20 s
let prip s = print_stream (fun (x,y) -> "("^(string_of_int x)^","^(string_of_int y)^")") 20 s
let prs s = print_stream (fun x -> x) 20 s
let prf s = print_stream string_of_float 20 s


(* Some functions we saw in class *)

let rec mk_nats () = fby (cst 0)
                         (fun () -> (map (fun x -> x+1) (mk_nats ())))

let nats = mk_nats ()

let evens = map (fun x -> 2*x) nats
let odds = map (fun x -> x+1) evens

let drop s = let (_,r) = split s in r


(* test streams *)

let s_ampl =
  let transf (v,(d,m)) =
    if d = 1 && v = m then (v-1,(-1,m))
    else if d = -1 && v = -m then (v+1,(1,m+1))
    else if d = 1 then (v+1,(1,m))
    else (v-1,(-1,m))  in
  let rec f () = fby (map2 (fun x y -> (x,y)) (cst 0) (cst (1,1)))
                     (fun () -> map transf (f ())) in
  map (fun (x,y) -> x) (f ())

let s_as = map (fun n -> "a"^(string_of_int n)) nats


(* 
 * QUESTION 1 
 * 
 *)

let scale (n:int) (s:int stream):int stream = 
  failwith "not implemented"

let mult (s1:int stream) (s2:int stream):int stream =
  failwith "not implemented"

let zip (s1:'a stream) (s2:'b stream):('a * 'b) stream = 
  failwith "not implemented"

let unzip (s:('a * 'b) stream):('a stream * 'b stream) =
  failwith "not implemented"

let rec fold (f:'a -> 'b -> 'b) (init_s:'b stream) (s:'a stream):'b stream =
  failwith "not implemented"

let running_max (s:int stream):int stream =
  failwith "not implemented"

let rec stutter (s:'a stream):'a stream =
  failwith "not implemented"



(*
 * QUESTION 2
 * 
 *)

let rec scalef r s = 
  failwith "not implemented"

let rec addf s t = 
  failwith "not implemented"

let rec psumsf s =
  failwith "not implemented"

let rec arctan (z:float):float stream =
  failwith "not implemented"

let pi ():float stream = 
  failwith "not implemented"
    
let rec newton (f:float -> float) (df:float -> float) (guess:float):float stream =
  failwith "not implemented"

let derivative (f:float -> float) (x:float):float stream =
  failwith "not implemented"

let limit (epsilon:float) (s:float stream):float stream =
  failwith "not implemented"


