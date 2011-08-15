package no.officenet.example.rpm.web.lib

import net.liftweb._
import common.{Empty, Full, Box}
import http.js.JE.{Call}
import http.js.JsCmd
import http.js.JsCmds._
import http.S.AFuncHolder
import http.S.LFuncHolder
import http.SHtml.ChoiceItem
import http.{S, SHtml}
import util.Helpers._
import collection.mutable.{ArrayBuffer, HashMap, Buffer}
import javax.validation.Validator
import xml._
import scala.collection.JavaConversions.iterableAsScalaIterable
import javax.validation.constraints.{Max, Size}
import no.officenet.example.rpm.support.infrastructure.logging.Loggable
import no.officenet.example.rpm.support.infrastructure.scala.lang.IdentityHashMap
import javax.annotation.Resource
import no.officenet.example.rpm.support.infrastructure.jpa.validation.MethodValidationGroup
import no.officenet.example.rpm.support.infrastructure.jpa.util.ReflectionUtils
import no.officenet.example.rpm.support.domain.util.GlobalTexts
import org.joda.time.DateTime

case class FieldError(fieldName: String, errorValue: String, errorId: String, errorMessage: String)

trait ValidatableScreen extends Loggable with Localizable {

	// Needed for pattern-matching as one cannot use classOf[SomeType] as an extractor
	val stringCls = classOf[String]
	val intCls = classOf[java.lang.Integer]
	val longCls = classOf[java.lang.Long]
	val floatCls = classOf[java.lang.Float]
	val doubleCls = classOf[java.lang.Double]
	val bigDcmlCls = classOf[java.math.BigDecimal]
	val dateCls = classOf[java.util.Date]
	val dateTimeCls = classOf[DateTime]

	private def convert[T](value: String, klass: Class[T]): T = klass match {
		case `stringCls` => value.asInstanceOf[T]
		case `intCls` => value.toInt.asInstanceOf[T]
		case `longCls` => value.toLong.asInstanceOf[T]
		case `floatCls` => value.toFloat.asInstanceOf[T]
		case `doubleCls` => value.toDouble.asInstanceOf[T]
		case `bigDcmlCls` => new java.math.BigDecimal(value).asInstanceOf[T]
		case `dateCls` => getDateFromString(L(GlobalTexts.dateformat_fullDate), value).asInstanceOf[T]
		case `dateTimeCls` => getDateTimeFromString(L(GlobalTexts.dateformat_fullDate), value).asInstanceOf[T]
		case _ => throw new IllegalArgumentException("Don't know how to convert value " + value + " of type " + klass.getName)
	}

	val errorsMap = new IdentityHashMap[AnyRef, HashMap[String, Buffer[FieldError]]]()

	@Resource
	val validator: Validator = null

	def clearErrors = {
		S.formGroup(-1)(SHtml.hidden(() =>
            errorsMap.clear()
		))
	}

	def labelDateInput[T <: AnyRef](label:String, bean: AnyRef, field: javax.persistence.metamodel.Attribute[_, T], value: String, func: T => Any,
					   isMandatory:Boolean, attrs: SHtml.ElemAttr*)(implicit m: Manifest[T]): NodeSeq = {
		val r = getTextInputElement(bean, field.getName, value, func, m, attrs:_*)
		val inputId: String = r._1
		val containerId: String = r._2
		val fieldErrors: Buffer[FieldError] = r._3
		val textElement: NodeSeq = r._4
		val errorSeq: NodeSeq = r._5

		<td><label for={inputId}>{label}</label></td> ++
		renderTextInputContainer(containerId, textElement, isMandatory, errorSeq, fieldErrors)
	}

	def dateInput[T <: AnyRef](bean: AnyRef, field: javax.persistence.metamodel.Attribute[_, T], value: String, func: T => Any,
					   isMandatory:Boolean, attrs: SHtml.ElemAttr*)(implicit m: Manifest[T]): NodeSeq = {
		val r = getTextInputElement(bean, field.getName, value, func, m, attrs:_*)
		val containerId: String = r._2
		val fieldErrors: Buffer[FieldError] = r._3
		val textElement: NodeSeq = r._4
		val errorSeq: NodeSeq = r._5

		renderInlineInputContainer(fieldErrors, containerId, textElement, isMandatory, errorSeq)
	}

