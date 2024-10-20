# --------------------------------------------------
#
# HOMEWORK 4
#
# Due: Tue, Oct 29 2024 (23h59)
#
# Name:
#
# Email:
#
# Remarks, if any:
#
#
#
# --------------------------------------------------
#
# Please fill in this file with your solutions and submit it
#
# The functions below are stubs that you should replace with your
# own implementation.
#
# PLEASE DO NOT CHANGE THE SIGNATURE IN THE STUBS BELOW.
# Doing so risks making it impossible for me to test your code.
#
# --------------------------------------------------


#
# Some sample context-free grammars.
#

GR_ANBN = {
    "nonterminals": ["S"],
    "alphabet": ["a", "b"],
    "rules": [
        ("S", ""),
        ("S", "aSb")
    ],
    "start": "S"
}

GR_ANBM = {
    "nonterminals": ["S", "T", "U"],
    "alphabet": ["a", "b"],
    "rules": [
        ("S", "TU"),
        ("T", ""),
        ("T", "aTb"),
        ("U", ""),
        ("U", "Ub")
    ],
    "start": "S"
}


# Try to apply rules to a given string.

def replace(str, pos, lhs, rhs):
    # Replace lhs with rhs at pos of string.
    return str[:pos] + rhs + str[pos + len(lhs):]

def apply_rule(str, pos, rs):
    # Try to apply a rule in rs to str at position pos
    result = []
    for (lhs, rhs) in rs:
        if str[pos:].startswith(lhs):
            res = replace(str, pos, lhs, rhs)
            result.append(res)
    return result

def apply_rules(rs, str):
    result = []
    for pos in range(len(str)):
        res = apply_rule(str, pos, rs)
        result.extend(res)
    return result


# Remove null rules to simplify generation.

def nullable(g, nonterm):
    for (lhs, rhs) in g['rules']:
        if lhs == nonterm:
            if not rhs:
                return True
            for sym in rhs:
                if sym in g['alphabet']:
                    continue
                if sym == lhs:
                    # Skip in case of recursion.
                    continue
                if not nullable(g, sym):
                    continue
            return True
    return False

def remove_nullable(g):
    large = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    nonterminals = [x for x in g['nonterminals']]
    def get_next():
        for t in large:
            if t not in nonterminals:
                nonterminals.append(t)
                return t
        raise Exception("Ran out of capital letters!")
    new_start = get_next()
    rules = g['rules']
    rules = [r for r in rules if r[1]]
    new_rules = []
    for (lhs, rhs) in rules:
        nullables = [i for (i, s) in enumerate(rhs) if nullable(g, s)]
        update = [rhs]
        for i in nullables:
            new_update = []
            for c in update:
                new_update.append(c)
                new_update.append(replace(c, i, "?", "."))
            update = new_update
        new_rules.extend([(lhs, rhs.replace(".", "")) for rhs in update])
    new_rules.append((new_start, g['start']))
    if nullable(g, g['start']):
        new_rules.append((new_start, ""))
    return {
        "alphabet": g['alphabet'],
        "nonterminals": nonterminals,
        "rules": new_rules,
        "start": new_start
    }
            
# Perform an iteratively deepening depth-first search of the rewrite tree.

def dfs_path(maxdepth, grammar, target):
    lt = len(target)
    q = [([grammar['start']], 0)]
    seen = set()
    while q:
        (path, d) = q[0]
        ###print(path, d)
        q = q[1:]
        if path:
            str = path[-1]
            if len(str) == lt and str == target:
                return path
            if str in seen:
                continue
            if d > maxdepth:
                seen.add(str)
                continue
            new_strs = apply_rules(grammar['rules'], str)
            ##print(new_strs)
            new_strs_d = [(path + [x], d + 1) for x in new_strs]
            ##print(new_strs_d)
            q = new_strs_d + q
            seen.add(str)
        else:
            raise Exception(f"Problem: empty path in dfs_path?")
    return []

def idfs_path(maxdepth, grammar, target, verbose=False):
    for d in range(1, maxdepth):
        if verbose:
            print(f"Searching - depth {d}")
        path = dfs_path(d, grammar, target)
        ##print(path)
        if path:
            return path

