package no.officenet.example.rpm.support.infrastructure.errorhandling

case class FieldError(fieldName: String, errorValue: Option[Any], errorId: String, errorMessage: String)
