// LECTURE 11 - compiler with simpler virtual machine

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
import java.io._


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
}


class VBoolean (val b:Boolean) extends Value {

  override def toString (): String =
    "VBoolean[" + b.toString() + "]"

  def toDisplay (): String =
    if (b) "true" else "false"

  override def isBoolean (): Boolean = true
  override def getBool (): Boolean = b
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

  def bindings () : List[(String, A)] = content

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
  def analyzeIds (env: Env[Value]): Unit
  def compile (addr: Int): Vector[Opcode]
  def compileTail (addr: Int): Vector[Opcode] =
    // by default, tail compilation is compilation
    compile(addr)
}


class EInteger (val i:Int) extends Exp {
  // integer literal

  override def toString (): String =
    "EInteger[" + i.toString() + "]"

  def analyzeIds (env: Env[Value]): Unit = ()

  def eval (env: Env[Value]): Value =
    new VInteger(i)

  def compile (addr:Int) : Vector[Opcode] = {
    val cg = new CodeGenerator(addr)
    cg.generate_PUSH(i)
    return cg.code()
  }
}


class EBoolean (val b:Boolean) extends Exp {
  // Boolean literal

  override def toString (): String =
    "EBoolean[" + b.toString() + "]"

  def analyzeIds (env: Env[Value]): Unit = ()

  def eval (env: Env[Value]): Value =
    new VBoolean(b)
        
  def compile (addr:Int) : Vector[Opcode] = {
    val cg = new CodeGenerator(addr)
    cg.generate_PUSH(if (b) 1 else 0)
    return cg.code()
  }
}


class EPrimitive (val oper: (List[Value]) => Value) extends Exp { 

  override def toString (): String = 
    "EPrimitive[" + Integer.toHexString(oper.hashCode()) + "]"

  def analyzeIds (env: Env[Value]): Unit = ()

  def eval (env: Env[Value]): Value =
    new VPrimitive(oper)

  def compile (addr: Int): Vector[Opcode] =
    runtimeError("Cannot compile EPrimitive")
}
 

class EIf (val ec: Exp, val et: Exp, val ee: Exp) extends Exp {
  // Conditional expression

  override def toString (): String =
    "EIf[" + ec.toString() + ", " + et.toString() + ", " + ee.toString() + "]"

  def analyzeIds (env: Env[Value]): Unit = {
    ec.analyzeIds(env)
    et.analyzeIds(env)
    ee.analyzeIds(env)
  }

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

  def compile (addr:Int) : Vector[Opcode] = {
    val cg = new CodeGenerator(addr)

    cg.compile(ec)
    cg.generate_PUSH_PATCH()
    cg.generate_JUMP_TRUE()
    cg.compile(ee)
    cg.generate_PUSH_PATCH()
    cg.generate_JUMP()
    val then_part = cg.compile(et)
    val done_part = cg.generate_NOP()
    return cg.patch(List(then_part, done_part))
    }

  override def compileTail (addr:Int) : Vector[Opcode] = {
    val cg = new CodeGenerator(addr)
    cg.compile(ec)
    cg.generate_PUSH_PATCH()
    cg.generate_JUMP_TRUE()
    cg.compileTail(ee)
    cg.generate_PUSH_PATCH()
    cg.generate_JUMP()
    val then_part = cg.compileTail(et)
    val done_part = cg.generate_NOP()
    return cg.patch(List(then_part, done_part))
  }
}


class EId (val id: String) extends Exp {

  override def toString (): String =
    "EId[" + id + "]"

  var index = -1

  def analyzeIds (env: Env[Value]): Unit =
    index = env.findIndex(id)

  def eval (env: Env[Value]): Value =
    env.lookup(id)

  def compile (addr:Int) : Vector[Opcode] = {
    val cg = new CodeGenerator(addr)
    cg.generate_PUSH(index)
    cg.generate_LOOKUP()
    return cg.code()
  }
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

  def analyzeIds (env: Env[Value]): Unit = {
    fn.analyzeIds(env)
    for (arg <- args)
      arg.analyzeIds(env)
  }
  
