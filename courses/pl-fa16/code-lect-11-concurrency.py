############################################################
# CONCUR
# S-expressions surface syntax
# recursive closures
# cps transformation
# 
# ALSO: concurrency!
#
# See #CONCUR comments for details
#
# The surface syntax for the concurrency extension is:
#
#   (run <expr>)           Evaluate <expr> in the presence of a
#                          scheduler so that it can use the
#                          concurrency operations 
#                       
#   (spawn <expr>)         Create a new thread to evaluate <expr>
#
#   (channel)              Create a communication channel
#
#   (send <expr1> <expr2>) Send value <expr2> on the channel described
#                          by <expr2>
#
#   (recv <expr>)          Receive a value on the channel described by
#                          <expr>
#
# This extension is inspired by CML (without select)



import sys
import time
import traceback



############################################################
# helper code to time the execution of a piece of code
#

class Timer(object):
    def __enter__(self):
        self.__start = time.time()
        return self

    def __exit__(self, type, value, traceback):
        # Error handling here
        self.__finish = time.time()

    def duration_in_seconds(self):
        return self.__finish - self.__start

    def time (self):
        return time.time() - self.__start




#
# The evaluation functions
#
#CONCUR
#
# The main evaluation function is eval_iter() that we saw before,
# which lets us do tail-call optimization.
#
# But the entry point for evaluation is actually eval_scheduler()
# (the various .eval() methods all call eval_scheduler)
#
# eval_scheduler() first checks if we're evaluating within a (run ...)
# by checking SUSPENDED_ACTIVE, that is, whether we're evaluating in
# the presence of a scheduler. If # not, it just calls eval_iter() 
#
# If we're evaluating in the presence of a scheduler, then we
# repeatedly call eval_iter() on everything we find in the
# SUSPENDED_QUEUE that the scheduler uses to queue up suspended
# processes. When all the processes have finished evaluating,
# eval_scheduler() returns. (It always returns value VNone.)
#
# 

SUSPENDED_QUEUE = []
SUSPENDED_RECV_QUEUE = []
SUSPENDED_SEND_QUEUE = []
SUSPENDED_ACTIVE = False



def eval_scheduler (exp,env):
    global SUSPENDED_QUEUE
    global SUSPENDED_SEND_QUEUE
    global SUSPENDED_RECV_QUEUE

    if not SUSPENDED_ACTIVE:
        return eval_iter(exp,env)

    SUSPENDED_QUEUE = []
    SUSPENDED_SEND_QUEUE = []
    SUSPENDED_RECV_QUEUE = []

    var = "__result{}__".format(fresh())
    susp = VClosure([],exp,env)
    while susp:
        res = eval_iter(ECall(EValue(susp),[]),env)
        susp = scheduler_next()

    return VNone()


#CONCUR
#
# This is the main scheduling function. It is invoked by
# eval_scheduler() when a processes finishes evaluating and there's
# other processes to evaluate, or when the evaluator encounters a send
# or a receive. It returns the next process (stored as a closure which
# is really a continuation) waiting to be resumed if there is one, or
# it looks through the queue of processes blocked on a send or recv to
# see whether we have a send and recv that are waiting to synchronize
# on the same channel. If so, the continuation of the send process is
# put in the SUSPENDED_QUEUE (where it will be eventually resumed by
# the scheduler) and the continuation of the recv process is returned
# for evaluation.
#

def scheduler_next ():
    global SUSPENDED_QUEUE
    global SUSPENDED_SEND_QUEUE
    global SUSPENDED_RECV_QUEUE

    # if we can run something, go
    if SUSPENDED_QUEUE:
        exp = SUSPENDED_QUEUE[0]
        SUSPENDED_QUEUE = SUSPENDED_QUEUE[1:]
         return exp
    
    # if there's a send/recv that match, fire them up
    if SUSPENDED_SEND_QUEUE or SUSPENDED_RECV_QUEUE:
        for (i,(send_name,v,send_clos)) in enumerate(SUSPENDED_SEND_QUEUE):
            for (j,(recv_name,recv_clos_f)) in enumerate(SUSPENDED_RECV_QUEUE):
                if send_name == recv_name:
                    # match!
                    #CONCUR : just so that we see when we match a send
                    #         and recv
                    print "[match on channel {} with value {}]".format(send_name,v)
                    SUSPENDED_SEND_QUEUE = SUSPENDED_SEND_QUEUE[:i]+SUSPENDED_SEND_QUEUE[i+1:]
                    SUSPENDED_RECV_QUEUE = SUSPENDED_RECV_QUEUE[:j]+SUSPENDED_RECV_QUEUE[j+1:]
                    SUSPENDED_QUEUE.append(send_clos)
                    return recv_clos_f(v)
        raise Exception("Deadlock!")
                
    return None


