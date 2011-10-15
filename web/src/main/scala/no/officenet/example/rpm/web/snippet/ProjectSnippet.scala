package no.officenet.example.rpm.web.snippet

import net.liftweb._
import common.Box
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
import no.officenet.example.rpm.web.lib.{RolfJsCmds, JQueryDialog}
import no.officenet.example.rpm.web.lib.ContextVars._
import no.officenet.example.rpm.pets.domain.model.entities.Pet
import no.officenet.example.rpm.support.domain.i18n.{GlobalTexts, Localizer}
import no.officenet.example.rpm.support.domain.i18n.Localizer.L
import no.officenet.example.rpm.support.domain.i18n.Localizer.L_!
import no.officenet.example.rpm.web.menu.{ProjectLoc, ProjectViewParam}
import no.officenet.example.rpm.web.comet.ProjectDetailCometRenderer
import scala.collection.JavaConversions.iterableAsScalaIterable

@Configurable
class ProjectSnippet extends Loggable {

	@Resource
	val projectAppService: ProjectAppService = null

	@Resource
	val userService: UserService = null

	object SomeRequestVar extends RequestVar("Hello")

	def openNewProjectDialog = {
		".openNewProjectDialog" #> SHtml.ajaxButton(L(ProjectTexts.V.button_newProjectDialog_text), () => {
			val project = new Project(new DateTime, userService.findByUserName(SecurityContextHolder.getContext.getAuthentication.getName).getOrElse(null))
			project.projectType = ProjectType.scrum
			projectVar.set(new ProjectDto(project, new Pet()))
			editProjectDialogVar.set(JQueryDialog(S.runTemplate(List(ProjectDetailCometRenderer.newProjectTemplate)).openOr(<div>Template {ProjectDetailCometRenderer.newProjectTemplate} not found</div>),
												  L(ProjectTexts.V.projectDialog_title_newProject)))
			editProjectDialogVar.get.open
		})
	}

	def list = {
		val snapshot = SomeRequestVar.generateSnapshotRestorer[JsCmd]()

		".projectListTable" #> (
							   ".projectBodyRow" #> projectAppService.findAll.map(project => {
								   val buttonContainerId = nextFuncName
								   val petIdBox = Box.legacyNullTest(project.petId)
								   "tr [data-json]" #> ("{'id': " + project.id + ", 'name':"+ project.name.encJs + "}") &
								   ".projectName *" #> ProjectLoc.createLink(ProjectViewParam(project.id.toString)).map(l => SHtml.link(l.toString(), () => (), Text(project.name))) &
								   ".projectType *" #> L(project.projectType.wrapped) &
								   ".showActivitiesButton *" #> SHtml.ajaxButton(L(ProjectTexts.V.button_showActivities_text), () => showActivitiesForProject(project)) &
								   ".createdDate *" #> Localizer.formatDate(L(GlobalTexts.dateformat_fullDateTime), project.created.toDate,
																  S.locale) &
								   ".projectBudget *" #> Localizer.formatLong(project.budget) &
								   ".projectEstimatedStart *" #> Localizer.formatDateTime(L(GlobalTexts.dateformat_fullDateTime),
																				Box.legacyNullTest(project.estimatedStartDate),
																				S.locale) &
								   ".createdBy *" #> project.createdBy.displayName &
								   ".petId *" #> petIdBox.map(_.toString) &
								   ".petName *" #> petIdBox.map(id => <lift:GetPetName petId={id.toString} parallel="true" />) &
								   ".editButtonContainer" #> ((ns:NodeSeq) =>
									   renderButtonContainer(buttonContainerId,
														 project,
														 ns.asInstanceOf[Elem] % ("id" -> buttonContainerId), snapshot)
										   (ns.asInstanceOf[Elem] % ("id" -> buttonContainerId)))
							   }))
	}

	private def showActivitiesForProject(project: Project): JsCmd = {
		val template = "templates-hidden/project/activitiesForProject"
		val nodeSeq = S.runTemplate(List(template)).map(xhtml => {
			(
			if (project.activityList.isEmpty) {
				"*" #> L_!(ProjectTexts.V.activityListIsEmpty, project.name)
			} else {
				".activitiesForProjectHeader" #> L_!(ProjectTexts.V.header_activitiesForProject_text,
													 project.activityList.size.asInstanceOf[AnyRef], project.name) &
				"tbody" #> (
						   ".activityRow" #> project.activityList.map(activity => {
							   ".activityName *" #> activity.name
						   })
						   )
			}
			).apply(xhtml)}).openOr(<div>Template {template} not found</div>)
		JQueryDialog(nodeSeq).open
	}

	private def renderButtonContainer(buttonContainerId: String, project: Project, ns:NodeSeq, snapshot: (() => JsCmd) => JsCmd) = {
		".lastModified *" #> Localizer.formatDateTime(L(GlobalTexts.dateformat_fullDateTimeSeconds),
											Option(project.modified)).getOrElse("<not-modified>") &
		".editButton" #> getEditButtonLink(buttonContainerId, project.id, ns, snapshot)
	}

	private def getEditButtonLink(buttonContainerId: String, projectId: java.lang.Long, ns:NodeSeq, snapshot: (() => JsCmd) => JsCmd) = {
		SHtml.a(() => {
			trace("retrieving projectId: " + projectId)
			projectVar.set(projectAppService.retrieve(projectId)) // The popup uses this to access the selected project
			projectUpdatedCallbackFuncVar.set(refreshProject(buttonContainerId, ns, snapshot)) // The callback-func to run after successfully saving in popup
			// Put the handle for the popup in a request-var so that the pop is able to close itself.
			editProjectDialogVar.set(JQueryDialog(S.runTemplate(List(ProjectDetailCometRenderer.newProjectTemplate)).openOr(<div>Template {ProjectDetailCometRenderer.newProjectTemplate} not found</div>),
												  L(ProjectTexts.V.projectDialog_title_newProject)))
			editProjectDialogVar.get.open
		},
				Text(L(GlobalTexts.button_edit)))
	}

	/**
	 * This function is passed as a callback to the ProjectEditSnippet. Its purpose is to update the editButtonContainer
	 * (the last TD in the project-list) with an updated modified-timestamp after saving and closing the popup.
	 * <p/>
	 * todo: When {@link #renderButtonContainer} gets called as a callback (Line 116 in {@link ProjectEditSnippet#render}), it renders the link in the "ajax-context"
	 * of the popup-dialog (backed by ProjectEditSnippet), not in the context of this project-listing (ProjectSnippet).
	 * The result of this is that clicking on the (then updated) "Edit"-link after saving in the popup returns the same
	 * instance of ProjectEditSnippet, which we don't want. We always want a new instance of ProjectEditSnippet
	 * for each project we click "Edit" on.
	 */
	def refreshProject(buttonContainerId: String, ns:NodeSeq, snapshot: (() => JsCmd) => JsCmd): (Project) => JsCmd = {
		(project: Project) => {
			val jsCmd = snapshot(() => RolfJsCmds.JqReplaceWith(buttonContainerId, renderButtonContainer(buttonContainerId, project, ns, snapshot)(ns)))
			jsCmd
		}
	}


}