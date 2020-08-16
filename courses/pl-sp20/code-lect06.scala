
//  Languages REF

//
//  Values
//



def runtimeError (msg: String) : Nothing = {
  
  throw new Exception("Runtime error: "+msg)
}

abstract class Value {

   // default behaviors for values
   
   def isInteger () : Boolean = false
   def isBoolean () : Boolean = false
   def isVector () : Boolean = false
   def isFunction () : Boolean = false
   def isRefCell () : Boolean = false
   def isNone () : Boolean = false
   
  def valueError (msg : String) : Nothing = {
      runtimeError(msg + "\n   in value " + this.toString())
   } 

   def getInt () : Int = {
      valueError("Value not of type INTEGER")
   }

   def getBool () : Boolean = {
      valueError("Value not of type BOOLEAN")
   }

   def getList () : List[Value] = {
      valueError("Value not of type VECTOR")
   }

   def apply (args: List[Value]) : Value =  {
      valueError("Value not of type FUNCTION")
   }

   def getRefContent () : Value = {
      valueError("Value not of type REFCELL")
   }
   
   def putRefContent (v:Value) : Unit = {
      valueError("Value not of type REFCELL")
   }

   def checkInteger () : Unit = {
     if (!isInteger()) {
        valueError("Value not of type INTEGER")
     }
   }

   def checkBoolean () : Unit = {
     if (!isBoolean()) {
        valueError("Value not of type BOOLEAN")
     }
   }

   def checkVector () : Unit = {
     if (!isVector()) {
        valueError("Value not of type VECTOR")
     }
   }

   def checkFunction () : Unit = {
     if (!isFunction()) {
        valueError("Value not of type FUNCTION")
     }
   }

   def checkRefCell () : Unit = {
     if (!isRefCell()) {
        valueError("Value not of type REFCELL")
     }
   }

