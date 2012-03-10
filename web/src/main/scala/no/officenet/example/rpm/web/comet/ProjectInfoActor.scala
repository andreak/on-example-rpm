package no.officenet.example.rpm.web.comet

import dto.ProjectCometDto
import net.liftweb._
import common.{Full, Empty, Box}
import http.S
import server.{ProjectCometCreatedMessage, ProjectCometMasterServer}
import util.ClearNodes
import util.Helpers._
import org.springframework.beans.factory.annotation.Configurable
import no.officenet.example.rpm.support.infrastructure.logging.Loggable
import xml.NodeSeq
import org.springframework.context.i18n.LocaleContextHolder


@Configurable
class ProjectInfoActor extends RpmCometActor with Loggable {

	lazy val projectId = asLong(nameParts(1)).get
	protected def registerWith = ProjectCometMasterServer.registerWithProjectCometServer(projectId)
	var cachedProject: Box[ProjectCometDto] = Empty

	override def lowPriority = {
		case ProjectCometCreatedMessage(project) =>
			cachedProject = Full(project)
			reRender()
		case project: ProjectCometDto => {
			cachedProject = Full(project)
			reRender()
		}
	}

	def render = {
		trace("Using locale: " + S.locale + " (LocaleContextHolder=" + LocaleContextHolder.getLocale + " for actor: " + name)
		cachedProject.map(project => {
			ProjectDetailCometRenderer.getNodeSeqNodeSeq(project)
		}).openOr(ClearNodes):(NodeSeq => NodeSeq)

	}
}