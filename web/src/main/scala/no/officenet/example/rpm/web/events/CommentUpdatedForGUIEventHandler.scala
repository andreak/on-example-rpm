package no.officenet.example.rpm.web.events

import org.springframework.stereotype.Component
import javax.annotation.Resource
import no.officenet.example.rpm.blog.domain.event.CommentUpdatedEvent
import no.officenet.example.rpm.support.infrastructure.logging.Loggable
import no.officenet.example.rpm.blog.domain.service.BlogEntryService
import no.officenet.example.rpm.web.comet.dto.{CommentCometDto, BlogEntryCometDto}
import no.officenet.example.rpm.support.domain.events.{AfterCommitEventDispatcher, OperationType, DomainEventHandler}
import no.officenet.example.rpm.web.comet.server.{CommentUpdatedMessage, BlogEntryMasterServer, CommentCreatedMessage, BlogMasterServer, BlogEntryCommentAddedMessage}

@Component
class CommentUpdatedForGUIEventHandler extends DomainEventHandler[CommentUpdatedEvent] with Loggable {

	@Resource
	val blogEntryService: BlogEntryService = null

	AfterCommitEventDispatcher.registerEventHandler(classOf[CommentUpdatedEvent], this)

	def handleEvent(event: CommentUpdatedEvent) {
		if (event.operationType == OperationType.CREATE) {
			BlogEntryMasterServer.findServerFor(event.comment.commentedId.longValue())
				.foreach(_ ! CommentCreatedMessage(CommentCometDto(event.comment), None))

			val blogEntry = blogEntryService.retrieve(event.comment.commentedId)
			BlogMasterServer.findServerFor(blogEntry.blog.id.longValue())
				.foreach(_ ! BlogEntryCommentAddedMessage(BlogEntryCometDto(blogEntry)))

		} else if (event.operationType == OperationType.UPDATE) {
			BlogEntryMasterServer.findServerFor(event.comment.commentedId.longValue())
				.foreach(_ ! CommentUpdatedMessage(CommentCometDto(event.comment)))
		}
	}
}