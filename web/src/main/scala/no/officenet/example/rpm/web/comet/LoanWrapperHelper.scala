package no.officenet.example.rpm.web.comet

import org.springframework.transaction.support.TransactionSynchronizationManager
import org.springframework.orm.jpa.{EntityManagerFactoryUtils, EntityManagerHolder}
import net.liftweb.util.LoanWrapper
import org.springframework.dao.DataAccessResourceFailureException
import org.apache.commons.lang.exception.ExceptionUtils
import java.util.Locale
import org.springframework.context.i18n.LocaleContextHolder
import javax.persistence.{PersistenceException, EntityManager, EntityManagerFactory}
import org.springframework.beans.factory.annotation.Configurable
import javax.annotation.Resource
import no.officenet.example.rpm.support.infrastructure.logging.Loggable

@Configurable
object LoanWrapperHelper extends Loggable {

	@Resource
	private val entityManagerFactory: EntityManagerFactory = null

	def getLoanWrapper(getLocale: () => Option[Locale]): LoanWrapper = {
		val lw = new LoanWrapper {
			def apply[T](f: => T): T = try {
				for (locale <- getLocale()) {
					LocaleContextHolder.setLocale(locale)
				}

				var participate = false
				if (TransactionSynchronizationManager.hasResource(entityManagerFactory)) {
					// Do not modify the EntityManager: just set the participate flag.
					participate = true;
				} else {
					try {
						val em = createEntityManager();
						TransactionSynchronizationManager.bindResource(entityManagerFactory, new EntityManagerHolder(em));
					}
					catch {
						case ex: PersistenceException => throw new DataAccessResourceFailureException("Could not create JPA EntityManager", ex);
					}
				}

				try {
					f
				}
				finally {
					if (!participate) {
						closeEntityManager()
					}
					LocaleContextHolder.resetLocaleContext()
				}
			} catch {
				case e =>
					debug("LoanWrapperHelper: " + e.getMessage, e)
					throw e
			}
		}
		lw
	}

	private def createEntityManager(): EntityManager = {
		entityManagerFactory.createEntityManager()
	}

	private def closeEntityManager() {
		val emHolder = TransactionSynchronizationManager.unbindResource(entityManagerFactory).asInstanceOf[EntityManagerHolder]
		EntityManagerFactoryUtils.closeEntityManager(emHolder.getEntityManager)
	}


}