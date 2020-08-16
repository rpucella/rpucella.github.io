
/* LECTURE 4
 *
 * Expression language with different types of values
 *
 * Identifiers and let-bindings (via environments)
 *
 * First class functions  (EFunction, EApply)
 *
 * Static binding
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

   def apply1 (arg: Value) : Value = {
      throw new Exception("Value not of type FUNCTION")
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


class VClosure1 (val param: String, val body:Exp, val env : Env) extends Value {

  override def toString () : String = "<" + param + " -> " + body + ">"
  override def isFunction () : Boolean = true

  override def apply1 (arg: Value) : Value = {
     return body.eval(env.push(param,arg))
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


      // push a single binding (id,v) on top of the environment
      
      def push (id : String, v : Value) : Env =
          new Env((id,v)::content)


      // lookup value for an identifier in the environment
      
      def lookup (id : String) : Value = {
      	  for (entry <- content) {
	      if (entry._1 == id) {
	      	 return entry._2
	      }
	  }
	  throw new Exception("Environment error: unbound identifier "+id)
      }
}


abstract class Exp {

    def eval (env : Env) : Value

    def error (msg : String) : Nothing = { 
       throw new Exception("Eval error: "+ msg + "\n   in expression " + this)
    } 

}

    
class ELiteral (val v:Value) extends Exp {
    // value literal

    override def toString () : String = 
        "ELiteral(" + v + ")"

    def eval (env:Env) : Value = 
        v
}



class EPlus (val e1 : Exp, val e2 : Exp) extends Exp { 
    // Addition operation

    override def toString () : String =
        "EPlus(" + e1 + "," + e2 + ")"
	
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




class EId (val id : String) extends Exp {

    override def toString () : String =
        "EId(" + id + ")"

    def eval (env : Env) : Value = env.lookup(id)

}


class ELet (val id : String, val e : Exp, val ebody : Exp) extends Exp {

    override def toString () : String =
        "ELet(" + id + "," + e + "," + ebody + ")"

    def eval (env : Env) : Value = {
        val v = e.eval(env)
	return ebody.eval(env.push(id,v))
    }
}



class EApply1 (val f : Exp, val arg : Exp) extends Exp {

   override def toString () : String =
      "EApply1(" + f + "," + arg + ")"
      
   def eval (env : Env) : Value = {
      val vf = f.eval(env)
      val varg = arg.eval(env)
      return vf.apply1(varg)
   }
}


class EFunction1 (val param : String, val body : Exp) extends Exp {

   override def toString () : String =
     "EFunction1(" + param + "," + body + ")"
     
   def eval (env : Env) : Value =
      new VClosure1(param,body,env)

}


   val nullEnv = new Env(List())

   def _int (i:Int) : Exp = new ELiteral(new VInteger(i))
   def _bool (b:Boolean) : Exp = new ELiteral(new VBoolean(b))
   def _frac (i:Int,j:Int) : Exp = new ELiteral(new VFraction(i,j))
   def _plus (e1:Exp,e2:Exp) : Exp = new EPlus(e1,e2)
   def _times (e1:Exp,e2:Exp) : Exp = new ETimes(e1,e2)
   def _and (e1:Exp,e2:Exp) : Exp = new EAnd(e1,e2)
   def _if (e1:Exp,e2:Exp,e3:Exp) : Exp = new EIf(e1,e2,e3)
   def _let (id:String,e1:Exp,e2:Exp) : Exp = new ELet(id,e1,e2)
   def _id (id:String) : Exp = new EId(id)

   def _apply1 (f:Exp,arg:Exp) : Exp = new EApply1(f,arg)
   def _function1 (param:String, body:Exp) : Exp = new EFunction1(param,body)



// put some tests here

val ef = _function1("a",_plus(_times(_id("a"),_id("a")),_id("a")))
val e = _apply1(ef,_int(3))
println(e)
println(e.eval(nullEnv))


val from_slides = _let("x",_int(10),
                       _let("f",_function1("y",_plus(_id("x"),_id("y"))),
		            _let("x",_int(9000),
   			         _apply1(_id("f"),_int(100)))))
println(from_slides)
println(from_slides.eval(nullEnv))
