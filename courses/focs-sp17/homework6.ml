(* 

HOMEWORK 6

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



(*************************************************************
 * Binary trees
 *
 *)
   
type 'a bintree =
  | Empty
  | Node of 'a * 'a bintree * 'a bintree

let sample = Node(10,Node(3,Node(7,Empty,Empty),
                            Node(5,Empty,Empty)),
                     Node(6,Node(99,Empty,
                                 Node(66,Empty,Empty)),
                          Empty))

let pbt bt =
  let rec loop bt depth = 
    match bt with
    | Empty -> ()
    | Node(n,left,right) ->
	(loop right (depth^"    ");
         print_endline (depth^(string_of_int n));
         loop left (depth^"    ")) in
  loop bt ""




(*
 * QUESTION 1
 *
 *)


let rec size (t:'a bintree):int =
  failwith "size not implemnented"

let rec height (t:'a bintree):int = 
  failwith "height not implemnented"


let rec sum (t:int bintree):int = 
  failwith "sum not implemnented"


let rec fringe (t:'a bintree):'a list = 
  failwith "fringe not implemnented"


let rec map (f:'a -> 'b) (t:'a bintree):'b bintree = 
  failwith "map not implemnented"


let rec fold (f:'a -> 'b -> 'b -> 'b) (t:'a bintree) (b:'b):'b = 
  failwith "fold not implemnented"


let rec preorder (t:'a bintree):'a list = 
  failwith "preorder not implemnented"

let rec postorder (t:'a bintree):'a list =
  failwith "postorder not implemnented"

let rec inorder (t:'a bintree):'a list =
  failwith "inorder not implemnented"



(*
 *  QUESTION 2 
 *
 *)


let rec bst_insert (t:'a bintree) (x:'a):'a bintree = 
  failwith "bst_insert not implemnented"


let rec bst_lookup (t:'a bintree) (x:'a):bool =
  failwith "bst_lookup not implemnented"


let rec bstify (t:'a bintree):'a bintree = 
  failwith "bstify not implemnented"


let rotate_left (t:'a bintree):'a bintree = 
  failwith "rotate_left not implemnented"


let rotate_right (t:'a bintree):'a bintree = 
  failwith "rotate_right not implemnented"


let avl_insert (t:'a bintree) (x:'a):'a bintree = 
  failwith "avl_insert not implemnented"



(* 
 * QUESTION 3  (insert below when available)
 *
 *)
