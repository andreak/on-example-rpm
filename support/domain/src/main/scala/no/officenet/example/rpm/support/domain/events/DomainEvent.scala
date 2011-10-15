package no.officenet.example.rpm.support.domain.events

trait DomainEvent

trait DomainEventHandler[T <: DomainEvent] {
	def handleEvent(event: T)
}