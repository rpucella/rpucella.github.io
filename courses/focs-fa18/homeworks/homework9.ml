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
 * Doing so will make it impossible to test your code.
 *
 * Always make sure you can #use this file before submitting it.
 * It has to load without any errors.
 *
 *)



(* 
 * The implementation of dataflow networks
 *
 * I've isolated it away into a module that you don't need to peer into
 * 
 * Please do not modify it -- I will be using my own copy, and your code
 * might break if you change it
 *
 *)

module S : sig

type 'a network
type 'a syntax

val cst : 'a -> 'a syntax
val fby : string -> string -> 'a syntax
val map : ('a -> 'a) -> string -> 'a syntax
val map2 : ('a -> 'a -> 'a) -> string -> string -> 'a syntax
val splitFirst : string -> 'a syntax
val splitRest : string -> 'a syntax
val filter : ('a -> 'a -> bool) -> string -> string -> 'a syntax
val component : 'a network -> string list -> 'a syntax
val rcomponent : string list -> 'a syntax

val network : string list -> string -> (string * 'a syntax) list -> 'a network
val apply : 'a network -> 'a network list -> 'a network
val prefix : int -> 'a network -> 'a list

end = struct

type 'a syntax = Cst of 'a
             | Fby of string * string
             | Map of ('a -> 'a) * string
             | Map2 of ('a -> 'a -> 'a) * string * string
             | SplitFirst of string
             | SplitRest of string
             | Filter of ('a -> 'a -> bool) * string * string
             | Component of 'a network * string list
             | RecComponent of string list

and 'a primitive = P_Cst of 'a
             | P_Fby of 'a comp * 'a comp
             | P_Map of ('a -> 'a) * 'a comp
             | P_Map2 of ('a -> 'a -> 'a) * 'a comp * 'a comp
             | P_SplitFirst of 'a comp
             | P_SplitRest of 'a comp
             | P_Filter of ('a -> 'a -> bool) * 'a comp * 'a comp
             | P_Wire of string
             | P_Component of 'a network * 'a comp list
             | P_RecComponent of 'a network ref * 'a comp list

and 'a comp = C of ('a stream option ref * 'a primitive * 'a network ref)
            | NC of ('a primitive * 'a network ref)  (* non-cachable *)

and 'a network = Comp of ((string * 'a comp) list * string list * string)

and 'a stream = Stream of ('a * 'a comp)

let hd (Stream (h,t)) = h
let tl (Stream (h,t)) = t

let lookup (Comp (comp,_,_)) n =
  let rec loop comp =
    match comp with
    | [] -> failwith ("cannot find "^n)
    | (n',s)::comp' when n=n' -> s
    | _::comp' -> loop comp'  in
  loop comp

(* reach into a component and reset every environment to the provided one *)
let rec smash prim renv =
match prim with
             | P_Fby (s1,s2) -> P_Fby (smash_comp s1 renv, smash_comp s2 renv)
| P_Map (f,s1) -> P_Map (f,smash_comp s1 renv)
             | P_Map2 (f,s1,s2) -> P_Map2(f,smash_comp s1 renv, smash_comp s2 renv)
| P_SplitFirst (s1) -> P_SplitFirst (smash_comp s1 renv)
             | P_SplitRest (s1) -> P_SplitRest (smash_comp s1 renv)
| P_Filter (p,s1,s2) -> P_Filter (p,smash_comp s1 renv, smash_comp s2 renv)
| x -> x

and smash_comp = function (NC (c,_)) -> (fun renv -> NC (smash c renv,renv)) | (C (r,c,_)) -> (fun renv -> C (r,smash c renv,renv))

let rec eval comp primitive =
match primitive with

| P_Wire n -> eval_comp (lookup comp n)
| P_Cst k -> Stream(k, C (ref None, P_Cst k, ref comp))
| P_Fby (a,b) -> let a' = eval_comp a in
               Stream(hd(a'), b)
| P_Map (f,a) -> let a' = eval_comp a in
               Stream(f(hd(a')), C (ref None, P_Map (f,tl(a')),ref comp))
| P_Map2 (f,a,b) -> let a' = eval_comp a in let b' = eval_comp b in
Stream(f(hd(a')) (hd (b')), C (ref None, P_Map2 (f,tl(a'),tl(b')),ref comp))
| P_SplitFirst (a) -> let a' = eval_comp a in eval comp (P_Cst (hd a'))
| P_SplitRest (a) -> let a' = eval_comp a in eval_comp (tl(a'))
| P_Filter (p,a,b) -> let a' = eval_comp a in let b' = eval_comp b in
                    if p (hd a') (hd b') then Stream(hd b', C (ref None, P_Filter(p,tl(a'),tl(b')),ref comp))
                   else eval comp (P_Filter(p,tl(a'),tl(b')))
| P_Component (comp',input_comps) -> let Comp(c,inps,out) = comp' in 
let newW = List.map2 (fun x -> function (NC (y,_)) -> (x,C (ref None, y, ref comp)) | (C _) -> failwith "cannot be cached") inps input_comps in
                               let me = ref (Comp ([],[],"")) in
let newC = Comp(newW @ (List.map (fun (n,x) -> (n, smash_comp x me)) c), inps, out)  in
                               let _ = (me := newC) in
                                 eval newC (P_Wire out)
| P_RecComponent (rcomp',input_comps) -> let Comp(c,inps,out) = !rcomp' in 
let newW = List.map2 (fun x -> function (NC (y,_)) -> (x,C (ref None, y, ref comp)) | (C _) -> failwith "cannot be cached") inps input_comps in
                               let me = ref (Comp ([],[],"")) in
let newC = Comp(newW @ (List.map (fun (n,x) -> (n, smash_comp x me)) c), inps, out)  in
                               let _ = (me := newC) in
                                 eval newC (P_Wire out)

and eval_comp comp = 
match comp with
| C (memoref, prim, comp) -> 
(match !memoref with
| Some v -> v
| None -> let v = eval (!comp) prim  in
 let _ = (memoref := Some v) in
 v)
| NC (prim, comp) -> eval (!comp) prim


let prefix n comp =
 let Comp (_,_,output) = comp in
  let rec loop n c = 
if n = 0 then []
else
let str = eval_comp c in
(* let _ = begin print_stream str ; print_newline () end in *)

(hd str)::(loop (n-1) (tl str))  in
loop n (lookup comp output)


let apply comp comps =
let Comp (c,inps,out) = comp  in
let all = List.map2 (fun x y -> (x, NC (P_Component(y,[]),ref (Comp([],[],""))))) inps comps in
let me = ref (Comp([],[],""))  in
let result = Comp (all@( List.map (fun (n,x) -> (n,smash_comp x me)) c),[],out) in
let _ = (me := result) in
result


let network inputs output lst =
   let me = ref (Comp ([],[],"")) in
let rec convert s renv =
match s with
| Cst i -> NC (P_Cst (i),renv)
             | Fby (w1,w2) -> NC (P_Fby (NC (P_Wire w1, renv), NC (P_Wire w2, renv)),renv)
             | Map (f,w1) -> NC (P_Map (f,NC (P_Wire w1, renv)),renv)
             | Map2 (f,w1,w2) -> NC (P_Map2 (f,NC (P_Wire w1, renv), NC (P_Wire w2, renv)),renv)
             | SplitFirst (w1) -> NC (P_SplitFirst (NC (P_Wire w1, renv)),renv)
             | SplitRest (w1) -> NC (P_SplitRest (NC (P_Wire w1, renv)), renv)
             | Filter (p,w1,w2) -> NC (P_Filter (p,NC (P_Wire w1, renv), NC (P_Wire w2, renv)),renv)
             | Component (c,cl) -> NC (P_Component(c,List.map (fun (w) -> NC(P_Wire w,renv)) cl),renv)
             | RecComponent (cl) -> NC (P_RecComponent(me,List.map (fun (w) -> NC(P_Wire w, renv)) cl),renv)  in
   let env = List.map (fun (n,p) -> (n,convert p me)) lst in
   let _ = (me := Comp (env,inputs,output))  in
   Comp (env,inputs,output)


let cst (x:'a):'a syntax = Cst x
let fby (s:string) (t:string):'a syntax = Fby (s,t)
let map (f:'a -> 'a) (s:string):'a syntax = Map (f,s)
let map2 (f:'a -> 'a -> 'a) (s:string) (t:string):'a syntax = Map2 (f,s,t)
let splitFirst (s:string):'a syntax = SplitFirst (s)
let splitRest (s:string):'a syntax = SplitRest (s)
let filter (p:'a -> 'a -> bool) (s:string) (t:string) = Filter (p,s,t)
let component (c:'a network) (args:string list):'a syntax = Component (c,args)
let rcomponent (args:string list):'a syntax = RecComponent (args)

end


(*
 * 
 * THESE ARE THE FUNCTIONS YOU GET TO USE
 *
 *)

type 'a syntax = 'a S.syntax
type 'a network = 'a S.network

(* primitives *)
let cst : 'a -> 'a syntax = S.cst
let fby : string -> string -> 'a syntax = S.fby
let map : ('a -> 'a) -> string -> 'a syntax = S.map
let map2 : ('a -> 'a -> 'a) -> string -> string -> 'a syntax = S.map2
let splitFirst : string -> 'a syntax = S.splitFirst
let splitRest : string -> 'a syntax = S.splitRest
let filter : ('a -> 'a -> bool) -> string -> string -> 'a syntax = S.filter
let component : 'a network -> string list -> 'a syntax = S.component
let rcomponent : string list -> 'a syntax = S.rcomponent

(* helper *)
let network : string list -> string -> (string * 'a syntax) list -> 'a network = S.network
let apply : 'a network -> 'a network list -> 'a network = S.apply
let prefix : int -> 'a network -> 'a list = S.prefix




(* 
 *
 * Some sample dataflow networks
 *
 * Mostly from class
 *
 *)


let nats = network [] "result" [
  ("input", cst 0);
  ("result", fby "input" "add1");
  ("add1", map (fun x->x+1) "result")
]

let evens = network [] "result" [
  ("input", cst 0);
  ("result", fby "input" "add2");
  ("add2", map (fun x->x+2) "result")
]

let odds = network [] "result" [
  ("input", component evens []);
  ("result", map (fun x -> x+1) "input")
]

let add = network ["input1";"input2"] "result" [
 ("result", map2 (fun x y -> x+y) "input1" "input2")
]

let squares = network [] "result" [
 ("result", component add ["odds"; "fby"]);
 ("odds", component odds []);
 ("fby", fby "zero" "result");
 ("zero", cst 0)
]

let fib = network [] "result" [
  ("result", fby "zero" "rest");
  ("zero", cst 0);
  ("rest", fby "one" "feedback");
  ("one", cst 1);
  ("feedback", component add ["result";"drop"]);
  ("drop", splitRest "result")
]

let psums = network ["input"] "result" [
  ("feedback", component add ["drop";"result"]);
  ("drop", splitRest "input");
  ("result", fby "input" "feedback")
]

let sieve = 
  let notdivides c x = x mod c <> 0  in
  network ["input"] "result" [
    ("top", splitFirst "input");
    ("bot", splitRest "input");
    ("result", fby "top" "rec_sieve");
    ("rec_sieve", rcomponent ["filtered"]);
    ("filtered", filter notdivides "top" "bot");
  ]

let primes = network [] "result" [
  ("result", component sieve ["map"]);
  ("map", map (fun x -> x+2) "nats");
  ("nats", component nats [])
]
                                                        

(*
 *
 * An interesting dataflow network
 * for testing other networks
 *
 * Produces a streams 0 1 2 ... n-1 0 1 2 ... n-1 0 1 2 ...
 *
 *)

let stairs n = network [] "result" [
  ("input", cst 0);
  ("result", fby "input" "add");
  ("add", map (fun x -> if x  = n-1 then 0 else x+1) "result")
]


let dummyIntNetwork : int network = network [] "result" []
let dummyFloatNetwork : float network = network [] "result" []



(* 
 * QUESTION 1 
 * 
 * As usual, replace the placeholder answers by your own implementation
 *
 *)


let scale (n : int): int network = dummyIntNetwork


let mult : int network = dummyIntNetwork


let fold (f : 'a -> 'a -> 'a): 'a network  = dummyIntNetwork


let running_max : int network = dummyIntNetwork


let stutter : 'a network = dummyIntNetwork



(*
 * QUESTION 2
 * 
 * As usual, replace the placeholder answers by your own implementation
 *
 *)


let arctan (z:float): float network = dummyFloatNetwork


let pi : float network = dummyFloatNetwork


let newton (f : float -> float) (df : float -> float) (guess: float): float network = dummyFloatNetwork


let derivative (f:float -> float) (x:float): float network = dummyFloatNetwork


let limit (epsilon:float): float network = dummyFloatNetwork
