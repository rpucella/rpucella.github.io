
/* LECTURE 2, VERSION 2
 *
 * Expression language with different types of values
 *
 * Identifiers and let-bindings (via environments)
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

  def getInt () : Int = {
    throw new Exception("Value " + this.toString() + " is not of type INTEGER")
  }

  def getBool () : Boolean = {
    throw new Exception("Value " + this.toString() + " is not of type BOOLEAN")
  }

  def getNum () : Int = {
    throw new Exception("Value " + this.toString() + " is not of type FRACTION")
  }

  def getDen () : Int = {
    throw new Exception("Value " + this.toString() + " is not of type FRACTION")
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


class VFraction (val num:Int, val den:Int) extends Value {

  override def toString () : String =
    "VFraction(" + num.toString() + ", " + den.toString() + ")"
  
  override def isFraction () : Boolean = true
  override def getNum () : Int = num
  override def getDen () : Int = den
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
    throw new Exception("Runtime error : unbound identifier "+id)
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


class EFraction (val num:Int, val den:Int) extends Exp {
  // fraction literal

  override def toString () : String =
    "EFraction(" + num.toString() + ", " + den.toString() + ")"

  def eval (env : Env) : Value =
    new VFraction(num, den)
}



class EPlus (val e1 : Exp, val e2 : Exp) extends Exp {
  // Addition operation

  override def toString () : String =
    "EPlus(" + e1.toString() + "," + e2.toString() + ")"
  
  def eval (env:Env) : Value = {
    val v1 = e1.eval(env)
    val v2 = e2.eval(env)
    if (v1.isInteger() && v2.isInteger()) {
      return new VInteger(v1.getInt() + v2.getInt() )
    } else if (v1.isFraction() && v2.isFraction()) {
      val new_num = v1.getNum() * v2.getDen() + v2.getNum() * v1.getDen()
      val new_den = v1.getDen()*v2.getDen()
      return new VFraction(new_num, new_den)
    } else {
      error("cannot add values of different types")
    }
  }
}


class ETimes (val e1 : Exp, val e2 : Exp) extends Exp {
  // Multiplication operation

  override def toString () : String =
    "ETimes(" + e1.toString() + "," + e2.toString() + ")"
  
  def eval (env:Env) : Value = {
    val v1 = e1.eval(env)
    val v2 = e2.eval(env)
    if (v1.isInteger() && v2.isInteger()) {
      return new VInteger(v1.getInt() * v2.getInt() )
    } else if (v1.isFraction() && v2.isFraction()) {
      val new_num = v1.getNum() * v2.getNum()
      val new_den = v1.getDen() * v2.getDen()
      return new VFraction(new_num, new_den)
    } else {
      error("cannot multiply values of different types")
    }
  }
}


class EIf (val ec : Exp, val et : Exp, val ee : Exp) extends Exp {
  // Conditional expression

  override def toString () : String =
    "EIf(" + ec.toString() + ", " + et.toString() + ", " + ee.toString() + ")"
  
  def eval (env:Env) : Value = {
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


class EAnd (val e1 : Exp, val e2 : Exp) extends Exp {

  override def toString () : String =
    "EAnd(" + e1.toString() + "," + e2.toString() + ")"

  def eval (env:Env) : Value = {
    val v1 = e1.eval(env)
    val v2 = e2.eval(env)
    if (v1.isBoolean() && v2.isBoolean()) {
      return new VBoolean(v1.getBool() && v2.getBool() )
    } else {
      error("cannot AND non-Boolean values")
    }
  }
}


class EId (val id : String) extends Exp {

  override def toString () : String =
    "EId(" + id + ")"

  def eval (env : Env) : Value = env.lookup(id)

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

