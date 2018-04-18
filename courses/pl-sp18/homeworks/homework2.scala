/************************************************************

HOMEWORK 2

Team members:

Emails:

Remarks, if any:

************************************************************/


/*
 *
 * Please fill in this file with your solutions and submit it
 *
 * The functions and methods below are stubs that you should
 * replace with your own implementation.
 *
 * PLEASE DO NOT CHANGE THE TYPES
 * Doing so risks making it impossible for me to test your code.
 *
 * Always make sure you can run this file as a whole with scala
 * by running
 *    scala <file>
 * before submitting it. It has to load without any errors.
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
   def isVector () : Boolean = false

   def getInt () : Int = {
      throw new Exception("Value not of type INTEGER")
   }

   def getBool () : Boolean = {
      throw new Exception("Value not of type BOOLEAN")
   }

   def getNum () : Int = {
      throw new Exception("Value not of type FRACTION")
   }  

   def getDen () : Int = {
      throw new Exception("Value not of type FRACTION")
   }

   def getList () : List[Value] = {
      throw new Exception("Value not of type VECTOR")
   }
}


class VInteger (val i:Int) extends Value {

  override def toString () : String = i.toString()
  override def isInteger () : Boolean = true
  override def getInt () : Int = i

}


class VBoolean (val b:Boolean) extends Value {

  override def toString () : String = b.toString()
  override def isBoolean () : Boolean = true
  override def getBool () : Boolean = b
}


class VFraction (val num:Int, val den:Int) extends Value {

  override def toString () : String = num.toString() + "/" + den.toString()
  override def isFraction () : Boolean = true
  override def getNum () : Int = num
  override def getDen () : Int = den
}



class VVector (val l:List[Value]) extends Value {

  override def toString () : String = 
     return l.addString(new StringBuilder(), "< ", " , ", " >").toString()


  // Complete this class for Q2

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

/* empty environment - useful for evaluation */

val nullEnv = new Env(List())


def runtimeError (msg:String) : Nothing = {
       throw new Exception ("Runtime error: " + msg)
}



def mkFraction (n:Int, d:Int) : Value = {

   // complete this function for Q1
   
   throw new Exception ("mkFraction not implemented")
}



abstract class Exp {

    def eval (env : Env) : Value
    
    def error (msg : String) : Nothing = 
       runtimeError(msg + "\n   in expression " + this)
}
    
    
class ELiteral (val v:Value) extends Exp {
    // value literal

    override def toString () : String = 
        "ELiteral(" + v + ")"

    def eval (env:Env) : Value =  v
}



class EPlus (val e1 : Exp, val e2 : Exp) extends Exp { 
    // Addition operation

    override def toString () : String =
        "EPlus(" + e1 + "," + e2 + ")"

    // modify this class for Q1 and Q2
    
    def eval (env:Env) : Value = {
        val v1 = e1.eval(env)
        val v2 = e2.eval(env)
	if (v1.isInteger() && v2.isInteger()) { 
	   return new VInteger(v1.getInt() + v2.getInt() )
	} else if (v1.isFraction() && v2.isFraction()) { 
	   return new VFraction(v1.getNum()*v2.getDen() + v2.getNum()*v1.getDen(),
				v1.getDen()*v2.getDen())
	} else {
	   error("cannot add values of different types")
	} 
    }

}


class ETimes (val e1 : Exp, val e2 : Exp) extends Exp { 
    // Multiplication operation

    override def toString () : String =
        "ETimes(" + e1 + "," + e2 + ")"
	
    // modify this class for Q1 and Q2
    
    def eval (env:Env) : Value = {
        val v1 = e1.eval(env)
        val v2 = e2.eval(env)
	if (v1.isInteger() && v2.isInteger()) { 
	   return new VInteger(v1.getInt() * v2.getInt() )
	} else if (v1.isFraction() && v2.isFraction()) { 
	   return new VFraction(v1.getNum() * v2.getNum(),
	   	                v1.getDen() * v2.getDen())
	} else {
	   error("cannot multiply values of different types")
	}
    }
}


class EIf (val ec : Exp, val et : Exp, val ee : Exp) extends Exp {
    // Conditional expression

    override def toString () : String =
        "EIf(" + ec + "," + et + "," + ee +")"
	
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
         "EAnd(" + e1 + "," + e2 + ")"

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



class EDiv (val e1: Exp, val e2: Exp) extends Exp {

    override def toString () : String =
        "EDiv(" + e1 + "," + e2 + ")"

    // complete this class for Q1
    
    def eval (env:Env) : Value = {
       throw new Exception("EDiv not implemented")
    }
}


class EVector (val es : List[Exp]) extends Exp {

  override def toString () : String = 
     return es.addString(new StringBuilder(), "EVector(", " , ", ")").toString()

  // complete this class for Q2
  
    def eval (env:Env) : Value = {
       throw new Exception("EVector not implemented")
    }
}





class EId (val id : String) extends Exp {

    override def toString () : String =
        "EId(" + id + ")"

    def eval (env : Env) : Value = env.lookup(id)

}


class ELet (val id : String, val e : Exp, val body : Exp) extends Exp {

    override def toString () : String =
        "ELet(" + id + "," + e + "," + body + ")"

    def eval (env : Env) : Value = {
        val v = e.eval(env)
	return body.eval(env.push(id,v))
    }
}



class ELetSim (val bindings : List[(String, Exp)], val body : Exp) extends Exp {

    override def toString () : String =
        "ELetSim(" + bindings + "," + body + ")"

    // complete this class for Q3

    def eval (env:Env) : Value = {
       throw new Exception("ELetSim not implemented")
    }
}



class ELetSeq (val bindings : List[(String, Exp)], val body : Exp) extends Exp {

    override def toString () : String =
        "ELetSeq(" + bindings + "," + body + ")"

    // complete this class for Q3

    def eval (env:Env) : Value = {
       throw new Exception("ELetSeq not implemented")
    }
}


def _int (i:Int) : Exp = new ELiteral(new VInteger(i))
def _bool (b:Boolean) : Exp = new ELiteral(new VBoolean(b))
def _frac (n:Int,d:Int) : Exp = new ELiteral(new VFraction(n,d))
def _plus (e1:Exp, e2:Exp) : Exp = new EPlus(e1,e2)
def _times (e1:Exp, e2:Exp) : Exp = new ETimes(e1,e2)
def _div (e1:Exp, e2:Exp) : Exp = new EDiv(e1,e2)
def _and (e1:Exp,e2:Exp) : Exp = new EAnd(e1,e2)
def _vector (es:List[Exp]) : Exp = new EVector(es)

def _id (i:String) : Exp = new EId(i)
def _let (i:String,e1:Exp,e2:Exp) : Exp = new ELet(i,e1,e2)
def _letsim (b:List[(String,Exp)],e:Exp) : Exp = new ELetSim(b,e)
def _letseq (b:List[(String,Exp)],e:Exp) : Exp = new ELetSeq(b,e)