	def labelTextInput[T <: AnyRef](label: String, bean: AnyRef, field: javax.persistence.metamodel.Attribute[_, T], value: String,
									func: T => Any, isMandatory: Boolean, attrs: SHtml.ElemAttr*)
								   (implicit m: Manifest[T]): NodeSeq = {
		labelTextInput[T](label, bean, field.getName, value, func, isMandatory, attrs: _*)(m)
	}

	def labelTextInput[T <: AnyRef](label:String, bean: AnyRef, fieldName: String, value: String, func: T => Any,
					   isMandatory:Boolean, attrs: SHtml.ElemAttr*)(implicit m: Manifest[T]): NodeSeq = {
		val r = getTextInputElement(bean, fieldName, value, func, m, attrs:_*)
		val inputId: String = r._1
		val containerId: String = r._2
		val fieldErrors: Buffer[FieldError] = r._3
		val textElement: NodeSeq = r._4
		val errorSeq: NodeSeq = r._5

		<td><label for={inputId}>{label}</label></td> ++
		renderTextInputContainer(containerId, textElement, isMandatory, errorSeq, fieldErrors)
	}

	def textInput[T <: AnyRef](bean: AnyRef, field: javax.persistence.metamodel.Attribute[_, T], value: String, func: T => Any,
							   isMandatory:Boolean, attrs: SHtml.ElemAttr*)(implicit m: Manifest[T]): NodeSeq = {
		textInput[T](bean, field.getName, value, func, isMandatory, attrs:_*)(m)
	}

	def textInput[T <: AnyRef](bean: AnyRef, fieldName: String, value: String, func: T => Any,
					isMandatory:Boolean, attrs: SHtml.ElemAttr*)(implicit m: Manifest[T]): NodeSeq = {
		val r = getTextInputElement(bean, fieldName, value, func, m, attrs:_*)
		val containerId: String = r._2
		val fieldErrors: Buffer[FieldError] = r._3
		val textElement: NodeSeq = r._4
		val errorSeq: NodeSeq = r._5

		renderInlineInputContainer(fieldErrors, containerId, textElement, isMandatory, errorSeq)
	}

	def labelTextAreaInput(label: String, bean: AnyRef, field: javax.persistence.metamodel.Attribute[_, String], value: String,
									func: String => Any, isMandatory: Boolean, attrs: SHtml.ElemAttr*): NodeSeq = {
		labelTextAreaInput(label, bean, field.getName, value, func, isMandatory, attrs: _*)
	}

	def labelTextAreaInput(label:String, bean: AnyRef, fieldName: String, value: String, func: String => Any,
					   isMandatory:Boolean, attrs: SHtml.ElemAttr*): NodeSeq = {
		val r = getTextAreaInputElement(bean, fieldName, value, func, attrs:_*)
		val inputId: String = r._1
		val containerId: String = r._2
		val fieldErrors: Buffer[FieldError] = r._3
		val textElement: NodeSeq = r._4
		val errorSeq: NodeSeq = r._5

		<td><label for={inputId}>{label}</label></td> ++
		renderTextInputContainer(containerId, textElement, isMandatory, errorSeq, fieldErrors)
	}

	def textAreaInput(bean: AnyRef, field: javax.persistence.metamodel.Attribute[_, String], value: String, func: String => Any,
					  isMandatory:Boolean, attrs: SHtml.ElemAttr*): NodeSeq = {
		textAreaInput(bean, field.getName, value, func, isMandatory, attrs:_*)
	}

	def textAreaInput(bean: AnyRef, fieldName: String, value: String, func: String => Any,
					  isMandatory:Boolean, attrs: SHtml.ElemAttr*): NodeSeq = {
		val r = getTextAreaInputElement(bean, fieldName, value, func, attrs:_*)
		val containerId: String = r._2
		val fieldErrors: Buffer[FieldError] = r._3
		val textElement: NodeSeq = r._4
		val errorSeq: NodeSeq = r._5

		renderInlineInputContainer(fieldErrors, containerId, textElement, isMandatory, errorSeq)
	}

