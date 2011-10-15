package no.officenet.example.rpm.projectmgmt.domain.events

import no.officenet.example.rpm.projectmgmt.domain.model.entities.Activity
import no.officenet.example.rpm.support.domain.events.{DomainEvent, OperationType}

class ActivityUpdatedEvent(val activity: Activity, val operationType: OperationType.ExtendedValue) extends DomainEvent {

}