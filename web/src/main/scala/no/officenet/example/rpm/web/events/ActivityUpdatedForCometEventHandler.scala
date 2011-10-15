package no.officenet.example.rpm.web.events

import no.officenet.example.rpm.support.domain.events.OperationType
import no.officenet.example.rpm.projectmgmt.domain.events.ActivityUpdatedEvent
import org.springframework.stereotype.Component
import no.officenet.example.rpm.support.domain.events.{AfterCommitEventDispatcher, DomainEventHandler}

@Component
class ActivityUpdatedForCometEventHandler extends DomainEventHandler[ActivityUpdatedEvent] {

	AfterCommitEventDispatcher.registerEventHandler(classOf[ActivityUpdatedEvent], this)

	def handleEvent(event: ActivityUpdatedEvent) {
		if (OperationType.UPDATE == event.operationType) {
			// Send actor a message for comet-updates

		}
	}
}