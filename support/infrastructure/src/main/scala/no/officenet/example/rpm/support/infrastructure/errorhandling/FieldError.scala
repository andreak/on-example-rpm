package no.officenet.example.rpm.support.infrastructure.errorhandling

case class FieldError(fieldName: String, errorValue: Any, errorId: String, errorMessage: String)
