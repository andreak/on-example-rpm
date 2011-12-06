package no.officenet.example.rpm.web.lib

import net.liftweb._
import common.{Empty, Full, Box}
import http.js.JE._
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
import org.joda.time.DateTime
import no.officenet.example.rpm.support.domain.i18n.GlobalTexts
import no.officenet.example.rpm.support.domain.i18n.Localizer
import no.officenet.example.rpm.support.domain.i18n.Localizer.L
import no.officenet.example.rpm.support.infrastructure.errorhandling.InvalidDateException

case class FieldError(fieldName: String, errorValue: String, errorId: String, errorMessage: String)

case object NullElemAttr extends SHtml.ElemAttr {
	def apply(in: Elem): Elem = in % Null
}

final class ApplicableElem(in: Elem) {
	def %(attr: SHtml.ElemAttr): Elem = attr.apply(in)
}

private[lib] object ValidationCache {
	val sizeMap = new HashMap[String, Option[Long]]
	val mandatoryMap = new HashMap[String, Boolean]
}

trait ValidatableScreen extends Loggable {

	// Needed for pattern-matching as one cannot use classOf[SomeType] as an extractor
	val stringCls = classOf[String]
	val shortCls = classOf[java.lang.Short]
	val shortPrimitiveCls = java.lang.Short.TYPE
	val intCls = classOf[java.lang.Integer]
	val intPrimitiveCls = java.lang.Integer.TYPE
	val longCls = classOf[java.lang.Long]
	val longPrimitiveCls = java.lang.Long.TYPE
	val floatCls = classOf[java.lang.Float]
	val floatPrimitiveCls = java.lang.Float.TYPE
	val doubleCls = classOf[java.lang.Double]
	val doublePrimitiveCls = java.lang.Double.TYPE
	val javaBigDcmlCls = classOf[java.math.BigDecimal]
	val bigDcmlCls = classOf[BigDecimal]
	val dateCls = classOf[java.util.Date]
	val dateTimeCls = classOf[DateTime]
	val optionCls = classOf[Option[_]]

	private def convert[T](value: String, klass: Class[_], isOption: Boolean = false): T = {
		def fixDecimalFormat(value: String) = value.replaceAll(" ", "").replaceAll(",", ".")

		val converted = klass match {
		case `stringCls` => value
		case `shortCls` | `shortPrimitiveCls` => value.toShort
		case `intCls` | `intPrimitiveCls` => value.toInt
		case `longCls` | `longPrimitiveCls` => value.toLong
		case `floatCls` | `floatPrimitiveCls` => fixDecimalFormat(value).toFloat
		case `doubleCls` | `doublePrimitiveCls` => fixDecimalFormat(value).toDouble
		case `javaBigDcmlCls` => new java.math.BigDecimal(fixDecimalFormat(value))
		case `bigDcmlCls` => BigDecimal(fixDecimalFormat(value))
		case `dateCls` => Localizer.getDateFromString(L(GlobalTexts.dateformat_fullDate), value)
		case `dateTimeCls` => Localizer.getDateTimeFromString(L(GlobalTexts.dateformat_fullDate), value)
		case _ => throw new IllegalArgumentException("Don't know how to convert value " + value + " of type " + klass.getName)
	}
		val retValue = if (isOption) {
			Some(converted)
		} else {
			converted
		}
		retValue.asInstanceOf[T]
	}

	val errorsMap = new IdentityHashMap[AnyRef, HashMap[String, Buffer[FieldError]]]()

	@Resource
	val validator: Validator = null

	def clearErrors = {
		S.formGroup(-1)(SHtml.hidden(() =>
            errorsMap.clear()
		))
	}

	implicit def elemToApplicable(e: Elem): ApplicableElem = new ApplicableElem(e)

	trait Label {
		def label: () => NodeSeq
	}

	implicit def string2NodeSeq(s: String) = Text(s)

	implicit def formField2NodeSeq(field: FormField[_,_]): NodeSeq = field.toForm

