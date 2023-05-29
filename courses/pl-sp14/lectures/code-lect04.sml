
(* Code from Lecture 04 - Functions *)

(* Final version, with primitive operations pushed into the 
   function environment *)



(* Internal representation *)

datatype value = VInt of int
               | VVec of int list
               | VBool of bool

datatype expr = EVal of value
              | EAnd of expr * expr
              | EIf of expr * expr * expr
              | ELet of string * expr * expr
              | EIdent of string
              | ECall of string * expr list

datatype function = FDef of string list * expr     (* (params,body) *)
		  | FPrim of value list -> value



(* Helper functions for vector operations *)

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


(* Primitive operations *)

fun applyAdd [VInt i, VInt j] = VInt (i+j)
  | applyAdd [VVec v, VVec w] = if length v = length w
                                   then VVec (addVec v w)
                                 else raise TypeError "applyAdd"
  | applyAdd _ = raise TypeError "applyAdd"

fun applyMul [VInt i, VInt j] = VInt (i*j)
  | applyMul [VInt i, VVec v] = VVec (scaleVec i v)
  | applyMul [VVec v, VInt i] = VVec (scaleVec i v)
  | applyMul [VVec v, VVec w] = if length v = length w
                                   then VInt (inner v w)
                                 else raise TypeError "applyMul"
  | applyMul _ = raise TypeError "applyMul"

fun applyNeg [VInt i] = VInt (~ i)
  | applyNeg [VVec v] = VVec (scaleVec ~1 v)
  | applyNeg _ = raise TypeError "applyNeg"

fun equalLists [] [] = true
  | equalLists (x::xs) (y::ys) = (x = y) andalso (equalLists xs ys)
  | equalLists _ _ = false

fun applyEq [VInt i, VInt j] = VBool (i = j)
  | applyEq [VVec v, VVec w] = VBool (equalLists v w)
  | applyEq [VBool b, VBool c] = VBool (b = c)
  | applyEq _ = raise TypeError "applyEq"

fun applyAnd [VBool b, VBool c] = VBool (b andalso c)
  | applyAnd _ = raise TypeError "applyAnd"

fun applySub [a, b] = applyAdd [a, applyNeg [b]]
  | applySub _ = raise TypeError "applySub"


(* Substitution functions *)

fun subst (EVal value) id v = EVal value
  | subst (EAnd (f,g)) id v = EAnd (subst f id v, subst g id v)
  | subst (EIf (f,g,h)) id v = EIf (subst f id v, 
                                    subst g id v,
                                    subst h id v)
  | subst (ELet (id',f,g)) id v = 
      if id = id'
      then ELet (id',subst f id v, g)
      else ELet (id',subst f id v, subst g id v)
  | subst (EIdent id') id v = if id = id'
                                then EVal v
                              else EIdent id'
  | subst (ECall (n,fs)) id v = ECall (n,substList fs id v)

and substList [] id v = []
  | substList (e::es) id v = (subst e id v)::(substList es id v)

fun substAll e [] [] = e
  | substAll e (id::ids) (v::vs) = substAll (subst e id v) ids vs
  | substAll _ _ _ = raise EvalError "substAll"



(* Lookup a function name in the function environment *)

fun lookup (name:string) [] = raise EvalError "lookup"
  | lookup name ((n,f)::fenv) = 
      if (n = name)
        then f
      else lookup name fenv 


(* Evaluation functions *)

fun eval _ (EVal v) = v
  | eval fenv (EAnd (e,f)) = evalAnd fenv (eval fenv e) f
  | eval fenv (EIf (e,f,g)) = evalIf fenv (eval fenv e) f g
  | eval fenv (ELet (name,e,f)) = evalLet fenv name e f
  | eval _ (EIdent _) = raise EvalError "eval/EId"
  | eval fenv (ECall (name,es)) = 
                evalCall fenv (lookup name fenv) (evalList fenv es)

and evalList fenv [] = []
  | evalList fenv (e::es) = (eval fenv e)::(evalList fenv es)

and evalCall fenv (FDef (params,body)) vs = 
               eval fenv (substAll body params vs)
  | evalCall fenv (FPrim f) vs = f vs

and evalIf fenv (VBool true) f g = eval fenv f
  | evalIf fenv (VBool false) f g = eval fenv g
  | evalIf _ _ _ _ = raise TypeError "evalIf"

and evalAnd fenv (VBool true) f = eval fenv f
  | evalAnd _ (VBool false) f = VBool false
  | evalAnd _ _ _ = raise TypeError "evalAnd"

and evalLet fenv id exp body = eval fenv (subst body id (eval fenv exp))
                        

(* The initial function environment (often called a basis) *)

val basis = [("add", FPrim applyAdd),
	     ("sub", FPrim applySub),
	     ("mul", FPrim applyMul),
	     ("neg", FPrim applyNeg),
	     ("eq", FPrim applyEq)]


(* Some sample expressions *)

val sample1 = EIf (EAnd (EVal (VBool false), EVal (VBool true)),
                   ECall ("add", [EVal (VInt 1), EVal (VInt 2)]),
                   ECall ("add", [EVal (VInt 1), EVal (VInt 3)]))

val sample2 = ECall ("add", [EVal (VInt 1),
			     EIf (EAnd (EVal (VBool false), EVal (VBool true)),
				  EVal (VInt 2), EVal (VInt 3))])

val sample3 = ELet ("x", ECall ("add", [EVal (VInt 10), EVal (VInt 10)]),
                    ECall ("mul", [EIdent "x", EIdent "x"]))

val sample4 = ELet ("x", EVal (VInt 10),
                    ELet ("y", EIdent "x",
                          ELet ("x", EVal (VInt 30),
                                ECall ("mul", [EIdent "x", EIdent "y"]))))

val succ = ("succ", FDef (["n"], ECall ("add", [EIdent "n", EVal (VInt 1)])))

val pred = ("pred", FDef (["n"], ECall ("add", [EIdent "n", EVal (VInt ~1)])))

val fact = ("fact", 
	    FDef (["n"], 
		  EIf (ECall ("eq", [EIdent "n", EVal (VInt 0)]),
    	   	       EVal (VInt 1),
		       ECall ("mul", [EIdent "n", 
				      ECall ("fact", 
					     [ECall ("pred", 
						     [EIdent "n"])])]))))

val exp = ("exp",
	   FDef (["a", "n"],
		 EIf (ECall ("eq", [EIdent "n", EVal (VInt 0)]),
		      EVal (VInt 1),
		      ECall ("mul", [EIdent "a",
				     ECall ("exp", 
					    [EIdent "a",
					     ECall ("pred", 
						    [EIdent "n"])])]))))

