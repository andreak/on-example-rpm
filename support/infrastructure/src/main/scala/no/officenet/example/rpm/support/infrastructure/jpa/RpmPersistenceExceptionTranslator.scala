package no.officenet.example.rpm.support.infrastructure.jpa

import no.officenet.example.rpm.support.infrastructure.logging.Loggable
import java.sql.{SQLIntegrityConstraintViolationException, SQLException}
import org.springframework.dao.DataAccessException
import org.springframework.orm.jpa.vendor.{EclipseLinkJpaVendorAdapter, EclipseLinkJpaDialect}
import javax.persistence.OptimisticLockException
import no.officenet.example.rpm.support.infrastructure.errorhandling.{RpmOptimisticLockException, RpmDataIntegrityViolationException}

class RpmPersistenceExceptionTranslator extends EclipseLinkJpaDialect with Loggable {

	def setVendorAdapter(vendorAdapter: EclipseLinkJpaVendorAdapter) {
		this.vendorAdapter = vendorAdapter
	}

	private var vendorAdapter: EclipseLinkJpaVendorAdapter = _

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
		ex match {
			case lockException: OptimisticLockException =>
				return new RpmOptimisticLockException(lockException)
			case _ =>
		}
		log.debug("No translation performed. Fall-back to " + getClass.getSimpleName + " JPA Dialect and its exception translator.")
		super.translateExceptionIfPossible(ex)
	}



	def extractConstraintViolationExceptionIfPossible(ex: RuntimeException ): DataAccessException = {
		localTranslateExceptionIfPossible(ex)
	}

	def localTranslateExceptionIfPossible(ex: RuntimeException ): DataAccessException = {
		var cause = ex.getCause
		while (cause != null) {
			cause match {
				case sqlException: SQLException =>
					return customTranslate(sqlException, ex)
				case _ =>
			}
			cause = cause.getCause
		}
		null
	}

	private def customTranslate(sqlException: SQLException , ex: RuntimeException): DataAccessException = {
		val sqlstate = sqlException.getSQLState
		if (sqlstate != null) {
			val classCode = sqlstate.substring(0, 2)
			if (integrityViolationCodes.contains(classCode)) {
				sqlException match {
					case integretyEx: SQLIntegrityConstraintViolationException =>
						val constraintName = integretyEx.getMessage
						if (constraintName ne null) {
							return new RpmDataIntegrityViolationException(constraintName, ex)
						}
					case _ =>

				}
			}
		}
		null
	}

}