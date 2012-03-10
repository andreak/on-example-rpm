package no.officenet.example.rpm.web.lib

import net.liftweb._
import common.{Failure, Empty, Full, Box}
import http.js.JE._
import http.js.JsCmd
import http.js.JsCmds._
import http.S.AFuncHolder
import http.S.LFuncHolder
import http.SHtml.ChoiceItem
import http.{LiftRules, AjaxContext, S, SHtml}
import util.Helpers._
import xml._
import org.joda.time.DateTime
import no.officenet.example.rpm.support.infrastructure.i18n.GlobalTexts
import no.officenet.example.rpm.support.infrastructure.i18n.Localizer.L
import no.officenet.example.rpm.support.infrastructure.i18n.DateFormatter._
import no.officenet.example.rpm.support.infrastructure.i18n.NumberFormatter._
import no.officenet.example.rpm.support.infrastructure.scala.lang.IdentityHashMap
import no.officenet.example.rpm.web.lib.LiftUtils._
import no.officenet.example.rpm.web.lib.RolfJsCmds.{AttachFieldError, RemoveFieldError}
import collection.mutable.{ArrayBuffer, HashMap, Buffer}
import no.officenet.example.rpm.support.infrastructure.logging.Loggable
import no.officenet.example.rpm.support.infrastructure.errorhandling.{FieldError, InvalidDateException}

trait ValidatableScreen extends Loggable with ErrorsAware {

	final class ApplicableElem(in: Elem) {
		def %(attr: SHtml.ElemAttr): Elem = attr.apply(in)
	}

	implicit def elemToApplicable(e: Elem): ApplicableElem = new ApplicableElem(e)


	// Note: Having multiple submit-buttons, one for each row of input-fields but all rows of inputs in the same form
	// makes errors-accumulate when only re-rendering 1 row (row.applyAgain). This is because the errors are cleared out
	// upon re-render of the element the error(s) belong to and when the other rows are not re-rendered, the errors are not
	// cleared. This results in lots of duplicate errors for fields in other rows when they are submitted.
	// Example is editing 2 address rows and not filling in zip-code for any of them and trying to save them one at a time

	val errorsMap = new IdentityHashMap[AnyRef, HashMap[String, Buffer[FieldError]]]()

	override def hasErrors = !errorsMap.isEmpty

	private def clearErrors: Elem = {
		S.formGroup(-1)(SHtml.hidden(() =>
			errorsMap.clear()
		))
	}

	// Taken from net.liftweb.builtin.snippet.Form
	private[this] def addAjaxForm(): MetaData = {
		val id = nextFuncName

		// Note: Exclude the attribute-name we've choosen, 'type', here, unless it's added as an attribute to the form-element
		val attr = S.currentAttrsToMetaData(name => name != "id" && name != "onsubmit" && name != "action" && name != "type")

		val pre = S.attr.~("onsubmit").map(_.text + ";") getOrElse ""

		val post = S.attr.~("postsubmit").map("function() { " + _.text + "; }")

		val ajax: String = pre + SHtml.makeAjaxCall(LiftRules.jsArtifacts.serialize(id), AjaxContext.js(post)).toJsCmd + ";" + "return false;"

		new UnprefixedAttribute("id", Text(id),
			new UnprefixedAttribute("action", Text("javascript://"),
				new UnprefixedAttribute("onsubmit", Text(ajax), attr)))
	}

	// Taken from net.liftweb.builtin.snippet.Form
	private[this] def renderAjaxForm(kids: NodeSeq): Elem = {
		if (kids.length == 1 &&
			kids(0).isInstanceOf[Elem] &&
			(kids(0).prefix eq null) &&
			kids(0).label == "form") {
			new Elem(null, "form", addAjaxForm(), TopScope, kids(0).child: _*)
		} else {
			Elem(null, "form", addAjaxForm(), TopScope, kids: _*)
		}
	}

