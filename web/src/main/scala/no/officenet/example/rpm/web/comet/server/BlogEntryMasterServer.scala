package no.officenet.example.rpm.web.comet.server

import net.liftweb.actor.LiftActor
import collection.mutable.HashMap
import net.liftweb.http.ListenerManager
import net.liftweb.common.{Box, Empty, Full}
import org.springframework.beans.factory.annotation.Configurable
import javax.annotation.Resource
import no.officenet.example.rpm.web.comet.dto.{CommentCometDto, BlogEntryCometDto}
import no.officenet.example.rpm.support.infrastructure.logging.Loggable
import no.officenet.example.rpm.blog.domain.service.{BlogEntryService, CommentService}
import no.officenet.example.rpm.web.comet.RpmActor

case class BlogEntryServerCreatedMessage(cachedBlogEntry: Box[BlogEntryCometDto], voteMap: Map[Long,  Int])

case class FindServerMessage(entityId: Long)

case class RegisterServerMessage(entityId: Long)

case class ServerListenersListEmptiedMessage(entityId: Long)

case class BlogEntryCreatedMessage(blogEntry: BlogEntryCometDto)

case class BlogEntryUpdatedMessage(blogEntry: BlogEntryCometDto)

// Special message to signal that only comment is added so there's no need to FadeIn the message as only the number of comments changes
case class BlogEntryCommentAddedMessage(blogEntry: BlogEntryCometDto)

case class CommentCreatedMessage(comment: CommentCometDto, voteMap: Option[Map[Long,  Int]])

case class CommentUpdatedMessage(comment: CommentCometDto)

case class RemoveVoteMessage(commentId: Long, userId: Long)

case class AddPlusVoteMessage(commentId: Long, userId: Long)

case class AddMinusVoteMessage(commentId: Long, userId: Long)

case class VoteAddedMessage(commentId: Long, userId: Long, voteValue: Boolean)

case class VoteRemovedMessage(commentId: Long, userId: Long, voteValue: Boolean, voteMap: Option[Map[Long,  Int]])

object VoteRemovedMessage {
	def apply(commentId: Long, userId: Long, voteValue: Boolean): VoteRemovedMessage = {
		VoteRemovedMessage(commentId, userId, voteValue, None)
	}
}

case class VotesUpdatedMessage(commentId: Long, voteMap: Map[Long, Int], userId: Long, voteValue: Boolean)

object BlogEntryMasterServer extends LiftActor with Loggable {

	private val serverMap = new HashMap[Long, BlogEntryServer]()

	def findServerFor(blogEntryId: Long): Option[BlogEntryServer] = {
		this !! FindServerMessage(blogEntryId) match {
			case Full(server: BlogEntryServer) => Some(server)
			case e => None
		}
	}

	def registerWithServer(blogEntryId: Long): BlogEntryServer = {
		(this !! RegisterServerMessage(blogEntryId)).open_!.asInstanceOf[BlogEntryServer]
	}

	protected def messageHandler = {
		case FindServerMessage(entityId) =>
			reply(serverMap.get(entityId).getOrElse(()))
		case RegisterServerMessage(entityId) =>
			val server = serverMap.getOrElseUpdate(entityId, new BlogEntryServer(entityId))
			reply(server)
		case ServerListenersListEmptiedMessage(entityId) =>
			serverMap.remove(entityId)
			reply("Removed server: " + entityId + " from registry of server")
	}

}

@Configurable
object CommentVoteActor extends RpmActor {
	@Resource
	private val commentService: CommentService = null

	case class AddPlus(sender: RpmActor, commentId: Long, userId: Long)

	case class AddMinus(sender: RpmActor, commentId: Long, userId: Long)

	case class Remove(sender: RpmActor, commentId: Long, userId: Long)

