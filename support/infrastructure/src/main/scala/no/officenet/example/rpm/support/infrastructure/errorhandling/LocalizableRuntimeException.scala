package no.officenet.example.rpm.support.infrastructure.errorhandling


private object LocalizableRuntimeExceptionHelper {
	def getReadableMessage(resourceKey: String, arguments: AnyRef*): String = {
		resourceKey + (if (!arguments.isEmpty) "(" + arguments.mkString(", ") + ")" else "")
	}
}

abstract class LocalizableRuntimeException(resourceKey: String, cause: Throwable, arguments: AnyRef*)
	extends RuntimeException(LocalizableRuntimeExceptionHelper.getReadableMessage(resourceKey, arguments:_*), cause)
			with Localizable {

	def this(resourceKey: String) {
		this(resourceKey, null, Seq.empty:_*)
	}

	def this(resourceKey: String, arguments: AnyRef*) {
		this(resourceKey, null, arguments:_*)
	}

	def getResourceKey = resourceKey

	def getArguments = arguments
}