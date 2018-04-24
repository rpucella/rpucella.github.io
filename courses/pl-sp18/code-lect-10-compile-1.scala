//
// language FUNC with (simple) static types
//
// Simple stack-based compiler
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

   def toCodeValue () : CodeValue =
      throw new Exception("Cannot convert to code value: "+this)
}


class VInteger (val i:Int) extends Value {

  override def toString () : String = i.toString()
  override def getInt () : Int = i
  override def toCodeValue () : CodeValue = CVInteger(i)
}


class VBoolean (val b:Boolean) extends Value {

  override def toString () : String = b.toString()
  override def getBool () : Boolean = b
  override def toCodeValue () : CodeValue = CVInteger(if (b) 1 else 0)
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
   
       return new VBoolean(v1.getInt() < v2.getInt())
   }
   
}




//
//  Types
//

abstract class Type {

   def isSame (t:Type) : Boolean
   
   def isInteger () : Boolean = return false
   def isBoolean () : Boolean = return false
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

    def eval (env : Env[Value]) : Value = {

      var currExp = this
      var currEnv = env

      while (true) {
         currExp match {
            case EInteger(i) =>
	       return new VInteger(i) 
	    case EBoolean(b) =>
	       return new VBoolean(b)
	    case EIf(ec,et,ee) => {
              val ev = ec.eval(currEnv)
              if (!ev.getBool()) { 
                currExp = ee
              } else {
                currExp = et
              }
	     }
	     case EId(n) =>
	       return currEnv.lookup(n)
	     case EApply(f,args) => {
               val vf = f.eval(currEnv)
               val vargs = args.map((e:Exp) => e.eval(currEnv))
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
             case ERecFunction(self,params,typ,body) =>
	        return new VRecClosure(self,params,body,currEnv)
	     case ELet(bindings,body) => {
                var new_env = currEnv
                for ((n,e) <- bindings) { 
                  val v = e.eval(currEnv)
                  new_env = new_env.push(n,v)
                }
		currEnv = new_env
		currExp = body
              }
           }
        }
	return new VInteger(0) // needed for typechecking
   }


   def compile (addr:Int) : Vector[Opcode] 
}


case class EInteger (val i:Integer) extends Exp {
    // integer literal

    override def toString () : String = 
        "EInteger(" + i + ")"

    def typeOf (symt:Env[Type]) : Type = 
        TInteger

    def compile (addr:Int) : Vector[Opcode] = 
       Vector(Op_PUSH(i))
}


case class EBoolean (val b:Boolean) extends Exp {
    // boolean literal

    override def toString () : String = 
        "EBoolean(" + b + ")"

    def typeOf (symt:Env[Type]) : Type = 
        TBoolean
	
    def compile (addr:Int) : Vector[Opcode] = 
       Vector(Op_PUSH(if (b) 1 else 0))
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

    def compile (addr:Int) : Vector[Opcode] = {
      val ccond = ec.compile(addr)
      val celse = ee.compile(addr+ccond.length+2)
      val then_part = addr+ccond.length+2+celse.length+2
      val cthen = et.compile(then_part)
      val done_part = then_part + cthen.length
      return (ccond ++ Vector(Op_PUSH_ADDR(then_part),Op_JUMP_TRUE()) ++
              celse ++ Vector(Op_PUSH_ADDR(done_part),Op_JUMP()) ++
	      cthen)
    }
}


case class EId (val id : String) extends Exp {

    override def toString () : String =
        "EId(" + id + ")"

    def typeOf (symt:Env[Type]) : Type =  symt.lookup(id)

    def compile (addr:Int) : Vector[Opcode] =
       Vector(Op_LOOKUP(id))
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

    def compile (addr:Int) : Vector[Opcode] = {

       var code = Vector(Op_PUSH_ENV()) : Vector[Opcode]
       var caddr = addr+1
       for (e <- args.reverse) {
          val ce = e.compile(caddr)
          code = code ++ ce
	  caddr += ce.length
       }
       val cf = f.compile(caddr)
       code = code ++ cf
       caddr += cf.length
       return code ++ Vector(Op_PUSH_ADDR(caddr+3),
       	                     Op_POP_CLOSURE(),
			     Op_JUMP(),
			     Op_POP_ENV())
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
      // assume self has the declared function type
      var new_symt = symt.push(self,typ)
      for ((p,pt) <- params.zip(tparams)) {
        new_symt = new_symt.push(p,pt)
      }
      if (body.typeCheck(tresult,new_symt)) {
        return typ
      } else {
        terror("Return type of function not same as declared")
      }
    }      

    def compile (addr:Int) : Vector[Opcode] = {
       // cute way to create a vector of length params.length
       // filled with Op_POP_TO_ENV()
       var cfun = Vector[Opcode]()
       
       if (self == "") {
          // not recursive
          cfun = params.map((n) => Op_POP_TO_ENV(n)).toVector ++
                     body.compile(addr+4+params.length) ++
		     Vector(Op_JUMP())
       } else {
          cfun = Vector( Op_PUSH_ADDR(addr+4),
		         Op_PUSH_CLOSURE(),
		         Op_POP_TO_ENV(self)) ++
                     params.map((n) => Op_POP_TO_ENV(n)).toVector ++
                     body.compile(addr+7+params.length) ++
		     Vector(Op_JUMP())
       }
       val fun_part = addr+4
       val done_part = addr+4+cfun.length
       return Vector(Op_PUSH_ADDR(fun_part),
		     Op_PUSH_CLOSURE(),
		     Op_PUSH_ADDR(done_part),
		     Op_JUMP()) ++ cfun
    }
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

    def compile (addr:Int) : Vector[Opcode] = {
       var result = Vector() : Vector[Opcode]
       var caddr = addr
       for ((n,e) <- bindings.reverse) {
          val c = e.compile(caddr)
	  result = result ++ c
	  caddr += c.length
       }
       result = result ++ bindings.map((p) => Op_POP_TO_ENV(p._1)).toVector
       caddr = addr + result.length
       result = result ++ ebody.compile(caddr)
       return result
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
   def TRUE : Parser[Unit] = "true" ^^ { s => () } 
   def FALSE : Parser[Unit] = "false" ^^ { s => () } 

   def FUN : Parser[Unit] = "fun" ^^ { s => () }
   def LET : Parser[Unit] = "let" ^^ { s => () }

   def TINT : Parser[Unit] = "int" ^^ { s => () } 
   def TBOOL : Parser[Unit] = "bool" ^^ { s => () }
   def TFUN : Parser[Unit] = "tfun" ^^ { s => () }
   
   // grammar

   def atomic_int : Parser[Exp] = INT ^^ { i => new EInteger(i) }

   def atomic_bool : Parser[Exp] = (TRUE ^^ { e => new EBoolean(true) } |
                                    FALSE ^^ { e => new EBoolean(false) } )

   def atomic_id : Parser[Exp] =
      ID ^^ { s => new EId(s) }

   def atomic : Parser[Exp] =
      ( atomic_int | atomic_bool | atomic_id ) ^^ { e => e}
      
   def expr_if : Parser[Exp] =
      LP ~ IF ~ expr ~ expr ~ expr ~ RP ^^
        { case _ ~ _ ~ e1 ~ e2 ~ e3 ~ _ => new EIf(e1,e2,e3) }

   def binding : Parser[(String,Exp)] =
      LP ~ ID ~ expr ~ RP ^^ { case _ ~ n ~ e ~ _ => (n,e) }
      
   def expr_let : Parser[Exp] =
      LP ~ LET ~ LP ~ rep(binding) ~ RP ~ expr ~ RP ^^
           { case _ ~ _ ~ _ ~ bindings ~ _ ~ e2 ~ _ => new ELet(bindings,e2) }

   def expr_fun : Parser[Exp] =
      LP ~ FUN ~ LP ~ rep(ID) ~ RP ~ typ ~ expr ~ RP ^^
        { case _ ~ _ ~ _ ~ params ~ _ ~ typ ~ e ~ _ => new ERecFunction("",params,typ,e) }

   def expr_funr : Parser[Exp] =
      LP ~ FUN ~ ID ~ LP ~ rep(ID) ~ RP ~ typ ~ expr ~ RP ^^
        { case _ ~ _ ~ self ~ _ ~ params ~ _ ~ typ ~ e ~ _ => new ERecFunction(self,params,typ,e) }

   def expr_app : Parser[Exp] =
      LP ~ expr ~ rep(expr) ~ RP ^^ { case _ ~ ef ~ eargs ~ _ => new EApply(ef,eargs) }


   def expr : Parser[Exp] =
      ( atomic | expr_if | expr_fun | expr_funr | expr_let | expr_app) ^^
           { e => e }

   def typ_int : Parser[Type] =
      TINT ^^ { _ => TInteger }
      
   def typ_bool : Parser[Type] =
      TBOOL ^^ { _ => TBoolean }

   def typ_fun : Parser[Type] =
      LP ~ TFUN ~ LP ~ rep(typ) ~ RP ~ typ ~ RP ^^
         { case _ ~ _ ~ _ ~ tparams ~ _ ~ tresult ~ _ => new TFunction(tparams,tresult) }

   def typ : Parser[Type] =
      ( typ_int | typ_bool | typ_fun ) ^^ { e => e }

   def shell_entry : Parser[ShellEntry] =
//      (LP ~ "define" ~ ID ~ expr ~ RP  ^^ { case _ ~ _ ~ n ~ e ~ _  => new SEdefine(n,e) }) |
      (expr ^^ { e => new SEexpr(e) }) |
      ("#quit" ^^ { s => new SEquit() }) |
      ("#show" ~ expr ^^ { case _ ~ e => new SEshow(e) })
//      ("#exec" ~ expr ^^ { case _ ~ e => new SEexec(e) }) 
      
}



//
//  Shell 
//


  // abstract class for shell entries
  // (representing the various entries you
  //  can type at the shell)
abstract class ShellEntry 
case class SEshow (e:Exp) extends ShellEntry
// case class SEexec (e:Exp) extends ShellEntry
case class SEexpr (e:Exp) extends ShellEntry
// case class SEdefine (n:String, e:Exp) extends ShellEntry
case class SEquit () extends ShellEntry


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
     ("+", new VPrimOp(Ops.operPlus)),
     ("*", new VPrimOp(Ops.operTimes)),
     ("=", new VPrimOp(Ops.operEqual)),
     ("<", new VPrimOp(Ops.operLess))
   ))

   val stdSymt = new Env[Type](List(
     ("+", new TFunction(List(TInteger,TInteger),TInteger)),
     ("*", new TFunction(List(TInteger,TInteger),TInteger)),
     ("=", new TFunction(List(TInteger,TInteger),TBoolean)),
     ("<", new TFunction(List(TInteger,TInteger),TBoolean))
   ))

   val initCode = Vector(Op_PRIMCALL(2,CVOps.operPlus),
			 Op_JUMP(),
			 Op_PRIMCALL(2,CVOps.operTimes),
			 Op_JUMP(),
			 Op_PRIMCALL(2,CVOps.operEqual),
			 Op_JUMP(),
			 Op_PRIMCALL(2,CVOps.operLess),
			 Op_JUMP())

   val stdCodeEnv = new Env[CodeValue](List(
      ("+", CVClosure(0,new Env(List()))),
      ("*", CVClosure(2,new Env(List()))),
      ("=", CVClosure(4,new Env(List()))),
      ("<", CVClosure(6,new Env(List())))))


   def shell () : Unit = {
   
       var env = stdEnv
       var symt = stdSymt
       var cenv = stdCodeEnv
       val code = new Code()
       code.append(initCode)
       
       while (true) {
          print("TFUNC(C)> ")
          try { 
             val input = scala.io.StdIn.readLine()
             val se = parse(input)
	     val result = 
                se match {
		   case SEshow(e) => {
                      val t = e.typeOf(symt)
		      val caddr = code.length()
                      val c = e.compile(caddr) ++ Vector(Op_STOP())
                      var i = caddr
                      for (v <- c) {
                        println("  " + i + " : " + v)
                	i += 1
                      }
                      code.append(c)
                      val v = time { code.execute(caddr,cenv) } 
                      println(v)
		   }
		   case SEexpr(e) =>  {
                      val t = e.typeOf(symt)
		      val caddr = code.length()
                      val c = e.compile(caddr) ++ Vector(Op_STOP())
                      code.append(c)
                      val v = time { code.execute(caddr,cenv) } 
                      println(v)
                   }
/*		   case SEdefine(n,e) => {
                      val t = e.typeOf(symt)
                      val v = e.eval(env)
                      println(n + " defined with type " + t)
		      env = env.push(n,v)
		      symt = symt.push(n,t)
		   }
*/		   case SEquit() => 
                      System.exit(0)
		}
          } catch {
             case e : Exception => println(e.getMessage)
          } 
       }
   }

   def main (argv:Array[String]) : Unit = {
       shell()
   }

}



