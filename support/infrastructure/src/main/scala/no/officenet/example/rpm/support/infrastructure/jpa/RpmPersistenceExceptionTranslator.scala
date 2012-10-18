package no.officenet.example.rpm.support.infrastructure.jpa

import no.officenet.example.rpm.support.infrastructure.logging.Loggable
import java.sql.SQLException
import org.springframework.dao.DataAccessException
import org.hibernate.exception.ConstraintViolationException
import org.hibernate.cfg.AvailableSettings
import org.hibernate.dialect.Dialect
import org.springframework.orm.jpa.vendor.{HibernateJpaVendorAdapter, HibernateJpaDialect}
import javax.persistence.OptimisticLockException
import no.officenet.example.rpm.support.infrastructure.errorhandling.{RpmOptimisticLockException, RpmDataIntegrityViolationException}

class RpmPersistenceExceptionTranslator extends HibernateJpaDialect with Loggable {

	def setVendorAdapter(vendorAdapter: HibernateJpaVendorAdapter) {
		this.vendorAdapter = vendorAdapter
	}

	private var vendorAdapter: HibernateJpaVendorAdapter = _

	lazy val extractor = Class.forName(vendorAdapter.getJpaPropertyMap.get(AvailableSettings.DIALECT).asInstanceOf[String])
		.newInstance.asInstanceOf[Dialect].getViolatedConstraintNameExtracter

	val integrityViolationCodes = Set("23" // Integrity constraint violation
									  , "27" // Trigger violation
									  , "44" // Check violation
									  , "72") // Trigger violation

	override def translateExceptionIfPossible(ex: RuntimeException): DataAccessException = {
		val dataAccessException = extractConstraintViolationExceptionIfPossible(ex)
		if (dataAccessException != null) {
			if (log.isDebugEnabled) {
				log.debug("Translating successful: " + dataAccessException)
			}
			return dataAccessException
		}
		if (ex.isInstanceOf[OptimisticLockException])  {
			return new RpmOptimisticLockException(ex.asInstanceOf[OptimisticLockException])
		}
		log.debug("No translation performed. Fall-back to " + getClass.getSimpleName + " JPA Dialect and its exception translator.")
		super.translateExceptionIfPossible(ex)
	}



	def extractConstraintViolationExceptionIfPossible(ex: RuntimeException ): DataAccessException = {
		if (ex.isInstanceOf[ConstraintViolationException]) {
			var constraintName = ex.asInstanceOf[ConstraintViolationException].getConstraintName
			val dotPos = constraintName.indexOf('.')
			if (dotPos > 0) constraintName = constraintName.substring(dotPos + 1)
			return new RpmDataIntegrityViolationException(constraintName, ex)
		}
		localTranslateExceptionIfPossible(ex)
	}

	def localTranslateExceptionIfPossible(ex: RuntimeException ): DataAccessException = {
		var cause = ex.getCause
		while (cause != null) {
			if (cause.isInstanceOf[SQLException]) {
				return customTranslate(cause.asInstanceOf[SQLException], ex)
			}
			cause = cause.getCause
		}
		null
	}

	private def getConstraintKeyName(ex: SQLException): String = {
		val constraintName = extractor.extractConstraintName(ex)
		constraintName
	}

	private def customTranslate(sqlException: SQLException , ex: RuntimeException): DataAccessException = {
		val sqlstate = sqlException.getSQLState
		if (sqlstate != null) {
			val classCode = sqlstate.substring(0, 2)
			if (integrityViolationCodes.contains(classCode)) {
				val constraintName = getConstraintKeyName(sqlException)
				if (constraintName != null) {
					return new RpmDataIntegrityViolationException(constraintName, ex)
				}
			}
		}
		null
	}

}