type json =
  | JInteger of int
  | JString of string
  | JList of json list
  | JObject of (string * json) list


let rec dumps (item : json) =
  (* Convert a JSON item to a string *)
  match item with
  | JInteger i -> Printf.sprintf "%d" i
  | JString s -> Printf.sprintf "\"%s\"" s
  | JList items -> let content = String.concat ", " (List.map dumps items) in
                   Printf.sprintf "[%s]" content
  | JObject obj -> let dumps_binding (key, item) = Printf.sprintf "\"%s\": %s" key (dumps item) in
                   let content = String.concat ", " (List.map dumps_binding obj) in
                   Printf.sprintf "{%s}" content


let get (key : string)  (item : json): json option =
  (* Extract an item from a JSON object by key *)
  match item with
  | JObject obj -> (match List.find_opt (fun (k, _) -> k = key) obj with
                    | None -> None
                    | Some (_, j) -> Some j)
  | _ -> None


let rec rget (keys : string list) (item : json): json option =
  (* Extract an item from a nested JSON object by key path *)
  match keys with
  | [] -> Some item
  | k::ks -> (match get k item with
              | None -> None
              | Some new_item -> rget ks new_item)


let sample1 n s = JObject [("a", JInteger n); ("b", JString s)]

let sample2 = JObject [("first", sample1 10 "hello"); ("second", sample1 20 "world")]
    
