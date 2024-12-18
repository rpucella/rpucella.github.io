// LECTURE 7 - RECORDS + OBJECTS

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

val gensym : () => String = {
  var counter = 0
  () => { 
    val next = counter
    counter += 1
    " _gs_" + next.toString()
  }
}


def runtimeError (msg: String) : Nothing = {
  throw new Exception("Runtime error: "+msg)
}


/*
 *  Values
 *
 */


abstract class Value {

  // default behaviors for values
  
  def isInteger () : Boolean = false
  def isBoolean () : Boolean = false
  def isFraction () : Boolean = false
  def isVector () : Boolean = false
  def isFunction () : Boolean = false
  def isRefCell () : Boolean = false
  def isRecord () : Boolean = false

  def toDisplay () : String

  def getInt () : Int = {
    runtimeError("Value " + this.toString() + " is not of type INTEGER")
  }

  def getBool () : Boolean = {
    runtimeError("Value " + this.toString() + " is not of type BOOLEAN")
  }

  def getDenominator () : Int = {
    runtimeError("Value " + this.toString() + " is not of type FRACTION")
  }

  def getNumerator () : Int = {
    runtimeError("Value " + this.toString() + " is not of type FRACTION")
  }

  def getList () : List[Value] = {
    runtimeError("Value " + this.toString() + " is not of type VECTOR")
  }

  def getRefContent () : Value = {
    runtimeError("Value " + this.toString() + " is not of type REFCELL")
  }
  
  def putRefContent (v:Value) : Unit = {
    runtimeError("Value " + this.toString() + " is not of type REFCELL")
  }

  def getBindings () : List[(String, Value)] = {
    runtimeError("Value " + this.toString() + " is not of type RECORD")
  }

  def lookup (s : String) : Value = {
    runtimeError("Value " + this.toString() + " is not of type RECORD")
  }
  
  def apply (vs : List[Value]) : Value = {
    runtimeError("Value " + this.toString() + " is not of type FUNCTION")
  }
}


class VInteger (val i:Int) extends Value {

  override def toString () : String =
    "VInteger[" + i.toString() + "]"

  def toDisplay () : String =
    i.toString()

  override def isInteger () : Boolean = true
  override def isFraction () : Boolean = true
  override def getInt () : Int = i
  override def getNumerator () : Int = i
  override def getDenominator () : Int = 1
}


class VBoolean (val b:Boolean) extends Value {

  override def toString () : String =
    "VBoolean[" + b.toString() + "]"

  def toDisplay () : String =
    if (b) "true" else "false"

  override def isBoolean () : Boolean = true
  override def getBool () : Boolean = b
}


class VFraction (val numerator:Int, val denominator:Int) extends Value {

  override def toString () : String =
    "VFraction[" + numerator.toString() + ", " + denominator.toString() + "]"

  def toDisplay () : String =
    numerator.toString() + "/" + denominator.toString()

  def gcd (n : Int, m : Int) : Int = {
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

  def sign (n : Int) : Int = if (n < 0) -1 else 1
  def abs (n : Int) : Int = if (n < 0) -n else n

  def simplify () : Value = {
    val s = sign(numerator) * sign(denominator)
    val g = gcd(abs(numerator), abs(denominator))
    val sn = abs(numerator) / g
    val sd = abs(denominator) / g
    if (sd == 1) new VInteger(s * sn) else new VFraction(s * sn, sd)    
  }

  override def isFraction () : Boolean = true
  override def getNumerator () : Int = numerator
  override def getDenominator () : Int = denominator
}


class VVector (val l : List[Value]) extends Value {

  override def toString () : String = 
     l.addString(new StringBuilder(), "VVector[", ", ", "]").toString()

  def toDisplay () : String =
     l.map((v:Value) => v.toDisplay()).addString(new StringBuilder(), "(", " ", ")").toString()

  override def isVector () : Boolean = true
  override def getList () : List[Value] = l
}


class VRefCell (val init : Value) extends Value {

  var content = init
  
  override def toString () : String =
    "VRefCell[" + content.toString() + "]"
    
  def toDisplay () : String =
    "#REF[" + content.toDisplay() + "]"
    
  override def isRefCell () : Boolean = true

  override def getRefContent () : Value = content
  override def putRefContent (v:Value) : Unit = {
    content = v
  }
}


class VPrimitive (val oper : (List[Value]) => Value) extends Value {