	object Label {
		def apply(l: => NodeSeq) = new Label{val label = () => l}
	}

	trait InputContainerWithLabel[T] extends InputContainer[T] with Label {
	}

	trait InputContainer[T] {
		var formField: FormField[_,T] = null
		var isEditMode: () => Boolean = () => true
		var readonlyCssClasses: Box[String] = Empty

		protected def mandatoryIcon(isMandatory:Boolean) = {
			if(isMandatory)
				<div class="mandatory">*</div>
			else{
				NodeSeq.Empty
			}
		}

		protected def getReadOnlyText: NodeSeq = {
			val readOnlyText = if (formField.readOnlyFormatterFunc.isDefined) {
				formField.readOnlyFormatterFunc.get.apply()
			} else {
				formField.defaultReadOnlyValue
			}
			if (readOnlyText == null || readOnlyText.trim.length == 0) {
				NodeSeq.Empty
			} else {
				Text(readOnlyText)
			}
		}

		protected def cssClasses: Box[String] = {
			Seq(
				Full("errorContainer").filter(s => !formField.fieldErrors.isEmpty),
				readonlyCssClasses.filter(x => !isEditMode())
			).flatten.reduceOption((a, b) => a + " " + b)
		}

		def withReadOnlyCssClasses(classes: String*) = {
			readonlyCssClasses = Full(classes.mkString(" ")).filter(s => !s.isEmpty)
			this
		}

		def toForm: NodeSeq
	}

	class InlineInputContainer[T](_formField: FormField[_,T]) extends InputContainer[T] {
		formField = _formField

		def toForm: NodeSeq = {
			(".inputContainer [class+]" #> cssClasses)
				.apply(
				<div style="display: inline-block" id={formField.containerId} class="inputContainer">
					{
					if (isEditMode()) {
						getReadOnlyText
					} else {
						formField.textElement ++ mandatoryIcon(formField.isMandatory) ++ formField.errorSeq
					}
					}
				</div>
			)
		}

	}

	object InlineInputContainer {
		def apply[T](formField: FormField[_,T]):InputContainer[T] = new InlineInputContainer[T](formField)
	}

	class TdInputContainer[T](_label: Label, editModeFunc: Box[() => Boolean]) extends InputContainerWithLabel[T] {

		def this(label: Label) {
			this(label, Empty)
		}

		def this(label: String) {
			this(Label(Text(label)), Empty)
		}

		def this(label: String, editModeFunc: () => Boolean) {
			this(Label(Text(label)), Full(editModeFunc))
		}

		var label = _label.label
		isEditMode = editModeFunc.openOr(isEditMode)

		def toForm: NodeSeq = {
			<td><label for={formField.inputId}>{label()}</label></td> ++
			("td [class+]" #> cssClasses)
				.apply(
				<td id={formField.containerId}>
					{
					if (isEditMode()) {
						formField.textElement ++ mandatoryIcon(formField.isMandatory) ++ formField.errorSeq
					} else {
						getReadOnlyText
					}
					}
				</td>
			)
		}

	}

	object TdInputContainer {
		def apply[T](_label: String):InputContainer[T] = new TdInputContainer[T](Label(Text(_label)))

		def apply[T](_label: Label):InputContainer[T] = new TdInputContainer[T](_label)

		def apply[T](_label: String, editModeFunc: () => Boolean):InputContainer[T] = new TdInputContainer[T](_label, editModeFunc)
	}

	trait TextFormField[A <: AnyRef, T] extends FormField[A, T] {
		type K = String

		def defaultReadOnlyValueFunc(k: K): String = k

		protected final def getInputElementDrus(fieldErrors: Buffer[FieldError], klass: Class[_], isOption: Boolean): Elem = {
			val func: (String) => Any = (s) => {
				validateInput(s, bean, fieldName, assignmentCallback, additionalValidationFunc, additionalErrorMessageFunc,
							  klass, isOption)
			}
			val inputSeq = getInputElement(tryo {fieldErrors(0).errorValue}.getOrElse(nullSafeString(defaultValue)), func)
			inputSeq
		}

