package no.officenet.example.rpm.support.infrastructure.errorhandling

class InvalidTimeInputException(input: String) extends AbstractInfrastructureSystemException ("invalid_time_input", input)