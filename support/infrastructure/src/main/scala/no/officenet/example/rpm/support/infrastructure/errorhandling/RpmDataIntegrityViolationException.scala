package no.officenet.example.rpm.support.infrastructure.errorhandling

import org.springframework.dao.DataIntegrityViolationException

object RpmDataIntegrityViolationException {
	val DB_CONSTRAINTS_RESOURCE_BUNDLE_NAME = "dataIntegrityResources"
}

class RpmDataIntegrityViolationException(val constraintName: String, cause: Throwable)
	extends DataIntegrityViolationException(constraintName, cause) with ApplicationException with Localizable {

	def this(constraintName: String) {
		this(constraintName, null)
	}

	val localizableRuntimeException = new LocalizableRuntimeException(constraintName, cause) {
		def getResourceBundleName = RpmDataIntegrityViolationException.DB_CONSTRAINTS_RESOURCE_BUNDLE_NAME
	}

	def getResourceBundleName = localizableRuntimeException.getResourceBundleName

	def getResourceKey = localizableRuntimeException.getResourceKey

	def getArguments = localizableRuntimeException.getArguments

}