		protected final def getOnEventValidation(inputSeq: Elem, klass: Class[_], isOption: Boolean): Elem = {
			val oldOnBlurAttribute = inputSeq.attribute("onblur")
			val validationOnBlur = onBlurForTextInput(bean, fieldName, assignmentCallback, additionalValidationFunc,
													  additionalErrorMessageFunc, ajaxCallbackFunc, inputId, containerId,
													  klass, isOption)
			if (oldOnBlurAttribute.isDefined) {
				val oldOnBlur = oldOnBlurAttribute.get.text
				if (oldOnBlur.trim().length() > 0) {
					inputSeq % ("onblur" -> ("if (" + getAsAnonFunc(oldOnBlur) + ") {" + validationOnBlur + "}"))
				} else inputSeq
			} else {
				inputSeq % ("onblur" -> validationOnBlur)
			}
		}

		private def onBlurForTextInput[T](bean: AnyRef, fieldName: String, func: (T) => Any,
										  additionalValidationFunc: Box[T => Boolean] = Empty,
										  additionalErrorMessageFunc: Box[String => String] = Empty,
										  ajaxCallbackFunc: Box[T => JsCmd] = Empty,
										  inputId: String, containerId: String, klass: Class[_], isOption: Boolean) = {
			SHtml.onEvent((s) => {
				cleanErrorsForProperty(bean, fieldName) // Important to clear errors for this field in case previous action was "submit" on form
				val convertedValue = validateInput(s, bean, fieldName, func, additionalValidationFunc, additionalErrorMessageFunc, klass, isOption)
				val fieldErrors = getFieldErrors(bean, fieldName)
				val errorSeq: NodeSeq = getErrorsSeq(fieldErrors)
				cleanErrorsForProperty(bean, fieldName)
				val errorHandlerCmd = if (fieldErrors.isEmpty) {
					Call("Rolf.removeFieldError", containerId, inputId)
				} else {
					Call("Rolf.attachFieldError", containerId, inputId, errorSeq.toString())
				}
				val extraCmd: JsCmd = convertedValue match {
					case d: java.util.Date => JsRaw("$(%s).value=%s".format(inputId.encJs, Localizer.formatDate(L(GlobalTexts.dateformat_fullDate), d, S.locale).encJs))
					case d: DateTime => JsRaw("$(%s).value=%s".format(inputId.encJs, Localizer.formatDateTime(L(GlobalTexts.dateformat_fullDate), Full(d), S.locale).get.encJs))
					case _ => Noop
				}
				errorHandlerCmd & extraCmd & (if (fieldErrors.isEmpty) ajaxCallbackFunc.map(f => f(convertedValue.asInstanceOf[T])).openOr(Noop) else Noop)
			})._2.toJsCmd
		}

	}

	trait PicableFormField[A <: AnyRef, T] extends FormField[A, T] {
		type K = T

		protected final def getInputElementDrus(fieldErrors: Buffer[FieldError], klass: Class[_], isOption: Boolean): Elem = {
			val inputSeq = getInputElement(defaultValue, assignmentCallback)
			inputSeq
		}

