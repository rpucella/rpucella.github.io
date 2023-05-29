############################################################
# FUNC 
# S-expressions surface syntax
# reference cells
# recursive closures
# cps transformation
#

import sys


#
# Expressions
#



def eval_iter (exp,env):
    current_exp = exp
    current_env = env
    while True:

        # these are all the special forms which directly
        # return the result of evaluating an expression

        if current_exp.expForm == "ECall":
            f = current_exp._fun.eval(current_env)
            if f.type != "function":
                raise Exception("Runtime error: trying to call a non-function")
            args = [ e.eval(current_env) for e in current_exp._args]
            if len(args) != len(f._params):
                raise Exception("Runtime error: argument # mismatch in call")
            new_env = zip(f._params,args) + f._env
            current_exp = f._body
            current_env = new_env

        elif current_exp.expForm == "EIf":
            v = current_exp._cond.eval(current_env)
            if v.type != "boolean":
                raise Exception ("Runtime error: condition not a Boolean")
            if v.value:
                current_exp = current_exp._then
            else:
                current_exp = current_exp._else

        else:
            return current_exp.real_eval(current_env)



class Exp (object):
    pass

    

class EValue (Exp):
    # Value literal (could presumably replace EInteger and EBoolean)
    def __init__ (self,v):
        self._value = v
        self.expForm = "EValue"
    
    def __str__ (self):
        return "EValue({})".format(self._value)

    def real_eval (self,env):
        return self._value

    def eval (self,env):
        return eval_iter(self,env)

    def cps (self,k):
        return ECall(k,[self])

    
class EPrimCall (Exp):
    # Call an underlying Python primitive, passing in Values
    #
    # simplifying the prim call
    # it takes an explicit function as first argument

    def __init__ (self,prim,es):
        self._prim = prim
        self._exps = es
        self.expForm = "EPrimCall"

    def __str__ (self):
        return "EPrimCall(<prim>,[{}])".format(",".join([ str(e) for e in self._exps]))

    def real_eval (self,env):
        vs = [ e.eval(env) for e in self._exps ]
        return apply(self._prim,vs)

    def eval (self,env):
        return eval_iter(self,env)

    def cps (self,k):
        # under the assumption that all primcalls wrapped as a function in the top-level environment
        return ECall(k,[self])


COUNTER = 0
def fresh ():
    global COUNTER
    COUNTER += 1
    return COUNTER-1

class EIf (Exp):
    # Conditional expression

    def __init__ (self,e1,e2,e3):
        self._cond = e1
        self._then = e2
        self._else = e3
        self.expForm = "EIf"

    def __str__ (self):
        return "EIf({},{},{})".format(self._cond,self._then,self._else)

    def real_eval (self,env):
        v = self._cond.eval(env)
        if v.type != "boolean":
            raise Exception ("Runtime error: condition not a Boolean")
        if v.value:
            return self._then.eval(env)
        else:
            return self._else.eval(env)

    def eval (self,env):
        return eval_iter(self,env)

    def cps (self,k):
        var = " _cps_{}_".format(fresh())
        return self._cond.cps(EFunction([var],EIf(EId(var),self._then.cps(k),self._else.cps(k))))



class EId (Exp):
    # identifier

    def __init__ (self,id):
        self._id = id
        self.expForm = "EId"

    def __str__ (self):
        return "EId({})".format(self._id)

    def real_eval (self,env):
        for (id,v) in env:
            if self._id == id:
                return v
        raise Exception("Runtime error: unknown identifier {}".format(self._id))

    def eval (self,env):
        return eval_iter(self,env)

    def cps (self,k):
        return ECall(k,[self])


class ECall (Exp):
    # Call a defined function in the function dictionary
    # uses an "apply" function in the closure for encapsulation

    def __init__ (self,fun,exps):
        self._fun = fun
        self._args = exps
        self.expForm = "ECall"

    def __str__ (self):
        return "ECall({},[{}])".format(str(self._fun),",".join([str(e) for e in self._args]))

    def real_eval (self,env):
        f = self._fun.eval(env)
        if f.type != "function":
            raise Exception("Runtime error: trying to call a non-function")
        args = [ e.eval(env) for e in self._args]
        return f.apply(args)

    def eval (self,env):
        return eval_iter(self,env)

    def cps (self,k):
        var_f = " _cps_{}_".format(fresh())
        vars_e = [ " _cps_{}_".format(fresh()) for e in self._args ]
        result = ECall(EId(var_f),[ EId(x) for x in vars_e]+[k])
        for (var,e) in reversed(zip(vars_e,self._args)):
            result = e.cps(EFunction([var],result))
        return self._fun.cps(EFunction([var_f],result))


