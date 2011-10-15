package no.officenet.example.rpm.web.snippet

import net.liftweb.http.S


class RedirectToCurrentLocaleSnippet {

	def render = S.redirectTo("/"+S.locale+"/project")

}