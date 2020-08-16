
(* Function to convert Celcius to Fahrenheit *)

let c_to_f (c) = (c *. 9.0 /. 5.0) +. 32.0


(* Function to sum up integers from 0 to n 
   Uses a helper function 
 
 Corresponds pretty closely to the Python function

 def sumto (n):
    result = 0
    index = 0
    while index <= n:
      result += index
      index += 1
    return result
 *)

let rec sumto_loop (index,n,result) = 
  if index <= n
    then sumto_loop(index+1,n,result+index)
  else result

let sumto (n) = sumto_loop(0,n,0)


(* Alternative and nicer way to write sumto *)

let rec sumto2 (n) = 
  if n = 0 then 0 else sumto2(n-1) + n


(* Sum the elements of a pair *)

let sum_pair (p) = match p with (a,b) -> a + b


(* Add two pairs
   note parentheses around the second match for
   clarity *)

let add_pairs (p1,p2) = 
  match p1 with (a,b) -> 
    (match p2 with (c,d) -> (a+c,b+d))


(* Length of a list *)

let rec length (xs) = 
  match xs with
    [] -> 0
  | y::ys -> length(ys) + 1


(* Sum elements of a list *)

let rec sum (xs) = 
  match xs with
    [] -> 0
  | y::ys -> y+sum(ys)