def eval_iter (exp,env):
    current_exp = exp
    current_env = env
    while True:
        # these are all the special forms which directly
        # return the result of evaluating an expression
        if current_exp.expForm == "ECall":

            f = eval_iter(current_exp._fun,current_env)
            if f.type != "function":
                raise Exception("Runtime error: trying to call a non-function")
            args = [ eval_iter(e,current_env) for e in current_exp._args]
            if len(args) != len(f._params):
                raise Exception("Runtime error: argument # mismatch in call")
            new_env = f._env + zip(f._params,args)
            current_exp = f._body
            current_env = new_env

        elif current_exp.expForm == "EIf":

            v = eval_iter(current_exp._cond,current_env)
            if v.type != "boolean":
                raise Exception ("Runtime error: condition not a Boolean")
            if v.value:
                current_exp = current_exp._then
            else:
                current_exp = current_exp._else

        elif current_exp.expForm == "EValue":

            return current_exp._value

        elif current_exp.expForm == "EPrimCall":

            vs = [ eval_iter(e,current_env) for e in current_exp._exps ]
            return apply(current_exp._prim,vs)

        elif current_exp.expForm == "EId":

            for (id,v) in reversed(current_env):
                if current_exp._id == id:
                    return v
            raise Exception("Runtime error: unknown identifier {}".format(current_exp._id))

        elif current_exp.expForm == "EFunction":
            
            return VClosure(current_exp._params,current_exp._body,current_env,current_exp._name)


        #CONCUR
        #
        # to evaluate an ERun, set the flag that we're entering
        # evaluation under a scheduler, and invoke eval_scheduler()
        # on the body of ERun
        #
        elif current_exp.expForm == "ERun":

            global SUSPENDED_ACTIVE
            
            SUSPENDED_ACTIVE = True
            eval_scheduler(current_exp._body,current_env)
            SUSPENDED_ACTIVE = False
            current_exp = ECall(current_exp._kont,[EValue(VNone())])

        #CONCUR
        #
        # to evaluate an ESpawn, we simply put the body of the ESpawn
        # in the SUSPENDED_QUEUE and the scheduler will eventually get
        # to evaluate it. Continue evaluating the continuation of the
        # ESpawn
        #
        elif current_exp.expForm == "ESpawn":

            susp = VClosure([],current_exp._body,current_env)
            SUSPENDED_QUEUE.append(susp)
            current_exp = ECall(current_exp._kont,[EValue(VNone())])

        #CONCUR
        #
        # to evaluate an ESend, we put the channel name and value of
        # the ESend on the SUSPENDED_SEND_QUEUE along with the ESend
        # continuation, and ask the scheduler for another process
        # to continue evaluating
        # 
        elif current_exp.expForm == "ESend":
            
            ch = eval_iter(current_exp._chan,current_env)
            v = eval_iter(current_exp._exp,current_env)
            kont = eval_iter(current_exp._kont,current_env)
            SUSPENDED_SEND_QUEUE.append((ch.name,v,VClosure([],ECall(EValue(kont),[EValue(VNone())]),current_env)))
            next_exp = scheduler_next()
            if next_exp:
                current_exp = ECall(EValue(next_exp),[])
            else:
                return VNone()

        #CONCUR
        #
        # to evaluate an ERecv, we put the channel name of 
        # the ERecv on the SUSPENDED_RECV_QUEUE along with the ERecv
        # continuation, and ask the scheduler for another process
        # to continue evaluating
        # 
        elif current_exp.expForm == "ERecv":
            
            ch = eval_iter(current_exp._chan,current_env)
            kont = eval_iter(current_exp._kont,current_env)
            evkont = EValue(kont)
            SUSPENDED_RECV_QUEUE.append((ch.name,lambda v:VClosure([],ECall(evkont,[EValue(v)]),current_env)))
            next_exp = scheduler_next()
            if next_exp:
                current_exp = ECall(EValue(next_exp),[])
            else:
                return VNone()

        else:

            raise Exception("Unrecognized expression form: {}".format(current_exp.expForm))



#
# Expressions
#

class Exp (object):
    pass

    
class EValue (Exp):
    # Value literal (could presumably replace EInteger and EBoolean)
    def __init__ (self,v):
        self._value = v
        self.expForm = "EValue"
        self.is_basic = True
    
    def __str__ (self):
        return "EValue({})".format(self._value)

    def eval (self,env):
        return eval_scheduler(self,env)

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
        self.is_basic = True

    def __str__ (self):
        return "EPrimCall(<prim>,[{}])".format(",".join([ str(e) for e in self._exps]))

    def eval (self,env):
        return eval_scheduler(self,env)

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
        self.is_basic = False

    def __str__ (self):
        return "EIf({},{},{})".format(self._cond,self._then,self._else)

    def eval (self,env):
        return eval_scheduler(self,env)

    def cps (self,k):
        if self._cond.is_basic:
            return EIf(self._cond,self._then.cps(k),self._else.cps(k))
        var = " _x{}_".format(fresh())
        return self._cond.cps(EFunction([var],EIf(EId(var),self._then.cps(k),self._else.cps(k))))

    
