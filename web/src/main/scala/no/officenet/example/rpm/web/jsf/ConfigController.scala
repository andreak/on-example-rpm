package no.officenet.example.rpm.web.jsf

import org.springframework.stereotype.Controller
import org.springframework.context.annotation.Scope
import java.util.Locale
import org.springframework.context.i18n.LocaleContextHolder
import javax.faces.context.FacesContext
import org.apache.commons.lang.StringUtils
import javax.servlet.http.{HttpSession, HttpServletRequest}
import no.officenet.example.rpm.web.lib.UrlLocalizer
import net.liftweb.http.LiftRules

@Controller
@Scope("view")
class ConfigController {

	def getLocale: Locale = {
		val localeKey = "RPM_LOCALE_KEY"
		val servletRequest = FacesContext.getCurrentInstance.getExternalContext.getRequest.asInstanceOf[HttpServletRequest]
		val session = FacesContext.getCurrentInstance.getExternalContext.getSession(true).asInstanceOf[HttpSession]

		val langFromParameter = servletRequest.getParameter("lang")
		if (!StringUtils.isBlank(langFromParameter)) {
			UrlLocalizer.locales.get(langFromParameter).foreach(locale => session.setAttribute(localeKey, locale))
		}

		if (session.getAttribute(localeKey) == null) {
			session.setAttribute(localeKey, servletRequest.getLocale)
		}

		session.getAttribute(localeKey).asInstanceOf[Locale]
	}

	def getLiftAjaxPath = LiftRules.ajaxPath
}