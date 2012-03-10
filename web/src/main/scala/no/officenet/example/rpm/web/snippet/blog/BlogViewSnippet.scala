package no.officenet.example.rpm.web.snippet.blog

import net.liftweb.http._
import net.liftweb.util.Helpers._
import js._
import js.JsCmds._
import org.springframework.beans.factory.annotation.Configurable
import javax.annotation.Resource
import xml.NodeSeq
import no.officenet.example.rpm.web.menu.BlogViewParam
import no.officenet.example.rpm.web.lib.{LiftUtils, ValidatableScreen, JpaFormFields}
import no.officenet.example.rpm.blog.domain.model.entities.{BlogEntryJPAFields, BlogEntry}
import no.officenet.example.rpm.web.lib.RolfJsCmds.{SlideUp, SlideDown}
import no.officenet.example.rpm.blog.domain.service.{BlogEntryService, BlogService}
import no.officenet.example.rpm.support.domain.model.entities.User
import no.officenet.example.rpm.web.lib.ContextVars._
import net.liftweb.common.Full
import java.util.concurrent.atomic.AtomicBoolean

@Configurable
class BlogViewSnippet(blogViewParam: BlogViewParam) extends ValidatableScreen with JpaFormFields {

	@Resource
	private val blogService: BlogService = null
	@Resource
	val blogEntryService: BlogEntryService = null

	var newBlogPost: BlogEntry = null

	def render = {
		".blogEntryList" #> ((ns: NodeSeq) => {
			val blogId: Long = blogVar.get.get.id
			// The extra "frontpage" is to make the name different so that a separate actor is created for
			// the front-page to allow different formatting (more social-like)
			val cometName = List(S.locale, blogId, "frontpage")
			<div class={"lift:comet?type=BlogCometActor;name="+cometName.mkString(":")}
				 style="display: inline;">
				{ns}
			</div>
		})
	}

	override def renderScreen() = {
		".inputContainer" #> LiftUtils.getLoggedInUser.map(user => {
			val hideOnShow = new AtomicBoolean(true)
			newBlogPost = new BlogEntry(blogVar.get.get, user)
			val newBlogEntryDomID = nextFuncName
			SHtml.idMemoize{idMemoize =>
				".title" #> JpaTextField(newBlogPost, BlogEntryJPAFields.title, newBlogPost.title, (v: String) => newBlogPost.title = v).
					withAttrs("onfocus" -> SlideDown(newBlogEntryDomID, "slow").toJsCmd, "placeholder" -> "New entry").
					disableInPlaceValidation()  &
				".blogDetailsInput [id]" #> newBlogEntryDomID &
				".blogDetailsInput [style]" #> Full("display: none").filter(v => hideOnShow.get()) &
				".blogDetailsInput" #> (
					".summary" #> JpaTextAreaField(newBlogPost, BlogEntryJPAFields.summary, newBlogPost.summary, (v: String) => newBlogPost.summary = v) &
						".content" #> JpaTextAreaField(newBlogPost, BlogEntryJPAFields.content, newBlogPost.content, (v: String) => newBlogPost.content = v) &
						":submit" #> SHtml.ajaxSubmit("Save", () => saveBlogEntry(idMemoize, user, hideOnShow, newBlogEntryDomID)) &
						".cancel [onclick]" #> SHtml.ajaxInvoke(() => {
							newBlogPost = new BlogEntry(blogVar.get.get, user)
							hideOnShow.set(false)
							idMemoize.setHtml() & SlideUp(newBlogEntryDomID, "slow")
						})._2.toJsCmd
					)
			}
		})
	}

	private def saveBlogEntry(idMemoize: IdMemoizeTransform, user: User,
							  hideOnShow: AtomicBoolean, newBlogEntryDomID: String): JsCmd = {
		if (!hasErrors) {
			if (newBlogPost.id == null) {
				blogEntryService.createBlogEntry(newBlogPost)
			} else {
				newBlogPost.modifiedBy = user
				blogEntryService.updateBlogEntry(newBlogPost)
			}
			hideOnShow.set(true)
			newBlogPost = new BlogEntry(blogVar.get.get, user)
			idMemoize.setHtml() & SlideUp(newBlogEntryDomID, "slow")
		} else {
			hideOnShow.set(false)
			idMemoize.setHtml()
		}
	}

}