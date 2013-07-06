package no.officenet.example.rpm.web.comet.dto

import org.joda.time.DateTime
import no.officenet.example.rpm.blog.domain.model.entities.BlogEntrySummary

case class BlogEntrySummaryCometDto(blogId: Long,
									entityId: Long,
									created: DateTime,
									createdBy: UserCometDto,
									modified: Option[DateTime] = None,
									modifiedBy: Option[UserCometDto] = None,
									title: String,
									summary: String,
									content: String,
									numComments: Long)

object BlogEntrySummaryCometDto {
	def apply(blogEntrySummary: BlogEntrySummary): BlogEntrySummaryCometDto = {
		BlogEntrySummaryCometDto(
			blogId = blogEntrySummary.blogId,
			entityId = blogEntrySummary.id,
			created = blogEntrySummary.created,
			createdBy = UserCometDto(blogEntrySummary.createdBy),
			title = blogEntrySummary.title,
			summary = blogEntrySummary.summary,
			content = blogEntrySummary.content,
			numComments = blogEntrySummary.numComments
		)
	}

	def apply(blogEntryCometDto: BlogEntryCometDto): BlogEntrySummaryCometDto = {
		BlogEntrySummaryCometDto(
			blogId = blogEntryCometDto.blogId,
			entityId = blogEntryCometDto.entityId,
			created = blogEntryCometDto.created,
			createdBy = blogEntryCometDto.createdBy,
			modified = blogEntryCometDto.modified,
			modifiedBy = blogEntryCometDto.modifiedBy,
			title = blogEntryCometDto.title,
			summary = blogEntryCometDto.summary,
			content = blogEntryCometDto.content,
			numComments = blogEntryCometDto.comments.size
		)
	}

}