# --------------------------------------------------
#
# HOMEWORK 3
#
# Due: Sun, Oct 13 2024 (23h59)
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
# Some sample PDAs.
#

PDA_ANBN = {
    "states": [0, 1],
    "alphabet": ["a", "b"],
    "stack_alphabet": [".", "X"],
    "start": 0,
    "final": [1],
    "delta": [
        (0, "a", ".", 0, ["X", "."]),
        (0, "a", "X", 0, ["X", "X"]),
        (0, None, ".", 1, ["."]),
        (0, None, "X", 1, ["X"]),
        (1, "b", "X", 1, []),
    ]
}

PDA_PAL = {
    "states": [0, 1],
    "alphabet": ["a", "b"],
    "stack_alphabet": [".", "X", "Y"],
    "start": 0,
    "final": [1],
    "delta": [
        (0, "a", ".", 0, ["X", "."]),
        (0, "a", "X", 0, ["X", "X"]),
        (0, "a", "Y", 0, ["X", "Y"]),
        (0, "b", ".", 0, ["Y", "."]),
        (0, "b", "X", 0, ["Y", "X"]),
        (0, "b", "Y", 0, ["Y", "Y"]),
        (0, None, ".", 1, ["."]),
        (0, None, "X", 1, ["X"]),
        (0, None, "Y", 1, ["Y"]),
        (0, "a", ".", 1, ["."]),
        (0, "a", "X", 1, ["X"]),
        (0, "a", "Y", 1, ["Y"]),
        (0, "b", ".", 1, ["."]),
        (0, "b", "X", 1, ["X"]),
        (0, "b", "Y", 1, ["Y"]),
        (1, "a", "X", 1, []),
        (1, "b", "Y", 1, [])
    ]
}

# A _deterministic_ PDA corresponding to PDA_ANBN.

DPDA_ANBN = {
    "states": [0, 1, 2, 99],
    "alphabet": ["a", "b"],
    "stack_alphabet": [".", "X"],
    "start": 0,
    "final": [0, 2],
    "delta": [
        (0, "a", ".", 1, ["X", "."]),
        (1, "a", "X", 1, ["X", "X"]),
        (1, "b", "X", 2, []),
        (2, "b", "X", 2, []),

        # Sink transitions (for deterministism)
        (0, "a", "X", 99, []),
        (0, "b", ".", 99, ["."]),
        (0, "b", "X", 99, []),
        (1, "a", ".", 99, ["."]),
        (1, "b", ".", 99, ["."]),
        (2, "a", ".", 99, ["."]),
        (2, "a", "X", 99, []),
        (2, "b", ".", 99, ["."]),
        (99, "a", ".", 99, ["."]),
        (99, "a", "X", 99, []),
        (99, "b", ".", 99, ["."]),
        (99, "b", "X", 99, [])
    ]
}


#
# A generic function to create an acceptance function for nondeterministic machines.
#

def accept_ndet(step, start, is_done, is_accept):
    def accept(m, input):
        config = start(m, input)
        seen = []
        queue = [config]
        while queue:
            config = queue[0]
            queue = queue[1:]
            if config in seen:
                continue
            seen.append(config)
            if is_done(m, config):
                if is_accept(m, config):
                    return True
            next = step(m, config)
            queue.extend(next)
        return False
    return accept


#
# Function to print the language of an automaton
# up to a certain string length.
#

def language(m, n, accept):

    def to_base_n_rev(num, n):
        result = []
        while num > 0:
            result.append(num % n)
            num = num // n
        return result

    def enum(size):
        result = []
        for num in range(len(alphabet) ** size):
            digits = to_base_n_rev(num, len(alphabet))
            digits.extend([0] * (size - len(digits)))
            result.append("".join(alphabet[digits[size - i - 1]] for i in range(size)))
        return result

    alphabet = m["alphabet"]
    for size in range(n + 1):
        for input in enum(size):
            if accept(m, input):
                print(input or "<empty string>")


#############################################################
#
# Question 1
#

def step_pda(m, config):
    raise Exception("Not implemented")


def start_pda(m, input):
    raise Exception("Not implemented")


def is_done_pda(m, config):
    raise Exception("Not implemented")


def is_accept_pda(m, config):
    raise Exception("Not implemented")


def accept_dpda(m, input):
    raise Exception("Not implemented")



#############################################################
#
# Question 2
#

#
# The acceptance function for nondeterministic PDAs.
#

accept_pda = accept_ndet(step_pda, start_pda, is_done_pda, is_accept_pda)


PDA_AMBN_MGN = None


PDA_AMBN_NGM = None


PDA_AMBNCMN = None


PDA_AMBMNCN = None
