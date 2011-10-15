package no.officenet.example.rpm.projectmgmt.domain.events

import org.springframework.stereotype.Component
import no.officenet.example.rpm.support.domain.events.{DomainEventHandler, AfterCommitEventDispatcher, DomainEventDispatcher}

@Component
class ActivityUpdatedEventHandler extends DomainEventHandler[ActivityUpdatedEvent] {

	DomainEventDispatcher.registerEventHandler(classOf[ActivityUpdatedEvent], this)

	def handleEvent(event: ActivityUpdatedEvent) {
		AfterCommitEventDispatcher.registerAfterCommitEvent(event)
	}

}