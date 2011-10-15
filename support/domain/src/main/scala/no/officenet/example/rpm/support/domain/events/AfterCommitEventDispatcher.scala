package no.officenet.example.rpm.support.domain.events


import collection.mutable.{Set, LinkedHashSet, LinkedHashMap}
import org.springframework.transaction.support.{TransactionSynchronizationManager, TransactionSynchronizationAdapter}
import no.officenet.example.rpm.support.infrastructure.logging.Loggable

object AfterCommitEventDispatcher extends Loggable {
	private val eventHandlers = new LinkedHashMap[Class[_ <: DomainEvent], Set[DomainEventHandler[_ <: DomainEvent]]]()

	def registerEventHandler[T <: DomainEvent](domainEvent: Class[T], eventHandler: DomainEventHandler[T]) {
		var eventHandlersForEvent = eventHandlers.getOrElseUpdate(domainEvent, new LinkedHashSet[DomainEventHandler[_ <: DomainEvent]])
		eventHandlersForEvent += eventHandler
	}

	def registerAfterCommitEvent[T <: DomainEvent](domainEvent: T) {
		val domainEventClass = domainEvent.getClass.asInstanceOf[Class[T]]
		val eventHandlersForEventBox = eventHandlers.get(domainEventClass)
		if (!eventHandlersForEventBox.isDefined) {
			throw new IllegalStateException("No event-handlers registered for event " + domainEventClass)
		}
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
			override def afterCommit() {
				trace("Executing after-commit event for: %s".format(domainEventClass.getName))
				eventHandlersForEventBox.foreach(eventHandlersForEvent => {
					eventHandlersForEvent.foreach(eventHandler =>
													  eventHandler.asInstanceOf[DomainEventHandler[T]].handleEvent(domainEvent))
				})
			}
		})
	}

}