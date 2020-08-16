//
// language FUNC with (simple) static types
//
// tail-optimized evaluation (via exp.evalTail)
//
// 


//
//  Values
//


abstract class Value {

   def getInt () : Int = 0
   def getBool () : Boolean = false
   def getList () : List[Value] = List()

   // we still need to distinguish primitive operations from functions :/
   def isPrimOp () : Boolean = false
   def applyOper (args:List[Value]) : Value = new VInteger(0)

   def getParams () : List[String] = List()
   def getSelf () : String = ""
   def getBody () : Exp = new EInteger(0)
   def getEnv () : Env[Value] = new Env(List())
}


class VInteger (val i:Int) extends Value {

  override def toString () : String = i.toString()
  override def getInt () : Int = i
}


class VBoolean (val b:Boolean) extends Value {

  override def toString () : String = b.toString()
  override def getBool () : Boolean = b
}


class VVector (val l:List[Value]) extends Value {

  override def toString () : String = 
     return l.addString(new StringBuilder(), "[ ", " ", " ]").toString()

  override def getList () : List[Value] = l
}


class VPrimOp (val oper : (List[Value]) => Value) extends Value {

  override def toString () : String = "primop(" + oper + ")"

  override def isPrimOp () : Boolean = true
  override def applyOper (args:List[Value]) : Value = oper(args)
}


class VRecClosure (val self: String, val params: List[String], val body:Exp, val env:Env[Value]) extends Value {

  override def toString () : String = params + " | " + self + " => " + body 

  override def getParams () : List[String] = params
  override def getSelf () : String = self
  override def getBody () : Exp = body
  override def getEnv () : Env[Value] = env
}





//
//  Primitive operations
//

object Ops { 

   def runtimeError (msg: String) : Nothing = {
       throw new Exception("Runtime error: "+msg)
   }

   def operPlus (vs:List[Value]) : Value = {
   
      val v1 = vs(0)
      val v2 = vs(1)
      
      return new VInteger(v1.getInt() + v2.getInt())
   }
   
   
   def operTimes (vs: List[Value]):Value = {
   
      val v1 = vs(0)
      val v2 = vs(1)
      
      return new VInteger(v1.getInt() * v2.getInt())
   }
   
   
   def operEqual (vs: List[Value]) : Value = {
       
      val v1 = vs(0)
      val v2 = vs(1)
   
      return new VBoolean(v1.getInt() == v2.getInt())
   }
   
   
   def operLess (vs: List[Value]) : Value = {
   
       val v1 = vs(0)
       val v2 = vs(1)
   
       return new VBoolean(v1.getBool() < v2.getBool())
   }
   
   
   def operEmpty (vs : List[Value]) : Value = {
   
     val v = vs(0)
     return new VBoolean(v.getList().length == 0)
   }
   
   
   def operFirst (vs : List[Value]) : Value = {
     val v = vs(0)
     val l = v.getList()
     if (l.length == 0) {
       runtimeError("Taking first of an empty vector")
     }
     return l(0)
   }
   
   
   def operRest (vs : List[Value]) : Value = {
     val v = vs(0)
     val l = v.getList()
     if (l.length == 0) {
       runtimeError("Taking rest of an empty vector")
     }
     return new VVector(l.tail)
   }
   
   
   def operCons (vs : List[Value]) : Value = {
     val item = vs(0)
     val vec = vs(1)
     return new VVector(item::vec.getList())
   }

}




//
//  Types
//


abstract class Type {

   def isSame (t:Type) : Boolean
   
   def isInteger () : Boolean = return false
   def isBoolean () : Boolean = return false
   def isIntVector () : Boolean = return false
   def isFunction () : Boolean = return false

   def funParams () : List[Type] = {
      throw new Exception("Type error: type is not a function\n   "+this)
   }

   def funResult () : Type = {
      throw new Exception("Type error: type is not a function\n   "+this)
   }
      
}


object TInteger extends Type {

   override def toString () : String = "int"
   
   def isSame (t:Type):Boolean = return t.isInteger()
   override def isInteger () : Boolean = true
}

object TBoolean extends Type {

   override def toString () : String = "bool"
   
   def isSame (t:Type):Boolean = return t.isBoolean()
   override def isBoolean () : Boolean = true
}

object TIntVector extends Type {

   override def toString () : String = "ivector"

   def isSame (t:Type):Boolean = return t.isIntVector()
   override def isIntVector () : Boolean = true
}

class TFunction (val params:List[Type], val result:Type) extends Type {

   override def toString () : String =
     "(fun "+params.addString(new StringBuilder(),"("," ",")").toString() + " " + result + ")"
   
