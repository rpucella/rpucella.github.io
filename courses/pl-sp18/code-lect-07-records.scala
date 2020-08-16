//
// language REF with records
//


//
//  Values
//


abstract class Value {

   // default behaviors for values
   
   def isInteger () : Boolean = false
   def isBoolean () : Boolean = false
   def isVector () : Boolean = false
   def isFunction () : Boolean = false
   def isRefCell () : Boolean = false
   def isNone () : Boolean = false
   def isRecord () : Boolean = false
   
   def error (msg : String) : Nothing = { 
      throw new Exception("Value error: "+ msg + "\n   in value " + this)
   } 

   def getInt () : Int = {
      error("Value not of type INTEGER")
   }

   def getBool () : Boolean = {
      error("Value not of type BOOLEAN")
   }

   def getList () : List[Value] = {
      error("Value not of type VECTOR")
   }

   def apply (args: List[Value]) : Value =  {
      error("Value not of type FUNCTION")
   }

   def getRefContent () : Value = {
      error("Value not of type REFCELL")
   }
   
   def setRefContent (v:Value) : Unit = {
      error("Value not of type REFCELL")
   }

   def getFields () : List[(String,Value)] = {
      error("Value not of type RECORD")
   }

   def lookup (s:String) : Value = { 
      error("Value not of type RECORD")
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

   def checkRefCell () : Unit = {
     if (!isRefCell()) {
        error("Value not of type REFCELL")
     }
   }

   def checkUnit () : Unit = {
     if (!isNone()) {
        error("Value not of type NONE")
     }
   }

   def checkRecord () : Unit = {
     if (!isRecord()) {
        error("Value not of type RECORD")
     }
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


class VPrimOp (val oper : (List[Value]) => Value) extends Value {

  override def toString () : String = "primop(" + oper + ")"
  override def isFunction () : Boolean = true

  override def apply (args: List[Value]) : Value =
     oper(args)
}


class VRecClosure (val self: String, val params: List[String], val body:Exp, val env:Env) extends Value {

  override def toString () : String = params + " | " + self + " => " + body 
  override def isFunction () : Boolean = true

  override def apply (args: List[Value]) : Value = {
     if (params.length != args.length) {
        throw new Exception("Runtime error : wrong number of arguments\n  Function "+this.toString())
     }
     var new_env = env
     for ((p,v) <- params.zip(args)) {
        new_env = new_env.push(p,v)
     }
     
     // push the current closure as the value bound to identifier self
     new_env = new_env.push(self,this)
     return body.eval(new_env)
  }
}


class VRefCell (val init : Value) extends Value {

  var content = init
  
  override def toString () : String = "ref(" + content + ")"
  override def isRefCell () : Boolean = true

  override def getRefContent () : Value = content
  override def setRefContent (v:Value) : Unit = {
    content = v
  }
}


object VNone extends Value {

  override def toString () : String = "None"
  override def isNone () : Boolean = true
}




//
//  Primitive operations
//

object Ops { 

   def runtimeError (msg: String) : Nothing = {
   
       throw new Exception("Runtime error: "+msg)
   }
   
   
   
   def checkArgsLength (vs:List[Value], min: Int, max : Int) : Unit = {
   
      //
      // check whether an argument list has size between min and max
      //
   
      if (vs.length < min) {
         runtimeError("Number of args < "+min)
      }
      if (vs.length > max) {
         runtimeError("Number of args > "+max)
      }
   }
   
   
   
   def operPlus (vs:List[Value]) : Value = {
   
      checkArgsLength(vs,2,2)
   
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
   
      checkArgsLength(vs,2,2)
   
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
   
      checkArgsLength(vs,2,2)
       
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


   def operRefCell (vs:List[Value]) : Value = {
     checkArgsLength(vs,1,1)
     val init = vs(0)
     return new VRefCell(init)
   }

   def operReadRefCell (vs:List[Value]) : Value = {
     checkArgsLength(vs,1,1)
     val r = vs(0)
     r.checkRefCell()
     return r.getRefContent()
   }

   def operWriteRefCell (vs:List[Value]) : Value = {
     checkArgsLength(vs,2,2)
     val r = vs(0)
     val v = vs(1)
     r.checkRefCell()
     r.setRefContent(v)
     return VNone
   }

   def operPrint (vs:List[Value]) : Value = {

     for (v <- vs) {
       print(v)
       print(" ")
     }
     println(" ")
     return VNone
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


class EApply (val f: Exp, val args: List[Exp]) extends Exp {
   override def toString () : String =
      "EApply(" + f + "," + args + ")"
      
   def eval (env : Env) : Value = {
      val vf = f.eval(env)
      val vargs = args.map((e:Exp) => e.eval(env))
      return vf.apply(vargs)
   }
}


class EFunction (val params : List[String], val body : Exp) extends Exp {

   override def toString () : String =
     "EFunction(" + params + "," + body + ")"
     
   def eval (env : Env) : Value =
      new VRecClosure("",params,body,env)

}

class ERecFunction (val self: String, val params: List[String], val body : Exp) extends Exp {

   override def toString () : String =
     "ERecFunction(" + self + "," + params + "," + body + ")"
     
   def eval (env : Env) : Value =
      new VRecClosure(self,params,body,env)
}


class EDo (val es : List[Exp]) extends Exp {

   override def toString () : String = 
      "EDo(" + es + ")"

   def eval (env : Env) : Value = {

      var v : Value = VNone
      for (e <- es) { 
        v = e.eval(env)
      }
      return v
   }
}


class ELet (val bindings : List[(String,Exp)], val ebody : Exp) extends Exp {

    override def toString () : String =
        "ELet(" + bindings + "," + ebody + ")"

    def eval (env : Env) : Value = {
        var new_env = env
        for ((n,e) <- bindings) { 
          val v = e.eval(env)
	  new_env = new_env.push(n,v)
	}
	return ebody.eval(new_env)
    }
}


class EWhile (val cond: Exp, val body: Exp) extends Exp {

   override def toString () : String =
       "EWhile(" + cond + "," + body + ")"

   def eval (env : Env) : Value = {
      var vc = cond.eval(env)
      vc.checkBoolean()
      while (vc.getBool()) {

        val v = body.eval(env)    // result never used
	vc = cond.eval(env)
	vc.checkBoolean()
      }
      return VNone
   }

}


/*
 * RECORDS
 *
 */

class VRecord (fields: List[(String,Value)]) extends Value {

  override def toString () : String = "record(" + fields + ")"
  override def isRecord () : Boolean = true

  override def getFields () : List[(String,Value)] = {
     return fields
  }
  
  override def lookup (s:String) : Value = {
     for ((n,v) <- fields) {
       if (n==s) {
       	  return v
       }
     }
     error("No field "+s+" in record")
  }

}

class ERecord (val fields: List[(String,Exp)]) extends Exp {

   override def toString () : String =
       "ERecord(" + fields + ")"

    def eval (env : Env) : Value = {
       val bvs = fields.map((p) => (p._1,p._2.eval(env)))
       return new VRecord(bvs)
    }
}


class EField (val r:Exp, val f:String) extends Exp {

  override def toString () : String =
     "EField(" + r + "," + f + ")"

  def eval (env : Env) : Value = {
    val vr = r.eval(env)
    return vr.lookup(f)
  }
}


class EWith (val r:Exp, val body:Exp) extends Exp {

   override def toString () : String =
      "EWith(" + r + "," + body + ")"

   def eval (env : Env) : Value = {
     val vr = r.eval(env)
     var newenv = env
     for ((n,v) <- vr.getFields()) {
       newenv = newenv.push(n,v)
     }
     return body.eval(newenv)
   }
}


//
// SURFACE SYNTAX (S-expressions)
//


import scala.util.parsing.combinator._


class SExpParser extends RegexParsers { 

   // tokens
   
   def LP : Parser[Unit] = "(" ^^ { s => () }
   def RP : Parser[Unit] = ")" ^^ { s => () }
   def LB : Parser[Unit] = "[" ^^ { s => () }
   def RB : Parser[Unit] = "]" ^^ { s => () } 
   def PLUS : Parser[Unit] = "+" ^^ { s => () }
   def TIMES : Parser[Unit] = "*" ^^ { s => () }
   def INT : Parser[Int] = """[0-9]+""".r ^^ { s => s.toInt }
   def IF : Parser[Unit] = "if" ^^ { s => () }
   def ID : Parser[String] = """[a-zA-Z_+*\-:.?=<>!|][a-zA-Z0-9_+\-*:.?=<>!|]*""".r ^^ { s => s }
   def RECORD : Parser[Unit] = "record" ^^ { s => () }  
   def FIELD : Parser[Unit] = "field" ^^ { s => () }
   def WITH : Parser[Unit] = "with" ^^ { s => () }

   def FUN : Parser[Unit] = "fun" ^^ { s => () }
   def LET : Parser[Unit] = "let" ^^ { s => () }

   def DO : Parser[Unit] = "do" ^^ { s => () } 
   def WHILE : Parser[Unit] = "while" ^^ { s => () } 

   // grammar

   def atomic_int : Parser[Exp] = INT ^^ { i => new ELiteral(new VInteger(i)) }

   def atomic_id : Parser[Exp] =
      ID ^^ { s => new EId(s) }

   def atomic : Parser[Exp] =
      ( atomic_int | atomic_id ) ^^ { e => e}
      
   def expr_if : Parser[Exp] =
      LP ~ IF ~ expr ~ expr ~ expr ~ RP ^^
        { case _ ~ _ ~ e1 ~ e2 ~ e3 ~ _ => new EIf(e1,e2,e3) }

   def binding : Parser[(String,Exp)] =
      LP ~ ID ~ expr ~ RP ^^ { case _ ~ n ~ e ~ _ => (n,e) }
      
   def expr_let : Parser[Exp] =
      LP ~ LET ~ LP ~ rep(binding) ~ RP ~ expr ~ RP ^^
           { case _ ~ _ ~ _ ~ bindings ~ _ ~ e2 ~ _ => new ELet(bindings,e2) }

   def expr_vec : Parser[Exp] =
      LB ~ rep(expr) ~ RB ^^ { case _ ~ es ~ _ => new EApply(new ELiteral(new VPrimOp(Ops.operVector)),es) }

   def expr_fun : Parser[Exp] =
      LP ~ FUN ~ LP ~ rep(ID) ~ RP ~ expr ~ RP ^^
        { case _ ~ _ ~ _ ~ params ~ _ ~ e ~ _ => new EFunction(params,e) }

   def expr_funr : Parser[Exp] =
      LP ~ FUN ~ ID ~ LP ~ rep(ID) ~ RP ~ expr ~ RP ^^
        { case _ ~ _ ~ self ~ _ ~ params ~ _ ~ e ~ _ => new ERecFunction(self,params,e) }

   def expr_app : Parser[Exp] =
      LP ~ expr ~ rep(expr) ~ RP ^^ { case _ ~ ef ~ eargs ~ _ => new EApply(ef,eargs) }

   def expr_do : Parser[Exp] = 
      LP ~ DO ~ rep(expr) ~ RP ^^ { case _ ~ _ ~ es ~ _ => new EDo(es) }

   def expr_while : Parser[Exp] =
      LP ~ WHILE ~ expr ~ expr ~ RP ^^ { case _ ~ _ ~ cond ~ body ~ _ => new EWhile(cond,body) }

   def expr_record : Parser[Exp] =
      LP ~ RECORD ~ rep(binding) ~ RP ^^ { case _ ~ _ ~ bs ~ _ => new ERecord(bs) }

   def expr_field : Parser[Exp] =
     LP ~ FIELD ~ expr ~ ID ~ RP ^^ { case _ ~ _ ~ e ~ id ~ _ => new EField(e,id) }

   def expr_with : Parser[Exp] =
     LP ~ WITH ~ expr ~ expr ~ RP ^^ { case _ ~ _ ~ e1 ~ e2 ~ _ => new EWith(e1,e2) }

   def expr : Parser[Exp] =
      ( atomic | expr_if | expr_vec | expr_fun | expr_funr | expr_let | expr_do | expr_while | expr_record | expr_field | expr_with | expr_app) ^^
           { e => e }

   def shell_entry : Parser[ShellEntry] =
      (LP ~ "define" ~ ID ~ expr ~ RP  ^^ { case _ ~ _ ~ n ~ e ~ _  => new SEdefine(n,e) }) |
      (expr ^^ { e => new SEexpr(e) }) |
      ("#quit" ^^ { s => new SEquit() })
      
      
}



//
//  Shell 
//

abstract class ShellEntry {

   // abstract class for shell entries
   // (representing the various entries you
   //  can type at the shell)

   def processEntry (env:Env) : Env
}


class SEexpr (e:Exp) extends ShellEntry {

   def processEntry (env:Env) : Env = {
      val v = e.eval(env)
      println(v)
      return env
   }
}

class SEdefine (n:String, e:Exp) extends ShellEntry {

   def processEntry (env:Env) : Env = {
      val v = e.eval(env)
      println(n + " defined")
      return env.push(n,v)
   }

}

class SEquit extends ShellEntry {

   def processEntry (env:Env) : Env = {

      System.exit(0)
      return env
   }
}

object Shell {

   val parser = new SExpParser

   def parse (input:String) : ShellEntry = {
   
      parser.parseAll(parser.shell_entry, input) match {
         case parser.Success(result,_) => result
         case failure : parser.NoSuccess => throw new Exception("Cannot parse "+input+": "+failure.msg)
      }  
   }
   
   
   val nullEnv = new Env(List())
   
   //
   // Standard environment
   //

   val stdEnv = new Env(List(
     ("true",new VBoolean(true)),
     ("false",new VBoolean(false)),
     ("not", new VRecClosure("",List("a"), new EIf(new EId("a"), new ELiteral(new VBoolean(false)), new ELiteral(new VBoolean(true))),nullEnv)),
     ("+", new VPrimOp(Ops.operPlus)),
     ("*", new VPrimOp(Ops.operTimes)),
     ("-", new VRecClosure("",List("a","b"), new EApply(new ELiteral(new VPrimOp(Ops.operPlus)), List(new EId("a"), new EApply(new ELiteral(new VPrimOp(Ops.operTimes)),List(new ELiteral(new VInteger(-1)),new EId("b"))))),nullEnv)),
     ("=", new VPrimOp(Ops.operEqual)),
     ("<", new VPrimOp(Ops.operLess)),
     ("empty?",new VPrimOp(Ops.operEmpty)),
     ("first",new VPrimOp(Ops.operFirst)),
     ("rest",new VPrimOp(Ops.operRest)),
     ("empty",new VVector(List())),
     ("cons",new VPrimOp(Ops.operCons)),
     ("ref",new VPrimOp(Ops.operRefCell)),
     ("read",new VPrimOp(Ops.operReadRefCell)),
     ("write!",new VPrimOp(Ops.operWriteRefCell)),
     ("print!",new VPrimOp(Ops.operPrint))
   ))


   def shell () : Unit = {
   
       var env = stdEnv
   
       while (true) {
          print("REF+records> ")
          try { 
             val input = scala.io.StdIn.readLine()
             val se = parse(input)
	     env = se.processEntry(env)
          } catch {
             case e : Exception => println(e.getMessage)
          } 
       }
   }

   def main (argv:Array[String]) : Unit = {
       shell()
   }

}
