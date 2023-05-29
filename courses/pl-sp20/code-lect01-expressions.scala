
//
//  Expressions
//
 

abstract class Exp {

    def eval () : Int

}
    
    
class EInteger (val i:Int) extends Exp {
    // Integer literal

    override def toString () : String = 
        "EInteger(" + i + ")"

    def eval () : Int = 
        i
}



class EPlus (val e1 : Exp, val e2 : Exp) extends Exp { 
    // Addition operation

    override def toString () : String =
        "EPlus(" + e1 + "," + e2 + ")"
	
    def eval () : Int = {
        val v1 = e1.eval()
        val v2 = e2.eval()
	return v1 + v2
    }
}


class ETimes (val e1 : Exp, val e2 : Exp) extends Exp { 
    // Multiplication operation

    override def toString () : String =
        "ETimes(" + e1 + "," + e2 + ")"
	
    def eval () : Int = {
        val v1 = e1.eval()
        val v2 = e2.eval()
	return v1 * v2
    }
}


class EIf (val ec : Exp, val et : Exp, val ee : Exp) extends Exp {
    // Conditional expression

    override def toString : String =
        "EIf(" + ec + "," + et + "," + ee +")"
	
    def eval () : Int = { 
        val ev = ec.eval()
	if (ev == 0) {
	    return ee.eval()
	} else {
	    return et.eval()
	}
    }
}


def eval_print (e : Exp) : Unit = {

    println(e + " -> " + e.eval())
}

//val e33 = new EInteger(33)
//val e66 = new EInteger(66)

//eval_print(e33)
//eval_print(e66)

//val sum = new EPlus(e33,e66)
//val prod = new ETimes(e33,sum)

//eval_print(sum)
//eval_print(prod)

//val zero = new EInteger(0)
//val one = new EInteger(1)
//val ifz = new EIf(new ETimes(zero,one),e33,e66)
//val ifnz = new EIf(one,e33,e66)

//eval_print(zero)
//eval_print(ifz)
//eval_print(ifnz)
