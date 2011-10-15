package no.officenet.example.rpm.support.infrastructure.errorhandling

class AccessDeniedException(cause: Throwable)
	extends AbstractInfrastructureApplicationException("access_denied", cause) {

}