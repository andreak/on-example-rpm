package no.officenet.example.rpm.support.infrastructure.scala.lang


object ControlHelpers {

	val boxedPredefImplicits = List("Byte2byte", "Short2short", "Character2char", "Integer2int", "Long2long", "Float2float",
									"Double2double", "Boolean2boolean")

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