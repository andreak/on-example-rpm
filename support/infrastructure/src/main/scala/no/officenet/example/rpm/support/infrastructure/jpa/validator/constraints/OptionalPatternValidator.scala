package no.officenet.example.rpm.support.infrastructure.jpa.validator.constraints

import javax.validation.{ConstraintValidatorContext, ConstraintValidator}
import no.officenet.example.rpm.support.infrastructure.jpa.validation.OptionalPattern
import java.util.regex.PatternSyntaxException

class OptionalPatternValidator extends ConstraintValidator[OptionalPattern, Option[String]] {

	private var pattern: java.util.regex.Pattern = null

	def initialize(parameters: OptionalPattern) {
		val flags = parameters.flags();
		var intFlag = 0;
		for ( flag <- flags ) {
			intFlag = intFlag | flag.getValue;
		}

		try {
			pattern = java.util.regex.Pattern.compile( parameters.regexp(), intFlag );
		}
		catch {
			case e: PatternSyntaxException => throw new IllegalArgumentException( "Invalid regular expression.", e );
		}
	}

	def isValid(valueOption: Option[String], context: ConstraintValidatorContext): Boolean = {
		valueOption match {
			case Some(value) => pattern.matcher(value).matches()
			case None => true
		}
	}
}