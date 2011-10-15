package no.officenet.example.rpm.support.domain.events

import no.officenet.example.rpm.support.infrastructure.enums.EnumWithName


object OperationType extends EnumWithName {
	val CREATE, UPDATE, DELETE = Name
}