class Code {

   var content : Vector[Opcode] = Vector()
   
   override def toString () : String = content.toString()

   def append (code: Vector[Opcode]) : Unit = {
      content = content ++ code
   }

   def length () : Int = content.length
   
   def execute (i:Int,env:Env[CodeValue]) : CodeValue = {
      val vStack : Stack[CodeValue] = new Stack()
      val aStack : Stack[Int] = new Stack()
      val eStack : Stack[Env[CodeValue]] = new Stack()

      var PC = i
      var ENV = env
      
      try { 
      while (true) { 
        content(PC) match {
	   case Op_STOP() =>
	      return vStack.top()
	   case Op_PUSH(i) =>
	      vStack.push(new CVInteger(i))
	      PC = PC + 1
	   case Op_PUSH_ADDR(a) =>
	      aStack.push(a)
	      PC = PC + 1
	   case Op_JUMP() =>
	      val a = aStack.pop()
	      PC = a
	   case Op_JUMP_TRUE() =>
	      val a = aStack.pop()
	      val v = vStack.pop()
	      v match {
	         case CVInteger(i) => 
  		   if (i != 0) {
		     PC = a
		   } else {
	             PC = PC + 1
                   }
	      }
	   case Op_POP_CLOSURE() =>
	       val v = vStack.pop()
	       v match {
	         case CVClosure(addr,env) =>
		    aStack.push(addr)
		    ENV = env
		    PC = PC + 1
	       }
	   case Op_POP_ENV() =>
	       ENV = eStack.pop()
	       PC = PC + 1
	   case Op_PUSH_ENV() =>
	       eStack.push(ENV)
	       PC = PC + 1
	   case Op_PUSH_CLOSURE() =>
	       val a = aStack.pop()
	       vStack.push(CVClosure(a,ENV))
	       PC = PC + 1
	   case Op_POP_TO_ENV(n) =>
	       val v = vStack.pop()
	       ENV = ENV.push(n,v)
	       PC = PC + 1
	   case Op_LOOKUP(n) =>
	       vStack.push(ENV.lookup(n))
	       PC = PC + 1
	   case Op_PRIMCALL(n,p) =>
	       var vs : List[CodeValue] = List()
	       for (i <- 1 to n) { 
   	         vs = (vStack.pop()) :: vs
	       }
               vStack.push(p(vs))
	       PC = PC + 1
	    case Op_NOP() =>
	       PC = PC + 1
	}
      }
      } catch {
           case e : Throwable => {
	      println("At PC = " + PC)
	      println(e)
	      throw e
	   }
	   
      }
      return CVInteger(0)   // to satisfy the type checker...
   }
}

