package no.officenet.example.rpm.blog.domain.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.beans.factory.annotation.Autowired
import org.joda.time.{DateTimeUtils, DateTime}
import javax.annotation.Resource
import no.officenet.example.rpm.support.domain.service.GenericDomainService
import no.officenet.example.rpm.support.domain.service.repository.UserRepository
import repository.CommentRepository
import no.officenet.example.rpm.blog.domain.model.entities.{CommentVoteOwner, CommentVote, Comment}
import no.officenet.example.rpm.blog.domain.event.CommentUpdatedEvent
import no.officenet.example.rpm.support.domain.events.{OperationType, AfterCommitEventDispatcher}

trait CommentService extends GenericDomainService[Comment] {
	val commentRepository: CommentRepository

	@Resource
	val userRepository: UserRepository = null

	repository = commentRepository

	def createComment(comment: Comment) {
		comment.created = new DateTime(DateTimeUtils.currentTimeMillis())
		val vote = repository.entityManager.merge(new CommentVote())
		comment.commentVote = vote
		val persistentEntity = commentRepository.save(comment)
		AfterCommitEventDispatcher.registerAfterCommitEvent(CommentUpdatedEvent(OperationType.CREATE, persistentEntity))
		persistentEntity
	}

	def updateComment(comment: Comment) {
		comment.modified = Some(new DateTime(DateTimeUtils.currentTimeMillis()))
		val persistentEntity = commentRepository.save(comment)
		AfterCommitEventDispatcher.registerAfterCommitEvent(CommentUpdatedEvent(OperationType.UPDATE, persistentEntity))
		persistentEntity
	}

	def removeCommentVote(commentId: Long, userId: Long): CommentVoteOwner = {
		val voteOwner = commentRepository.retrieveCommentVoteOwner(commentId, userId).get
		commentRepository.entityManager.remove(voteOwner)
		val comment = commentRepository.retrieve(commentId)
		val vote = comment.commentVote
		vote.addVote(!voteOwner.voteValue)
		repository.entityManager.merge(vote)
		voteOwner
	}

	def addCommentVote(commentId: Long, userId: Long, voteValue: Boolean) {
		val comment = commentRepository.retrieve(commentId)
		val voter = userRepository.retrieve(userId)
		val commentVoteOwner = new CommentVoteOwner(new DateTime(), comment, voter, voteValue)
		val owner = repository.entityManager.merge(commentVoteOwner)
		val vote = comment.commentVote
		vote.addVote(voteValue)
		repository.entityManager.merge(vote)
	}

	def findVotesForPerson(blogEntryIdent: Long, userId: Long): Map[Long, Boolean] = {
		commentRepository.findVotesForPerson(blogEntryIdent, userId)
	}

}

@Service
@Transactional
class CommentServiceImpl @Autowired()(val commentRepository: CommentRepository) extends CommentService {

}