   def checkUnit () : Unit = {
     if (!isNone()) {
        valueError("Value not of type NONE")
     }
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


class VVector (val l:List[Value]) extends Value {

  override def toString () : String = 
     return l.addString(new StringBuilder(), "VVector[ ", ", ", " ]").toString()

  override def isVector () : Boolean = true
  override def getList () : List[Value] = l
}


class VPrimitive (val oper : (List[Value]) => Value) extends Value {

  override def toString () : String = 
    "VPrimitive[" + Integer.toHexString(hashCode()) + "]"
  override def isFunction () : Boolean = true

  override def apply (args: List[Value]) : Value =
     oper(args)
}


class VRecClosure (val self: String, val params: List[String], val body:Exp, val env:Env) extends Value {

  override def toString () : String =
    "VClosure[" + self + ", " + params + ", " + body.toString() + ", " + env.toString() + "]"

  override def isFunction () : Boolean = true

  override def apply (args: List[Value]) : Value = {
     if (params.length != args.length) {
        runtimeError("wrong number of arguments\n  Function " + this.toString())
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
  
  override def toString () : String = "VRefCell[" + content.toString() + "]"
  override def isRefCell () : Boolean = true

  override def getRefContent () : Value = content
  override def putRefContent (v:Value) : Unit = {
    content = v
  }
}


object VNone extends Value {

  override def toString () : String = "VNone"
  override def isNone () : Boolean = true
}



//
//  Primitive operations
//

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

def operGetRefCell (vs:List[Value]) : Value = {
  checkArgsLength(vs,1,1)
  val r = vs(0)
  r.checkRefCell()
  return r.getRefContent()
}

def operPutRefCell (vs:List[Value]) : Value = {
  checkArgsLength(vs,2,2)
  val r = vs(0)
  val v = vs(1)
  r.checkRefCell()
  r.putRefContent(v)
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
       runtimeError(msg + "\n   in expression " + this.toString())
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

class EPrimitive (val oper : (List[Value]) => Value) extends Exp {

  override def toString () : String = 
    "EPrimitive[" + Integer.toHexString(hashCode()) + "]"

  def eval (env : Env) : Value = {
    new VPrimitive(oper)
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


class EId (val id : String) extends Exp {

    override def toString () : String =
        "EId[" + id + "]"

    def eval (env : Env) : Value = env.lookup(id)

}


class EApply (val f: Exp, val args: List[Exp]) extends Exp {
   override def toString () : String =
      "EApply[" + f.toString() + ", " + args.toString() + "]"
      
   def eval (env : Env) : Value = {
      val vf = f.eval(env)
      val vargs = args.map((e:Exp) => e.eval(env))
      return vf.apply(vargs)
   }
}


class EFunction (val params : List[String], val body : Exp) extends Exp {

   override def toString () : String =
     "EFunction[" + params.toString() + ", " + body.toString() + "]"
     
   def eval (env : Env) : Value =
      new VRecClosure("",params,body,env)

}

class ERecFunction (val self: String, val params: List[String], val body : Exp) extends Exp {

   override def toString () : String =
     "ERecFunction[" + self + ", " + params.toString() + ", " + body.toString() + "]"
     
   def eval (env : Env) : Value =
      new VRecClosure(self,params,body,env)
}


class EDo (val es : List[Exp]) extends Exp {

   override def toString () : String = 
      "EDo[" + es.toString() + "]"

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
        "ELet[" + bindings.toString() + ", " + ebody + "]"

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
       "EWhile[" + cond.toString() + ", " + body.toString() + "]"

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
 

   def FUN : Parser[Unit] = "fun" ^^ { s => () }
   def LET : Parser[Unit] = "let" ^^ { s => () }

   def DO : Parser[Unit] = "do" ^^ { s => () } 
   def WHILE : Parser[Unit] = "while" ^^ { s => () } 

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
      

   def expr : Parser[Exp] =
      ( atomic | expr_if | expr_fun | expr_funr | expr_let | expr_do | expr_while | expr_app) ^^
           { e => e }
}



class CStyleSExpParser extends RegexParsers { 

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
 
   def FUN : Parser[Unit] = "fun" ^^ { s => () }
   def LET : Parser[Unit] = "let" ^^ { s => () }

   def DO : Parser[Unit] = "do" ^^ { s => () } 
   def WHILE : Parser[Unit] = "while" ^^ { s => () }

   // CSTYLE: new token for set
   def SET : Parser[Unit] = "set" ^^ { s => () } 

   // grammar

   def atomic_int : Parser[Exp] = INT ^^ { i => new EInteger(i) }

   // CSTYLE: helper functions to call a primop
  // better to wrap the primop directly!
   def wrapRef (e:Exp) : Exp = new EApply(new EPrimitive(operRefCell), List(e))
   def wrapGet (e:Exp) : Exp = new EApply(new EPrimitive(operGetRefCell), List(e))
   def wrapPut (e1:Exp,e2:Exp) : Exp = new EApply(new EPrimitive(operPutRefCell), List(e1,e2))

   // CSTYlE: when referencing an identifier, we read  its cell
   def atomic_id : Parser[Exp] =
      ID ^^ { s => wrapGet(new EId(s)) }

   def atomic : Parser[Exp] =
      ( atomic_int | atomic_id ) ^^ { e => e}
      
   def expr_if : Parser[Exp] =
      LP ~ IF ~ expr ~ expr ~ expr ~ RP ^^
        { case _ ~ _ ~ e1 ~ e2 ~ e3 ~ _ => new EIf(e1,e2,e3) }

   // CSTYLE: a binding now wraps its expression in a ref cell
   def binding : Parser[(String,Exp)] =
      LP ~ ID ~ expr ~ RP ^^
         { case _ ~ n ~ e ~ _ => (n,wrapRef(e)) }
      
   def expr_let : Parser[Exp] =
      LP ~ LET ~ LP ~ rep(binding) ~ RP ~ expr ~ RP ^^
           { case _ ~ _ ~ _ ~ bindings ~ _ ~ e2 ~ _ => new ELet(bindings,e2) }

   // CSTYLE: the first thing a function does is "rebind" its arguments to ref cells
   //    containing the value of those arguments
   def expr_fun : Parser[Exp] =
      LP ~ FUN ~ LP ~ rep(ID) ~ RP ~ expr ~ RP ^^
        { case _ ~ _ ~ _ ~ params ~ _ ~ e ~ _ =>
	     new EFunction(params,
	                   new ELet(params.map((s) => (s,wrapRef(new EId(s)))), e)) }


   def expr_funr : Parser[Exp] =
      LP ~ FUN ~ ID ~ LP ~ rep(ID) ~ RP ~ expr ~ RP ^^
        { case _ ~ _ ~ self ~ _ ~ params ~ _ ~ e ~ _ =>
	     new ERecFunction(self,params,
  	                   new ELet(params.map((s) => (s,wrapRef(new EId(s)))), e)) }
			   

   def expr_app : Parser[Exp] =
      LP ~ expr ~ rep(expr) ~ RP ^^
        { case _ ~ ef ~ eargs ~ _ => new EApply(ef,eargs) }

   def expr_do : Parser[Exp] = 
      LP ~ DO ~ rep(expr) ~ RP ^^ { case _ ~ _ ~ es ~ _ => new EDo(es) }

   def expr_while : Parser[Exp] =
      LP ~ WHILE ~ expr ~ expr ~ RP ^^ { case _ ~ _ ~ cond ~ body ~ _ => new EWhile(cond,body) }

   // CSTYLE: set! expression -- cannot be a primitive operation (why?)
   def expr_set : Parser[Exp] =
      LP ~ SET ~ ID ~ expr ~ RP ^^
         { case _ ~ _ ~ s ~ e ~ _ => wrapPut(new EId(s),e) }

   def expr : Parser[Exp] =
      ( atomic | expr_if | expr_fun | expr_funr | expr_let | expr_do | expr_while | expr_set | expr_app) ^^
           { e => e }
}



//
//  Shell 
//
   
   
val stdBindings = List(
  ("true",new VBoolean(true)),
  ("false",new VBoolean(false)),
  ("not", new VRecClosure("", List("a"), new EIf(new EId("a"), new EBoolean(false), new EBoolean(true)), new Env())),
  ("+", new VPrimitive(operPlus)),
  ("*", new VPrimitive(operTimes)),
  ("=", new VPrimitive(operEqual)),
  ("<", new VPrimitive(operLess)),
  ("empty?",new VPrimitive(operEmpty)),
  ("first",new VPrimitive(operFirst)),
  ("rest",new VPrimitive(operRest)),
  ("vector", new VPrimitive(operVector)),
  ("empty",new VVector(List())),
  ("cons",new VPrimitive(operCons)),

  ("ref",new VPrimitive(operRefCell)),
  ("get",new VPrimitive(operGetRefCell)),
  ("put",new VPrimitive(operPutRefCell)),
  ("print",new VPrimitive(operPrint))
)


def shell () : Unit = {
  
  def parse (input:String) : Exp = {

    val parser = new SExpParser
    //  val parser = new CStyleSExpParser
    parser.parseAll(parser.expr, input) match {
      case parser.Success(result,_) => result
      case failure : parser.NoSuccess => throw new Exception("Cannot parse "+input+": "+failure.msg)
    }
  }

  val env = new Env(stdBindings)

  println("Lecture 6 (ML-style) - use :quit to get out")
  while (true) {
    try {
      val input = scala.io.StdIn.readLine("> ")
      if (input == ":quit") {
        return
      }
      val e = parse(input)
      println("PARSE = " + e.toString())
      val v = e.eval(env)
      println(v)
    } catch {
      case e : Exception => println(e.getMessage)
    }
  }
}

def shellC () : Unit = {
  
  def parse (input:String) : Exp = {

    val parser = new CStyleSExpParser
    parser.parseAll(parser.expr, input) match {
      case parser.Success(result,_) => result
      case failure : parser.NoSuccess => throw new Exception("Cannot parse "+input+": "+failure.msg)
    }
  }

  val env = new Env(stdBindings.map((ent) => (ent._1,new VRefCell(ent._2))))

  println("Lecture 6 (C-style) - use :quit to get out")
  while (true) {
    try {
      val input = scala.io.StdIn.readLine("> ")
      if (input == ":quit") {
        return
      }
      val e = parse(input)
      println("PARSE = " + e.toString())
      val v = e.eval(env)
      println(v)
    } catch {
      case e : Exception => println(e.getMessage)
    }
  }
}

