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


class Coordinates (val x : Double, val y : Double) {
   /* Class for Cartesian coordinates */

   def xCoord () : Double = x

   def yCoord () : Double = y

   def isSame (c : Coordinates) : Boolean =
      throw new Exception("Not implemented")

   def isOrigin () : Boolean =
      throw new Exception("Not implemented")

   def translate (dx : Double, dy : Double) : Coordinates = 
      throw new Exception("Not implemented")

   def scale (s : Double) : Coordinates =
      throw new Exception("Not implemented")

   def rotate (angle: Double) : Coordinates = 
      throw new Exception("Not implemented")
}



/*   QUESTIONS 2 & 3 */


abstract class Exp {
    /*  Abstract base class for abstract representation  */
    
    def eval () : Int
    def readable () : String
}


class EInteger (val i:Int) extends Exp {
   /*  Integer literals  */
    
    override def toString () : String = 
        "EInteger(" + i.toString() + ")"

    def eval () : Int =
        i

    def readable () : String =
        throw new Exception("Not implemented")
}


class EPlus (val e1 : Exp, val e2 : Exp) extends Exp { 
    /*   Addition  */
    
    override def toString () : String =
        "EPlus(" + e1.toString() + ", " + e2.toString() + ")"
	
    def eval () : Int = {
        val v1 = e1.eval()
        val v2 = e2.eval()
	return v1 + v2
    }

    def readable () : String =
        throw new Exception("Not implemented")
}


class ETimes (val e1 : Exp, val e2 : Exp) extends Exp { 
    /*  Multiplication  */

    override def toString () : String =
        "ETimes(" + e1.toString() + ", " + e2.toString() + ")"
	
    def eval () : Int = {
        val v1 = e1.eval()
        val v2 = e2.eval()
	return v1 * v2
    }

    def readable () : String =
        throw new Exception("Not implemented")
}


class EIf (val ec : Exp, val et : Exp, val ee : Exp) extends Exp {
    /*  Conditional  */

    override def toString () : String =
        "EIf(" + ec.toString() + ", " + et.toString() + ", " + ee.toString() +")"
	
    def eval () : Int = { 
        val ev = ec.eval()
	if (ev == 0) {
	    return ee.eval()
	} else {
	    return et.eval()
	}
    }

    def readable () : String =
        throw new Exception("Not implemented")
}


class ENeg (val e1 : Exp) extends Exp {
    /* Negation */

    def eval () : Int =
        throw new Exception("Not implemented")

    def readable () : String =
        throw new Exception("Not implemented")
}


class EMax (val e1 : Exp, val e2 : Exp) extends Exp { 
    /*   Maximum  */

    def eval () : Int =
        throw new Exception("Not implemented")

    def readable () : String =
        throw new Exception("Not implemented")
}


class EWithin (val e1 : Exp, val e2 : Exp, val e3 : Exp) extends Exp {
    /*   Within  */

    def eval () : Int =
        throw new Exception("Not implemented")

    def readable () : String =
        throw new Exception("Not implemented")
}

