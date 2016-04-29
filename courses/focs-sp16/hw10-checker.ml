
(* To use this file:
 * 
 * (1) start a _new_ OCaml shell
 * (2) load your homework submission #use "homework10.ml";;
 * (3) load this file  #use "hw10-checker.ml";;
 * 
 * If there are _any_ errors, go back and fix your homework. 
 *
 * The only errors this will check for are:
 * - function not defined or with syntax or type errors
 * - functions not having the required type
 *
 *)


(* Q1 *)
let a:int = try size (Node(1,Empty,Empty)) with Failure _ -> 0 in let _ = a in
let a:int = try sum (Node(1,Empty,Empty)) with Failure _ -> 0 in let _ = a in
let a:int list = try fringe (Node(1,Empty,Empty)) with Failure _ -> [0] in let _ = a in
let a:int bintree = try map (fun x -> x) (Node(1,Empty,Empty)) with Failure _ -> Node(1,Empty,Empty) in let _ = a in
let a:int = try fold (fun x y z -> z) (Node(1,Empty,Empty)) 0 with Failure _ -> 0 in let _ = a in
let a:int bintree = try fold (fun x y z -> z) (Node(1,Empty,Empty)) (Node(1,Empty,Empty)) with Failure _ -> Node(1,Empty,Empty) in let _ = a in
let a:int list = try preorder (Node(1,Empty,Empty)) with Failure _ -> [0] in let _ = a in
let a:int list = try postorder (Node(1,Empty,Empty)) with Failure _ -> [0] in let _ = a in
let a:int list = try inorder (Node(1,Empty,Empty)) with Failure _ -> [0] in let _ = a in
let a:int bintree = try bst_insert (Node(1,Empty,Empty)) 0 with Failure _ -> Node(1,Empty,Empty) in let _ = a in
let a:bool = try bst_lookup (Node(1,Empty,Empty)) 0 with Failure _ -> true in let _ = a in 
let a:int bintree = try bstify (Node(1,Empty,Empty)) with Failure _ -> Node(1,Empty,Empty) in let _ = a in
(* Q2 *)
let a:int bintree = try avl_insert (Node(1,Empty,Empty)) 0 with Failure _ -> Node(1,Empty,Empty) in let _ = a in
  print_string "Types all OK.\n"

