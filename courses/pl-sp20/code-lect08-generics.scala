// LECTURE 8 - STATIC TYPES (WITH GENERICS)

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
  throw new Exception("Runtime error: " + msg)
}

def typeError (msg: String) : Nothing = {
  throw new Exception("Type error: " + msg)
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

  def lookup (s: String) : Value = {
    runtimeError("Value " + this.toString() + " is not of type RECORD")
  }
  
  def apply (vs: List[Value]) : Value = {
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


class VRecClosure (val self: String, val params: List[String], val body:Exp, val env:Env[Value]) extends Value {

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
     new_env = new_env.push(self, this)
     return body.eval(new_env)
  }
}


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
    runtimeError("unknown field " + id)
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
}


abstract class Exp {

  def eval (env : Env[Value]) : Value
  def typeOf (tenv : Env[Type], tvenv: Env[Type]) : Type
}


class EInteger (val i:Int) extends Exp {
  // integer literal

  override def toString () : String =
    "EInteger[" + i.toString() + "]"

  def eval (env : Env[Value]) : Value =
    new VInteger(i)

  def typeOf (tenv : Env[Type], tvenv: Env[Type]) : Type =
    TNum
}


class EBoolean (val b:Boolean) extends Exp {
  // Boolean literal

  override def toString () : String =
    "EBoolean[" + b.toString() + "]"

  def eval (env : Env[Value]) : Value =
    new VBoolean(b)

  def typeOf (tenv : Env[Type], tvenv: Env[Type]) : Type =
    TBool
}


class EPrimitive (val oper : (List[Value]) => Value) extends Exp { 

  override def toString () : String = 
    "EPrimitive[" + Integer.toHexString(oper.hashCode()) + "]"

  def eval (env : Env[Value]) : Value =
    new VPrimitive(oper)

  def typeOf (tenv : Env[Type], tvenv: Env[Type]) : Type =
    throw new Exception("typeOf not support on EPrimitive")
}
 

class EIf (val ec : Exp, val et : Exp, val ee : Exp) extends Exp {
  // Conditional expression

  override def toString () : String =
    "EIf[" + ec.toString() + ", " + et.toString() + ", " + ee.toString() + "]"

  def eval (env : Env[Value]) : Value = {
    val ev = ec.eval(env)
    if (!ev.getBool()) {
      return ee.eval(env)
    } else {
      return et.eval(env)
    }
  }

  def typeOf (tenv : Env[Type], tvenv: Env[Type]) : Type = {
    val tc = ec.typeOf(tenv, tvenv)
    if (!tc.isBool()) {
      typeError("Expected Boolean condition")
    }
    val tt = et.typeOf(tenv, tvenv)
    val te = ee.typeOf(tenv, tvenv)
    return tt.compare(te)
  }
}


class EId (val id : String) extends Exp {

  override def toString () : String =
    "EId[" + id + "]"

  def eval (env : Env[Value]) : Value =
    env.lookup(id)

  def typeOf (tenv : Env[Type], tvenv: Env[Type]) : Type =
    tenv.lookup(id)
}


class EApply (val fn : Exp, val targs: List[Type], val args : List[Exp]) extends Exp {

  override def toString () : String = {
    if (args.isEmpty) {
      return "EApply[" + fn + "]"
    } else { 
      val sArgs = args.map(_.toString()).addString(new StringBuilder(), "", ", ", "").toString()
      return "EApply[" + fn + ", " + sArgs + "]"
    }
  }
  
  def eval (env : Env[Value]) : Value = {
    val vfn = fn.eval(env)
    val vargs = args.map((e:Exp) => e.eval(env))
    return vfn.apply(vargs)
  }

  def typeOf (tenv : Env[Type], tvenv: Env[Type]) : Type = {
    var tfn = fn.typeOf(tenv, tvenv)
    if (!tfn.isFun()) {
      typeError("Application expected FUN type but received: " + tfn.toDisplay())
    }
    if (tfn.getTArgs().length != targs.length)
      typeError("Wrong number of type arguments to function")
    ///println("Pre-substitution type = " + tfn.toString())
    for ((n, t) <- tfn.getTArgs().zip(targs))
      tfn = new TFun(List(), tfn.getArgs().map((x) => x.substitute(n, t)), tfn.getResult().substitute(n, t))
    ///println("Post-substitution type = " + tfn.toString())
    val argTs = args.map((e:Exp) => e.typeOf(tenv, tvenv))
    if (tfn.getArgs().length != argTs.length) {
      typeError("Wrong number of arguments to function")
    }
    val ts = tfn.getArgs().zip(argTs).map((p) => p._1.compare(p._2))
    return tfn.getResult()
  }
}


