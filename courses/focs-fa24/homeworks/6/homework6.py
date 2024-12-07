# --------------------------------------------------
#
# HOMEWORK 6
#
# Due: Thu, Dec 12 2024 (23h59)
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

    def show(self, program):
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

    def run(self, program, init):
        program = self.resolve_executes(program)
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

    def register_names(self, program):
        # Also in instructions?
        result = []
        for instruction in program:
            opcode = instruction[0]
            if opcode in ["register", "temporary"] and instruction[1] not in result:
                result.append(instruction[1])
        return result

    def label_names(self, program):
        # Also in instructions?
        result = []
        for instruction in program:
            opcode = instruction[0]
            if opcode in ["label"] and instruction[1] not in result:
                result.append(instruction[1])
        return result

    def resolve_registers(self, program):
        rmap = {}
        current_temp = self.max_register(program) + 1
        for instruction in program:
            if instruction[0] == "register":
                if isinstance(instruction[2], str):
                    rmap[instruction[1]] = rmap[instruction[2]]
                else:
                    rmap[instruction[1]] = instruction[2]
            elif instruction[0] == "temporary":
                rmap[instruction[1]] = current_temp
                current_temp += 1
        def convert(instruction):
            opcode = instruction[0].lower()
            if opcode == "inc":
                if isinstance(instruction[1], str):
                    return ("inc", rmap[instruction[1]])
                else:
                    return instruction
            elif opcode == "dec":
                if isinstance(instruction[1], str):
                    return ("dec", rmap[instruction[1]], instruction[2])
                else:
                    return instruction
            elif opcode == "stop":
                if isinstance(instruction[1], str):
                    return ("stop", rmap[instruction[1]])
                else:
                    return instruction
            elif opcode in ["register", "temporary"]:
                return None
            else:
                return instruction
        return [convert(p) for p in program if convert(p) is not None]

    def resolve_labels(self, program):
        lmap = {}
        pc = 0
        for p in program:
            if p[0].lower() == "label":
                lmap[p[1]] = pc
            else:
                pc += 1
        def convert(instruction):
            opcode = instruction[0].lower()
            if opcode == "dec":
                if isinstance(instruction[2], str):
                    return ("dec", instruction[1], lmap[instruction[2]])
                else:
                    return instruction
            elif opcode == "jump":
                if isinstance(instruction[1], str):
                    return ("jump", lmap[instruction[1]])
                else:
                    return instruction
            elif opcode == "label":
                return None
            else:
                return instruction
        return [convert(p) for p in program if convert(p) is not None]

    def fresh_label(self):
        curr = self._fresh
        self._fresh += 1
        return f"_L{curr}"

    def fresh_register(self):
        curr = self._fresh
        self._fresh += 1
        return f"_R{curr}"

    def rename_register(self, program, source, target):
        # PLEASE COMPLETE THIS METHOD.
        return program

    def rename_label(self, program, source, target):
        # PLEASE COMPLETE THIS METHOD.
        return program

    def retarget_register(self, program, source, target):
        # PLEASE COMPLETE THIS METHOD.
        return program

    def replace_stop(self, program, label):
        # PLEASE COMPLETE THIS METHOD.
        return program

    def resolve_executes(self, program):
        # PLEASE COMPLETE THIS METHOD.
        return program


# Some core programs for manipulating registers.

P_COPY = [
    # Copy register X to register Y.
    ("register", "X", 0),
    ("register", "Y", 1),
    ("temporary", "T"),
    ("label", "start"),
    ("dec", "X", "reset-x"),
    ("inc", "Y"),
    ("inc", "T"),
    ("jump", "start"),
    ("label", "reset-x"),
    ("dec", "T", "done"),
    ("inc", "X"),
    ("jump", "reset-x"),
    ("label", "done"),
    ("stop", "Y")
]

P_EMPTY = [
    # Empty out register X.
    ("register", "X", 0),
    ("label", "start"),
    ("dec", "X", "done"),
    ("jump", "start"),
    ("label", "done"),
    ("stop", "X")
]

P_MOVE = [
    # Move register X to register Y.
    ("register", "X", 0),
    ("register", "Y", 1),
    ("label", "start"),
    ("dec", "X", "done"),
    ("inc", "Y"),
    ("jump", "start"),
    ("label", "done"),
    ("stop", "Y")
]


# Some sample programs using EXECUTE.

P_PLUS = [
    # Z = X + Y.
    ("register", "X", 0),
    ("register", "Y", 1),
    ("register", "Z", 2),
    ("temporary", "T"),
    ("temporary", "U"),
    ("execute", P_COPY, ["X", "T"]),
    ("execute", P_COPY, ["Y", "U"]),
    ("execute", P_MOVE, ["T", "Z"]),
    ("execute", P_MOVE, ["U", "Z"]),
    ("stop", "Z")
]

P_TIMES = [
    # Z = X * Y.
    ("register", "X", 0),
    ("register", "Y", 1),
    ("register", "Z", 2),
    ("temporary", "T"),
    ("temporary", "U"),
    ("execute", P_COPY, ["X", "T"]),
    ("label", "loop"),
    ("dec", "T", "done"),
    ("execute", P_COPY, ["Y", "U"]),
    ("execute", P_MOVE, ["U", "Z"]),
    ("jump", "loop"),
    ("label", "done"),
    ("stop", "Z")
]

P_MINUS = [
    # Z = X - Y if X > Y else 0.
    ("register", "X", 0),
    ("register", "Y", 1),
    ("register", "Z", 2),
    ("temporary", "T"),
    ("temporary", "U"),
    ("execute", P_COPY, ["X", "Z"]),
    ("execute", P_COPY, ["Y", "T"]),
    ("label", "loop"),
    ("dec", "T", "done"),
    ("dec", "Z", "done"),
    ("jump", "loop"),
    ("label", "done"),
    ("execute", P_EMPTY, ["T"]),
    ("stop", "Z")
]

P_DIFF = [
    # Z = difference between X and Y.
    ("register", "X", 0),
    ("register", "Y", 1),
    ("register", "Z", 2),
    ("temporary", "T"),
    ("temporary", "U"),
    ("temporary", "V"),
    # T = X - Y.
    ("execute", P_COPY, ["X", "T"]),
    ("execute", P_COPY, ["Y", "V"]),
    ("label", "loop1"),
    ("dec", "V", "done1"),
    ("dec", "T", "done1"),
    ("jump", "loop1"),
    ("label", "done1"),
    ("execute", P_EMPTY, ["V"]),
    # U = Y - X.
    ("execute", P_COPY, ["Y", "U"]),
    ("execute", P_COPY, ["X", "V"]),
    ("label", "loop2"),
    ("dec", "V", "done2"),
    ("dec", "U", "done2"),
    ("jump", "loop2"),
    ("label", "done2"),
    ("execute", P_EMPTY, ["V"]),
    # Z = T + U.
    ("execute", P_MOVE, ["T", "Z"]),
    ("execute", P_MOVE, ["U", "Z"]),
    ("stop", "Z")
]
