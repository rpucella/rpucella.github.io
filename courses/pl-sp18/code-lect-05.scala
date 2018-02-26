
/* LECTURE 5
 *
 * Expression language with different types of values
 *
 * Identifiers and let-bindings (via environments)
 *
 * First class functions  (EFunction1, EApply1)
 *
 * Static binding
 *
 * Primitive operations via EOperation
 *
 */



//
//  Values
//


abstract class Value {

   // default behaviors for values
   
   def isInteger () : Boolean = false
   def isBoolean () : Boolean = false
   def isVector () : Boolean = false
   def isFunction () : Boolean = false

    def error (msg : String) : Nothing = { 
       throw new Exception("Value error: "+ msg + "\n   in value " + this)
    } 

   def checkInteger () : Unit = {
     if (!isInteger()) {
        error("Value not of type INTEGER")
     }
   }

   def checkBoolean () : Unit = {
     if (!isBoolean()) {
        error("Value not of type BOOLEAN")
     }
   }

   def checkVector () : Unit = {
     if (!isVector()) {
        error("Value not of type VECTOR")
     }
   }

   def checkFunction () : Unit = {
     if (!isFunction()) {
        error("Value not of type FUNCTION")
     }
   }

   def getInt () : Int = {
      error("Value not of type INTEGER")
   }

   def getBool () : Boolean = {
      error("Value not of type BOOLEAN")
   }

   def getList () : List[Value]= {
      error("Value not of type VECTOR")
   }

