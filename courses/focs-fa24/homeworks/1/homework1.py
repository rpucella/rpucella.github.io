# --------------------------------------------------
#
# HOMEWORK 1
#
# Due: Sun, Sep 29, 2023 (23h59)
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
# Some sample DFAs.
#

DFA_MOD_3 = {
    "states": [1, 2, 3],
    "alphabet": ["a", "b", "c"],
    "delta": [
        (1, "a", 2),
        (2, "a", 3),
        (3, "a", 1),
        (1, "b", 1),
        (2, "b", 2),
        (3, "b", 3),
        (1, "c", 1),
        (2, "c", 2),
        (3, "c", 3)
    ],
    "start": 1,
    "final": [1]
}

DFA_START_END = {
    "states": [0, 1, 2, 99],
    "alphabet": ["a", "b", "c"],
    "delta": [
        (0, "a", 1),
        (0, "b", 99),
        (0, "c", 99),
        (1, "a", 1),
        (1, "b", 1),
        (1, "c", 2),
        (2, "a", 1),
        (2, "b", 1),
        (2, "c", 2),
        (99, "a", 99),
        (99, "b", 99),
        (99, "c", 99),
    ],
    "start": 0,
    "final": [2]
}


#
# Function to print the language of a DFA
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


#
# Question 1
#


def accept_dfa(m, input):
    raise Exception("No implemented")


#
# Question 2
#

DFA_Q2A = None


DFA_Q2B = None


DFA_Q2C = None


DFA_Q2D = None


DFA_Q2E = None



#
# Question 3
#

def inter(dfa1, dfa2):
    raise Exception("Not implemented")
