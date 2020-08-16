// LECTURE 10 - compiler (with static analysis for IDs)
//

/*

To run this code in the shell, you need to provide a pointer to the
parser combinators library:

 scala-parser-combinators_2.13-1.1.2.jar

The easiest way to do that is to start the shell with:

  scala -classpath scala-parser-combinators_2.13-1.1.2.jar -Xnojline

(assuming scala-parser-combinators_2.13-1.1.2.jar is in the
the folder where you start the shall -- if not, you need to
provide a path to the .jar file -- standard stuff)

The -Xnojline makes it possible to run shell() without getting
odd behavior
 
*/


import scala.util.parsing.combinator._;
import scala.io.Source


/*
 *  Function to create a fresh identifier
 *  (that cannot be expressed at the surface syntax leve)
 *
 */

val gensym: () => String = {
  var counter = 0
  () => { 
    val next = counter
    counter += 1
    " _gs_" + next.toString()
  }
}


def runtimeError (msg: String): Nothing = {
  throw new Exception("Runtime error: "+msg)
}


/*
 *  Values
 *
 */


abstract class Value {

  // default behaviors for values
  
  def isInteger (): Boolean = false
  def isBoolean (): Boolean = false
  def isFraction (): Boolean = false
  def isVector (): Boolean = false
  def isFunction (): Boolean = false
  def isRefCell (): Boolean = false

  def toDisplay (): String

  def getInt (): Int = {
    runtimeError("Value " + this.toString() + " is not of type INTEGER")
  }

  def getBool (): Boolean = {
    runtimeError("Value " + this.toString() + " is not of type BOOLEAN")
  }

  def getDenominator (): Int = {
    runtimeError("Value " + this.toString() + " is not of type FRACTION")
  }

  def getNumerator (): Int = {
    runtimeError("Value " + this.toString() + " is not of type FRACTION")
  }

  def getList (): List[Value] = {
    runtimeError("Value " + this.toString() + " is not of type VECTOR")
  }

  def getRefContent (): Value = {
    runtimeError("Value " + this.toString() + " is not of type REFCELL")
  }
  
  def putRefContent (v:Value): Unit = {
    runtimeError("Value " + this.toString() + " is not of type REFCELL")
  }

  def apply (vs: List[Value]): Value = {
    runtimeError("Value " + this.toString() + " is not of type FUNCTION")
  }

   def toCodeValue () : CodeValue =
      throw new Exception("Cannot convert to code value: "+this)
}


class VInteger (val i:Int) extends Value {

  override def toString (): String =
    "VInteger[" + i.toString() + "]"

  def toDisplay (): String =
    i.toString()

  override def isInteger (): Boolean = true
  override def isFraction (): Boolean = true
  override def getInt (): Int = i
  override def getNumerator (): Int = i
  override def getDenominator (): Int = 1

  override def toCodeValue () : CodeValue = CVInteger(i)
}


class VBoolean (val b:Boolean) extends Value {

  override def toString (): String =
    "VBoolean[" + b.toString() + "]"

  def toDisplay (): String =
    if (b) "true" else "false"

  override def isBoolean (): Boolean = true
  override def getBool (): Boolean = b
  
  override def toCodeValue () : CodeValue = CVInteger(if (b) 1 else 0)
}


class VFraction (val numerator:Int, val denominator:Int) extends Value {

  override def toString (): String =
    "VFraction[" + numerator.toString() + ", " + denominator.toString() + "]"

  def toDisplay (): String =
    numerator.toString() + "/" + denominator.toString()

  def gcd (n: Int, m: Int): Int = {
    if (m == 0) { 
      return n
    } else if (n == 0) { 
      return m
    } else if (m > n) { 
      return gcd (m % n,n)
    } else { 
      return gcd (m, n % m)
    }
  }

  def sign (n: Int): Int = if (n < 0) -1 else 1
  def abs (n: Int): Int = if (n < 0) -n else n

  def simplify (): Value = {
    val s = sign(numerator) * sign(denominator)
    val g = gcd(abs(numerator), abs(denominator))
    val sn = abs(numerator) / g
    val sd = abs(denominator) / g
    if (sd == 1) new VInteger(s * sn) else new VFraction(s * sn, sd)    
  }

  override def isFraction (): Boolean = true
  override def getNumerator (): Int = numerator
  override def getDenominator (): Int = denominator
}


class VVector (val l: List[Value]) extends Value {

  override def toString (): String = 
     l.addString(new StringBuilder(), "VVector[", ", ", "]").toString()

  def toDisplay (): String =
     l.map((v:Value) => v.toDisplay()).addString(new StringBuilder(), "(", " ", ")").toString()

