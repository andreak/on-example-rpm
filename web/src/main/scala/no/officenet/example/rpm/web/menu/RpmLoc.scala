package no.officenet.example.rpm.web.menu

import net.liftweb.sitemap.Loc
import net.liftweb.common.Full._
import xml.Text._
import net.liftweb.sitemap.Loc._
import net.liftweb.util.NamedPF._
import net.liftweb.http.RewriteResponse._
import net.liftweb.http.{RewriteResponse, ParsePath, RewriteRequest, S}
import net.liftweb.util.NamedPF
import net.liftweb.util.Helpers._
import xml.{Text, NodeSeq}

import no.officenet.example.rpm.support.infrastructure.i18n.Localizer.L_!
import no.officenet.example.rpm.support.infrastructure.i18n.{Localizer, Bundle}
import no.officenet.example.rpm.support.infrastructure.i18n.Localizer.L
import no.officenet.example.rpm.web.menu.LocHelper._
import no.officenet.example.rpm.web.lib.{ContextVars, UrlLocalizer}
import no.officenet.example.rpm.projectmgmt.application.dto.ProjectDto
import net.liftweb.common.{Empty, Box, Full}
import no.officenet.example.rpm.blog.domain.service.BlogService
import org.springframework.beans.factory.annotation.Configurable
import javax.annotation.Resource
import org.springframework.security.core.context.SecurityContextHolder
import no.officenet.example.rpm.projectmgmt.application.service.ProjectAppService

object LocHelper {
	implicit def list2ParsePath(list: List[String]) = ParsePath(list, "", true, false)
}

trait LocalizableMenu {
	def L_menu_!(key:String, arguments: AnyRef*) = L_!(Bundle.MENU, "menu." + key, arguments:_*)

	def L_menu(key:String, arguments: AnyRef*) = L(Bundle.MENU, "menu." + key, arguments:_*)
}

trait ProjectParam {
	val id: String
	lazy val projectDto: ProjectDto = ContextVars.projectVar.get
}

case class ProjectViewParam(id: String) extends ProjectParam

@Configurable
object ProjectLoc extends Loc[ProjectParam] with LocalizableMenu {
	@Resource
	val projectAppService: ProjectAppService = null
	def name = "project"
	def defaultValue = S.param("id").map(ProjectViewParam(_)) // When this is Empty, the menu-item will not get displayed

	val link = new Loc.Link[ProjectParam](List("lift", "project", "projectView"), false){
		override def createLink(in: ProjectParam) = Full(Text("/" + UrlLocalizer.currentLocale.get + "/project/" + in.id))
	}

	def params = MenuCssClass("top") :: Nil

	val text = new Loc.LinkText(calcLinkText _)

	def calcLinkText(in: ProjectParam): NodeSeq = L_menu_!("project", in.id)

	override val rewrite: LocRewrite = Full(NamedPF("Project rewrite") {
		case RewriteRequest(ParsePath(UrlLocalizer(locale) :: "project" :: projectId :: Nil,_,_,_),_,_) => {
			asLong(projectId).foreach(id => ContextVars.projectVar.set(projectAppService.retrieve(id)))
			(RewriteResponse("lift" :: "project" :: "projectView" :: Nil, Map("id" -> projectId), true), ProjectViewParam(projectId))
		}
	})
}

object ProjectListLoc extends Loc[Unit] with LocalizableMenu {
	def name = "projectList"
	def defaultValue = Full(())

	val link = new Loc.Link[Unit](List("lift", "project", "projectList"), false){
		override def createLink(in: Unit) = Full(Text("/" + UrlLocalizer.currentLocale.get + "/project"))
	}

	def params = MenuCssClass("top") :: Nil

	val text = new Loc.LinkText(calcLinkText _)

	def calcLinkText(in: Unit): NodeSeq = L_menu_!("projects")

	override val rewrite: LocRewrite = Full(NamedPF("Project-list rewrite") {
		case RewriteRequest(ParsePath(UrlLocalizer(locale) :: "project" :: Nil,_,_,_),_,_) => {
			(RewriteResponse("lift" :: "project" :: "projectList" :: Nil, Map.empty, true), ())
		}
	})
}

case class BlogViewParam(userName: String)

@Configurable
object BlogViewLoc extends Loc[BlogViewParam] with LocalizableMenu {

