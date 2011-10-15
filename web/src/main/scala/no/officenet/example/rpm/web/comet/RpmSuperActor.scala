package no.officenet.example.rpm.web.comet
import net.liftweb._
import common.{Box, Empty, CommonLoanWrapper}
import http._
import util.LoanWrapper
import net.liftweb.util.ControlHelpers.tryo
import org.springframework.transaction.support.TransactionSynchronizationManager
import org.springframework.dao.DataAccessResourceFailureException
import org.springframework.orm.jpa.{EntityManagerFactoryUtils, EntityManagerHolder}
import org.springframework.context.i18n.LocaleContextHolder
import javax.annotation.Resource
import javax.persistence.{EntityManagerFactory, PersistenceException}
import org.springframework.security.web.context.HttpSessionSecurityContextRepository
import org.springframework.security.core.context.{SecurityContext, SecurityContextHolder}
import org.springframework.security.core.Authentication
import no.officenet.example.rpm.support.infrastructure.logging.Loggable
import no.officenet.example.rpm.web.lib.UrlLocalizer
import javax.validation.ConstraintViolationException
import no.officenet.example.rpm.web.errorhandling.ExceptionHandlerDelegate

// Note: implementations must be @Configurable
trait RpmSuperActor extends CometActor with CometListener with Loggable {

	// First part of name is always the locale
	lazy val nameParts = name.open_!.split(":")

	@Resource(name = "RPM")
	private val emf: EntityManagerFactory = null

	lazy val locale = UrlLocalizer.locales.get(nameParts(0)) // Get from 1st part of the actor's name

	var authentication: Box[Authentication] = Empty

	override protected def localSetup() {
		super.localSetup()
		authentication = S.session.flatMap(ls => {
			ls.httpSession.flatMap(session => {
				tryo{session.attribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY).asInstanceOf[SecurityContext].getAuthentication}
			})
		})
	}

	override protected def aroundLoans: List[CommonLoanWrapper] = {
		val lw = new LoanWrapper  {
			def apply[T](f: => T) : T =  {
				trace("********* aroundLoans: Setting locale to: " + locale)
				locale.foreach{l =>
					LocaleContextHolder.setLocale(l)
							  }
				authentication.foreach(auth => SecurityContextHolder.getContext.setAuthentication(auth))
				var participate = false
				if (TransactionSynchronizationManager.hasResource(emf)) {
					// Do not modify the EntityManager: just set the participate flag.
					participate = true;
				} else {
					try {
						TransactionSynchronizationManager.bindResource(emf, new EntityManagerHolder(emf.createEntityManager()));
					}
					catch {
						case ex: PersistenceException => throw new DataAccessResourceFailureException("Could not create JPA EntityManager", ex);
					}
				}

				try {f} finally {
					if (!participate) {
						closeEntityManager(emf)
					}
					LocaleContextHolder.resetLocaleContext()
					SecurityContextHolder.getContext.setAuthentication(null)
				}
			}
		}
		lw :: Nil
	}

	private def closeEntityManager(emf: EntityManagerFactory) {
		val emHolder = TransactionSynchronizationManager.unbindResource(emf).asInstanceOf[EntityManagerHolder]
		EntityManagerFactoryUtils.closeEntityManager(emHolder.getEntityManager)
	}

	override protected def exceptionHandler = {
		case c: ConstraintViolationException =>
			partialUpdate(ExceptionHandlerDelegate.createValidationErrorDialog(c).open)

		case ex =>
			val localizableEx = ExceptionHandlerDelegate.handleException(log, ex)
			partialUpdate(ExceptionHandlerDelegate.createErrorDialog(localizableEx).open)

	}

}