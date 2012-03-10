package no.officenet.example.rpm.web.snippet.project

import net.liftweb.util.Helpers._
import net.liftweb.http.S
import net.liftweb.common.{Full, Box}
import net.liftweb.http.js._
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.JE._
import org.springframework.beans.factory.annotation.Configurable
import xml.NodeSeq
import no.officenet.example.rpm.web.lib.ContextVars
import javax.annotation.Resource
import no.officenet.example.rpm.projectmgmt.application.service.ProjectAppService

@Configurable
class ProjectEditDialogWrapperSnippet {
	@Resource
	val projectAppService: ProjectAppService = null

	lazy val projectId: Box[Long] = asLong(S.param("id"))

	def render(ns: NodeSeq): NodeSeq = {
		projectId.foreach(id => ContextVars.projectVar.set(projectAppService.retrieve(id)))
		(".liftPageSetter" #> Script(SetExp(JsVar("lift_page"), S.renderVersion))).apply(ns)
	}

}