		protected def onEventForSelect[T](secure: Seq[(T, String, String)], bean: AnyRef, fieldName: String, assignmentCallback: (T) => Any,
										  inputId: String, containerId: String, onSelectAjaxCallback: Box[T => JsCmd] = Empty)(implicit m: Manifest[T]) = {
			SHtml.onEvent((s) => {
				cleanErrorsForProperty(bean, fieldName) // Important to clear errors for this field in case previous action was "submit" on form
				//			trace("onChangeForSelect: " + s)
				secure.find(_._2 == s).map{x =>
					val element = x._1
					//				trace("onChangeForSelect.element: " + element)
					assignmentCallback(element)
					registerPropertyViolations(bean, fieldName)
					registerBeanViolations(bean, fieldName)
					val fieldErrors = getFieldErrors(bean, fieldName)
					val errorSeq: NodeSeq = getErrorsSeq(fieldErrors)
					cleanErrorsForProperty(bean, fieldName)
					if (fieldErrors.isEmpty) {
						Call("Rolf.removeFieldError", containerId, inputId) & onSelectAjaxCallback.map(f => f(element)).openOr(Noop)
					} else {
						Call("Rolf.attachFieldError", containerId, inputId, errorSeq.toString()) & onSelectAjaxCallback.map(f => f(element)).openOr(Noop)
					}
										  }.getOrElse(Noop)
			})._2.toJsCmd
		}

	}

	trait FormField[A <: AnyRef, T] {
		type K
		private def mandatory(bean: A, fieldName: String): Boolean = {
			val c = bean.getClass
			val s = c.getName + "." + fieldName
			val cached = ValidationCache.mandatoryMap.get(s)

			if (cached.isDefined) {
				return cached.get
			}

			val beanDescriptor = validator.getConstraintsForClass(c)
			val pd = beanDescriptor.getConstraintsForProperty(fieldName)

			if (pd != null) {
				pd.getConstraintDescriptors.foreach(cd =>
														if (cd.getAnnotation.isInstanceOf[javax.validation.constraints.NotNull]) {
															ValidationCache.mandatoryMap.put(s, true)
															return true
														}
				)
			}
			ValidationCache.mandatoryMap.put(s, false)
			false
		}

		private def getClassFromManifest[T](m: Manifest[T]): (Class[_], Boolean) = {
			val klass = m.erasure
			val isOption: Boolean = klass == optionCls
			val fish = if (isOption && m.typeArguments.length > 0) m.typeArguments.head.erasure else klass
			(fish, isOption)
		}

		protected def getAsAnonFunc(jsExp: String): String = {
			"(" + AnonFunc(Run(jsExp)).toJsCmd + ").bind(this)()"
		}

		private def getFormInputElement: (Buffer[FieldError], NodeSeq, NodeSeq) = {
			val fieldErrors = getFieldErrors(bean, fieldName)
			val errorSeq = getErrorsSeq(fieldErrors)

			val maxLength = getMaxLengthOfProperty(bean, fieldName)
			val (klass, isOption) = getClassFromManifest(manifest)

			var inputSeq = getInputElementDrus(fieldErrors, klass, isOption) %
						   ("id" -> inputId) %
						   maxLength.map(length => ("maxlength" -> length.toString):MetaData).getOrElse(Null)

			inputSeq = getOnEventValidation(inputSeq, klass, isOption)

			val textElement = addErrorClass(fieldErrors, inputSeq)
			(fieldErrors, textElement, errorSeq)
		}

		protected def getInputElementDrus(fieldErrors: Buffer[FieldError], klass: Class[_], isOption: Boolean): Elem
		protected def getInputElement(value: K, func: K => Any): Elem
		protected def getOnEventValidation(inputSeq: Elem, klass: Class[_], isOption: Boolean): Elem

		val inputId = nextFuncName
		val containerId = nextFuncName

		lazy val (fieldErrors, textElement, errorSeq) = getFormInputElement

		def toForm = container.toForm

		def manifest: Manifest[T]

		def bean: A
		def fieldName: String
		def defaultValue: K

		protected def defaultReadOnlyValueFunc(value: K): String

		final def defaultReadOnlyValue: String = {
			defaultReadOnlyValueFunc(defaultValue)
		}

		def assignmentCallback: T => Any

		def isMandatory = mandatory(bean, fieldName) || mandatory

