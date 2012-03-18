package no.officenet.example.rpm.web.menu

import net.liftweb.sitemap.{Loc, Menu}

object RpmMenu {
	def menu = {
		(Menu("Index") / "index" >> Loc.Hidden) ::
		Menu(ProjectEditDialogWrapperLoc) ::
		Menu(ProjectViewWrapperLoc) ::
		Menu(ProjectListLoc, Menu(ProjectLoc)) ::
		Menu(BlogViewLoc, Menu(BlogEntryViewLoc)) ::
		Nil
	}
}