# Try to generate a string for a given grammar.

def check_cfg(g):
    # Check if a grammar is context free.
    for r in g['rules']:
        (lhs, rhs) = r
        if len(lhs) != 1:
            raise Exception(f"Rule {r} is not context free")
        for sym in rhs:
            if sym not in g['alphabet'] and sym not in g['nonterminals']:
                raise Exception(f"Symbol {sym} not defined")
    if g['start'] not in g['nonterminals']:
        raise Exception(f"Start symbol not a nonterminal")
    
def generate(grammar, str, maxdepth):
    check_cfg(grammar)
    g = remove_nullable(grammar)
    path = idfs_path(maxdepth, g, str, verbose=True)
    for (i, p) in enumerate(path or []):
        print(f"{'->' if i else ''} {p}")
    return bool(path)


#
# QUESTION 1
#

GR_A = None

GR_B = None

GR_C = None

GR_D = None




# Sample Turing machines.


TM_ANBN = {
    "states": [1, 2, 4, 6, 7, 777, 666],
    "alphabet": ['a','b'],
    "tape_alphabet": ['a','b','X','Y','_'],
    "start": 1,
    "accept": 777,
    "reject": 666,
    "delta": [
        (1, 'a', 2, 'X', 1),
        (1, '_', 777, '_', 1),
        (2, 'a', 2, 'a', 1),
        (2, 'Y', 2, 'Y', 1),
        (2, 'b', 4, 'Y', -1),
        (4, 'Y', 4, 'Y', -1),
        (4, 'a', 7, 'a', -1),
        (4, 'X', 6, 'X', 1),
        (6, 'Y', 6, 'Y', 1),
        (6, '_', 777, '_', 1),
        (7, 'a', 7, 'a', -1),
        (7, 'X', 1, 'X', 1)
    ]
}


TM_ANBNCN = {
    "states": [1, 2, 3, 4, 5, 6, 7, 8, 666, 777],
    "alphabet": ['a','b','c'],
    "tape_alphabet": ['a','b','c','X','Y','Z','_'],
    "start": 1,
    "accept": 777,
    "reject": 666,
    "delta": [
        (1, 'a', 2, 'X', 1),
        (1, '_', 777, '_', 1),
        (2, 'a', 2, 'a', 1),
        (2, 'Y', 2, 'Y', 1),
        (2, 'b', 3, 'Y', 1),
        (3, 'b', 3, 'b', 1),
        (3, 'Z', 3, 'Z', 1),
        (3, 'c', 4, 'Z', -1),
        (4, 'Z', 4, 'Z', -1),
        (4, 'Y', 5, 'Y', -1),
        (4, 'b', 7, 'b', -1),
        (5, 'Y', 5, 'Y', -1),
        (5, 'X', 6, 'X', 1),
        (6, 'Y', 6, 'Y', 1),
        (6, 'Z', 6, 'Z', 1),
        (6, '_', 777, '_', 1),
        (7, 'b', 7, 'b', -1),
        (7, 'Y', 7, 'Y', -1),
        (7, 'a', 8, 'a', -1),
        (8, 'a', 8, 'a', -1),
        (8, 'X', 1, 'X', 1)
    ]
}


# Print a configuration from a Turing machine.

def print_config(m, c):
    (state, tape, pos) = c
    width = max(len(str(q)) for q in m['states'])
    padding = max(0, pos - len(tape))
    tape = tape + (' ' * padding)
    content = "".join(f"[{x}]" if i + 1 == pos else f" {x} " for (i, x) in enumerate(tape))
    print(f"{(str(state) + ' ' * width)[:width]} {content}")


#
# QUESTION 2
#

def start_tm(m, w):
    raise Exception("Not implemented")


def is_accept_tm(m, c):
    raise Exception("Not implemented")

    
def is_done_tm(m, c):
    raise Exception("Not implemented")

    
def step_tm(m, c):
    raise Exception("Not implemented")


def accept_tm(m, input):
    raise Exception("Not implemented")



#
# QUESTION 3
#

TM_EQUAL = None

TM_AND = None

TM_PLUS1 = None

TM_PLUS = None

