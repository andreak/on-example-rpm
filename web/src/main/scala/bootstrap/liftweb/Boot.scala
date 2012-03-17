package bootstrap.liftweb

import net.liftweb.http._
import net.liftweb.util._
import net.liftweb.util.Helpers._
import net.liftweb.http.js.JE.JsRaw
import net.liftweb.http.js.jquery.JQuery14Artifacts

import no.officenet.example.rpm.web.snippet.I18n
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.context.i18n.LocaleContextHolder
import net.liftweb.sitemap.SiteMap
import no.officenet.example.rpm.web.menu.RpmMenu
import no.officenet.example.rpm.web.errorhandling.ExceptionHandlerDelegate
import no.officenet.example.rpm.support.infrastructure.i18n.Localizer.L
import no.officenet.example.rpm.support.infrastructure.i18n.{GlobalTexts, ResourceBundleHelper}
import xml.Text
import java.util.concurrent.ExecutorService
import net.liftweb.actor.{ILAExecute, LAScheduler}
import net.liftweb.common.{Full, Logger}
import no.officenet.example.rpm.support.infrastructure.logging.Loggable
import no.officenet.example.rpm.web.lib.{ContextVars, ErrorDialog, UrlLocalizer}

class Boot {

	def boot() {
		// Do nothing. We don't want Lift to try to mess up our logging. Having log4j.xml in classpath is sufficient
		Logger.setup = Full(() => ())

		// Use custom executor-service to be able to monitor it using JMX. Lift's is private so we need to install our own
		setupLiftScheduler(ContextVars.liftSchedulerExecutor)

		LiftRules.htmlProperties.default.set((r: Req) => new XHtmlInHtml5OutProperties(r.userAgent))

		LiftRules.templateSuffixes = "lift" :: LiftRules.templateSuffixes

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

	def setupLiftScheduler(liftSchedulerExecutor: ExecutorService) {
		LAScheduler.createExecutor = () => {
			new ILAExecute with Loggable {
				def execute(f: () => Unit) {
					liftSchedulerExecutor.execute(new Runnable{
						def run() {
							try {
								f()
							} catch {
								case e: Exception => log.error("Lift Actor Scheduler", e)
							}
						}})
				}

				def shutdown() {}
			}
		}
	}


}