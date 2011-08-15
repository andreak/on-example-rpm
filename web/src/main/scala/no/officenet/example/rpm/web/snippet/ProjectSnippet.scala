package no.officenet.example.rpm.web.snippet

import net.liftweb._
import common.Box
import http._
import util.Helpers._

import org.springframework.beans.factory.annotation.Configurable
import no.officenet.example.rpm.projectmgmt.application.service.ProjectAppService
import javax.annotation.Resource
import no.officenet.example.rpm.support.domain.util.{GlobalTexts, Bundle}
import no.officenet.example.rpm.web.lib.Localizable

@Configurable
class ProjectSnippet extends Localizable {

	val defaultBundle = Bundle.PROJECT_D

	@Resource
	val projectAppService: ProjectAppService = null

	def list = {
		".projectListTable" #> (
							   ".projectBodyRow" #> projectAppService.findAll.map(project => {
								   "tr [data-json]" #> ("{'id': " + project.id + ", 'name':"+ project.name.encJs + "}") &
								   ".projectName *" #> project.name &
								   ".projectType *" #> L(project.projectType.wrapped) &
								   ".createdDate *" #> formatDate(L(GlobalTexts.dateformat_fullDateTime), project.created.toDate, S.locale) &
								   ".projectBudget *" #> formatLong(Box.legacyNullTest(project.budget)) &
								   ".projectEstimatedStart *" #> formatDateTime(L(GlobalTexts.dateformat_fullDateTime), Box.legacyNullTest(project.estimatedStartDate), S.locale) &
								   ".createdBy *" #> project.createdBy.displayName &
								   ".petName" #> "" /* How to make this parallel??*/
							   })
							   )
	}

}