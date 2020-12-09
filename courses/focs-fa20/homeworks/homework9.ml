(* 

HOMEWORK 9

Due: Fri, Dec 18, 2020 (23h59)

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
 * PLEASE DO NOT CHANGE THE TYPES IN THE STUBS BELOW.
 * Doing so risks making it impossible for me to test your code.
 *
 * Always make sure you can #use this file in a FRESH OCaml shell
 * before submitting it. It has to load without any errors.
 *
 *)



(*************************************************************
 * Binary trees
 *
 *)
   
type 'a bintree =
  | Empty
  | Node of 'a * 'a bintree * 'a bintree


let sample = Node(10, Node(3, Node(7, Empty, Empty), 
                            Node(5, Empty, Empty)), 
                     Node(6, Node(99, Empty, 
                                 Node(66, Empty, Empty)), 
                          Empty))

(* Function to print a binary tree *)
           
let pbt bt =
  let rec loop bt cdepth ldepth rdepth = 
    match bt with
    | Empty -> ()
    | Node(n, left, right) ->
	(loop right (rdepth^"+--- ") (rdepth^"|    ") (rdepth^"     ");
         print_endline (cdepth^(string_of_int n));
         loop left (ldepth^"+--- ") (ldepth^"     ") (ldepth^"|    ")) in
  loop bt "" "" ""


  
(*
 * QUESTION 1
 *
 *)


let rec size (t:'a bintree):int =
  failwith "Not implemented"


let rec height (t:'a bintree):int = 
  failwith "Not implemented"


let rec sum (t:int bintree):int = 
  failwith "Not implemented"


let rec leaves (t:'a bintree):'a list = 
  failwith "Not implemented"


let rec map (f:'a -> 'b) (t:'a bintree):'b bintree = 
  failwith "Not implemented"


let rec fold (f:'a -> 'b -> 'b -> 'b) (t:'a bintree) (b:'b):'b = 
  failwith "Not implemented"


let rec preorder (t:'a bintree):'a list = 
  failwith "Not implemented"


let rec postorder (t:'a bintree):'a list =
  failwith "Not implemented"


let rec inorder (t:'a bintree):'a list =
  failwith "Not implemented"

  
let rec is_bst (t: 'a bintree) : bool =
  failwith "Not implemented"


  
(* 
 * QUESTION 2
 *
 *)

type exp = 
 (* A *)
 | Num of int
 | Ident of string
 | Plus of exp * exp
 | Times of exp * exp
 (* B *)
 | EQ of exp * exp
 | GT of exp * exp
 | And of exp * exp
 | Not of exp
 | If of exp * exp * exp
 (* C *)
 | Letval of string * exp * exp
 (* D *)
 | Letfun of string * string * exp * exp
 | App of string * exp

type 'a env = string -> 'a

let update (env:'a env) (x:string) (v:'a):'a env = 
  fun y -> if (y = x) then v else (env y)

         
let rec eval (exp:exp) (env:int env):int = 
  failwith "Not implemented"


let rec evalF (exp:exp) (env:int env) (fenv:(int -> int) env):int = 
  failwith "Not implemented"


(* some examples *)
  
let init:int env = (fun x -> 0)
let initF:(int -> int) env = (fun x -> failwith ("unknown function "^x))

let test1 = Plus(Times(Num(3), Ident("x")), Num(1))
let test2 = If(GT(Ident("x"), Num(10)), Plus(Ident("x"), Num(1)), Times(Ident("x"), Num(2)))
let test3 = Letval("x", Times(Ident("x"), Ident("x")), Plus(Ident("x"), Num(1)))

let test4 = Letfun("square", "x", Times(Ident("x"), Ident("x")), App("square", Num(10)))
let test5 = Letfun("square", "x", Times(Ident("x"), Ident("x")), 
                   Letfun("double_square", "y", Times(Num(2), App("square", Ident("y"))), 
                          Plus(App("double_square", Num(42)), 
                               Num(1))))
              
