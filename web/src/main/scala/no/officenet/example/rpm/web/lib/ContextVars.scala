package no.officenet.example.rpm.web.lib

import net.liftweb.util.Helpers._
import no.officenet.example.rpm.projectmgmt.application.dto.ProjectDto
import no.officenet.example.rpm.projectmgmt.application.service.ProjectAppService
import org.springframework.beans.factory.annotation.Configurable
import net.liftweb.http.{S, RequestVar}
import javax.annotation.Resource
import java.lang.IllegalArgumentException

@Configurable
object ContextVars {
	
	@Resource
	val projectAppService: ProjectAppService = null
	object projectVar extends RequestVar[ProjectDto](asLong(S.param("id")).map(id => projectAppService.retrieve(id)).
														 getOrElse(S.param("id").map(id => {
		throw new IllegalArgumentException("Invalid projectId: " + id)
	}).get))
}