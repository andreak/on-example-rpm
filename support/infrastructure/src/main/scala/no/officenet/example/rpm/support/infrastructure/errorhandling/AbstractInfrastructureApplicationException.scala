package no.officenet.example.rpm.support.infrastructure.errorhandling

abstract class AbstractInfrastructureApplicationException(resourceKey: String, cause: Throwable, arguments: AnyRef*)
	extends LocalizableRuntimeException(resourceKey, cause, arguments:_*) with ApplicationException {

	def this(resourceKey: String) {
		this(resourceKey, null, Seq.empty:_*)
	}

	def this(resourceKey: String, arguments: AnyRef*) {
		this(resourceKey, null, arguments:_*)
	}

	final def get = this

	val RESOURCE_BUNDLE_NAME = "infrastructureErrorResources"

	def getResourceBundleName = RESOURCE_BUNDLE_NAME
}