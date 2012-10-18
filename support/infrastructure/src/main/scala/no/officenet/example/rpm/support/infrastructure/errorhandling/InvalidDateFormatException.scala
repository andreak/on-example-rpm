package no.officenet.example.rpm.support.infrastructure.errorhandling

class InvalidDateFormatException(input: String) extends AbstractInfrastructureSystemException ("invalid_date_format", input)
