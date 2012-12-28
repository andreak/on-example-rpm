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
import org.joda.time.{LocalDate, DateTime}
import no.officenet.example.rpm.support.infrastructure.i18n.Localizer.L
import no.officenet.example.rpm.support.infrastructure.i18n.DateFormatter._
import no.officenet.example.rpm.support.infrastructure.i18n.NumberFormatter._
import no.officenet.example.rpm.support.infrastructure.scala.lang.IdentityHashMap
import no.officenet.example.rpm.web.lib.LiftUtils._
import no.officenet.example.rpm.web.lib.RolfJsCmds.{AttachFieldError, RemoveFieldError}
import collection.mutable.{ArrayBuffer, HashMap, Buffer}
import no.officenet.example.rpm.support.infrastructure.logging.Loggable
import no.officenet.example.rpm.support.infrastructure.errorhandling.{InvalidDateInputException, FieldError}
import no.officenet.example.rpm.support.infrastructure.i18n.{InputStringConverter, GlobalTexts}

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
	private[this] def renderAjaxForm (kids: NodeSeq) : Elem = {
		if (kids.length == 1 &&
			kids(0).isInstanceOf[Elem] &&
			(kids(0).prefix eq null) &&
			kids(0).label == "form") {
			new Elem(null, "form", addAjaxForm(), TopScope, kids(0).child :_*)
		} else {
			Elem(null, "form", addAjaxForm(), TopScope, kids : _*)
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
				new Elem(null, "form", meta , e.scope, e.child :_*)
			} else {
				<form method="post" action={S.uri}>{kids}</form>
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

	implicit def formField2NodeSeq(field: FormField[_,_]): NodeSeq = field.toForm

	object Label {
		def apply(l: => NodeSeq) = new Label {
			val label = () => l
    	}
	}

	trait InputContainerWithLabel[T] extends InputContainer[T] with Label {
	}

	trait InputContainer[T] {
		var formField: FormField[_,T] = null
		var isEditMode: () => Boolean = () => true
		var readonlyCssClasses: Box[String] = Empty

		protected def getReadOnlyText: NodeSeq = {
			if(formField.readOnlyFormatterFunc.isDefined){
				return formField.readOnlyFormatterFunc.get.apply()
			}
			formField.defaultReadOnlyValue match {
				case None => NodeSeq.Empty
				case Some(v) => Text(v)
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

	class InlineInputContainer[T](_formField: FormField[_,T]) extends InputContainer[T] {
		formField = _formField

		def toForm: NodeSeq = {
			(".inputContainer [class+]" #> cssClasses)
				.apply(
				<div style="display: inline-block" id={formField.containerId} class="inputContainer">
					{
					if (isEditMode()) {
						formField.getFormField ++ mandatoryIcon(formField.isMandatory) ++ formField.getFormFieldErrorSeq
					} else {
						getReadOnlyText
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
						formField.getFormField ++ mandatoryIcon(formField.isMandatory) ++ formField.getFormFieldErrorSeq
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

		def defaultReadOnlyValueFunc(k: K): Option[String] = Some(k)

		private[this] def convertValue(inputValueOpt: Option[String], bean: AnyRef, fieldName: String)
									  (implicit m: Manifest[T]): Box[T] = {
			inputValueOpt.filterNot(_.isEmpty) match {
				case None => Empty
				case Some(inputValue) =>
					val newValue = inputValue.trim
					try {
						val convertedValue: T = InputStringConverter.convert[T](newValue)
						trace("S.locale: " + S.locale + ", dateformat_fullDate: " + L(GlobalTexts.dateformat_fullDate))
						Full(convertedValue)
					} catch {
						case e @ (_ : NumberFormatException | _: java.text.ParseException) =>
							registerError(Some(newValue))(bean, fieldName,
								L(GlobalTexts.validation_notANumber_number_text, inputValue), errorsMap, nextFuncName)
							Failure(inputValue)
						case e: InvalidDateInputException =>
							registerError(Some(newValue))(bean, fieldName,
								L(GlobalTexts.validation_invalidDate_text, inputValue), errorsMap, nextFuncName)
							Failure(inputValue)
					}
			}
		}

		protected def convertAndValidateInput(inputValue: Option[String], bean: A, fieldName: String,
											  assignmentCallback: Option[T] => Any)(implicit m: Manifest[T]): Box[T] = {
			convertValue(inputValue, bean, fieldName) match {
				case r@Failure(_,_,_) =>
					r
				case r =>
//					trace("Validating field: " + fieldName + " with value '" + convertedValue + "'")
					if (doFieldValidation(bean, fieldName, r, inputValue)) {
						assignmentCallback(r)
						doExternalFieldValidation(bean, fieldName, r, inputValue)
					} else if (skipValidationFunc.isDefined && skipValidationFunc.get.apply(r)) {
						// Run assignment-callback if we're instructed to skip validation
						assignmentCallback(r)
					}
					r
			}
		}

		protected final def getInputElementDrus(fieldErrors: Buffer[FieldError])(implicit m: Manifest[T]): Elem = {
			val func: (Option[String]) => Any = (stringOpt) => {
				convertAndValidateInput(stringOpt, bean, fieldName, assignmentCallback)
			}
			val inputSeq = getInputElement(tryo {fieldErrors(0).errorValue.map(_.toString).getOrElse("")}.or(defaultValue), func)
			inputSeq
		}

		protected def onBlurForTextInput(bean: A, fieldName: String, func: (Option[T]) => Any,
											ajaxCallbackFunc: Box[Box[T] => JsCmd] = Empty,
											inputId: String, containerId: String)(implicit m: Manifest[T]): String = {
			SHtml.onEvent((s) => {
				cleanErrorsForProperty(bean, fieldName) // Important to clear errors for this field in case previous action was "submit" on form
				val convertedValue: Box[T] = convertAndValidateInput(Option(s).filterNot(_.isEmpty), bean, fieldName, func)
				val fieldErrors = getFieldErrors(bean, fieldName)
				val errorSeq: NodeSeq = getErrorsSeq(fieldErrors)
				cleanErrorsForProperty(bean, fieldName)
				val errorHandlerCmd = if (fieldErrors.isEmpty) {
					RemoveFieldError(containerId, inputId)
				} else {
					AttachFieldError(containerId, inputId, errorSeq)
				}
				val extraCmd: JsCmd = convertedValue match {
					case Full(d:java.util.Date) => JsRaw("$(%s).value=%s".format(inputId.encJs, formatFullDate(d).encJs))
					case Full(d:DateTime) => JsRaw("$(%s).value=%s".format(inputId.encJs, formatFullDate(d.toDate).encJs))
					case Full(d:LocalDate) => JsRaw("$(%s).value=%s".format(inputId.encJs, formatFullDate(d.toDateTimeAtStartOfDay.toDate).encJs))
					case _ => Noop
				}
				val ajaxCallbackFuncResult: JsCmd = convertedValue match {
					case Failure(_,_,_) => Noop
					case r@(Full(_) | Empty) => ajaxCallbackFunc.map(f => f(r)).openOr(Noop)
				}
				errorHandlerCmd & extraCmd & ajaxCallbackFuncResult
			})._2.toJsCmd
		}

		private[this] def getConversionOnBlurForTextInput(bean: AnyRef, fieldName: String, func: Option[T] => Any,
														  ajaxCallbackFunc: Box[Box[T] => JsCmd] = Empty,
														  inputId: String, containerId: String)(implicit m: Manifest[T]): String = {
			SHtml.onEvent((s) => {
				cleanErrorsForProperty(bean, fieldName) // Important to clear errors for this field in case previous action was "submit" on form
				val convertedValue: Box[T] = convertValue(Option(s), bean, fieldName)
				val fieldErrors = getFieldErrors(bean, fieldName)
				val errorSeq: NodeSeq = getErrorsSeq(fieldErrors)
				cleanErrorsForProperty(bean, fieldName)
				val errorHandlerCmd = if (fieldErrors.isEmpty) {
					RemoveFieldError(containerId, inputId)
				} else {
					AttachFieldError(containerId, inputId, errorSeq)
				}
				val extraCmd: JsCmd = convertedValue match {
					case Full(d:java.util.Date) => JsRaw("$(%s).value=%s".format(inputId.encJs, formatFullDate(d).encJs))
					case Full(d:DateTime) => JsRaw("$(%s).value=%s".format(inputId.encJs, formatFullDate(d.toDate).encJs))
					case Full(d:LocalDate) => JsRaw("$(%s).value=%s".format(inputId.encJs, formatFullDate(d.toDateTimeAtStartOfDay.toDate).encJs))
					case _ => Noop
				}
				val ajaxCallbackFuncResult: JsCmd = convertedValue match {
					case Failure(_,_,_) => Noop
					case r@(Full(_) | Empty) => ajaxCallbackFunc.map(f => f(r)).openOr(Noop)
				}
				errorHandlerCmd & extraCmd & ajaxCallbackFuncResult
			})._2.toJsCmd
		}

		// For text-inputs we do full validation if onChange is specified
		protected final def getOnChange(inputSeq: Elem, convertIfStringInput: Boolean)(implicit m: Manifest[T]): Elem = {
			if (convertIfStringInput || ajaxCallbackFunc.isDefined) {
				// Also call this if ajaxCallbackFunc exists
				getConversionErrors(inputSeq)
			} else {
				// If no conversion needed or no ajaxCallbackFunc exists, no need to attach onblur
				inputSeq
			}
		}

		def getFiskebolle(inputSeq: Elem, validationOnBlur: String): Elem = {
			val oldOnBlurAttribute = inputSeq.attribute("onblur")
			if (oldOnBlurAttribute.isDefined) {
				val oldOnBlur = oldOnBlurAttribute.get.text
				if (oldOnBlur.trim().length() > 0) {
					inputSeq % ("onblur" -> ("if (" + getAsAnonFunc(oldOnBlur) + " !== false) {" + validationOnBlur + "}"))
				} else inputSeq
			} else {
				inputSeq % ("onblur" -> validationOnBlur)
			}
		}

		private[this] def getConversionErrors(inputSeq: Elem)(implicit m: Manifest[T]): Elem = {
			val conversionOnBlur = getConversionOnBlurForTextInput(bean, fieldName, assignmentCallback,
				ajaxCallbackFunc, inputId, containerId)
			getFiskebolle(inputSeq, conversionOnBlur)
		}

		protected final def getOnEventValidation(inputSeq: Elem)(implicit m: Manifest[T]): Elem = {
			val validationOnBlur = onBlurForTextInput(bean, fieldName, assignmentCallback,
				ajaxCallbackFunc, inputId, containerId)
			getFiskebolle(inputSeq, validationOnBlur)
		}

		def withInputMask(mask: InputMask): this.type = {
			this.inputMask = Full(mask)
			this
		}

		override protected final def convertIfStringInput(m: Manifest[T]): Boolean = {
			// If it's anything other than String we need to convert and would like to get conversion-errors
			!InputStringConverter.isTypeOrOptionOfType(classOf[String], m)
		}
	}

	trait PicableFormField[A <: AnyRef, T] extends FormField[A, T] {
		type K = T

		override protected final def getInputElementDrus(fieldErrors: Buffer[FieldError])(implicit m: Manifest[T]): Elem = {
			val inputSeq = getInputElement(defaultValue, assignmentCallback)
			inputSeq
		}

		override protected final def convertIfStringInput(m: Manifest[T]): Boolean = false
	}

	trait FormField[A <: AnyRef, T] {
		type K

		protected def convertIfStringInput(m: Manifest[T]): Boolean

		def hasExternalFieldValidation = false

		protected def getOnChange(inputSeq: Elem, convertIfStringInput: Boolean)(implicit m: Manifest[T]): Elem

		def performInPlaceFieldValidation: Boolean = {
			doPerformInPlaceFieldValidation && hasFieldValidation
		}

		def hasFieldValidation: Boolean = {
			(additionalValidationFunc.isDefined && additionalErrorMessageFunc.isDefined) || hasExternalFieldValidation
		}

		def registerError(originalValue: Option[K])(key: AnyRef, property: String, errorMessage: String,
						  errorsMap: IdentityHashMap[AnyRef, HashMap[String, Buffer[FieldError]]],
						  uniqueErrorId: String) {
			val errorsForKey = errorsMap.getOrElseUpdate(key, new HashMap[String, Buffer[FieldError]]())
			val errorsForField = errorsForKey.getOrElseUpdate(property, new ArrayBuffer[FieldError]())
			if(errorsForField.exists(_.errorMessage == errorMessage)) return
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

			for (mask <- inputMask) {
				mask.applyTo(this)
			}

			var inputSeq = getInputElementDrus(fieldErrors) %
						   ("id" -> inputId) %
						   _maxLength.map(length => ("maxlength" -> length.toString):MetaData).getOrElse(Null)

			if (performInPlaceFieldValidation) { // Enforce validation if not String to catch conversion-errors
				inputSeq = getOnEventValidation(inputSeq)
			} else  {
				inputSeq = getOnChange(inputSeq, convertIfStringInput(m))
			}

			val textElement = addErrorClass(fieldErrors, inputSeq)
			(fieldErrors, textElement, errorSeq)
		}

		// This only does external (JSR-303, Oval or other), empty by default
		protected def doExternalFieldValidation(bean: A, fieldName: String, valueToValidate: Option[T], originalValue: Option[K]) {
		}

		final private[lib] def doFieldValidation(bean: AnyRef, fieldName: String, valueToValidate: Option[T], inputValue: Option[K]): Boolean = {
			skipValidationFunc.map(f => !f(valueToValidate)).openOr(true) &&
				additionalValidationFunc.map(f => {
					if (!f(valueToValidate)) {
						for (emFunc <- additionalErrorMessageFunc) {
							registerError(inputValue)(bean, fieldName, emFunc(inputValue), errorsMap, nextFuncName)
						}
						return false
					} else true
				}).openOr(true)
		}

		protected def getInputElementDrus(fieldErrors: Buffer[FieldError])(implicit m: Manifest[T]): Elem

		protected def getInputElement(value: Option[K], func: Option[K] => Any): Elem

		protected def getOnEventValidation(inputSeq: Elem)(implicit m: Manifest[T]): Elem

		var _inputId: Box[String] = Empty

		def inputId: String = {
			_inputId.openOr {
				val newId = nextFuncName
				_inputId = Full(newId)
				newId
			}
		}

		var containerId = nextFuncName

		private[this] lazy val (fieldErrors, textElement, errorSeq) = getFormInputElement(manifest)

		var script = NodeSeq.Empty

		def toForm = container.toForm

		def manifest: Manifest[T]

		def bean: A

		def fieldName: String

		def defaultValue: Option[K]

		def getFormFieldErrors: Buffer[FieldError] = fieldErrors

		final def getInputFieldElement: NodeSeq = textElement

		def getFormFieldElement: NodeSeq = getInputFieldElement

		final def getFormField: NodeSeq = getFormFieldElement ++ script

		def getFormFieldErrorSeq: NodeSeq = errorSeq

		protected def defaultReadOnlyValueFunc(value: K): Option[String]

		final def defaultReadOnlyValue: Option[String] = {
			defaultValue.flatMap(defaultReadOnlyValueFunc(_))
		}

		def assignmentCallback: Option[T] => Any

		def isMandatory = mandatory

		protected[lib] def maxLengthOfFieldInChars(implicit m: Manifest[T]): Option[Long] = Empty

		protected var skipValidationFunc: Box[(Option[T]) => Boolean] = Empty
		protected var mandatory = false
		protected[lib] var maxLength: Box[Long] = Empty
		var container: InputContainer[T] = InlineInputContainer[T](this)
		private var additionalValidationFunc: Box[Option[T] => Boolean] = Empty
		private var additionalErrorMessageFunc: Box[Option[K] => String] = Empty
		var ajaxCallbackFunc: Box[Box[T] => JsCmd] = Empty
		protected var origAttrs: Seq[SHtml.ElemAttr] = Nil
		private[this] var attrs: Seq[SHtml.ElemAttr] = Nil
		var readOnlyFormatterFunc: Box[() => NodeSeq] = Empty
		private[this] var doPerformInPlaceFieldValidation = true

		var inputMask: Box[InputMask] = Empty

		def getAttributes = origAttrs ++ attrs

		def withContainer(container: InputContainer[T]): this.type = {
			container.formField = this
			this.container = container
			this
		}

		def withValidation(validationFunc: Option[T] => Boolean, errorMessageFunc: Option[K] => String): this.type = {
			if ((validationFunc eq null) || (errorMessageFunc eq null)) {
				throw new IllegalArgumentException("validationFunc AND errorMessageFunc cannot be null")
			}
			this.additionalValidationFunc = Full(validationFunc)
			this.additionalErrorMessageFunc = Full(errorMessageFunc)
			this
		}

		def withAjaxCallbackFunc(func: Box[T] => JsCmd): this.type = {
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

		def withSkipValidationFunc(func: Option[T] => Boolean): this.type = {
			skipValidationFunc = Full(func)
			this
		}

		def withInputId(id: String): this.type = {
			_inputId = Full(id)
			this
		}

	}

	abstract class AbstractFormField[A <: AnyRef, T](implicit m: Manifest[T]) {
		self: FormField[A, T]  =>
		val manifest = m
	}

	trait StandardFormField[A <: AnyRef, T] {
		self: FormField[A, T]  =>
	}

	object TextField {
		def apply[A <: AnyRef, T](bean: A, fieldName: String, defaultValue: Option[String],
								  assignmentCallback: Option[T] => Any)(implicit m: Manifest[T]) = {
			val fn = fieldName
			new TextField[A, T](bean, defaultValue, assignmentCallback) with StandardFormField[A, T] {
				override val fieldName = fn
			}
		}
		def apply[T](fieldName: String, defaultValue: Option[String],
								  assignmentCallback: Option[T] => Any)(implicit m: Manifest[T]) = {
			val fn = fieldName
			new TextField[this.type, T](this, defaultValue, assignmentCallback) with StandardFormField[this.type, T] {
				override val fieldName = fn
			}
		}
	}

	abstract class TextField[A <: AnyRef, T](val bean: A,
											 val defaultValue: Option[TextFormField[A, T]#K],
											 val assignmentCallback: Option[T] => Any)(implicit m: Manifest[T])
		extends AbstractFormField[A, T] with TextFormField[A, T] {
		override def getInputElement(value: Option[String], func: Option[String] => Any): Elem = {
			SHtml.text(value.getOrElse(""), s => func(if (s.isEmpty) None else Some(s)), getAttributes: _*)
		}
	}

	abstract class DecimalField[A <: AnyRef](bean: A,
									value: Option[java.math.BigDecimal],
									assignmentCallback: Option[java.math.BigDecimal] => Any)(implicit m: Manifest[java.math.BigDecimal])
		extends TextField[A, java.math.BigDecimal](bean, value.map(formatBigDecimalNumber(_)), assignmentCallback) {

	}

	abstract class NaturalNumberField[A <: AnyRef](bean: A,
									value: Option[java.lang.Integer],
									assignmentCallback: Option[java.lang.Integer] => Any)(implicit m: Manifest[java.lang.Integer])
		extends TextField[A, java.lang.Integer](bean, value.map(formatWholeNumber(_)), assignmentCallback) {
		withReadOnlyFormatter(value.map(v => Text(formatWholeNumber(v))).getOrElse(NodeSeq.Empty))
		withInputMask(NaturalNumberMask())
	}

	object PercentField {
		def apply[A <: AnyRef](bean: A,
							   fieldName: String,
							   value: Option[java.math.BigDecimal],
							   assignmentCallback: Option[java.math.BigDecimal] => Any) = {
			val fn = fieldName
			new PercentField[A](bean, value, assignmentCallback) with StandardFormField[A, java.math.BigDecimal] {
				override val fieldName = fn
			}
		}
	}

	abstract class PercentField[A <: AnyRef](bean: A,
									value: Option[java.math.BigDecimal],
									assignmentCallback: Option[java.math.BigDecimal] => Any)
		extends DecimalField[A](bean, value, assignmentCallback) {
		withReadOnlyFormatter(formatPercent(value).map(Text(_)).getOrElse(NodeSeq.Empty))
		withInputMask(PercentMask())
	}


	object TextAreaField {
		def apply[A <: AnyRef, T](bean: A, fieldName: String, defaultValue: Option[String],
								  assignmentCallback: Option[T] => Any)(implicit m: Manifest[T]) = {
			val fn = fieldName
			new TextAreaField[A, T](bean, defaultValue, assignmentCallback) with StandardFormField[A, T] {
				override val fieldName = fn
			}
		}
		def apply[T](fieldName: String, defaultValue: Option[String],
								  assignmentCallback: Option[T] => Any)(implicit m: Manifest[T]) = {
			val fn = fieldName
			new TextAreaField[this.type, T](this, defaultValue, assignmentCallback) with StandardFormField[this.type, T] {
				override val fieldName = fn
			}
		}
	}

	abstract class TextAreaField[A <: AnyRef, T](override val bean: A,
										override val defaultValue: Option[TextFormField[A, T]#K],
										override val assignmentCallback: Option[T] => Any)(implicit m: Manifest[T])
		extends TextField[A, T](bean, defaultValue, assignmentCallback) with TextFormField[A, T] {
		override def getInputElement(value: Option[String], func: Option[String] => Any): Elem = {
			SHtml.textarea(value.getOrElse(""), s => func(if (s.isEmpty) None else Some(s)), getAttributes: _*)
		}
	}

	object SelectField {
		def apply[A <: AnyRef, T](bean: A,
								  fieldName: String,
								  options: Seq[(T, List[SHtml.ElemAttr])],
								  defaultValue: Option[T],
								  assignmentCallback: T => Any,
								  valueLabel: (T, Int) => String)(implicit m: Manifest[T]) = {
			val fn = fieldName
			implicit val krafs = 0
			new SelectField[A, T](bean, options.map(t => Some(t._1) -> t._2), defaultValue, (o: Option[T]) => o.map(v => assignmentCallback(v)), valueLabel) with StandardFormField[A, T] {
				override val fieldName = fn
			}
		}
	}

	object SelectFieldWithUnselectedOption {
		def apply[A <: AnyRef, T](bean: A,
								  fieldName: String,
								  options: Seq[(T, List[SHtml.ElemAttr])],
								  defaultValue: Option[T],
								  assignmentCallback: Option[T] => Any,
								  valueLabel: (T, Int) => String, notSelectedText: String)(implicit m: Manifest[T]) = {
			val fn = fieldName
			val valueLabelWithDefaultValue = (option: Option[T], idx: Int) => option.map(value => valueLabel(value, idx)).getOrElse(notSelectedText)
			new SelectField[A, T](bean, options.map(t => Some(t._1) -> t._2), defaultValue, assignmentCallback,
				valueLabelWithDefaultValue, includeUnselectedOption = true) with StandardFormField[A, T] {
				override val fieldName = fn
			}
		}
		def apply[T](fieldName: String,
					 options: Seq[(T, List[SHtml.ElemAttr])],
					 defaultValue: Option[T],
					 assignmentCallback: Option[T] => Any,
					 valueLabel: (T, Int) => String, notSelectedText: String)(implicit m: Manifest[T]) = {
			val fn = fieldName
			val valueLabelWithDefaultValue = (option: Option[T], idx: Int) => option.map(value => valueLabel(value, idx)).getOrElse(notSelectedText)
			new SelectField[this.type, T](this, options.map(t => Some(t._1) -> t._2), defaultValue, assignmentCallback,
				valueLabelWithDefaultValue, includeUnselectedOption = true) with StandardFormField[this.type, T] {
				override val fieldName = fn
			}
		}
	}

	abstract class SelectField[A <: AnyRef, T](val bean: A,
									  options: Seq[(Option[T], List[SHtml.ElemAttr])],
									  val defaultValue: Option[T],
									  val assignmentCallback: Option[T] => Any,
									  valueLabel: (Option[T], Int) => String,
									  includeUnselectedOption: Boolean = false)(implicit m: Manifest[T])
		extends AbstractFormField[A, T] with PicableFormField[A, T] {

		def this(bean: A,
				 options: Seq[(Option[T], List[SHtml.ElemAttr])],
				 defaultValue: Option[T],
				 assignmentCallback: Option[T] => Any,
				 valueLabel: (T, Int) => String)(implicit m: Manifest[T], v: Int) { // The last 'v' is just to cope with type-erasure issues: Making this constructor's signature different
			this(bean, options, defaultValue, assignmentCallback, (t: Option[T], idx: Int) => t.map(v => valueLabel(v, idx)).getOrElse(""))
		}

		def defaultReadOnlyValueFunc(value: T): Option[String] = defaultValue match {
			case Some(dv) => Some(valueLabel(defaultValue, options.map(_._1).indexOf(defaultValue)))
			case _ => None
		}

		lazy val secure = {
			val allOptions = if (includeUnselectedOption) {
				(None, if (isMandatory) List[SHtml.ElemAttr]("disabled" -> "disabled") else Nil) +: options
			} else {
				options
			}
			allOptions.zipWithIndex.map{
				case ((valueOpt, oattrs), idx) => (valueOpt, valueLabel(valueOpt, idx), oattrs)
			}.map {case (obj, txt, oattrs) => (obj, randomString(20), txt, oattrs)}
		}

		override def getInputElement(defaultValue: Option[T], assignmentCallback: Option[T] => Any): Elem = {
			val (nonces, defaultNonce, secureOnSubmit) =
				secureOptionsWithDrus(secure, defaultValue, (selectedOpt: Option[T]) => {
					assignmentCallback(selectedOpt)
					if (doFieldValidation(bean, fieldName, selectedOpt, selectedOpt)) {
						doExternalFieldValidation(bean, fieldName, selectedOpt, selectedOpt)
					}
				})
			val inputSeq = ritchSelect_*(nonces, defaultNonce, secureOnSubmit, getAttributes: _*)
			inputSeq
		}

		private def onEventForSelect(secure: Seq[(Option[T], String, String)], bean: A, fieldName: String, assignmentCallback: Option[T] => Any,
													   inputId: String, containerId: String, onSelectAjaxCallback: Box[Box[T] => JsCmd] = Empty)(implicit m: Manifest[T]) = {
				SHtml.onEvent((s) => {
					cleanErrorsForProperty(bean, fieldName) // Important to clear errors for this field in case previous action was "submit" on form
					secure.find(_._2 == s).map{selected =>
						val selectedOpt = selected._1
						assignmentCallback(selectedOpt)
						if (doFieldValidation(bean, fieldName, selectedOpt, selectedOpt)) {
							doExternalFieldValidation(bean, fieldName, selectedOpt, selectedOpt)
						}
						val fieldErrors = getFieldErrors(bean, fieldName)
						val errorSeq: NodeSeq = getErrorsSeq(fieldErrors)
						cleanErrorsForProperty(bean, fieldName)
						if (fieldErrors.isEmpty) {
							RemoveFieldError(containerId, inputId) & onSelectAjaxCallback.map(f => f(selectedOpt)).openOr(Noop)
						} else {
							AttachFieldError(containerId, inputId, errorSeq) & onSelectAjaxCallback.map(f => f(selectedOpt)).openOr(Noop)
						}
					}.getOrElse(Noop)
				})._2.toJsCmd
			}

		private def onChangeForSelect(secure: Seq[(Option[T], String, String)], bean: AnyRef, fieldName: String, assignmentCallback: Option[T] => Any,
													   inputId: String, containerId: String, onSelectAjaxCallback: Box[Box[T] => JsCmd] = Empty)(implicit m: Manifest[T]) = {
				SHtml.onEvent((s) => {
					secure.find(_._2 == s).flatMap{x =>
							assignmentCallback(x._1)
							onSelectAjaxCallback.map(f => f(x._1))
					}.getOrElse(Noop)
				})._2.toJsCmd
		}

		override protected final def getOnChange(inputSeq: Elem, convertIfStringInput: Boolean)(implicit m: Manifest[T]): Elem = {
			val validationOnChange = onChangeForSelect(secure.map{case (obj, nonce, txt, oattrs) => (obj, nonce, txt)},
				bean, fieldName, assignmentCallback,
				inputId, containerId, ajaxCallbackFunc)
			onSnabelCallback(inputSeq, validationOnChange)
		}

		protected def onSnabelCallback(inputSeq: Elem, onChangeFunc: String): Elem = {
			val oldOnChangeAttribute = inputSeq.attribute("onchange")
			if (oldOnChangeAttribute.isDefined) {
				val oldOnChange = oldOnChangeAttribute.get.text
				if (oldOnChange.trim().length() > 0) {
					inputSeq % ("onchange" -> ("if (" + getAsAnonFunc(oldOnChange) + " !== false) {" + onChangeFunc + "}"))
				} else inputSeq
			} else {
				inputSeq % ("onchange" -> onChangeFunc)
			}
		}

		protected final def getOnEventValidation(inputSeq: Elem)(implicit m: Manifest[T]): Elem = {
			val validationOnChange = onEventForSelect(secure.map{case (obj, nonce, txt, oattrs) => (obj, nonce, txt)},
													  bean, fieldName, assignmentCallback,
													  inputId, containerId, ajaxCallbackFunc)
			onSnabelCallback(inputSeq, validationOnChange)
		}

		private def ritchSelect_*(opts: Seq[(String, String, List[SHtml.ElemAttr])], deflt: Box[String],
								  func: AFuncHolder, attrs: SHtml.ElemAttr*): Elem = {
			def selected(in: Boolean) = if (in) new UnprefixedAttribute("selected", "selected", Null) else Null
			val vals = opts.map(_._1)
			val testFunc = LFuncHolder(in => in.filter(v => vals.contains(v)) match {case Nil => false case xs => func(xs)}, func.owner)

			attrs.foldLeft(S.fmapFunc(testFunc)(fn => <select name={fn}>{opts.flatMap {case (value, text, optAttrs) =>
				optAttrs.foldLeft(<option value={value}>{text}</option>)(_ % _) % selected(deflt.exists(_ == value)) }}</select>))(_ % _)
		}

		private def secureOptionsWithDrus(secure: Seq[(Option[T], String, String, List[SHtml.ElemAttr])], default: Option[T],
											 onSubmit: Option[T] => Any): (Seq[(String, String, List[SHtml.ElemAttr])], Box[String], AFuncHolder) = {
			val defaultNonce = secure.find(_._1 == default).map(_._2)
			val nonces = secure.map {case (obj, nonce, txt, oattrs) => (nonce, txt, oattrs)}
			def process(nonce: String) {
				secure.find(_._2 == nonce).map(x => onSubmit(x._1))
			}
			(nonces, defaultNonce, S.SFuncHolder(process))
		}

	}

	abstract class DateField[A <: AnyRef, T](bean: A,
											 defaultValue: Option[String],
											 assignmentCallback: Option[T] => Any)(implicit m: Manifest[T])
		extends TextField[A,T](bean, defaultValue, assignmentCallback) {

		script = script ++ Script(Call("Rolf.DateSetup.setupDatePicker", inputId))

		override def getFormFieldElement: NodeSeq = {
			<span class={cssClass.mkString(" ")}>{getInputFieldElement}<span class="calendarButton"/></span>
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
		def apply[A <: AnyRef, T](bean: A, fieldName: String, defaultValue: Option[String],
								  assignmentCallback: Option[T] => Any)(implicit m: Manifest[T]) = {
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
		def apply(in: Elem): Elem = {
			val snipp = (s:T) => value(s)
			in % (name -> snipp)
		}

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
			<div class="errorContainer">{
				fieldErrors.map{fieldError =>
					trace("Displaying error for fieldName '" + fieldError.fieldName + "' error-id: " + fieldError.errorId + " " +
						  "input-string: " + fieldError.errorValue.getOrElse(""))
					<div>{fieldError.errorMessage}</div>
							   }
				}</div>
		} else {
			NodeSeq.Empty
		}
	}

}
