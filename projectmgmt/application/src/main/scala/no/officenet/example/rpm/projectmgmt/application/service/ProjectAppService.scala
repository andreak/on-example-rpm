package no.officenet.example.rpm.projectmgmt.application.service

import no.officenet.example.rpm.support.infrastructure.spring.AppService
import no.officenet.example.rpm.projectmgmt.application.dto.ProjectDto
import javax.annotation.Resource
import no.officenet.example.rpm.projectmgmt.domain.service.ProjectService
import no.officenet.example.rpm.pets.domain.service.repository.PetRepository
import no.officenet.example.rpm.pets.domain.model.entities.Pet
import org.joda.time.DateTime
import no.officenet.example.rpm.support.infrastructure.logging.Loggable
import no.officenet.example.rpm.support.domain.service.UserService
import org.springframework.security.core.context.SecurityContextHolder
import javax.annotation.security.RolesAllowed

@AppService
class ProjectAppServiceImpl extends ProjectAppService

trait ProjectAppService extends Loggable {

	@Resource
	val projectService: ProjectService = null
	@Resource
	val petRepository: PetRepository = null
	@Resource
	val userService: UserService = null

	def retrieve(id: java.lang.Long): ProjectDto = {
		val project = projectService.retrieve(id)
		val projectDto = new ProjectDto(project)
		if (project.petId != null) {
			projectDto.pet = petRepository.retrieve(project.petId)
		} else {
			projectDto.pet = new Pet
		}
		projectDto
	}

	@RolesAllowed(Array("PROJECT_MANAGER"))
	def create(projectDto: ProjectDto): ProjectDto = {
		if (projectDto.project.id != null) throw new IllegalArgumentException("Cannot create existing entity with id: " + projectDto.project.id)
		if (projectDto.pet != null) {
			projectDto.pet.created = new DateTime()
			projectDto.pet = petRepository.save(projectDto.pet)
			projectDto.project.petId = projectDto.pet.id
		}
		projectDto.project = projectService.create(projectDto.project)
		projectDto
	}

	@RolesAllowed(Array("PROJECT_MANAGER"))
	def update(projectDto: ProjectDto): ProjectDto = {
		trace("Activities in project: " + projectDto.project.activityList.size())
		if (projectDto.project.id == null) throw new IllegalArgumentException("Cannot update non-existing entity")
		if (projectDto.pet != null) {
			if (projectDto.pet.id == null) {
				projectDto.pet.created = new DateTime()
			} else {
				projectDto.pet.modified = new DateTime()
			}
			projectDto.pet = petRepository.save(projectDto.pet)
			projectDto.project.petId = projectDto.pet.id
		}
		projectDto.project.modified = new DateTime()
		userService.findByUserName(SecurityContextHolder.getContext.getAuthentication.getName).foreach(projectDto.project.modifiedBy = _)
		projectDto.project = projectService.update(projectDto.project)
		projectDto
	}

	def findAll = projectService.findAll
}