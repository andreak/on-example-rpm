package no.officenet.example.rpm.projectmgmt.domain.events

import no.officenet.example.rpm.support.domain.events.{DomainEvent, OperationType}
import no.officenet.example.rpm.projectmgmt.domain.model.entities.Project

class ProjectUpdatedEvent(val project: Project, val operationType: OperationType.ExtendedValue) extends DomainEvent {

}