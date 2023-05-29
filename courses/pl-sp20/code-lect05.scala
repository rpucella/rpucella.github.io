//
// To run this code in the shell, you need to provide a pointer to the
// parser combinators library:
//
//  scala-parser-combinators_2.13-1.1.2.jar
//
// The easiest way to do that is to start the shell with:
//
//   scala -classpath scala-parser-combinators_2.13-1.1.2.jar
//
// (assuming scala-parser-combinators_2.13-1.1.2.jar is in the
// the folder where you start the shall -- if not, you need to
// provide a path to the .jar file -- standard stuff)
//


/* LECTURE 5
 *
 * Expression language with different types of values
 *
 * Identifiers and let-bindings (via environments)
 *
 * Functions as values via letfun (static binding)
 *
 * S-expression surface syntax 
 *
 */



import scala.util.parsing.combinator._;


// Function to create a fresh identifier
// (that cannot be expressed at the surface syntax leve)
//
val gensym : () => String = {
  var counter = 0
  () => { 
    val next = counter
    counter += 1
    " _gs_" + next.toString()
  }
}

//
//  Values
//


abstract class Value {

  // default behaviors for values
  
  def isInteger () : Boolean = false
  def isBoolean () : Boolean = false
  def isFunction () : Boolean = false

  def getInt () : Int = {
    throw new Exception("Value " + this.toString() + " is not of type INTEGER")
  }

  def getBool () : Boolean = {
    throw new Exception("Value " + this.toString() + " is not of type BOOLEAN")
  }

  def apply1 (v1 : Value) : Value = {
    throw new Exception("Value " + this.toString() + " is not of type CLOSURE-1")
  }
}


class VInteger (val i:Int) extends Value {

  override def toString () : String =
    "VInteger[" + i.toString() + "]"

  override def isInteger () : Boolean = true
  override def getInt () : Int = i
}


class VBoolean (val b:Boolean) extends Value {

  override def toString () : String =
    "VBoolean[" + b.toString() + "]"

  override def isBoolean () : Boolean = true
  override def getBool () : Boolean = b
}


class VClosure1 (val param1 : String, val body : Exp, val env : Env) extends Value {

  override def toString () : String =
    "VClosure1[" + param1 + ", " + body.toString() + ", " + env.toString() + "]"

  override def apply1 (arg1 : Value) : Value = {
    val new_env = env.push(param1, arg1)
    return body.eval(new_env)
  }
}


class VPrimitive1 (val oper : (Value) => Value) extends Value {

  override def toString () : String = 
    "VPrimitive1[" + Integer.toHexString(hashCode()) + "]"

  override def apply1 (v1 : Value) : Value = oper(v1)
}


class VPrimitive2 (val oper : (Value, Value) => Value) extends Value {

  override def toString () : String = 
    "VPrimitive2[" + Integer.toHexString(hashCode()) + "]"

  override def apply1 (v1 : Value) : Value =
    return new VPrimitive1((v2 : Value) => oper(v1, v2))
}


//
//  Expressions
//

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
    throw new Exception("Runtime error : unbound identifier " + id)
  }
}


abstract class Exp {

  def eval (env : Env) : Value

  def error (msg : String) : Nothing = {
    throw new Exception ("Runtime error: " + msg +
                           "\n   in expression " + this)
  }
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



class EIf (val ec : Exp, val et : Exp, val ee : Exp) extends Exp {
  // Conditional expression

  override def toString () : String =
    "EIf[" + ec.toString() + ", " + et.toString() + ", " + ee.toString() + "]"
  
  def eval (env : Env) : Value = {
    val ev = ec.eval(env)
    if (ev.isBoolean()) {
      if (!ev.getBool()) {
        return ee.eval(env)
      } else {
        return et.eval(env)
      }
    } else {
      error("condition not a Boolean")
    }
  }
}


class EId (val id : String) extends Exp {

  override def toString () : String =
    "EId[" + id + "]"

  def eval (env : Env) : Value =
    env.lookup(id)
}


class ELet (val id : String, val e : Exp, val ebody : Exp) extends Exp {

  override def toString () : String =
    "ELet[" + id + ", " + e.toString() + ", " + ebody.toString() + "]"

