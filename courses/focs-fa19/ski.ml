(* Add this code at the end of your homework7.ml file to try out transforming terms to their SKI form *)

let convert_ski term =
  let open Lambda in 
  let term = Lambda.parse term  in
  let rec has_lam t =
    match t with
    | LIdent _ -> false
    | LLam _ -> true
    | LApp (t', t'') -> has_lam t' || has_lam t''  in
  let rec transform1 t =
    match t with
    | LIdent s -> LIdent s
    | LApp (t', t'') -> LApp (transform1 t', transform1 t'')
    | LLam (s, LIdent(s')) when s = s' -> LIdent("I")
    | LLam (s, t') when not (List.mem s (fv t')) -> LApp(LIdent("K"), t')
    | LLam (s, LLam(s', t')) -> LLam (s, transform1 (LLam (s', t')))
    | LLam (s, LApp(t', t'')) -> LApp(LApp(LIdent("S"),
                                           LLam(s, t')),
                                      LLam(s, t''))
    | t -> t  in
  let rec transform t = 
    if has_lam t
      then transform (transform1 t)
    else t       in     
  pp (transform term)


(* Example:

# let four = convert_ski "<f -> <x -> f(f(f(f x)))>>";;
val four : string = "S (S (K S) (S (K K) I)) (S (S (K S) (S (K K) I)) (S (S (K S) (S (K K) I)) (S (S (K S) (S (K K) I)) (K I))))"
# let defs = [("S", "<x -> <y -> <z -> x z (y z)>>>"); ("K", "<x -> <y -> x>>"); ("I", "<x -> x>")];;
val defs : (string * string) list = [("S", "<x -> <y -> <z -> x z (y z)>>>"); ("K", "<x -> <y -> x>>"); ("I", "<x -> x>")]
# simplify defs four;;
- : string = "<z -> <z_4 -> z (z (z (z z_4)))>>"
  
Note that this won't take definitions into account

*)
