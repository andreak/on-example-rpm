package no.officenet.example.rpm.web.lib

import no.officenet.example.rpm.support.infrastructure.errorhandling.{OvalScreenValidator, Validator}


private[lib] trait Validatable {
	val validator: Validator = OvalScreenValidator
}