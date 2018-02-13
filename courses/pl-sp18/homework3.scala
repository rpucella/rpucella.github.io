/************************************************************

HOMEWORK 3

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
 

import scala.util.parsing.combinator._;


//
//  Values
//


abstract class Value {

   // default behaviors for values
   
   def isInteger () : Boolean = false
   def isBoolean () : Boolean = false
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
     return l.addString(new StringBuilder(), "< ", " , ", " >").toString()

  override def isVector () : Boolean = true
  override def getList () : List[Value] = l
}



//
//  Expressions
//


def runtimeError (msg:String):Nothing = {
  throw new Exception("Runtime error: "+msg)
}

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
          runtimeError("unbound identifier "+id)
      }
}

val nullEnv = new Env(List())


abstract class Exp {

    def eval (env : Env) : Value

    def error (msg : String) : Nothing = { 
       runtimeError(msg + "\n   in expression " + this)
    } 

}
    
    
class ELiteral (val v:Value) extends Exp {
    // value literal

    override def toString () : String = 
        "ELiteral(" + v + ")"

    def eval (env:Env) : Value = 
        v
}


def operPlus (v1:Value, v2:Value):Value = {

        if (v1.isInteger() && v2.isInteger()) {
	   return new VInteger(v1.getInt() + v2.getInt())
	} else if (v1.isVector() && v2.isVector()) {
            if (v1.getList().length == v2.getList().length) {
	       var result : List[Value] = List()
	       for ((entry1,entry2) <- v1.getList().zip(v2.getList())) {
	          result = result :+ operPlus(entry1,entry2)
	       }
	       return new VVector(result)
	    } else {
	       runtimeError("vectors of different length")
	    }
        } else if (v1.isVector() && !(v2.isVector())) {
	    return new VVector(v1.getList().map((v:Value) => operPlus(v,v2)))
        } else if (v2.isVector() && !(v1.isVector())) {
	    return new VVector(v2.getList().map((v:Value) => operPlus(v1,v)))
	} else { 
	   runtimeError("cannot add values of different types\n  "+v1+"\n  "+v2)
	} 
}

def operTimes (v1:Value, v2:Value):Value = {

        if (v1.isInteger() && v2.isInteger()) {
	   return new VInteger(v1.getInt() * v2.getInt())
	} else if (v1.isVector() && v2.isVector()) {
	  if (v1.getList().length == v2.getList().length) {
	       var result : Value = new VInteger(0)
	       for ((entry1,entry2) <- v1.getList().zip(v2.getList())) {
	          result = operPlus(result, operTimes(entry1,entry2))
	       }
	       return result
	   } else {
	     runtimeError("vectors of different length")
	   }
        } else if (v1.isVector() && !(v2.isVector())) {
	    return new VVector(v1.getList().map((v:Value) => operTimes(v,v2)))
        } else if (v2.isVector() && !(v1.isVector())) {
	    return new VVector(v2.getList().map((v:Value) => operTimes(v1,v)))
	} else { 
	   runtimeError("cannot multiply values of different types")
	}
}


class EPlus (val e1 : Exp, val e2 : Exp) extends Exp { 
    // Addition operation

    override def toString () : String =
        "EPlus(" + e1 + "," + e2 + ")"
	
    def eval (env:Env) : Value = {
        val v1 = e1.eval(env)
        val v2 = e2.eval(env)
        return operPlus(v1,v2)
    }
}


class ETimes (val e1 : Exp, val e2 : Exp) extends Exp { 
    // Multiplication operation

    override def toString () : String =
        "ETimes(" + e1 + "," + e2 + ")"
	
    def eval (env:Env) : Value = {
        val v1 = e1.eval(env)
        val v2 = e2.eval(env)
	return operTimes(v1,v2)
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


class EVector (val es : List[Exp]) extends Exp {

  override def toString () : String = 
     return es.addString(new StringBuilder(), "EVector(", " , ", ")").toString()

  def eval (env : Env) : Value = {
     val vs = es.map((e:Exp) => e.eval(env))
     return new VVector(vs)
  }
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

class ELet (val id : String, val e : Exp, val ebody : Exp) extends Exp {

    override def toString () : String =
        "ELet(" + id + "," + e + "," + ebody + ")"

    def eval (env : Env) : Value = {
        val v = e.eval(env)
	return ebody.eval(env.push(id,v))
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
   def TRUE : Parser[Boolean] = "true" ^^ { s => true }
   def FALSE : Parser[Boolean] = "false" ^^ { s => false }
   def IF : Parser[Unit] = "if" ^^ { s => () }
   def LET : Parser[Unit] = "let" ^^ { s => () }
   def ID : Parser[String] = """[a-zA-Z][a-zA-Z0-9_]*""".r ^^ { s => s }

   // grammar

   def atomic_int : Parser[Exp] = INT ^^ { i => new ELiteral(new VInteger(i)) }

   def atomic_bool : Parser[Exp] =
      (TRUE | FALSE) ^^ { b => new ELiteral(new VBoolean(b)) }

   def atomic_id : Parser[Exp] =
      ID ^^ { s => new EId(s) }

   def atomic : Parser[Exp] =
      ( atomic_int | atomic_bool | atomic_id ) ^^ { e => e}

   def expr_plus : Parser[Exp] =
      LP ~ PLUS ~ expr ~ expr ~ RP ^^ { case _ ~ _ ~ e1 ~ e2 ~ _ => new EPlus(e1,e2) }
      
   def expr_times : Parser[Exp] =
      LP ~ TIMES ~ expr ~ expr ~ RP ^^ { case _ ~ _ ~ e1 ~ e2 ~ _ => new ETimes(e1,e2) }

   def expr_if : Parser[Exp] =
      LP ~ IF ~ expr ~ expr ~ expr ~ RP ^^
        { case _ ~ _ ~ e1 ~ e2 ~ e3 ~ _ => new EIf(e1,e2,e3) }

   def expr_let : Parser[Exp] =
      LP ~ LET ~ LP ~ LP ~ ID ~ expr ~ RP ~ RP ~ expr ~ RP ^^
           { case _ ~ _ ~ _ ~ _ ~ s ~ e1 ~ _ ~ _ ~ e2 ~ _ => new ELet(s,e1,e2) }

   def expr : Parser[Exp] =
      ( atomic | expr_plus | expr_times | expr_if | expr_let ) ^^ { e => e }
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

def shell () : Unit = {

    while (true) {
       print("lect03-calc> ")
       val input = scala.io.StdIn.readLine()
       val e = parse(input)
       val v = e.eval(new Env(List()))
       println(v)
    }
}