	@Resource
	private val blogService: BlogService = null

	def name = "Blog view"
	def defaultValue = Option(SecurityContextHolder.getContext.getAuthentication).map(auth =>
		(BlogViewParam(auth.getName)))

	val link = new Loc.Link[BlogViewParam](List("lift", "blog", "blogView"), false){
		override def createLink(in: BlogViewParam) = Full(Text("/" + UrlLocalizer.currentLocale.get + "/user/" + in.userName + "/blog"))
	}

	def params = MenuCssClass("top") :: Nil

	val text = new Loc.LinkText(calcLinkText _)

	def calcLinkText(in: BlogViewParam): NodeSeq = L_menu_!("blog", in.userName)

	override val rewrite: LocRewrite = Full(NamedPF("Blog-view rewrite") {
		case RewriteRequest(ParsePath(UrlLocalizer(locale) :: "user" :: userName :: "blog" :: Nil,_,_,_),_,_) => {
			val blog = blogService.findByNameForUser("main", userName)
			ContextVars.blogVar.set(blog)
			(RewriteResponse("lift" :: "blog" :: "blogView" :: Nil,
				Map("userName" -> userName), true),
				BlogViewParam(userName))
		}
	})
}

case class BlogEntryViewParam(userName: String, blogEntryId: Long)

object BlogEntryViewLoc extends Loc[BlogEntryViewParam] with LocalizableMenu {
	def name = "Blog-entry view"
	def defaultValue = Empty

	val link = new Loc.Link[BlogEntryViewParam](List("lift", "blog", "blogEntryView"), false){
		override def createLink(in: BlogEntryViewParam) = Full(Text("/" + UrlLocalizer.currentLocale.get + "/user/" + in.userName + "/blog/main/" + in.blogEntryId))
	}

	def params = Hidden :: Nil

	val text = new Loc.LinkText(calcLinkText _)

	def calcLinkText(in: BlogEntryViewParam): NodeSeq = L_menu_!("blogEntry", in.userName)

	override val rewrite: LocRewrite = Full(NamedPF("BlogEntry-view rewrite") {
		case RewriteRequest(ParsePath(UrlLocalizer(locale) :: "user" :: userName :: "blog" :: "main" :: blogEntryId :: Nil,_,_,_),_,_) => {
			(RewriteResponse("lift" :: "blog" :: "blogEntryView" :: Nil, Map.empty, true), BlogEntryViewParam(userName, blogEntryId.toLong))
		}
	})
}

object ProjectEditDialogWrapperLoc extends Loc[Unit] {
	def name = "editProjectWrapper"
	def defaultValue = Full(())

	val link = new Loc.Link[Unit](List("lift", "project", "projectEditDialogWrapperForJSF"), false){
		override def createLink(in: Unit) = Empty
	}

	def params = Loc.Hidden :: Nil

	val text = new Loc.LinkText(calcLinkText _)

	def calcLinkText(in: Unit): NodeSeq = NodeSeq.Empty

	override val rewrite: LocRewrite = Full(NamedPF("editProjectWrapper rewrite") {
		case RewriteRequest(ParsePath(UrlLocalizer(locale) :: "wrapper" :: "project" :: "projectEditDialogWrapper" :: Nil,_,_,_),_,_) => {
			(RewriteResponse("lift" :: "project" :: "projectEditDialogWrapperForJSF" :: Nil, Map.empty, true), ())
		}
	})


}

object ProjectViewWrapperLoc extends Loc[Unit] {
	def name = "viewProjectWrapper"
	def defaultValue = Full(())

	val link = new Loc.Link[Unit](List("lift", "project", "projectViewWrapperForJSF"), false){
		override def createLink(in: Unit) = Empty
	}

	def params = Loc.Hidden :: Nil

	val text = new Loc.LinkText(calcLinkText _)

	def calcLinkText(in: Unit): NodeSeq = NodeSeq.Empty

	override val rewrite: LocRewrite = Full(NamedPF("viewProjectWrapper rewrite") {
		case RewriteRequest(ParsePath(UrlLocalizer(locale) :: "wrapper" :: "project" :: "projectViewWrapperForJSF" :: Nil,_,_,_),_,_) => {
			(RewriteResponse("lift" :: "project" :: "projectViewWrapperForJSF" :: Nil, Map.empty, true), ())
		}
	})


}