   def isSame (t:Type):Boolean = {
   
     if (!t.isFunction()) {
        return false
     }

     if (t.funParams().length != params.length) {
        return false
     }
     for ((t1,t2) <- t.funParams().zip(params)) {
        if (!t1.isSame(t2)) {
	   return false
	}
     }
     return t.funResult().isSame(result)
   }
   
   override def isFunction () : Boolean = return true
   override def funParams () : List[Type] = return params
   override def funResult () : Type = return result
}


//
//  Expressions
//


class Env[A] (val content: List[(String, A)]) { 

      override def toString () : String = {
          var result = ""
	  for (entry <- content) {
	     result = result + "(" + entry._1 + " <- " + entry._2 + ") "
	  }
	  return result
      }

      
      def push (id : String, v : A) : Env[A] =
          // push a single binding (id,v) on top of the environment

          new Env[A]((id,v)::content)


      def lookup (id : String) : A = {
            // lookup value for an identifier in the environment
      
      	  for (entry <- content) {
	      if (entry._1 == id) {
	      	 return entry._2
	      }
	  }
	  throw new Exception("Environment error: unbound identifier "+id)
      }
}



abstract class Exp {

    def error (msg : String) : Nothing = { 
       throw new Exception("Eval error: "+ msg + "\n   in expression " + this)
    }

    def terror (msg : String) : Nothing = { 
       throw new Exception("Type error: "+ msg + "\n   in expression " + this)
    }

    def typeOf (symt:Env[Type]) : Type
    
    def typeCheck (t:Type, symt:Env[Type]) : Boolean = {
    	val t2 = this.typeOf(symt)
	return t.isSame(t2)
    }

    def eval (env : Env[Value]) : Value

    def evalTail (env : Env[Value]) : Value = {

      var currExp = this
      var currEnv = env

      while (true) {
         currExp match {
	 
	    case EIf(ec,et,ee) => {
              val ev = ec.evalTail(currEnv)
              if (!ev.getBool()) { 
                currExp = ee
              } else {
                currExp = et
              }
	     }

	     case EVector(es) => { 
               val vs = es.map((e:Exp) => e.evalTail(currEnv))
               return new VVector(vs)
	     }

	     case EApply(f,args) => {
               val vf = f.evalTail(currEnv)
               val vargs = args.map((e:Exp) => e.evalTail(currEnv))
               if (vf.isPrimOp()) {
                 return vf.applyOper(vargs)
               } else {
                 // defined function
                 // push the vf closure as the value bound to identifier self
                 var new_env = vf.getEnv().push(vf.getSelf(),vf)
                 for ((p,v) <- vf.getParams().zip(vargs)) {
                    new_env = new_env.push(p,v)
                 }
                 currEnv = new_env
                 currExp = vf.getBody()
               }
	     }
	     
	     case ELet(bindings,body) => {
                var new_env = currEnv
                for ((n,e) <- bindings) { 
                  val v = e.evalTail(currEnv)
                  new_env = new_env.push(n,v)
                }
		currEnv = new_env
		currExp = body
             }
	     // every other expression type evaluates normally
	     case _ => return currExp.eval(currEnv) 
           }
        }
	return new VInteger(0) // needed for typechecking
   }

}


case class EInteger (val i:Integer) extends Exp {
    // integer literal

    override def toString () : String = 
        "EInteger(" + i + ")"

    def typeOf (symt:Env[Type]) : Type = 
        TInteger

    def eval (env:Env[Value]) : Value = 
        new VInteger(i)
}


case class EBoolean (val b:Boolean) extends Exp {
    // boolean literal

    override def toString () : String = 
        "EBoolean(" + b + ")"

    def typeOf (symt:Env[Type]) : Type = 
        TBoolean

    def eval (env:Env[Value]) : Value = 
        new VBoolean(b)
}


case class EVector (val es: List[Exp]) extends Exp {
    // Vectors

   override def toString () : String =
      "EVector" + es.addString(new StringBuilder(),"(", " ", ")").toString()
      
    def typeOf (symt:Env[Type]) : Type = {
       for (e <- es) {
         if (!e.typeCheck(TInteger,symt)) {
	   terror("Vector component not an integer")
	 }
       }
       return TIntVector
    }

      
   def eval (env : Env[Value]) : Value = {
      val vs = es.map((e:Exp) => e.eval(env))
      return new VVector(vs)
   }
}


case class EIf (val ec : Exp, val et : Exp, val ee : Exp) extends Exp {
    // Conditional expression

    override def toString () : String =
        "EIf(" + ec + "," + et + "," + ee +")"
	
    def typeOf (symt:Env[Type]) : Type = {
      if (ec.typeCheck(TBoolean,symt)) {
        val t = et.typeOf(symt)
	if (ee.typeCheck(t,symt)) {
	  return t
	} else {
	  terror("Branches of conditional have different types")
	}
      } else {
        terror("Condition should be Boolean")
      }
    }
      
    def eval (env:Env[Value]) : Value = { 
        val ev = ec.eval(env)
	if (!ev.getBool()) { 
  	  return ee.eval(env)
	} else {
	  return et.eval(env)
	}
    }
}