	/**
	 * For use with manual (non-JPA) validation by passing a validationFunc: String => Boolean. If it evaluates to false a field-error containing
	 * the errorMessage: (String) => String is created and shown
	 */
	def labelTextInput(label:String, bean: AnyRef, fieldName: String, value: String, func: String => Any,
					   validationFunc: String => Boolean, errorMessage: (String) => String, isMandatory:Boolean,attrs: SHtml.ElemAttr*): NodeSeq = {
		val inputId = nextFuncName
		val containerId = nextFuncName
		val fieldErrors = getFieldErrors(bean, fieldName)
		val errorSeq = getErrorsSeq(fieldErrors)
		val textElement = addErrorClass(fieldErrors, {
			SHtml.text(tryo{fieldErrors(0).errorValue}.getOrElse(nullSafeString(value)), (s) => {
				if (!validationFunc(s)) {
					registerError(bean, fieldName, s, errorMessage(s))
				} else {
					func(s)
				}
			}, attrs: _*) % ("id" -> inputId) % ("onblur" -> SHtml.onEvent((s) => {
				cleanErrorsForProperty(bean, fieldName) // Important to clear errors for this field in case previous action was "submit" on form
				if (!validationFunc(s)) {
					registerError(bean, fieldName, s, errorMessage(s))
				}
				val fieldErrors = getFieldErrors(bean, fieldName)
				val errorSeq: NodeSeq = getErrorsSeq(fieldErrors)
				cleanErrorsForProperty(bean, fieldName)
				if (fieldErrors.isEmpty) {
					Call("Rolf.removeFieldError", containerId, inputId)
				} else {
					Call("Rolf.attachFieldError", containerId, inputId, errorSeq.toString())
				}
			})._2.toJsCmd)
		})
		<td><label for={inputId}>{label}</label></td> ++
		renderTextInputContainer(containerId, textElement, isMandatory, errorSeq, fieldErrors)
	}

	def labelSelect[T <: AnyRef](label: String, bean: AnyRef, field: javax.persistence.metamodel.Attribute[_, T], options: Seq[T],
								 default: T, func: (T) => Any, valueLabel: (T) => String, isMandatory: Boolean, attrs: SHtml.ElemAttr*)
								(implicit m: Manifest[T]): NodeSeq = {
		labelSelect[T](label, bean, field.getName, options, default, func, valueLabel, isMandatory, attrs: _*)(m)
	}

	def labelSelect[T <: AnyRef](label: String, bean: AnyRef, fieldName: String, options: Seq[T], default: T, func: (T) => Any,
								 valueLabel: (T) => String, isMandatory: Boolean, attrs: SHtml.ElemAttr*)(implicit m: Manifest[T]): NodeSeq = {
		val allOptions:Seq[(T, String)] = (null.asInstanceOf[T] , L(GlobalTexts.select_noItemSelected)) :: options.map(t => (t, valueLabel(t))).toList
		val inputId = nextFuncName
		val containerId = nextFuncName
		val fieldErrors = getFieldErrors(bean, fieldName)
		val errorSeq = getErrorsSeq(fieldErrors)

		val secure = allOptions.map {case (obj, txt) => (obj, randomString(20), txt)}

		val textElement = addErrorClass(fieldErrors, {
			val (nonces, defaultNonce, secureOnSubmit) =
				secureOptions(secure, Full(default), (selectedItem:T) => {
					func(selectedItem)
					registerPropertyViolations(bean, fieldName)
					registerBeanViolations(bean, fieldName)
				})

			SHtml.select_*(nonces, defaultNonce, secureOnSubmit, attrs: _*) % ("id" -> inputId) %
			("onchange" -> onChangeForSelect(secure, bean, fieldName, func, inputId, containerId, m.erasure.asInstanceOf[Class[T]]))
		})
		<td><label for={inputId}>{label}</label></td> ++
		renderTextInputContainer(containerId, textElement, isMandatory, errorSeq, fieldErrors)
	}

