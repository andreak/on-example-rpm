package no.officenet.example.rpm.web.lib

import net.liftweb._
import common.Box
import http._
import provider._

import java.util.Locale
import no.officenet.example.rpm.support.infrastructure.logging.Loggable
import org.springframework.context.i18n.LocaleContextHolder

object UrlLocalizer extends Loggable {

	/**
	 * What are the available locales?
	 */
	val locales: Map[String, Locale] =
		Map(Locale.getAvailableLocales.map(l => l.toString -> l) :_*)

	object currentLocale extends RequestVar(Locale.getDefault)

	/**
	 * Extract the locale
	 */
	def unapply(in: String): Option[Locale] = {
		val locale = locales.get(in)
		locale.foreach{l =>
			LocaleContextHolder.setLocale(l)
			currentLocale.set(l)
		}
		locale
	}

	/**
	 * Calculate the Locale
	 */
	def calcLocale(in: Box[HTTPRequest]): Locale =
		if (LocaleContextHolder.getLocaleContext != null) {
			val locale = LocaleContextHolder.getLocale
			trace("calcLocale: using LocaleContextHolder.getLocale: " + locale)
			locale
		} else if (currentLocale.set_?) {
			val locale = currentLocale.get
			trace("calcLocale: set_? == TRUE: " + locale)
			LocaleContextHolder.setLocale(locale)
			locale
		}
		else {
			// Use the browser's locale or system's default
			val locale = in.flatMap(r => r.locale).openOr(Locale.getDefault)
			trace("calcLocale: using Locale.getDefault: " + locale)
			locale
		}

}
