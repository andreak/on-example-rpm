package no.officenet.example.rpm.projectmgmt.domain.events

import org.springframework.stereotype.Component
import no.officenet.example.rpm.support.domain.events.{DomainEventHandler, AfterCommitEventDispatcher, DomainEventDispatcher}

@Component
class ProjectUpdatedEventHandler extends DomainEventHandler[ProjectUpdatedEvent] {

	DomainEventDispatcher.registerEventHandler(classOf[ProjectUpdatedEvent], this)

	def handleEvent(event: ProjectUpdatedEvent) {
		AfterCommitEventDispatcher.registerAfterCommitEvent(event)
	}

}