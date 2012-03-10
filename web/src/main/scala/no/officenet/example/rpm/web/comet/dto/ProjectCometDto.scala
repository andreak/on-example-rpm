package no.officenet.example.rpm.web.comet.dto

import no.officenet.example.rpm.projectmgmt.domain.model.entities.Project
import org.joda.time.DateTime
import no.officenet.example.rpm.support.infrastructure.scala.lang.ControlHelpers.?
import no.officenet.example.rpm.projectmgmt.domain.model.enums.ProjectType

case class ProjectCometDto(id: Long,
						   name: String,
						   description: Option[String] = None,
						   created: DateTime,
						   createdBy: UserCometDto,
						   modified: Option[DateTime],
						   modifiedBy: Option[UserCometDto],
						   projectType: ProjectType.ExtendedValue,
						   budget: Option[Long],
						   estimatedStartDate: Option[DateTime],
						   activityList: List[ActivityCometDto] = Nil
							  )

object ProjectCometDto {
	def apply(project: Project): ProjectCometDto = {
		ProjectCometDto(
			id = project.id,
			name = project.name,
			description = project.description,
			created = project.created,
			createdBy = UserCometDto(project.createdBy),
			modified = ?(project.modified),
			modifiedBy = ?(project.modifiedBy).map(UserCometDto(_)),
			projectType = project.projectType,
			budget = project.budget,
			estimatedStartDate = ?(project.estimatedStartDate)
		)
	}
}
