package bootstrap.liftweb

import net.liftweb.http._
import net.liftweb.util._
import net.liftweb.util.Helpers._
import net.liftweb.common._
import net.liftweb.http.js.JE.JsRaw
import net.liftweb.http.js.jquery.JQuery14Artifacts

import no.officenet.example.rpm.web.snippet.I18n
import no.officenet.example.rpm.support.infrastructure.util.ResourceBundleHelper
import no.officenet.example.rpm.web.lib.UrlLocalizer
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.context.i18n.LocaleContextHolder
import no.officenet.example.rpm.support.infrastructure.logging.Loggable

class Boot extends Loggable {
	def boot() {

		Logger.setup = Full(() => ()) // Do nothing. We don't want Lift to try to mess up our logging. Having log4j.xml in classpath is sufficient

		LiftRules.templateSuffixes = "lift" :: LiftRules.templateSuffixes
		LiftRules.snippetNamesToSearch.default.set(() => LiftRules.searchSnippetsWithRequestPath(_))
		LiftRules.addToPackages("no.officenet.example.rpm.web")

		LiftRules.loggedInTest = Full(() => SecurityContextHolder.getContext.getAuthentication != null)

		LiftRules.snippetDispatch.append(Map("i18n" -> I18n))

		LiftRules.ajaxStart = Full(() => JsRaw("Rolf.liftAjaxStart()").cmd)
		LiftRules.ajaxEnd = Full(() => JsRaw("Rolf.liftAjaxEnd()").cmd)
/*
		LiftRules.ajaxDefaultFailure = Full(() => JsRaw("Rolf.liftAjaxErrorHandler()").cmd)
*/
		LiftRules.ajaxPostTimeout = (5 minutes).toInt

		// Force the request to be UTF-8
		LiftRules.early.append(_.setCharacterEncoding("UTF-8"))

		LiftRules.jsArtifacts = JQuery14Artifacts

		S.addAround(
			new LoanWrapper  {
				def apply[T](f: => T) : T =  {
					LocaleContextHolder.setLocale(S.locale)
					info("Locale used in S.locale: " + S.locale)
					f
				}
			}
		)

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

		// initialize the localizer
		UrlLocalizer.init()

	}

}