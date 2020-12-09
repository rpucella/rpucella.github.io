
-- use take n l   to take the first n elements of list l

cst k = k : (cst k)

fby s1 s2 = (head s1) : s2

map2 f (h1 : t1) (h2 : t2) = (f h1 h2) : (map2 f t1 t2)
map2 f _ _ = []



-- Examples from lecture

plus1 s = map (\a -> a + 1) s

nats = fby (cst 0) (plus1 nats)

evens = fby (cst 0) (plus1 (plus1 evens))

odds = plus1 evens

plus s1 s2 = map2 (\a -> \b -> a + b) s1 s2

triangs = fby (cst 0) (plus triangs (plus1 nats))

fibs = fby (cst 0) (fby (cst 1) (plus fibs (tail fibs)))

psums s = fby s (plus (tail s) (psums s))



-- Sieve of Eratosthenes

divides c x = mod x c == 0

sieve s = fby s (sieve (filter (\a -> not (divides (head s) a)) (tail s)))

