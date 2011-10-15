package no.officenet.example.rpm.support.infrastructure.errorhandling


class InvalidDateException private(message: String,  cause: Throwable) extends RuntimeException(message, cause) {

	def this(message: String) {
		this(message, null)
	}

	def this(cause: Throwable) {
		this(cause.getMessage, cause)
	}
}