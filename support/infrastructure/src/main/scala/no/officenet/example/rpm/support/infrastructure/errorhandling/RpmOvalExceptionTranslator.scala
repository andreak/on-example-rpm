package no.officenet.example.rpm.support.infrastructure.errorhandling

import net.sf.oval.exception.{ConstraintsViolatedException, OValException, ExceptionTranslator}

class RpmOvalExceptionTranslator extends ExceptionTranslator {

	def translateException(ovalException: OValException) = ovalException match {
		case e: ConstraintsViolatedException =>
			new RpmConstraintsViolatedException(e.getConstraintViolations)
		case _ => ovalException
	}
}