class EFunction (Exp):
    # Creates an anonymous function

    def __init__ (self,params,body,name=None):
        self._param = params
        self._body = body
        self._name = name
        self.expForm = "EFunction"

    def __str__ (self):
        return "EFunction([{}],{})".format(",".join(self._param),str(self._body))

    def real_eval (self,env):
        return VClosure(self._param,self._body,env,self._name)

    def eval (self,env):
        return eval_iter(self,env)

    def cps (self,k):
        var = " _k_{}_".format(fresh())
        return ECall(k,[EFunction([ p for p in self._param] + [var],self._body.cps(EId(var)),name=self._name)])
    
    
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
    
    def __init__ (self,params,body,env,name=None):
        self._params = params
        self._body = body
        extra = [(name,self)] if name else []
        self._env = extra + env
        self.type = "function"

    def __str__ (self):
        return "<function [{}] {}>".format(",".join(self._params),str(self._body))

    def apply (self,args):
        if len(args) != len(self._params):
            raise Exception("Runtime error: argument # mismatch in call")
        new_env = zip(self._params,args) + self._env
        return self._body.eval(new_env)

    
class VRefCell (Value):

    def __init__ (self,initial):
        self.content = initial
        self.type = "ref"

    def __str__ (self):
        return "<ref {}>".format(str(self.content))


class VNone (Value):

    def __init__ (self):
        self.type = "none"

    def __str__ (self):
        return "none"





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


def oper_ref (v1):
    return VRefCell(v1)

def oper_deref (v1):
    if v1.type == "ref":
        return v1.content
    raise Exception ("Runtime error: dereferencing a non-reference value")

def oper_update (v1,v2):
    if v1.type == "ref":
        v1.content = v2
        return VNone()
    raise Exception ("Runtime error: updating a non-reference value")
 
def oper_print (v1):
    print v1
    return VNone()



# Initial primitives dictionary

def initial_env ():
    # A sneaky way to allow functions to refer to functions that are not
    # yet defined at top level, or recursive functions
    env = []
    env.insert(0,
               ("+",
                VClosure(["x","y"],
                         EPrimCall(oper_plus,[EId("x"),EId("y")]),
                         env)))
    env.insert(0,
               ("-",
                VClosure(["x","y"],
                         EPrimCall(oper_minus,[EId("x"),EId("y")]),
                         env)))
    env.insert(0,
               ("*",
                VClosure(["x","y"],
                         EPrimCall(oper_times,[EId("x"),EId("y")]),
                         env)))
    env.insert(0,
               ("zero?",
                VClosure(["x"],
                         EPrimCall(oper_zero,[EId("x")]),
                         env)))
    env.insert(0,
               ("ref",
                VClosure(["x"],
                         EPrimCall(oper_ref, [EId("x")]),
                         env)))
    env.insert(0,
               ("deref",
                VClosure(["x"],
                         EPrimCall(oper_deref, [EId("x")]),
                         env)))
    env.insert(0,
               ("update!",
                VClosure(["x","y"],
                         EPrimCall(oper_update,[EId("x"),EId("y")]),
                         env)))
    env.insert(0,
               ("print!",
                VClosure(["x"],
                         EPrimCall(oper_print,[EId("x")]),
                         env))),
    env.insert(0,
               ("=",
                VClosure(["x","y"],
                         ECall(EId("zero?"),
                               [ECall(EId("-"),[EId("x"),EId("y")])]),
                         env)))
    return env



##
## PARSER
##
# cf http://pyparsing.wikispaces.com/

from pyparsing import Word, Literal, ZeroOrMore, OneOrMore, Keyword, Forward, alphas, alphanums, NoMatch


