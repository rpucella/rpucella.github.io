(* 

HOMEWORK 10

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
 * Do that in a _fresh_ OCaml shell 
 * It has to load without any errors.
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


(* Printing an integer binary tree *)

let pbt bt =
  let rec loop bt depth = 
    match bt with
    | Empty -> ()
    | Node(n,left,right) ->
	(loop right (depth^"    ");
         print_endline (depth^(string_of_int n));
         loop left (depth^"    ")) in
  loop bt ""





(* Q1 *)

let rec size t = failwith ("size not implemented")


let rec sum t = failwith ("sum not implemented")


let rec height t = failwith ("height not implemented")


let rec fringe t = failwith ("fringe not implemented")


let rec map f t = failwith ("map not implemented")


let rec fold f t b = failwith ("fold not implemented")


let preorder t = failwith ("preorder not implemented")


let postorder t = failwith ("postorder not implemented")


let inorder t = failwith ("inorder not implemented")


let rec bst_insert t x = failwith ("bst_insert not implemented")


let rec bst_lookup t x = failwith ("bst_lookup not implemented")


let rec bstify t =  failwith ("bstify not implemented")


let avl_insert t x = failwith ("avl_insert not implemented")