case class EId (val id : String) extends Exp {

    override def toString () : String =
        "EId(" + id + ")"

    def typeOf (symt:Env[Type]) : Type =  symt.lookup(id)

    def eval (env : Env[Value]) : Value = env.lookup(id)
}




case class EApply (val f: Exp, val args: List[Exp]) extends Exp {
   override def toString () : String =
      "EApply(" + f + "," + args + ")"
      
    def typeOf (symt:Env[Type]) : Type = {
      val t = f.typeOf(symt)
      if (t.isFunction()) {
        val params = t.funParams()
        if (params.length != args.length) {
	   terror("Wrong number of arguments")
	} else {
	   // check the argument types
	   for ((pt,a) <- params.zip(args)) {
	     if (!a.typeCheck(pt,symt)) {
	        terror("Argument "+a+" not of expected type")
	     }
	   }
	   return t.funResult()
	}
      } else {
        terror("Applied expression not of function type")
      }
   }

   def eval (env : Env[Value]) : Value = {
      val vf = f.evalTail(env)
      val vargs = args.map((e:Exp) => e.eval(env))
      if (vf.isPrimOp()) {
         return vf.applyOper(vargs)
      } else {
         // defined function
         // push the vf closure as the value bound to identifier self
         var new_env = vf.getEnv().push(vf.getSelf(),vf)
         for ((p,v) <- vf.getParams().zip(vargs)) {
            new_env = new_env.push(p,v)
         }
	 return vf.getBody.eval(new_env)
      }
    }
}


case class ERecFunction (val self: String, val params: List[String], val typ: Type, val body : Exp) extends Exp {

   override def toString () : String =
     "ERecFunction(" + self + "," + params + "," + body + ")"
     
   def typeOf (symt:Env[Type]) : Type = {
      if (!typ.isFunction()) {
        terror("Function not defined with function type")
      }
      var tparams = typ.funParams()
      var tresult = typ.funResult()
      if (params.length != tparams.length) {
        terror("Wrong number of types supplied")
      }
      var new_symt = symt
      for ((p,pt) <- params.zip(tparams)) {
        new_symt = new_symt.push(p,pt)
      }
      // assume self has the declared function type
      new_symt = new_symt.push(self,typ)
      if (body.typeCheck(tresult,new_symt)) {
        return typ
      } else {
        terror("Return type of function not same as declared")
      }
    }      

   def eval (env : Env[Value]) : Value =
      new VRecClosure(self,params,body,env)
}


