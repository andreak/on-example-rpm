package no.officenet.example.rpm.projectmgmt.application.service

import no.officenet.example.rpm.support.infrastructure.spring.AppService
import no.officenet.example.rpm.projectmgmt.application.dto.ProjectDto
import javax.annotation.Resource
import no.officenet.example.rpm.projectmgmt.domain.service.ProjectService
import no.officenet.example.rpm.pets.domain.service.repository.PetRepository

@AppService
class ProjectAppServiceImpl extends ProjectAppService

trait ProjectAppService {

	@Resource
	val projectService: ProjectService = null
	@Resource
	val petRepository: PetRepository = null

	def retrieve(id: java.lang.Long): ProjectDto = {
		val projectDto = new ProjectDto
		projectDto
	}

	def create(projectDto: ProjectDto): ProjectDto = {
		if (projectDto.pet != null) {
			projectDto.pet = petRepository.save(projectDto.pet)
			projectDto.project.petId = projectDto.pet.id
		}
		projectDto.project = projectService.create(projectDto.project)
		projectDto
	}

	def findAll = projectService.findAll
}