  def eval (env: Env[Value]): Value = {
    val vfn = fn.eval(env)
    val vargs = args.map((e:Exp) => e.eval(env))
    return vfn.apply(vargs)
  }
  
  def compile (addr:Int) : Vector[Opcode] = {
    val cg = new CodeGenerator(addr)
    cg.generate_PUSH_PATCH()
    cg.generate_PUSH_ENV()
    for (e <- args.reverse)
      cg.compile(e)
    cg.compile(fn)
    cg.generate_OPEN()
    cg.generate_JUMP()
    val done = cg.generate_NOP()
    return cg.patch(List(done))
  }
    
  override def compileTail (addr:Int) : Vector[Opcode] = {
    val cg = new CodeGenerator(addr)
    for (e <- args.reverse)
      cg.compile(e)
    cg.compile(fn)
    cg.generate_OPEN()
    cg.generate_JUMP()
    return cg.code()
  }
}


class EFunction (val recName: String, val params: List[String], val body: Exp) extends Exp {

  override def toString (): String = {
    val ps = params.addString(new StringBuilder(), "(", ", ", ")").toString()
    "EFunction[" + recName + ", " + ps + ", " + body.toString() + "]"
  }
  
  def eval (env: Env[Value]): Value = 
    new VRecClosure(recName, params, body, env)

  def analyzeIds (env: Env[Value]): Unit = {
    var newEnv = env.push(recName, new VBoolean(false))
    for (p <- params)
      newEnv = newEnv.push(p, new VBoolean(false))
    body.analyzeIds(newEnv)
  }
    
  def compile (addr:Int) : Vector[Opcode] = {
    val cg = new CodeGenerator(addr)
    cg.generate_PUSH_PATCH()
    cg.generate_JUMP()
    val cfun = cg.generate_PUSH_PATCH()
    cg.generate_CLOSURE()
    cg.generate_ADD_ENV()
    for (n <- params)
      cg.generate_ADD_ENV()
    cg.compileTail(body)
    cg.generate_SWAP()
    cg.generate_ENV()
    cg.generate_SWAP()
    cg.generate_JUMP()
    val done = cg.generate_PUSH_PATCH()
    cg.generate_CLOSURE()
    return cg.patch(List(done, cfun, cfun))
  }
}  


class ELet (val bindings: List[(String, Exp)], val body: Exp) extends Exp {

  override def toString (): String = {
    val bs1 = bindings.map((p) => "(" + p._1 + ", " + p._2.toString() + ")")
    val bs2 = bs1.addString(new StringBuilder(), "", ", ", "").toString()
    "ELet[" + bs2 + ", " + body.toString() + "]"
  }

  def analyzeIds (env: Env[Value]): Unit = {
    var newEnv = env
    for ((n, e) <- bindings) {
      e.analyzeIds(env)
      newEnv = newEnv.push(n, new VBoolean(false))
    }
    body.analyzeIds(newEnv)  
  }

  def eval (env: Env[Value]): Value = {
    var newEnv = env
    for (binding <- bindings) {
      val v = binding._2.eval(env)
      newEnv = newEnv.push(binding._1, v)
    }
    return body.eval(newEnv)
  }

  def compile (addr:Int) : Vector[Opcode] = {
    val cg = new CodeGenerator(addr)
    for ((_, e) <- bindings.reverse) {
      cg.compile(e)
      cg.generate_ADD_ENV()
    }
    cg.compile(body)
    return cg.code()
  }