def parse (input):
    # parse a string into an element of the abstract representation

    # Grammar:
    #
    # <expr> ::= <integer>
    #            true
    #            false
    #            <identifier>
    #            ( if <expr> <expr> <expr> )
    #            ( let ( ( <name> <expr> ) ... ) <expr )
    #            ( function ( <name> ... ) <expr> )
    #            ( <expr> <expr> ... )
    #


    idChars = alphas+"_+*-?!=<>"

    pIDENTIFIER = Word(idChars, idChars+"0123456789")
    pIDENTIFIER.setParseAction(lambda result: EId(result[0]))

    # A name is like an identifier but it does not return an EId...
    pNAME = Word(idChars,idChars+"0123456789")

    pNAMES = ZeroOrMore(pNAME)
    pNAMES.setParseAction(lambda result: [result])

    pINTEGER = Word("0123456789")
    pINTEGER.setParseAction(lambda result: EValue(VInteger(int(result[0]))))

    pBOOLEAN = Keyword("true") | Keyword("false")
    pBOOLEAN.setParseAction(lambda result: EValue(VBoolean(result[0]=="true")))

    pEXPR = Forward()

    pEXPRS = ZeroOrMore(pEXPR)
    pEXPRS.setParseAction(lambda result: [result])

    pIF = "(" + Keyword("if") + pEXPR + pEXPR + pEXPR + ")"
    pIF.setParseAction(lambda result: EIf(result[2],result[3],result[4]))

    pBINDING = "(" + pNAME + pEXPR + ")"
    pBINDING.setParseAction(lambda result: (result[1],result[2]))

    pBINDINGS = ZeroOrMore(pBINDING)
    pBINDINGS.setParseAction(lambda result: [ result ])

    def makeLet (bindings,body):
        params = [ param for (param,exp) in bindings ]
        args = [ exp for (param,exp) in bindings ]
        return ECall(EFunction(params,body),args)

    pLET = "(" + Keyword("let") + "(" + pBINDINGS + ")" + pEXPR + ")"
    pLET.setParseAction(lambda result: makeLet(result[3],result[5]))

    pCALL = "(" + pEXPR + pEXPRS + ")"
    pCALL.setParseAction(lambda result: ECall(result[1],result[2]))

    pFUN = "(" + Keyword("function") + "(" + pNAMES + ")" + pEXPR + ")"
    pFUN.setParseAction(lambda result: EFunction(result[3],result[5]))

    pFUNrec = "(" + Keyword("function") + pNAME + "(" + pNAMES + ")" + pEXPR + ")"
    pFUNrec.setParseAction(lambda result: EFunction(result[4],result[6],name=result[2]))

    def makeDo (exprs):
        result = exprs[-1]
        for e in reversed(exprs[:-1]):
            # space is not an allowed identifier in the syntax!
            result = makeLet([(" ",e)],result)
        return result

    pDO = "(" + Keyword("do") + pEXPRS + ")"
    pDO.setParseAction(lambda result: makeDo(result[2]))

    def makeWhile (cond,body):
        return makeLet([(" while",
                         EFunction([],EIf(cond,makeLet([(" ",body)],ECall(EId(" while"),[])),EValue(VNone())),name=" while"))],
                       ECall(EId(" while"),[]))

    pWHILE = "(" + Keyword("while") + pEXPR + pEXPR + ")"
    pWHILE.setParseAction(lambda result: makeWhile(result[2],result[3]))

    pEXPR << (pINTEGER | pBOOLEAN | pIDENTIFIER | pIF | pLET | pFUN | pFUNrec| pDO | pWHILE | pCALL)

    # can't attach a parse action to pEXPR because of recursion, so let's duplicate the parser
    pTOPEXPR = pEXPR.copy()
    pTOPEXPR.setParseAction(lambda result: {"result":"expression","expr":result[0]})

    pDEFINE = "(" + Keyword("define") + pNAME + pEXPR + ")"
    pDEFINE.setParseAction(lambda result: {"result":"value",
                                           "name":result[2],
                                           "expr":result[3]})

    pDEFUN = "(" + Keyword("defun") + pNAME + "(" + pNAMES + ")" + pEXPR + ")"
    pDEFUN.setParseAction(lambda result: {"result":"function",
                                          "name":result[2],
                                          "params":result[4],
                                          "body":result[6]})

    pABSTRACT = "#abs" + pEXPR
    pABSTRACT.setParseAction(lambda result: {"result":"abstract",
                                             "expr":result[1]})

    pQUIT = Keyword("#quit")
    pQUIT.setParseAction(lambda result: {"result":"quit"})
    
    pTOP = (pDEFUN | pDEFINE | pQUIT | pABSTRACT | pTOPEXPR)

    result = pTOP.parseString(input)[0]
    return result    # the first element of the result is the expression



# The standard shell (using the interpreter that optimizes tail calls)

