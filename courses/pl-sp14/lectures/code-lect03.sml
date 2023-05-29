
(* Code from Lecture 03 - Identifiers and Bindings *)


datatype value = VInt of int
               | VVec of int list
               | VBool of bool

datatype expr = EInt of int
	      | EVec of int list
              | EAdd of expr * expr
              | ESub of expr * expr
              | EMul of expr * expr
              | ENeg of expr
              | EBool of bool
              | EAnd of expr * expr
              | EIf of expr * expr * expr
              | ELet of string * expr * expr
              | EIdent of string

fun scaleVec a [] = []
  | scaleVec a (x::xs) = (a*x)::(scaleVec a xs)

fun addVec [] [] = []
  | addVec (x::xs) (y::ys) = (x+y)::(addVec xs ys)
  | addVec _ _ = []

fun inner [] [] = 0
  | inner (x::xs) (y::ys) = (x*y)+(inner xs ys)
  | inner _ _ = 0

exception TypeError of string
exception EvalError of string

fun applyAdd (VInt i) (VInt j) = VInt (i+j)
  | applyAdd (VVec v) (VVec w) = if length v = length w
                                   then VVec (addVec v w)
                                 else raise TypeError "applyAdd"
  | applyAdd _ _ = raise TypeError "applyAdd"

fun applyMul (VInt i) (VInt j) = VInt (i*j)
  | applyMul (VInt i) (VVec v) = VVec (scaleVec i v)
  | applyMul (VVec v) (VInt i) = VVec (scaleVec i v)
  | applyMul (VVec v) (VVec w) = if length v = length w
                                   then VInt (inner v w)
                                 else raise TypeError "applyMul"
  | applyMul _ _ = raise TypeError "applyMul"

fun applyNeg (VInt i) = VInt (~ i)
  | applyNeg (VVec v) = VVec (scaleVec ~1 v)
  | applyNeg _ = raise TypeError "applyNeg"

fun applyAnd (VBool b) (VBool c) = VBool (b andalso c)
  | applyAnd _ _ = raise TypeError "applyAnd"

fun applySub a b = applyAdd a (applyNeg b)

fun subst (EInt i) id e = EInt i
  | subst (EVec v) id e = EVec v
  | subst (EAdd (f,g)) id e = EAdd (subst f id e, subst g id e)
  | subst (ESub (f,g)) id e = ESub (subst f id e, subst g id e)
  | subst (EMul (f,g)) id e = EMul (subst f id e, subst g id e)
  | subst (ENeg f) id e = ENeg (subst f id e)
  | subst (EBool b) id e = EBool b
  | subst (EAnd (f,g)) id e = EAnd (subst f id e, subst g id e)
  | subst (EIf (f,g,h)) id e = EIf (subst f id e, 
                                    subst g id e,
                                    subst h id e)
  | subst (ELet (id',f,g)) id e = 
      if id = id'
      then ELet (id',subst f id e, g)
      else ELet (id',subst f id e, subst g id e)
  | subst (EIdent id') id e = if id = id'
                                then e
                              else EIdent id'

fun eval (EInt i) = VInt i
  | eval (EVec v) = VVec v
  | eval (EAdd (e,f)) = applyAdd (eval e) (eval f)
  | eval (ESub (e,f)) = applySub (eval e) (eval f)
  | eval (EMul (e,f)) = applyMul (eval e) (eval f)
  | eval (ENeg e) = applyNeg (eval e)
  | eval (EBool b) = VBool b
  | eval (EAnd (e,f)) = evalAnd (eval e) f
  | eval (EIf (e,f,g)) = evalIf (eval e) f g
  | eval (ELet (name,e,f)) = evalLet name e f
  | eval (EIdent _) = raise EvalError "eval/EId"

and evalIf (VBool true) f g = eval f
  | evalIf (VBool false) f g = eval g
  | evalIf _ _ _ = raise TypeError "evalIf"

and evalAnd (VBool true) f = eval f
  | evalAnd (VBool false) f = VBool false
  | evalAnd _ _ = raise TypeError "evalAnd"

and evalLet id exp body = eval (subst body id exp)
                        

(* Some sample expressions presented in class to evaluate *)

val sample1 = EIf (EAnd (EBool false, EBool true),
                   EAdd (EInt 1, EInt 2),
                   EAdd (EInt 1, EInt 3))

val sample2 = EAdd (EInt 1, 
                    EIf (EAnd (EBool false, EBool true),
                         EInt 2, EInt 3))

val sample3 = ELet ("x", EAdd (EInt 10, EInt 10),
                         EMul (EIdent "x", EIdent "x"))

val sample4 = ELet ("x", EInt 10,
                    ELet ("y", EIdent "x",
                          ELet ("x", EInt 30,
                                EMul (EIdent "x", EIdent "y"))))
