package no.officenet.example.rpm.projectmgmt.domain.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import no.officenet.example.rpm.support.domain.service.GenericDomainService
import no.officenet.example.rpm.projectmgmt.domain.model.entities.Project
import repository.ProjectRepository
import org.springframework.beans.factory.annotation.Autowired
import no.officenet.example.rpm.support.infrastructure.jpa.{Order, OrderBy}
import no.officenet.example.rpm.projectmgmt.domain.events.ProjectUpdatedEvent
import no.officenet.example.rpm.support.domain.events.{OperationType, DomainEventDispatcher}

@Service
@Transactional
class ProjectServiceImpl @Autowired() (val projectRepository: ProjectRepository) extends ProjectService

trait ProjectService extends GenericDomainService[Project] {

	val projectRepository: ProjectRepository

	repository = projectRepository // Note: *MUST* use constructor-injection for this to be set

	def findAll = {
		super.findAll(OrderBy(Project.name, Order.ASC))
	}

	override def update(project: Project) = {
		val updated = super.update(project)
		DomainEventDispatcher.raiseEvent(new ProjectUpdatedEvent(updated, OperationType.UPDATE))
		updated
	}
}