package no.officenet.example.rpm.blog.domain.event

import no.officenet.example.rpm.support.domain.events.{DomainEvent, OperationType}
import no.officenet.example.rpm.blog.domain.model.entities.Comment

case class CommentUpdatedEvent(operationType: OperationType.ExtendedValue, comment: Comment) extends DomainEvent