############################################################
# Simple functional language
# S-expressions surface syntax
# 

import sys

#
# Expressions
#

class Exp (object):
    pass


class EValue (Exp):
    # Value literal (could presumably replace EInteger and EBoolean)
    def __init__ (self,v):
        self._value = v
    
    def __str__ (self):
        return "EValue({})".format(self._value)

    def eval (self,env):
        return self._value

    
class EPrimCall (Exp):
    # Call an underlying Python primitive, passing in Values
    #
    # simplifying the prim call
    # it takes an explicit function as first argument

    def __init__ (self,prim,es):
        self._prim = prim
        self._exps = es

    def __str__ (self):
        return "EPrimCall(<prim>,[{}])".format(",".join([ str(e) for e in self._exps]))

    def eval (self,env):
        vs = [ e.eval(env) for e in self._exps ]
        return apply(self._prim,vs)


class EIf (Exp):
    # Conditional expression

    def __init__ (self,e1,e2,e3):
        self._cond = e1
        self._then = e2
        self._else = e3

    def __str__ (self):
        return "EIf({},{},{})".format(self._cond,self._then,self._else)

    def eval (self,env):
        v = self._cond.eval(env)
        if v.type != "boolean":
            raise Exception ("Runtime error: condition not a Boolean")
        if v.value:
            return self._then.eval(env)
        else:
            return self._else.eval(env)


class ELet (Exp):
    # local binding
    # allow multiple bindings
    # eager (call-by-avlue)

    def __init__ (self,bindings,e2):
        self._bindings = bindings
        self._e2 = e2

    def __str__ (self):
        return "ELet([{}],{})".format(",".join([ "({},{})".format(id,str(exp)) for (id,exp) in self._bindings ]),self._e2)

    def eval (self,env):
        new_env = [ (id,e.eval(env)) for (id,e) in self._bindings] + env
        return self._e2.eval(new_env)

class EId (Exp):
    # identifier

    def __init__ (self,id):
        self._id = id

    def __str__ (self):
        return "EId({})".format(self._id)

    def eval (self,env):
        for (id,v) in env:
            if self._id == id:
                return v
        raise Exception("Runtime error: unknown identifier {}".format(self._id))


class ECall (Exp):
    # Call a defined function in the function dictionary

    def __init__ (self,fun,exp):
        self._fun = fun
        self._arg = exp

    def __str__ (self):
        return "ECall({},{})".format(str(self._fun),str(self._arg))

    def eval (self,env):
        f = self._fun.eval(env)
        if f.type != "function":
            raise Exception("Runtime error: trying to call a non-function")
        arg = self._arg.eval(env)
        new_env = [(f.param,arg)] + f.env
        return f.body.eval(new_env)

class EFunction (Exp):
    # Creates an anonymous function

    def __init__ (self,param,body):
        self._param = param
        self._body = body

    def __str__ (self):
        return "EFunction({},{})".format(self._param,str(self._body))

    def eval (self,env):
        return VClosure(self._param,self._body,env)

    
#
# Values
#

class Value (object):
    pass


class VInteger (Value):
    # Value representation of integers
    
    def __init__ (self,i):
        self.value = i
        self.type = "integer"

    def __str__ (self):
        return str(self.value)

    
class VBoolean (Value):
    # Value representation of Booleans
    
    def __init__ (self,b):
        self.value = b
        self.type = "boolean"

    def __str__ (self):
        return "true" if self.value else "false"

    
class VClosure (Value):
    
    def __init__ (self,param,body,env):
        self.param = param
        self.body = body
        self.env = env
        self.type = "function"

    def __str__ (self):
        return "<function {} {}>".format(self.param,str(self.body))



# Primitive operations

def oper_plus (v1,v2): 
    if v1.type == "integer" and v2.type == "integer":
        return VInteger(v1.value + v2.value)
    raise Exception ("Runtime error: trying to add non-numbers")

def oper_minus (v1,v2):
    if v1.type == "integer" and v2.type == "integer":
        return VInteger(v1.value - v2.value)
    raise Exception ("Runtime error: trying to subtract non-numbers")

def oper_times (v1,v2):
    if v1.type == "integer" and v2.type == "integer":
        return VInteger(v1.value * v2.value)
    raise Exception ("Runtime error: trying to multiply non-numbers")

def oper_zero (v1):
    if v1.type == "integer":
        return VBoolean(v1.value==0)
    raise Exception ("Runtime error: type error in zero?")


# Initial primitives dictionary

