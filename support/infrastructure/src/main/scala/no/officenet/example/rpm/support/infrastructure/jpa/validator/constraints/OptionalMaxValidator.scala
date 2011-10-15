package no.officenet.example.rpm.support.infrastructure.jpa.validator.constraints

import javax.validation.{ConstraintValidatorContext, ConstraintValidator}
import no.officenet.example.rpm.support.infrastructure.jpa.validation.OptionalMax

class OptionalMaxValidator extends ConstraintValidator[OptionalMax, Option[_ <: java.lang.Number]] {

	private var maxValue: Long = 0

	def initialize(maxValue: OptionalMax) {
		this.maxValue = maxValue.value()
	}

	def isValid(valueOption: Option[_ <: java.lang.Number], context: ConstraintValidatorContext): Boolean = {
		valueOption match {
			case Some(value) => value match {
				case v:java.math.BigDecimal => v.compareTo(java.math.BigDecimal.valueOf(maxValue)) != -1
				case v:java.math.BigInteger => v.compareTo(java.math.BigInteger.valueOf(maxValue)) != -1
				case v: java.lang.Long => v <= maxValue
				case v => throw new IllegalArgumentException("Unhandled Number-type: " + v)
			}
			case None => true
		}
	}
}