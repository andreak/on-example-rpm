package no.officenet.example.rpm.support.infrastructure.jpa


object Order extends Enumeration {
	type OrderType = Value
	val ASC, DESC = Value
}