package no.officenet.example.rpm.web.lib

import net.liftweb._
import util.Helpers._
import http.SHtml
import no.officenet.example.rpm.support.infrastructure.i18n.GlobalTexts
import no.officenet.example.rpm.support.infrastructure.i18n.Localizer.L

trait JpaFormFields extends Validatable {
	self: ValidatableScreen =>

	trait JpaFormField[A <: AnyRef, T] {
		self: FormField[A, T] =>
		override def hasExternalFieldValidation: Boolean = {
			validator.hasFieldValidation(bean, fieldName)
		}

		override def isMandatory = validator.mandatory(bean, fieldName) || mandatory

		override protected def doExternalFieldValidation(bean: AnyRef, fieldName: String, valueToValidate: T, originalValue: K) {
			validator.registerFieldViolations(registerError(originalValue) _, bean, fieldName, valueToValidate, errorsMap, nextFuncName)
		}

		override def maxLengthOfFieldInChars(implicit m: Manifest[T]) = validator.getMaxLengthOfProperty[T](bean, fieldName)
	}

	object JpaTextField {
		def apply[A <: AnyRef, T](bean: A,
								  field: javax.persistence.metamodel.Attribute[A, T],
								  defaultValue: String,
								  assignmentCallback: T => Any)(implicit m: Manifest[T]) =
			new TextField[A, T](bean, defaultValue, assignmentCallback) with JpaFormField[A, T] {
				override val fieldName = field.getName
			}
	}

	object JpaTextAreaField {
		def apply[A <: AnyRef, T](bean: A,
								  field: javax.persistence.metamodel.Attribute[A, T],
								  defaultValue: String,
								  assignmentCallback: T => Any)(implicit m: Manifest[T]) = {
			new TextAreaField[A, T](bean, defaultValue, assignmentCallback) with JpaFormField[A, T] {
				override val fieldName = field.getName
			}
		}
	}

	object JpaPercentField {
		def apply[A <: AnyRef](bean: A,
							   field: javax.persistence.metamodel.Attribute[A, java.math.BigDecimal],
							   value: Option[java.math.BigDecimal],
							   assignmentCallback: java.math.BigDecimal => Any) = {
			new PercentField[A](bean, value, assignmentCallback) with JpaFormField[A, java.math.BigDecimal] {
				override val fieldName = field.getName
			}
		}
	}

	object JpaDecimalField {
		def apply[A <: AnyRef](bean: A,
							   field: javax.persistence.metamodel.Attribute[A, java.math.BigDecimal],
							   value: Option[java.math.BigDecimal],
							   assignmentCallback: java.math.BigDecimal => Any) = {
			new DecimalField[A](bean, value, assignmentCallback) with JpaFormField[A, java.math.BigDecimal] {
				override val fieldName = field.getName
			}
		}
	}

	object JpaNaturalNumberField {
		def apply[A <: AnyRef](bean: A,
							   field: javax.persistence.metamodel.Attribute[A, java.lang.Integer],
							   value: Option[java.lang.Integer],
							   assignmentCallback: java.lang.Integer => Any) = {
			new NaturalNumberField[A](bean, value, assignmentCallback) with JpaFormField[A, java.lang.Integer] {
				override val fieldName = field.getName
			}
		}
	}

	object JpaSelectField {
		def apply[A <: AnyRef, T](bean: A,
								  field: javax.persistence.metamodel.Attribute[A, T],
								  options: Seq[(T, List[SHtml.ElemAttr])],
								  defaultValue: T,
								  assignmentCallback: T => Any,
								  valueLabel: (T, Int) => String)(implicit m: Manifest[T]) = {
			new SelectField[A, T](bean, options, defaultValue, assignmentCallback, valueLabel) with JpaFormField[A, T] {
				override val fieldName = field.getName
			}
		}
	}

	object JpaSelectFieldWithUnselectedOption {
		def apply[A <: AnyRef, T](bean: A,
								  field: javax.persistence.metamodel.Attribute[A, T],
								  options: Seq[(T, List[SHtml.ElemAttr])],
								  defaultValue: T,
								  assignmentCallback: T => Any,
								  valueLabel: (T, Int) => String)(implicit m: Manifest[T]) = {
			val unSelectedOption = (null.asInstanceOf[T], List[SHtml.ElemAttr]("disabled" -> "disabled"))
			val allOptions = unSelectedOption +: options
			val valueLabelWithDefaultValue = (option: T, idx: Int) => if (idx == 0) L(GlobalTexts.select_noItemSelected) else valueLabel(option, idx)
			JpaSelectField(bean, field, allOptions, defaultValue, assignmentCallback, valueLabelWithDefaultValue)
		}
	}

	object JpaDateField {
		def apply[A <: AnyRef, T](bean: A, field: javax.persistence.metamodel.Attribute[A, T], defaultValue: String,
								  assignmentCallback: T => Any)(implicit m: Manifest[T]) = {
			new DateField[A, T](bean, defaultValue, assignmentCallback) with JpaFormField[A, T] {
				override val fieldName = field.getName
			}
		}
	}

}