   def apply1 (arg: Value) : Value = {
      error("Value not of type FUNCTION")
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


class VClosure1 (val param: String, val body:Exp, val env : Env) extends Value {

  override def toString () : String = param + " => " + body
  override def isFunction () : Boolean = true

  override def apply1 (arg: Value) : Value = {
     return body.eval(env.push(param,arg))
  }
}



//
// Environments
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





//
//  Expressions
//

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


class EOperation (val oper : (List[Value]) => Value, val es: List[Exp]) extends Exp { 

    override def toString () : String =
        "EOper(" + oper + ", " + es + ")"

    def eval (env:Env) : Value = {
        val vs = es.map((e) => e.eval(env))
	return oper(vs)
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



class EId (val id : String) extends Exp {

    override def toString () : String =
        "EId(" + id + ")"

    def eval (env : Env) : Value = env.lookup(id)

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


//
// Primitive operations
//


def runtimeError (msg:String) : Nothing = {

   throw new Exception("Runtime error: "+msg)
}

def checkArgsLength (vs:List[Value], min: Int, max : Int) : Unit = {

   if (vs.length < min) {
      runtimeError("Number of args < "+min)
   }
   if (vs.length > max) {
      runtimeError("Number of args > "+max)
   }
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


def operLess (vs: List[Value]) : Value = {

    checkArgsLength(vs,2,2)
    
    val v1 = vs(0)
    val v2 = vs(1)
    v1.checkInteger()
    v2.checkInteger()

    return new VBoolean(v1.getBool() < v2.getBool())
}


def operVector (vs: List[Value]) : Value = {

   return new VVector(vs)
}


def operEmpty (vs : List[Value]) : Value = {

  checkArgsLength(vs,1,1)
  val v = vs(0)
  v.checkVector()
  return new VBoolean(v.getList().length == 0)
}


def operFirst (vs : List[Value]) : Value = {
  checkArgsLength(vs,1,1)
  val v = vs(0)
  v.checkVector()
  val l = v.getList()
  if (l.length == 0) {
    runtimeError("Taking first of an empty vector")
  }
  return l(0)
}


def operRest (vs : List[Value]) : Value = {
  checkArgsLength(vs,1,1)
  val v = vs(0)
  v.checkVector()
  val l = v.getList()
  if (l.length == 0) {
    runtimeError("Taking rest of an empty vector")
  }
  return new VVector(l.tail)
}


def operCons (vs : List[Value]) : Value = {
  checkArgsLength(vs,2,2)
  val item = vs(0)
  val vec = vs(1)
  vec.checkVector()
  return new VVector(item::vec.getList())
}



//
// Standard environment
//

val nullEnv = new Env(List())

val _vtrue = new VBoolean(true)
val _vfalse = new VBoolean(false)
def _vclos (param:String, body:Exp, env:Env) : Value = new VClosure1(param,body,env)
def _vvec (vs:List[Value]) : Value = new VVector(vs)
def _oper (oper:(List[Value])=>Value,es:List[Exp]) : Exp = new EOperation(oper,es)
def _fun (param:String, body:Exp) : Exp = new EFunction1(param,body)
def _id (s:String) : Exp = new EId(s)
def _if (c:Exp,t:Exp,e:Exp) : Exp = new EIf(c,t,e)
val _true = new ELiteral(_vtrue)
val _false = new ELiteral(_vfalse)
def _int (i:Int) : Exp = new ELiteral(new VInteger(i))

val stdEnv = new Env(List(
  ("true",_vtrue),
  ("false",_vfalse),
  ("not",_vclos("a",_if(_id("a"),_false,_true),nullEnv)),
  ("+",_vclos("a",_fun("b",_oper(operPlus,List(_id("a"),_id("b")))),nullEnv)),
  ("*",_vclos("a",_fun("b",_oper(operTimes,List(_id("a"),_id("b")))),nullEnv)),
  ("=",_vclos("a",_fun("b",_oper(operEqual,List(_id("a"),_id("b")))),nullEnv)),
  ("<",_vclos("a",_fun("b",_oper(operLess,List(_id("a"),_id("b")))),nullEnv)),
  // operations on vectors
  ("empty?",_vclos("a",_oper(operEmpty,List(_id("a"))),nullEnv)),
  ("first",_vclos("a",_oper(operFirst,List(_id("a"))),nullEnv)),
  ("rest",_vclos("a",_oper(operRest,List(_id("a"))),nullEnv)),
  ("empty",_vvec(List())),
  ("cons",_vclos("a",_fun("b",_oper(operCons,List(_id("a"),_id("b")))),nullEnv))
))



import scala.util.parsing.combinator._;


/*
 * PARSER FOR S-EXPRESSIONS
 *
 */


class SExpParser extends RegexParsers { 

   // tokens
   
   def LP : Parser[Unit] = "(" ^^ { s => () }
   def RP : Parser[Unit] = ")" ^^ { s => () }
   def LB : Parser[Unit] = "[" ^^ { s => () }
   def RB : Parser[Unit] = "]" ^^ { s => () }
   def INT : Parser[Int] = """-?[0-9]+""".r ^^ { s => s.toInt }
   def IF : Parser[Unit] = "if" ^^ { s => () }
   def ID : Parser[String] = """[a-zA-Z_+*-/:.?=<>!|][a-zA-Z0-9_+*-/:.?=<>!|]*""".r ^^ { s => s }
   def FUN : Parser[Unit] = "fun" ^^ { s => () } 
   def AND : Parser[Unit] = "and" ^^ { s => () } 
   def OR : Parser[Unit] = "or" ^^ { s => () }


   // grammar

   def atomic_int : Parser[Exp] = INT ^^ { i => new ELiteral(new VInteger(i)) }

   def atomic_id : Parser[Exp] =
      ID ^^ { s => new EId(s) }

   def atomic : Parser[Exp] =
      ( atomic_int | atomic_id ) ^^ { e => e}

   def expr_if : Parser[Exp] =
      LP ~ IF ~ expr ~ expr ~ expr ~ RP ^^
        { case _ ~ _ ~ e1 ~ e2 ~ e3 ~ _ => new EIf(e1,e2,e3) }

   def expr_fun1 : Parser[Exp] =
      LP ~ FUN ~ LP ~ ID ~ RP ~ expr ~ RP ^^ { case _ ~ _ ~ _ ~ param ~ _ ~ body ~ _ => new EFunction1(param,body) }

   def mkCurriedFun(params:List[String], body:Exp) : Exp = {

      var result = body
      for (p <- params.reverse) {
         result = new EFunction1(p,result)
      }
      return result
   }

   def mkCurriedApp(ef:Exp,es:List[Exp]) : Exp = {

      var result = ef
      for (e <- es) {
         result = new EApply1(result,e)
      }
      return result
   }


   def expr_and : Parser[Exp] =
      LP ~ AND ~ expr ~ expr ~ RP ^^ {case _ ~ _ ~ e1 ~ e2 ~ _ => new EIf(e1,e2,new ELiteral(new VBoolean(false))) }

   def expr_or : Parser[Exp] =
      LP ~ OR ~ expr ~ expr ~ RP ^^ {case _ ~ _ ~ e1 ~ e2 ~ _ => new EIf(e1,new ELiteral(new VBoolean(true)),e2) }

   def expr_fun : Parser[Exp] =
      LP ~ FUN ~ LP ~ rep1(ID) ~ RP ~ expr ~ RP ^^ { case _ ~ _ ~ _ ~ params ~ _ ~ body ~ _ => mkCurriedFun(params,body) }

   def expr_app1 : Parser[Exp] =
      LP ~ expr ~ expr ~ RP ^^ { case _ ~ e1 ~ e2 ~ _ => new EApply1(e1,e2) }

   def expr_app : Parser[Exp] = 
      LP ~ expr ~ rep1(expr) ~ RP ^^ { case _ ~ ef ~ es ~ _ => mkCurriedApp(ef,es) }

   def expr_vec : Parser[Exp] =
      LB ~ rep(expr) ~ RB ^^ { case _ ~ es ~ _ => new EOperation(operVector,es) }

   def binding : Parser[(String,Exp)] =
      LP ~ ID ~ expr ~ RP ^^ { case _ ~ id ~ e ~ _ => (id,e) }

   def expr : Parser[Exp] =
      ( atomic | expr_vec | expr_if | expr_fun | expr_and | expr_or | expr_app ) ^^
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

    val env = stdEnv

    while (true) {
       print("lect05-func> ")
       try { 
          val input = scala.io.StdIn.readLine()
          val v = parse(input).eval(env)
          println(v)
       } catch {
       	  case e : Exception => println(e.getMessage)
       } 
    }
}

println("Loaded.")

shell()