		private var mandatory = false
		var container: InputContainer[T] = InlineInputContainer[T](this)
		var additionalValidationFunc: Box[T => Boolean] = Empty
		var additionalErrorMessageFunc: Box[String => String] = Empty
		var ajaxCallbackFunc: Box[T => JsCmd] = Empty
		var attrs: Seq[SHtml.ElemAttr] = Nil
		var readOnlyFormatterFunc: Box[() => String] = Empty

		def withContainer(container: InputContainer[T]): this.type = {
			container.formField = this
			this.container = container
			this
		}

		def withAdditionalValidationFunc(func: T => Boolean): this.type = {
			this.additionalValidationFunc = Full(func)
			this
		}

		def withAdditionalErrorMessageFunc(func: String => String): this.type = {
			this.additionalErrorMessageFunc = Full(func)
			this
		}

		def withAjaxCallbackFunc(func: T => JsCmd): this.type = {
			this.ajaxCallbackFunc = Full(func)
			this
		}

		def withMandatory(isMandatory: Boolean): this.type = {
			this.mandatory = isMandatory
			this
		}

		def withAttrs(attrs: SHtml.ElemAttr*): this.type = {
			this.attrs = attrs
			this
		}

		def withReadOnlyFormatter(formatterFunc: => String): this.type = {
			readOnlyFormatterFunc = Full(() => formatterFunc)
			this
		}

	}

	abstract class AbstractFormField[A <: AnyRef, T](implicit m: Manifest[T]) {
		self: FormField[A, T]  =>
		val manifest = m
	}

	abstract class JpaFormField[A <: AnyRef, T](field: javax.persistence.metamodel.Attribute[A, T])(implicit m: Manifest[T])
		extends AbstractFormField[A, T] {
		self: FormField[A, T]  =>
		def fieldName = field.getName
	}

	object TextField {
		def apply[A <: AnyRef, T](bean: A, field: javax.persistence.metamodel.Attribute[A, T], defaultValue: String,
											assignmentCallback: T => Any)(implicit m: Manifest[T]) =
			new TextField[A, T](bean, field, defaultValue, assignmentCallback)
	}

	class TextField[A <: AnyRef, T](val bean: A,
									val field: javax.persistence.metamodel.Attribute[A, T],
									val defaultValue: TextFormField[A, T]#K,
									val assignmentCallback: T => Any)(implicit m: Manifest[T])
		extends JpaFormField[A, T](field) with TextFormField[A, T] {
		def getInputElement(value: String, func: String => Any): Elem = {
			SHtml.text(value, func, attrs: _*)
		}
	}

	object TextAreaField {
		def apply[A <: AnyRef, T](bean: A, field: javax.persistence.metamodel.Attribute[A, T], defaultValue: String,
								  assignmentCallback: T => Any)(implicit m: Manifest[T]) =
			new TextAreaField[A, T](bean, field, defaultValue, assignmentCallback)
	}

	class TextAreaField[A <: AnyRef, T](val bean: A,
										val field: javax.persistence.metamodel.Attribute[A, T],
										val defaultValue: TextFormField[A, T]#K,
										val assignmentCallback: T => Any)(implicit m: Manifest[T])
		extends JpaFormField[A, T](field) with TextFormField[A, T] {
		def getInputElement(value: String, func: String => Any): Elem = {
			SHtml.textarea(value, func, attrs: _*)
		}
	}

	object SelectField {
		def apply[A <: AnyRef, T](bean: A,
								  field: javax.persistence.metamodel.Attribute[A, T],
								  options: Seq[(T, List[SHtml.ElemAttr])],
								  defaultValue: T,
								  assignmentCallback: T => Any,
								  valueLabel: (T,Int) => String)(implicit m: Manifest[T]) =
			new SelectField[A, T](bean, field, options, defaultValue, assignmentCallback, valueLabel)
	}

