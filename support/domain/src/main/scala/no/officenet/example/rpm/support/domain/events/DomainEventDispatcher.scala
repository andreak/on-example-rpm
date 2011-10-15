package no.officenet.example.rpm.support.domain.events

import collection.mutable.{Set, LinkedHashSet, LinkedHashMap}

object DomainEventDispatcher {
	private val eventHandlers = new LinkedHashMap[Class[_ <: DomainEvent], Set[DomainEventHandler[_ <: DomainEvent]]]()

	def registerEventHandler[T <: DomainEvent](domainEvent: Class[T], eventHandler: DomainEventHandler[T]) {
		var eventHandlersForEvent = eventHandlers.getOrElseUpdate(domainEvent, new LinkedHashSet[DomainEventHandler[_ <: DomainEvent]])
		eventHandlersForEvent += eventHandler
	}

	def raiseEvent[T <: DomainEvent](domainEvent: T) {
		val domainEventClass = domainEvent.getClass.asInstanceOf[Class[T]]
		val eventHandlersForEventBox = eventHandlers.get(domainEventClass)
		if (!eventHandlersForEventBox.isDefined) {
			throw new IllegalStateException("No event-handlers registered for event " + domainEventClass)
		}
		eventHandlersForEventBox.foreach(eventHandlersForEvent => {
			eventHandlersForEvent.foreach(eventHandler =>
											  eventHandler.asInstanceOf[DomainEventHandler[T]].handleEvent(domainEvent))
		})
	}
}