def initialEnv ():
    # A sneaky way to allow functions to refer to functions that are not
    # yet defined at top level, or recursive functions
    env = []
    base = [ 
        ("!+",
         VClosure("x",EFunction("y",EPrimCall(oper_plus,
                                              [EId("x"),EId("y")])),
                  env)),
        ("!-",
         VClosure("x",EFunction("y",EPrimCall(oper_minus,
                                              [EId("x"),EId("y")])),
                  env)),
        ("!*",
         VClosure("x",EFunction("y",EPrimCall(oper_times,
                                              [EId("x"),EId("y")])),
                  env)),
        ("zero?",
         VClosure("x",EPrimCall(oper_zero,
                                         [EId("x")]),
                           env)),
        ("square",
         VClosure("x",ECall(ECall(EId("!*"),EId("x")),
                            EId("x")),
                  env)),
        ("!=",
         VClosure("x",EFunction("y",ECall(EId("zero?"),
                                          ECall(ECall(EId("!-"),EId("x")),
                                                EId("y")))),
                  env)),
        ("+1",
         VClosure("x",ECall(ECall(EId("!+"),EId("x")),
                            EValue(VInteger(1))),
                  env)),
        ("!sum_from_to",
         VClosure("s",EFunction("e",
                                EIf(ECall(ECall(EId("!="),EId("s")),EId("e")),
                                    EId("s"),
                                    ECall(ECall(EId("!+"),EId("s")),
                                          ECall(ECall(EId("!sum_from_to"),
                                                      ECall(EId("+1"),EId("s"))),
                                                EId("e"))))),
                  env)),
    ]
    env.extend(base)
    return env



##
## PARSER
##
# cf http://pyparsing.wikispaces.com/

from pyparsing import Word, Literal, ZeroOrMore, OneOrMore, Keyword, Forward, alphas, alphanums


def parse (input):
    # parse a string into an element of the abstract representation

    # Grammar:
    #
    # <expr> ::= <integer>
    #            true
    #            false
    #            <identifier>
    #            ( if <expr> <expr> <expr> )
    #            ( let ( ( <name> <expr> ) ) <expr )
    #            (function ( <name> ) <expr> )
    #            ( <expr> <expr> )
    #


    idChars = alphas+"_+*-?!=<>"

    pIDENTIFIER = Word(idChars, idChars+"0123456789")
    pIDENTIFIER.setParseAction(lambda result: EId(result[0]))

    # A name is like an identifier but it does not return an EId...
    pNAME = Word(idChars,idChars+"0123456789")

    pINTEGER = Word("-0123456789","0123456789")
    pINTEGER.setParseAction(lambda result: EValue(VInteger(int(result[0]))))

    pBOOLEAN = Keyword("true") | Keyword("false")
    pBOOLEAN.setParseAction(lambda result: EValue(VBoolean(result[0]=="true")))

    pEXPR = Forward()

    pIF = "(" + Keyword("if") + pEXPR + pEXPR + pEXPR + ")"
    pIF.setParseAction(lambda result: EIf(result[2],result[3],result[4]))

    pBINDING = "(" + pNAME + pEXPR + ")"
    pBINDING.setParseAction(lambda result: (result[1],result[2]))

    pBINDINGS = OneOrMore(pBINDING)
    pBINDINGS.setParseAction(lambda result: [ result ])

    pLET = "(" + Keyword("let") + "(" + pBINDINGS + ")" + pEXPR + ")"
    pLET.setParseAction(lambda result: ELet(result[3],result[5]))

    pCALL = "(" + pEXPR + pEXPR + ")"
    pCALL.setParseAction(lambda result: ECall(result[1],result[2]))

    pFUN = "(" + Keyword("function") + "(" + pNAME + ")" + pEXPR + ")"
    pFUN.setParseAction(lambda result: EFunction(result[3],result[5]))

    pEXPR << (pINTEGER | pBOOLEAN | pIDENTIFIER | pIF | pLET | pFUN | pCALL)

    result = pEXPR.parseString(input)[0]
    return result    # the first element of the result is the expression


def shell ():
    # A simple shell
    # Repeatedly read a line of input, parse it, and evaluate the result

    print "Lecture 4 - Func Language"
    env = initialEnv()
    while True:
        inp = raw_input("func> ")
        if not inp:
            return
        exp = parse(inp)
        print "Abstract representation:", exp
        v = exp.eval(env)
        print v

        
# increase stack size to let us call recursive functions quasi comfortably
sys.setrecursionlimit(10000)