  override def isVector (): Boolean = true
  override def getList (): List[Value] = l

//  override def toCodeValue () : CodeValue = CVVector(l.map((v) => v.toCodeValue()))
}


class VRefCell (val init: Value) extends Value {

  var content = init
  
  override def toString (): String =
    "VRefCell[" + content.toString() + "]"
    
  def toDisplay (): String =
    "#REF[" + content.toDisplay() + "]"
    
  override def isRefCell (): Boolean = true

  override def getRefContent (): Value = content
  override def putRefContent (v:Value): Unit = {
    content = v
  }

}


class VPrimitive (val oper: (List[Value]) => Value) extends Value {

  override def toString (): String = 
    "VPrimitive[" + Integer.toHexString(oper.hashCode()) + "]"

  def toDisplay (): String =
    "#PRIMITIVE[" + Integer.toHexString(oper.hashCode()) + "]"

  override def isFunction (): Boolean = true
  override def apply (args: List[Value]): Value =
     oper(args)
}


class VRecClosure (val self: String, val params: List[String], val body:Exp, val env:Env[Value]) extends Value {

  override def toString (): String =
    "VRecClosure[" + self + ", " + params + ", " + body.toString() + ", " + env.toString() + "]"

   def toDisplay (): String =
    "#CLOSURE[" + Integer.toHexString(hashCode()) + "]"

  override def isFunction (): Boolean = true

  override def apply (args: List[Value]): Value = {
     if (params.length != args.length) {
        runtimeError("wrong number of arguments\n  Function " + this.toString())
     }
     var new_env = env
     for ((p,v) <- params.zip(args)) {
        new_env = new_env.push(p,v)
     }
     
     // push the current closure as the value bound to identifier self
     new_env = new_env.push(self,this)
     return body.eval(new_env)
  }
}


/*
 *  Expressions
 *
 */

class Env[A] (val content: List[(String, A)]) {

  def this () = {
    this(List())
  }
      
  override def toString () : String = {
    val cs = content.map((p) => p._1 + " <- " + p._2.toString())
    return cs.addString(new StringBuilder(), "{", ", ", "}").toString()
  }

  def push (id : String, v : A) : Env[A] =
    new Env((id,v)::content)

  def lookup (id : String) : A = {
    for (entry <- content) {
      if (entry._1 == id) {
        return entry._2
      }
    }
    runtimeError("unknown identifier " + id)
  }
  
  def findIndex (id:String) : Int = {
    var idx = 0
    for (entry <- content) {
      if (entry._1 == id) 
        return idx
      idx = idx + 1
    }
    runtimeError("unknown identifier " + id)
  }

  def lookupByIndex (idx: Int): A = {
    content(idx)._2
  }
}


abstract class Exp {

  def eval (env: Env[Value]): Value

  def analyzeIds (env: Env[Unit]): Unit

  def compile (addr: Int): Vector[Opcode]
  def compileTail (addr: Int): Vector[Opcode] =
    // by default, tail compilation is compilation
    compile(addr)
}


class EInteger (val i:Int) extends Exp {
  // integer literal

  override def toString (): String =
    "EInteger[" + i.toString() + "]"

  def eval (env: Env[Value]): Value =
    new VInteger(i)

  def analyzeIds (env: Env[Unit]): Unit = ()

  def compile (addr:Int) : Vector[Opcode] = 
    Vector(Op_PUSH_INT(i))
}


class EBoolean (val b:Boolean) extends Exp {
  // Boolean literal

  override def toString (): String =
    "EBoolean[" + b.toString() + "]"

  def eval (env: Env[Value]): Value =
    new VBoolean(b)
        
  def analyzeIds (env: Env[Unit]): Unit = ()

  def compile (addr:Int) : Vector[Opcode] = 
    Vector(Op_PUSH_INT(if (b) 1 else 0))
}


class EPrimitive (val oper: (List[Value]) => Value) extends Exp { 

  override def toString (): String = 
    "EPrimitive[" + Integer.toHexString(oper.hashCode()) + "]"

  def eval (env: Env[Value]): Value =
    new VPrimitive(oper)

  def analyzeIds (env: Env[Unit]): Unit = ()

  def compile (addr: Int): Vector[Opcode] =
    runtimeError("Cannot compile EPrimitive")
}
 

class EIf (val ec: Exp, val et: Exp, val ee: Exp) extends Exp {
  // Conditional expression

  override def toString (): String =
    "EIf[" + ec.toString() + ", " + et.toString() + ", " + ee.toString() + "]"

  def eval (env: Env[Value]): Value = {
    val ev = ec.eval(env)
    if (ev.isBoolean()) {
      if (!ev.getBool()) {
        return ee.eval(env)
      } else {
        return et.eval(env)
      }
    } else {
      runtimeError("condition not a Boolean")
    }
  }