	class SelectField[A <: AnyRef, T](val bean: A,
									  val field: javax.persistence.metamodel.Attribute[A, T],
									  options: Seq[(T, List[SHtml.ElemAttr])],
									  val defaultValue: T,
									  val assignmentCallback: T => Any,
									  valueLabel: (T,Int) => String)(implicit m: Manifest[T])
		extends JpaFormField[A, T](field) with PicableFormField[A, T] {


		def defaultReadOnlyValueFunc(value: T): String = defaultValue match {
			case null => null
			case _ => valueLabel(defaultValue, 0)
		}

		val allOptions:Seq[(T, String, List[SHtml.ElemAttr])] =
			options.zipWithIndex.map{case ((t, oattrs), idx) => (t, valueLabel(t, idx), oattrs)}.toList

		val secure = allOptions.map {case (obj, txt, oattrs) => (obj, randomString(20), txt, oattrs)}

		val (nonces, defaultNonce, secureOnSubmit) =
			secureOptionsWithDrus(secure, Full(defaultValue), (selectedItem:T) => {
				assignmentCallback(selectedItem)
				registerPropertyViolations(bean, fieldName)
				registerBeanViolations(bean, fieldName)
			})

		def getInputElement(defaultValue: T, assignmentCallback: (T) => Any): Elem = {
			val inputSeq = ritchSelect_*(nonces, defaultNonce, secureOnSubmit, attrs: _*)
			inputSeq
		}

		protected final def getOnEventValidation(inputSeq: Elem, klass: Class[_], isOption: Boolean): Elem = {
			val oldOnChangeAttribute = inputSeq.attribute("onchange")
			val validationOnChange = onEventForSelect(secure.map{case (obj, nonce, txt, oattrs) => (obj, nonce, txt)},
													  bean, fieldName, assignmentCallback,
													  inputId, containerId, ajaxCallbackFunc)
			if (oldOnChangeAttribute.isDefined) {
				val oldOnChange = oldOnChangeAttribute.get.text
				if (oldOnChange.trim().length() > 0) {
					inputSeq % ("onchange" -> ("if (" + getAsAnonFunc(oldOnChange) + ") {" + validationOnChange + "}"))
				} else inputSeq
			} else {
				inputSeq % ("onchange" -> validationOnChange)
			}
		}

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


	private def ritchSelect_*(opts: Seq[(String, String, List[SHtml.ElemAttr])], deflt: Box[String],
							  func: AFuncHolder, attrs: SHtml.ElemAttr*): Elem = {
		def selected(in: Boolean) = if (in) new UnprefixedAttribute("selected", "selected", Null) else Null
		val vals = opts.map(_._1)
		val testFunc = LFuncHolder(in => in.filter(v => vals.contains(v)) match {case Nil => false case xs => func(xs)}, func.owner)

		attrs.foldLeft(S.fmapFunc(testFunc)(fn => <select name={fn}>{opts.flatMap {case (value, text, optAttrs) =>
			optAttrs.foldLeft(<option value={value}>{text}</option>)(_ % _) % selected(deflt.exists(_ == value)) }}</select>))(_ % _)
	}

	private def secureOptionsWithDrus[T](secure: Seq[(T, String, String, List[SHtml.ElemAttr])], default: Box[T],
								 onSubmit: T => Any): (Seq[(String, String, List[SHtml.ElemAttr])], Box[String], AFuncHolder) = {
		val defaultNonce = default.flatMap(d => secure.find(_._1 == d).map(_._2))
		val nonces = secure.map {case (obj, nonce, txt, oattrs) => (nonce, txt, oattrs)}
		def process(nonce: String) {
			secure.find(_._2 == nonce).map(x => onSubmit(x._1))
		}
		(nonces, defaultNonce, S.SFuncHolder(process))
	}

	private def registerFieldViolations[T](bean: AnyRef, fieldName: String, newValue: T) {
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
		trace("Registered errors so far: " + errorsMap)
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

	private def convertInputValue[T](bean: AnyRef, fieldName: String, newValue: String, klass: Class[_], isOption: Boolean): T = {
		val convertedValue: T = if (newValue == null) {
			if (isOption) {
				None.asInstanceOf[T]
			} else {
				null.asInstanceOf[T]
			}
		} else {
			convert(newValue, klass, isOption)
		}
		convertedValue
	}

	private def doFieldAndBeanValidation[T](bean: AnyRef, fieldName: String, convertedValue: T) {
		registerFieldViolations(bean, fieldName, convertedValue)
		registerBeanViolations(bean, fieldName)
	}

	private def doValidation[T](bean: AnyRef, fieldName: String, convertedValue: T, inputValue: String, additionalValidationFunc: Box[(T) => Boolean] = Empty, additionalErrorMessageFunc: Box[(String) => String] = Empty) {
		if (!(additionalValidationFunc.isDefined && additionalErrorMessageFunc.isDefined ||
				additionalValidationFunc.isEmpty && additionalErrorMessageFunc.isEmpty)) {
			throw new IllegalArgumentException("additionalValidationFunc AND additionalErrorMessageFunc must be both Full or both Empty")
		}
		doFieldAndBeanValidation(bean, fieldName, convertedValue)
		val tmpFieldErrors = getFieldErrors(bean, fieldName)
		if (tmpFieldErrors.isEmpty) {
			additionalValidationFunc.foreach(f => {
				if (!f(convertedValue)) {
					additionalErrorMessageFunc.foreach(emFunc => registerError(bean, fieldName, inputValue, emFunc(inputValue)))
				}
			})
		} else {
			if (log.isTraceEnabled && additionalValidationFunc.isDefined) {
				trace("tmpFieldErrors: " + tmpFieldErrors)
			}
		}
	}

	private def validateInput[T](inputValue: String, bean: AnyRef, fieldName: String, assignmentCallback: (T) => Any,
								 additionalValidationFunc: Box[T => Boolean] = Empty,
								 additionalErrorMessageFunc: Box[String => String] = Empty,
								 klass: Class[_], isOption: Boolean): Any = {
		val newValue = if (inputValue == null || inputValue.trim().isEmpty) null else inputValue.trim()
		try {
			val convertedValue: T = convertInputValue(bean, fieldName, newValue, klass, isOption)
			assignmentCallback(convertedValue)
			trace("dateformat_fullDate: " + L(GlobalTexts.dateformat_fullDate))
			trace("S.locale: " + S.locale)
			trace("Validating field: " + fieldName + " with value '" + convertedValue + "'")
			doValidation(bean, fieldName, convertedValue, inputValue, additionalValidationFunc, additionalErrorMessageFunc)
			convertedValue
		} catch {
			case e: NumberFormatException =>
				registerError(bean, fieldName, if (newValue != null) newValue.toString else null,
							  L(GlobalTexts.validation_notANumber_number_text, inputValue))
				inputValue
			case e: InvalidDateException =>
				registerError(bean, fieldName, if (newValue != null) newValue.toString else null,
							  L(GlobalTexts.validation_invalidDate_text, inputValue))
				inputValue
		}
	}

	private def getMaxLengthOfProperty(bean: AnyRef, fieldName: String): Option[Long] = {
		val c = bean.getClass
		val s = c.getName + "." + fieldName
		val cached = ValidationCache.sizeMap.get(s)

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
																ValidationCache.sizeMap.put(s, Some(maxSize))
																return Some(maxSize)
															}
														}
				)
			} else {
				pd.getConstraintDescriptors.foreach(cd =>
														if (cd.getAnnotation.isInstanceOf[Max]) {
															val max = cd.getAnnotation.asInstanceOf[Max]
															val size = max.value()
															val maxSize = if (size % 10 == 0) {
																scala.math.log10(size).toLong + 1L
															} else {
																scala.math.ceil(scala.math.log10(size)).toLong
															}
															ValidationCache.sizeMap.put(s, Some(maxSize))
															return Some(maxSize)
														}
				)
			}
		}
		ValidationCache.sizeMap.put(s, None)
		None
	}

	def nullSafeString(string:String) = if (string == null) "" else string

}


