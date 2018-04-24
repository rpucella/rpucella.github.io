//
// language FUNC with (simple) static types
//
// Stack-based compiler
// Index-based identifier lookup
// inlining of primitive operations
// Virtual machine with numeric instructions
//


case class RuntimeError(s: String) extends Exception(s)
case class EnvError(s: String)  extends Exception(s)
case class TypeError(s: String)  extends Exception(s)
case class SyntaxError(s: String) extends Exception(s)


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
       throw new RuntimeError("Runtime error: "+msg)
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
      throw new TypeError("Type error: type is not a function\n   "+this)
   }

   def funResult () : Type = {
      throw new TypeError("Type error: type is not a function\n   "+this)
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
	  throw new EnvError("Environment error: unbound identifier "+id)
      }

      def find_index (id:String) : Int = {
          // index is position from the end!
          var idx = content.length-1
      	  for (entry <- content) {
	      if (entry._1 == id) {
	      	 return idx
	      }
	      idx = idx - 1
	  }
	  throw new EnvError("Environment error: unbound identifier "+id)
      }

      def lookup_by_index (idx : Int) : A =  
          content(content.length-idx-1)._2

      def map[B] (f:(A) => B) : Env[B] =
         new Env(content.map((p) => (p._1,f(p._2))))
}


abstract class Exp {

    def error (msg : String) : Nothing = { 
       throw new RuntimeError("Runtime error: "+ msg + "\n   in expression " + this)
    }

    def terror (msg : String) : Nothing = { 
       throw new TypeError("Type error: "+ msg + "\n   in expression " + this)
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


   def compile (addr:Int) : Vector[Int] 
   def compileTail (addr:Int) : Vector[Int]
   def indices (symt:Env[Unit]) : Exp
}


case class EInteger (val i:Integer) extends Exp {
    // integer literal

    override def toString () : String = 
        "EInteger(" + i + ")"

    def typeOf (symt:Env[Type]) : Type = 
        TInteger

    def eval (env:Env[Value]) : Value = 
        new VInteger(i)

    def compile (addr:Int) : Vector[Int] = 
       Vector(OpCodes.OP_PUSH,i)

    def compileTail (addr:Int) : Vector[Int] =
       Vector(OpCodes.OP_PUSH,i,OpCodes.OP_JUMP)

   def indices (symt:Env[Unit]) : Exp = this
       
}


case class EBoolean (val b:Boolean) extends Exp {
    // boolean literal

    override def toString () : String = 
        "EBoolean(" + b + ")"

    def typeOf (symt:Env[Type]) : Type = 
        TBoolean
	
    def eval (env:Env[Value]) : Value = 
        new VBoolean(b)

    def compile (addr:Int) : Vector[Int] = 
       Vector(OpCodes.OP_PUSH,if (b) 1 else 0)

    def compileTail (addr:Int) : Vector[Int] =
       Vector(OpCodes.OP_PUSH,if (b) 1 else 0,OpCodes.OP_JUMP)

   def indices (symt:Env[Unit]) : Exp = this
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

    def compile (addr:Int) : Vector[Int] = {
      val ccond = ec.compile(addr)
      val celse = ee.compile(addr+ccond.length+3)
      val then_part = addr+ccond.length+3+celse.length+3
      val cthen = et.compile(then_part)
      val done_part = then_part + cthen.length
      val result = (ccond ++ Vector(OpCodes.OP_PUSH_ADDR,then_part,OpCodes.OP_JUMP_TRUE) ++
              celse ++ Vector(OpCodes.OP_PUSH_ADDR,done_part,OpCodes.OP_JUMP) ++
	      cthen)
      //OpCodes.dump(result,addr)
      return result
    }

    def compileTail (addr:Int) : Vector[Int] = {
      val ccond = ec.compile(addr)
      val celse = ee.compileTail(addr+ccond.length+3)
      val then_part = addr+ccond.length+3+celse.length
      val cthen = et.compileTail(then_part)
      val result = (ccond ++ Vector(OpCodes.OP_PUSH_ADDR,then_part,OpCodes.OP_JUMP_TRUE) ++
              celse ++ cthen)
      //OpCodes.dump(result,addr)
      return result
    }

   def indices (symt:Env[Unit]) : Exp = 
      EIf(ec.indices(symt),
          et.indices(symt),
          ee.indices(symt))
}


case class EId (val id : String) extends Exp {

    override def toString () : String =
        "EId(" + id + ")"

    def typeOf (symt:Env[Type]) : Type =  symt.lookup(id)

    def eval (env : Env[Value]) : Value = env.lookup(id)

    def compile (addr:Int) : Vector[Int] =
       error("not implemented")

    def compileTail (addr:Int) : Vector[Int] =
       error("not implemented")

    def indices (symt:Env[Unit]) : Exp = 
       EIndex(symt.find_index(id))
}


case class EIndex (val idx : Int) extends Exp {

    override def toString () : String =
        "EIndex(" + idx + ")"

    def typeOf (symt:Env[Type]) : Type =
       error("not implemented")

    def eval (env:Env[Value]) : Value =
       return env.lookup_by_index(idx)
       
    def compile (addr:Int) : Vector[Int] =
       Vector(OpCodes.OP_LOOKUP,idx)

    def compileTail (addr:Int) : Vector[Int] =
       Vector(OpCodes.OP_LOOKUP,idx,OpCodes.OP_JUMP)

    def indices (symt:Env[Unit]) : Exp =
       error("not implemented")
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
      val vf = f.eval(env)
      val vargs = args.map((e:Exp) => e.eval(env))
      if (vf.isPrimOp()) {
         return vf.applyOper(vargs)
      }
      // defined function
      // push the vf closure as the value bound to identifier self
      var new_env = vf.getEnv().push(vf.getSelf(),vf)
      for ((p,v) <- vf.getParams().zip(vargs)) {
         new_env = new_env.push(p,v)
      }
      return vf.getBody().eval(new_env)
   }

