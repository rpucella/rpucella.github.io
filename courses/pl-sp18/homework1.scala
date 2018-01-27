/************************************************************

HOMEWORK 1

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
 


/*   QUESTION 1  */


def gcd (n:Int,m:Int) : Int = {
   throw new Exception("Not implemented")
}



def isPrime (n:Int) : Boolean = { 
   throw new Exception("Not implemented")
}



class Coordinates (val x : Double, val y : Double) {

}



/*   QUESTIONS 2 & 3 */


abstract class Exp {

    /*  Abstract base class for the abstract representation  */
    
    def eval () : Int

}


class EInteger (val i:Int) extends Exp {

   /*  Integer literals  */
    
    override def toString () : String = 
        "EInteger(" + i + ")"

    def eval () : Int =
        i
}



class EPlus (val e1 : Exp, val e2 : Exp) extends Exp { 

    /*   Addition  */
    
    override def toString () : String =
        "EPlus(" + e1 + "," + e2 + ")"
	
    def eval () : Int = {
        val v1 = e1.eval()
        val v2 = e2.eval()
	return v1 + v2
    }

}


class ETimes (val e1 : Exp, val e2 : Exp) extends Exp { 

    /*  Multiplication  */

    override def toString () : String =
        "ETimes(" + e1 + "," + e2 + ")"
	
    def eval () : Int = {
        val v1 = e1.eval()
        val v2 = e2.eval()
	return v1 * v2
    }
}



class EIf (val ec : Exp, val et : Exp, val ee : Exp) extends Exp {

    /*  Conditional  */

    override def toString () : String =
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



class ENeg (val e1 : Exp) extends Exp {

    /* Negation */

    override def toString () : String = { 
        throw new Exception("Not implemented")
    }
	
    def eval () : Int = {
        throw new Exception("Not implemented")
    }
}



class EMax (val e1 : Exp, val e2 : Exp) extends Exp { 

    /*   Maximum  */

    override def toString () : String = { 
        throw new Exception("Not implemented")
    }
	
    def eval () : Int = {
        throw new Exception("Not implemented")
    }

}


class EWithin (val e1 : Exp, val e2 : Exp, val e3 : Exp) extends Exp {

    /*   Within  */

    override def toString () : String = { 
        throw new Exception("Not implemented")
    }
	
    def eval () : Int = {
        throw new Exception("Not implemented")
    }

}



class EMin (es : List[Exp]) extends Exp {
  
    /*   Minimum   */

    override def toString () : String = { 
        throw new Exception("Not implemented")
    }
	
    def eval () : Int = {
        throw new Exception("Not implemented")
    }

}
