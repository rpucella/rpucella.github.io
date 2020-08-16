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


import scala.io.Source
import scala.util.parsing.combinator._;


//
//  Values
//


abstract class Value {

   // default behaviors for values
   
   def isInteger () : Boolean = false
   def isBoolean () : Boolean = false
   def isVector () : Boolean = false
   def isClosure () : Boolean = false

   def getInt () : Int = {
      throw new Exception("Value not of type INTEGER")
   }

   def getBool () : Boolean = {
      throw new Exception("Value not of type BOOLEAN")
   }
   
   def getList () : List[Value] = {
      throw new Exception("Value not of type VECTOR")
   }

   def apply (vs : List[Value]) : Value = {
      throw new Exception("Value not of type CLOSURE")
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


class VVector (val l : List[Value]) extends Value {

  override def toString () : String = 
     return l.addString(new StringBuilder(), "VVector[", ", ", "]").toString()

  override def isVector () : Boolean = true
  override def getList () : List[Value] = l
}


class VClosure (val params : List[String], val body : Exp, val env : Env) extends Value {

  override def toString () : String = {
    val ps = params.addString(new StringBuilder(), "[", ", ", "]").toString()
    return "VClosure[" + ps + ", " + body.toString() + ", " + env.toString() + ")"
  }
  
  override def apply (args : List[Value]) : Value = {
    throw new Exception("Not implemented")
  }
}


class VRecursiveClosure (val self : String, val params : List[String], val body : Exp, val env : Env) extends Value {

  override def toString () : String = {
    val ps = params.addString(new StringBuilder(), "[", ", ", "]").toString()
    return "VRecursiveClosure[" + self + ", " + ps + ", " + body.toString() + ", " + env.toString() + ")"
  }
  
  override def apply (args : List[Value]) : Value = {
    throw new Exception("Not implemented")
  }
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
	
    def eval (env : Env) : Value = {
        val v1 = e1.eval(env)
        val v2 = e2.eval(env)
        return this.plus(v1, v2)
    }

    def plus (v1 : Value, v2 : Value) : Value = { 
	if (v1.isInteger() && v2.isInteger()) { 
	   return new VInteger(v1.getInt() + v2.getInt() )
	} else if (v1.isVector() & v2.isVector()) {
           if (v1.getList().length != v2.getList().length) {
             error("cannot add vectors of different length")
           }
           val l1 = v1.getList()
           val l2 = v2.getList()
           val vs = l1.zip(l2).map((p : (Value, Value)) => this.plus(p._1, p._2))
           return new VVector(vs)
        } else { 
	   error("cannot add non-integer values")
	} 
    }
}


class ETimes (val e1 : Exp, val e2 : Exp) extends Exp { 
    // Multiplication operation

    override def toString () : String =
        "ETimes[" + e1.toString() + ", " + e2.toString() + "]"
	
    def eval (env : Env) : Value = {
        val v1 = e1.eval(env)
        val v2 = e2.eval(env)
        return this.times(v1, v2)
    }

    def times (v1 : Value, v2 : Value) : Value = { 
	if (v1.isInteger() && v2.isInteger()) { 
	   return new VInteger(v1.getInt() * v2.getInt() )
	} else if (v1.isVector() & v2.isVector()) {
           if (v1.getList().length != v2.getList().length) {
             error("cannot multiply vectors of different length")
           }
           val l1 = v1.getList()
           val l2 = v2.getList()
           val vs = l1.zip(l2).map((p : (Value, Value)) => this.times(p._1, p._2))
           return new VVector(vs)
        } else { 
	   error("cannot multiply non-integer values")
	} 
    }
}


class EEqual (val e1 : Exp, val e2 : Exp) extends Exp { 

    override def toString () : String =
        "EEqual[" + e1.toString() + ", " + e2.toString() + "]"
	
    def eval (env : Env) : Value = {
        val v1 = e1.eval(env)
        val v2 = e2.eval(env)
        return this.equal(v1, v2)
    }

    def equal (v1 : Value, v2 : Value) : Value = {
       if (v1.isInteger() && v2.isInteger()) {
         return new VBoolean(v1.getInt() == v2.getInt())
       } else {
         if (v1.isVector() && v2.isVector()) { 
           if (v1.getList().length != v2.getList().length) {
             return new VBoolean(false)
           }
           // compare all the components of the vectors for equality
           val l1 = v1.getList()
           val l2 = v2.getList()
           for (p <- l1.zip(l2)) {
             if (!this.equal(p._1, p._2).getBool()) {
               // if any two components are unequal, the two vectors are unequal
               return new VBoolean(false)
             }
           }
           // if we get here, all components are equal, so the vectors are equal
           return new VBoolean(true)
        } else { 
	   error("cannot compare non-integer values")
	}
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


class EVector (val es : List[Exp]) extends Exp {

  override def toString () : String = 
     return es.addString(new StringBuilder(), "EVector[", ", ", "]").toString()

  def eval (env : Env) : Value = {
     val vs = es.map((e : Exp) => e.eval(env))
     return new VVector(vs)
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


class EFunction (val params : List[String], body : Exp) extends Exp {

  override def toString () : String = {
    val ps = params.addString(new StringBuilder(), "[", ", ", "]").toString()
    return "EFunction[" + ps + ", " + body.toString() + "]"
  }

  def eval (env : Env) : Value = {
    throw new Exception("Not implemented")
  }
}


class ERecursiveFunction (val self : String, val params : List[String], body : Exp) extends Exp {

  override def toString () : String = {
    val ps = params.addString(new StringBuilder(), "[", ", ", "]").toString()
    return "EFunction[" + self + ", " + ps + ", " + body.toString() + "]"
  }

  def eval (env : Env) : Value = {
    throw new Exception("Not implemented")
  }
}


class EApply (val fn : Exp, val args : List[Exp]) extends Exp {

  override def toString () : String = {
    val as = args.map((arg) => arg.toString).addString(new StringBuilder(), "[", ", ", "]").toString()
    return "EApply[" + fn + ", " + as + "]"
  }
  
  def eval (env : Env) : Value = {
    throw new Exception("Not implemented")
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
   def EQUAL : Parser[Unit] = "=" ^^ { s => () }
   def INT : Parser[Int] = """-?[0-9]+""".r ^^ { s => s.toInt }
   def TRUE : Parser[Unit] = "true" ^^ { s => () }
   def FALSE : Parser[Unit] = "false" ^^ { s => () }
   def IF : Parser[Unit] = "if" ^^ { s => () }
   def LET : Parser[Unit] = "let" ^^ { s => () }
   def ID : Parser[String] = """[a-zA-Z][a-zA-Z0-9_]*""".r ^^ { s => s }
   // new tokens
   def AND : Parser[Unit] = "and" ^^ { s => () }
   def VECTOR : Parser[Unit] = "vector" ^^ { s => () }
   def FUN : Parser[Unit] = "fun" ^^ { s => () }
   
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
      
   def expr_plus : Parser[Exp] =
      LP ~ PLUS ~ expr ~ expr ~ RP ^^ { case _ ~ _ ~ e1 ~ e2 ~ _ => new EPlus(e1,e2) }

   // WRITE ME!
   def expr_plus_multi : Parser[Exp] =
      failure("Not implemented")

   def expr_times : Parser[Exp] =
      LP ~ TIMES ~ expr ~ expr ~ RP ^^ { case _ ~ _ ~ e1 ~ e2 ~ _ => new ETimes(e1,e2) }

   // WRITE ME!
   def expr_times_multi : Parser[Exp] =
      failure("Not implemented")

  def expr_equal : Parser[Exp] =
     LP ~ EQUAL ~ expr ~ expr ~ RP ^^ { case _ ~ _ ~ e1 ~ e2 ~ _ => new EEqual(e1,e2) }

  def expr_if : Parser[Exp] =
      LP ~ IF ~ expr ~ expr ~ expr ~ RP ^^
        { case _ ~ _ ~ e1 ~ e2 ~ e3 ~ _ => new EIf(e1,e2,e3) }

   def expr_let : Parser[Exp] =
      LP ~ LET ~ LP ~ LP ~ ID ~ expr ~ RP ~ RP ~ expr ~ RP ^^
           { case _ ~ _ ~ _ ~ _ ~ s ~ e1 ~ _ ~ _ ~ e2 ~ _ => new ELet(s,e1,e2) }

   // WRITE ME!
   def expr_and : Parser[Exp] =
      failure("Not implemented")

   // WRITE ME!
   def expr_vector : Parser[Exp] =
      failure("Not implemented")

   // WRITE ME!
   def exprs : Parser[List[Exp]] =
      failure("Not implemented")

   // WRITE ME!
   def expr_fun : Parser[Exp] =
      failure("Not implemented")

   // WRITE ME!
   def expr_rec_fun : Parser[Exp] =
      failure("Not implemented")

   // WRITE ME!
   def params : Parser[List[String]] =
      failure("Not implemented")
      
   // WRITE ME!
   def expr_apply : Parser[Exp] =
      failure("Not implemented")

   def expr : Parser[Exp] =
      ( atomic | expr_plus | expr_times | expr_plus_multi | expr_times_multi | expr_equal | expr_if | expr_let | expr_and | expr_vector | expr_fun | expr_rec_fun | expr_apply ) ^^ { e => e }
}


def parse (input:String):Exp = {
   val p = new SExpParser
   p.parseAll(p.expr, input) match {
      case p.Success(result,_) => result
      case failure : p.NoSuccess => throw new Exception("Cannot parse "+input+": "+failure.msg)
   }  
}


def parseFile (file : String) : Exp = {
  val bufferedSource = Source.fromFile(file)
  val content = bufferedSource.getLines.mkString
  bufferedSource.close
  return parse(content)
}
