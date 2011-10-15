package bootstrap.liftweb

import net.liftweb.http._
import net.liftweb.util._
import net.liftweb.util.Helpers._
import net.liftweb.common._
import net.liftweb.http.js.JE.JsRaw
import net.liftweb.http.js.jquery.JQuery14Artifacts

import no.officenet.example.rpm.web.snippet.I18n
import no.officenet.example.rpm.support.infrastructure.util.ResourceBundleHelper
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.context.i18n.LocaleContextHolder
import net.liftweb.sitemap.SiteMap
import no.officenet.example.rpm.web.menu.RpmMenu
import no.officenet.example.rpm.web.errorhandling.ExceptionHandlerDelegate
import no.officenet.example.rpm.support.domain.i18n.GlobalTexts
import no.officenet.example.rpm.support.domain.i18n.Localizer._
import no.officenet.example.rpm.web.lib.{ErrorDialog, UrlLocalizer}
import xml.Text

class Boot {
	def boot() {
		// Do nothing. We don't want Lift to try to mess up our logging. Having log4j.xml in classpath is sufficient
		Logger.setup = Full(() => ())

		LiftRules.templateSuffixes = "lift" :: LiftRules.templateSuffixes
		LiftRules.snippetNamesToSearch.default.set(() => LiftRules.searchSnippetsWithRequestPath(_))
		LiftRules.addToPackages("no.officenet.example.rpm.web")

		LiftRules.loggedInTest = Full(() => SecurityContextHolder.getContext.getAuthentication != null)

		LiftRules.snippetDispatch.append(Map("i18n" -> I18n))

		LiftRules.ajaxStart = Full(() => JsRaw("Rolf.liftAjaxStart()").cmd)
		LiftRules.ajaxEnd = Full(() => JsRaw("Rolf.liftAjaxEnd()").cmd)
		LiftRules.ajaxDefaultFailure = Full(() => ErrorDialog(L(GlobalTexts.exception_popup_title),
															  Text(L(GlobalTexts.error_popup_serverError)), None).open)
		LiftRules.ajaxPostTimeout = (5 minutes).toInt

		// Force the request to be UTF-8
		LiftRules.early.append(_.setCharacterEncoding("UTF-8"))

		LiftRules.jsArtifacts = JQuery14Artifacts

 		LiftRules.onBeginServicing.append(req => {
			Props.mode match {
				case Props.RunModes.Development =>
					ResourceBundleHelper.resetCachedFormats()
				case _ =>
			}
		})

		LiftRules.onEndServicing.append((req, liftResponse) => {
			LocaleContextHolder.resetLocaleContext()
		})

		LiftRules.localeCalculator = UrlLocalizer.calcLocale

		SiteMap.enforceUniqueLinks = false
		LiftRules.setSiteMapFunc(() => SiteMap(RpmMenu.menu:_*))

		ExceptionHandlerDelegate.setUpLiftExceptionHandler()
	}

}