	/**
	 * It's important to serialize voting, so keep it here
	 */
	protected def messageHandler = {
		case AddPlus(sender, commentId, userId) =>
			commentService.addCommentVote(commentId, userId, true)
			sender ! VoteAddedMessage(commentId, userId, true)

		case AddMinus(sender, commentId, userId) =>
			commentService.addCommentVote(commentId, userId, false)
			sender ! VoteAddedMessage(commentId, userId, false)

		case Remove(sender, commentId, userId) =>
			val vote = commentService.removeCommentVote(commentId, userId)
			sender ! VoteRemovedMessage(commentId, userId, vote.voteValue)
	}
}

@Configurable
class BlogEntryServer(val blogEntryId: Long) extends RpmActor with ListenerManager with Loggable {

	@Resource
	private val blogEntryService: BlogEntryService = null

	private var cachedBlogEntry: Box[BlogEntryCometDto] = Empty

	private var voteMap = Map[Long, Int]()

	/**
	 * When a new comet-actor (CometListener) is initially created, send this message
	 */
	override def createUpdate = {
		trace("Sending message to newly subscribed comet-actor for blogEntryId=" + blogEntryId)
		def populateVotes(comment: CommentCometDto) {
			voteMap += comment.id -> comment.commentVote.voteValue
			comment.children.foreach(populateVotes)
		}
		if (!cachedBlogEntry.isDefined) {
			cachedBlogEntry = Full(BlogEntryCometDto(blogEntryService.retrieve(blogEntryId)))
			for (entry <- cachedBlogEntry) {
				entry.comments.foreach(populateVotes)
			}
		}
		BlogEntryServerCreatedMessage(cachedBlogEntry, voteMap)
	}

	/**
	 * process messages that are sent to the Actor.
	 */
	override def lowPriority = {
		case m@BlogEntryUpdatedMessage(blogEntry) =>
			cachedBlogEntry = Full(blogEntry)
			updateListeners(m)
		case m@CommentCreatedMessage(comment, _) =>
			voteMap += (comment.id -> 0)
			cachedBlogEntry = cachedBlogEntry.map(entry => entry.addComment(comment))
			updateListeners(cachedBlogEntry -> m.copy(voteMap = Some(voteMap)))
		case m@CommentUpdatedMessage(comment) =>
			cachedBlogEntry = cachedBlogEntry.map(entry => entry.replaceComment(comment))
			updateListeners(cachedBlogEntry -> m)
		case AddPlusVoteMessage(commentId, userId) =>
			CommentVoteActor ! CommentVoteActor.AddPlus(this, commentId, userId)
		case AddMinusVoteMessage(commentId, userId) =>
			CommentVoteActor ! CommentVoteActor.AddMinus(this, commentId, userId)
		case RemoveVoteMessage(commentId, userId) =>
			CommentVoteActor ! CommentVoteActor.Remove(this, commentId, userId)
		case m@VoteAddedMessage(commentId, userId, voteValue) =>
			if (voteValue) {
				voteMap += commentId -> (voteMap.get(commentId).getOrElse(0) + 1)
			} else {
				voteMap += commentId -> (voteMap.get(commentId).getOrElse(0) - 1)
			}
			updateListeners(VotesUpdatedMessage(commentId, voteMap, userId, voteValue))
		case m@VoteRemovedMessage(commentId, userId, voteValue, _) =>
			// Note; The logic is reversed as we want to increment if removing a minus-vote
			if (!voteValue) {
				voteMap += commentId -> (voteMap.get(commentId).getOrElse(0) + 1)
			} else {
				voteMap += commentId -> (voteMap.get(commentId).getOrElse(0) - 1)
			}
			updateListeners(VoteRemovedMessage(commentId, userId, voteValue, Some(voteMap)))
	}

	override protected def onListenersListEmptied() {
		trace("I'm out of listeners: " + toString)
		val reply = BlogEntryMasterServer !! ServerListenersListEmptiedMessage(blogEntryId)
		reply.foreach(r => trace("BlogEntryMasterServer replied: " + r))
	}


	override def toString = getClass.getSimpleName + "(" + blogEntryId + ")"

	override protected def finalize() {
		trace("GC: " + getClass.getSimpleName + ": " + blogEntryId)
	}

}