/* LECTURE 3, VERSION 2
 *
 * Expression language with different types of values
 *
 * Identifiers and let-bindings (via environments)
 *
 * Top level functions as values
 *
 */



//
//  Values
//


abstract class Value {

  // default behaviors for values
  
  def isInteger () : Boolean = false
  def isBoolean () : Boolean = false
  def isFraction () : Boolean = false
  def isFunction () : Boolean = false

  def getInt () : Int = {
    throw new Exception("Value " + this.toString() + " is not of type INTEGER")
  }

  def getBool () : Boolean = {
    throw new Exception("Value " + this.toString() + " is not of type BOOLEAN")
  }

  def apply1 (v1 : Value, env : Env) : Value = {
    throw new Exception("Value " + this.toString() + " is not of type FUNCTION-1")
  }
  
  def apply2 (v1 : Value, v2 : Value, env : Env) : Value = {
    throw new Exception("Value " + this.toString() + " is not of type FUNCTION-2")
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


class VFunction1 (val param1 : String, val body : Exp) extends Value {

  override def toString () : String =
    "VFunction(" + param1 + ", " + body.toString() + ")"

  override def apply1 (arg1 : Value, env : Env) : Value = {
    val new_env = env.push(param1, arg1)
    return body.eval(new_env)
  }
}


class VFunction2 (val param1 : String, val param2 : String, val body : Exp) extends Value {

  override def toString () : String =
    "VFunction(" + param1 + ", " + param2 + ", " + body.toString() + ")"

  override def apply2 (arg1 : Value, arg2 : Value, env : Env) : Value = {
    val new_env = env.push(param1, arg1).push(param2, arg2)
    return body.eval(new_env)
  }
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
    "EInteger(" + i.toString() + ")"

  def eval (env : Env) : Value =
    new VInteger(i)
}


class EBoolean (val b:Boolean) extends Exp {
  // Boolean literal

  override def toString () : String =
    "EBoolean(" + b.toString() + ")"

  def eval (env : Env) : Value =
    new VBoolean(b)
}


class EPlus (val e1 : Exp, val e2 : Exp) extends Exp {
  // Addition operation

  override def toString () : String =
    "EPlus(" + e1.toString() + "," + e2.toString() + ")"
  
  def eval (env : Env) : Value = {
    val v1 = e1.eval(env)
    val v2 = e2.eval(env)
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
  
  def eval (env : Env) : Value = {
    val v1 = e1.eval(env)
    val v2 = e2.eval(env)
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
    "EId(" + id + ")"

  def eval (env : Env) : Value =
    env.lookup(id)
}


class ELet (val id : String, val e : Exp, val ebody : Exp) extends Exp {

  override def toString () : String =
    "ELet(" + id + ", " + e.toString() + ", " + ebody.toString() + ")"

  def eval (env : Env) : Value = {
    val v = e.eval(env)
    val new_env = env.push(id, v)
    return ebody.eval(new_env)
  }
}


class EApply1 (val fn : String, val arg1 : Exp) extends Exp {

  override def toString () : String =
    "EApply(" + fn + ", " + arg1.toString() + ")"

  def eval (env : Env) : Value = {
    val varg1 = arg1.eval(env)
    val vfn = env.lookup(fn)
    return vfn.apply1(varg1, env)
  }
}


class EApply2 (val fn : String, val arg1 : Exp, val arg2 : Exp) extends Exp {

  override def toString () : String =
    "EApply(" + fn + ", " + arg1.toString() + ", " + arg2.toString() + ")"

  def eval (env : Env) : Value = {
    val varg1 = arg1.eval(env)
    val varg2 = arg2.eval(env)
    val vfn = env.lookup(fn)
    return vfn.apply2(varg1, varg2, env)
  }
}


//  def add1 (x) = x + 1
//  def square (x) = x * x
val env = new Env(List(("add1",
                         new VFunction1("x",
                                        new EPlus(new EId("x"),
                                                  new EInteger(1)))),
                        ("square",
                         new VFunction1("x",
                                        new ETimes(new EId("x"),
                                                   new EId("x")))),
                        ("apply-twice",
                         new VFunction2("f", "x",
                                        new EApply1("f",
                                                    new EApply1("f", new EId("x")))))))


// (new EApply1("square", new EInteger(10))).eval(env)
// (new ELet("y", new EId("square"), new EApply1("y", new EInteger(9)))).eval(env)
// (new EApply2("apply-twice", new EId("square"), new EInteger(9))).eval(env)
