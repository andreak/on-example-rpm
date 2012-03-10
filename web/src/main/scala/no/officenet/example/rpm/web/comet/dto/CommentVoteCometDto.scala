package no.officenet.example.rpm.web.comet.dto

import no.officenet.example.rpm.blog.domain.model.entities.CommentVote

case class CommentVoteCometDto(voteValue: Int)

object CommentVoteCometDto {
	def apply(commentVote: CommentVote): CommentVoteCometDto = {
		CommentVoteCometDto(
			voteValue = commentVote.voteValue
		)
	}
}