
(* Code from Lecture 05 - Lexical analysis *)



(* Magic to import the regexp library FROM THE SHELL *)

CM.make "$/regexp-lib.cm";




(* Wrappers around regexp library *)

structure R = RegExpFn (structure P = AwkSyntax structure E = DfaEngine)

(* A regular expression needs to be "compiled" before it can be used for
 *   matching. 
 * Function R.compileString takes a string representing the
 *   regular expression and compiles it.
 * Function matchRE takes a string representing a regular expression
 *   and a list of characters and attempts to match a prefix of the 
 *   characters with the regular expression.
 * Function matchRE' does the same, but it takes a compiled regular
 *   expression instead. 
 * Since it is expensive to compile a string representing a regular
 *   expression if it is used often, it makes sense to compile
 *   them in advance sometimes and reuse the result.
 *)

fun matchRE' re cs = let
    val prefix = R.prefix re List.getItem
    fun getMatch NONE = NONE
      | getMatch (SOME (mt, cs')) = let
	    val {pos,len} = MatchTree.root mt
	in
	    SOME (implode (List.take (pos,len)), cs')
	end
in
    getMatch (prefix cs)
end

fun matchRE re cs = matchRE' (R.compileString re) cs



(* A SIMPLE LEXER *)

datatype token = TLET 
	       | TSYM of string 
	       | TINT of int 
	       | TTRUE 
	       | TFALSE
	       | TADD 
	       | TSUB 
	       | TMUL 
	       | TNEG
	       | TIF 
	       | TCALL 
	       | TLPAREN 
	       | TRPAREN

(* List of regular expressions and associated tokens
 *
 * An expression 'fn x => <some expression>' represents an SML
 *   function that takes an 'x' and evaluates to <some expression>
 *
 * It lets you define a function in a data structure without giving it
 *   a name. (Recall that you can treat functions like any other
 *   value: pass it to functions, store it in data structures, etc.)
 *)

val tokens = [("( |\\n|\\t)+", fn _ => NONE),			
	      ("let",     fn _ => SOME (TLET)),		
	      ("true",    fn _ => SOME (TTRUE)),
	      ("false",   fn _ => SOME (TFALSE)),
	      ("call",    fn _ => SOME (TCALL)),
	      ("if",      fn _ => SOME (TIF)),
	      ("add",     fn _ => SOME (TADD)),
	      ("sub",     fn _ => SOME (TSUB)),
	      ("neg",     fn _ => SOME (TNEG)),
	      ("mul",     fn _ => SOME (TMUL)),
	      ("[a-zA-Z][a-zA-Z0-9]*", fn txt => SOME (TSYM (txt))),
	      ("[0-9]+",  fn txt => SOME (TINT (valOf (Int.fromString txt)))),
	      ("\\(",     fn _ => SOME (TLPAREN)),
	      ("\\)",     fn _ => SOME (TRPAREN))]

(* Compile the regular expressions in the above list, producing a new
 *   list. 
 *
 * Here 'map f l' creates a new list with the results of applying
 *   'f' to every element of 'l'.
 *)

val compiledTokens = map (fn (re,f) => (R.compileString re,f)) tokens

fun getToken cs = let
    fun loop [] = raise Fail ("cannot lex "^(implode cs))
      | loop ((re,f)::xs) = (case matchRE' re cs of
				 NONE => loop xs
			       | SOME (m,cs') => (f m,cs'))
in
    loop compiledTokens
end

fun lex []  = []
  | lex cs = let
	val (token,cs') = getToken cs
    in
        case token of 
          NONE => lex cs'
	| SOME t => t::(lex cs')
    end

fun lex' str = lex (explode str)




(* A SIMPLE PARSER *)

datatype value = VInt of int
               | VBool of bool

datatype expr = EVal of value
              | EAdd of expr * expr
              | ESub of expr * expr
              | EMul of expr * expr
              | ENeg of expr
	      | EEq of expr * expr
              | EIf of expr * expr * expr
              | ELet of string * expr * expr
              | EIdent of string
              | ECall of string * expr

fun perror str = raise Fail ("parsing error: "^str)


fun parse_expr [] = perror "unexpected end of token sequence"
  | parse_expr ((TINT i)::ts) = (EVal (VInt i),ts)
  | parse_expr (TTRUE::ts) = (EVal (VBool true),ts)
  | parse_expr (TFALSE::ts) = (EVal (VBool false),ts)
  | parse_expr ((TSYM id)::ts) = (EIdent id, ts)
  | parse_expr (TLPAREN::ts) = let
	val (e,ts') = parse_seq ts
	val ts'' = expect TRPAREN ts'
    in
	(e,ts'')
    end
  | parse_expr _ = perror "unexpected token"


and expect t [] = perror "unexpected end of token sequence"
  | expect t (t'::ts) = if (t=t') then ts 
			else perror "unexpected token"


and parse_seq [] = perror "unexpected end of token sequence"
  | parse_seq (TLET::ts) = let
	val (name,ts') = parse_name ts
	val (e1, ts'') = parse_expr ts'
	val (e2, ts''') = parse_expr ts''
    in
	(ELet (name,e1,e2),ts''')
    end
  | parse_seq (TIF::ts) = let
	val (e1,ts') = parse_expr ts
	val (e2,ts'') = parse_expr ts'
	val (e3,ts''') = parse_expr ts''
    in
	(EIf (e1,e2,e3),ts''')
    end
  | parse_seq (TADD::ts) = let
	val (e1,ts') = parse_expr ts
	val (e2,ts'') = parse_expr ts'
    in
	(EAdd (e1,e2),ts'')
    end
  | parse_seq (TMUL::ts) = let
	val (e1,ts') = parse_expr ts
	val (e2,ts'') = parse_expr ts'
    in
	(EMul (e1,e2),ts'')
    end
  | parse_seq (TSUB::ts) = let
	val (e1,ts') = parse_expr ts
	val (e2,ts'') = parse_expr ts'
    in
	(ESub (e1,e2),ts'')
    end
  | parse_seq (TNEG::ts) = let
	val (e1,ts') = parse_expr ts
    in
	(ENeg e1,ts')
    end
  | parse_seq (TCALL::ts) = let
	val (name,ts') = parse_name ts
	val (e1,ts'') = parse_expr ts'
    in
	(ECall (name,e1),ts'')
    end
  | parse_seq _ = perror "unexpected token"

	
and parse_name [] = perror "unexpected end of token sequence"
  | parse_name ((TSYM name)::ts) = (name,ts)
  | parse_name _ = perror "unexpected token"


(* Top-level function to tokenize and parse a string *)

fun parse str = let
    val (e,_) = parse_expr (lex (explode str))
in
    e
end
