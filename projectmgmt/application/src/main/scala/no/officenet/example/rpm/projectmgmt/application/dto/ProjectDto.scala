package no.officenet.example.rpm.projectmgmt.application.dto

import no.officenet.example.rpm.pets.domain.model.entities.Pet
import no.officenet.example.rpm.projectmgmt.domain.model.entities.Project

class ProjectDto(var project: Project, var pet: Pet) extends Serializable {

	def this() {
		this(null, null)
	}

	def this(project: Project) {
		this(project, null)
	}
}