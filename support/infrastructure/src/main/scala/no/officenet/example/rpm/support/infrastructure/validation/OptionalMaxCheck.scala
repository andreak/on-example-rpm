package no.officenet.example.rpm.support.infrastructure.validation

import net.sf.oval.Validator
import net.sf.oval.configuration.annotation.AbstractAnnotationCheck
import net.sf.oval.context.OValContext
import net.sf.oval.Validator.getCollectionFactory

class OptionalMaxCheck extends AbstractAnnotationCheck[OptionalMax] {


	var max: Double = 0

	override def configure(constraintAnnotation: OptionalMax) {
		super.configure(constraintAnnotation)
		setMax(constraintAnnotation)
	}

	override def createMessageVariables() = {
		val messageVariables = getCollectionFactory.createMap[String,  String](2)
		messageVariables.put("max", max.toString)
		messageVariables
	}

	override def isSatisfied(validatedObject: Any, valueToValidate: Any, context: OValContext , validator: Validator ): Boolean = {
		if (valueToValidate == null) throw new IllegalArgumentException("Option-types cannot be null, use Empty")
		valueToValidate match {
			case Some(x) => x match {
				// todo: Set format of messageVariable according to type of 'v'
				case v: Double => v <= max
				case v: Float => v <= max.floatValue()
				case v: Long => v <= max.longValue()
				case v: Int => v <= max.intValue()
				case v: java.lang.Number => v.doubleValue() <= max
				case _ => throw new IllegalArgumentException("Only valid for Option-types, got " + valueToValidate.getClass)
			}
			case None => true
		}
	}

	private def setMax(constraintAnnotation: OptionalMax) {
		max = constraintAnnotation.value()
		requireMessageVariablesRecreation()
	}
}
