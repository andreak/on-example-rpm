package no.officenet.example.rpm.web.snippet.blog

import net.liftweb._
import http.js.{JsCmds, JsCmd}
import http.{TransientSnippet, RequestVar, IdMemoizeTransform, SHtml}
import util._
import util.Helpers._
import org.springframework.beans.factory.annotation.Configurable
import no.officenet.example.rpm.blog.domain.model.entities.BlogEntryJPAFields._
import javax.annotation.Resource
import no.officenet.example.rpm.web.lib.ContextVars._
import no.officenet.example.rpm.support.infrastructure.logging.Loggable
import no.officenet.example.rpm.blog.domain.service.BlogEntryService
import no.officenet.example.rpm.web.lib.{LiftUtils, JpaFormFields, ValidatableScreen}
import no.officenet.example.rpm.support.infrastructure.scala.lang.ControlHelpers.?

object afterBlogEntrySaveVar extends RequestVar[() => JsCmd](() => JsCmds.Noop)

@Configurable
class NewBlogPostDialogSnippet extends ValidatableScreen with JpaFormFields with TransientSnippet with Loggable {

	@Resource
	val blogEntryService: BlogEntryService = null

	val blogPost = BlogEntryVar.get
	val afterSave = afterBlogEntrySaveVar.get

	// Must be here for ValidatableScreen
	override protected def renderScreen() = {
			".inputContainer" #> SHtml.idMemoize {
				idMemoize =>
					".title" #> JpaTextField(blogPost, title, ?(blogPost.title), (v: Option[String]) => v.foreach(blogPost.title = _)) &
						".summary" #> JpaTextAreaField(blogPost, summary, ?(blogPost.summary), (v: Option[String]) => v.foreach(blogPost.summary = _)) &
						".content" #> JpaTextAreaField(blogPost, content, ?(blogPost.content), (v: Option[String]) => v.foreach(blogPost.content = _)) &
						":submit" #> SHtml.ajaxSubmit("Save", () => saveBlogEntry(idMemoize))
			}
	}

	private def saveBlogEntry(idMemoize: IdMemoizeTransform): JsCmd = {
		if (!hasErrors) {
			if (blogPost.id == null) {
				blogEntryService.createBlogEntry(blogPost)
			} else {
				LiftUtils.getLoggedInUser.foreach(user => blogPost.modifiedByOpt = Some(user))
				blogEntryService.updateBlogEntry(blogPost)
			}
			afterSave()
		} else {
			idMemoize.setHtml()
		}
	}


}