package no.officenet.example.rpm.support.infrastructure.errorhandling

import org.springframework.beans.factory.annotation.Configurable
import javax.annotation.Resource
import scala.collection.JavaConversions.iterableAsScalaIterable
import collection.mutable.{Buffer, HashMap}
import no.officenet.example.rpm.support.infrastructure.scala.lang.IdentityHashMap
import no.officenet.example.rpm.support.infrastructure.logging.Loggable
import net.sf.oval.internal.util.ReflectionUtils
import net.sf.oval.Check
import net.sf.oval.constraint._
import java.lang.reflect.{Method, Field}
import no.officenet.example.rpm.support.infrastructure.scala.lang.ControlHelpers.?
import no.officenet.example.rpm.support.infrastructure.validation.OvalValidator

private[errorhandling] object ValidationCache {
	val sizeMap = new HashMap[String, Option[Long]]
	val mandatoryMap = new HashMap[String, Boolean]
	val hasFieldChecksMap = new HashMap[String, Boolean]
}

trait Validator {

	def mandatory[A <: AnyRef](bean: A, fieldName: String): Boolean

	def getMaxLengthOfProperty(bean: AnyRef, fieldName: String): Option[Long]

	def registerFieldViolations[T](registerErrorFunc: (AnyRef, String, String, String,
		IdentityHashMap[AnyRef, HashMap[String, Buffer[FieldError]]], String) => Unit,
								   bean: AnyRef, fieldName: String, newValue: T,
								   errorsMap: IdentityHashMap[AnyRef, HashMap[String, Buffer[FieldError]]],
								   uniqueErrorId: String)

	def hasFieldValidation[A <: AnyRef](bean: A, fieldName: String): Boolean
}

@Configurable
object OvalScreenValidator extends Validator with Loggable {

	@Resource
	private val validator: OvalValidator = null

	override def mandatory[A <: AnyRef](bean: A, fieldName: String): Boolean = {
		val c = bean.getClass
		val s = c.getName + "." + fieldName
		val cached = ValidationCache.mandatoryMap.get(s)

		if (cached.isDefined) {
			return cached.get
		}

		val allChecks = getChecksForFieldAndMethod(c, fieldName)
		val isMandatory: Boolean = allChecks.find(_.isInstanceOf[NotNullCheck]).map(check => check.isActive(bean, "", validator)).getOrElse(false) //empty string as value is ok for NotNullCheck

		ValidationCache.mandatoryMap.put(s, isMandatory)
		isMandatory
	}

	override def getMaxLengthOfProperty(bean: AnyRef, fieldName: String): Option[Long] = {
		val c = bean.getClass
		val s = c.getName + "." + fieldName
		val cached = ValidationCache.sizeMap.get(s)

		if (cached.isDefined) {
			return cached.get
		}

		val allChecks = getChecksForFieldAndMethod(c, fieldName)

		val maxOpt = findMax(allChecks)

		ValidationCache.sizeMap.put(s, maxOpt)
		maxOpt

	}


	def hasFieldValidation[A <: AnyRef](bean: A, fieldName: String): Boolean = {
		val c = bean.getClass
		val s = c.getName + "." + fieldName
		val cached = ValidationCache.hasFieldChecksMap.get(s)
		if (cached.isDefined) {
			return cached.get
		}
		val allChecks = getChecksForFieldAndMethod(c, fieldName)
		val hasFieldValidation = allChecks.size > 0
		ValidationCache.hasFieldChecksMap.put(s, hasFieldValidation)
		hasFieldValidation
	}

	override def registerFieldViolations[T](registerErrorFunc: (AnyRef, String, String, String,
		IdentityHashMap[AnyRef, HashMap[String, Buffer[FieldError]]], String) => Unit,
											bean: AnyRef, fieldName: String, newValue: T,
											errorsMap: IdentityHashMap[AnyRef, HashMap[String, Buffer[FieldError]]],
											uniqueErrorId: String) {
		val fieldErrors = validateFieldValue(bean, fieldName, newValue)
		for (fieldError <- fieldErrors) {
			trace("Validation-violation for " + bean.getClass.getName + "(" + System.identityHashCode(bean) + ") field: " + fieldName + ": " + fieldError)
			registerErrorFunc(bean, fieldName, if (newValue != null) newValue.toString else null, fieldError.getMessage,
				errorsMap, uniqueErrorId)
		}
		registerBeanViolations(registerErrorFunc, bean, fieldName, errorsMap, uniqueErrorId)
	}

