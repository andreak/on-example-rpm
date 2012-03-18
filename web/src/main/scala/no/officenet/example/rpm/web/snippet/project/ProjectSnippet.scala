package no.officenet.example.rpm.web.snippet.project

import net.liftweb._
import common.{Full, Empty, Box}
import http._
import util.Helpers._
import js._
import js.JsCmds._

import org.springframework.beans.factory.annotation.Configurable
import no.officenet.example.rpm.projectmgmt.application.service.ProjectAppService
import javax.annotation.Resource
import no.officenet.example.rpm.projectmgmt.domain.model.entities.Project
import no.officenet.example.rpm.projectmgmt.domain.model.enums.{ProjectType, ProjectTexts}
import org.springframework.security.core.context.SecurityContextHolder
import org.joda.time.DateTime
import no.officenet.example.rpm.support.domain.service.UserService
import no.officenet.example.rpm.projectmgmt.application.dto.ProjectDto
import no.officenet.example.rpm.support.infrastructure.logging.Loggable
import xml.{Elem, NodeSeq, Text}
import no.officenet.example.rpm.web.lib.ContextVars._
import no.officenet.example.rpm.support.infrastructure.i18n.Localizer.L
import no.officenet.example.rpm.support.infrastructure.i18n.{Localizer, GlobalTexts}
import no.officenet.example.rpm.support.infrastructure.i18n.Localizer.L_!
import no.officenet.example.rpm.web.menu.{ProjectLoc, ProjectViewParam}
import no.officenet.example.rpm.web.comet.ProjectDetailCometRenderer
import scala.collection.JavaConversions.iterableAsScalaIterable
import no.officenet.example.rpm.web.lib.{CloseDialog, JQueryDialog}

@Configurable
class ProjectSnippet extends Loggable {

	@Resource
	val projectAppService: ProjectAppService = null

	@Resource
	val userService: UserService = null

	object SomeRequestVar extends RequestVar("Hello")

	var listMemoize: Box[IdMemoizeTransform] = Empty

	private def openNewProjectDialog = {
		".openNewProjectDialog" #> SHtml.ajaxButton(L(ProjectTexts.V.button_newProjectDialog_text), () => {
			val project = new Project(new DateTime, userService.findByUserName(SecurityContextHolder.getContext.getAuthentication.getName).getOrElse(null))
			project.projectType = ProjectType.scrum
			projectVar.set(new ProjectDto(project))
			val dialogId = nextFuncName
			afterProjectSaveVar.set(() => CloseDialog(dialogId) & listMemoize.map(m => Replace(m.latestId, m.applyAgain())).openOr(Noop))
			val dialog = JQueryDialog(S.runTemplate(List(ProjectDetailCometRenderer.newProjectTemplate)).openOr(<div>Template {ProjectDetailCometRenderer.newProjectTemplate} not found</div>),
				L(ProjectTexts.V.projectDialog_title_newProject),
				false, dialogId)
			dialog.open
		})
	}

	/**
	 * This function is passed as a callback to the ProjectEditSnippet. Its purpose is to update the editButtonContainer
	 * (the last TD in the project-list) with an updated modified-timestamp after saving and closing the popup.
	 */
	def refreshProject(rowId: String, ns: NodeSeq): (Project) => JsCmd = {
		(project: Project) => {
			Replace(rowId, renderProjectRow(rowId, project).apply(ns))
		}
	}

	def list = {
		".openNewProjectDialog" #> openNewProjectDialog &
		".projectListTable" #> SHtml.idMemoize(idMemoize => {
			listMemoize = Full(idMemoize)
			".projectBodyRow" #> projectAppService.findAll.map(project => {
				val rowId = nextFuncName
				renderProjectRow(rowId, project)
			})
		})
	}

	private def renderProjectRow(rowId: String, project: Project) = {
		"tr" #> ((ns: NodeSeq) => {
			"tr [id]" #> rowId &
			".projectName *" #> ProjectLoc.createLink(ProjectViewParam(project.id)).map(l => SHtml.link(l.toString(), () => (), Text(project.name))) &
			".projectType *" #> L(project.projectType.wrapped) &
			".showActivitiesButton *" #> SHtml.ajaxButton(L(ProjectTexts.V.button_showActivities_text), () => showActivitiesForProject(project)) &
			".createdDate *" #> Localizer.formatDate(L(GlobalTexts.dateformat_fullDateTime), project.created.toDate,
				S.locale) &
			".projectBudget *" #> Localizer.formatLong(project.budget) &
			".projectEstimatedStart *" #> Localizer.formatDateTime(L(GlobalTexts.dateformat_fullDateTime),
				Box.legacyNullTest(project.estimatedStartDate),
				S.locale) &
			".createdBy *" #> project.createdBy.displayName &
			".editButtonContainer" #> (
				".lastModified *" #> Localizer.formatDateTime(L(GlobalTexts.dateformat_fullDateTimeSeconds),
					Option(project.modified)).getOrElse("<not-modified>") &
				".editButton" #> getEditButtonLink(rowId, project.id, ns)
				)
		}.apply(ns))
	}

	private def showActivitiesForProject(project: Project): JsCmd = {
		val template = "templates-hidden/project/activitiesForProject"
		val nodeSeq = S.runTemplate(List(template)).map(xhtml => {
			(
			if (project.activityList.isEmpty) {
				"*" #> L_!(ProjectTexts.V.activityListIsEmpty, project.name)
			} else {
				".activitiesForProjectHeader" #> L_!(ProjectTexts.V.header_activitiesForProject_text,
													 project.activityList.size, project.name) &
				"tbody" #> (
						   ".activityRow" #> project.activityList.map(activity => {
							   ".activityName *" #> activity.name
						   })
						   )
			}
			).apply(xhtml)}).openOr(<div>Template {template} not found</div>)
		JQueryDialog(nodeSeq).open
	}

	private def getEditButtonLink(rowId: String, projectId: java.lang.Long, ns: NodeSeq) = {
		SHtml.a(() => {
			trace("retrieving projectId: " + projectId)
			projectVar.set(projectAppService.retrieve(projectId)) // The popup uses this to access the selected project
			projectUpdatedCallbackFuncVar.set(refreshProject(rowId, ns)) // The callback-func to run after successfully saving in popup
			// Put the handle for the popup in a request-var so that the pop is able to close itself.
			val dialogId = nextFuncName
			afterProjectSaveVar.set(() => CloseDialog(dialogId))
			val dialog = JQueryDialog(S.runTemplate(List(ProjectDetailCometRenderer.newProjectTemplate)).openOr(<div>Template {ProjectDetailCometRenderer.newProjectTemplate} not found</div>),
				L(ProjectTexts.V.projectDialog_title_newProject),
				false, dialogId)
			dialog.open
		},
				Text(L(GlobalTexts.button_edit)))
	}

}