case class ELet (val bindings : List[(String,Exp)], val ebody : Exp) extends Exp {

    override def toString () : String =
        "ELet(" + bindings + "," + ebody + ")"

    def typeOf (symt:Env[Type]) : Type = {
       var new_symt = symt
       for ((n,e) <- bindings) {
         val t = e.typeOf(symt)
	 new_symt = new_symt.push(n,t)
       }
       return ebody.typeOf(new_symt)
    }

    def eval (env : Env[Value]) : Value = {
        var new_env = env
        for ((n,e) <- bindings) { 
          val v = e.eval(env)
	  new_env = new_env.push(n,v)
	}
	return ebody.eval(new_env)
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
   def INT : Parser[Int] = """-?[0-9]+""".r ^^ { s => s.toInt }
   def IF : Parser[Unit] = "if" ^^ { s => () }
   def ID : Parser[String] = """[a-zA-Z_+*\-:.?=<>!|][a-zA-Z0-9_+\-*:.?=<>!|]*""".r ^^ { s => s }

   def FUN : Parser[Unit] = "fun" ^^ { s => () }
   def LET : Parser[Unit] = "let" ^^ { s => () }

   def TINT : Parser[Unit] = "int" ^^ { s => () } 
   def TBOOL : Parser[Unit] = "bool" ^^ { s => () }
   def TINTV : Parser[Unit] = "ivector" ^^ { s => () }
   def TFUN : Parser[Unit] = "tfun" ^^ { s => () }
   
   // grammar

   def atomic_int : Parser[Exp] = INT ^^ { i => new EInteger(i) }

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
      LB ~ rep(expr) ~ RB ^^ { case _ ~ es ~ _ => new EVector(es) }

   def expr_fun : Parser[Exp] =
      LP ~ FUN ~ LP ~ rep(ID) ~ RP ~ typ ~ expr ~ RP ^^
        { case _ ~ _ ~ _ ~ params ~ _ ~ typ ~ e ~ _ => new ERecFunction("",params,typ,e) }

   def expr_funr : Parser[Exp] =
      LP ~ FUN ~ ID ~ LP ~ rep(ID) ~ RP ~ typ ~ expr ~ RP ^^
        { case _ ~ _ ~ self ~ _ ~ params ~ _ ~ typ ~ e ~ _ => new ERecFunction(self,params,typ,e) }

   def expr_app : Parser[Exp] =
      LP ~ expr ~ rep(expr) ~ RP ^^ { case _ ~ ef ~ eargs ~ _ => new EApply(ef,eargs) }


   def expr : Parser[Exp] =
      ( atomic | expr_if | expr_vec | expr_fun | expr_funr | expr_let | expr_app) ^^
           { e => e }

   def typ_int : Parser[Type] =
      TINT ^^ { _ => TInteger }
      
   def typ_bool : Parser[Type] =
      TBOOL ^^ { _ => TBoolean }

   def typ_intvec : Parser[Type] =
      TINTV ^^ { _ => TIntVector }

   def typ_fun : Parser[Type] =
      LP ~ TFUN ~ LP ~ rep(typ) ~ RP ~ typ ~ RP ^^
         { case _ ~ _ ~ _ ~ tparams ~ _ ~ tresult ~ _ => new TFunction(tparams,tresult) }

   def typ : Parser[Type] =
      ( typ_int | typ_bool | typ_intvec | typ_fun ) ^^ { e => e }

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

   def processEntry (env:Env[Value],symt:Env[Type]) : (Env[Value],Env[Type])
}


class SEexpr (e:Exp) extends ShellEntry {

   def processEntry (env:Env[Value],symt:Env[Type]) : (Env[Value],Env[Type]) = {
      val t = e.typeOf(symt)
      // uses tail-optimized form of evaluation!
      val v = e.evalTail(env)
      println(v+" : "+t)
      return (env,symt)
   }
}

class SEdefine (n:String, e:Exp) extends ShellEntry {

   def processEntry (env:Env[Value],symt:Env[Type]) : (Env[Value],Env[Type]) = {
      val t = e.typeOf(symt)
      val v = e.eval(env)
      println(n + " defined with type " + t)
      return (env.push(n,v),symt.push(n,t))
   }

}

class SEquit extends ShellEntry {

   def processEntry (env:Env[Value],symt:Env[Type]) : (Env[Value],Env[Type]) = {

      System.exit(0)
      return (env,symt)
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


   def time[R](block: => R):R = {
     val t0 = System.currentTimeMillis()
     val result = block
     val t1 = System.currentTimeMillis()
     println("Elapsed time: " + (t1-t0) + "ms")
     result
   }
   
   val nullEnv = new Env[Value](List())
   
   //
   // Standard environment
   //

   val stdEnv = new Env[Value](List(
     ("true",new VBoolean(true)),
     ("false",new VBoolean(false)),
     ("not", new VRecClosure("",List("a"), new EIf(new EId("a"), new EBoolean(false), new EBoolean(true)),nullEnv)),
     ("+", new VPrimOp(Ops.operPlus)),
     ("*", new VPrimOp(Ops.operTimes)),
     ("=", new VPrimOp(Ops.operEqual)),
     ("<", new VPrimOp(Ops.operLess)),
     ("empty?",new VPrimOp(Ops.operEmpty)),
     ("first",new VPrimOp(Ops.operFirst)),
     ("rest",new VPrimOp(Ops.operRest)),
     ("empty",new VVector(List())),
     ("cons",new VPrimOp(Ops.operCons)),
   ))

   
   val stdSymt = new Env[Type](List(
     ("true",TBoolean),
     ("false",TBoolean),
     ("not", new TFunction(List(TBoolean),TBoolean)),
     ("+", new TFunction(List(TInteger,TInteger),TInteger)),
     ("*", new TFunction(List(TInteger,TInteger),TInteger)),
     ("=", new TFunction(List(TInteger,TInteger),TBoolean)),
     ("<", new TFunction(List(TInteger,TInteger),TBoolean)),
     ("empty?",new TFunction(List(TIntVector),TBoolean)),
     ("first",new TFunction(List(TIntVector),TInteger)),
     ("rest",new TFunction(List(TIntVector),TIntVector)),
     ("empty", TIntVector),
     ("cons",new TFunction(List(TInteger,TIntVector),TIntVector))
   ))
   
   def shell () : Unit = {
   
       var env = stdEnv
       var symt = stdSymt

       println("With tail-optimized evaluation")
       while (true) {
          print("TFUNC> ")
          try { 
             val input = scala.io.StdIn.readLine()
             val se = parse(input)
	     val result = time { se.processEntry(env,symt) }
	     env = result._1
	     symt = result._2
          } catch {
             case e : Exception => println(e.getMessage)
          } 
       }
   }

   def main (argv:Array[String]) : Unit = {
       shell()
   }

}