	// Taken from net.liftweb.builtin.snippet.Form
	private[this] def renderPostForm(kids: NodeSeq): Elem = {
		val ret: Elem =
			if (kids.length == 1 &&
				kids(0).isInstanceOf[Elem] &&
				(kids(0).prefix eq null) &&
				kids(0).label == "form") {
				val e = kids(0).asInstanceOf[Elem]
				val meta =
					new UnprefixedAttribute("method", "post",
						new UnprefixedAttribute(
							"action", S.uri,
							e.attributes.filter {
								case up: UnprefixedAttribute =>
									up.key != "method" && up.key != "action"
								case x => true
							}))
				new Elem(null, "form", meta, e.scope, e.child: _*)
			} else {
				<form method="post" action={S.uri}>
					{kids}
				</form>
			}

		S.attr("multipart") match {
			case Full(x) if toBoolean(x) => ret % ("enctype" -> "multipart/form-data")
			case _ => ret
		}
	}

	final def renderForm(kids: NodeSeq): NodeSeq = {
		val formKids = clearErrors ++ renderScreen().apply(kids)
		val form: NodeSeq = S.attr("type") match {
			case Full(x) => x match {
				case "ajax" => renderAjaxForm(formKids)
				case "post" => renderPostForm(formKids)
				case _ => throw new IllegalArgumentException("Invalid value '" + x + "' to 'type' parameter")
			}
			case _ => renderScreen().apply(kids)
		}
		form
	}

	protected def renderScreen(): (NodeSeq) => NodeSeq = (ns: NodeSeq) => renderScreen(ns)

	protected def renderScreen(ns: NodeSeq): NodeSeq = throw new IllegalStateException("Must override renderScreen")

	trait Label {
		def label: () => NodeSeq
	}

	implicit def string2NodeSeq(s: String) = Text(s)

	implicit def formField2NodeSeq(field: FormField[_, _]): NodeSeq = field.toForm

	object Label {
		def apply(l: => NodeSeq) = new Label {
			val label = () => l
		}
	}

	trait InputContainerWithLabel[T] extends InputContainer[T] with Label {
	}

	trait InputContainer[T] {
		var formField: FormField[_, T] = null
		var isEditMode: () => Boolean = () => true
		var readonlyCssClasses: Box[String] = Empty

		protected def getReadOnlyText: NodeSeq = {
			if (formField.readOnlyFormatterFunc.isDefined) {
				return formField.readOnlyFormatterFunc.get.apply()
			}
			if (formField.defaultReadOnlyValue == null || formField.defaultReadOnlyValue.trim.length == 0) {
				NodeSeq.Empty
			} else {
				Text(formField.defaultReadOnlyValue)
			}
		}

		protected def cssClasses: Box[String] = {
			Seq(
				Full("errorContainer").filter(s => !formField.getFormFieldErrors.isEmpty),
				readonlyCssClasses.filter(x => !isEditMode())
			).flatten.reduceOption((a, b) => a + " " + b)
		}

		def withReadOnlyCssClasses(classes: String*) = {
			readonlyCssClasses = Full(classes.mkString(" ")).filter(s => !s.isEmpty)
			this
		}

		def toForm: NodeSeq
	}

	class InlineInputContainer[T](_formField: FormField[_, T]) extends InputContainer[T] {
		formField = _formField

		def toForm: NodeSeq = {
			(".inputContainer [class+]" #> cssClasses)
				.apply(
				<div style="display: inline-block" id={formField.containerId} class="inputContainer">
					{if (isEditMode()) {
					formField.getFormField ++ mandatoryIcon(formField.isMandatory) ++ formField.getFormFieldErrorSeq
				} else {
					getReadOnlyText
				}}
				</div>
			)
		}

	}

	object InlineInputContainer {
		def apply[T](formField: FormField[_, T]): InputContainer[T] = new InlineInputContainer[T](formField)
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
			<td>
				<label for={formField.inputId}>
					{label()}
				</label>
			</td> ++
				("td [class+]" #> cssClasses)
					.apply(
					<td id={formField.containerId}>
						{if (isEditMode()) {
						formField.getFormField ++ mandatoryIcon(formField.isMandatory) ++ formField.getFormFieldErrorSeq
					} else {
						getReadOnlyText
					}}
					</td>
				)
		}

	}

