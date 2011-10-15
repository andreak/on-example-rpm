package no.officenet.example.rpm.web.menu

import net.liftweb.sitemap.Loc
import net.liftweb.common.Full._
import xml.Text._
import net.liftweb.sitemap.Loc._
import net.liftweb.util.NamedPF._
import net.liftweb.http.RewriteResponse._
import net.liftweb.http.{RewriteResponse, ParsePath, RewriteRequest, S}
import net.liftweb.util.NamedPF
import xml.{Text, NodeSeq}

import no.officenet.example.rpm.support.domain.i18n.Localizer.L_!
import no.officenet.example.rpm.support.domain.i18n.Localizer.L
import no.officenet.example.rpm.support.domain.i18n.Bundle
import no.officenet.example.rpm.web.menu.LocHelper._
import no.officenet.example.rpm.web.lib.{ContextVars, UrlLocalizer}
import no.officenet.example.rpm.projectmgmt.application.dto.ProjectDto
import net.liftweb.common.{Empty, Box, Full}

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

object ProjectLoc extends Loc[ProjectParam] with LocalizableMenu {
	def name = "project"
	def defaultValue = S.param("id").map(ProjectViewParam(_))

	val link = new Loc.Link[ProjectParam](List("lift", "project", "projectView"), false){
		override def createLink(in: ProjectParam) = Full(Text("/" + UrlLocalizer.currentLocale.get + "/project/" + in.id))
	}

	def params = MenuCssClass("top") :: Nil

	val text = new Loc.LinkText(calcLinkText _)

	def calcLinkText(in: ProjectParam): NodeSeq = L_menu_!("project", in.id)

	override val rewrite: LocRewrite = Full(NamedPF("Project rewrite") {
		case RewriteRequest(ParsePath(UrlLocalizer(locale) :: "project" :: projectId :: Nil,_,_,_),_,_) => {
			UrlLocalizer.currentLocale.set(locale)
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
			UrlLocalizer.currentLocale.set(locale)
			(RewriteResponse("lift" :: "project" :: "projectList" :: Nil, Map.empty, true), ())
		}
	})
}
