package no.officenet.example.rpm.web.comet.server

import net.liftweb.actor.LiftActor
import collection.mutable.HashMap
import org.springframework.beans.factory.annotation.Configurable
import net.liftweb.http.ListenerManager
import javax.annotation.Resource
import net.liftweb.common.{Empty, Box, Full}
import no.officenet.example.rpm.web.comet.dto.BlogEntrySummaryCometDto
import no.officenet.example.rpm.support.infrastructure.logging.Loggable
import no.officenet.example.rpm.blog.domain.service.BlogService
import no.officenet.example.rpm.web.comet.RpmActor

case class BlogCometListenerAddedMessage(blogEntryList: List[BlogEntrySummaryCometDto])

object BlogMasterServer extends LiftActor with Loggable {

	private val serverMap = new HashMap[Long, BlogServer]()

	def findServerFor(blogId: Long): Option[BlogServer] = {
		this !! FindServerMessage(blogId) match {
			case Full(server: BlogServer) => Some(server)
			case e => None
		}
	}

	def registerWithServer(blogId: Long): BlogServer = {
		(this !! RegisterServerMessage(blogId)).open_!.asInstanceOf[BlogServer]
	}

	protected def messageHandler = {
		case FindServerMessage(entityId) =>
			reply(serverMap.get(entityId).getOrElse(()))
		case RegisterServerMessage(entityId) =>
			val server = serverMap.getOrElseUpdate(entityId, new BlogServer(entityId))
			reply(server)
		case ServerListenersListEmptiedMessage(entityId) =>
			serverMap.remove(entityId)
			reply("Removed server: " + entityId + " from registry of server")
	}

}

@Configurable
class BlogServer(val blogId: Long) extends RpmActor with ListenerManager with Loggable {

	@Resource
	private val blogService: BlogService = null

	private var cachedBlogEntryList: List[BlogEntrySummaryCometDto] = Nil

	/**
	 * When a new comet-actor (CometListener) is initially created, send this message
	 */
	override def createUpdate = {
		trace("Sending message to newly subscribed comet-actor for blogId=" + blogId)
		if (cachedBlogEntryList.isEmpty) {
			cachedBlogEntryList = blogService.retrieveBlogSummaries(blogId).map(BlogEntrySummaryCometDto(_)).toList
		}
		BlogCometListenerAddedMessage(cachedBlogEntryList)
	}

	/**
	 * process messages that are sent to the Actor.
	 */
	override def lowPriority = {
		case m@BlogEntryCreatedMessage(blogEntry) =>
			// Prepend as the list is sorted created DESC
			cachedBlogEntryList = BlogEntrySummaryCometDto(blogEntry) :: cachedBlogEntryList.dropRight(1)
			updateListeners(cachedBlogEntryList -> m)
		case m@BlogEntryUpdatedMessage(blogEntry) =>
			// Only update listeners if the entry is in the "watch-list"
			val index = cachedBlogEntryList.indexWhere(_.entityId == blogEntry.entityId)
			if (index != -1) {
				cachedBlogEntryList = cachedBlogEntryList.updated(index, BlogEntrySummaryCometDto(blogEntry))
				updateListeners(cachedBlogEntryList -> m)
			}
		case m@BlogEntryCommentAddedMessage(blogEntry) =>
			// Only update listeners if the entry is in the "watch-list"
			val index = cachedBlogEntryList.indexWhere(_.entityId == blogEntry.entityId)
			if (index != -1) {
				cachedBlogEntryList = cachedBlogEntryList.updated(index, BlogEntrySummaryCometDto(blogEntry))
				updateListeners(cachedBlogEntryList -> m)
			}
	}

	override protected def onListenersListEmptied() {
		trace("I'm out of listeners: " + toString)
		val reply = BlogMasterServer !! ServerListenersListEmptiedMessage(blogId)
		reply.foreach(r => trace("BlogEntryMasterServer replied: " + r))
	}


	override def toString = getClass.getSimpleName + "(" + blogId + ")"

	override protected def finalize() {
		trace("GC: " + getClass.getSimpleName + ": " + blogId)
	}

}