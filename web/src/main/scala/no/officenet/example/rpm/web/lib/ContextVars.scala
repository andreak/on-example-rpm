package no.officenet.example.rpm.web.lib

import net.liftweb.util.Helpers._
import no.officenet.example.rpm.projectmgmt.application.dto.ProjectDto
import no.officenet.example.rpm.projectmgmt.application.service.ProjectAppService
import org.springframework.beans.factory.annotation.Configurable
import net.liftweb.http.{S, RequestVar}
import javax.annotation.Resource
import java.lang.IllegalArgumentException
import no.officenet.example.rpm.support.domain.service.UserService
import org.springframework.security.core.context.SecurityContextHolder
import no.officenet.example.rpm.support.infrastructure.scala.lang.ControlHelpers.?
import net.liftweb.common.{Empty, Box}
import no.officenet.example.rpm.support.domain.model.entities.{AbstractDomainObject, User}
import no.officenet.example.rpm.blog.domain.model.entities.{Blog, Comment, BlogEntry}
import no.officenet.example.rpm.blog.domain.service.BlogService
import java.util.concurrent.ExecutorService

@Configurable
object ContextVars {
	
	@Resource
	val projectAppService: ProjectAppService = null
	@Resource
	val blogService: BlogService = null
	@Resource
	val userService: UserService = null

	object projectVar extends RequestVar[ProjectDto](new ProjectDto)

	object BlogEntryVar extends RequestVar[BlogEntry](new BlogEntry)

	object loggedInUserVar extends RequestVar[Box[User]](?(SecurityContextHolder.getContext.getAuthentication).
		flatMap(auth => userService.findByUserName(auth.getName)))

	object CommentedEntityVar extends RequestVar[Box[AbstractDomainObject]](Empty)

	object CommentVar extends RequestVar[Comment](new Comment)

	object blogVar extends RequestVar[Option[Blog]](LiftUtils.getLoggedInUser.flatMap(user => blogService.findByNameForUser("main", user.userName)))

}