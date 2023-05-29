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


/* LECTURE 4
 *
 * Expression language with different types of values
 *
 * Identifiers and let-bindings (via environments)
 *
 * S-expression surface syntax 
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

   def getInt () : Int = {
      throw new Exception("Value not of type INTEGER")
   }

   def getBool () : Boolean = {
      throw new Exception("Value not of type BOOLEAN")
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





//
//  Expressions
//



class Env (val content: List[(String, Value)]) { 

      override def toString () : String = {
          var result = ""
	  for (entry <- content) {
	     result = result + "[" + entry._1 + " <- " + entry._2.toString() + "] "
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



class EPlus (val e1 : Exp, val e2 : Exp) extends Exp { 
    // Addition operation

    override def toString () : String =
        "EPlus[" + e1.toString() + ", " + e2.toString() + "]"
	
    def eval (env:Env) : Value = {
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
        "ETimes[" + e1.toString() + ", " + e2.toString() + "]"
	
    def eval (env:Env) : Value = {
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
        "EIf[" + ec.toString() + ", " + et.toString() + ", " + ee.toString() +"]"
	
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
         "EAnd[" + e1.toString() + ", " + e2.toString() + "]"

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
        "EId[" + id + "]"

    def eval (env : Env) : Value = env.lookup(id)

}


class ELet (val id : String, val e : Exp, val ebody : Exp) extends Exp {

    override def toString () : String =
        "ELet[" + id + ", " + e.toString() + ", " + ebody.toString() + "]"

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
   def TRUE : Parser[Unit] = "true" ^^ { s => () }
   def FALSE : Parser[Unit] = "false" ^^ { s => () }
   def IF : Parser[Unit] = "if" ^^ { s => () }
   def LET : Parser[Unit] = "let" ^^ { s => () }
   def ID : Parser[String] = """[a-zA-Z][a-zA-Z0-9_]*""".r ^^ { s => s }

   // grammar

   def atomic_int : Parser[Exp] = INT ^^ { i => new EInteger(i) }

   def atomic_id : Parser[Exp] =
      ID ^^ { s => new EId(s) }

   def atomic_true : Parser[Exp] =
      TRUE  ^^ { _ => new EBoolean(true) }

   def atomic_false : Parser[Exp] =
      FALSE  ^^ { _ => new EBoolean(false) }

   def atomic : Parser[Exp] =
      ( atomic_int | atomic_id | atomic_true | atomic_false) ^^ { e => e}

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
       print("demo/lect04> ")
       val input = scala.io.StdIn.readLine()
       val e = parse(input)
       val v = e.eval(new Env(List()))
       println(v)
    }
}


// uncomment to run the interactive shell
// shell()