  override def toString () : String = 
    "VPrimitive[" + Integer.toHexString(oper.hashCode()) + "]"

  def toDisplay () : String =
    "#PRIMITIVE[" + Integer.toHexString(oper.hashCode()) + "]"

  override def isFunction () : Boolean = true
  override def apply (args: List[Value]) : Value =
     oper(args)
}


class VRecClosure (val self: String, val params: List[String], val body:Exp, val env:Env) extends Value {

  override def toString () : String =
    "VRecClosure[" + self + ", " + params + ", " + body.toString() + ", " + env.toString() + "]"

   def toDisplay () : String =
    "#CLOSURE[" + Integer.toHexString(hashCode()) + "]"

  override def isFunction () : Boolean = true

  override def apply (args: List[Value]) : Value = {
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

class Env (val content: List[(String, Value)]) {

  def this () = {
    this(List())
  }
      
  override def toString () : String = {
    val cs = content.map((p) => p._1 + " <- " + p._2.toString())
    return cs.addString(new StringBuilder(), "{", ", ", "}").toString()
  }

  def push (id : String, v : Value) : Env =
    new Env((id,v)::content)

  def lookup (id : String) : Value = {
    for (entry <- content) {
      if (entry._1 == id) {
        return entry._2
      }
    }
    runtimeError("Runtime error : unbound identifier " + id)
  }
}


abstract class Exp {

  def pp (prefix : String, skipPrefix : Boolean, suffix : String) : Unit =
    if (skipPrefix) 
      println(this.toString() + suffix)
    else
      println(prefix + this.toString() + suffix)

  def eval (env : Env) : Value
}


class EInteger (val i:Int) extends Exp {
  // integer literal

  override def toString () : String =
    "EInteger[" + i.toString() + "]"

  def eval (env : Env) : Value =
    new VInteger(i)
}


class EBoolean (val b:Boolean) extends Exp {
  // Boolean literal

  override def toString () : String =
    "EBoolean[" + b.toString() + "]"

  def eval (env : Env) : Value =
    new VBoolean(b)
}


class EPrimitive (val oper : (List[Value]) => Value) extends Exp { 

  override def toString () : String = 
    "EPrimitive[" + Integer.toHexString(oper.hashCode()) + "]"

  def eval (env : Env) : Value =
    new VPrimitive(oper)
}
 

class EIf (val ec : Exp, val et : Exp, val ee : Exp) extends Exp {
  // Conditional expression

  override def toString () : String =
    "EIf[" + ec.toString() + ", " + et.toString() + ", " + ee.toString() + "]"

  override def pp (prefix : String, skipPrefix : Boolean, suffix : String) : Unit = {
    if (!skipPrefix) {
      print(prefix)
    }
    print("EIf[")
    ec.pp(prefix + "    ", true, "")
    et.pp(prefix + "    ", false, "")
    ee.pp(prefix + "    ", false, "]" + suffix)
  }
  
  def eval (env : Env) : Value = {
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
}


class EId (val id : String) extends Exp {

  override def toString () : String =
    "EId[" + id + "]"

  def eval (env : Env) : Value =
    env.lookup(id)
}


class EApply (val fn : Exp, val args : List[Exp]) extends Exp {

  override def toString () : String = {
    if (args.isEmpty) {
      return "EApply[" + fn + "]"
    } else { 
      val sArgs = args.map(_.toString()).addString(new StringBuilder(), "", ", ", "").toString()
      return "EApply[" + fn + ", " + sArgs + "]"
    }
  }
  
  override def pp (prefix : String, skipPrefix : Boolean, suffix : String) : Unit = {
    if (!skipPrefix) {
      print(prefix)
    }
    print("EApply[")
    val len = args.length
    if (len > 0) {     
      fn.pp(prefix + "       ", true, "")
      for ((arg, i) <- args.zipWithIndex) {
        if (i < len - 1) { 
          arg.pp(prefix + "       ", false, "");
        } else {
          arg.pp(prefix + "       ", false, "]" + suffix)
        }
      }
    } else {
      fn.pp(prefix + "       ", true, "]" + suffix)
    }
  }

  def eval (env : Env) : Value = {
    val vfn = fn.eval(env)
    val vargs = args.map((e:Exp) => e.eval(env))
    return vfn.apply(vargs)
  }
}


class EFunction (val recName : String, val params : List[String], val body : Exp) extends Exp {

  override def toString () : String = {
    val ps = params.addString(new StringBuilder(), "(", ", ", ")").toString()
    "EFunction[" + recName + ", " + ps + ", " + body.toString() + "]"
  }
  
  override def pp (prefix : String, skipPrefix : Boolean, suffix : String) : Unit = {
    if (!skipPrefix) {
      print(prefix)
    }
    val ps = params.addString(new StringBuilder(), "(", ", ", ")").toString()
    println("EFunction[" + recName)
    println(prefix + "          " + ps)
    body.pp(prefix + "          ", false, "]" + suffix)
  }

  def eval (env : Env) : Value = 
    new VRecClosure(recName, params, body, env)
}


class ELet (val bindings : List[(String, Exp)], val body : Exp) extends Exp {

  override def toString () : String = {
    val bs1 = bindings.map((p) => "(" + p._1 + ", " + p._2.toString() + ")")
    val bs2 = bs1.addString(new StringBuilder(), "", ", ", "").toString()
    "ELet[" + bs2 + ", " + body.toString() + "]"
  }

  override def pp (prefix : String, skipPrefix : Boolean, suffix : String) : Unit = {
    if (!skipPrefix) {
      print(prefix)
    }
    if (bindings.isEmpty) {
      print("ELet[")
      body.pp(prefix + "      ", true, "]" + suffix)
    } else { 
      print("ELet[")
      val b = bindings.head
      print("(" + b._1 + ", ")
      b._2.pp(prefix + "        " + ' '.toString() * b._1.length, true, ")")
      for (binding <- bindings.tail) {
        print(prefix + "     (" + binding._1 + ", ")
        binding._2.pp(prefix + "        " + ' '.toString() * binding._1.length, true, ")")
      }
      body.pp(prefix + "     ", false, "]" + suffix)
    }
  }

  def eval (env : Env) : Value = {
    var newEnv = env
    for (binding <- bindings) {
      val v = binding._2.eval(env)
      newEnv = newEnv.push(binding._1, v)
    }
    return body.eval(newEnv)
  }
}



/*
 *  PARSER FOR S-EXPRESSIONS
 *
 */


class SExpParser extends RegexParsers { 

   // tokens
   
   def LP : Parser[Unit] = "(" ^^ { s => () }
   def RP : Parser[Unit] = ")" ^^ { s => () }
   def INT : Parser[Int] = """-?[0-9]+""".r ^^ { s => s.toInt }
   def IF : Parser[Unit] = "if" ^^ { s => () }
   def FUN : Parser[Unit] = "fun" ^^ { s => () }
   def LET : Parser[Unit] = "let" ^^ { s => () }
   def ID : Parser[String] = """[a-zA-Z_*+=</?-][a-zA-Z0-9_*+=</?-]*""".r ^^ { s => s }

   // transformation tokens
   def DO : Parser[Unit] = "do" ^^ { s => () } 


   // grammar

   def atomic_int : Parser[Exp] = INT ^^ { i => new EInteger(i) }

   def atomic_id : Parser[Exp] =
      ID ^^ { s => new EId(s) }

   def atomic : Parser[Exp] =
      ( atomic_int | atomic_id ) ^^ { e => e}

   def expr_if : Parser[Exp] =
      LP ~ IF ~ expr ~ expr ~ expr ~ RP ^^
        { case _ ~ _ ~ e1 ~ e2 ~ e3 ~ _ => new EIf(e1,e2,e3) }

   def expr_let : Parser[Exp] =
     LP ~ LET ~ LP ~ bindings ~ RP ~ expr ~ RP ^^
       { case _ ~ _ ~ _ ~ bindings ~ _ ~ e2 ~ _ => new ELet(bindings, e2) }

  def bindings : Parser[List[(String, Exp)]] =
    ( bindings_many | bindings_none ) ^^ { bs => bs }

  def bindings_many : Parser[List[(String, Exp)]] =
    LP ~ ID ~ expr ~ RP ~ bindings ^^ { case _ ~ s ~ e ~ _ ~ bs => (s, e) :: bs }

  def bindings_none : Parser[List[(String, Exp)]] =
    success(()) ^^ { case _  => List() }

  def mkDo (es : List[Exp]) : Exp = {
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

  def expr_fun : Parser[Exp] =
    LP ~ FUN ~ LP ~ params ~ RP ~ expr ~ RP ^^
      { case _ ~ _ ~ _ ~ ps ~ _ ~ e ~ _ => new EFunction(gensym(), ps, e) }

  def expr_rec_fun : Parser[Exp] =
    LP ~ FUN ~ ID ~ LP ~ params ~ RP ~ expr ~ RP ^^
      { case _ ~ _ ~ rn ~ _ ~ ps ~ _ ~ e ~ _ => new EFunction(rn, ps, e) }

  def params : Parser[List[String]] =
    ( params_many | params_none ) ^^ { ps => ps }

  def params_many : Parser[List[String]] =
    ID ~ params ^^ { case p ~ ps => p::ps }

  def params_none : Parser[List[String]] =
    success(()) ^^ { p => List() }

  def exprs : Parser[List[Exp]] = 
      ( exprs_many | exprs_none ) ^^ { es => es }

  def exprs_many : Parser[List[Exp]] = 
      expr ~ exprs ^^ { case e ~ es => e :: es }

  def exprs_none : Parser[List[Exp]] = 
      success(()) ^^ { case _ => List() }

  def expr_do : Parser[Exp] = 
    LP ~ DO ~ exprs ~ RP ^^ { case _ ~ _ ~ es ~ _ => mkDo(es) } 

  def expr_transf : Parser[Exp] =
    ( expr_do  ) ^^ { e => e }
          
  def expr_apply : Parser[Exp] =
    LP ~ expr ~ exprs ~ RP ^^ { case _ ~ f ~ es ~ _ => new EApply(f, es) }

  def expr : Parser[Exp] =
     ( atomic | expr_if | expr_fun | expr_rec_fun | expr_let |
       expr_record | expr_field | expr_open |
       expr_class | expr_new | expr_method | expr_applym |
       expr_transf | expr_apply ) ^^ { e => e }

  // RECORDS

  def RECORD : Parser[Unit] = "record" ^^ { s => () } 
  def FIELD : Parser[Unit] = "field" ^^ { s => () } 
  def OPEN : Parser[Unit] = "open" ^^ { s => () }
   
  def expr_record : Parser[Exp] = 
    LP ~ RECORD ~ bindings ~ RP ^^ { case _ ~ _ ~ entries ~ _ => new ERecord(entries) }

  def expr_field : Parser[Exp] = 
    LP ~ FIELD ~ expr ~ ID ~ RP ^^ { case _ ~ _ ~ rec ~ fld ~ _ => new EField(rec, fld) }
    
  def expr_open : Parser[Exp] = 
    LP ~ OPEN ~ expr ~ expr ~ RP ^^ { case _ ~ _ ~ rec ~ body~ _ => new EOpen(rec, body) }

  // OBJECTS

  def CLASS : Parser[Unit] = "class" ^^ { s => () }
  def NEW : Parser[Unit] = "new" ^^ { s => () } 
  def METHOD : Parser[Unit] = "method" ^^ { s => () }
  def APPLYM : Parser[Unit] = "->" ^^ { s => () } 

  def mkClass (params: List[String], fields: List[(String, Exp)], methods: List[(String, List[String], Exp)]) : Exp =  {
    val entries = methods.map((p) => (p._1, new EFunction(gensym(), List("this"), new EFunction(gensym(), p._2, p._3))))
    return new EFunction(gensym(), params, new ELet(fields, new ERecord(entries)))
  }

  def mkNew (cl: Exp, args: List[Exp]) : Exp =
    new EApply(cl, args)

  def mkMethod (record: Exp, field: String) : Exp = {
    val obj = gensym()
    return new ELet(List((obj, record)), new EApply(new EField(new EId(obj), field), List(new EId(obj))))
  }

  def mkApplyM (record: Exp, field: String, args: List[Exp]) : Exp =
    new EApply(mkMethod(record, field), args)

  def expr_class : Parser[Exp] =
    LP ~ CLASS ~ LP ~ params ~ RP ~ LP ~ bindings ~ RP ~ methods ~ RP ^^
      { case _ ~ _ ~ _ ~ ps ~ _ ~ _ ~ fields ~ _ ~ methods ~ _ => mkClass(ps, fields, methods) }

  def methods : Parser[List[(String, List[String], Exp)]] =
    ( methods_some | methods_none) ^^ { m => m }

  def methods_some : Parser[List[(String, List[String], Exp)]] =
    LP ~ ID ~ LP ~ params ~ RP ~ expr ~ RP ~ methods ^^
      { case _ ~ name ~ _ ~ ps ~ _ ~ body ~ _ ~ methods => (name, ps, body)::methods }

  def methods_none : Parser[List[(String, List[String], Exp)]] =
    success(()) ^^ { _ => List() }

  def expr_new : Parser[Exp] =
    LP ~ NEW ~ expr ~ exprs ~ RP ^^ { case _ ~ _ ~ cl ~ args ~ _ => mkNew(cl, args) }

  def expr_method : Parser[Exp] = 
    LP ~ METHOD ~ expr ~ ID ~ RP ^^ { case _ ~ _ ~ rec ~ fld ~ _ => mkMethod(rec, fld) }
    
  def expr_applym : Parser[Exp] = 
    LP ~ APPLYM ~ expr ~ ID ~ exprs ~ RP ^^ { case _ ~ _ ~ rec ~ fld ~ args ~ _ => mkApplyM(rec, fld, args) }
}

def parse (input:String):Exp = {
   val p = new SExpParser
   p.parseAll(p.expr, input) match {
      case p.Success(result,_) => result
      case failure : p.NoSuccess => runtimeError("Cannot parse "+input+": "+failure.msg)
   }  
}


/*
 *  Primitive operations
 *
 */

def checkNumberArgs (vs:List[Value], num: Int) : Unit = {
  if (vs.length != num) {
    runtimeError("Wrong number of arguments " + vs.length.toString() + " - expected " + num.toString())
  }
}

def checkInteger (v : Value) : Unit = {
  if (!v.isInteger()) {
    runtimeError("Value " + v.toString() + " is not of type INTEGER")
  }
}

def checkFraction (v : Value) : Unit = {
  if (!v.isFraction()) {
    runtimeError("Value " + v.toString() + " is not of type FRACTION")
  }
}

def checkBoolean (v : Value) : Unit = {
  if (!v.isBoolean()) {
    runtimeError("Value " + v.toString() + " is not of type BOOLEAN")
  }
}

def checkVector (v : Value) : Unit = {
  if (!v.isVector()) {
    runtimeError("Value " + v.toString() + " is not of type VECTOR")
  }
}

def checkFunction (v : Value) : Unit = {
  if (!v.isFunction()) {
    runtimeError("Value " + v.toString() + " is not of type FUNCTION")
  }
}

def checkRefCell (v : Value) : Unit = {
  if (!v.isRefCell()) {
    runtimeError("Value " + v.toString() + " is not of type REFCELL")
  }
}

def operMinus (vs : List[Value]) : Value = { 
  checkNumberArgs(vs, 1)
  val v1 = vs(0)
  checkFraction(v1)
  return new VFraction(-v1.getNumerator(), v1.getDenominator()).simplify()
}

def operPlus (vs : List[Value]) : Value = { 
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

def operTimes (vs : List[Value]) : Value = { 
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

def operDiv (vs : List[Value]) : Value = { 
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

def operEqual (vs: List[Value]) : Value = {
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


def operLess (vs: List[Value]) : Value = {
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

def operRefCell (vs:List[Value]) : Value = {
  checkNumberArgs(vs, 1)
  val init = vs(0)
  return new VRefCell(init)
}

def operGetRefCell (vs:List[Value]) : Value = {
  checkNumberArgs(vs, 1)
  val r = vs(0)
  checkRefCell(r)
  return r.getRefContent()
}

def operPutRefCell (vs:List[Value]) : Value = {
  checkNumberArgs(vs, 2)
  val r = vs(0)
  val v = vs(1)
  checkRefCell(r)
  r.putRefContent(v)
  return new VBoolean(true)
}

def operPrint (vs:List[Value]) : Value = {
  for (v <- vs) {
    print(v.toDisplay())
    print(" ")
  }
  println(" ")
  return new VBoolean(true)
}

def operCons (vs : List[Value]) : Value = {
  checkNumberArgs(vs, 2)
  val v1 = vs(0)
  val v2 = vs(1)
  checkVector(v2)
  return new VVector(v1 :: v2.getList())
}

def operFirst (vs : List[Value]) : Value = {
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

def operRest (vs : List[Value]) : Value = {
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

def operEmptyP (vs : List[Value]) : Value = {
  checkNumberArgs(vs, 1)
  val v1 = vs(0)
  checkVector(v1)
  return new VBoolean(v1.getList().isEmpty)
}

/* 
 *  Initial environment
 *
 */
  
val initEnv = new Env(List(
  ("true", new VBoolean(true)),
  ("false", new VBoolean(false)),
  ("-", new VPrimitive(operMinus)),
  ("*", new VPrimitive(operTimes)),
  ("+", new VPrimitive(operPlus)),
  ("/", new VPrimitive(operDiv)),
  ("=", new VPrimitive(operEqual)),
  ("<", new VPrimitive(operLess)),
  ("ref", new VPrimitive(operRefCell)),
  ("get", new VPrimitive(operGetRefCell)),
  ("put", new VPrimitive(operPutRefCell)),
  ("print", new VPrimitive(operPrint)),
  ("empty", new VVector(List())),
  ("cons", new VPrimitive(operCons)),
  ("first", new VPrimitive(operFirst)),
  ("rest", new VPrimitive(operRest)),
  ("empty?", new VPrimitive(operEmptyP)),
  ("not", new VRecClosure("", List("a"), new EIf(new EId("a"), new EBoolean(false), new EBoolean(true)), new Env()))
))


/*
 *  Shell 
 *
 */

def shell () : Unit = {

  var env = initEnv
  println("Type #quit to quit")

  while (true) {
    print("FUNC+RECORDS> ")
    val input = scala.io.StdIn.readLine()
    try { 
      // a hack --
      if (input.trim() == "#quit") { 
        return
      }  
      val e = parse(input)
      println(";; Parse: ")
      e.pp(";;  ", false, "")
      val v = e.eval(env)
      println(v.toDisplay())
    } catch { 
      case e : Throwable => println(e)
    }
  }
}

def evalFile (file : String) : Value = {
  var env = initEnv
  val bufferedSource = Source.fromFile(file)
  val content = bufferedSource.getLines.mkString
  bufferedSource.close()
  val e = parse(content)
  println(";; Parse: ")
  e.pp(";;  ", false, "")
  return e.eval(env)
}



//
//
//  RECORD value and abstract representations
//
//


class VRecord (val entries : List[(String, Value)]) extends Value {

  override def toString () : String = {
    val es1 = entries.map((p) => "(" + p._1 + ", " + p._2.toString() + ")")
    val es2 = es1.addString(new StringBuilder(), "", ", ", "").toString()
    "VRecord[" + es2 + "]"
  }

  def toDisplay () : String = {
    val es1 = entries.map((p) => "(" + p._1 + ", " + p._2.toDisplay() + ")")
    val es2 = es1.addString(new StringBuilder(), "", ", ", "").toString()
    "#RECORD[" + es2 + "]"
  }

  override def isRecord () : Boolean = true

  override def getBindings () : List[(String, Value)] =
    entries

  override def lookup  (id: String) : Value = {
    for (entry <- entries) {
      if (entry._1 == id) {
        return entry._2
      }
    }
    runtimeError("Runtime error : unbound field " + id)
  }
}


class ERecord (val entries : List[(String, Exp)]) extends Exp {
  override def toString () : String = {
    val es1 = entries.map((p) => "(" + p._1 + ", " + p._2.toString() + ")")
    val es2 = es1.addString(new StringBuilder(), "", ", ", "").toString()
    "ERecord[" + es2 + "]"
  }

  def eval (env : Env) : Value = {
    val vs = entries.map((p) => (p._1, p._2.eval(env)))
    return new VRecord(vs)
  }
}


class EField (val record : Exp, val field : String) extends Exp {
  override def toString () : String =
    "EField[" + record.toString() + ", " + field + "]"

  def eval (env : Env) : Value = {
    val vr = record.eval(env)
    return vr.lookup(field)
  }
}


class EOpen (val record : Exp, val body : Exp) extends Exp {
  override def toString () : String =
    "EOpen[" + record.toString() + ", " + body.toString() + "]"

  def eval (env : Env) : Value = {
    val vr = record.eval(env)
    var newEnv = env
    for ((id, v) <- vr.getBindings()) {
      newEnv = newEnv.push(id, v)
    }
    return body.eval(newEnv)
  }
}