  override def compileTail (addr:Int) : Vector[Opcode] = {
    val cg = new CodeGenerator(addr)
    for ((_, e) <- bindings.reverse) {
      cg.compile(e)
      cg.generate_ADD_ENV()
    }
    cg.compileTail(body)
    return cg.code()
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
  checkNumberArgs(vs, 1)
  val v = vs(0)
  println(v.toDisplay())
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

def operNot (vs: List[Value]): Value = {
  checkNumberArgs(vs, 1)
  val v1 = vs(0)
  checkBoolean(v1)
  return new VBoolean(!v1.getBool())
}



/* 
 *  Initial bindings
 *
 */
  
val initBindings = List(
  ("true", new VBoolean(true), CVInteger(1)),
  ("false", new VBoolean(false), CVInteger(0)),
  ("*", new VPrimitive(operTimes), CVPrimitive(1)),
  ("+", new VPrimitive(operPlus), CVPrimitive(2)),
  ("=", new VPrimitive(operEqual), CVPrimitive(3)),
  ("<", new VPrimitive(operLess), CVPrimitive(4)),
  ("print", new VPrimitive(operPrint), CVPrimitive(5)),
  ("-", new VPrimitive(operMinus), CVPrimitive(6)),
  ("not", new VPrimitive(operNot), CVPrimitive(7)),
  // not implemented in the compiler
  ("/", new VPrimitive(operDiv), CVPrimitive(0)),
  ("ref", new VPrimitive(operRefCell), CVPrimitive(0)),
  ("get", new VPrimitive(operGetRefCell), CVPrimitive(0)),
  ("put", new VPrimitive(operPutRefCell), CVPrimitive(0)),
  ("empty", new VVector(List()), CVInteger(0)),
  ("cons", new VPrimitive(operCons), CVPrimitive(0)),
  ("first", new VPrimitive(operFirst), CVPrimitive(0)),
  ("rest", new VPrimitive(operRest), CVPrimitive(0)),
  ("empty?", new VPrimitive(operEmptyP), CVPrimitive(0))
)



/*
 *  Shell 
 *
 */

def shell (): Unit = {
  
  var env = new Env(initBindings.map((p) => (p._1, p._2)))

  val code = new Code()

  val cg = new CodeGenerator(0)
  var primAddresses: Vector[Int] = Vector()
  for (((_, oper), idx) <- CVOps.zipWithIndex) {
    val addr = cg.generate_PRIM_CALL(idx)
    cg.generate_SWAP()
    cg.generate_ENV()
    cg.generate_SWAP()
    cg.generate_JUMP()
    primAddresses = primAddresses ++ Vector(addr)
  }
  code.append(cg.code())

  def convertPrim (v: CodeValue): CodeValue =
    v match {
      case CVPrimitive(idx) => CVClosure(primAddresses(idx))
      case v => v
    }
  var cenv : Env[CodeValue] = new Env(initBindings.map((p) => (p._1, convertPrim(p._3))))

  println("Type #quit to quit")

  while (true) {
    print("FUNC> ")
    val input = scala.io.StdIn.readLine()
    try { 
      // a hack --
      if (input.trim() == "#quit") { 
        return
      } else if (input.trim().startsWith("#code ")) {
        val e = parse(input.substring(6))
	e.analyzeIds(env)
        val caddr = code.length()
        val c = e.compile(caddr) ++ Vector(OP_STOP)
        var i = caddr
        for (v <- c) {
          println("  " + i + " : " + v)
          i += 1
        }
      } else { 
        val e = parse(input)
	e.analyzeIds(env)
        val caddr = code.length()
        val c = e.compile(caddr) ++ Vector(OP_STOP)
        code.append(c)
        val v = code.execute(caddr, cenv)
        println(v)
      }
    } catch { 
      case e: Throwable => println(e)
    }
  }
}


def compileFile (fname: String): Unit = {
  val env : Env[Value] = new Env(initBindings.map((p) => (p._1, p._2)))
  // read code from file
  val bufferedSource = Source.fromFile(fname)
  val content = bufferedSource.getLines.mkString
  bufferedSource.close()
  val e = parse(content)
  e.analyzeIds(env)

  // prepare to generate code
  val cg = new CodeGenerator(0)
  
  // jump to setting up the initial environment
  cg.generate_PUSH_PATCH()
  cg.generate_JUMP()
  
  var primAddresses: Vector[Int] = Vector()
  for (((_, oper), idx) <- CVOps.zipWithIndex) {
    val addr = cg.generate_PRIM_CALL(idx)
    cg.generate_SWAP()
    cg.generate_ENV()
    cg.generate_SWAP()
    cg.generate_JUMP()
    primAddresses = primAddresses ++ Vector(addr)
  }
  
  def convertPrim (v: CodeValue): CodeValue =
    v match {
      case CVPrimitive(idx) => CVClosure(primAddresses(idx))
      case v => v
    }
  val cenv : Env[CodeValue] = new Env(initBindings.map((p) => (p._1, convertPrim(p._3))))
  
  // set up initial environment
  val setup = cg.generate_NOP()
  
  for ((n, v) <- cenv.bindings().reverse) {
    v match {
      case CVInteger(i) =>
        cg.generate_PUSH(i)
	cg.generate_ADD_ENV()
	
      case CVClosure(a) =>
        cg.generate_PUSH(a)
	cg.generate_CLOSURE()
	cg.generate_ADD_ENV()
    }
  }
  cg.compile(e)
  cg.generate_STOP()
  val code = cg.patch(List(setup))

  // write code to file
  val file = new File(fname + ".fvm")
  val bw = new BufferedWriter(new FileWriter(file))
  for (c <- code) {
    bw.write(c.toCode().toString() + "\n")
  }
  for ((c, i) <- code.zipWithIndex) {
    bw.write("# " + i + ": " + c + "\n")
  }
  bw.close()
  println("Written " + fname + ".fvm")
}

/*
 *
 *  COMPILER
 *
 */


/* modifications
- simplified virtual machine instructions (no args)
- no code values - only integers
- no lookup via identifiers
- memory arrays for environments and closures
- memory array for stack
- show max stack size
- slight change to calling convention - push addr THEN env
- new CodeGenerator with patching
- OP_CODES now just integers / with multi-byte opcodes for PUSH
TODO:
- inline primitive calls
*/




class Code {

   val memSize = 100000
   
   var content: Vector[Opcode] = Vector()
   
   override def toString (): String = content.toString()

   def append (code: Vector[Opcode]): Unit = {
      content = content ++ code
   }

   def length (): Int = content.length

   def dump (start: Int): Unit = {
     var i = 0
     for (v <- content) {
       println("  " + i + " : " + v)
       i += 1
     }
     println("Start = " + start.toString())
   }
   
   def execute (i:Int, env:Env[CodeValue]): Int = {

     val stack = new Array[Int](memSize)
     var stackPtr = 0
     var maxStackPtr = 0
     var maxEnvSize = 0

     val closures = new Array[Int](memSize)
     var closurePtr = 0
     val environments = new Array[Int](memSize)
     var environmentPtr = 0

     def push (i: Int): Unit = {
       stack(stackPtr) = i
       stackPtr = stackPtr + 1
     }

     def pop (): Int = {
       stackPtr = stackPtr - 1
       return stack(stackPtr)
     }

     def allocateEnv (v: Int, rest: Int): Int = {
       if (environmentPtr >= memSize) {
         throw new Exception("Out of environment memory")
       }
       environments(environmentPtr) = v
       environments(environmentPtr + 1) = rest
       environmentPtr = environmentPtr + 2
       return environmentPtr - 2
     }

     def envSize (e: Int): Int = {
       var res = 0
       var curr = e
       while (curr >= 0) {
         res += 1
         curr = environments(curr + 1)
       }
       return res
     }

     def allocateClosure (a: Int, env: Int): Int = {
       if (closurePtr >= memSize) {
         throw new Exception("Out of closure memory")
       }
       closures(closurePtr) = a
       closures(closurePtr + 1) = env
       closurePtr = closurePtr + 2
       return closurePtr - 2
     }

     def lookupEnv (i: Int, env: Int): Int = {
       var curr = env
       for (j <- 0.to(i - 1)) {
         curr = environments(curr + 1)
       }
       return environments(curr)
     }

     var ENV = -1
     for ((n, v) <- env.bindings().reverse) {
       v match {
         case CVInteger(i) => 
           ENV = allocateEnv(i, ENV)
         case CVClosure(a) => 
           val i = allocateClosure(a, -1)
           ENV = allocateEnv(i, ENV)
       }
     }

      var PC = i

      try { 
      while (true) {
      /*
        println("------------------------------")
        println("PC = " + PC.toString())
        println("Opcode = " + content(PC).toString())
        for (i <- (stackPtr - 1).to(0, -1))
          println(" " + i.toString())
      */
        maxStackPtr = math.max(maxStackPtr, stackPtr)
        maxEnvSize = math.max(maxEnvSize, envSize(ENV))
        content(PC) match {
           case OP_STOP =>
              println("Max stack size = " + maxStackPtr)
              println("Max env size = " + maxEnvSize)
              return pop() 
           case OP_PUSH =>
              content(PC + 1) match {
                case OP_INT(i) => 
                  push(i)
                  PC = PC + 2
              }
           case OP_JUMP =>
              val a = pop()
              PC = a
           case OP_JUMP_TRUE =>
              val a = pop()
              val i = pop()
              if (i != 0) {
                 PC = a
              } else {
                 PC = PC + 1
              }
           case OP_OPEN =>
               val i = pop()
               val addr = closures(i)
               val env = closures(i + 1)
               push(addr)
               ENV = env
               PC = PC + 1
           case OP_ENV =>
               ENV = pop()
               PC = PC + 1
           case OP_PUSH_ENV =>
               push(ENV)
               PC = PC + 1
           case OP_CLOSURE =>
               val a = pop()
               val i = allocateClosure(a, ENV)
               push(i)
               PC = PC + 1
           case OP_ADD_ENV =>
               val i = pop()
               ENV = allocateEnv(i, ENV)
               PC = PC + 1
           case OP_LOOKUP =>
               val i = pop()
               push(lookupEnv(i, ENV))
               PC = PC + 1
           case OP_PRIM_CALL =>
               val idx = pop()
               var vs : List[Int] = List()
               for (i <- 1 to CVOps(idx)._1)
                 vs = (pop()) :: vs
               push(CVOps(idx)._2(vs))
               PC = PC + 1
            case OP_NOP =>
               PC = PC + 1
            case OP_SWAP =>
               val v1 = pop()
               val v2 = pop()
               push(v1)
               push(v2)
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
      return 0   // to satisfy the type checker...
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
case class CVClosure (a:Int) extends CodeValue {
  def toDisplay (): String = "#<CLOSURE " + a.toString() + ">"
}
case class CVPrimitive (id: Int) extends CodeValue {
  def toDisplay (): String = "#<PRIMITIVE " + id.toString() + ">"
}


class CodeGenerator (addr: Int) {
  var codeVec: Vector[Opcode] = Vector()
  var caddr = addr
  var patchPositions: List[Int] = List()

  def _generate (ops: List[Opcode]): Int = {
    for (op <- ops)
      codeVec = codeVec ++ Vector(op)
    val raddr = caddr
    caddr += ops.length
    return raddr
  }
  
  def generate_STOP (): Int =
    _generate(List(OP_STOP))

  def generate_PUSH (i: Int): Int =
    _generate(List(OP_PUSH, OP_INT(i)))
  
  def generate_PUSH_PATCH (): Int =
    _generate(List(OP_PUSH_PATCH, OP_INT(0)))

  def generate_JUMP (): Int =
    _generate(List(OP_JUMP))

  def generate_JUMP_TRUE (): Int =
    _generate(List(OP_JUMP_TRUE))

  def generate_OPEN (): Int =
    _generate(List(OP_OPEN))

  def generate_ENV (): Int =
    _generate(List(OP_ENV))

  def generate_PUSH_ENV (): Int =
    _generate(List(OP_PUSH_ENV))

  def generate_CLOSURE (): Int =
    _generate(List(OP_CLOSURE))

  def generate_ADD_ENV (): Int =
    _generate(List(OP_ADD_ENV))

  def generate_LOOKUP (): Int =
    _generate(List(OP_LOOKUP))

  def generate_PRIM_CALL (oper: Int): Int =
    _generate(List(OP_PUSH, OP_INT(oper), OP_PRIM_CALL))

  def generate_NOP (): Int =
    _generate(List(OP_NOP))

  def generate_SWAP (): Int =
    _generate(List(OP_SWAP))

  def compile (exp: Exp): Int = {
    val vec = exp.compile(caddr)
    codeVec = codeVec ++ vec
    val raddr = caddr
    caddr += vec.length
    return raddr
  }

  def compileTail (exp: Exp): Int = {
    val vec = exp.compileTail(caddr)
    codeVec = codeVec ++ vec
    val raddr = caddr
    caddr += vec.length
    return raddr
  }

  def patch (patches: List[Int]): Vector[Opcode] = {
    var resultVec: Vector[Opcode] = Vector()
    var ps = patches
    var i = 0
    while (i < codeVec.length) {
      codeVec(i) match {
        case OP_PUSH_PATCH =>
          val p = ps.head
          ps = ps.tail
          resultVec = resultVec ++ Vector(OP_PUSH, OP_INT(p))
          i += 2
        case op =>
          resultVec = resultVec ++ Vector(op)
          i += 1
      }
    }
    return resultVec
  }
    
  def code (): Vector[Opcode] =
    codeVec
}


abstract class Opcode {
  def toCode (): Int
}
case object OP_STOP extends Opcode {
  override def toString (): String = "STOP"
  def toCode (): Int = 0
}
case object OP_PUSH extends Opcode {
  override def toString (): String = "PUSH"
  def toCode (): Int = 1
}
case class OP_INT (i: Int) extends Opcode {
  override def toString (): String = i.toString()
  def toCode (): Int = i
}
case object OP_PUSH_PATCH extends Opcode {
  override def toString (): String = "PUSH-patch"
  def toCode (): Int = throw new Exception("Unpatched PUSH in generated code")
}
case object OP_JUMP extends Opcode {
  override def toString (): String = "JUMP"
  def toCode (): Int = 2
}
case object OP_JUMP_TRUE extends Opcode {
  override def toString (): String = "JUMP-TRUE"
  def toCode (): Int = 3
}
case object OP_OPEN extends Opcode {
  override def toString (): String = "OPEN"
  def toCode (): Int = 4
}
case object OP_ENV extends Opcode {
  override def toString (): String = "ENV"
  def toCode (): Int = 5
}
case object OP_PUSH_ENV extends Opcode {
  override def toString (): String = "PUSH-ENV"
  def toCode (): Int = 6
}
case object OP_CLOSURE extends Opcode {
  override def toString (): String = "CLOSURE"
  def toCode (): Int = 7
}
case object OP_ADD_ENV extends Opcode {
  override def toString (): String = "ADD-ENV"
  def toCode (): Int = 8
}
case object OP_LOOKUP extends Opcode {
  override def toString (): String = "LOOKUP"
  def toCode (): Int = 9
}
case object OP_PRIM_CALL extends Opcode {
  override def toString (): String = "PRIM-CALL"
  def toCode (): Int = 10
}
case object OP_NOP extends Opcode {
  override def toString (): String = "NOP"
  def toCode (): Int = 11
}
case object OP_SWAP extends Opcode {
  override def toString (): String = "SWAP"
  def toCode (): Int = 12
}


// primitive operations on code values
// note - arguments arrive in the reverse order of call!

val CVOps = Vector(
  (0, (vs:List[Int]) => throw new Exception("Operation not implemented")),
  (2, (vs: List[Int]) => {
    val v1 = vs(1)
    val v2 = vs(0)
    v1 * v2
  }),
  (2, (vs:List[Int]) => {
    val v1 = vs(1)
    val v2 = vs(0)
    v1 + v2
  }),
  (2, (vs: List[Int]) => {
    val v1 = vs(1)
    val v2 = vs(0)
    if (v1 == v2) 1 else 0
  }),
  (2, (vs: List[Int]) => { 
    val v1 = vs(1)
    val v2 = vs(0)
    if (v1 < v2) 1 else 0
  }),
  (1, (vs: List[Int]) => { 
    val v = vs(0)
    println(v)
    1
  }),
  (1, (vs: List[Int]) => {
    val v = vs(0)
    -v
  }),
  (1, (vs: List[Int]) => { 
    val v = vs(0)
    if (v != 0) 0 else 1
  })
)