class EFunction (val recName : String, val params : List[String], val body : Exp, val tvars: List[String], val tparams : List[Type], val tresult : Type) extends Exp {

  override def toString () : String = {
    val ps = params.addString(new StringBuilder(), "(", ", ", ")").toString()
    "EFunction[" + recName + ", " + ps + ", " + body.toString() + "]"
  }
  
  def eval (env : Env[Value]) : Value = 
    new VRecClosure(recName, params, body, env)

  def typeOf (tenv : Env[Type], tvenv: Env[Type]) : Type = {
    var newTVEnv = tvenv
    for (n <- tvars)
      newTVEnv = newTVEnv.push(n, TNum)  // type doesn't matter...
    var newTEnv = tenv
    for ((p, t) <- params.zip(tparams))
      newTEnv = newTEnv.push(p, t)
    // push the specified function type as the type bound to identifier recName
    newTEnv = newTEnv.push(recName, new TFun(tvars, tparams, tresult))
    val tres = tresult.compare(body.typeOf(newTEnv, tvenv))
    return new TFun(tvars, tparams, tres)
  }
}


class ELet (val bindings : List[(String, Exp)], val body : Exp) extends Exp {

  override def toString () : String = {
    val bs1 = bindings.map((p) => "(" + p._1 + ", " + p._2.toString() + ")")
    val bs2 = bs1.addString(new StringBuilder(), "", ", ", "").toString()
    "ELet[" + bs2 + ", " + body.toString() + "]"
  }

  def eval (env : Env[Value]) : Value = {
    var newEnv = env
    for (binding <- bindings) {
      val v = binding._2.eval(env)
      newEnv = newEnv.push(binding._1, v)
    }
    return body.eval(newEnv)
  }

  def typeOf (tenv : Env[Type], tvenv: Env[Type]) : Type = {
    var newTEnv = tenv
    for (binding <- bindings) {
      val t = binding._2.typeOf(tenv, tvenv)
      newTEnv = newTEnv.push(binding._1, t)
    }
    return body.typeOf(newTEnv, tvenv)
  }
}


class ERecord (val entries : List[(String, Exp)]) extends Exp {
  override def toString () : String = {
    val es1 = entries.map((p) => "(" + p._1 + ", " + p._2.toString() + ")")
    val es2 = es1.addString(new StringBuilder(), "", ", ", "").toString()
    "ERecord[" + es2 + "]"
  }

  def eval (env : Env[Value]) : Value = {
    val vs = entries.map((p) => (p._1, p._2.eval(env)))
    return new VRecord(vs)
  }

  def typeOf (tenv : Env[Type], tvenv: Env[Type]) : Type =
    runtimeError("not implemented")
}


class EField (val record : Exp, val field : String) extends Exp {
  override def toString () : String =
    "EField[" + record.toString() + ", " + field + "]"

  def eval (env : Env[Value]) : Value = {
    val vr = record.eval(env)
    return vr.lookup(field)
  }

  def typeOf (tenv : Env[Type], tvenv: Env[Type]) : Type =
    runtimeError("not implemented")
}


class EOpen (val record : Exp, val body : Exp) extends Exp {
  override def toString () : String =
    "EOpen[" + record.toString() + ", " + body.toString() + "]"

  def eval (env : Env[Value]) : Value = {
    val vr = record.eval(env)
    var newEnv = env
    for ((id, v) <- vr.getBindings()) {
      newEnv = newEnv.push(id, v)
    }
    return body.eval(newEnv)
  }

  def typeOf (tenv : Env[Type], tvenv: Env[Type]) : Type =
    runtimeError("not implemented")
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

  def RECORD : Parser[Unit] = "record" ^^ { s => () } 
  def FIELD : Parser[Unit] = "field" ^^ { s => () } 
  def OPEN : Parser[Unit] = "open" ^^ { s => () }
   

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

