package no.officenet.example.rpm.web.lib

import net.liftweb._
import common.Box
import http._
import provider._

import java.util.Locale
import no.officenet.example.rpm.support.infrastructure.logging.Loggable
import org.springframework.context.i18n.LocaleContextHolder

object UrlLocalizer extends Loggable {
	// capture the old localization function
	val oldLocalizeFunc = LiftRules.localeCalculator

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
		// if it's a valid locale, it matches
		val locale = locales.get(in)
		locale.foreach(l => LocaleContextHolder.setLocale(l))
		locale
	}

	/**
	 * Calculate the Locale
	 */
	def calcLocale(in: Box[HTTPRequest]): Locale =
		if (currentLocale.set_?) {
			val locale = currentLocale.get
			trace("calcLocale: set_? == TRUE: " + locale)
			LocaleContextHolder.setLocale(locale)
			locale
		}
		else {
			if (LocaleContextHolder.getLocaleContext != null) {
				val locale = LocaleContextHolder.getLocale
				trace("calcLocale: using LocaleContextHolder.getLocale: " + locale + ", currentLocale.get: " + currentLocale.get)
				locale
			} else {
				val locale = oldLocalizeFunc(in)
				trace("calcLocale: using oldLocalizeFunc: " + locale)
				locale
			}
		}

}
