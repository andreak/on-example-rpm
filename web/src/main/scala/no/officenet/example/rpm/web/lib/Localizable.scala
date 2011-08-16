package no.officenet.example.rpm.web.lib

import net.liftweb._
import http._
import common._
import xml.{XML, NodeSeq, Text}
import no.officenet.example.rpm.support.infrastructure.util.ResourceBundleHelper
import java.util.{Date, MissingResourceException, Locale}
import java.text.{ParsePosition, SimpleDateFormat, DecimalFormatSymbols, DecimalFormat}
import no.officenet.example.rpm.support.domain.util.{GlobalTexts, WithBundle, Bundle}
import org.joda.time.DateTime

class InvalidDateException(message: String, t: Throwable) extends RuntimeException(message, t) {
	def this(t: Throwable) {
		this(null, t)
	}
	def this(message: String) {
		this(message, null)
	}
}

trait Localizable {

	def strToNodeSeq(str: String) = NodeSeq.fromSeq(XML.loadString("<x>"+str+"</x>").child.toSeq)

	val defaultBundle: Bundle.ExtendedValue

	def L(resourceKey: String, arguments: AnyRef*): String =
		L(defaultBundle, resourceKey, arguments:_*)

	def L(locale: Locale, resourceKey: String, arguments: AnyRef*): String =
		L(locale, defaultBundle, resourceKey, arguments:_*)

	def L(withBundle: WithBundle, arguments: AnyRef*): String =
		L(S.locale, withBundle.bundle, withBundle.resourceKey, arguments:_*)

	def L(bundle: Bundle.ExtendedValue, resourceKey: String, arguments: AnyRef*): String =
		L(S.locale, bundle, resourceKey, arguments:_*)

	def L(locale: Locale, bundle: Bundle.ExtendedValue, resourceKey: String, arguments: AnyRef*): String =
		try {
			ResourceBundleHelper.getMessage(locale, bundle.path(), resourceKey, arguments:_*)
		} catch {
			case e: MissingResourceException => e.getMessage + " " + e.getClassName + ": " + e.getKey
		}

	def L_!(withBundle: WithBundle, arguments: AnyRef*): NodeSeq ={
		try {
			strToNodeSeq(ResourceBundleHelper.getMessage(S.locale, withBundle.bundle.path(), withBundle.resourceKey, arguments:_*))
		} catch {
			case e: MissingResourceException => Text(e.getMessage + " " + e.getClassName + ": " + e.getKey)
		}
	}

	def formatDate(pattern: String, date: Date, locale: Locale): String = {
		if (date == null) throw new IllegalArgumentException("date cannot be null")
		if (locale == null) throw new IllegalArgumentException("locale cannot be null")
		try {
			formatDateString(pattern, date, locale) match {
				case s: String if !s.isEmpty => s
			}
		} catch {
			case e => throw new InvalidDateException("Unable to format date: " + date + " with pattern: " + pattern)
		}
	}

	def formatDate(pattern: String, date: Box[Date], locale: Locale): Box[String] = {
		date.map(d => formatDate(pattern, d, locale))
	}

	def formatDateTime(pattern: String, date: Box[DateTime], locale: Locale): Box[String] = {
		date.map(d => formatDate(pattern, d.toDate, locale))
	}

	def getDateFromString(pattern: String, dateString: String): Date = {
		try {toDate(dateString, pattern) match {
			case date: Date => date
			case _ => throw new InvalidDateException("Invalid date-string: " + dateString)
		}} catch {
			case e => throw new InvalidDateException(e)
		}
	}

	def getDateTimeFromString(pattern: String, dateString: String): DateTime = {
		try {toDate(dateString, pattern) match {
			case date: Date => new DateTime(date.getTime)
			case _ => throw new InvalidDateException("Invalid date-string: " + dateString)
		}} catch {
			case e => throw new InvalidDateException(e)
		}
	}

	def formatDecimalNumber(value:Double):String= {
		val df = new DecimalFormat(L(GlobalTexts.decimalFormat_pattern))
		df.setDecimalFormatSymbols(createDecimalFormatSymbol)
		df.format(value)
	}

	def formatDecimalNumber(value: Box[Double]): Box[String] = {
		value.map(v => formatDecimalNumber(v))
	}

	def formatLong(value:Long) = formatNumber(value.toDouble)

	def formatLong(value: Box[java.lang.Long]): Box[String] = value.map(v => formatLong(v))

	def formatInteger(value:Int) = formatNumber(value.toDouble)
	def formatInteger(value: Box[java.lang.Integer]): Box[String] = value.map(v => formatInteger(v))

	def formatNumber(value: Box[java.lang.Double]):Box[String] = value.map(v => formatNumber(v))
	def formatNumber(value:Double):String= {
		val nf = new DecimalFormat()
		nf.setMaximumFractionDigits(0)
		nf.setDecimalFormatSymbols(createDecimalFormatSymbol)
		nf.format(value)
	}

	private	def createDecimalFormatSymbol: DecimalFormatSymbols = {
		val dfs = new DecimalFormatSymbols()
		dfs.setGroupingSeparator(L(GlobalTexts.numberFormat_groupingSeparator).charAt(0))
		dfs.setDecimalSeparator(L(GlobalTexts.numberFormat_decimalSeparator).charAt(0))
		dfs
	}

	def toDate (value: String, format: String): Date = {
		var s = value
		if (s == null || format == null) {
			return null
		}
		val idx = s.lastIndexOf("+")
		if (idx != -1) {
			var zone = s.substring(idx)
			zone = zone.replaceAll(":", "")
			s = s.substring(0, idx).concat(zone)
		}
		var date: Date = null
		try {
			date = new SimpleDateFormat(format).parse(s, new ParsePosition(0))
		} catch {
			case e =>
		}
		date
	}

	private def formatDateString(format: String , date: Date, locale: Locale): String = {
		if (date == null) return ""
		(new SimpleDateFormat(format, locale)).format(date)
	}


}