  def LB : Parser[Unit] = "[" ^^ { s => () }
  def RB : Parser[Unit] = "]" ^^ { s => () }

  def expr_fun : Parser[Exp] =
    LP ~ FUN ~ LP ~ params_types ~ typ ~ RP ~ expr ~ RP ^^
      { case _ ~ _ ~ _ ~ pts ~ t ~ _ ~ e ~ _ => new EFunction(gensym(), pts.map((p) => p._1), e, List(), pts.map((p) => p._2), t) }
      	
  def expr_rec_fun : Parser[Exp] =
    LP ~ FUN ~ ID ~ LP ~ params_types ~ typ ~ RP ~ expr ~ RP ^^
      { case _ ~ _ ~ rn ~ _ ~ pts ~ t ~ _ ~ e ~ _ => new EFunction(rn, pts.map((p) => p._1), e, List(), pts.map((p) => p._2), t) }

  def expr_fun_tvs : Parser[Exp] =
    LP ~ FUN ~ LB ~ params ~ RB ~ LP ~ params_types ~ typ ~ RP ~ expr ~ RP ^^
      { case _ ~ _ ~ _ ~ tvs ~ _ ~ _ ~ pts ~ t ~ _ ~ e ~ _ => new EFunction(gensym(), pts.map((p) => p._1), e, tvs, pts.map((p) => p._2), t) }
      	
  def expr_rec_fun_tvs : Parser[Exp] =
    LP ~ FUN ~ ID ~ LB ~ params ~ RB ~ LP ~ params_types ~ typ ~ RP ~ expr ~ RP ^^
      { case _ ~ _ ~ rn ~ _ ~ tvs ~ _ ~ _ ~ pts ~ t ~ _ ~ e ~ _ => new EFunction(rn, pts.map((p) => p._1), e, tvs, pts.map((p) => p._2), t) }

  def params : Parser[List[String]] =
    ( params_many | params_none ) ^^ { ps => ps }

  def params_many : Parser[List[String]] =
    ID ~ params ^^ { case p ~ ps => p::ps }

  def params_none : Parser[List[String]] =
    success(()) ^^ { case _ => List() }

  def params_types : Parser[List[(String, Type)]] =
    ( params_types_many | params_types_none ) ^^ { ps => ps }

  def params_types_many : Parser[List[(String, Type)]] =
      LP ~ ID ~ typ ~ RP ~ params_types ^^ { case _ ~ p ~ t ~ _ ~ ps => (p, t) :: ps }

  def params_types_none : Parser[List[(String, Type)]] =
    success(()) ^^ { case _ => List() }

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
          
  def expr_apply_tvs : Parser[Exp] =
    LP ~ expr ~ LB ~ typs ~ RB ~ exprs ~ RP ^^ { case _ ~ f ~ _ ~ ts ~ _ ~ es ~ _ => new EApply(f, ts, es) }

  def expr_apply : Parser[Exp] =
    LP ~ expr ~ exprs ~ RP ^^ { case _ ~ f ~ es ~ _ => new EApply(f, List(), es) }

  def expr_record : Parser[Exp] = 
    LP ~ RECORD ~ bindings ~ RP ^^ { case _ ~ _ ~ entries ~ _ => new ERecord(entries) }

  def expr_field : Parser[Exp] = 
    LP ~ FIELD ~ expr ~ ID ~ RP ^^ { case _ ~ _ ~ rec ~ fld ~ _ => new EField(rec, fld) }
    
  def expr_open : Parser[Exp] = 
    LP ~ OPEN ~ expr ~ expr ~ RP ^^ { case _ ~ _ ~ rec ~ body~ _ => new EOpen(rec, body) }

  def expr : Parser[Exp] =
     ( atomic | expr_if | expr_fun | expr_rec_fun | expr_fun_tvs | expr_rec_fun_tvs | expr_let |
       expr_record | expr_field | expr_open |
       expr_transf | expr_apply_tvs | expr_apply ) ^^ { e => e }


  // types

  def TNUM : Parser[Unit] = ":num" ^^ { s => () }
  def TBOOL : Parser[Unit] = ":bool" ^^ { s => () }
  def TVEC : Parser[Unit] = ":vec" ^^ { s => () }
  def TREF : Parser[Unit] = ":ref" ^^ { s => () }
  def TFUN : Parser[Unit] = ":fun" ^^ { s => () }