class EId (Exp):
    # identifier

    def __init__ (self,id):
        self._id = id
        self.expForm = "EId"
        self.is_basic = True

    def __str__ (self):
        return "EId({})".format(self._id)

    def eval (self,env):
        return eval_scheduler(self,env)

    def cps (self,k):
        return ECall(k,[self])



class ECall (Exp):
    # function call

    def __init__ (self,fun,exps):
        self._fun = fun
        self._args = exps
        self.expForm = "ECall"
        self.is_basic = False

    def __str__ (self):
        return "ECall({},[{}])".format(str(self._fun),",".join([str(e) for e in self._args]))

    def eval (self,env):
        return eval_scheduler(self,env)

    def cps (self,k):
        vars_e = [ " _x{}_".format(fresh()) for e in self._args ]
        var_f = " _x{}_".format(fresh())
        args = []
        cont_args = []
        cont_exps = []
        if self._fun.is_basic:
            fun = self._fun
        else:
            if self._fun.expForm == "EFunction":
                fun = self._fun.cps_in_place()
            else:
                fun = EId(var_f)
                cont_args.append(var_f)
                cont_exps.append(self._fun)
        for (var,e) in zip(vars_e,self._args):
            if e.is_basic:
                args.append(e)
            else:
                if e.expForm == "EFunction":
                    args.append(e.cps_in_place())
                else:
                    args.append(EId(var))
                    cont_args.append(var)
                    cont_exps.append(e)
        result = ECall(fun,args + [k])
        for (var,exp) in reversed(zip(cont_args,cont_exps)):
            result = exp.cps(EFunction([var],result))
        return result



class EFunction (Exp):
    # Creates an anonymous function

    def __init__ (self,params,body,name=None):
        self._params = params
        self._body = body
        self._name = name
        self.expForm = "EFunction"
        self.is_basic = False

    def __str__ (self):
        return "EFunction([{}],{})".format(",".join(self._params),str(self._body))

    def eval (self,env):
        return eval_scheduler(self,env)

    def cps (self,k):
        var = " _k_{}_".format(fresh())
        return ECall(k,[EFunction([ p for p in self._params] + [var],self._body.cps(EId(var)),name=self._name)])

    # hack to get functions to compile faster...
    def cps_in_place (self):
        var = " _k_{}_".format(fresh())
        return EFunction([ p for p in self._params] + [var],self._body.cps(EId(var)),name=self._name)
    
        
    
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
        self._env = env + extra
        self.type = "function"

    def __str__ (self):
        return "<function [{}] {}>".format(",".join(self._params),str(self._body))

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




#CONCUR
#
# new values and expressions for concurrency
#
# ESpawn, ERun, ESend, ERecv are expressions
# because they have special evaluation rules
# that interact with the scheduler


class VChannel (Value):

    def __init__ (self):
        self.type = "channel"
        self.name = "chan{}".format(fresh())

    def __str__ (self):
        return "<channel {}>".format(self.name)

def oper_channel ():
    return VChannel()


class ESpawn (Exp):

    def __init__ (self,e,kont=None):
        self._body = e
        self._kont = kont
        self.expForm = "ESpawn"
        self.is_basic = False

    def __str__ (self):
        return "ESpawn({},{})".format(self._body,self._kont if self._kont else "None")

    def eval (self,env):
        return eval_scheduler(self,env)

    def cps (self,k):
        var = " _x{}_".format(fresh())
        body = self._body.cps(EFunction([var],EValue(VNone())))
        return ESpawn(body,k)

    
class ERun (Exp):

    def __init__ (self,e,kont=None):
        self._body = e
        self._kont = kont
        self.expForm = "ERun"
        self.is_basic = False

    def __str__ (self):
        return "ERun({},{})".format(self._body,self._kont if self._kont else "None")

    def eval (self,env):
        return eval_scheduler(self,env)

    def cps (self,k):
        var = " _x{}_".format(fresh())
        body = self._body.cps(EFunction([var],EValue(VNone())))
        return ERun(body,k)

class ESend (Exp):

    def __init__ (self,ch,e,kont=None):
        self._chan = ch
        self._exp = e
        self._kont = kont
        self.expForm = "ESend"
        self.is_basic = False

    def __str__ (self):
        return "ESend({},{},{})".format(self._chan,self._exp,self._kont if self._kont else "None")

    def eval (self,env):
        return scheduler_eval(self,env)

    # non-optimized translation
    def cps (self,k):
        var1 = " _k{}_".format(fresh())
        var2 = " _k{}_".format(fresh())
        result = ESend(EId(var1),EId(var2),k)
        result = self._exp.cps(EFunction([var2],result))
        result = self._chan.cps(EFunction([var1],result))
        return result

