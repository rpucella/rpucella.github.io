
// Booleans.

const _true = x => y => x
const _false = x => y => y
const _not = b => b(_false)(_true)
const _and = b => c => b(c)(_false)

function evalB(b) {
    return b(true)(false)
}


// Natural numbers.

const _0 = f => x => x
const _1 = f => x => f(x)
const _2 = f => x => f(f(x))
const _3 = f => x => f(f(f(x)))
const _4 = f => x => f(f(f(f(x))))
const _succ = n => f => x => n(f)(f(x))
const _plus = n => m => f => x => n(f)(m(f)(x))
const _times = n => m => f => x => n(m(f))(x)
const _pred = n => f => x => n(g => (h => h(g(f))))(u => x)(u => u)
const _minus = n => m => m(_pred)(n)

const _iszero = n => n(x => _false)(_true)
const _gt = n => m => _not(_iszero(_minus(n)(m)))
const _ge = n => m => _gt(_succ(n))(m)
const _eq = n => m => _and(_ge(n)(m))(_ge(m)(n))

function evalN(n) {
    return n(a => a+1)(0)
}


// Pairs.

const _pair = x => y => s => s(x)(y)
const _fst = p => p(x => y => x)
const _snd = p => p(x => y => y)

function evalPN(p) {
    const t = p(x => y => [x, y])
    return [evalN(t[0]), evalN(t[1])]
}


// Integers.

const _int = n => _pair(n)(_0)
const _neg_int = i => _pair(_snd(i))(_fst(i))
const _plus_int = i => j => _pair(_plus(_fst(i))(_fst(j)))(_plus(_snd(i))(_snd(j)))
const _minus_int = i => j => _plus_int(i)(_neg_int(j))
const _times_int = i => j => _pair(_plus(_times(_fst(i))(_fst(j)))(_times(_snd(i))(_snd(j))))(_plus(_times(_fst(j))(_snd(i)))(_times(_fst(i))(_snd(j))))
const _eq_int = i => j => _eq(_plus(_fst(i))(_snd(j)))(_plus(_snd(i))(_fst(j)))

function evalI(i) {
    const t = i(x => y => [x, y])
    return evalN(t[0]) - evalN(t[1])
}


// Some tests.

console.log("_true =", evalB(_true))
console.log("_false =", evalB(_false))
console.log("_not _true =", evalB(_not(_true)))
console.log("_and _true _true =", evalB(_and(_true)(_true)))
console.log("_and _true _false =", evalB(_and(_true)(_false)))

