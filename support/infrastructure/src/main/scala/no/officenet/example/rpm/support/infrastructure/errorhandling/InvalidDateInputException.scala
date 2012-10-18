package no.officenet.example.rpm.support.infrastructure.errorhandling

class InvalidDateInputException(input: String) extends AbstractInfrastructureSystemException ("invalid_date_input", input)