class ERecv (Exp):

    def __init__ (self,ch,kont=None):
        self._chan = ch
        self._kont = kont
        self.expForm = "ERecv"
        self.is_basic = False

    def __str__ (self):
        return "ERecv({},{})".format(self._chan,self._kont if self._kont else "None")

    def eval (self,env):
        return scheduler_eval(self,env)

    # non-optimized translation
    def cps (self,k):
        var1 = " _k{}_".format(fresh())
        result = ERecv(EId(var1),k)
        result = self._chan.cps(EFunction([var1],result))
        return result



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


    #CONCUR : the next four rules let you parse
    #   (run <expr>)
    #   (spawn <expr>)
    #   (send <expr> <expr>)
    #   (recv <expr>)
    # The final function introduced by the concurrency extension is
    #   (channel)
    # which is just a primitive operation in the initial environment
    
    pRUN= "(" + Keyword("run") + pEXPR + ")"
    pRUN.setParseAction(lambda result: ERun(result[2]))

    pSPAWN = "(" + Keyword("spawn") + pEXPR + ")"
    pSPAWN.setParseAction(lambda result: ESpawn(result[2]))

    pSEND = "(" + Keyword("send") + pEXPR + pEXPR + ")"
    pSEND.setParseAction(lambda result: ESend(result[2],result[3]))

    pRECV = "(" + Keyword("recv") + pEXPR + ")"
    pRECV.setParseAction(lambda result: ERecv(result[2]))

    pEXPR << (pINTEGER | pBOOLEAN | pIDENTIFIER | pIF | pLET | pFUN | pFUNrec| pDO | pWHILE | pSPAWN | pSEND | pRECV | pRUN | pCALL)

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



def add_binding (name,value,env):
    return env + [(name,value)]


def initial_env_cps ():
    env = []
    env = add_binding("+",
                VClosure(["x","y","k"],
                         ECall(EId("k"),[EPrimCall(oper_plus,[EId("x"),EId("y")])]),
                         env),
                env)
    env = add_binding("-",
                VClosure(["x","y","k"],
                         ECall(EId("k"),[EPrimCall(oper_minus,[EId("x"),EId("y")])]),
                         env),
                env)
    env = add_binding("*",
                VClosure(["x","y","k"],
                         ECall(EId("k"),[EPrimCall(oper_times,[EId("x"),EId("y")])]),
                         env),
                env)
    env = add_binding("zero?",
                VClosure(["x","k"],
                         ECall(EId("k"),[EPrimCall(oper_zero,[EId("x")])]),
                         env),
                env)
    env = add_binding("ref",
                VClosure(["x","k"],
                         ECall(EId("k"),[EPrimCall(oper_ref, [EId("x")])]),
                         env),
                env)
    env = add_binding("deref",
                VClosure(["x","k"],
                         ECall(EId("k"),[EPrimCall(oper_deref, [EId("x")])]),
                         env),
                env)
    env = add_binding("update!",
                VClosure(["x","y","k"],
                         ECall(EId("k"),[EPrimCall(oper_update,[EId("x"),EId("y")])]),
                         env),
                env)
    env = add_binding("print!",
                VClosure(["x","k"],
                         ECall(EId("k"),[EPrimCall(oper_print,[EId("x")])]),
                         env),
                env)

    #CONCUR : the channel primitive function that creates a channel
    env = add_binding("channel",
                      VClosure(["k"],
                               ECall(EId("k"),[EPrimCall(oper_channel,[])]),
                               env),
                      env)
    return env




def shell ():
    # A simple shell
    # Repeatedly read a line of input, parse it, and evaluate the result

    print "Lecture 11 - CONCUR Language (defun, define, CPS, concurrency)"
    print "#quit to quit, #abs to see abstract representation"
    env = initial_env_cps()
        
    while True:
        inp = raw_input("concur> ")

        try:
            result = parse(inp)

            if result["result"] == "expression":
                exp = result["expr"]
                with Timer() as timer:
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
                env = add_binding(result["name"],VClosure([ p for p in result["params"]] + [var],
                                                          result["body"].cps(EId(var)),
                                                          env,
                                                          name=result["name"]),
                                  env)
                print "{} defined".format(result["name"])
                
            elif result["result"] == "value":
                exp = result["expr"]
                cps_exp = exp.cps(EFunction(["x"],EId("x")))
                v = cps_exp.eval(env)
                env = add_binding(result["name"],v,env)
                print "{} defined".format(result["name"])
                
        # except Exception as e:
        except IOError as e:
            print "Exception: {}".format(e)