  def analyzeIds (env: Env[Unit]): Unit = {
    ec.analyzeIds(env)
    et.analyzeIds(env)
    ee.analyzeIds(env)
  }

  def compile (addr:Int) : Vector[Opcode] = {
    val ccond = ec.compile(addr)
    val celse = ee.compile(addr + ccond.length + 2)
    val then_part = addr + ccond.length + 2 + celse.length + 2
    val cthen = et.compile(then_part)
    val done_part = then_part + cthen.length
    return (ccond ++ Vector(Op_PUSH_ADDR(then_part),Op_JUMP_TRUE()) ++
            celse ++ Vector(Op_PUSH_ADDR(done_part),Op_JUMP()) ++
            cthen)
    }

  override def compileTail (addr:Int) : Vector[Opcode] = {
    val ccond = ec.compile(addr)
    val celse = ee.compileTail(addr + ccond.length + 2)
    val then_part = addr + ccond.length + 2 + celse.length + 2
    val cthen = et.compileTail(then_part)
    val done_part = then_part + cthen.length
    return (ccond ++ Vector(Op_PUSH_ADDR(then_part),Op_JUMP_TRUE()) ++
            celse ++ Vector(Op_PUSH_ADDR(done_part),Op_JUMP()) ++
            cthen)
    }

}


class EId (val id: String) extends Exp {

  override def toString (): String =
    "EId[" + id + "]"

  def eval (env: Env[Value]): Value =
    env.lookup(id)

  var index = -1

  def analyzeIds (env: Env[Unit]): Unit =
    index = env.findIndex(id)

  def compile (addr:Int) : Vector[Opcode] =
    Vector(Op_LOOKUP(index))
}


class EApply (val fn: Exp, val args: List[Exp]) extends Exp {

  override def toString (): String = {
    if (args.isEmpty) {
      return "EApply[" + fn + "]"
    } else { 
      val sArgs = args.map(_.toString()).addString(new StringBuilder(), "", ", ", "").toString()
      return "EApply[" + fn + ", " + sArgs + "]"
    }
  }

  def eval (env: Env[Value]): Value = {
    val vfn = fn.eval(env)
    val vargs = args.map((e:Exp) => e.eval(env))
    return vfn.apply(vargs)
  }
  
  def analyzeIds (env: Env[Unit]): Unit = {
    fn.analyzeIds(env)
    for (arg <- args)
      arg.analyzeIds(env)
  }
  
  def compile (addr:Int) : Vector[Opcode] = {
     var code = Vector() : Vector[Opcode] 
     var caddr = addr+2
     for (e <- args.reverse) {
        val ce = e.compile(caddr)
        code = code ++ ce
        caddr += ce.length
     }
     val cfn = fn.compile(caddr)
     code = code ++ cfn
     caddr += cfn.length
     return Vector(Op_PUSH_ADDR(caddr + 2),
                   Op_PUSH_ENV()) ++
            code ++
            Vector(Op_OPEN(),
                   Op_JUMP())
  }
  
  override def compileTail (addr:Int) : Vector[Opcode] = {
     var code = Vector() : Vector[Opcode]
     var caddr = addr
     for (e <- args.reverse) {
        val ce = e.compile(caddr)
        code = code ++ ce
        caddr += ce.length
     }
     val cfn = fn.compile(caddr)
     code = code ++ cfn
     caddr += cfn.length
     return code ++
            Vector(Op_OPEN(),
                   Op_JUMP())
                           
  }
}


class EFunction (val recName: String, val params: List[String], val body: Exp) extends Exp {

  override def toString (): String = {
    val ps = params.addString(new StringBuilder(), "(", ", ", ")").toString()
    "EFunction[" + recName + ", " + ps + ", " + body.toString() + "]"
  }
  
  def eval (env: Env[Value]): Value = 
    new VRecClosure(recName, params, body, env)

  def analyzeIds (env: Env[Unit]): Unit = {
    var newEnv = env.push(recName, ())
    for (p <- params)
      newEnv = newEnv.push(p, ())
    body.analyzeIds(newEnv)
  }
    
  def compile (addr:Int) : Vector[Opcode] = {
     var cfun = Vector[Opcode]()
     cfun = Vector( Op_PUSH_ADDR(addr+4),
                    Op_CLOSURE(),
                    Op_ADD_ENV()) ++
            params.map((n) => Op_ADD_ENV()).toVector ++
            body.compileTail(addr+7+params.length) ++
            Vector( Op_SWAP(),
	            Op_ENV(),
		    Op_SWAP(),
                    Op_JUMP())
     val fun_part = addr+4
     val done_part = addr+4+cfun.length
     return Vector(Op_PUSH_ADDR(fun_part),
     Op_CLOSURE(),
     Op_PUSH_ADDR(done_part),
     Op_JUMP()) ++ cfun
  }
}  

