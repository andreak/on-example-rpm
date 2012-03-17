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
import no.officenet.example.rpm.support.infrastructure.i18n.Localizer.L
import no.officenet.example.rpm.web.lib.{NaturalNumberMask, JpaFormFields, ValidatableScreen}

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
			val attrs: List[SHtml.ElemAttr] = if (index % 2 == 0) List[SHtml.ElemAttr]("disabled" -> "disabled") else Nil
			(v, attrs)
		}}
		var selectedColor = ProjectColor.BLACK.name
		val radioValues = ProjectColor.getValues.map(st => (st.name, L(st.wrapped)))
		val niceColorIdKey = nextFuncName
		val badColorIdKey = nextFuncName

		val colorLabelMap = Map(
			ProjectColor.BLACK.name -> niceColorIdKey,
			ProjectColor.GREEN.name -> niceColorIdKey,
			ProjectColor.PINK.name -> niceColorIdKey,
			ProjectColor.RED.name -> badColorIdKey,
			ProjectColor.ORANGE.name -> badColorIdKey
		)

		val checkedColor = Full(ProjectColor.valueOf(selectedColor).get.name, L(ProjectColor.valueOf(selectedColor).get.wrapped))

		val labelKeyForSelectedSuretyType = colorLabelMap.get(selectedColor).get

		def getStyleForLabel(label: String) = {
			if (labelKeyForSelectedSuretyType == label) ""
			else "display: none"
		}
		val firstOption: (ProjectType.ExtendedValue, scala.List[SHtml.ElemAttr]) =
			(null.asInstanceOf[ProjectType.ExtendedValue], List[SHtml.ElemAttr]("disabled" -> "disabled"))

		"*" #> SHtml.idMemoize(id => {
			".projectName *" #> JpaTextField(project, Project.name, project.name, (v:String) => project.name = v).
				withContainer(TdInputContainer(L(ProjectTexts.D.name))) &
			".projectDescription *" #> JpaTextAreaField(project, Project.description, project.description.getOrElse(""), (v: Option[String]) => project.description = v).
				withContainer(TdInputContainer(L(ProjectTexts.D.description))) &
			".projectType *" #> JpaSelectField(project, Project.projectType, firstOption :: projectTypes.toList, project.projectType,
											(pt: ProjectType.ExtendedValue) => project.projectType = pt,
											(pt: ProjectType.ExtendedValue, idx) => if (idx == 0) L(GlobalTexts.select_noItemSelected) else L(pt.wrapped)).
				withContainer(TdInputContainer(L(ProjectTexts.D.projectType))) &
			".project_color_radio *" #> DisplayRadioWithLabelHorizontallyTemplate.toForm(
				ritchRadioElem(radioValues,
						 checkedColor,
						 StrFuncElemAttr[(String, String)]("onchange", {(t:(String, String)) =>
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
			".budget *" #> JpaTextField(project, Project.budget, project.budget.map(d => d.toString).getOrElse(""), (v: Option[Long]) => project.budget = v).
				withContainer(TdInputContainer(L(ProjectTexts.V.projectDialog_details_label_budget))).
				withInputMask(NaturalNumberMask()) &
			".estimatedStart *" #> JpaDateField(project, Project.estimatedStartDate,
											 Localizer.formatDateTime(L(GlobalTexts.dateformat_fullDate),
																	  Box.legacyNullTest(project.estimatedStartDate)).getOrElse(""),
											 (v: DateTime) => project.estimatedStartDate = v).
				withContainer(TdInputContainer(L(ProjectTexts.V.projectDialog_details_label_estimatedStartDate) +
											   "(%s)".format(L(GlobalTexts.dateformat_fullDate)))) &
			".saveButton" #> SHtml.ajaxSubmit(L(ProjectTexts.V.projectDialog_button_save), () => {
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