   def getPrim (i:Int) : (List[CodeValue]) => CodeValue = {
      // println("[index = "+i+"]")
      // remember, index is position from the end!
      i match {
         case 3 => CVOps.operPlus
	 case 2 => CVOps.operTimes
	 case 1 => CVOps.operEqual
	 case 0 => CVOps.operLess
      }
    }
    def compile (addr:Int) : Vector[Int] = {

       f match {
           case EIndex(idx) if (idx < 4) =>
	      var code = Vector() : Vector[Int]
              var caddr = addr
              for (e <- args) {
                 val ce = e.compile(caddr)
                 code = code ++ ce
                 caddr += ce.length
              }
              return code ++ Vector(OpCodes.OP_PRIMCALL,idx)
	      
	   case _ => {
              var code = Vector(OpCodes.OP_PUSH_ENV) : Vector[Int]
              var caddr = addr+1
              for (e <- args.reverse) {
                 val ce = e.compile(caddr)
                 code = code ++ ce
                 caddr += ce.length
              }
              val cf = f.compile(caddr)
              code = code ++ cf
              caddr += cf.length
              return code ++ Vector(OpCodes.OP_PUSH_ADDR,caddr+4,
                                    OpCodes.OP_POP_CLOSURE,
                                    OpCodes.OP_JUMP,
                                    OpCodes.OP_POP_ENV)
           }
       }
    }

    def compileTail (addr:Int) : Vector[Int] = {

       f match {
           case EIndex(idx) if (idx < 4) =>
	      var code = Vector() : Vector[Int]
              var caddr = addr
              for (e <- args) {
                 val ce = e.compile(caddr)
                 code = code ++ ce
                 caddr += ce.length
              }
              return code ++ Vector(OpCodes.OP_PRIMCALL,idx,OpCodes.OP_JUMP)

           case _ => 
              var code = Vector() : Vector[Int]
              var caddr = addr
              for (e <- args.reverse) {
                 val ce = e.compile(caddr)
                 code = code ++ ce
                 caddr += ce.length
              }
              val cf = f.compile(caddr)
              code = code ++ cf
              caddr += cf.length
              return code ++ Vector(OpCodes.OP_POP_CLOSURE,
                                    OpCodes.OP_JUMP)
       }
    }

