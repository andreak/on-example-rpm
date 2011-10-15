package no.officenet.example.rpm.web.comet

import net.liftweb._
import common.{Full, Empty, Box}
import http.{S, SHtml}
import util.ClearNodes
import util.Helpers._
import no.officenet.example.rpm.projectmgmt.domain.model.entities.Project
import no.officenet.example.rpm.support.domain.i18n.GlobalTexts
import xml.Text
import no.officenet.example.rpm.web.snippet.editProjectDialogVar
import no.officenet.example.rpm.projectmgmt.domain.model.enums.ProjectTexts
import no.officenet.example.rpm.support.infrastructure.logging.Loggable
import no.officenet.example.rpm.web.lib.JQueryDialog
import no.officenet.example.rpm.web.lib.ContextVars._
import no.officenet.example.rpm.support.domain.i18n.Localizer._
import no.officenet.example.rpm.support.infrastructure.scala.lang.ControlHelpers.?

object ProjectDetailCometRenderer extends Loggable {

	val newProjectTemplate = "lift/project/_projectEdit"

	def getEditButtonLink(projectId: java.lang.Long) = {
		SHtml.a(() => {
			trace("retrieving projectId: " + projectId)
			projectVar.set(projectAppService.retrieve(projectId)) // The popup uses this to access the selected project
			// Put the handle for the popup in a request-var so that the pop is able to close itself.
			editProjectDialogVar.set(JQueryDialog(S.runTemplate(List(newProjectTemplate)).openOr(<div>Template {newProjectTemplate} not found</div>),
												  L(ProjectTexts.V.projectDialog_title_newProject)))
			editProjectDialogVar.get.open
		},
				Text(L(GlobalTexts.button_edit)))
	}

	def getNodeSeqNodeSeq(project: Project) = {
		".projectName *" #> project.name &
		".editButton *" #> getEditButtonLink(project.id) &
		".description *" #> project.description &
		".created *" #> formatDateTime(L(GlobalTexts.dateformat_fullDateTime), Some(project.created)) &
		".createdBy *" #> project.createdBy.displayName &
		".modified *" #> formatDateTime(L(GlobalTexts.dateformat_fullDateTime), ?(project.modified)) &
		".modifiedBy *" #> ?(project.modifiedBy).map(_.displayName) &
		".projectType *" #> L(project.projectType.wrapped) &
		".budget *" #> project.budget.map(_.toString) &
		".projectEstimatedStart *" #> formatDateTime(L(GlobalTexts.dateformat_fullDateTime),
													 ?(project.estimatedStartDate)) &
		".petId *" #> ?(project.petId).map(_.toString)
	}
}