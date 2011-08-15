package no.officenet.example.rpm.web.snippet

import net.liftweb._
import util.Helpers._

import no.officenet.example.rpm.web.lib.Localizable
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.beans.factory.annotation.Configurable
import javax.annotation.Resource
import no.officenet.example.rpm.support.domain.util.{GlobalTexts, Bundle}
import xml.Text
import no.officenet.example.rpm.support.domain.service.UserService

@Configurable
class UserSnippet extends Localizable {

	@Resource
	val userService: UserService = null

	val defaultBundle = Bundle.GLOBAL

	def username = "* *" #> getUserName

	def displayName = "* *" #> userService.findByUserName(getUserName).map(user => Text(user.displayName)).
		getOrElse(L_!(GlobalTexts.error_userNotFound, getUserName))

	private def getUserName = SecurityContextHolder.getContext.getAuthentication.getName

}