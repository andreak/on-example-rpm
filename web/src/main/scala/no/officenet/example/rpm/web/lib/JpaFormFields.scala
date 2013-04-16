package no.officenet.example.rpm.web.lib

import net.liftweb._
import util.Helpers._
import http.SHtml
import no.officenet.example.rpm.support.infrastructure.i18n.GlobalTexts
import no.officenet.example.rpm.support.infrastructure.i18n.Localizer.L

trait JpaFormFields extends Validatable {
	self: ValidatableScreen =>

	trait JpaFormField[A <: AnyRef, T] {
		self: FormField[A, T]  =>
		override def hasExternalFieldValidation: Boolean = {
			validator.hasFieldValidation(bean, fieldName)
		}

		override def isMandatory = validator.mandatory(bean, fieldName) || mandatory

		override protected def doExternalFieldValidation(bean: A, fieldName: String, valueToValidate: Option[T], originalValue: Option[K]) {
			validator.registerFieldViolations[A, T](registerError(originalValue) _, bean, fieldName, valueToValidate, errorsMap, nextFuncName)
		}

		override def maxLengthOfFieldInChars(implicit m: Manifest[T]) = validator.getMaxLengthOfProperty[A, T](bean, fieldName)
	}

	object JpaTextField {
		def apply[A <: AnyRef, T](bean: A,
								  field: javax.persistence.metamodel.Attribute[A, T],
								  defaultValue: Option[String],
								  assignmentCallback: Option[T] => Any)(implicit m: Manifest[T]) =
			new TextField[A, T](bean, defaultValue, assignmentCallback) with JpaFormField[A, T] {
				override val fieldName = field.getName
			}
	}

	object JpaTextAreaField {
		def apply[A <: AnyRef, T](bean: A,
								  field: javax.persistence.metamodel.Attribute[A, T],
								  defaultValue: Option[String],
								  assignmentCallback: Option[T] => Any)(implicit m: Manifest[T]) = {
			new TextAreaField[A, T](bean, defaultValue, assignmentCallback) with JpaFormField[A, T] {
				override val fieldName = field.getName
			}
		}
	}

	object JpaPercentField {
		def apply[A <: AnyRef](bean: A,
							   field: javax.persistence.metamodel.Attribute[A, BigDecimal],
							   value: Option[BigDecimal],
							   assignmentCallback: Option[BigDecimal] => Any) = {
			new PercentField[A](bean, value, assignmentCallback) with JpaFormField[A, BigDecimal] {
				override val fieldName = field.getName
			}
		}
	}

	object JpaDecimalField {
		def apply[A <: AnyRef](bean: A,
							   field: javax.persistence.metamodel.Attribute[A, BigDecimal],
							   value: Option[BigDecimal],
							   assignmentCallback: Option[BigDecimal] => Any) = {
			new DecimalField[A](bean, value, assignmentCallback) with JpaFormField[A, BigDecimal] {
				override val fieldName = field.getName
			}
		}
	}

	object JpaNaturalNumberField {
		def apply[A <: AnyRef](bean: A,
							   field: javax.persistence.metamodel.Attribute[A, java.lang.Integer],
							   value: Option[java.lang.Integer],
							   assignmentCallback: Option[java.lang.Integer] => Any) = {
			new NaturalNumberField[A](bean, value, assignmentCallback) with JpaFormField[A, java.lang.Integer] {
				override val fieldName = field.getName
			}
		}
	}

	object JpaSelectField {
		def apply[A <: AnyRef, T](bean: A,
								  field: javax.persistence.metamodel.Attribute[A, T],
								  options: Seq[(T, List[SHtml.ElemAttr])],
								  defaultValue: Option[T],
								  assignmentCallback: T => Any,
								  valueLabel: (T, Int) => String)(implicit m: Manifest[T]) = {
			implicit val krafs = 0
			new SelectField[A, T](bean, options.map(t => Some(t._1) -> t._2), defaultValue, (o: Option[T]) => o.map(v => assignmentCallback(v)), valueLabel) with JpaFormField[A, T] {
				override val fieldName = field.getName
			}
		}
	}

	object JpaSelectFieldWithUnselectedOption {
		def apply[A <: AnyRef, T](bean: A,
								  field: javax.persistence.metamodel.Attribute[A, T],
								  options: Seq[(T, List[SHtml.ElemAttr])],
								  defaultValue: Option[T],
								  assignmentCallback: Option[T] => Any,
								  valueLabel: (T, Int) => String, notSelectedText: String)(implicit m: Manifest[T]) = {
			val valueLabelWithDefaultValue = (option: Option[T], idx: Int) => option.map(value => valueLabel(value, idx)).getOrElse(notSelectedText)
			new SelectField[A, T](bean, options.map(t => Some(t._1) -> t._2), defaultValue, assignmentCallback,
				valueLabelWithDefaultValue, includeUnselectedOption = true) with JpaFormField[A, T] {
				override val fieldName = field.getName
			}
		}
	}

	object JpaDateField {
		def apply[A <: AnyRef, T](bean: A, field: javax.persistence.metamodel.Attribute[A, T], defaultValue: Option[String],
								  assignmentCallback: Option[T] => Any)(implicit m: Manifest[T]) = {
			new DateField[A, T](bean, defaultValue, assignmentCallback) with JpaFormField[A, T] {
				override val fieldName = field.getName
			}
		}
	}

	object JpaTimeField {
		def apply[A <: AnyRef, T](bean: A, field: javax.persistence.metamodel.Attribute[A, T], defaultValue: Option[String],
								  assignmentCallback: Option[T] => Any)(implicit m: Manifest[T]) = {
			new TimeField[A, T](bean, defaultValue, assignmentCallback) with JpaFormField[A, T] {
				override val fieldName = field.getName
			}
		}
	}

	object JpaSelectGroupField {
		def apply[A <: AnyRef, T](bean: A,
								  field: javax.persistence.metamodel.Attribute[A, T],
								  options: Seq[OptionGroupModel[T]],
								  defaultValue: Option[T],
								  assignmentCallback: T => Any,
								  valueLabel: T => String)(implicit m: Manifest[T]) = {
			new SelectGroupField[A, T](bean, options, defaultValue, (o: Option[T]) => o.map(v => assignmentCallback(v)), valueLabel) with StandardFormField[A, T] {
				override val fieldName = field.getName
			}
		}
	}

	object JpaSelectGroupFieldWithUnselectedOption {
		def apply[A <: AnyRef, T](bean: A,
								  field: javax.persistence.metamodel.Attribute[A, T],
								  options: Seq[OptionGroupModel[T]],
								  defaultValue: Option[T],
								  assignmentCallback: Option[T] => Any,
								  valueLabel: T => String, notSelectedText: String)(implicit m: Manifest[T]) = {
			val valueLabelWithDefaultValue = (option: Option[T]) => option.map(value => valueLabel(value)).getOrElse(notSelectedText)
			new SelectGroupField[A, T](bean, options, defaultValue, assignmentCallback, valueLabelWithDefaultValue, true) with StandardFormField[A, T] {
				override val fieldName = field.getName
			}
		}
	}

}