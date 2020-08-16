
let add x y = x + y
let mult x y = x * y

let dup f x = f x x
let swap f x y = f y x

let squares = List.map (dup mult)

let sum = swap (List.fold_right add) 0


(*

    # #use "combinators.ml";;
    val add : int -> int -> int = <fun>
    val mult : int -> int -> int = <fun>
    val dup : ('a -> 'a -> 'b) -> 'a -> 'b = <fun>
    val swap : ('a -> 'b -> 'c) -> 'b -> 'a -> 'c = <fun>
    val squares : int list -> int list = <fun>
    val sum : int list -> int = <fun>
    # sum [1; 2; 3; 4];;
    - : int = 10
    # squares [1; 2; 3; 4; 5];;
    - : int list = [1; 4; 9; 16; 25]

*)