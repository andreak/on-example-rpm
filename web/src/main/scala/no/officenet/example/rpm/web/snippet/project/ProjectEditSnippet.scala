package no.officenet.example.rpm.web.snippet.project

import net.liftweb._
import common.{Box, Full}
import http._
import js._
import js.JsCmds._
import js.jquery.JqJsCmds.{Show, Hide}
import util.Helpers._

import org.springframework.beans.factory.annotation.Configurable
import no.officenet.example.rpm.projectmgmt.application.service.ProjectAppService
import javax.annotation.Resource
import no.officenet.example.rpm.web.lib.ContextVars._
import no.officenet.example.rpm.projectmgmt.domain.model.entities.Project
import org.joda.time.DateTime
import no.officenet.example.rpm.support.domain.service.UserService
import xml.NodeSeq
import no.officenet.example.rpm.projectmgmt.domain.model.enums.{ProjectColor, ProjectType, ProjectTexts}
import no.officenet.example.rpm.support.infrastructure.i18n.{Localizer, Bundle, GlobalTexts}
import no.officenet.example.rpm.web.lib.{NaturalNumberMask, JpaFormFields, ValidatableScreen}
import no.officenet.example.rpm.macros.Macros.?
import no.officenet.example.rpm.web.lib.LiftUtils

object DisplayRadioWithLabelHorizontallyTemplate {
	def buildElement(item: SHtml.ChoiceItem[(String, String)]): NodeSeq = {
		<label>{item.xhtml} {item.key._2}</label>
	}
	def toForm(holder: SHtml.ChoiceHolder[(String, String)]): NodeSeq =
		holder.flatMap(buildElement _)
}

object projectUpdatedCallbackFuncVar extends RequestVar[Project => JsCmd]((project: Project) => Noop)
object afterProjectSaveVar extends RequestVar[() => JsCmd](() => Noop)

@Configurable
class ProjectEditSnippet extends ValidatableScreen with JpaFormFields with TransientSnippet {

	val defaultBundle = Bundle.PROJECT_D

	@Resource
	val projectAppService: ProjectAppService = null

	@Resource
	val userService: UserService = null

	var projectDto = projectVar.get

	def project: Project = projectDto.project

	def isNewEntity = projectDto.project.id == null

	val afterProjectSave = afterProjectSaveVar.get

	trace("\n\n********************** ctor: " + this + " + project: " + projectDto.project)

	override protected def renderScreen() = {
		trace("\n\n***** this: " + this)
		val projectTypes: Seq[(ProjectType.ExtendedValue, List[SHtml.ElemAttr])] = ProjectType.getValues.zipWithIndex.map{case (v,index) => {
			val attrs: List[SHtml.ElemAttr] = Nil
			(v, attrs)
		}}
		var selectedColor = ProjectColor.BLACK.name
		val radioValues = ProjectColor.getValues.map(st => (st.name, st.wrapped.localize))
		val niceColorIdKey = nextFuncName
		val badColorIdKey = nextFuncName

		val colorLabelMap = Map(
			ProjectColor.BLACK.name -> niceColorIdKey,
			ProjectColor.GREEN.name -> niceColorIdKey,
			ProjectColor.PINK.name -> niceColorIdKey,
			ProjectColor.RED.name -> badColorIdKey,
			ProjectColor.ORANGE.name -> badColorIdKey
		)

		val checkedColor = Full(ProjectColor.valueOf(selectedColor).get.name, ProjectColor.valueOf(selectedColor).get.wrapped.localize)

		val labelKeyForSelectedSuretyType = colorLabelMap.get(selectedColor).get

		def getStyleForLabel(label: String) = {
			if (labelKeyForSelectedSuretyType == label) ""
			else "display: none"
		}

		"*" #> SHtml.idMemoize(id => {
			".projectName *" #> JpaTextField(project, Project.name, ?(project.name), (v: Option[String]) => project.name = v.orNull).
				withContainer(TdInputContainer(ProjectTexts.D.name.localize)) &
			".projectDescription *" #> JpaTextAreaField(project, Project.description, project.description, (v: Option[String]) => project.description = v).
				withContainer(TdInputContainer(ProjectTexts.D.description.localize)) &
			".projectType *" #> JpaSelectField(project, Project.projectType, projectTypes.toList, ?(project.projectType),
											(pt: ProjectType.ExtendedValue) => project.projectType = pt,
											(pt: ProjectType.ExtendedValue, idx) => pt.wrapped.localize).
				withContainer(TdInputContainer(ProjectTexts.D.projectType.localize)) &
			".project_color_radio *" #> DisplayRadioWithLabelHorizontallyTemplate.toForm(
				LiftUtils.ritchRadioElem(radioValues,
						 checkedColor,
					LiftUtils.StrFuncElemAttr[(String, String)]("onchange", {(t:(String, String)) =>
							 val key = t._1
							 colorLabelMap.keys.map(k => Hide(colorLabelMap(k))).toList &
							 Show(colorLabelMap(key))
						 }))
				{_.map{case(key,value) => selectedColor = key}}
			) &
			".nice_color_id [id]" #> niceColorIdKey &
			".nice_color_id [style]" #> getStyleForLabel(niceColorIdKey) &
			".bad_color_id [id]" #> badColorIdKey &
			".bad_color_id [style]" #> getStyleForLabel(badColorIdKey) &
			".budget *" #> JpaTextField(project, Project.budget, project.budget.map(d => d.toString), (v: Option[Long]) => project.budget = v).
				withContainer(TdInputContainer(ProjectTexts.V.projectDialog_details_label_budget.localize)).
				withInputMask(NaturalNumberMask) &
			".estimatedStart *" #> JpaDateField(project, Project.estimatedStartDate,
											 Localizer.formatDateTime(GlobalTexts.dateformat_fullDate.localize,
																	  project.estimatedStartDate),
											 (v: Option[DateTime]) => project.estimatedStartDate = v).
				withContainer(TdInputContainer(ProjectTexts.V.projectDialog_details_label_estimatedStartDate.localize +
											   "(%s)".format(GlobalTexts.dateformat_fullDate.localize))) &
			".saveButton" #> SHtml.ajaxSubmit(ProjectTexts.V.projectDialog_button_save.localize, () => {
				trace("Saving")
				if (!hasErrors) {
					projectDto = if (isNewEntity) {
						projectAppService.create(projectDto)
					} else {
						projectAppService.update(projectDto)
					}
					projectVar.set(projectDto)
					projectUpdatedCallbackFuncVar.apply(projectDto.project) &
					afterProjectSave()
				} else {
					trace("errors found, re-rendering form")
					id.setHtml()
				}
			})
		})
	}

}