  def eval (env : Env) : Value = {
    val v = e.eval(env)
    val new_env = env.push(id, v)
    return ebody.eval(new_env)
  }
}


class EApply1 (val fn : Exp, val arg1 : Exp) extends Exp {

  override def toString () : String =
    "EApply[" + fn + ", " + arg1.toString() + "]"

  def eval (env : Env) : Value = {
    val varg1 = arg1.eval(env)
    val vfn = fn.eval(env)
    return vfn.apply1(varg1)
  }
}


class ELetFun1 (val fn : String, val param1 : String, val fbody : Exp, val ebody : Exp) extends Exp {

  override def toString () : String =
    "ELetFun1[" + fn + ", " + ", " + param1 + ", " + fbody.toString() + ", " +
      ebody.toString() + "]"

  def eval (env : Env) : Value = {
    val vfn = new VClosure1(param1, fbody, env)
    val new_env = env.push(fn, vfn)
    return ebody.eval(new_env)
  }
}




/*
 * PARSER FOR S-EXPRESSIONS
 *
 */


class SExpParser extends RegexParsers { 

   // tokens
   
   def LP : Parser[Unit] = "(" ^^ { s => () }
   def RP : Parser[Unit] = ")" ^^ { s => () }
   def PLUS : Parser[Unit] = "+" ^^ { s => () }
   def TIMES : Parser[Unit] = "*" ^^ { s => () }
   def INT : Parser[Int] = """[0-9]+""".r ^^ { s => s.toInt }
   def TRUE : Parser[Unit] = "true" ^^ { s => () }
   def FALSE : Parser[Unit] = "false" ^^ { s => () }
   def IF : Parser[Unit] = "if" ^^ { s => () }
   def LETFUN : Parser[Unit] = "letfun" ^^ { s => () }
   def LET : Parser[Unit] = "let" ^^ { s => () }
   def ID : Parser[String] = """[*+a-zA-Z-][a-zA-Z0-9_*+-]*""".r ^^ { s => s }
   // transformation tokens
   def AND : Parser[Unit] = "and" ^^ { s => () }
  def OR : Parser[Unit] = "or" ^^ { s => () }
  def FUN : Parser[Unit] = "fun" ^^ { s => () }
  def NEG : Parser[Unit] = "-" ^^ { s => () }

   def mkFun (p : String, e : Exp) : Exp = {
     val fun_name = gensym()
     new ELetFun1(fun_name, p, e, new EId(fun_name))
   }


   // grammar

   def atomic_int : Parser[Exp] = INT ^^ { i => new EInteger(i) }

   def atomic_id : Parser[Exp] =
      ID ^^ { s => new EId(s) }

   def atomic_true : Parser[Exp] =
      TRUE  ^^ { _ => new EBoolean(true) }

   def atomic_false : Parser[Exp] =
      FALSE  ^^ { _ => new EBoolean(false) }

   def atomic : Parser[Exp] =
      ( atomic_int | atomic_true | atomic_false | atomic_id ) ^^ { e => e}

   def expr_if : Parser[Exp] =
      LP ~ IF ~ expr ~ expr ~ expr ~ RP ^^
        { case _ ~ _ ~ e1 ~ e2 ~ e3 ~ _ => new EIf(e1,e2,e3) }

   def expr_let : Parser[Exp] =
      LP ~ LET ~ LP ~ LP ~ ID ~ expr ~ RP ~ RP ~ expr ~ RP ^^
           { case _ ~ _ ~ _ ~ _ ~ s ~ e1 ~ _ ~ _ ~ e2 ~ _ => new ELet(s, e1, e2) }

   def expr_letfun1 : Parser[Exp] =
      LP ~ LETFUN ~ LP ~ LP ~ ID ~ LP ~ ID ~ RP ~ expr ~ RP ~ RP ~ expr ~ RP ^^
           { case _ ~ _ ~ _ ~ _ ~ s ~ _ ~ p ~ _ ~ e1 ~ _ ~ _ ~ e2 ~ _ => new ELetFun1(s, p, e1, e2) }

