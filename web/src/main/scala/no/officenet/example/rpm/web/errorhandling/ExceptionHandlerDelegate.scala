package no.officenet.example.rpm.web.errorhandling

import org.apache.commons.logging.Log
import java.lang.reflect.InvocationTargetException
import no.officenet.example.rpm.support.infrastructure.errorhandling.{Localizable, ApplicationException, SystemException,
InternalErrorException}
import xml.NodeSeq
import no.officenet.example.rpm.support.domain.i18n.Localizer._
import collection.JavaConversions.iterableAsScalaIterable
import no.officenet.example.rpm.support.domain.i18n.{Bundle, GlobalTexts}
import javax.validation.{ConstraintViolation, ConstraintViolationException}
import no.officenet.example.rpm.web.lib.ErrorDialog
import no.officenet.example.rpm.support.infrastructure.util.ResourceBundleHelper
import org.apache.commons.lang.exception.ExceptionUtils
import net.liftweb.http.{JavaScriptResponse, XhtmlResponse, LiftRules, S}
import no.officenet.example.rpm.support.infrastructure.logging.Loggable

object ExceptionHandlerDelegate extends Loggable {

	def constructValidationViolationMessage(cv: ConstraintViolation[_]): NodeSeq = {
		<li>
			<em>{L(Bundle.GLOBAL, "global_validation_rootBean_"+cv.getRootBeanClass.getName+"."+cv.getPropertyPath.toString)}</em>
			{cv.getMessage}
		</li>
	}

	def createValidationErrorDialog(c: ConstraintViolationException) = {
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
			val localizableEx = ExceptionHandlerDelegate.handleException(log, ex)
			if (req.acceptsJavaScript_? && req.section == LiftRules.ajaxPath) {
				ex match {
					case c: ConstraintViolationException =>
						JavaScriptResponse(ExceptionHandlerDelegate.createValidationErrorDialog(c).open)
					case _ =>
						JavaScriptResponse(ExceptionHandlerDelegate.createErrorDialog(localizableEx).open)
				}
			} else {
				XhtmlResponse(ExceptionHandlerDelegate.createHtmlErrorPage(req.uri, ex, localizableEx),
							  S.htmlProperties.docType, List("Content-Type" -> "text/html; charset=utf-8"), Nil, 500, S.ieMode)
			}
										   }
	}

	def handleException(LOG: Log, originalException: Throwable): Localizable = {
		var cause: Throwable = null
		if (originalException.isInstanceOf[InvocationTargetException] && originalException.getCause.isInstanceOf[Localizable]) {
			cause = originalException.getCause
		}
		else {
			cause = originalException
		}
		var exceptionToThrow: Throwable = cause
		val message: String = cause.getMessage
		if (!(isApplicationException(cause) || isSystemException(cause))) {
			LOG.error(message, cause)
			exceptionToThrow = new InternalErrorException(originalException)
		}
		if (isSystemException(cause)) {
			LOG.error(message, cause)
		}
		exceptionToThrow.asInstanceOf[Localizable]
	}

	private def isSystemException(e: Throwable): Boolean = {
		e.isInstanceOf[SystemException]
	}

	private def isApplicationException(e: Throwable): Boolean = {
		e.isInstanceOf[ApplicationException]
	}

}