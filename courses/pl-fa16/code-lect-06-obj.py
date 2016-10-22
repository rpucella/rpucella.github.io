############################################################
# OBJ
# S-expressions surface syntax
# reference cells
# recursive closures
#
# classes & objects
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
    # uses an "apply" function in the closure for encapsulation

    def __init__ (self,fun,exps):
        self._fun = fun
        self._args = exps

    def __str__ (self):
        return "ECall({},[{}])".format(str(self._fun),",".join([str(e) for e in self._args]))

    def eval (self,env):
        f = self._fun.eval(env)
        if f.type != "function":
            raise Exception("Runtime error: trying to call a non-function")
        args = [ e.eval(env) for e in self._args]
        return f.apply(args)


class EFunction (Exp):
    # Creates an anonymous function

    def __init__ (self,params,body,name=None):
        self._param = params
        self._body = body
        self._name = name

    def __str__ (self):
        return "EFunction([{}],{})".format(",".join(self._param),str(self._body))

    def eval (self,env):
        return VClosure(self._param,self._body,env,self._name)


class EDo (Exp):

    def __init__ (self,exps):
        self._exps = exps

    def __str__ (self):
        return "EDo([{}])".format(",".join([str(e) for e in self._exps]))

    def eval (self,env):
        # default return value for do when no arguments
        v = VNone()
        for e in self._exps:
            v = e.eval(env)
        return v


class EWhile (Exp):

    def __init__ (self,cond,exp):
        self._cond = cond
        self._exp = exp

    def __str__ (self):
        return "EWhile({},{})".format(str(self._cond),str(self._exp))

    def eval (self,env):
        c = self._cond.eval(env)
        if c.type != "boolean":
            raise Exception ("Runtime error: while condition not a Boolean")
        while c.value:
            self._exp.eval(env)
            c = self._cond.eval(env)
            if c.type != "boolean":
                raise Exception ("Runtime error: while condition not a Boolean")
        return VNone()



    
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




############################################################
# CLASSES / OBJECTS
#

class EClass (Exp):
    
    def __init__ (self,params,fields,methods):
        self._params = params
        self._fields = fields
        self._methods = methods

    def __str__ (self):
        return "EClass([{}],[{}],[{}])".format(",".join(self._params),
                                               ",".join([ "({},{})".format(id,str(exp)) for (id,exp) in self._fields]),
                                               ",".join([ "({},{})".format(id,str(exp)) for (id,exp) in self._methods]))

    def eval (self,env):
        constructor = VClosure(self._params,
                               EObject(self._fields,
                                       [(id,EFunction(["this"],exp)) for (id,exp) in self._methods]),
                               env)
        return VClass(self._params,constructor)

class VClass (Value):

    def __init__ (self,params,constructor):
        self._params = params
        self._constructor = constructor
        self.type = "class"

    def __str__ (self):
        return "VClass([{}],{})".format(",".join(self._params),str(self._constructor))

    def instantiate (self,args):
        return self._constructor.apply(args)


class ENew (Exp):
    def __init__ (self,cls,args):
        self._class = cls
        self._args = args
        
    def __str__ (self):
        return "ENew({},[{}])".format(str(self._class),",".join([str(e) for e in self._args]))

    def eval (self,env):
        cls = self._class.eval(env)
        if cls.type != "class":
            raise Exception("Runtime error: new() on a non-class")
        args = [ e.eval(env) for e in self._args ]
        return cls.instantiate(args)


class EObject (Exp):
    
    def __init__ (self,fields,methods):
        self._fields = fields
        self._methods = methods
        
    def __str__ (self):
        return "EObject([{}],[{}])".format(",".join([ "({},{})".format(id,str(exp)) for (id,exp) in self._fields]),
                                           ",".join([ "({},{})".format(id,str(exp)) for (id,exp) in self._methods]))
    
    def eval (self,env):
        fields = [ (id,e.eval(env)) for (id,e) in self._fields]
        methods = [ (id,e.eval(env)) for (id,e) in self._methods]
        return VObject(fields,methods)
        
        
class VObject (Value):

    def __init__ (self,fields,methods):
        self.type = "object"
        self._fields = fields
        self._methods = methods
        self.env = fields + [ (id,v.apply([self])) for (id,v) in methods]
         # this is the mind bending bit
    
    def __str__ (self):
        return "<object {} {}>".format(",".join( id+":"+(str(v)) for (id,v) in self._fields),
                                       ",".join( id+":"+(str(v)) for (id,v) in self._methods))


class EWithObj (Exp):
    def __init__ (self,exp1,exp2):
        self._object = exp1
        self._exp = exp2
        
    def __str__ (self):
        return "EWithObj({},{})".format(str(self._object),str(self._exp))

    def eval (self,env):
        object = self._object.eval(env)
        if object.type != "object":
            raise Exception("Runtime error: expected an object")
        return self._exp.eval(object.env+env)







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
    #            ( let ( ( <name> <expr> ) ) <expr )
    #            ( function ( <name> ... ) <expr> )
    #            ( ref <expr> )
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

    pLET = "(" + Keyword("let") + "(" + pBINDINGS + ")" + pEXPR + ")"
    pLET.setParseAction(lambda result: ELet(result[3],result[5]))

    pCALL = "(" + pEXPR + pEXPRS + ")"
    pCALL.setParseAction(lambda result: ECall(result[1],result[2]))

    pFUN = "(" + Keyword("function") + "(" + pNAMES + ")" + pEXPR + ")"
    pFUN.setParseAction(lambda result: EFunction(result[3],result[5]))

    pFUNrec = "(" + Keyword("function") + pNAME + "(" + pNAMES + ")" + pEXPR + ")"
    pFUNrec.setParseAction(lambda result: EFunction(result[4],result[6],name=result[2]))

    pCLASS = "(" + Keyword("class") + "(" + pNAMES + ")" + Keyword("(") + pBINDINGS + ")" + Keyword("(") + pBINDINGS + Keyword(")") + ")"
    pCLASS.setParseAction(lambda result: EClass(result[3],result[6],result[9]))

    pNEW = "(" + Keyword("new") + pEXPR + pEXPRS +")"
    pNEW.setParseAction(lambda result: ENew(result[2],result[3]))

    pWITH = "(" + Keyword("with") + pEXPR + pEXPR +")"
    pWITH.setParseAction(lambda result: EWithObj(result[2],result[3]))

    pDO = "(" + Keyword("do") + pEXPRS + ")"
    pDO.setParseAction(lambda result: EDo(result[2]))

    pWHILE = "(" + Keyword("while") + pEXPR + pEXPR + ")"
    pWHILE.setParseAction(lambda result: EWhile(result[2],result[3]))

    pEXPR << (pINTEGER | pBOOLEAN | pIDENTIFIER | pIF | pLET | pFUN | pFUNrec| pCLASS |  pNEW | pWITH | pDO | pWHILE | pCALL)

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


def shell ():
    # A simple shell
    # Repeatedly read a line of input, parse it, and evaluate the result

    print "Lecture 6 - Obj Language (defun, define)"
    print "#quit to quit, #abs to see abstract representation"
    env = initial_env()

        
    while True:
        inp = raw_input("obj> ")

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
                env.insert(0,(result["name"],VClosure(result["param"],result["body"],env,name=result["name"])))
                print "{} defined".format(result["name"])

            elif result["result"] == "value":
                v = result["expr"].eval(env)
                env.insert(0,(result["name"],v))
                print "{} defined".format(result["name"])
                
        except Exception as e:
            print "Exception: {}".format(e)
        
# increase stack size to let us call recursive functions quasi comfortably
sys.setrecursionlimit(10000)



