package no.officenet.example.rpm.web.comet.dto

import no.officenet.example.rpm.projectmgmt.domain.model.entities.Activity

case class ActivityCometDto(id: Long,  name: String)

object ActivityCometDto {
	def apply(activity: Activity): ActivityCometDto = {
		ActivityCometDto(
			id = activity.id,
			name = activity.name
		)
	}
}