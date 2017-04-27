(* 

HOMEWORK 9

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
 * DO NOT MODIFY THIS CODE, INCLUDING ADDING FUNCTIONS 
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
    let mk h t = R (h, memoize t) 
    let unmk1 s = let R (h,t) = s in h
    let unmk2 s = let R (h,t) = s in t ()
    let rec cst v = mk v (fun () -> cst v)
    let fby s1 ps2 = mk (unmk1 s1) ps2
    let rec map f s = mk (f (unmk1 s)) (fun () -> map f (unmk2 s))
    let rec map2 f s1 s2 = mk (f (unmk1 s1) (unmk1 s2)) (fun () -> map2 f (unmk2 s1) (unmk2 s2))
    let rec filter p ctl s = if p (unmk1 ctl) (unmk1 s) then mk (unmk1 s) (fun () -> filter p (unmk2 ctl) (unmk2 s)) else filter p (unmk2 ctl) (unmk2 s)
    let split s = (cst (unmk1 s), unmk2 s)
    let rec zip s1 s2 = mk (unmk1 s1, unmk1 s2) (fun () -> zip (unmk2 s1) (unmk2 s2))
    let rec prefix n s = if n > 0 then (unmk1 s)::(prefix (n-1) (unmk2 s)) else []
    let print_stream tr n s =
      let rec loop n s = 
        if n > 0 then (print_string ((tr (unmk1 s))^" "); loop (n-1) (unmk2 s))
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

let rec gen_nats () = fby (cst 0)
                        (fun () -> (map (fun x -> x+1) (gen_nats ())))
let nats = gen_nats()

let evens = map (fun x -> 2*x) nats
let odds = map (fun x -> x+1) evens


(* some test streams *)

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
  failwith "Not implemented: scale"


let mult (s1:int stream) (s2:int stream):int stream =
  failwith "Not implemented: mult"


let zip (s1:'a stream) (s2:'b stream):('a * 'b) stream = 
  failwith "Not implemented: zip"


let unzip (s:('a * 'b) stream):('a stream * 'b stream) =
  failwith "Not implemented: unzip"


let rec fold (f:'a -> 'b -> 'b) (init_s:'b stream) (s:'a stream):'b stream =
  failwith "Not implemented: fold"


let running_max (s:int stream):int stream =
  failwith "Not implemented: running_max"


let rec stutter (s:'a stream):'a stream =
  failwith "Not implemented: stutter"



(*
 * QUESTION 2
 * 
 *)

let rec arctan (z:float):float stream =
  failwith "Not implemented: arctan"


let pi ():float stream = 
  failwith "Not implemented: pi"

    
let rec newton (f:float -> float) (df:float -> float) (guess:float):float stream =
  failwith "Not implemented: newton"


let derivative (f:float -> float) (x:float):float stream =
  failwith "Not implemented: derivative"


let limit (epsilon:float) (s:float stream):float stream =
  failwith "Not implemented: limit"
