package no.officenet.example.rpm.blog.domain.event

import no.officenet.example.rpm.blog.domain.model.entities.BlogEntry
import no.officenet.example.rpm.support.domain.events.{OperationType, DomainEvent}


case class BlogEntryUpdatedEvent(operationType: OperationType.ExtendedValue, blogEntry: BlogEntry) extends DomainEvent