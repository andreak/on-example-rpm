package no.officenet.example.rpm.web.snippet

import net.liftweb._
import util.Helpers._

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.beans.factory.annotation.Configurable
import javax.annotation.Resource
import xml.Text
import no.officenet.example.rpm.support.domain.service.UserService
import no.officenet.example.rpm.support.domain.i18n.GlobalTexts
import no.officenet.example.rpm.support.domain.i18n.Localizer.L_!
import no.officenet.example.rpm.support.domain.i18n.Localizer.L

@Configurable
class UserSnippet {

	@Resource
	val userService: UserService = null

	def username = "* *" #> getUserName

	def displayName = "* *" #> userService.findByUserName(getUserName).map(user => Text(user.displayName)).
		getOrElse(L_!(GlobalTexts.error_userNotFound, getUserName))

	private def getUserName = SecurityContextHolder.getContext.getAuthentication.getName

}