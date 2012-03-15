package no.officenet.example.rpm.support.infrastructure.errorhandling

import net.sf.oval.ConstraintViolation

class RpmConstraintsViolatedException(_constraintViolations: Array[ConstraintViolation])
	extends RuntimeException(_constraintViolations(0).getMessage) with ApplicationException {
	/* the message of the first occurring constraint violation will be used, i.e. in logs where getMessage() is used*/

	def this(violations: java.util.List[net.sf.oval.ConstraintViolation]) {
		this(violations.toArray(new Array[ConstraintViolation](violations.size)))
	}

	def getConstraintViolations = _constraintViolations

}
