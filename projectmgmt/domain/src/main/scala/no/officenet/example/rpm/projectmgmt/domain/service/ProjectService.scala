package no.officenet.example.rpm.projectmgmt.domain.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import no.officenet.example.rpm.support.domain.service.GenericDomainService
import no.officenet.example.rpm.projectmgmt.domain.model.entities.Project
import repository.ProjectRepository
import org.springframework.beans.factory.annotation.Autowired
import no.officenet.example.rpm.support.infrastructure.jpa.{Order, OrderBy}

@Service
@Transactional
class ProjectServiceImpl @Autowired() (val projectRepository: ProjectRepository) extends ProjectService

trait ProjectService extends GenericDomainService[Project] {

	val projectRepository: ProjectRepository

	repository = projectRepository

	def findAll = {
		super.findAll(OrderBy("created", Order.ASC))
	}

}