  def typ : Parser[Type] =
     ( typ_num | typ_bool | typ_fun | typ_vec | typ_ref | typ_var ) ^^ { t => t }

  def typ_var : Parser[Type] =
    ID ^^ { case n => new TVar(n) }
    
  def typ_num : Parser[Type] =
    TNUM ^^ { case _ => TNum }

  def typ_bool : Parser[Type] =
    TBOOL ^^ { case _ => TBool }

  def typ_vec : Parser[Type] =
    LP ~ TVEC ~ typ ~ RP ^^ { case _ ~ _ ~ t ~ _ => new TVec(t) }

  def typ_ref : Parser[Type] =
    LP ~ TREF ~ typ ~ RP ^^ { case _ ~ _ ~ t ~ _ => new TRef(t) }

  def typ_fun : Parser[Type] =
    LP ~ TFUN ~ LP ~ typs ~ RP ~ typ ~ RP ^^ { case _ ~ _ ~ _ ~ ts ~ _ ~ t ~ _ => new TFun(List(), ts, t) }

  def typs : Parser[List[Type]] =
    ( typs_many | typs_none ) ^^ { ts => ts }

  def typs_many : Parser[List[Type]] =
    typ ~ typs ^^ { case t ~ ts => t :: ts }

  def typs_none : Parser[List[Type]] =
    success(()) ^^ { case _ => List() } 
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

def operMinus (vs : List[Value]) : Value = { 
  val v1 = vs(0)
  return new VFraction(-v1.getNumerator(), v1.getDenominator()).simplify()
}

def operPlus (vs : List[Value]) : Value = { 
  val v1 = vs(0)
  val v2 = vs(1)
  val n1 = v1.getNumerator()
  val n2 = v2.getNumerator()
  val d1 = v1.getDenominator()
  val d2 = v2.getDenominator()
  return new VFraction(n1 * d2 + n2 * d1, d1 * d2).simplify()
}

def operTimes (vs : List[Value]) : Value = { 
  val v1 = vs(0)
  val v2 = vs(1)
  val n1 = v1.getNumerator()
  val n2 = v2.getNumerator()
  val d1 = v1.getDenominator()
  val d2 = v2.getDenominator()
  return new VFraction(n1 * n2, d1 * d2).simplify()
}

def operDiv (vs : List[Value]) : Value = { 
  val v1 = vs(0)
  val v2 = vs(1)
  val n1 = v1.getNumerator()
  val n2 = v2.getNumerator()
  val d1 = v1.getDenominator()
  val d2 = v2.getDenominator()
  return new VFraction(n1 * d2, n2 * d1).simplify()
}

def operEqual (vs: List[Value]) : Value = {
  val v1 = vs(0)
  val v2 = vs(1)
  val n1 = v1.getNumerator()
  val n2 = v2.getNumerator()
  val d1 = v1.getDenominator()
  val d2 = v2.getDenominator()
  return new VBoolean(n1 == n2 && d1 == d2)
}


def operLess (vs: List[Value]) : Value = {
  val v1 = vs(0)
  val v2 = vs(1)
  val n1 = v1.getNumerator()
  val n2 = v2.getNumerator()
  val d1 = v1.getDenominator()
  val d2 = v2.getDenominator()
  return new VBoolean(n1 * d2 < n2 * d1)
}

def operRefCell (vs:List[Value]) : Value = {
  val init = vs(0)
  return new VRefCell(init)
}

def operGetRefCell (vs:List[Value]) : Value = {
  val r = vs(0)
  return r.getRefContent()
}

def operPutRefCell (vs:List[Value]) : Value = {
  val r = vs(0)
  val v = vs(1)
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

def operEmpty (vs : List[Value]) : Value = {
  return new VVector(List())
}

def operCons (vs : List[Value]) : Value = {
  val v1 = vs(0)
  val v2 = vs(1)
  return new VVector(v1 :: v2.getList())
}

def operFirst (vs : List[Value]) : Value = {
  val v1 = vs(0)
  val l = v1.getList()
  if (l.isEmpty) { 
    runtimeError("cannot apply first to an empty VECTOR")
  } else {
    return l.head
  }
}

def operRest (vs : List[Value]) : Value = {
  val v1 = vs(0)
  val l = v1.getList()
  if (l.isEmpty) {
    runtimeError("cannot apply rest to an empty VECTOR")
  } else {
    return new VVector(l.tail)
  }
}

def operEmptyP (vs : List[Value]) : Value = {
  val v1 = vs(0)
  return new VBoolean(v1.getList().isEmpty)
}

/* 
 *  Initial environment
 *
 */

val initBindings = List(
  ("true", new VBoolean(true), TBool),
  ("false", new VBoolean(false), TBool),
  ("-", new VPrimitive(operMinus), new TFun(List(), List(TNum), TNum)),
  ("*", new VPrimitive(operTimes), new TFun(List(), List(TNum, TNum), TNum)),
  ("+", new VPrimitive(operPlus), new TFun(List(), List(TNum, TNum), TNum)),
  ("/", new VPrimitive(operDiv), new TFun(List(), List(TNum, TNum), TNum)),
  ("=", new VPrimitive(operEqual), new TFun(List(), List(TNum, TNum), TBool)),
  ("<", new VPrimitive(operLess), new TFun(List(), List(TNum, TNum), TBool)),
  ("ref", new VPrimitive(operRefCell), new TFun(List("A"), List(new TVar("A")), new TRef(new TVar("A")))),
  ("get", new VPrimitive(operGetRefCell), new TFun(List("A"), List(new TRef(new TVar("A"))), new TVar("A"))),
  ("put", new VPrimitive(operPutRefCell), new TFun(List("A"), List(new TRef(new TVar("A")), new TVar("A")), TBool)),
  ("print", new VPrimitive(operPrint), new TFun(List("A"), List(new TVar("A")), TBool)),
  ("empty", new VPrimitive(operEmpty), new TFun(List("A"), List(), new TVec(new TVar("A")))),
  ("cons", new VPrimitive(operCons), new TFun(List("A"), List(new TVar("A"), new TVec(new TVar("A"))), new TVec(new TVar("A")))),
  ("first", new VPrimitive(operFirst), new TFun(List("A"), List(new TVec(new TVar("A"))), new TVar("A"))),
  ("rest", new VPrimitive(operRest), new TFun(List("A"), List(new TVec(new TVar("A"))), new TVec(new TVar("A")))),
  ("empty?", new VPrimitive(operEmptyP), new TFun(List("A"), List(new TVec(new TVar("A"))), TBool)),
  ("not", new VRecClosure("", List("a"), new EIf(new EId("a"), new EBoolean(false), new EBoolean(true)), new Env()), new TFun(List(), List(TBool), TBool))
)
  
val initEnv = new Env[Value](initBindings.map((p) => (p._1, p._2)))
val initTEnv = new Env[Type](initBindings.map((p) => (p._1, p._3)))


/*
 *  Shell 
 *
 */

def shell () : Unit = {

  var env = initEnv
  var tenv = initTEnv
  println("Type #quit to quit")

  while (true) {
    print("FUNC+TYPES> ")
    val input = scala.io.StdIn.readLine()
    try { 
      // a hack --
      if (input.trim() == "#quit") { 
        return
      }  
      val e = parse(input)
      val t = e.typeOf(tenv, new Env())
      println(";; Type: " + t.toDisplay())
      val v = e.eval(env)
      println(v.toDisplay())
    } catch { 
      case e : Throwable => println(e)
    }
  }
}

def evalFile (file : String) : Value = {
  val env = initEnv
  val tenv = initTEnv
  val bufferedSource = Source.fromFile(file)
  val content = bufferedSource.getLines.mkString
  bufferedSource.close()
  val e = parse(content)
  val t = e.typeOf(tenv, new Env())
  println(";; Type: " + t.toDisplay())
  return e.eval(env)
}




////////////////////////////////////////////////////////////
// TYPES

abstract class Type {
  def toDisplay () : String
  def isNum () : Boolean = false
  def isBool () : Boolean = false
  def isFun () : Boolean = false
  def isVec () : Boolean = false
  def isRef () : Boolean = false
  def isVar () : Boolean = false
  def getArgs () : List[Type] = 
    typeError("Type " + this.toDisplay() + " is not a FUN type")
  def getTArgs () : List[String] = 
    typeError("Type " + this.toDisplay() + " is not a FUN type")
  def getResult () : Type =
    typeError("Type " + this.toDisplay() + " is not a FUN type")
  def getItem () : Type =
    typeError("Type " + this.toDisplay() + " is not a VEC or REF type")
  def getName () : String =
    typeError("Type " + this.toDisplay() + " is not a type variable")
  def compare (t : Type) : Type =
    typeError("expected type " + this.toDisplay() + " but received: " + t.toDisplay())
  def substitute (n: String, t: Type): Type
}


object TNum extends Type {
  override def toString () : String =
    "TNum"

  def toDisplay () : String =
    ":num"

  override def isNum () : Boolean = true
  override def compare (t : Type) : Type =
    if (t.isNum()) this else super.compare(t)

  def substitute (n:String, t: Type): Type =
    TNum
}


object TBool extends Type {
  override def toString () : String =
    "TBool"

  def toDisplay () : String =
    ":bool"

  override def isBool () : Boolean = true
  override def compare (t : Type) : Type =
    if (t.isBool()) this else super.compare(t)
  def substitute (n:String, t: Type): Type =
    TBool
}


class TFun (val targs: List[String], val args : List[Type], val result : Type) extends Type {
  override def toString () : String = { 
    val ts = args.addString(new StringBuilder(), "(", ", ", ")").toString()
    var tvs = targs.addString(new StringBuilder(), "[", " ", "]").toString()
    "TFun[" + tvs + ", " + ts + ", " + result.toString() + "]"
  }

  def toDisplay () : String = {
    val ts = args.map((t) => t.toDisplay()).addString(new StringBuilder(), "(", " ", ")").toString()
    var tvs = targs.addString(new StringBuilder(), "[", " ", "]").toString()
    "(:fun " + tvs + " " + ts + " " + result.toDisplay() + ")"
  }

  override def isFun () : Boolean = true

  override def compare (t : Type) : Type = {
    if (!t.isFun() || args.length != t.getArgs().length || targs.length != t.getTArgs().length)
      return super.compare(t)
    val res = result.compare(t.getResult())
    val ts = args.zip(t.getArgs()).map((p) => p._1.compare(p._2))
    val tvs = targs.zip(t.getTArgs()).map((p) => p._1 == p._2)
    return this
  }

  override def getArgs () : List[Type] = args
  override def getTArgs () : List[String] = targs
  override def getResult () : Type = result
  
  def substitute (n: String, t: Type): Type = {
    if (targs.contains(n)) {
      return this
    }
    return new TFun(targs, args.map((x) => x.substitute(n, t)), result.substitute(n, t))
  }
}


class TVec (val item: Type) extends Type {
  override def toString () : String =
    "TVec[" + item.toString() + "]"

  def toDisplay () : String =
    "(:vec " + item.toDisplay() + ")"

  override def isVec () : Boolean = true
    
  override def compare (t : Type) : Type = {
    if (!t.isVec())
      super.compare(t)
    val titem = item.compare(t.getItem())
    return this
  }
  override def getItem () : Type = item
  def substitute (n: String, t: Type): Type =
    new TVec(item.substitute(n, t))
}


class TRef (val item: Type) extends Type {
  override def toString () : String =
    "TRef[" + item.toString() + "]"

  def toDisplay () : String =
    "(:ref " + item.toDisplay() + ")"

  override def isRef () : Boolean = true

   override def compare (t : Type) : Type = {
    if (!t.isRef())
      super.compare(t)
    val titem = item.compare(t.getItem())
    return this
  }
  override def getItem () : Type = item
  def substitute (n: String, t: Type): Type =
    new TRef(item.substitute(n, t))
}


class TVar (val name: String) extends Type {
  override def toString () : String =
    "TVar[" + name + "]"

  def toDisplay () : String =
    name

  override def isVar () : Boolean = true
    
  override def compare (t : Type) : Type = {
    if (!t.isVar() || name != t.getName())
      super.compare(t)
    return this
  }
	
  override def getName () : String = name
  def substitute (n: String, t: Type): Type =
    if (n == name) t else this
}