	def labelAjaxSelect[T](label:String, bean: AnyRef, fieldName: String, options:Seq[T], default:T, onSelect: (T)=> JsCmd,
						   valueLabel: (T) => String, isMandatory:Boolean, attrs: SHtml.ElemAttr*):NodeSeq = {
		val allOptions:Seq[(T, String)] = (null.asInstanceOf[T] , L(GlobalTexts.select_noItemSelected)) :: options.map(t => (t, valueLabel(t))).toList
		val inputId = nextFuncName
		val containerId = nextFuncName
		val fieldErrors = getFieldErrors(bean, fieldName)
		val errorSeq = getErrorsSeq(fieldErrors)

		val textElement = SHtml.ajaxSelectObj(allOptions, Full(default), (selectedItem:T) => {
			val retVal = onSelect(selectedItem)
			cleanErrorsForProperty(bean, fieldName)
			registerPropertyViolations(bean, fieldName) // This also registers errors
			registerBeanViolations(bean, fieldName)
			val fieldErrors = getFieldErrors(bean, fieldName)
			val errorSeq: NodeSeq = getErrorsSeq(fieldErrors)
			cleanErrorsForProperty(bean, fieldName)
			if (fieldErrors.isEmpty) {
				Call("Rolf.removeFieldError", containerId, inputId) &
				retVal
			} else {
				Call("Rolf.attachFieldError", containerId, inputId, errorSeq.toString())
			}
		}
											  , attrs: _*) % ("id" -> inputId)

		<td><label for={inputId}>{label}</label></td> ++
		renderTextInputContainer(containerId, textElement, isMandatory, errorSeq, fieldErrors)
	}

	final case class StrFuncElemAttr[T](name: String, value: (T) => String) extends SHtml.ElemAttr {
		/**
		 * Apply the attribute to the element
		 */
		def apply(in: Elem): Elem = in % (name -> ((s:T) => value(s)))
		def apply(in: T): SHtml.ElemAttr = (name -> value(in))
	}

	def ritchRadioElem[T](opts: Seq[T], deflt: Box[T], attrs: SHtml.ElemAttr*)
				   (onSubmit: Box[T] => Any): SHtml.ChoiceHolder[T] = {

		final class ApplicableElem(in: Elem) {
			def %(attr: SHtml.ElemAttr): Elem = attr.apply(in)
		}

		implicit def elemToApplicable(e: Elem): ApplicableElem = new ApplicableElem(e)

		def checked(in: Boolean) = if (in) new UnprefixedAttribute("checked", "checked", Null) else Null

		val possible = opts.map(v => nextFuncName -> v).toList

		val hiddenId = nextFuncName

		S.fmapFunc(LFuncHolder(lst => lst.filter(_ != hiddenId) match {
			case Nil => onSubmit(Empty)
			case x :: _ => onSubmit(possible.filter(_._1 == x).
										headOption.map(_._2))
		})) {
				name => {
					val items = possible.zipWithIndex.map {case ((id, value), idx) => {
						val radio =
							attrs.foldLeft(<input type="radio"
												  name={name} value={id}/>){(b, a) =>
								b % (if (a.isInstanceOf[StrFuncElemAttr[T]]) {
									a.asInstanceOf[StrFuncElemAttr[T]].apply(value)
								} else {
									a
								})} %
							checked(deflt.filter(_ == value).isDefined)

						val elem = if (idx == 0) {
							radio ++ <input type="hidden" value={hiddenId} name={name}/>
						} else {
							radio
						}

						ChoiceItem(value, elem)
					}}

					SHtml.ChoiceHolder(items)
				}
			}
	}

	def getFieldErrors(key: AnyRef, property: String): Buffer[FieldError] = {
		val errorsForProperty = errorsMap.get(key) match {
			case Some(map) =>
				map.get(property) match {
					case Some(fieldErrors) =>
						trace("errors for element: " + fieldErrors + " " + errorsMap)
						fieldErrors
					case _ => Buffer.empty[FieldError]
				}
			case _ => Buffer.empty[FieldError]
		}
		errorsForProperty
	}

	///////////////////////
	// private methods

	private def secureOptions[T](secure: Seq[(T, String, String)], default: Box[T],
								 onSubmit: T => Any): (Seq[(String, String)], Box[String], AFuncHolder) = {
		val defaultNonce = default.flatMap(d => secure.find(_._1 == d).map(_._2))
		val nonces = secure.map {case (obj, nonce, txt) => (nonce, txt)}
		def process(nonce: String) {
			secure.find(_._2 == nonce).map(x => onSubmit(x._1))
		}
		(nonces, defaultNonce, S.SFuncHolder(process))
	}