    def indices (symt:Env[Unit]) : Exp = 
       EApply(f.indices(symt),
              args.map((e) => e.indices(symt)))
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
      // assume self has the declared function type
      if (self != "") { 
        new_symt = new_symt.push(self,typ)
      }
      for ((p,pt) <- params.zip(tparams)) {
        new_symt = new_symt.push(p,pt)
      }
      if (body.typeCheck(tresult,new_symt)) {
        return typ
      } else {
        terror("Return type of function not same as declared")
      }
    }      

   def eval (env : Env[Value]) : Value =
      new VRecClosure(self,params,body,env)

    def compile (addr:Int) : Vector[Int] = {
       // cute way to create a vector of length params.length
       // filled with Op_POP_TO_ENV()
       var cfun = Vector[Int]()
       
       if (self == "") {
          // not recursive
          cfun = params.map((n) => OpCodes.OP_POP_TO_ENV).toVector ++
                     body.compileTail(addr+6+params.length)
       } else {
          cfun = Vector( OpCodes.OP_PUSH_ADDR,addr+6,
		         OpCodes.OP_PUSH_CLOSURE,
		         OpCodes.OP_POP_TO_ENV) ++
                     params.map((n) => OpCodes.OP_POP_TO_ENV).toVector ++
                     body.compileTail(addr+10+params.length)
       }
       val fun_part = addr+6
       val done_part = addr+6+cfun.length
       return Vector(OpCodes.OP_PUSH_ADDR,fun_part,
		     OpCodes.OP_PUSH_CLOSURE,
		     OpCodes.OP_PUSH_ADDR,done_part,
		     OpCodes.OP_JUMP) ++ cfun
    }

    def compileTail (addr:Int) : Vector[Int] = {
       // cute way to create a vector of length params.length
       // filled with Op_POP_TO_ENV()
       var cfun = Vector[Int]()
       
       if (self == "") {
          // not recursive
          cfun = params.map((n) => OpCodes.OP_POP_TO_ENV).toVector ++
                     body.compile(addr+6+params.length) ++
		     Vector(OpCodes.OP_JUMP)
       } else {
          cfun = Vector( OpCodes.OP_PUSH_ADDR,addr+6,
		         OpCodes.OP_PUSH_CLOSURE,
		         OpCodes.OP_POP_TO_ENV) ++
                     params.map((n) => OpCodes.OP_POP_TO_ENV).toVector ++
                     body.compile(addr+11+params.length) ++
		     Vector(OpCodes.OP_JUMP)
       }
       val fun_part = addr+6
       val done_part = addr+6+cfun.length
       return Vector(OpCodes.OP_PUSH_ADDR,fun_part,
		     OpCodes.OP_PUSH_CLOSURE,
		     OpCodes.OP_PUSH_ADDR,done_part,
		     OpCodes.OP_JUMP) ++ cfun ++ Vector(OpCodes.OP_JUMP)
    }


    def indices (symt:Env[Unit]) : Exp = {
      var new_symt = symt
      if (self != "") { 
        new_symt = new_symt.push(self,())
      }
      for (p <- params) {
        new_symt = new_symt.push(p,())
      }
      ERecFunction(self,params,typ,body.indices(new_symt))
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

    def eval (env : Env[Value]) : Value = {
        var new_env = env
        for ((n,e) <- bindings) { 
          val v = e.eval(env)
	  new_env = new_env.push(n,v)
	}
	return ebody.eval(new_env)
    }

    def compile (addr:Int) : Vector[Int] = {
       var result = Vector() : Vector[Int]
       var caddr = addr
       for ((n,e) <- bindings.reverse) {
          val c = e.compile(caddr)
	  result = result ++ c
	  caddr += c.length
       }
       result = result ++ bindings.map((p) => OpCodes.OP_POP_TO_ENV).toVector
       caddr = addr + result.length
       result = result ++ ebody.compile(caddr)
       return result
    }

    def compileTail (addr:Int) : Vector[Int] = {
       var result = Vector() : Vector[Int]
       var caddr = addr
       for ((n,e) <- bindings.reverse) {
          val c = e.compile(caddr)
	  result = result ++ c
	  caddr += c.length
       }
       result = result ++ bindings.map((p) => OpCodes.OP_POP_TO_ENV).toVector
       caddr = addr + result.length
       result = result ++ ebody.compileTail(caddr)
       return result
    }

    def indices (symt:Env[Unit]) : Exp = {
       var new_symt = symt
       for ((n,e) <- bindings) {
	 new_symt = new_symt.push(n,())
       }
       ELet(bindings.map((p) => (p._1,p._2.indices(symt))),
            ebody.indices(new_symt))
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
      
}



//
//  Shell 
//


  // abstract class for shell entries
  // (representing the various entries you
  //  can type at the shell)
abstract class ShellEntry 
case class SEshow (e:Exp) extends ShellEntry
case class SEexpr (e:Exp) extends ShellEntry
case class SEquit () extends ShellEntry


object Shell {

   val parser = new SExpParser

   def parse (input:String) : ShellEntry = {
   
      parser.parseAll(parser.shell_entry, input) match {
         case parser.Success(result,_) => result
         case failure : parser.NoSuccess => throw new SyntaxError("Cannot parse "+input+": "+failure.msg)
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

   val initFastCode = Vector(OpCodes.OP_PRIMCALL, 3,
			     OpCodes.OP_JUMP,
			     OpCodes.OP_PRIMCALL, 2,
			     OpCodes.OP_JUMP,
			     OpCodes.OP_PRIMCALL, 1,
			     OpCodes.OP_JUMP,
			     OpCodes.OP_PRIMCALL, 0,
			     OpCodes.OP_JUMP)

   val stdCodeEnv = List(CVClosure(0,List()),
                         CVClosure(3,List()),
                         CVClosure(6,List()),
                         CVClosure(9,List()))

   def shell () : Unit = {
   
       var env = stdEnv
       var symt = stdSymt
       var cenv = stdCodeEnv
       val code = new FastCode()
       code.append(initFastCode)
       
       while (true) {
          print("TFUNC> ")
          try { 
             val input = scala.io.StdIn.readLine()
             val se = parse(input)
	     val result = 
                se match {
		
		   case SEshow(e) => {
                      val t = e.typeOf(symt)
		      val ee = e.indices(symt.map((t) => ()))
		      val caddr = code.length()
                      val c = ee.compile(caddr) ++ Vector(OpCodes.OP_STOP)
		      OpCodes.dump(c,caddr)
                      code.append(c)
                      val v = time { code.execute(caddr,cenv) } 
                      println(v)
		   }
		   
		   case SEexpr(e) => {
                      val t = e.typeOf(symt)
		      val ee = e.indices(symt.map((t) => ()))
		      val caddr = code.length()
                      val c = ee.compile(caddr) ++ Vector(OpCodes.OP_STOP)
                      code.append(c)
                      val v = time { code.execute(caddr,cenv) } 
                      println(v)
                   }
		   
		   case SEquit() => 
                      System.exit(0)
		}
          } catch {
	     case e : RuntimeError => println(e.getMessage)
	     case e : EnvError => println(e.getMessage)
	     case e : TypeError => println(e.getMessage)
	     case e : SyntaxError => println(e.getMessage)
             case e : Exception => println("EXCEPTION: " + e)
          } 
       }
   }

   def main (argv:Array[String]) : Unit = {
       shell()
   }

}



class FastCode {

   var content : Vector[Int] = Vector()
   

   override def toString () : String = content.toString()

   def append (code: Vector[Int]) : Unit = {
      content = content ++ code
   }

   def length () : Int = content.length

   val stackSize = 100000
   
   def execute (i:Int,env:List[CodeValue]) : CodeValue = {
      val vStack : Array[CodeValue] = new Array[CodeValue](stackSize)
      val aStack : Array[Int] = new Array[Int](stackSize)
      val eStack : Array[List[CodeValue]] = new Array[List[CodeValue]](stackSize)

      var vStackPtr = -1
      var aStackPtr = -1
      var eStackPtr = -1

      var PC = i
      var ENV = env

      try { 
      while (true) {
        //println(PC + " <- " + content(PC))
        content(PC) match {
	   case 0  =>
	      return vStack(vStackPtr)
	   case 1  =>
	      // vStack.push(CVInteger(content(PC+1)))
	      vStackPtr += 1
	      vStack(vStackPtr) = CVInteger(content(PC+1))
	      PC = PC + 2
	   case 2 => 
	      //aStack.push(content(PC+1))
	      aStackPtr += 1
	      aStack(aStackPtr) = content(PC+1)
	      PC = PC + 2
	   case 3 =>
	      //val a = aStack.pop()
	      val a = aStack(aStackPtr)
	      aStackPtr -= 1
	      PC = a
	   case 4 => 
	      //val a = aStack.pop()
	      //val v = vStack.pop()
	      val a = aStack(aStackPtr)
	      aStackPtr -= 1
	      val v = vStack(vStackPtr)
	      vStackPtr -= 1
	      v match {
	         case CVInteger(i) => 
  		   if (i != 0) {
		     PC = a
		   } else {
	             PC = PC + 1
                   }
	      }
	   case 5 => 
	       //val v = vStack.pop()
	       val v = vStack(vStackPtr)
	       vStackPtr -= 1
	       v match {
	         case CVClosure(addr,env) =>
		    //aStack.push(addr)
		    aStackPtr += 1
		    aStack(aStackPtr) = addr
		    ENV = env
		    PC = PC + 1
	       }
	   case 6 =>
	       //ENV = eStack.pop()
	       ENV = eStack(eStackPtr)
	       eStackPtr -= 1
	       PC = PC + 1
	   case 7 => 
	       //eStack.push(ENV)
	       eStackPtr += 1
	       eStack(eStackPtr) = ENV
	       PC = PC + 1
	   case 8 => 
	       //val a = aStack.pop()
	       //vStack.push(CVClosure(a,ENV))
	       val a = aStack(aStackPtr)
	       aStackPtr -= 1
	       vStackPtr += 1
	       vStack(vStackPtr) = CVClosure(a,ENV)
	       PC = PC + 1
	   case 9 => 
	       //val v = vStack.pop()
	       val v = vStack(vStackPtr)
	       vStackPtr -= 1
	       ENV = v::ENV
	       PC = PC + 1
	   case 10 =>
	       //vStack.push(ENV(ENV.length-content(PC+1)-1))
	       vStackPtr += 1
	       vStack(vStackPtr) = ENV(ENV.length-content(PC+1)-1)
	       PC = PC + 2
	   case 11 =>
	       val prim = content(PC+1)
	       var vs : List[CodeValue] = List()
	       for (i <- 1 to OpCodes.getNumParams(prim)) { 
   	         //vs = (vStack.pop()) :: vs
		 vs = (vStack(vStackPtr)) :: vs
		 vStackPtr -= 1
	       }
               //vStack.push(OpCodes.getPrim(prim)(vs))
	       vStackPtr += 1
	       vStack(vStackPtr) = OpCodes.getPrim(prim)(vs)
	       PC = PC + 2
	    case 12 =>
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
case class CVClosure (a:Int,e:List[CodeValue]) extends CodeValue

class Stack[A] {

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


object OpCodes {

   val OP_STOP = 0
   val OP_PUSH = 1         // INT
   val OP_PUSH_ADDR = 2    // ADDR
   val OP_JUMP = 3
   val OP_JUMP_TRUE = 4
   val OP_POP_CLOSURE = 5
   val OP_POP_ENV = 6
   val OP_PUSH_ENV = 7
   val OP_PUSH_CLOSURE = 8
   val OP_POP_TO_ENV = 9
   val OP_LOOKUP = 10      // INT
   val OP_PRIMCALL = 11    // INT
   val OP_NOP = 12

   def decode (c: Vector[Int], i: Int) : (String, Int) = {
      c(i) match {
         case 0 => ("STOP",1)
	 case 1 => ("PUSH["+c(i+1)+"]", 2)
	 case 2 => ("PUSH-ADDR["+c(i+1)+"]", 2)
	 case 3 => ("JUMP",1)
	 case 4 => ("JUMP-TRUE",1)
	 case 5 => ("POP-CLOSURE",1)
	 case 6 => ("POP-ENV",1)
	 case 7 => ("PUSH-ENV",1)
	 case 8 => ("PUSH-CLOSURE",1)
	 case 9 => ("POP-TO-ENV",1)
	 case 10 => ("LOOKUP["+c(i+1)+"]",2)
	 case 11 => ("PRIMCALL["+c(i+1)+"]",2)
	 case 12 => ("NOP",1)
         case _ => ("???",1)
      }
   }

   def dump (c:Vector[Int],caddr:Int) : Unit = {
      var i = 0
      while (i < c.length) {
         val (str,len) = decode(c,i)
         println("  " + (i+caddr) + " : " + str)
         i += len
      }
   }


   def getNumParams (i:Int) : Int =
      i match {
         case 3 => 2
	 case 2 => 2
	 case 1 => 2
	 case 0 => 2
      }
   
   def getPrim (i:Int) : (List[CodeValue]) => CodeValue =
      i match {
         case 3 => CVOps.operPlus
	 case 2 => CVOps.operTimes
	 case 1 => CVOps.operEqual
	 case 0 => CVOps.operLess
      }

}

// primitive operations on code values

object CVOps { 

   def runtimeError (msg: String) : Nothing = {
       throw new RuntimeError("Runtime error: "+msg)
   }

   def operPlus (vs:List[CodeValue]) : CodeValue = {
   
      val v1 = vs(0)
      val v2 = vs(1)

      //println("[callin' + with "+(v1,v2)+"]")
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