console.log("_0 =", evalN(_0))
console.log("_succ _0 =", evalN(_succ(_0)))
console.log("_succ _4 =", evalN(_succ(_4)))
console.log("_succ (_succ _4) =", evalN(_succ(_succ(_4))))
console.log("_plus _0 _3 =", evalN(_plus(_0)(_3)))
console.log("_plus _3 _4 =", evalN(_plus(_3)(_4)))
console.log("_times _0 _3 =", evalN(_times(_0)(_3)))
console.log("_times _3 _4 =", evalN(_times(_3)(_4)))
console.log("_pred _0 =", evalN(_pred(_0)))
console.log("_pred _3 =", evalN(_pred(_3)))
console.log("_pred (_succ _3) =", evalN(_pred(_succ(_3))))
console.log("_minus _3 _0 =", evalN(_minus(_3)(_0)))
console.log("_minus _3 _2 =", evalN(_minus(_3)(_2)))
console.log("_minus _2 _3 =", evalN(_minus(_2)(_3)))
console.log("_iszero _0 =", evalB(_iszero(_0)))
console.log("_iszero _3 =", evalB(_iszero(_3)))
console.log("_iszero (_pred _1) =", evalB(_iszero(_pred(_1))))
console.log("_gt _3 _2 =", evalB(_gt(_3)(_2)))
console.log("_gt _2 _3 =", evalB(_gt(_2)(_3)))
console.log("_gt _3 _3 =", evalB(_gt(_3)(_3)))
console.log("_ge _3 _2 =", evalB(_ge(_3)(_2)))
console.log("_ge _2 _3 =", evalB(_ge(_2)(_3)))
console.log("_ge _3 _3 =", evalB(_ge(_3)(_3)))
console.log("_eq _3 _2 =", evalB(_eq(_3)(_2)))
console.log("_eq _2 _3 =", evalB(_eq(_2)(_3)))
console.log("_eq _3 _3 =", evalB(_eq(_3)(_3)))
console.log("_pair _2 _3 =", evalPN(_pair(_2)(_3)))
console.log("_fst (_pair _2 _3) =", evalN(_fst(_pair(_2)(_3))))
console.log("_snd (_pair _2 _3) =", evalN(_snd(_pair(_2)(_3))))
console.log("_pair (_pred _2) (_succ _3) =", evalPN(_pair(_pred(_2))(_succ(_3))))
console.log("_int _0 =", evalI(_int(_0)))
console.log("_int _2 =", evalI(_int(_2)))
console.log("_neg_int (_int _0) =", evalI(_neg_int(_int(_0))))
console.log("_neg_int (_int _2) =", evalI(_neg_int(_int(_2))))
console.log("_neg_int (_neg_int (_int _2)) =", evalI(_neg_int(_neg_int(_int(_2)))))
console.log("_neg_int (_neg_int (neg_int (_int _2))) =", evalI(_neg_int(_neg_int(_neg_int(_int(_2))))))
console.log("_plus_int (_int _2) (_int _3) =", evalI(_plus_int(_int(_2))(_int(_3))))
console.log("_plus_int (_neg_int (_int _2)) (_int _3) =", evalI(_plus_int(_neg_int(_int(_2)))(_int(_3))))
console.log("_plus_int (_int _2) (_neg_int (_int _3)) =", evalI(_plus_int(_int(_2))(_neg_int(_int(_3)))))
console.log("_plus_int (_neg_int (_int _2)) (_neg_int (_int _3)) =", evalI(_plus_int(_neg_int(_int(_2)))(_neg_int(_int(_3)))))
console.log("_minus_int (_int _2) (_int _3) =", evalI(_minus_int(_int(_2))(_int(_3))))
console.log("_minus_int (_neg_int (_int _2)) (_int _3) =", evalI(_minus_int(_neg_int(_int(_2)))(_int(_3))))
console.log("_minus_int (_int _2) (_neg_int (_int _3)) =", evalI(_minus_int(_int(_2))(_neg_int(_int(_3)))))
console.log("_minus_int (_neg_int (_int _2)) (_neg_int (_int _3)) =", evalI(_minus_int(_neg_int(_int(_2)))(_neg_int(_int(_3)))))
console.log("_times_int (_int _2) (_int _3) =", evalI(_times_int(_int(_2))(_int(_3))))
console.log("_times_int (_neg_int (_int _2)) (_int _3) =", evalI(_times_int(_neg_int(_int(_2)))(_int(_3))))
console.log("_times_int (_int _2) (_neg_int (_int _3)) =", evalI(_times_int(_int(_2))(_neg_int(_int(_3)))))
console.log("_times_int (_neg_int (_int _2)) (_neg_int (_int _3)) =", evalI(_times_int(_neg_int(_int(_2)))(_neg_int(_int(_3)))))
console.log("_eq_int (_int 2) (_int 3) =", evalB(_eq_int(_int(_2))(_int(_3))))
console.log("_eq_int (_neg_int (_int 2)) (_int 3) =", evalB(_eq_int(_neg_int(_int(_2)))(_int(_3))))
console.log("_eq_int (_int 2) (_neg_int (_int 3)) =", evalB(_eq_int(_int(_2))(_neg_int(_int(_3)))))
console.log("_eq_int (_neg_int (_int 2)) (_neg_int (_int 3)) =", evalB(_eq_int(_neg_int(_int(_2)))(_neg_int(_int(_3)))))
console.log("_eq_int (_int 2) (_int 2) =", evalB(_eq_int(_int(_2))(_int(_2))))
console.log("_eq_int (_neg_int (_int 2)) (_int 2) =", evalB(_eq_int(_neg_int(_int(_2)))(_int(_2))))
console.log("_eq_int (_int 2) (_neg_int (_int 2)) =", evalB(_eq_int(_int(_2))(_neg_int(_int(_2)))))
console.log("_eq_int (_neg_int (_int 2)) (_neg_int (_int 2)) =", evalB(_eq_int(_neg_int(_int(_2)))(_neg_int(_int(_2)))))
