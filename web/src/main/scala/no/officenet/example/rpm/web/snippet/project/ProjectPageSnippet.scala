package no.officenet.example.rpm.web.snippet.project

import net.liftweb._
import util.Helpers._
import org.springframework.beans.factory.annotation.Configurable
import no.officenet.example.rpm.web.menu.ProjectParam
import no.officenet.example.rpm.support.infrastructure.logging.Loggable
import xml.NodeSeq
import net.liftweb.http.S
import no.officenet.example.rpm.web.comet.ProjectDetailCometRenderer

@Configurable
class ProjectPageSnippet(projectParam: ProjectParam) extends Loggable {

	trace("Using locale: "+S.locale)

	def editNoComet = {
		".editButton *" #> ProjectDetailCometRenderer.getEditButtonLink(projectParam.id.toLong)
	}

	def render(in: NodeSeq) = {
		// The locale too has to be a part of the comet's name. Else switching locale doesn't affect the actor
		val cometName = List(S.locale, projectParam.id)
		<div class={"lift:comet?type=ProjectInfoActor;name="+cometName.mkString(":")}
			 style="display: inline;">
			{in}
		</div>

	}

}