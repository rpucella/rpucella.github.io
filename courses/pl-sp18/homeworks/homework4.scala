/************************************************************

HOMEWORK 4

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
   def isFunction () : Boolean = false
   def isVector () : Boolean = false
   
   def getInt () : Int = {
      throw new Exception("Value not of type INTEGER")
   }

   def getBool () : Boolean = {
      throw new Exception("Value not of type BOOLEAN")
   }

   def getList () : List[Value] = {
      throw new Exception("Value not of type VECTOR")
   }

   def apply (args: List[Value]) : Value =  {
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


class VVector (val l:List[Value]) extends Value {

  override def toString () : String = 
     return l.addString(new StringBuilder(), "[ ", " ", " ]").toString()

  override def isVector () : Boolean = true
  override def getList () : List[Value] = l
}


class VClosure (val params: List[String], val body:Exp, val env: Env) extends Value {

  override def toString () : String = params + " => " + body

  // complete this class in Q1
}


class VPrimOp (val oper : (List[Value]) => Value) extends Value {

  override def toString () : String = "primop(" + oper + ")"

  // complete this class in Q1
}


class VRecClosure (val self: String, val params: List[String], val body:Exp, val env:Env) extends Value {

  override def toString () : String = params + " | " + self + " => " + body

  // complete this class in Q2
}



def runtimeError (msg: String) : Nothing = {

    throw new Exception("Runtime error: "+msg)

}



def operPlus (vs:List[Value]) : Value = {

   if (vs.length != 2) {
     runtimeError("Wrong number of arguments to plus operation")
   }

   val v1 = vs(0)
   val v2 = vs(1)
   
        if (v1.isInteger() && v2.isInteger()) { 
	   return new VInteger(v1.getInt() + v2.getInt())
	} else if (v1.isVector() && v2.isVector()) {
            if (v1.getList().length == v2.getList().length) {
	       var result : List[Value] = List()
	       for ((entry1,entry2) <- v1.getList().zip(v2.getList())) {
	          result = result :+ operPlus(List(entry1,entry2))
	       }
	       return new VVector(result)
	    } else {
	       runtimeError("vectors of different length")
	    }
        } else if (v1.isVector() && !(v2.isVector())) {
	    return new VVector(v1.getList().map((v:Value) => operPlus(List(v,v2))))
        } else if (v2.isVector() && !(v1.isVector())) {
	    return new VVector(v2.getList().map((v:Value) => operPlus(List(v1,v))))
	} else { 
	   runtimeError("cannot add values of different types\n  "+v1+"\n  "+v2)
	} 
}


def operTimes (vs: List[Value]):Value = {

   if (vs.length != 2) {
     runtimeError("Wrong number of arguments to times operation")
   }

   val v1 = vs(0)
   val v2 = vs(1)
   
        if (v1.isInteger() && v2.isInteger()) { 
	   return new VInteger(v1.getInt() * v2.getInt())
	} else if (v1.isVector() && v2.isVector()) {
	  if (v1.getList().length == v2.getList().length) {
	       var result : Value = new VInteger(0)
	       for ((entry1,entry2) <- v1.getList().zip(v2.getList())) {
	          result = operPlus(List(result, operTimes(List(entry1,entry2))))
	       }
	       return result
	   } else {
	     runtimeError("vectors of different length")
	   }
        } else if (v1.isVector() && !(v2.isVector())) {
	    return new VVector(v1.getList().map((v:Value) => operTimes(List(v,v2))))
        } else if (v2.isVector() && !(v1.isVector())) {
	    return new VVector(v2.getList().map((v:Value) => operTimes(List(v1,v))))
	} else { 
	   runtimeError("cannot multiply values of different types")
	}
}


def operEqual (vs: List[Value]) : Value = {

   if (vs.length != 2) {
     runtimeError("Wrong number of arguments to equal operation")
   }
    
   val v1 = vs(0)
   val v2 = vs(1)

   if (v1.isBoolean() && v2.isBoolean()) {
      return new VBoolean(v1.getBool() == v2.getBool())
   } else if (v1.isInteger() && v2.isInteger()) {
      return new VBoolean(v1.getInt() == v2.getInt())
   } else if (v1.isVector() && v2.isVector()) {
      if (v1.getList().length == v2.getList().length) {
         for ((vv1,vv2) <- v1.getList().zip(v2.getList())) {
	    if (!operEqual(List(vv1,vv2)).getBool()) {
	       return new VBoolean(false)
	    }
	 }
	 return new VBoolean(true)
      } else {
         return new VBoolean(false)
      }
   } else if (v1.isFunction() && v2.isFunction()) {
      return new VBoolean(v1==v2)
   } else {
      return new VBoolean(false)
   }
}


def operVector (vs: List[Value]) : Value = {

   return new VVector(vs)
}




def operMap (vs: List[Value]):Value = {

   // complete this function in Q3
   
   throw new Exception("Not implemented")
}


def operFilter (vs: List[Value]):Value = {

   // complete this function in Q3

   throw new Exception("Not implemented")
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



class EId (val id : String) extends Exp {

    override def toString () : String =
        "EId(" + id + ")"

    def eval (env : Env) : Value = env.lookup(id)
}


class ELetSim (val bindings : List[(String, Exp)], val body : Exp) extends Exp {

    override def toString () : String =
        "ELetSim(" + bindings + "," + body + ")"

    def eval (env : Env) : Value = {
    	var new_env = env
	for (entry <- bindings) {
	   val v = entry._2.eval(env)
	   new_env = new_env.push(entry._1, v)
	}
	return body.eval(new_env)
    }
}



class EApply (val f: Exp, val args: List[Exp]) extends Exp {

   override def toString () : String =
      "EApply(" + f + "," + args + ")"

   // complete this method in Q1

   def eval (env : Env) : Value = {
      throw new Exception("Not implemented")
   }
}



class EFunction (val params : List[String], val body : Exp) extends Exp {

   override def toString () : String =
     "EFunction(" + params + "," + body + ")"

   // complete this method in Q1

   def eval (env : Env) : Value = {
      throw new Exception("Not implemented")
   }
}



class ERecFunction (val self: String, val params: List[String], val body : Exp) extends Exp {

   override def toString () : String =
     "ERecFunction(" + self + "," + params + "," + body + ")"

   // complete this method in Q2
     
   def eval (env : Env) : Value = {
      throw new Exception("Not implemented")
   }
}





/*
 * PARSER FOR S-EXPRESSIONS
 *
 */


