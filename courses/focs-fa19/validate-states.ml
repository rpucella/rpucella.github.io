
(*
 * Function to check that all the states reachable from
 * the start state in the transition function of a Turing
 * machine appear in the states list of the Turing machine
 *
 *)

let validateStates (m:string tm):bool =
  let rec addTransitions q syms queue =
    match syms with
    | [] -> queue
    | sym::syms' -> let (p, _, _) = m.delta (q, sym) in
                    addTransitions q syms' (p :: queue) in
  let rec loop seen queue =
    match queue with
    | [] -> true
    | q::qs -> if not (List.mem q m.states)
               then failwith ("Reachable state " ^ q ^ " not defined in states list")
               else if not (List.mem q seen)
               then let _ = print_string ("Following state " ^ q ^ "\n") in
                    loop (q :: seen) (addTransitions q m.tape_alphabet qs)
               else loop seen qs  in
  loop [] [m.start]