class ELet (val bindings: List[(String, Exp)], val body: Exp) extends Exp {

  override def toString (): String = {
    val bs1 = bindings.map((p) => "(" + p._1 + ", " + p._2.toString() + ")")
    val bs2 = bs1.addString(new StringBuilder(), "", ", ", "").toString()
    "ELet[" + bs2 + ", " + body.toString() + "]"
  }

  def eval (env: Env[Value]): Value = {
    var newEnv = env
    for (binding <- bindings) {
      val v = binding._2.eval(env)
      newEnv = newEnv.push(binding._1, v)
    }
    return body.eval(newEnv)
  }

  def analyzeIds (env: Env[Unit]): Unit = {
    var newEnv = env
    for ((n, e) <- bindings) {
      e.analyzeIds(env)
      newEnv = newEnv.push(n, ())
    }
    body.analyzeIds(newEnv)  
  }

  def compile (addr:Int) : Vector[Opcode] = {
     var result = Vector() : Vector[Opcode]
     var caddr = addr
     for ((n, e) <- bindings.reverse) {
        val c = e.compile(caddr)
        result = result ++ c
        caddr += c.length
     }
     result = result ++ bindings.map((p) => Op_ADD_ENV()).toVector
     caddr = addr + result.length
     result = result ++ body.compile(caddr)
     return result
  }

  override def compileTail (addr:Int) : Vector[Opcode] = {
     var result = Vector() : Vector[Opcode]
     var caddr = addr
     for ((n, e) <- bindings.reverse) {
        val c = e.compile(caddr)
        result = result ++ c
        caddr += c.length
     }
     result = result ++ bindings.map((p) => Op_ADD_ENV()).toVector
     caddr = addr + result.length
     result = result ++ body.compileTail(caddr)
     return result
  }
}



/*
 *  PARSER FOR S-EXPRESSIONS
 *
 */


class SExpParser extends RegexParsers { 

   // tokens
   
   def LP: Parser[Unit] = "(" ^^ { s => () }
   def RP: Parser[Unit] = ")" ^^ { s => () }
   def INT: Parser[Int] = """-?[0-9]+""".r ^^ { s => s.toInt }
   def IF: Parser[Unit] = "if" ^^ { s => () }
   def FUN: Parser[Unit] = "fun" ^^ { s => () }
   def LET: Parser[Unit] = "let" ^^ { s => () }
   def ID: Parser[String] = """[a-zA-Z_*+=</?-][a-zA-Z0-9_*+=</?-]*""".r ^^ { s => s }

   // transformation tokens
   def DO: Parser[Unit] = "do" ^^ { s => () } 


   // grammar

   def atomic_int: Parser[Exp] = INT ^^ { i => new EInteger(i) }

   def atomic_id: Parser[Exp] =
      ID ^^ { s => new EId(s) }

   def atomic: Parser[Exp] =
      ( atomic_int | atomic_id ) ^^ { e => e}

   def expr_if: Parser[Exp] =
      LP ~ IF ~ expr ~ expr ~ expr ~ RP ^^
        { case _ ~ _ ~ e1 ~ e2 ~ e3 ~ _ => new EIf(e1,e2,e3) }

   def expr_let: Parser[Exp] =
     LP ~ LET ~ LP ~ bindings ~ RP ~ expr ~ RP ^^
       { case _ ~ _ ~ _ ~ bindings ~ _ ~ e2 ~ _ => new ELet(bindings, e2) }

  def bindings: Parser[List[(String, Exp)]] =
    ( bindings_many | bindings_none ) ^^ { bs => bs }

  def bindings_many: Parser[List[(String, Exp)]] =
    LP ~ ID ~ expr ~ RP ~ bindings ^^ { case _ ~ s ~ e ~ _ ~ bs => (s, e) :: bs }

  def bindings_none: Parser[List[(String, Exp)]] =
    success(()) ^^ { case _  => List() }

  def mkDo (es: List[Exp]): Exp = {
    if (es.isEmpty) { 
      return new EBoolean(false)
    }
    val ess = es.reverse
    var result = ess.head
    for (e <- ess.tail) { 
      result = new ELet(List((gensym(), e)), result)
    }
    return result
  }

  def expr_fun: Parser[Exp] =
    LP ~ FUN ~ LP ~ params ~ RP ~ expr ~ RP ^^
      { case _ ~ _ ~ _ ~ ps ~ _ ~ e ~ _ => new EFunction(gensym(), ps, e) }