	object TdInputContainer {
		def apply[T](_label: String): InputContainer[T] = new TdInputContainer[T](Label(Text(_label)))

		def apply[T](_label: Label): InputContainer[T] = new TdInputContainer[T](_label)

		def apply[T](_label: String, editModeFunc: () => Boolean): InputContainer[T] = new TdInputContainer[T](_label, editModeFunc)
	}

	trait TextFormField[A <: AnyRef, T] extends FormField[A, T] {
		type K = String

		def defaultReadOnlyValueFunc(k: K): String = k

		protected def validateInput(inputValue: String, bean: AnyRef, fieldName: String, assignmentCallback: (T) => Any)(implicit m: Manifest[T]): Box[Any] = {
			val newValue = if (inputValue == null || inputValue.trim().isEmpty) null else inputValue.trim()
			try {
				val convertedValue: T = InputStringConverter.convert[T](newValue)
				assignmentCallback(convertedValue)
				trace("dateformat_fullDate: " + L(GlobalTexts.dateformat_fullDate))
				trace("S.locale: " + S.locale)
				trace("Validating field: " + fieldName + " with value '" + convertedValue + "'")
				doFieldValidation(bean, fieldName, convertedValue, inputValue)
				Full(convertedValue)
			} catch {
				case e@(_: NumberFormatException | _: java.text.ParseException) =>
					registerError(newValue)(bean, fieldName, newValue,
						L(GlobalTexts.validation_notANumber_number_text, inputValue), errorsMap, nextFuncName)
					Failure(inputValue)
				case e: InvalidDateException =>
					registerError(newValue)(bean, fieldName, newValue,
						L(GlobalTexts.validation_invalidDate_text, inputValue), errorsMap, nextFuncName)
					Failure(inputValue)
			}
		}

		protected final def getInputElementDrus(fieldErrors: Buffer[FieldError])(implicit m: Manifest[T]): Elem = {
			val func: (String) => Any = (s) => {
				validateInput(s, bean, fieldName, assignmentCallback)
			}
			val inputSeq = getInputElement(tryo {
				fieldErrors(0).errorValue.toString
			}.getOrElse(nullSafeString(defaultValue)), func)
			inputSeq
		}

		protected def onBlurForTextInput(bean: AnyRef, fieldName: String, func: (T) => Any,
										 ajaxCallbackFunc: Box[T => JsCmd] = Empty,
										 inputId: String, containerId: String)(implicit m: Manifest[T]) = {
			SHtml.onEvent((s) => {
				cleanErrorsForProperty(bean, fieldName) // Important to clear errors for this field in case previous action was "submit" on form
				val convertedValue: Box[Any] = validateInput(s, bean, fieldName, func)
				val fieldErrors = getFieldErrors(bean, fieldName)
				val errorSeq: NodeSeq = getErrorsSeq(fieldErrors)
				cleanErrorsForProperty(bean, fieldName)
				val errorHandlerCmd = if (fieldErrors.isEmpty) {
					RemoveFieldError(containerId, inputId)
				} else {
					AttachFieldError(containerId, inputId, errorSeq)
				}
				val extraCmd: JsCmd = convertedValue match {
					case Full(d: java.util.Date) => JsRaw("$(%s).value=%s".format(inputId.encJs, formatFullDate(d).encJs))
					case Full(d: DateTime) => JsRaw("$(%s).value=%s".format(inputId.encJs, formatFullDate(d.toDate).encJs))
					case _ => Noop
				}
				val ajaxCallbackFuncResult: JsCmd = convertedValue match {
					case Full(cv) => ajaxCallbackFunc.map(f => f(cv.asInstanceOf[T])).openOr(Noop)
					case _ => Noop
				}
				errorHandlerCmd & extraCmd & ajaxCallbackFuncResult
			})._2.toJsCmd
		}

