package no.officenet.example.rpm.web.menu

import net.liftweb.sitemap.{Loc, Menu}

object RpmMenu {
	def menu = {
		(Menu("Index") / "index" >> Loc.Hidden) ::
		(Menu("Project-view wrapperForJSF") / "lift" / "project" / "projectViewWrapperForJSF" >> Loc.Hidden) ::
		(Menu("Project-view wrapperForJSF") / "lift" / "project" / "projectEditDialogWrapperForJSF" >> Loc.Hidden) ::
		Menu(ProjectListLoc) ::
		Menu(ProjectLoc) ::
		Nil
	}
}