import scala.util.parsing.combinator._;

class SExpParser extends RegexParsers { 

   // tokens
   
   def LP : Parser[Unit] = "(" ^^ { s => () }
   def RP : Parser[Unit] = ")" ^^ { s => () }
   def LB : Parser[Unit] = "[" ^^ { s => () }
   def RB : Parser[Unit] = "]" ^^ { s => () } 
   def PLUS : Parser[Unit] = "+" ^^ { s => () }
   def TIMES : Parser[Unit] = "*" ^^ { s => () }
   def INT : Parser[Int] = """[0-9]+""".r ^^ { s => s.toInt }
   def TRUE : Parser[Boolean] = "true" ^^ { s => true }
   def FALSE : Parser[Boolean] = "false" ^^ { s => false }
   def IF : Parser[Unit] = "if" ^^ { s => () }
   def LET : Parser[Unit] = "let" ^^ { s => () }
   def ID : Parser[String] = """[a-zA-Z_+*:.?=<>!|][a-zA-Z0-9_+*:.?=<>!|]*""".r ^^ { s => s }


   // grammar

      def atomic_int : Parser[Exp] = INT ^^ { i => new ELiteral(new VInteger(i)) }

   def atomic_bool : Parser[Exp] =
      (TRUE | FALSE) ^^ { b => new ELiteral(new VBoolean(b)) }

   def atomic_id : Parser[Exp] =
      ID ^^ { s => new EId(s) }

   def atomic : Parser[Exp] =
      ( atomic_int | atomic_bool | atomic_id ) ^^ { e => e}
      
   def expr_if : Parser[Exp] =
      LP ~ IF ~ expr ~ expr ~ expr ~ RP ^^
        { case _ ~ _ ~ e1 ~ e2 ~ e3 ~ _ => new EIf(e1,e2,e3) }

   def binding : Parser[(String, Exp)] =
      LP ~ ID ~ expr ~ RP ^^ { case _ ~ id ~ e ~ _ => (id,e) }

   def expr_let : Parser[Exp] =
      LP ~ LET ~ LP ~ rep(binding) ~ RP ~ expr ~ RP ^^
           { case _ ~ _ ~ _ ~ bindings ~ _ ~ e2 ~ _ => new ELetSim(bindings,e2) }

   def expr_vec : Parser[Exp] =
      LB ~ rep(expr) ~ RB ^^ { case _ ~ es ~ _ => new EApply(new ELiteral(new VPrimOp(operVector)),es) }

   def expr : Parser[Exp] =
      ( atomic | expr_if | expr_let | expr_vec ) ^^
           { e => e }
}



def parse (input:String):Exp = {

   val p = new SExpParser
   p.parseAll(p.expr, input) match {
      case p.Success(result,_) => result
      case failure : p.NoSuccess => throw new Exception("Cannot parse "+input+": "+failure.msg)
   }  
}


val nullEnv = new Env(List())

// a standard environment with a bunch of useful primitives defined

val stdEnv = new Env(List(("+", new VPrimOp(operPlus)),
                          ("*", new VPrimOp(operTimes)),
			  ("=", new VPrimOp(operEqual)),
		       	  ("map", new VPrimOp(operMap)),
		       	  ("filter", new VPrimOp(operFilter))))

def _int (i:Int) : Exp = new ELiteral(new VInteger(i))
val _true : Exp = new ELiteral(new VBoolean(true))
val _false : Exp = new ELiteral(new VBoolean(false))
def _if (c:Exp,t:Exp,e:Exp) : Exp = new EIf(c,t,e)
def _let (bs:List[(String,Exp)],b:Exp) : Exp = new ELetSim(bs,b)
def _fun (ps:List[String],b:Exp) : Exp = new EFunction(ps,b)
def _apply (e:Exp,es:List[Exp]) : Exp = new EApply(e,es)
def _id (s:String) : Exp = new EId(s)