		protected final def getOnEventValidation(inputSeq: Elem)(implicit m: Manifest[T]): Elem = {
			val oldOnBlurAttribute = inputSeq.attribute("onblur")
			val validationOnBlur = onBlurForTextInput(bean, fieldName, assignmentCallback,
				ajaxCallbackFunc, inputId, containerId)
			if (oldOnBlurAttribute.isDefined) {
				val oldOnBlur = oldOnBlurAttribute.get.text
				if (oldOnBlur.trim().length() > 0) {
					inputSeq % ("onblur" -> ("if (" + getAsAnonFunc(oldOnBlur) + " !== false) {" + validationOnBlur + "}"))
				} else inputSeq
			} else {
				inputSeq % ("onblur" -> validationOnBlur)
			}
		}

		def withInputMask(mask: InputMask): this.type = {
			mask.applyTo(this)
			this
		}

	}

	trait PicableFormField[A <: AnyRef, T] extends FormField[A, T] {
		type K = T

		protected final def getInputElementDrus(fieldErrors: Buffer[FieldError])(implicit m: Manifest[T]): Elem = {
			val inputSeq = getInputElement(defaultValue, assignmentCallback)
			inputSeq
		}

	}

	trait FormField[A <: AnyRef, T] {
		type K

		def hasExternalFieldValidation = false

		def performInPlaceFieldValidation: Boolean = {
			doPerformInPlaceFieldValidation && hasFieldValidation
		}

		def hasFieldValidation: Boolean = {
			(additionalValidationFunc.isDefined && additionalErrorMessageFunc.isDefined) || hasExternalFieldValidation
		}

		def registerError(originalValue: K)(key: AnyRef, property: String, input: String, errorMessage: String,
						  errorsMap: IdentityHashMap[AnyRef, HashMap[String, Buffer[FieldError]]],
						  uniqueErrorId: String) {
			val errorsForKey = errorsMap.getOrElseUpdate(key, new HashMap[String, Buffer[FieldError]]())
			val errorsForField = errorsForKey.getOrElseUpdate(property, new ArrayBuffer[FieldError]())
			if (errorsForField.exists(_.errorMessage == errorMessage)) return
			val fieldError = if (errorsForField.isEmpty) {
				FieldError(property, originalValue, uniqueErrorId, errorMessage)
			} else {
				FieldError(property, originalValue, errorsForField(0).errorId, errorMessage)
			}
			errorsForField += fieldError
			trace("Registered errors so far: " + errorsMap)
		}

		private def getFormInputElement(implicit m: Manifest[T]): (Buffer[FieldError], NodeSeq, NodeSeq) = {
			val fieldErrors = getFieldErrors(bean, fieldName)
			val errorSeq = getErrorsSeq(fieldErrors)

			val _maxLength = maxLength or maxLengthOfFieldInChars // prefer local if explicitly set via withMaxLength()

			var inputSeq = getInputElementDrus(fieldErrors) %
				("id" -> inputId) %
				_maxLength.map(length => ("maxlength" -> length.toString): MetaData).getOrElse(Null)

			if (performInPlaceFieldValidation) {
				inputSeq = getOnEventValidation(inputSeq)
			}

			val textElement = addErrorClass(fieldErrors, inputSeq)
			(fieldErrors, textElement, errorSeq)
		}

		// This only does external (JSR-303, Oval or other), empty by default
		protected def doExternalFieldValidation(bean: AnyRef, fieldName: String, valueToValidate: T, originalValue: K) {
		}

		final private[lib] def doFieldValidation(bean: AnyRef, fieldName: String, valueToValidate: T, inputValue: K) {
			if (!(additionalValidationFunc.isDefined && additionalErrorMessageFunc.isDefined ||
				additionalValidationFunc.isEmpty && additionalErrorMessageFunc.isEmpty)) {
				throw new IllegalArgumentException("additionalValidationFunc AND additionalErrorMessageFunc must be both Full or both Empty")
			}
			doExternalFieldValidation(bean, fieldName, valueToValidate, inputValue)
			val tmpFieldErrors = getFieldErrors(bean, fieldName)
			if (tmpFieldErrors.isEmpty) {
				additionalValidationFunc.foreach(f => {
					if (!f(valueToValidate)) {
						additionalErrorMessageFunc.foreach(emFunc =>
							registerError(inputValue)(bean, fieldName, defaultReadOnlyValueFunc(inputValue),
								emFunc(inputValue), errorsMap, nextFuncName))
					}
				})
			} else {
				if (log.isTraceEnabled && additionalValidationFunc.isDefined) {
					trace("tmpFieldErrors: " + tmpFieldErrors)
				}
			}
		}