  def expr_rec_fun: Parser[Exp] =
    LP ~ FUN ~ ID ~ LP ~ params ~ RP ~ expr ~ RP ^^
      { case _ ~ _ ~ rn ~ _ ~ ps ~ _ ~ e ~ _ => new EFunction(rn, ps, e) }

  def params: Parser[List[String]] =
    ( params_many | params_one ) ^^ { ps => ps }

  def params_many: Parser[List[String]] =
    ID ~ params ^^ { case p ~ ps => p::ps }

  def params_one: Parser[List[String]] =
    ID ^^ { p => List(p) }

  def exprs: Parser[List[Exp]] = 
      ( exprs_many | exprs_none ) ^^ { es => es }

  def exprs_many: Parser[List[Exp]] = 
      expr ~ exprs ^^ { case e ~ es => e :: es }

  def exprs_none: Parser[List[Exp]] = 
      success(()) ^^ { case _ => List() }

  def expr_do: Parser[Exp] = 
    LP ~ DO ~ exprs ~ RP ^^ { case _ ~ _ ~ es ~ _ => mkDo(es) } 

  def expr_transf: Parser[Exp] =
    ( expr_do  ) ^^ { e => e }
          
  def expr_apply: Parser[Exp] =
    LP ~ expr ~ exprs ~ RP ^^ { case _ ~ f ~ es ~ _ => new EApply(f, es) }

  def expr: Parser[Exp] =
     ( atomic | expr_if | expr_fun | expr_rec_fun | expr_let |
       expr_transf | expr_apply ) ^^ { e => e }
}

def parse (input:String):Exp = {
   val p = new SExpParser
   p.parseAll(p.expr, input) match {
      case p.Success(result,_) => result
      case failure: p.NoSuccess => runtimeError("Cannot parse "+input+": "+failure.msg)
   }  
}


/*
 *  Primitive operations
 *
 */

def checkNumberArgs (vs:List[Value], num: Int): Unit = {
  if (vs.length != num) {
    runtimeError("Wrong number of arguments " + vs.length.toString() + " - expected " + num.toString())
  }
}

def checkInteger (v: Value): Unit = {
  if (!v.isInteger()) {
    runtimeError("Value " + v.toString() + " is not of type INTEGER")
  }
}

def checkFraction (v: Value): Unit = {
  if (!v.isFraction()) {
    runtimeError("Value " + v.toString() + " is not of type FRACTION")
  }
}

def checkBoolean (v: Value): Unit = {
  if (!v.isBoolean()) {
    runtimeError("Value " + v.toString() + " is not of type BOOLEAN")
  }
}

def checkVector (v: Value): Unit = {
  if (!v.isVector()) {
    runtimeError("Value " + v.toString() + " is not of type VECTOR")
  }
}

def checkFunction (v: Value): Unit = {
  if (!v.isFunction()) {
    runtimeError("Value " + v.toString() + " is not of type FUNCTION")
  }
}

def checkRefCell (v: Value): Unit = {
  if (!v.isRefCell()) {
    runtimeError("Value " + v.toString() + " is not of type REFCELL")
  }
}

def operMinus (vs: List[Value]): Value = { 
  checkNumberArgs(vs, 1)
  val v1 = vs(0)
  checkFraction(v1)
  return new VFraction(-v1.getNumerator(), v1.getDenominator()).simplify()
}

def operPlus (vs: List[Value]): Value = { 
  checkNumberArgs(vs, 2)
  val v1 = vs(0)
  val v2 = vs(1)
  checkFraction(v1)
  checkFraction(v2)
  val n1 = v1.getNumerator()
  val n2 = v2.getNumerator()
  val d1 = v1.getDenominator()
  val d2 = v2.getDenominator()
  return new VFraction(n1 * d2 + n2 * d1, d1 * d2).simplify()
}

def operTimes (vs: List[Value]): Value = { 
  checkNumberArgs(vs, 2)
  val v1 = vs(0)
  val v2 = vs(1)
  checkFraction(v1)
  checkFraction(v2)
  val n1 = v1.getNumerator()
  val n2 = v2.getNumerator()
  val d1 = v1.getDenominator()
  val d2 = v2.getDenominator()
  return new VFraction(n1 * n2, d1 * d2).simplify()
}

def operDiv (vs: List[Value]): Value = { 
  checkNumberArgs(vs, 2)
  val v1 = vs(0)
  val v2 = vs(1)
  checkFraction(v1)
  checkFraction(v2)
  val n1 = v1.getNumerator()
  val n2 = v2.getNumerator()
  val d1 = v1.getDenominator()
  val d2 = v2.getDenominator()
  return new VFraction(n1 * d2, n2 * d1).simplify()
}

