package no.officenet.example.rpm.web.comet.dto

import org.joda.time.DateTime
import no.officenet.example.rpm.blog.domain.model.entities.Comment
import scala.collection.JavaConversions._

case class CommentCometDto(id: Long,
						   created: DateTime,
						   createdBy: UserCometDto,
						   commentText: String,
						   parentId: Option[Long],
						   commentVote: CommentVoteCometDto,
						   children: List[CommentCometDto])

object CommentCometDto {
	def apply(comment: Comment): CommentCometDto = {
		CommentCometDto(
			id = comment.id.longValue(),
			created = comment.created,
			createdBy = UserCometDto(comment.createdBy),
			commentText = comment.commentText,
			parentId = comment.parent match {
				case null => None
				case c: Comment => Some(c.id.longValue())
			},
			commentVote = CommentVoteCometDto(comment.commentVote),
			children = comment.children.map(CommentCometDto(_)).toList
		)
	}
}