package no.officenet.example.rpm.support.infrastructure.scala.lang


object ControlHelpers {

	val boxedPredefImplicits = List("Byte2byte", "Short2short", "Character2char", "Integer2int", "Long2long", "Float2float",
									"Double2double", "Boolean2boolean")

	/**
	 * val result = ?(company.employee.address.city)
	 */
	def ?[A](block: => A): Option[A] = {
		try {
			block match {
				case null => None
				case found => Some(found)
			}
		} catch {
			// checks to see if the 3rd to last method called in the stack, is the ?() function,
			// which means the null pointer exception was actually due to a null object,
			// otherwise the ?() function would be further down the stack.
			case e: NullPointerException if e.getStackTrace()(2).getMethodName == "$qmark" => None
			case e: NullPointerException if boxedPredefImplicits.contains(e.getStackTrace()(0).getMethodName) => None
			// for any other NullPointerException, or otherwise, re-throw the exception.
			case e => throw e
		}
	}

	/**
	 * Returns the result of the evaluated <code>block</code>
	 * unless a NullPointerException is thrown one the top level in the call chain, that would return false
	 * A NPE further down the stack would be rethrown
	 * NB! Does not handle implicit conversions in <code>block</code>
	 */
	def isTrue[T](block: => Boolean): Boolean = {
		try {
			if (block) {
				return true
			}
		}
		catch {
			case e: NullPointerException => {
				if (e.getStackTrace()(1).getMethodName == "isTrue" ||
					boxedPredefImplicits.contains(e.getStackTrace()(0).getMethodName)) return false
				throw e
			}
			case e => throw e
		}
		false
	}
	
}