def operEqual (vs: List[Value]): Value = {
  checkNumberArgs(vs, 2)
  val v1 = vs(0)
  val v2 = vs(1)
  checkFraction(v1)
  checkFraction(v2)
  val n1 = v1.getNumerator()
  val n2 = v2.getNumerator()
  val d1 = v1.getDenominator()
  val d2 = v2.getDenominator()
  return new VBoolean(n1 == n2 && d1 == d2)
}


def operLess (vs: List[Value]): Value = {
  checkNumberArgs(vs, 2)
  val v1 = vs(0)
  val v2 = vs(1)
  checkFraction(v1)
  checkFraction(v2)
  val n1 = v1.getNumerator()
  val n2 = v2.getNumerator()
  val d1 = v1.getDenominator()
  val d2 = v2.getDenominator()
  return new VBoolean(n1 * d2 < n2 * d1)
}

def operRefCell (vs:List[Value]): Value = {
  checkNumberArgs(vs, 1)
  val init = vs(0)
  return new VRefCell(init)
}

def operGetRefCell (vs:List[Value]): Value = {
  checkNumberArgs(vs, 1)
  val r = vs(0)
  checkRefCell(r)
  return r.getRefContent()
}

def operPutRefCell (vs:List[Value]): Value = {
  checkNumberArgs(vs, 2)
  val r = vs(0)
  val v = vs(1)
  checkRefCell(r)
  r.putRefContent(v)
  return new VBoolean(true)
}

def operPrint (vs:List[Value]): Value = {
  for (v <- vs) {
    print(v.toDisplay())
    print(" ")
  }
  println(" ")
  return new VBoolean(true)
}

def operCons (vs: List[Value]): Value = {
  checkNumberArgs(vs, 2)
  val v1 = vs(0)
  val v2 = vs(1)
  checkVector(v2)
  return new VVector(v1 :: v2.getList())
}

def operFirst (vs: List[Value]): Value = {
  checkNumberArgs(vs, 1)
  val v1 = vs(0)
  checkVector(v1)
  val l = v1.getList()
  if (l.isEmpty) { 
    runtimeError("cannot apply first to an empty VECTOR")
  } else {
    return l.head
  }
}

def operRest (vs: List[Value]): Value = {
  checkNumberArgs(vs, 1)
  val v1 = vs(0)
  checkVector(v1)
  val l = v1.getList()
  if (l.isEmpty) {
    runtimeError("cannot apply rest to an empty VECTOR")
  } else {
    return new VVector(l.tail)
  }
}

def operEmptyP (vs: List[Value]): Value = {
  checkNumberArgs(vs, 1)
  val v1 = vs(0)
  checkVector(v1)
  return new VBoolean(v1.getList().isEmpty)
}

/* 
 *  Initial environment
 *
 */
  
val initBindings = List(
  ("true", new VBoolean(true), CVInteger(1)),
  ("false", new VBoolean(false), CVInteger(0)),
  ("*", new VPrimitive(operTimes), CVClosure(5, new Env(List()))),
  ("+", new VPrimitive(operPlus), CVClosure(10, new Env(List()))),
  ("=", new VPrimitive(operEqual), CVClosure(15, new Env(List()))),
  ("<", new VPrimitive(operLess), CVClosure(20, new Env(List()))),
  ("print", new VPrimitive(operPrint), CVClosure(0, new Env(List()))),
  ("/", new VPrimitive(operDiv), CVClosure(0, new Env(List()))),
  ("-", new VPrimitive(operMinus), CVClosure(0, new Env(List()))),
//  ("ref", new VPrimitive(operRefCell), CVClosure(0, new Env(List()))),
//  ("get", new VPrimitive(operGetRefCell), CVClosure(0, new Env(List()))),
//  ("put", new VPrimitive(operPutRefCell), CVClosure(0, new Env(List()))),
//  ("empty", new VVector(List()), 
//  ("cons", new VPrimitive(operCons), CVClosure(0, new Env(List()))),
//  ("first", new VPrimitive(operFirst), CVClosure(0, new Env(List()))),
//  ("rest", new VPrimitive(operRest), CVClosure(0, new Env(List()))),
//  ("empty?", new VPrimitive(operEmptyP), CVClosure(0, new Env(List())))
//  ("not", new VRecClosure("", List("a"), new EIf(new EId("a"), new EBoolean(false), new EBoolean(true)), new Env()))
)

val initEnv: Env[Value] = new Env(initBindings.map((p) => (p._1, p._2)))

val initCodeEnv: Env[CodeValue] = new Env(initBindings.map((p) => (p._1, p._3)))

