package no.officenet.example.rpm.web.events

import no.officenet.example.rpm.support.domain.events.OperationType
import org.springframework.stereotype.Component
import no.officenet.example.rpm.support.domain.events.{AfterCommitEventDispatcher, DomainEventHandler}
import no.officenet.example.rpm.projectmgmt.domain.events.ProjectUpdatedEvent
import no.officenet.example.rpm.web.comet.server.ProjectCometMasterServer
import no.officenet.example.rpm.web.comet.dto.ProjectCometDto

@Component
class ProjectUpdatedForCometEventHandler extends DomainEventHandler[ProjectUpdatedEvent] {

	AfterCommitEventDispatcher.registerEventHandler(classOf[ProjectUpdatedEvent], this)

	def handleEvent(event: ProjectUpdatedEvent) {
		if (OperationType.UPDATE == event.operationType) {
			// Send actor a message for comet-updates
			ProjectCometMasterServer.findProjectCometServerFor(event.project.id).
				map(_.projectUpdated(ProjectCometDto(event.project)))
		}
	}
}