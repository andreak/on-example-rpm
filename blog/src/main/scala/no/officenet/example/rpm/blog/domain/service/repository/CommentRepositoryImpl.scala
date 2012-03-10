package no.officenet.example.rpm.blog.domain.service.repository

import org.springframework.stereotype.Repository
import no.officenet.example.rpm.blog.domain.model.entities.{CommentVoteOwner, Comment}
import no.officenet.example.rpm.support.infrastructure.jpa.{PersistenceUnits, GenericRepository}

@Repository
class CommentRepositoryImpl extends CommentRepository with PersistenceUnits.PersistenceUnitRPM

private case class VoteSummary(commentId: Long, voteValue: Boolean)

trait CommentRepository extends GenericRepository[Comment, java.lang.Long] {

	def findVotesForPerson(blogEntryId: Long, userId: Long): Map[Long, Boolean] = {
		val result = entityManager.createQuery[VoteSummary]("""
		SELECT new no.officenet.example.rpm.blog.domain.service.repository.VoteSummary(e.comment.id, e.voteValue)
		FROM CommentVoteOwner e WHERE e.comment.commentedId = :blogEntryId
		AND e.voter.id = :userId
		""").
			setParams("blogEntryId" -> blogEntryId, "userId" -> userId).
			getResultList().map(b => b.commentId -> b.voteValue).toMap
		result
	}

	def retrieveCommentVoteOwner(commentId: Long, voterId: Long): Option[CommentVoteOwner] = {
		entityManager.createQuery("SELECT e FROM CommentVoteOwner e WHERE e.comment.id = :commentId AND e.voter.id = :voterId").
			setParams("commentId" -> commentId, "voterId" -> voterId).
			findOne
	}
}
