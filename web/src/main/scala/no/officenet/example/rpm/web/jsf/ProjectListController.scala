package no.officenet.example.rpm.web.jsf

import org.springframework.stereotype.Controller
import org.springframework.context.annotation.Scope
import no.officenet.example.rpm.projectmgmt.application.service.ProjectAppService
import reflect.BeanProperty
import collection.JavaConversions.bufferAsJavaList
import org.springframework.beans.factory.annotation.Autowired
import no.officenet.example.rpm.projectmgmt.domain.model.entities.Project

@Controller
@Scope("view")
class ProjectListController @Autowired() (projectAppService: ProjectAppService) {

	case class ProjectBean(project: Project) {
		@BeanProperty
		var id = project.id
		@BeanProperty
		var name = project.name
		@BeanProperty
		var description = project.description.orNull
		@BeanProperty
		var created = project.created.toDate
		@BeanProperty
		var createdBy = project.createdBy.displayName
	}

	@BeanProperty
	val projectList = bufferAsJavaList(projectAppService.findAll.map(ProjectBean(_)))
}