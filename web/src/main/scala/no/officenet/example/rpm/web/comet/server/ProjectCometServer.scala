package no.officenet.example.rpm.web.comet.server

import collection.mutable.HashMap
import no.officenet.example.rpm.support.infrastructure.logging.Loggable
import net.liftweb.http.ListenerManager
import net.liftweb.actor.LiftActor
import no.officenet.example.rpm.projectmgmt.domain.model.entities.Project
import org.springframework.beans.factory.annotation.Configurable
import javax.annotation.Resource
import no.officenet.example.rpm.projectmgmt.application.service.ProjectAppService

case class ProjectCometServerKey(id: Long)

@Configurable
class ProjectCometServer(id: Long) extends LiftActor with ListenerManager with Loggable {

	trace("Creating new: "+this)

	private var cachedProject: Project = _
	@Resource
	val projectAppService: ProjectAppService = null

	def projectUpdated(project: Project) {
		this ! project
	}

	/**
	 * When a new comet-actor (CometListener) is initially created, send this message
	 */
	def createUpdate = {
		trace("Sending message to newly subscribed comet-actor for project: "+id)
		if (cachedProject == null) {
			cachedProject = projectAppService.retrieve(id).project
		}
		cachedProject
	}

	/**
	 * process messages that are sent to the Actor.
	 */
	override def lowPriority = {
		case project: Project =>
			cachedProject = project
			updateListeners(project)
	}


}

object ProjectCometMasterServer extends Loggable {
	val projectCometServers = new HashMap[ProjectCometServerKey, ProjectCometServer]()

	def findProjectCometServerFor(id: Long): Option[ProjectCometServer] = {
		val key = ProjectCometServerKey(id)
		projectCometServers.get(key)
	}

	def registerWithProjectCometServer(id: Long): ProjectCometServer = {
		val key = ProjectCometServerKey(id)
		projectCometServers.synchronized(projectCometServers.getOrElseUpdate(key, new ProjectCometServer(id)))
	}
}
