package no.officenet.example.rpm.projectmgmt.application.dto

import no.officenet.example.rpm.projectmgmt.domain.model.entities.Project

class ProjectDto(var project: Project) extends Serializable {

	def this() {
		this(null)
	}
}