		protected def getInputElementDrus(fieldErrors: Buffer[FieldError])(implicit m: Manifest[T]): Elem

		protected def getInputElement(value: K, func: K => Any): Elem

		protected def getOnEventValidation(inputSeq: Elem)(implicit m: Manifest[T]): Elem

		val inputId = nextFuncName
		val containerId = nextFuncName

		private[this] lazy val (fieldErrors, textElement, errorSeq) = getFormInputElement(manifest)

		var script = NodeSeq.Empty

		def toForm = container.toForm

		def manifest: Manifest[T]

		def bean: A

		def fieldName: String

		def defaultValue: K

		def getFormFieldErrors: Buffer[FieldError] = fieldErrors

		final def getInputFieldElement: NodeSeq = textElement

		def getFormFieldElement: NodeSeq = getInputFieldElement

		final def getFormField: NodeSeq = getFormFieldElement ++ script

		def getFormFieldErrorSeq: NodeSeq = errorSeq

		protected def defaultReadOnlyValueFunc(value: K): String

		final def defaultReadOnlyValue: String = {
			defaultReadOnlyValueFunc(defaultValue)
		}

		def assignmentCallback: T => Any

		def isMandatory = mandatory

		protected def maxLengthOfFieldInChars: Option[Long] = Empty

		protected var mandatory = false
		protected var maxLength: Box[Long] = Empty
		var container: InputContainer[T] = InlineInputContainer[T](this)
		var additionalValidationFunc: Box[T => Boolean] = Empty
		var additionalErrorMessageFunc: Box[K => String] = Empty
		var ajaxCallbackFunc: Box[T => JsCmd] = Empty
		protected var origAttrs: Seq[SHtml.ElemAttr] = Nil
		private[this] var attrs: Seq[SHtml.ElemAttr] = Nil
		var readOnlyFormatterFunc: Box[() => NodeSeq] = Empty
		private[this] var doPerformInPlaceFieldValidation = true

		def getAttributes = origAttrs ++ attrs

		def withContainer(container: InputContainer[T]): this.type = {
			container.formField = this
			this.container = container
			this
		}

		def withValidation(validationFunc: T => Boolean, errorMessageFunc: K => String): this.type = {
			this.additionalValidationFunc = Full(validationFunc)
			this.additionalErrorMessageFunc = Full(errorMessageFunc)
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

		def withReadOnlyFormatter(formatterFunc: NodeSeq): this.type = {
			readOnlyFormatterFunc = Full(() => formatterFunc)
			this
		}

		def withMaxLength(length: Long): this.type = {
			maxLength = Full(length)
			this
		}

		def disableInPlaceValidation(): this.type = {
			doPerformInPlaceFieldValidation = false
			this
		}

		def disable(): this.type = {
			withAttrs("disabled" -> "disabled")
			this
		}

	}

	abstract class AbstractFormField[A <: AnyRef, T](implicit m: Manifest[T]) {
		self: FormField[A, T] =>
		val manifest = m
	}

	trait StandardFormField[A <: AnyRef, T] {
		self: FormField[A, T] =>
	}

	object TextField {
		def apply[A <: AnyRef, T](bean: A, fieldName: String, defaultValue: String,
								  assignmentCallback: T => Any)(implicit m: Manifest[T]) = {
			val fn = fieldName
			new TextField[A, T](bean, defaultValue, assignmentCallback) with StandardFormField[A, T] {
				override val fieldName = fn
			}
		}
	}

	abstract class TextField[A <: AnyRef, T](val bean: A,
											 val defaultValue: TextFormField[A, T]#K,
											 val assignmentCallback: T => Any)(implicit m: Manifest[T])
		extends AbstractFormField[A, T] with TextFormField[A, T] {
		def getInputElement(value: String, func: String => Any): Elem = {
			SHtml.text(value, func, getAttributes: _*)
		}
	}

