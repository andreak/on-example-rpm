package no.officenet.example.rpm.web.jsf

import org.springframework.stereotype.Controller
import org.springframework.context.annotation.Scope
import no.officenet.example.rpm.projectmgmt.application.service.ProjectAppService
import reflect.BeanProperty
import org.springframework.beans.factory.annotation.Autowired
import no.officenet.example.rpm.projectmgmt.application.dto.ProjectDto

@Controller
@Scope("view")
class ProjectViewController @Autowired() (projectAppService: ProjectAppService) {

	case class ProjectBean(projectDto: ProjectDto) {
		val project = projectDto.project

		@BeanProperty
		var id = project.id
		@BeanProperty
		var name = project.name
		@BeanProperty
		var description = project.description.orNull
		@BeanProperty
		var created = project.created
		@BeanProperty
		var createdBy = project.createdBy.displayName
	}

	@BeanProperty
	var id: java.lang.Long = null

	private var project: ProjectBean = null

	def getProject = {
		if (project == null) {
			project = ProjectBean(projectAppService.retrieve(id))
		}
		project
	}
}