val initCode = Vector(Op_PRIMCALL(2, CVOps.operNotImplemented),
                      Op_SWAP(),
		      Op_ENV(),
		      Op_SWAP(),
                      Op_JUMP(),
                      Op_PRIMCALL(2, CVOps.operTimes),
                      Op_SWAP(),
		      Op_ENV(),
                      Op_SWAP(),
                      Op_JUMP(),
                      Op_PRIMCALL(2, CVOps.operPlus),
                      Op_SWAP(),
		      Op_ENV(),
                      Op_SWAP(),
                      Op_JUMP(),
                      Op_PRIMCALL(2, CVOps.operEqual),
                      Op_SWAP(),
		      Op_ENV(),
                      Op_SWAP(),
                      Op_JUMP(),
                      Op_PRIMCALL(2, CVOps.operLess),
                      Op_SWAP(),
		      Op_ENV(),
                      Op_SWAP(),
                      Op_JUMP())
                      


/*
 *  Shell 
 *
 */

def shell (): Unit = {

  var env = initEnv
  var names = new Env(initBindings.map((p) => (p._1, ())))

  val code = new Code()
  code.append(initCode)
  var cenv = initCodeEnv
  
  println("Type #quit to quit, #code <expr> to see the code for an expression")

  while (true) {
    print("FUNC> ")
    val input = scala.io.StdIn.readLine()
    try { 
      // a hack --
      if (input.trim() == "#quit") { 
        return
      } else if (input.trim().startsWith("#code ")) {
        val e = parse(input.substring(6))
	e.analyzeIds(names)
        val caddr = code.length()
        val c = e.compile(caddr) ++ Vector(Op_STOP())
        var i = caddr
        for (v <- c) {
          println("  " + i + " : " + v)
          i += 1
        }
      } else { 
        val e = parse(input)
	e.analyzeIds(names)
        val caddr = code.length()
        val c = e.compile(caddr) ++ Vector(Op_STOP())
        code.append(c)
        val v = code.execute(caddr, cenv)
        println(v.toDisplay())
      }
    } catch { 
      case e: Throwable => println(e)
    }
  }
}




/*
 *
 *  COMPILER
 *
 */


class Code {

   var content : Vector[Opcode] = Vector()
   
   override def toString () : String = content.toString()

   def append (code: Vector[Opcode]) : Unit = {
      content = content ++ code
   }

   def length () : Int = content.length
   
   def execute (i:Int,env:Env[CodeValue]) : CodeValue = {
     val stack: Stack[StackEntry] = new Stack()

      var PC = i
      var ENV = env
      
      try { 
      while (true) {
        /*
	println("------------------------------")
        println("PC = " + PC.toString())
        println("Opcode = " + content(PC).toString())
        stack.print()
	*/
	/* println("Stack size = " + stack.size().toString()) */
        content(PC) match {
           case Op_STOP() =>
              return stack.top().getValue()
           case Op_PUSH_INT(i) =>
              stack.push(SCodeValue(CVInteger(i)))
              PC = PC + 1
           case Op_PUSH_ADDR(a) =>
              stack.push(SAddress(a))
              PC = PC + 1
           case Op_JUMP() =>
              val a = stack.pop().getAddress()
              PC = a
           case Op_JUMP_TRUE() =>
              val a = stack.pop().getAddress()
              val v = stack.pop().getValue()
              v match {
                 case CVInteger(i) => 
                   if (i != 0) {
                     PC = a
                   } else {
                     PC = PC + 1
                   }
              }
           case Op_OPEN() =>
               val v = stack.pop().getValue()
               v match {
                 case CVClosure(addr,env) =>
                    stack.push(SAddress(addr))
                    ENV = env
                    PC = PC + 1
               }
           case Op_ENV() =>
               ENV = stack.pop().getEnv()
               PC = PC + 1
           case Op_PUSH_ENV() =>
               stack.push(SEnv(ENV))
               PC = PC + 1
           case Op_CLOSURE() =>
               val a = stack.pop().getAddress()
               stack.push(SCodeValue(CVClosure(a, ENV)))
               PC = PC + 1
           case Op_ADD_ENV() =>
               val v = stack.pop().getValue()
               ENV = ENV.push("", v)
               PC = PC + 1
           case Op_LOOKUP(i) =>
               stack.push(SCodeValue(ENV.lookupByIndex(i)))
               PC = PC + 1
           case Op_PRIMCALL(n,p) =>
               var vs : List[CodeValue] = List()
               for (i <- 1 to n) { 
                 vs = (stack.pop().getValue()) :: vs
               }
               stack.push(SCodeValue(p(vs)))
               PC = PC + 1
            case Op_NOP() =>
               PC = PC + 1
            case Op_SWAP() =>
               val v1 = stack.pop()
               val v2 = stack.pop()
               stack.push(v1)
               stack.push(v2)
               PC = PC + 1
        }
      }
      } catch {
           case e : Throwable => {
              println("At PC = " + PC)
              println(e)
              throw e
           }
           
      }
      return CVInteger(0)   // to satisfy the type checker...
   }
}


