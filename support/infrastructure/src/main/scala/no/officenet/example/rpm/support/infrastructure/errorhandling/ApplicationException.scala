package no.officenet.example.rpm.support.infrastructure.errorhandling


trait ApplicationException {
	def get: Throwable
}