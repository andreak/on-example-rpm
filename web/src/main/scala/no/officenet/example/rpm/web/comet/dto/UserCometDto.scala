package no.officenet.example.rpm.web.comet.dto

import no.officenet.example.rpm.support.domain.model.entities.User
import org.joda.time.DateTime

case class UserCometDto(id: Long,
						firstName: Option[String],
						lastName: Option[String],
						userName: String,
						displayName: String,
						created: DateTime,
						createdById: Long,
						imageIconPath: Option[String])

object UserCometDto {
	def apply(user: User): UserCometDto = {
		UserCometDto(id = user.id,
			firstName = user.firstName,
			lastName = user.lastName,
			userName = user.userName,
			displayName = user.displayName,
			created = user.created,
			createdById = user.createdBy.id,
			imageIconPath = user.imageIconPath
		)
	}
}