	abstract class DecimalField[A <: AnyRef](bean: A,
											 value: Option[java.math.BigDecimal],
											 assignmentCallback: java.math.BigDecimal => Any)(implicit m: Manifest[java.math.BigDecimal])
		extends TextField[A, java.math.BigDecimal](bean, value.map(formatBigDecimalNumber(_)).getOrElse(""), assignmentCallback) {

	}

	abstract class NaturalNumberField[A <: AnyRef](bean: A,
												   value: Option[java.lang.Integer],
												   assignmentCallback: java.lang.Integer => Any)(implicit m: Manifest[java.lang.Integer])
		extends TextField[A, java.lang.Integer](bean, value.map(formatWholeNumber(_)).getOrElse(""), assignmentCallback) {
		withReadOnlyFormatter(value.map(v => Text(formatWholeNumber(v))).getOrElse(""))
		withInputMask(NaturalNumberMask())
	}

	object PercentField {
		def apply[A <: AnyRef](bean: A,
							   fieldName: String,
							   value: Option[java.math.BigDecimal],
							   assignmentCallback: java.math.BigDecimal => Any) = {
			val fn = fieldName
			new PercentField[A](bean, value, assignmentCallback) with StandardFormField[A, java.math.BigDecimal] {
				override val fieldName = fn
			}
		}
	}

	abstract class PercentField[A <: AnyRef](bean: A,
											 value: Option[java.math.BigDecimal],
											 assignmentCallback: java.math.BigDecimal => Any)
		extends DecimalField[A](bean, value, assignmentCallback) {
		withReadOnlyFormatter(formatPercent(value))
		withInputMask(PercentMask())
	}


	object TextAreaField {
		def apply[A <: AnyRef, T](bean: A, fieldName: String, defaultValue: String,
								  assignmentCallback: T => Any)(implicit m: Manifest[T]) = {
			val fn = fieldName
			new TextAreaField[A, T](bean, defaultValue, assignmentCallback) with StandardFormField[A, T] {
				override val fieldName = fn
			}
		}
	}

	abstract class TextAreaField[A <: AnyRef, T](override val bean: A,
												 override val defaultValue: TextFormField[A, T]#K,
												 override val assignmentCallback: T => Any)(implicit m: Manifest[T])
		extends TextField[A, T](bean, defaultValue, assignmentCallback) with TextFormField[A, T] {
		override def getInputElement(value: String, func: String => Any): Elem = {
			SHtml.textarea(value, func, getAttributes: _*)
		}
	}

	object SelectField {
		def apply[A <: AnyRef, T](bean: A,
								  fieldName: String,
								  options: Seq[(T, List[SHtml.ElemAttr])],
								  defaultValue: T,
								  assignmentCallback: T => Any,
								  valueLabel: (T, Int) => String)(implicit m: Manifest[T]) = {
			val fn = fieldName
			new SelectField[A, T](bean, options, defaultValue, assignmentCallback, valueLabel) with StandardFormField[A, T] {
				override val fieldName = fn
			}
		}
	}

	object SelectFieldWithUnselectedOption {
		def apply[A <: AnyRef, T](bean: A,
								  fieldName: String,
								  options: Seq[(T, List[SHtml.ElemAttr])],
								  defaultValue: T,
								  assignmentCallback: T => Any,
								  valueLabel: (T, Int) => String)(implicit m: Manifest[T]) = {
			val fn = fieldName
			val unSelectedOption = (null.asInstanceOf[T], List[SHtml.ElemAttr]("disabled" -> "disabled"))
			val allOptions = unSelectedOption +: options
			val valueLabelWithDefaultValue = (option: T, idx: Int) => if (idx == 0) L(GlobalTexts.select_noItemSelected) else valueLabel(option, idx)
			new SelectField[A, T](bean, allOptions, defaultValue, assignmentCallback, valueLabelWithDefaultValue) with StandardFormField[A, T] {
				override val fieldName = fn
			}
		}
	}

