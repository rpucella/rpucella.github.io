(* TUTORIAL FROM CLASS 4/14/2016 *)


(* The underlying implementation for streams 
 *
 * Basically, a stream is a pair of an element and a 
 * "promise" to compute the rest of the stream.
 * 
 * That "promise" is represented as a function
 *
 * The implementation memoizes that function: once the function is
 * called once, it remembers its result, and subsequent calls to the
 * function directly return the result without executing the body of
 * the function
 *
 * You don't need to know anything about this code -- you will
 * use functions fby, head, and tail described below instead
 *)

module AbsStream :
  sig
      type 'a stream 
      val mk : 'a -> (unit -> 'a stream) -> 'a stream 
      val unmk1 : 'a stream -> 'a 
      val unmk2 : 'a stream -> 'a stream
      val cst : 'a -> 'a stream
      val fby : 'a stream -> (unit -> 'a stream) -> 'a stream
      val map : ('a -> 'b) -> 'a stream -> 'b stream
      val filter : ('a -> 'b -> bool) -> 'a stream -> 'b stream -> 'b stream
      val split : 'a stream -> ('a stream * 'a stream)
      val zip : 'a stream -> 'b stream -> ('a * 'b) stream
      val prefix : int -> 'a stream -> 'a list
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
    let rec filter p ctl s = if p (unmk1 ctl) (unmk1 s) then mk (unmk1 s) (fun () -> filter p (unmk2 ctl) (unmk2 s)) else filter p (unmk2 ctl) (unmk2 s)
    let split s = (cst (unmk1 s), unmk2 s)
    let rec zip s1 s2 = mk (unmk1 s1, unmk1 s2) (fun () -> zip (unmk2 s1) (unmk2 s2))

    let rec prefix n s = if n > 0 then (unmk1 s)::(prefix (n-1) (unmk2 s)) else []

  end


(*
 * These are the stream functions you will use
 *
 *)

type 'a stream = 'a AbsStream.stream

let cst : 'a -> 'a stream = AbsStream.cst
        (* constant *)

let fby : 'a stream -> (unit -> 'a stream) -> 'a stream = AbsStream.fby
        (* followed by *)

let map : ('a -> 'b) -> 'a stream -> 'b stream = AbsStream.map
        (* map a function over a stream *)

let filter : ('a -> 'b -> bool) -> 'a stream -> 'b stream -> 'b stream = AbsStream.filter
           (* filter a stream based on a control stream and a predicate *)

let zip : 'a stream -> 'b stream -> ('a * 'b) stream = AbsStream.zip
        (* zip two streams into a stream of pairs *)

let split : 'a stream -> ('a stream * 'a stream) = AbsStream.split
          (* split a stream into two streams *)

let prefix : int -> 'a stream -> 'a list = AbsStream.prefix
           (* return the first n elements of a stream *)

let rec nth n s =
  if n > 0 then nth (n-1) (AbsStream.unmk2 s) else AbsStream.unmk1 s
           (* return the nth element of a stream *)



(* Note that feedback has to go through functions in OCaml, so
   some streams such as nats are obtained by first constructing a
   function to return that stream *)

let nats =
  let rec natsF () = fby (cst 0)
                         (fun () -> (map (fun x -> x+1) (natsF ()))) in
  natsF ()

let evens =
  let rec evensF () = fby (cst 0)
                          (fun () -> (map (fun x -> x+2) (evensF ()))) in
  evensF ()

let odds =
  map (fun x -> x+1) evens

let add s1 s2 =
  map (fun (x,y) -> x+y) (zip s1 s2)

let triangles =
  let rec trianglesF () = add nats (fby (cst 0)
                                        (fun () -> trianglesF ()))  in
  trianglesF ()

let drop s = let (f,r) = split s in r

let fib =
  let rec fibF () = fby (cst 0)
                        (fun () -> fby (cst 1)
			               (fun () -> let x = fibF () in
				                  add x (drop x))) in
  fibF ()

let rec psums s =
  fby s
      (fun () -> add (psums s) (drop s))

let notdivides c x =
  x mod c <> 0

let rec sieve s =
  let (f,r) = split s in
  fby f
      (fun () -> sieve (filter notdivides f r))

let primes =
  sieve (map (fun s -> s+2) nats)