abstract class CodeValue {
  def getInt () : Int = 0 
}
case class CVInteger (i:Int) extends CodeValue {
  override def getInt () : Int = i
}
case class CVClosure (a:Int,e:Env[CodeValue]) extends CodeValue


class Stack[A] () {

   var content : List[A] = List()

   def push (v:A) : Unit = {
      content = v::content
   }

   def pop () : A = {
      val v = content.head
      content = content.tail
      return v
   }

   def top () : A = content.head
}


abstract class Opcode
case class Op_STOP () extends Opcode
case class Op_PUSH (i:Integer) extends Opcode
case class Op_PUSH_ADDR (a:Integer) extends Opcode
case class Op_JUMP () extends Opcode
case class Op_JUMP_TRUE () extends Opcode
case class Op_POP_CLOSURE () extends Opcode
case class Op_POP_ENV () extends Opcode
case class Op_PUSH_ENV () extends Opcode
case class Op_PUSH_CLOSURE () extends Opcode
case class Op_POP_TO_ENV (n:String) extends Opcode
case class Op_LOOKUP (n:String) extends Opcode
case class Op_PRIMCALL (n:Int,p:(List[CodeValue]) => CodeValue) extends Opcode
case class Op_NOP () extends Opcode


// primitive operations on code values

object CVOps { 

   def runtimeError (msg: String) : Nothing = {
       throw new Exception("Runtime error: "+msg)
   }

   def operPlus (vs:List[CodeValue]) : CodeValue = {
   
      val v1 = vs(0)
      val v2 = vs(1)
      
      return new CVInteger(v1.getInt() + v2.getInt())
   }
   
   
   def operTimes (vs: List[CodeValue]):CodeValue = {
   
      val v1 = vs(0)
      val v2 = vs(1)
      
      return new CVInteger(v1.getInt() * v2.getInt())
   }
   
   
   def operEqual (vs: List[CodeValue]) : CodeValue = {
       
      val v1 = vs(0)
      val v2 = vs(1)
   
      return new CVInteger(if (v1.getInt() == v2.getInt()) 1 else 0)
   }
   
   
   def operLess (vs: List[CodeValue]) : CodeValue = {
   
       val v1 = vs(0)
       val v2 = vs(1)
   
       return new CVInteger(if (v1.getInt() < v2.getInt()) 1 else 0)
   }
   
}