	private def registerFieldViolations(bean: AnyRef, fieldName: String, newValue: AnyRef) {
		val fieldErrors = validator.validateValue(bean.getClass, fieldName, newValue)
		for (fieldError <- fieldErrors) {
			trace("Validation-violation for "+bean.getClass.getName+ "("+System.identityHashCode(bean)+") field: "+fieldName+": "+fieldError)
			registerError(bean, fieldName, if (newValue != null) newValue.toString else null, fieldError.getMessage)
		}
	}

	private def registerPropertyViolations(bean: AnyRef, fieldName: String) {
		val fieldErrors = validator.validateProperty(bean, fieldName)
		for (fieldError <- fieldErrors) {
			trace("Validation-violation for "+bean.getClass.getName+ "("+System.identityHashCode(bean)+") field: "+fieldName+": "+fieldError)
			registerError(bean, fieldName, null, fieldError.getMessage)
		}
	}

	private def registerBeanViolations(bean: AnyRef, fieldName: String) {
		var extraErrors = validator.validate(bean, classOf[MethodValidationGroup]).toSet
		extraErrors = extraErrors.filter(cv => cv.getPropertyPath.iterator().next().getName == fieldName)
		trace("Extra-errors for field: " + fieldName + ": " + extraErrors)

		for (fieldError <- extraErrors) {
			trace("Validation-violation for field: "+fieldName+": "+fieldError)
			val fieldValue = ReflectionUtils.getFieldValue(fieldName, bean)
			registerError(bean, fieldName, if (fieldValue != null) fieldValue.toString else null, fieldError.getMessage)
		}
	}

