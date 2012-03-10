package no.officenet.example.rpm.web.errorhandling

import org.apache.commons.logging.Log
import java.lang.reflect.InvocationTargetException
import xml.NodeSeq
import no.officenet.example.rpm.support.infrastructure.i18n.{GlobalTexts, ResourceBundleHelper}
import no.officenet.example.rpm.support.infrastructure.i18n.Localizer._
import no.officenet.example.rpm.web.lib.ErrorDialog
import org.apache.commons.lang.exception.ExceptionUtils
import net.liftweb.http.{JavaScriptResponse, XhtmlResponse, LiftRules, S}
import no.officenet.example.rpm.support.infrastructure.logging.Loggable
import no.officenet.example.rpm.support.infrastructure.errorhandling.{RpmConstraintsViolatedException, Localizable, ApplicationException, SystemException, InternalErrorException}
import net.sf.oval.ConstraintViolation
import no.officenet.example.rpm.support.infrastructure.scala.lang.ControlHelpers.?

object ExceptionHandlerDelegate extends Loggable {

	def constructValidationViolationMessage(cv: ConstraintViolation): NodeSeq = {
		<li>
			{cv.getMessage}
			{?(cv.getCauses).map(_.map(sv => <ul>{constructValidationViolationMessage(sv)}</ul>)).getOrElse(NodeSeq.Empty)}
		</li>
	}

	def createValidationErrorDialog(c: RpmConstraintsViolatedException) = {
		val title = L_!(GlobalTexts.validationViolation_popup_title)
		val message1: NodeSeq = L_!(GlobalTexts.validationViolation_popup_header)
		val message2: NodeSeq = <br/> ++ <ul>
			{c.getConstraintViolations.map(cv => constructValidationViolationMessage(cv))}
		</ul>
		ErrorDialog(title, message1, Some(message2))
	}

	def createErrorDialog(localizableEx: Localizable) = {
		val stackTrace: NodeSeq = (localizableEx match {
			case e: SystemException =>
				ExceptionHandlerDelegate.getShowHide(e.get)
			case _ => NodeSeq.Empty
		})
		val title = L_!(GlobalTexts.exception_popup_title)
		val message1: NodeSeq = L_!(GlobalTexts.exception_popup_header)
		val message2: NodeSeq = <br/> ++
								strToNodeSeq(ResourceBundleHelper.getMessage(localizableEx.getResourceBundleName,
																			 localizableEx.getResourceKey,
																			 localizableEx.getArguments:_*)) ++ stackTrace
		ErrorDialog(title, message1, Some(message2))
	}

	def createValidationErrorPage(uri: String,  originalException: Throwable, constraintViolatedEx: RpmConstraintsViolatedException) =  <html>
		<header>
			<title>
				{L(GlobalTexts.exception_page_title)}
			</title>
			<script src={LiftRules.attachResourceId(S.contextPath + "/resources/js/external/prototype.js")} type="text/javascript"></script>
				<link type="text/css" media="all" rel="stylesheet" href={LiftRules.attachResourceId(S.contextPath + "/resources/css/main.css")} />
		</header>
		<body>{L(GlobalTexts.exception_page_header, uri)}<br/>
			{constraintViolatedEx.getMessage}
				<br/>
			{getShowHide(originalException)}
		</body>
	</html>

	def createHtmlErrorPage(uri: String,  originalException: Throwable, localizableEx: Localizable) =  <html>
		<header>
			<title>
				{L(GlobalTexts.exception_page_title)}
			</title>
			<script src={LiftRules.attachResourceId(S.contextPath + "/resources/js/external/prototype.js")} type="text/javascript"></script>
				<link type="text/css" media="all" rel="stylesheet" href={LiftRules.attachResourceId(S.contextPath + "/resources/css/Rolf.css")} />
		</header>
		<body>{L(GlobalTexts.exception_page_header, uri)}<br/>
			{strToNodeSeq(ResourceBundleHelper.getMessage(localizableEx.getResourceBundleName, localizableEx.getResourceKey, localizableEx.getArguments:_*))}
			{ExceptionHandlerDelegate.getShowHide(originalException)}
		</body>
	</html>

	private def getShowHide(cause: Throwable) = {
		<div>
			<a class="exceptionLink" onclick="this.up().down('.exception').toggle()">{L(GlobalTexts.exception_showHideStackTrace_text)}</a><br/>
			<div class="exception" style="display: none">
				{L(GlobalTexts.exception_originalException_text)} <br/>
				<textarea wrap="virtual" spellcheck="false" readonly="true" style="width: 100%; height: 960px;">{
					ExceptionUtils.getStackTrace(cause)
					}
				</textarea>
			</div>
		</div>
	}

	def setUpLiftExceptionHandler() {
		LiftRules.exceptionHandler.prepend {case (runMode, req, ex) =>
			ex match {
				case c: RpmConstraintsViolatedException =>
					if (req.acceptsJavaScript_? && req.section == LiftRules.ajaxPath) {
						JavaScriptResponse(createValidationErrorDialog(c).open)
					} else {
						XhtmlResponse(createValidationErrorPage(req.uri, ex, c),
							S.htmlProperties.docType, List("Content-Type" -> "text/html; charset=utf-8"), Nil, 500, S.ieMode)
					}
				case _ =>
					val localizableEx = handleException(log, ex)
					if (req.acceptsJavaScript_? && req.section == LiftRules.ajaxPath) {
						JavaScriptResponse(createErrorDialog(localizableEx).open)
					} else {
						XhtmlResponse(createHtmlErrorPage(req.uri, ex, localizableEx),
							S.htmlProperties.docType, List("Content-Type" -> "text/html; charset=utf-8"), Nil, 500, S.ieMode)
					}
			}
		}
	}

	def handleException(LOG: Log, originalException: Throwable): Localizable = {
		val cause: Throwable =
			if (originalException.isInstanceOf[InvocationTargetException] && originalException.getCause.isInstanceOf[Localizable]) {
				originalException.getCause
			}
			else {
				originalException
			}

		val message: String = cause.getMessage

		val exceptionToThrow: Localizable =
			if (!(isApplicationException(cause) || isSystemException(cause))) {
				LOG.error(message, cause)
				new InternalErrorException(originalException)
			} else {
				cause match {
					case e: Localizable => e
					case e =>
						val name: String = e match {
							case a: ApplicationException => classOf[ApplicationException].getSimpleName
							case s: SystemException => classOf[SystemException].getSimpleName
							case _ => throw new IllegalStateException("Must be " +
								classOf[ApplicationException].getSimpleName + " or " +
								classOf[SystemException].getSimpleName + " but was: " + e.getClass.getName,
								e)
						}
						throw new IllegalStateException(name + "(" + e.getClass.getName + ") is not of type " +
							(classOf[Localizable].getSimpleName) + " but: " + cause.getClass.getSimpleName, cause)
				}
			}

		if (isSystemException(cause)) {
			LOG.error(message, cause)
		}
		exceptionToThrow
	}

	private def isSystemException(e: Throwable): Boolean = {
		e.isInstanceOf[SystemException]
	}

	private def isApplicationException(e: Throwable): Boolean = {
		e.isInstanceOf[ApplicationException]
	}

}