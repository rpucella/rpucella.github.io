
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
       throw new Exception ("Runtime error: "+msg + "\n   in expression " + this)
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



def eval_print (e : Exp) : Unit = {

    val emptyEnv = new Env(List())
    println(e + " -> " + e.eval(emptyEnv))
}



// to help create expressions

def _int (i:Int) : Exp = new ELiteral(new VInteger(i))
def _bool (b:Boolean) : Exp = new ELiteral(new VBoolean(b))
def _frac (i:Int,j:Int) : Exp = new ELiteral(new VFraction(i,j))
def _plus (e1:Exp,e2:Exp) : Exp = new EPlus(e1,e2)
def _times (e1:Exp,e2:Exp) : Exp = new ETimes(e1,e2)
def _and (e1:Exp,e2:Exp) : Exp = new EAnd(e1,e2)
def _if (e1:Exp,e2:Exp,e3:Exp) : Exp = new EIf(e1,e2,e3)
def _let (id:String,e1:Exp,e2:Exp) : Exp = new ELet(id,e1,e2)
def _id (id:String) : Exp = new EId(id)


val e33 = _int(33)
val e66 = _int(66)

eval_print(e33)
eval_print(e66)

val sum = _plus(e33,e66)
val prod = _times(e33,sum)

eval_print(sum)
eval_print(prod)

val ifz = _if(_bool(false),e33,e66)
val ifnz = _if(_bool(true),e33,e66)

eval_print(ifz)
eval_print(ifnz)

val etrue = _bool(true)
val efalse = _bool(false)

val eand1 = _and(etrue,efalse)
val eand2 = _and(etrue,_and(etrue,efalse))
val eand3 = _and(_and(etrue,etrue),_and(etrue,etrue))

eval_print(eand1)
eval_print(eand2)
eval_print(eand3)

val ehalf = _frac(1,2)
val ethird = _frac(1,3)
val efrac1 = _plus(ehalf,ethird)
val efrac2 = _times(ehalf,ethird)
val efrac3 = _plus(efrac1,efrac2)
val efrac4 = _plus(ehalf,ehalf)

eval_print(ehalf)
eval_print(ethird)
eval_print(efrac1)
eval_print(efrac2)
eval_print(efrac3)
eval_print(efrac4)


/*  let (x = 1) x+33   */

val elet1 = _let("x", _int(1),
                 _plus(_id("x"),_int(33)))


/*  let (x = 1+2) x*10   */

val elet2 = _let("x", _plus(_int(1),_int(2)),
                 _times(_id("x"),_int(10)))

/*  let (x = 1+2)
      let (y = x+10)
         y*10          */
	 
val elet3 = _let("x", _plus(_int(1), _int(2)),
                 _let("y", _plus(_id("x"),_int(10)),
		      _times(_id("y"),_int(10))))

/*  let (x = let (y = 10) y*y)
      x+x                        */

val elet4 = _let("x", _let("y", _int(10),
                           _times(_id("y"),_id("y"))),
                 _plus(_id("x"), _id("x")))

eval_print(elet1)
eval_print(elet2)
eval_print(elet3)
eval_print(elet4)

