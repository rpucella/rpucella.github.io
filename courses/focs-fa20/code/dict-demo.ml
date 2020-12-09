(* DEMO code for data abstraction *)


(* ************************************************************ *)

(* Defines two modules, DictList and DictFun,, implementing dictionaries

   A dictionary is a map from keys (strings) to values (int)

   Both modules are given the same signature DICT

   Module Dict is an abbreviation for one of the dictionary implementation module

   *)     


module type DICT = sig

  type dict

  val empty : dict
  val add : dict -> string -> int -> dict
  val lookup : dict -> string -> int
  val keys : dict -> string list

end


module DictList : DICT = struct

  (* representation - a dictionary is a list of pairs of a key and a value *)

  type dict = (string * int) list

  let empty = []

  let add (d: dict) (k: string) (v: int): dict =
    (k, v) :: d
  
  let rec lookup (d: dict) (k: string): int =
    match d with
    | [] -> failwith "Key not found"
    | (key, value)::d' -> if k = key then value else lookup d' k

  let rec keys (d: dict): string list =
    match d with
    | [] -> []
    | (key, value)::d' -> let res = keys d' in
                          if List.mem key res then res else key :: res

end



module DictFun : DICT = struct

  (* representation - a dictionary is a pair of a list of keys
                      and a function taking a key and returning its
                      corresponding value *)

  type dict = (string list) * (string -> int)

  let empty = ([], fun k -> failwith "Key not found")

  let add (d: dict) (k: string) (v: int): dict =
    let (keys, fn) = d in 
    let new_keys = if List.mem k keys then keys else k :: keys in
    let new_fn = (fun key -> if (key = k) then v else fn key) in
    (new_keys, new_fn)

  let lookup (d: dict) (k: string): int =
    let (keys, fn) = d in
    fn k

  let keys (d: dict): string list =
    let (keys, fn) = d in
    keys

end


module Dict = DictFun



(* ************************************************************ *)

(* client code  - works with any implementation with signature DICT *)

let test_dict =
  (* a -> 10, b -> 20, c -> 30 *)
  let d1 = Dict.empty in
  let d2 = Dict.add d1 "a" 10 in
  let d3 = Dict.add d2 "b" 20 in
  let d4 = Dict.add d3 "c" 30 in
  d4

let test_lookup () =
  Dict.lookup test_dict "b"

let test_keys () =
  Dict.keys test_dict


(* this code does not type check even with DictList because the representation
   as a list is hidden *)

(*
let test_first () =
  match test_dict with
  | [] -> failwith "empty dictionary"
  | (k, v)::_ -> (k, v)
  
*)

