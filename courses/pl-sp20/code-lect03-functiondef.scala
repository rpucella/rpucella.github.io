
/* LECTURE 3, VERSION 1
 *
 * Expression language with different types of values
 *
 * Identifiers and let-bindings (via environments)
 *
 * Top level functions as function definitions
 *
 */



//
//  Values
//


abstract class Value {

  // default behaviors for values
  
  def isInteger () : Boolean = false
  def isBoolean () : Boolean = false

  def getInt () : Int = {
    throw new Exception("Value " + this.toString() + " is not of type INTEGER")
  }

  def getBool () : Boolean = {
    throw new Exception("Value " + this.toString() + " is not of type BOOLEAN")
  }
}


class VInteger (val i:Int) extends Value {

  override def toString () : String =
    "VInteger(" + i.toString() + ")"

  override def isInteger () : Boolean = true
  override def getInt () : Int = i
}


class VBoolean (val b:Boolean) extends Value {

  override def toString () : String =
    "VBoolean(" + b.toString() + ")"

  override def isBoolean () : Boolean = true
  override def getBool () : Boolean = b
}


//
//  Expressions
//

class Env (val content: List[(String, Value)]) {

  override def toString () : String = {
    var result = ""
    for (entry <- content) {
      result = result + "(" + entry._1 + " <- " + entry._2 + ") "
    }
    return result
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


class FEnv (val content: List[(String, FunctionDef)]) {

  override def toString () : String = {
    var result = ""
    for (entry <- content) {
      result = result + "(" + entry._1 + " <- " + entry._2 + ") "
    }
    return result
  }

  def lookup (id : String) : FunctionDef = {
    for (entry <- content) {
      if (entry._1 == id) {
        return entry._2
      }
    }
    throw new Exception("Runtime error : unbound function identifier " + id)
  }
}


abstract class Exp {

  def eval (env : Env, fenv : FEnv) : Value

  def error (msg : String) : Nothing = {
    throw new Exception ("Runtime error: " + msg +
                           "\n   in expression " + this)
  }
}


class FunctionDef (val param : String, val body : Exp) {

  override def toString () : String =
    "FunctionDef(" + param + ", " + body.toString() + ")"

  def apply (arg : Value, fenv : FEnv) : Value = 
    body.eval(new Env(List((param, arg))), fenv)
}



class EInteger (val i:Int) extends Exp {
  // integer literal

  override def toString () : String =
    "EInteger(" + i.toString() + ")"

  def eval (env : Env, fenv : FEnv) : Value =
    new VInteger(i)
}


class EBoolean (val b:Boolean) extends Exp {
  // Boolean literal

  override def toString () : String =
    "EBoolean(" + b.toString() + ")"

  def eval (env : Env, fenv : FEnv) : Value =
    new VBoolean(b)
}


class EPlus (val e1 : Exp, val e2 : Exp) extends Exp {
  // Addition operation

  override def toString () : String =
    "EPlus(" + e1.toString() + "," + e2.toString() + ")"
  
  def eval (env : Env, fenv : FEnv) : Value = {
    val v1 = e1.eval(env, fenv)
    val v2 = e2.eval(env, fenv)
    if (v1.isInteger() && v2.isInteger()) {
      return new VInteger(v1.getInt() + v2.getInt() )
    } else {
      error("cannot add values of different types")
    }
  }
}


class ETimes (val e1 : Exp, val e2 : Exp) extends Exp {
  // Multiplication operation

  override def toString () : String =
    "ETimes(" + e1.toString() + "," + e2.toString() + ")"
  
  def eval (env : Env, fenv : FEnv) : Value = {
    val v1 = e1.eval(env, fenv)
    val v2 = e2.eval(env, fenv)
    if (v1.isInteger() && v2.isInteger()) {
      return new VInteger(v1.getInt() * v2.getInt() )
    } else {
      error("cannot multiply values of different types")
    }
  }
}


class EIf (val ec : Exp, val et : Exp, val ee : Exp) extends Exp {
  // Conditional expression

  override def toString () : String =
    "EIf(" + ec.toString() + ", " + et.toString() + ", " + ee.toString() + ")"
  
  def eval (env : Env, fenv : FEnv) : Value = {
    val ev = ec.eval(env, fenv)
    if (ev.isBoolean()) {
      if (!ev.getBool()) {
        return ee.eval(env, fenv)
      } else {
        return et.eval(env, fenv)
      }
    } else {
      error("condition not a Boolean")
    }
  }
}


class EId (val id : String) extends Exp {

  override def toString () : String =
    "EId(" + id + ")"

  def eval (env : Env, fenv : FEnv) : Value =
    env.lookup(id)
}


class ELet (val id : String, val e : Exp, val ebody : Exp) extends Exp {

  override def toString () : String =
    "ELet(" + id + ", " + e.toString() + ", " + ebody.toString() + ")"

  def eval (env : Env, fenv : FEnv) : Value = {
    val v = e.eval(env, fenv)
    val new_env = env.push(id, v)
    return ebody.eval(new_env, fenv)
  }
}


class EApply (val fn : String, val arg : Exp) extends Exp {

  override def toString () : String =
    "EApply(" + fn + ", " + arg.toString() + ")"

  def eval (env : Env, fenv : FEnv) : Value = {
    val varg = arg.eval(env, fenv)
    val df = fenv.lookup(fn)
    return df.apply(varg, fenv)
  }
}


//  def add1 (x) = x + 1
//  def square (x) = x * x
val fenv = new FEnv(List(("add1",
                          new FunctionDef("x",
                                          new EPlus(new EId("x"),
                                                    new EInteger(1)))),
                         ("square",
                          new FunctionDef("x",
                                          new ETimes(new EId("x"),
                                                     new EId("x"))))))

