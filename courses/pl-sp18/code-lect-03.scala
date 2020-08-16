
/* LECTURE 3
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
      ( atomic | expr_plus | expr_times | expr_if | expr_let ) ^^
           { e => e }
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