abstract class CodeValue {
  def getInt () : Int = 0
  def toDisplay (): String
}
case class CVInteger (i:Int) extends CodeValue {
  override def getInt () : Int = i
  def toDisplay (): String = i.toString()
}
case class CVClosure (a:Int, e:Env[CodeValue]) extends CodeValue {
  def toDisplay (): String = "#<CLOSURE " + a.toString() + ">"
}


abstract class StackEntry {
  def getValue (): CodeValue =
    throw new Exception("Segmentation fault")
  def getAddress (): Int =
    throw new Exception("Segmentation fault")
  def getEnv(): Env[CodeValue] =
    throw new Exception("Segmentation fault")
}
case class SCodeValue (v: CodeValue) extends StackEntry {
  override def toString (): String = "CV[" + v.toDisplay() + "]"
  override def getValue (): CodeValue = v
}
case class SAddress (a: Int) extends StackEntry {
  override def toString (): String = "ADDR[" + a.toString() + "]"
  override def getAddress (): Int = a
}
case class SEnv (env: Env[CodeValue]) extends StackEntry {
  override def toString (): String = "ENV[]"
  override def getEnv (): Env[CodeValue] = env
}

class Stack[A] () {

   def print (): Unit =
     for (c <- content)
       println(c)

   var content : List[A] = List()

   def push (v:A) : Unit = {
      content = v::content
   }

   def pop () : A = {
      val v = content.head
      content = content.tail
      return v
   }

   def size () : Int = content.length

   def top () : A = content.head
}


abstract class Opcode
case class Op_STOP () extends Opcode {
  override def toString (): String = "STOP"
}
case class Op_PUSH_INT (i:Integer) extends Opcode {
  override def toString (): String = "PUSH-INT(" + i.toString() + ")"
}
case class Op_PUSH_ADDR (a:Integer) extends Opcode {
  override def toString (): String = "PUSH-ADDR(" + a.toString() + ")"
}
case class Op_JUMP () extends Opcode {
  override def toString (): String = "JUMP"
}
case class Op_JUMP_TRUE () extends Opcode {
  override def toString (): String = "JUMP-TRUE"
}
case class Op_OPEN () extends Opcode {
  override def toString (): String = "OPEN"
}
case class Op_ENV () extends Opcode {
  override def toString (): String = "ENV"
}
case class Op_PUSH_ENV () extends Opcode {
  override def toString (): String = "PUSH-ENV"
}
case class Op_CLOSURE () extends Opcode {
  override def toString (): String = "CLOSURE"
}
case class Op_ADD_ENV () extends Opcode {
  override def toString (): String = "ADD-ENV"
}
case class Op_LOOKUP (i: Int) extends Opcode {
  override def toString (): String = "LOOKUP(" + i.toString() + ")"
}
case class Op_PRIMCALL (n:Int,p:(List[CodeValue]) => CodeValue) extends Opcode {
  override def toString (): String = "PRIM-CALL(" + n.toString() + ", " + p.toString() + ")"
}
case class Op_NOP () extends Opcode {
  override def toString (): String = "NOP"
}
case class Op_SWAP () extends Opcode {
  override def toString (): String = "SWAP"
}


object CVOps { 

   def runtimeError (msg: String) : Nothing = {
       throw new Exception("Runtime error: "+msg)
   }

   def operNotImplemented (vs:List[CodeValue]) : CodeValue = {
     throw new Exception("Operations not implemented")
   }

   def operPlus (vs:List[CodeValue]) : CodeValue = {
   
      val v1 = vs(0)
      val v2 = vs(1)
      
      return new CVInteger(v1.getInt() + v2.getInt())
   }
   
   
   def operTimes (vs: List[CodeValue]):CodeValue = {
   
      val v1 = vs(0)
      val v2 = vs(1)
      
      return new CVInteger(v1.getInt() * v2.getInt())
   }
   
   
   def operEqual (vs: List[CodeValue]) : CodeValue = {
       
      val v1 = vs(0)
      val v2 = vs(1)
   
      return new CVInteger(if (v1.getInt() == v2.getInt()) 1 else 0)
   }
   
   
   def operLess (vs: List[CodeValue]) : CodeValue = {
   
       val v1 = vs(0)
       val v2 = vs(1)
   
       return new CVInteger(if (v1.getInt() < v2.getInt()) 1 else 0)
   }
   
}

