package no.officenet.example.rpm.web.comet.server

import collection.mutable.HashMap
import no.officenet.example.rpm.support.infrastructure.logging.Loggable
import net.liftweb.http.ListenerManager
import net.liftweb.actor.LiftActor
import org.springframework.beans.factory.annotation.Configurable
import javax.annotation.Resource
import no.officenet.example.rpm.projectmgmt.application.service.ProjectAppService
import no.officenet.example.rpm.web.comet.dto.ProjectCometDto

case class ProjectCometServerKey(id: Long)
case class ProjectCometCreatedMessage(project: ProjectCometDto)

@Configurable
class ProjectCometServer(id: Long) extends LiftActor with ListenerManager with Loggable {

	trace("Creating new: "+this)

	private var cachedProject: Option[ProjectCometDto] = None
	@Resource
	val projectAppService: ProjectAppService = null

	/**
	 * When a new comet-actor (CometListener) is initially created, send this message
	 */
	def createUpdate = {
		trace("Sending message to newly subscribed comet-actor for project: "+id)
		if (!cachedProject.isDefined) {
			cachedProject = Some(ProjectCometDto(projectAppService.retrieve(id).project))
		}
		ProjectCometCreatedMessage(cachedProject.get)
	}

	/**
	 * process messages that are sent to the Actor.
	 */
	override def lowPriority = {
		case project: ProjectCometDto =>
			cachedProject = Some(project)
			updateListeners(project)
	}

	override protected def onListenersListEmptied() {
		trace("I'm out of listeners: " + toString)
		val reply = ProjectCometMasterServer !! ServerListenersListEmptiedMessage(id)
		reply.foreach(r => trace("BlogEntryMasterServer replied: " + r))
	}

}

object ProjectCometMasterServer extends LiftActor with Loggable {
	val projectCometServers = new HashMap[ProjectCometServerKey, ProjectCometServer]()

	def findProjectCometServerFor(id: Long): Option[ProjectCometServer] = {
		val key = ProjectCometServerKey(id)
		projectCometServers.get(key)
	}

	def registerWithProjectCometServer(id: Long): ProjectCometServer = {
		val key = ProjectCometServerKey(id)
		projectCometServers.synchronized(projectCometServers.getOrElseUpdate(key, new ProjectCometServer(id)))
	}

	protected def messageHandler = {
		case ServerListenersListEmptiedMessage(id) =>
			projectCometServers.synchronized(projectCometServers.remove(ProjectCometServerKey(id)))
			reply("Removed server: " + id + " from registry of servers")
	}

}
