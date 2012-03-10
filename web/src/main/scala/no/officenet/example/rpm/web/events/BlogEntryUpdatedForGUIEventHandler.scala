package no.officenet.example.rpm.web.events

import org.springframework.stereotype.Component
import no.officenet.example.rpm.blog.domain.event.BlogEntryUpdatedEvent
import no.officenet.example.rpm.support.infrastructure.logging.Loggable
import no.officenet.example.rpm.web.comet.dto.BlogEntryCometDto
import no.officenet.example.rpm.web.comet.server.{BlogEntryUpdatedMessage, BlogEntryMasterServer, BlogEntryCreatedMessage, BlogMasterServer}
import no.officenet.example.rpm.support.domain.events.{OperationType, AfterCommitEventDispatcher, DomainEventHandler}

@Component
class BlogEntryUpdatedForGUIEventHandler extends DomainEventHandler[BlogEntryUpdatedEvent] with Loggable {

	AfterCommitEventDispatcher.registerEventHandler(classOf[BlogEntryUpdatedEvent], this)

	def handleEvent(event: BlogEntryUpdatedEvent) {
		if (event.operationType == OperationType.CREATE) {
			BlogMasterServer.findServerFor(event.blogEntry.blog.id.longValue())
				.foreach(server =>
				server ! BlogEntryCreatedMessage(BlogEntryCometDto(event.blogEntry))
			)
		} else if (event.operationType == OperationType.UPDATE) {
			BlogEntryMasterServer.findServerFor(event.blogEntry.id.longValue())
				.foreach(server =>
				server ! BlogEntryUpdatedMessage(BlogEntryCometDto(event.blogEntry))
			)
			BlogMasterServer.findServerFor(event.blogEntry.blog.id.longValue())
				.foreach(server =>
				server ! BlogEntryUpdatedMessage(BlogEntryCometDto(event.blogEntry))
			)
		}
	}
}