	private def findMax(checks: Array[Check]): Option[Long] = {

		checks.foreach(check => {
			check match {
				case maxCheck: MaxCheck =>
					// Number
					val isBigDecimal = check.getContext.getCompileTimeType == classOf[java.math.BigDecimal] ||
						check.getContext.getCompileTimeType == classOf[BigDecimal]
					//Need to adjust for scale when type is BigDecimal. Add 3 (1 for '.' and 2 for 2 decimals)
					val ajustForBigDecimal: Long = if (isBigDecimal) 3L else 0L
					val size = maxCheck.getMax.toLong
					val getMaxSize: Long = scala.math.ceil(scala.math.log10(size)).toLong
					val ajustForModZero: Long = if (size % 10 == 0) 1L else 0L

					val maxSize = getMaxSize + ajustForModZero + ajustForBigDecimal
					return Some(maxSize)
				case maxSizeCheck: MaxSizeCheck =>
					// Array or collection
					return Some(maxSizeCheck.getMax.toLong)
				case maxLengthCheck: MaxLengthCheck =>
					// String
					return Some(maxLengthCheck.getMax.toLong)
				case sizeCheck: SizeCheck =>
					// String
					return Some(sizeCheck.getMax.toLong)
				case lengthCheck: LengthCheck =>
					// String
					return Some(lengthCheck.getMax.toLong)
				case _ =>
			}

		})
		None
	}

	private def getterForField(field: Field): Option[Method] = {
		def isGetterForField(method: Method): Boolean = {
			// TODO M andreak 1/21/12: If field-name is "isPrivate", this will try to lookup isIsPrivate, which is wrong - fix it!
			method.getName == ("is" + (field.getName.charAt(0).toUpper + field.getName.substring(1))) ||
				method.getName == ("get" + (field.getName.charAt(0).toUpper + field.getName.substring(1)))
		}
		field.getDeclaringClass.getDeclaredMethods.find(isGetterForField(_))
	}

	private def validateFieldValue[A <: AnyRef](validatedObject: A, fieldName: String, valueToValidate: Any): java.util.List[net.sf.oval.ConstraintViolation] = {
		val field = ReflectionUtils.getFieldRecursive(validatedObject.getClass, fieldName)
		val violations = validator.validateFieldValue(validatedObject, field, valueToValidate)
		getterForField(field).foreach(getter => violations.addAll(validator.validateMethodReturnValue(validatedObject, getter, valueToValidate)))
		violations
	}

	private def getChecksForField(beanClass: Class[_], fieldName: String): Array[Check] = {
		val checks = validator.getChecks(ReflectionUtils.getFieldRecursive(beanClass, fieldName))
		if (checks == null) Array()
		else checks
	}

	private def getChecksForFieldAndMethod(clazz: Class[_], fieldName: String): Array[Check] = {
		val fieldChecks = getChecksForField(clazz, fieldName)
		val methodChecks = ?(ReflectionUtils.getFieldRecursive(clazz, fieldName)).flatMap(getterForField).
			flatMap(method => ?(validator.getChecks(method))).getOrElse(Array[Check]())

		val allChecks = fieldChecks ++ methodChecks
		allChecks
	}

	private def registerBeanViolations(registerErrorFunc: (AnyRef, String, String, String,
		IdentityHashMap[AnyRef, HashMap[String, Buffer[FieldError]]], String) => Unit,
									   bean: AnyRef, fieldName: String,
									   errorsMap: IdentityHashMap[AnyRef, HashMap[String, Buffer[FieldError]]],
									   uniqueErrorId: String) {
		/*
		  var extraErrors = validator.validate(bean, classOf[MethodValidationGroup]).toSet
		  extraErrors = extraErrors.filter(cv => cv.getPropertyPath.iterator().next().getName == fieldName)
		  trace("Extra-errors for field: " + fieldName + ": " + extraErrors)

		  for (fieldError <- extraErrors) {
			  val fieldValue = ReflectionUtils.getFieldValue(fieldName, bean)
			  registerError(bean, fieldName, if (fieldValue != null) fieldValue.toString else null, fieldError.getMessage,
				  errorsMap, uniqueErrorId)
		  }
  */
	}

}