def shell ():
    # A simple shell
    # Repeatedly read a line of input, parse it, and evaluate the result

    print "Lecture 7 - REF Language (defun, define, tail calls)"
    print "#quit to quit, #abs to see abstract representation"
    env = initial_env()

        
    while True:
        inp = raw_input("ref/tail> ")

        try:
            result = parse(inp)

            if result["result"] == "expression":
                exp = result["expr"]
                # print "Abstract representation:", exp
                v = exp.eval(env)
                print v

            elif result["result"] == "abstract":
                print result["expr"]

            elif result["result"] == "quit":
                return

            elif result["result"] == "function":
                # the top-level environment is special, it is shared
                # amongst all the top-level closures so that all top-level
                # functions can refer to each other
                env.insert(0,(result["name"],VClosure(result["params"],result["body"],env,name=result["name"])))
                print "{} defined".format(result["name"])

            elif result["result"] == "value":
                v = result["expr"].eval(env)
                env.insert(0,(result["name"],v))
                print "{} defined".format(result["name"])
                
        except Exception as e:
            print "Exception: {}".format(e)

        
# increase stack size to let us call recursive functions quasi comfortably
sys.setrecursionlimit(10000)



def initial_env_cps ():
    # A sneaky way to allow functions to refer to functions that are not
    # yet defined at top level, or recursive functions
    env = []
    env.insert(0,
               ("+",
                VClosure(["x","y","k"],
                         ECall(EId("k"),[EPrimCall(oper_plus,[EId("x"),EId("y")])]),
                         env)))
    env.insert(0,
               ("-",
                VClosure(["x","y","k"],
                         ECall(EId("k"),[EPrimCall(oper_minus,[EId("x"),EId("y")])]),
                         env)))
    env.insert(0,
               ("*",
                VClosure(["x","y","k"],
                         ECall(EId("k"),[EPrimCall(oper_times,[EId("x"),EId("y")])]),
                         env)))
    env.insert(0,
               ("zero?",
                VClosure(["x","k"],
                         ECall(EId("k"),[EPrimCall(oper_zero,[EId("x")])]),
                         env)))
    env.insert(0,
               ("ref",
                VClosure(["x","k"],
                         ECall(EId("k"),[EPrimCall(oper_ref, [EId("x")])]),
                         env)))
    env.insert(0,
               ("deref",
                VClosure(["x","k"],
                         ECall(EId("k"),[EPrimCall(oper_deref, [EId("x")])]),
                         env)))
    env.insert(0,
               ("update!",
                VClosure(["x","y","k"],
                         ECall(EId("k"),[EPrimCall(oper_update,[EId("x"),EId("y")])]),
                         env)))
    env.insert(0,
               ("print!",
                VClosure(["x","k"],
                         ECall(EId("k"),[EPrimCall(oper_print,[EId("x")])]),
                         env))),
    return env






# A version of the shell that first transforms code into continuation-passing
# style before evaluating it.

def shell_cps ():
    # A simple shell
    # Repeatedly read a line of input, parse it, and evaluate the result

    print "Lecture 7 - REF Language (defun, define, tail calls, CPS)"
    print "#quit to quit, #abs to see abstract representation"
    env = initial_env_cps()
        
    while True:
        inp = raw_input("ref/cps> ")

        try:
            result = parse(inp)

            if result["result"] == "expression":
                exp = result["expr"]
                # print "Abstract representation:", exp
                cps_exp = exp.cps(EFunction(["x"],EId("x")))
                v = cps_exp.eval(env)
                print v

            elif result["result"] == "abstract":
                exp = result["expr"]
                cps_exp = exp.cps(EFunction(["x"],EId("x")))
                print cps_exp

            elif result["result"] == "quit":
                return

            elif result["result"] == "function":
                # the top-level environment is special, it is shared
                # amongst all the top-level closures so that all top-level
                # functions can refer to each other
                var = " _k_{}_".format(fresh())
                env.insert(0,(result["name"],VClosure([ p for p in result["params"]] + [var],
                                                      result["body"].cps(EId(var)),
                                                      env,
                                                      name=result["name"])))
                print "{} defined".format(result["name"])

            elif result["result"] == "value":
                exp = result["expr"]
                cps_exp = exp.cps(EFunction(["x"],EId("x")))
                v = cps_exp.eval(env)
                env.insert(0,(result["name"],v))
                print "{} defined".format(result["name"])
                
        except Exception as e:
            print "Exception: {}".format(e)