	abstract class SelectField[A <: AnyRef, T](val bean: A,
											   options: Seq[(T, List[SHtml.ElemAttr])],
											   val defaultValue: T,
											   val assignmentCallback: T => Any,
											   valueLabel: (T, Int) => String)(implicit m: Manifest[T])
		extends AbstractFormField[A, T] with PicableFormField[A, T] {


		def defaultReadOnlyValueFunc(value: T): String = defaultValue match {
			case null => null
			case _ => valueLabel(defaultValue, options.map(_._1).indexOf(defaultValue))
		}

		val allOptions: Seq[(T, String, List[SHtml.ElemAttr])] = options.zipWithIndex.map {
			case ((t, oattrs), idx) => (t, valueLabel(t, idx), oattrs)
		}.toList

		val secure = allOptions.map {
			case (obj, txt, oattrs) => (obj, randomString(20), txt, oattrs)
		}

		val (nonces, defaultNonce, secureOnSubmit) =
			secureOptionsWithDrus(secure, Full(defaultValue), (selectedItem: T) => {
				assignmentCallback(selectedItem)
				doFieldValidation(bean, fieldName, selectedItem, selectedItem)
			})

		def getInputElement(defaultValue: T, assignmentCallback: (T) => Any): Elem = {
			val inputSeq = ritchSelect_*(nonces, defaultNonce, secureOnSubmit, getAttributes: _*)
			inputSeq
		}

		private def onEventForSelect(secure: Seq[(T, String, String)], bean: AnyRef, fieldName: String, assignmentCallback: (T) => Any,
									 inputId: String, containerId: String, onSelectAjaxCallback: Box[T => JsCmd] = Empty)(implicit m: Manifest[T]) = {
			SHtml.onEvent((s) => {
				cleanErrorsForProperty(bean, fieldName) // Important to clear errors for this field in case previous action was "submit" on form
				secure.find(_._2 == s).map {
					x =>
						val selectedItem = x._1
						assignmentCallback(selectedItem)
						doFieldValidation(bean, fieldName, selectedItem, selectedItem)
						val fieldErrors = getFieldErrors(bean, fieldName)
						val errorSeq: NodeSeq = getErrorsSeq(fieldErrors)
						cleanErrorsForProperty(bean, fieldName)
						if (fieldErrors.isEmpty) {
							RemoveFieldError(containerId, inputId) & onSelectAjaxCallback.map(f => f(selectedItem)).openOr(Noop)
						} else {
							AttachFieldError(containerId, inputId, errorSeq) & onSelectAjaxCallback.map(f => f(selectedItem)).openOr(Noop)
						}
				}.getOrElse(Noop)
			})._2.toJsCmd
		}

		protected final def getOnEventValidation(inputSeq: Elem)(implicit m: Manifest[T]): Elem = {
			val oldOnChangeAttribute = inputSeq.attribute("onchange")
			val validationOnChange = onEventForSelect(secure.map {
				case (obj, nonce, txt, oattrs) => (obj, nonce, txt)
			},
				bean, fieldName, assignmentCallback,
				inputId, containerId, ajaxCallbackFunc)
			if (oldOnChangeAttribute.isDefined) {
				val oldOnChange = oldOnChangeAttribute.get.text
				if (oldOnChange.trim().length() > 0) {
					inputSeq % ("onchange" -> ("if (" + getAsAnonFunc(oldOnChange) + " !== false) {" + validationOnChange + "}"))
				} else inputSeq
			} else {
				inputSeq % ("onchange" -> validationOnChange)
			}
		}

		private def ritchSelect_*(opts: Seq[(String, String, List[SHtml.ElemAttr])], deflt: Box[String],
								  func: AFuncHolder, attrs: SHtml.ElemAttr*): Elem = {
			def selected(in: Boolean) = if (in) new UnprefixedAttribute("selected", "selected", Null) else Null
			val vals = opts.map(_._1)
			val testFunc = LFuncHolder(in => in.filter(v => vals.contains(v)) match {
				case Nil => false
				case xs => func(xs)
			}, func.owner)

			attrs.foldLeft(S.fmapFunc(testFunc)(fn => <select name={fn}>
				{opts.flatMap {
					case (value, text, optAttrs) =>
						optAttrs.foldLeft(<option value={value}>
							{text}
						</option>)(_ % _) % selected(deflt.exists(_ == value))
				}}
			</select>))(_ % _)
		}

		private def secureOptionsWithDrus(secure: Seq[(T, String, String, List[SHtml.ElemAttr])], default: Box[T],
										  onSubmit: T => Any): (Seq[(String, String, List[SHtml.ElemAttr])], Box[String], AFuncHolder) = {
			val defaultNonce = default.flatMap(d => secure.find(_._1 == d).map(_._2))
			val nonces = secure.map {
				case (obj, nonce, txt, oattrs) => (nonce, txt, oattrs)
			}
			def process(nonce: String) {
				secure.find(_._2 == nonce).map(x => onSubmit(x._1))
			}
			(nonces, defaultNonce, S.SFuncHolder(process))
		}

	}

