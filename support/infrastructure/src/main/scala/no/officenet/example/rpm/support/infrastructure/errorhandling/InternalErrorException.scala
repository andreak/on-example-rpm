package no.officenet.example.rpm.support.infrastructure.errorhandling


class InternalErrorException(cause: Throwable) extends AbstractInfrastructureSystemException("internal_error", cause) {


}