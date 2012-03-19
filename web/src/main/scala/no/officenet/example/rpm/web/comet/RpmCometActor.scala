package no.officenet.example.rpm.web.comet
import net.liftweb._
import common.{Full, Box, Empty, CommonLoanWrapper}
import http._
import js.JsCmd
import net.liftweb.util.ControlHelpers.tryo
import org.springframework.security.web.context.HttpSessionSecurityContextRepository
import org.springframework.security.core.Authentication
import no.officenet.example.rpm.support.infrastructure.logging.Loggable
import no.officenet.example.rpm.web.errorhandling.ExceptionHandlerDelegate
import no.officenet.example.rpm.support.infrastructure.i18n.GlobalTexts
import no.officenet.example.rpm.support.infrastructure.i18n.Localizer._
import no.officenet.example.rpm.web.lib.{ErrorDialog, UrlLocalizer}
import util.LoanWrapper
import xml.{NodeSeq, Text}
import no.officenet.example.rpm.support.infrastructure.errorhandling.RpmConstraintsViolatedException
import org.springframework.security.core.context.{SecurityContextHolder, SecurityContext}
import no.officenet.example.rpm.support.domain.model.entities.User
import no.officenet.example.rpm.support.domain.service.UserService
import javax.annotation.Resource

// Note: implementations must be @Configurable
trait RpmCometActor extends CometActor with CometListener with Loggable {

	@Resource
	val userService: UserService = null
	// First part of name is always the locale
	lazy val nameParts = name.open_!.split(":")

	lazy val locale = UrlLocalizer.locales.get(nameParts(0)) // Get from 1st part of the actor's name

	var authentication: Box[Authentication] = Empty

	lazy val loggedOnUser: Box[User] = authentication.flatMap(auth => userService.findByUserName(auth.getName))

	override protected def localSetup() {
		super.localSetup()
		authentication = S.session.flatMap(ls => {
			ls.httpSession.flatMap(session => {
				tryo{session.attribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY).asInstanceOf[SecurityContext].getAuthentication}
			})
		})
	}

	override protected def aroundLoans: List[CommonLoanWrapper] = {
		val lw = LoanWrapperHelper.getLoanWrapper(() => locale)
		val cometLW = new LoanWrapper  {
			def apply[T](f: => T) : T =  {
				authentication.foreach(auth => SecurityContextHolder.getContext.setAuthentication(auth))
				try {
					f
				}
				finally {
					SecurityContextHolder.clearContext()
				}
			}
		}
		cometLW :: lw :: Nil
	}

	override def cometRenderTimeoutHandler(): Box[NodeSeq] = {
		Full(<div>{L(GlobalTexts.error_comet_renderTimeOut, cometRenderTimeout)}</div>)
	}

	override def cometProcessingTimeoutHandler(): JsCmd = {
		ErrorDialog(L_!(GlobalTexts.exception_popup_header),
			Text(L(GlobalTexts.error_comet_processTimeOut, cometProcessingTimeout)),
			None).open
	}

	override protected def exceptionHandler = {
		case c: RpmConstraintsViolatedException =>
			partialUpdate(ExceptionHandlerDelegate.createValidationErrorDialog(c).open)

		case ex =>
			val localizableEx = ExceptionHandlerDelegate.handleException(log, ex)
			partialUpdate(ExceptionHandlerDelegate.createErrorDialog(localizableEx).open)
	}

}