	private def addErrorClass(fieldErrors: Buffer[FieldError], block: => NodeSeq) = {
		("input [class+]" #> Full("value_error").filter(s => !fieldErrors.isEmpty) &
		 "textarea [class+]" #> Full("value_error").filter(s => !fieldErrors.isEmpty) &
		 "select [class+]" #> Full("value_error").filter(s => !fieldErrors.isEmpty))(block)
	}

	private def registerError(key: AnyRef, property: String, input: String, errorMessage: String) {
		val errorsForKey = errorsMap.getOrElseUpdate(key, new HashMap[String, Buffer[FieldError]]())
		val errorsForField = errorsForKey.getOrElseUpdate(property, new ArrayBuffer[FieldError]())
		if(errorsForField.exists(_.errorMessage == errorMessage)) return
		val fieldError = if (errorsForField.isEmpty) {
			FieldError(property, input, nextFuncName, errorMessage)
		} else {
			FieldError(property, input, errorsForField(0).errorId, errorMessage)
		}
		errorsForField += fieldError
		S.error(fieldError.errorId, errorMessage)
//		trace("Registered errors so far: " + errorsMap)
	}

	private def cleanErrorsForProperty(key: AnyRef, property: String) {
		errorsMap.get(key).map(_ -= property).map(m => if (m.isEmpty) errorsMap -= key)
	}

	private def getErrorsSeq(fieldErrors: Buffer[FieldError]) = {
		if (!fieldErrors.isEmpty) {
			<div class="errorContainer">{
				fieldErrors.map{fieldError =>
					trace("Displaying error for fieldName '" + fieldError.fieldName + "' error-id: " + fieldError.errorId + " " +
						  "input-string: " + fieldError.errorValue)
					<div>{fieldError.errorMessage}</div>
							   }
				}</div>
		} else {
			NodeSeq.Empty
		}
	}

	private def mandatoryIcon(isMandatory:Boolean) = {
		if(isMandatory)
			<div class="mandatory">*</div>
		else{
			NodeSeq.Empty
		}
	}

	private def validateInput[T <: AnyRef](s: String, bean: AnyRef, fieldName: String, func: (T) => Any, klass: Class[T]) {
		val newValue = if (s == null || s.trim().isEmpty) null else s.trim()
		try {
			val convertedValue: T = if (newValue == null) null.asInstanceOf[T]
			else convert(newValue, klass)
			func(convertedValue)
			trace("dateformat_fullDate: " + L(GlobalTexts.dateformat_fullDate))
			trace("S.locale: " + S.locale)
			trace("Validating field: " + fieldName + " with value '" + convertedValue + "'")
			registerFieldViolations(bean, fieldName, convertedValue)
			registerBeanViolations(bean, fieldName)
		} catch {
			case e: javax.validation.ValidationException => throw e
			case e: NumberFormatException =>
				registerError(bean, fieldName, if (newValue != null) newValue.toString else null,
							  L(GlobalTexts.validation_notANumber_number_text, s))
			case e: InvalidDateException =>
				registerError(bean, fieldName, if (newValue != null) newValue.toString else null,
							  L(GlobalTexts.validation_invalidDate_text, s))
		}
	}

	private def renderTextInputContainer(containerId: String, textElement: NodeSeq, isMandatory: Boolean, errorSeq: NodeSeq,
								 fieldErrors: Buffer[FieldError], extraInfo: NodeSeq = NodeSeq.Empty): Elem = {
		("td [class+]" #> Full("errorContainer").filter(s => !fieldErrors.isEmpty))
		(<td id={containerId}>
			 {textElement ++ mandatoryIcon(isMandatory) ++ errorSeq ++ extraInfo}
		 </td>)
	}

	private def onBlurForTextInput[T <: AnyRef](bean: AnyRef, fieldName: String, func: (T) => Any, inputId: String, containerId: String, klass: Class[T]) = {
		SHtml.onEvent((s) => {
			cleanErrorsForProperty(bean, fieldName) // Important to clear errors for this field in case previous action was "submit" on form
			validateInput(s, bean, fieldName, func, klass)
			val fieldErrors = getFieldErrors(bean, fieldName)
			val errorSeq: NodeSeq = getErrorsSeq(fieldErrors)
			cleanErrorsForProperty(bean, fieldName)
			if (fieldErrors.isEmpty) {
				Call("Rolf.removeFieldError", containerId, inputId)
			} else {
				Call("Rolf.attachFieldError", containerId, inputId, errorSeq.toString())
			}
		})._2.toJsCmd
	}

	private def onChangeForSelect[T <: AnyRef](secure: Seq[(T, String, String)], bean: AnyRef, fieldName: String, func: (T) => Any,
											   inputId: String, containerId: String, klass: Class[T]) = {
		SHtml.onEvent((s) => {
			cleanErrorsForProperty(bean, fieldName) // Important to clear errors for this field in case previous action was "submit" on form
			trace("onChangeForSelect: " + s)
			secure.find(_._2 == s).map{x =>
				val element = x._1
				trace("onChangeForSelect.element: " + element)
				func(element)
				registerPropertyViolations(bean, fieldName)
				registerBeanViolations(bean, fieldName)
				val fieldErrors = getFieldErrors(bean, fieldName)
				val errorSeq: NodeSeq = getErrorsSeq(fieldErrors)
				cleanErrorsForProperty(bean, fieldName)
				if (fieldErrors.isEmpty) {
					Call("Rolf.removeFieldError", containerId, inputId) & Noop
				} else {
					Call("Rolf.attachFieldError", containerId, inputId, errorSeq.toString()) & Noop
				}
									  }.getOrElse(Noop)
		})._2.toJsCmd
	}

	private def renderInlineInputContainer(fieldErrors: Buffer[FieldError], containerId: String, textElement: NodeSeq,
								   isMandatory: Boolean, errorSeq: NodeSeq, extraInfo: NodeSeq = NodeSeq.Empty): Elem = {
		(".inputContainer [class+]" #> Full("errorContainer").filter(s => !fieldErrors.isEmpty))
		(<div style="display: inline-block" id={containerId} class="inputContainer">
			{textElement ++ mandatoryIcon(isMandatory) ++ errorSeq}
		</div>)
	}

	private val sizeMap = new HashMap[String, Option[Long]]

	private def getMaxLengthOfProperty(bean: AnyRef, fieldName: String): Option[Long] = {
		val c = bean.getClass
		val s = c.getName + "." + fieldName
		val cached = sizeMap.get(s)

		if (cached.isDefined) {
			return cached.get
		}

		val beanDescriptor = validator.getConstraintsForClass(c)
		val pd = beanDescriptor.getConstraintsForProperty(fieldName)

		if (pd != null) {
			if (pd.getElementClass == classOf[String]) {
				pd.getConstraintDescriptors.foreach(cd =>
														if (cd.getAnnotation.isInstanceOf[Size]) {
															val size = cd.getAnnotation.asInstanceOf[Size]
															val maxSize = size.max()
															if (maxSize != java.lang.Integer.MAX_VALUE) {
																sizeMap.put(s, Some(maxSize))
																return Some(maxSize)
															}
														}
				)
			} else {
				pd.getConstraintDescriptors.foreach(cd =>
														if (cd.getAnnotation.isInstanceOf[Max]) {
															val max = cd.getAnnotation.asInstanceOf[Max]
															val size = max.value()
															val maxSize = scala.math.ceil(scala.math.log10(size)).toLong
															sizeMap.put(s, Some(maxSize))
															return Some(maxSize)
														}
				)
			}
		}
		sizeMap.put(s, None)
		None
	}

	private def getTextInputElement[T <: AnyRef](bean: AnyRef, fieldName: String, value: String, func: (T) => Any, m: Manifest[T],
												 attrs: SHtml.ElemAttr*): (String, String, Buffer[FieldError], NodeSeq, NodeSeq) = {
		val inputId = nextFuncName
		val containerId = nextFuncName
		val fieldErrors = getFieldErrors(bean, fieldName)
		val errorSeq = getErrorsSeq(fieldErrors)

		val maxLength = getMaxLengthOfProperty(bean, fieldName)

		var inputSeq = SHtml.text(tryo {fieldErrors(0).errorValue}.getOrElse(nullSafeString(value)), (s) => {
			validateInput(s, bean, fieldName, func, m.erasure.asInstanceOf[Class[T]])
		}, attrs: _*) % ("id" -> inputId) % maxLength.map(length => ("maxlength" -> length.toString):MetaData).getOrElse(Null)

		val oldOnBlurAttribute = inputSeq.attribute("onblur")
		val validationOnBlur = onBlurForTextInput(bean, fieldName, func, inputId, containerId, m.erasure.asInstanceOf[Class[T]])

		if (oldOnBlurAttribute.isDefined) {
			val oldOnBlur = oldOnBlurAttribute.get.text
			if (oldOnBlur.trim().length() > 0) {
				inputSeq = inputSeq % ("onblur" -> ("if (" + oldOnBlur + ") {" + validationOnBlur + "}"))
			}
		} else {
			inputSeq = inputSeq % ("onblur" -> validationOnBlur)
		}

		val textElement = addErrorClass(fieldErrors, inputSeq)
		(inputId, containerId, fieldErrors, textElement, errorSeq)
	}

	private def getTextAreaInputElement(bean: AnyRef, fieldName: String, value: String, func: (String) => Any,
												 attrs: SHtml.ElemAttr*): (String, String, Buffer[FieldError], NodeSeq, NodeSeq) = {
		val inputId = nextFuncName
		val containerId = nextFuncName
		val fieldErrors = getFieldErrors(bean, fieldName)
		val errorSeq = getErrorsSeq(fieldErrors)

		val maxLength = getMaxLengthOfProperty(bean, fieldName)

		var inputSeq = SHtml.textarea(tryo {fieldErrors(0).errorValue}.getOrElse(nullSafeString(value)), (s) => {
			validateInput(s, bean, fieldName, func, classOf[String])
		}, attrs: _*) % ("id" -> inputId) % maxLength.map(length => ("maxlength" -> length.toString):MetaData).getOrElse(Null)

		val oldOnBlurAttribute = inputSeq.attribute("onblur")
		val validationOnBlur = onBlurForTextInput(bean, fieldName, func, inputId, containerId, classOf[String])

		if (oldOnBlurAttribute.isDefined) {
			val oldOnBlur = oldOnBlurAttribute.get.text
			if (oldOnBlur.trim().length() > 0) {
				inputSeq = inputSeq % ("onblur" -> ("if (" + oldOnBlur + ") {" + validationOnBlur + "}"))
			}
		} else {
			inputSeq = inputSeq % ("onblur" -> validationOnBlur)
		}

		val textElement = addErrorClass(fieldErrors, inputSeq)
		(inputId, containerId, fieldErrors, textElement, errorSeq)
	}

	def nullSafeString(string:String) = if (string == null) "" else string

}