	abstract class DateField[A <: AnyRef, T](bean: A,
											 defaultValue: String,
											 assignmentCallback: T => Any)(implicit m: Manifest[T])
		extends TextField[A, T](bean, defaultValue, assignmentCallback) {

		script = script ++ Script(Call("Rolf.DateSetup.setupDatePicker", inputId))

		override def getFormFieldElement: NodeSeq = {
			<span class={cssClass.mkString(" ")}>
				{getInputFieldElement}<span class="calendarButton"/>
			</span>
		}

		origAttrs = origAttrs ++ Set(SHtml.BasicElemAttr("class", "date"), SHtml.BasicElemAttr("onblur", "return Rolf.DateSetup.fixDate(this)"))

		private var cssClass: Seq[String] = Seq("datePicker")

		override def disable(): this.type = {
			super.disable()
			cssClass = cssClass :+ "disabled"
			this
		}
	}

	object DateField {
		def apply[A <: AnyRef, T](bean: A, fieldName: String, defaultValue: String,
								  assignmentCallback: T => Any)(implicit m: Manifest[T]) = {
			val fn = fieldName
			new DateField[A, T](bean, defaultValue, assignmentCallback) with StandardFormField[A, T] {
				override val fieldName = fn
			}
		}
	}

	final case class StrFuncElemAttr[T](name: String, value: (T) => String) extends SHtml.ElemAttr {
		/**
		 * Apply the attribute to the element
		 */
		def apply(in: Elem): Elem = in % (name -> ((s: T) => value(s)))

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
				val items = possible.zipWithIndex.map {
					case ((id, value), idx) => {
						val radio =
							attrs.foldLeft(<input type="radio"
												  name={name} value={id}/>) {
								(b, a) =>
									b % (if (a.isInstanceOf[StrFuncElemAttr[T]]) {
										a.asInstanceOf[StrFuncElemAttr[T]].apply(value)
									} else {
										a
									})
							} %
								checked(deflt.filter(_ == value).isDefined)

						val elem = if (idx == 0) {
							radio ++ <input type="hidden" value={hiddenId} name={name}/>
						} else {
							radio
						}

						ChoiceItem(value, elem)
					}
				}

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

	private[lib] def addErrorClass(fieldErrors: Buffer[FieldError], block: => NodeSeq) = {
		("input [class+]" #> Full("value_error").filter(s => !fieldErrors.isEmpty) &
			"textarea [class+]" #> Full("value_error").filter(s => !fieldErrors.isEmpty) &
			"select [class+]" #> Full("value_error").filter(s => !fieldErrors.isEmpty))(block)
	}

	private[lib] def cleanErrorsForProperty(key: AnyRef, property: String) {
		errorsMap.get(key).map(_ -= property).map(m => if (m.isEmpty) errorsMap -= key)
	}

	private[lib] def getErrorsSeq(fieldErrors: Buffer[FieldError]) = {
		if (!fieldErrors.isEmpty) {
			<div class="errorContainer">
				{fieldErrors.map {
				fieldError =>
					trace("Displaying error for fieldName '" + fieldError.fieldName + "' error-id: " + fieldError.errorId + " " +
						"input-string: " + fieldError.errorValue)
					<div>
						{fieldError.errorMessage}
					</div>
			}}
			</div>
		} else {
			NodeSeq.Empty
		}
	}

}