   def expr_apply1 : Parser[Exp] =
      LP ~ expr ~ expr ~ RP ^^ { case _ ~ f ~ e ~ _ => new EApply1(f, e) }

  def expr_and : Parser[Exp] =
     LP ~ AND ~ expr ~ expr ~ RP ^^ { case _ ~ _ ~ e1 ~ e2 ~ _ => new EIf(e1, e2, new EBoolean(false)) }

   def expr_or : Parser[Exp] =
     LP ~ OR ~ expr ~ expr ~ RP ^^ { case _ ~ _ ~ e1 ~ e2 ~ _ => new EIf(e1, new EBoolean(true), e2) }

  def expr_fun : Parser[Exp] =
    LP ~ FUN ~ LP ~ ID ~ RP ~ expr ~ RP ^^
      { case _ ~ _ ~ _ ~ p ~ _ ~ e ~ _ => mkFun(p, e) }

   def expr_letfun2 : Parser[Exp] =
      LP ~ LETFUN ~ LP ~ LP ~ ID ~ LP ~ ID ~ ID ~ RP ~ expr ~ RP ~ RP ~ expr ~ RP ^^
        { case _ ~ _ ~ _ ~ _ ~ s ~ _ ~ p1 ~ p2 ~ _ ~ e1 ~ _ ~ _ ~ e2 ~ _ =>
            new ELetFun1(s, p1, mkFun(p2, e1), e2) }
 
   def expr_apply2 : Parser[Exp] =
      LP ~ expr ~ expr ~ expr ~ RP ^^ { case _ ~ f ~ e1 ~ e2 ~ _ => new EApply1(new EApply1(f, e1), e2) }

  def expr_fun2 : Parser[Exp] =
    LP ~ FUN ~ LP ~ ID ~ ID ~ RP ~ expr ~ RP ^^
      { case _ ~ _ ~ _ ~ p1 ~ p2 ~ _ ~ e ~ _ => mkFun(p1, mkFun(p2, e)) }

   def expr_transf : Parser[Exp] =
      ( expr_and | expr_or | expr_fun | expr_fun2 | expr_letfun2 | expr_apply2 ) ^^ { e => e }
           
   def expr : Parser[Exp] =
      ( atomic | expr_transf | expr_if | expr_let | expr_letfun1 | expr_apply1 ) ^^ { e => e }
}


def parse (input:String):Exp = {
   val p = new SExpParser
   p.parseAll(p.expr, input) match {
      case p.Success(result,_) => result
      case failure : p.NoSuccess => throw new Exception("Cannot parse "+input+": "+failure.msg)
   }  
}


/*
 * A simple shell
 *
 */

def operMinus (v1 : Value) : Value = { 
  if (v1.isInteger()) { 
    return new VInteger(-(v1.getInt()))
  } else {
    throw new Exception("cannot negate a non-integer")
  }
}

def operSquare (v1 : Value) : Value = { 
  if (v1.isInteger()) { 
    return new VInteger(v1.getInt() * v1.getInt())
  } else {
    throw new Exception("cannot square a non-integer")
  }
}

def operPlus (v1 : Value, v2 : Value) : Value = { 
    if (v1.isInteger() && v2.isInteger()) {
      return new VInteger(v1.getInt() + v2.getInt() )
    } else {
      throw new Exception("cannot add values of different types")
    }
}

def operTimes (v1 : Value, v2 : Value) : Value = { 
    if (v1.isInteger() && v2.isInteger()) {
      return new VInteger(v1.getInt() * v2.getInt() )
    } else {
      throw new Exception("cannot multiply values of different types")
    }
}

val initEnv = new Env(List(
  ("-", new VPrimitive1(operMinus)),
  ("**", new VPrimitive1(operSquare)),
  ("*", new VPrimitive2(operTimes)),
  ("+", new VPrimitive2(operPlus))
))

// Use -Xnojline when starting Scala to get readLine() to behave right...

def shell () : Unit = {

    while (true) {
       print("demo/lect05> ")
       val input = scala.io.StdIn.readLine()
       val e = parse(input)
       println("Parsed expression: " + e.toString())
       val v = e.eval(initEnv)
       println(v)
    }
}
