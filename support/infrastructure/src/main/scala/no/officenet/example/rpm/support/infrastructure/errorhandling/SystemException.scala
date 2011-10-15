package no.officenet.example.rpm.support.infrastructure.errorhandling


trait SystemException {
	def get: Throwable
}