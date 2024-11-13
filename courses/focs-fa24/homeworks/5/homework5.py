# --------------------------------------------------
#
# HOMEWORK 5
#
# Due: Tue, Nov 19 2024 (23h59)
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



class RM:

    def __init__(self):
        self._silent = False
        self._fresh = 100

    def silent(self, flag):
        self._silent = flag

    def expand_memory(self, register, memory):
        if register >= len(memory):
            memory.extend([0] * (1 + register - len(memory)))

    def show_step(self, pc, memory):
        content = "  ".join(str(c) for c in memory)
        if pc is None:
            print(f"----  {content}")
        else:
            print(f"{pc:04d}  {content}")

    def show_program(self, program):
        pc = 0
        for instruction in program:
            content = " ".join(str(c) for c in instruction)
            print(f"{pc:04d}  {content}")
            if instruction[0] not in ["label", "register", "temporary"]:
                pc += 1

    def get_register(self, memory, register):
        if register >= len(memory):
            memory.extend([0] * (1 + register - len(memory)))
        return memory[register]

    def max_register(self, program):
        def get_reg(instruction):
            opcode = instruction[0]
            if opcode == "inc" and isinstance(instruction[1], int):
                return instruction[1]
            elif opcode == "dec" and isinstance(instruction[1], int):
                return instruction[1]
            elif opcode == "stop" and isinstance(instruction[1], int):
                return instruction[1]
            elif opcode == "register" and isinstance(instruction[2], int):
                return instruction[2]
            else:
                return 0
        mx = 0
        for p in program:
            mx = max(mx, get_reg(p))
        return mx

    def run(self, program, init):
        program = self.resolve_registers(program)
        program = self.resolve_labels(program)
        pc = 0
        memory = init
        stop = None
        while stop is None:
            if pc < 0 or pc >= len(program):
                raise Exception(f"@{pc}: attempting to access instruction outside the program (= segfault)")
            if not self._silent:
                self.show_step(pc, memory)
            instruction = program[pc]
            try:
                opcode = instruction[0].lower()
                if opcode == 'inc':
                    v = self.get_register(memory, instruction[1])
                    memory[instruction[1]] = v + 1
                    pc += 1
                elif opcode == 'dec':
                    self.expand_memory(instruction[1], memory)
                    v = self.get_register(memory, instruction[1])
                    if v == 0:
                        pc = instruction[2]
                    else:
                        memory[instruction[1]] = v - 1
                        pc += 1
                elif opcode == 'jump':
                    pc = instruction[1]
                elif opcode == 'stop':
                    stop = self.get_register(memory, instruction[1])
                else:
                    raise Exception
            except:
                raise Exception(f"At {pc:04d}: bad instruction {instruction}")
        self.show_step(None, memory)
        return stop

    def resolve_registers(self, program):
        # Complete this method for Question 2(A).
        # Currently it does nothing.
        return program

    def resolve_labels(self, program):
        # Complete this method for Question 2(B).
        # Currently, it does nothing.
        return program




# Some sample programs.

P_RESET = [
    # Reset register 0.
    ("dec", 0, 2),
    ("jump", 0),

    ("stop", 0)
]

P_TRANSFER = [
    # Transfer register 0 to register 1.
    ("dec", 0, 3),
    ("inc", 1),
    ("jump", 0),

    ("stop", 0)
]

P_COPY = [
    # Copy register 0 to register 1 (preserving 0).
    ("dec", 0, 4),
    ("inc", 1),
    ("inc", 2),
    ("jump", 0),

    ("dec", 2, 7),
    ("inc", 0),
    ("jump", 4),

    ("stop", 1)
]

P_PLUS1 = [
    # Z := X + 1
    ("dec", 0, 3),
    ("inc", 2),
    ("jump", 0),

    ("dec", 2, 7),
    ("inc", 0),
    ("inc", 1),
    ("jump", 3),

    ("inc", 1),
    ("stop", 1)
]


# Question 1
#
# Replace these placeholder programs by your own.

P_PLUS = [
    ("stop", 0)
]

P_MINUS = [
    ("stop", 0)
]

P_MAX = [
    ("stop", 0)
]


# Question 2
#
# Replace these placeholder programs by your own.

P_DIFF = [
    ("register", "X", 0),
    ("register", "Y", 1),
    ("register", "Z", 2),
    ("stop", "Z")
]
