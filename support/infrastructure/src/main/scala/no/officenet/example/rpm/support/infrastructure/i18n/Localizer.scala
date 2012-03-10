package no.officenet.example.rpm.support.infrastructure.i18n

import org.springframework.context.i18n.LocaleContextHolder
import java.util.{Date, Locale, MissingResourceException}
import org.joda.time.DateTime
import java.text.{ParsePosition, SimpleDateFormat, DecimalFormatSymbols, DecimalFormat}
import no.officenet.example.rpm.support.infrastructure.errorhandling.InvalidDateException
import xml.{Unparsed, NodeSeq, Text}

object Localizer {

	def strToNodeSeq(str: String) = NodeSeq.fromSeq(Unparsed(str))

	object L_! {
		def apply(withBundle: WithBundle, arguments: AnyRef*): NodeSeq =
			apply(withBundle.bundle, withBundle.resourceKey, arguments: _*)

		def apply(bundle: Bundle.ExtendedValue, resourceKey: String, arguments: AnyRef*): NodeSeq = {
			try {
				strToNodeSeq(ResourceBundleHelper.getMessage(LocaleContextHolder.getLocale, bundle.path, resourceKey, arguments: _*))
			} catch {
				case e: MissingResourceException => Text(e.getMessage + " " + e.getClassName + ": " + e.getKey)
			}
		}
	}

	object L {
		def apply(withBundle: WithBundle, arguments: AnyRef*): String = {
			apply(LocaleContextHolder.getLocale, withBundle.bundle, withBundle.resourceKey, arguments: _*)
		}

		def apply(locale: Locale, withBundle: WithBundle, arguments: AnyRef*): String = {
			apply(locale, withBundle.bundle, withBundle.resourceKey, arguments: _*)
		}

		def apply(bundle: Bundle.ExtendedValue, resourceKey: String, arguments: AnyRef*): String =
			apply(LocaleContextHolder.getLocale, bundle, resourceKey, arguments: _*)

		def apply(locale: Locale, bundle: Bundle.ExtendedValue, resourceKey: String, arguments: AnyRef*): String = {
			ResourceBundleHelper.getMessage(locale, bundle.path, resourceKey, arguments: _*)
		}

	}

	def formatDate(pattern: String, date: Date, locale: Locale): String = {
		if (date == null) throw new IllegalArgumentException("date cannot be null. Use Option(date) with formatDate(pattern: String, date: Option[Date], locale: Locale) if you have nullable dates")
		if (locale == null) throw new IllegalArgumentException("locale cannot be null")
		try {
			formatDateString(pattern, date, locale) match {
				case s: String if !s.isEmpty => s
			}
		} catch {
			case e => throw new InvalidDateException("Unable to format date: " + date + " with pattern: " + pattern)
		}
	}

	def formatFullDate(date: Date): String = formatDate(L(GlobalTexts.dateformat_fullDateTime), date, LocaleContextHolder.getLocale)

	def formatDate(date: Date): String = formatDate(L(GlobalTexts.dateformat_fullDate), date, LocaleContextHolder.getLocale)

	def formatDate(pattern: String, date: Option[Date]): Option[String] = {
		date.map(d => formatDate(pattern, d, LocaleContextHolder.getLocale))
	}

	def formatDate(pattern: String, date: Option[Date], locale: Locale): Option[String] = {
		date.map(d => formatDate(pattern, d, locale))
	}

	def formatDateTime(pattern: String, date: Option[DateTime], locale: Locale): Option[String] = {
		date.map(d => formatDate(pattern, d.toDate, locale))
	}

	def formatDateTime(pattern: String, date: Option[DateTime]): Option[String] = formatDateTime(pattern, date, LocaleContextHolder.getLocale)

	def getDateFromString(pattern: String, dateString: String): Date = {
		try {
			toDate(dateString, pattern) match {
				case date: Date => date
				case _ => throw new InvalidDateException("Invalid date-string: " + dateString)
			}
		} catch {
			case e => throw new InvalidDateException(e)
		}
	}

	def getDateTimeFromString(pattern: String, dateString: String): DateTime = {
		try {
			toDate(dateString, pattern) match {
				case date: Date => new DateTime(date.getTime)
				case _ => throw new InvalidDateException("Invalid date-string: " + dateString)
			}
		} catch {
			case e => throw new InvalidDateException(e)
		}
	}

	def formatDecimalNumber(value: Double): String = {
		val df = new DecimalFormat(L(GlobalTexts.decimalFormat_pattern))
		df.setDecimalFormatSymbols(createDecimalFormatSymbol);
		df.format(value)
	}

	def formatDecimalNumber(value: Option[Double]): Option[String] = {
		value.map(v => formatDecimalNumber(v))
	}

	def formatBigDecimal(value: BigDecimal): String = {
		formatDouble(value.toDouble)
	}

	def formatBigDecimal(value: Option[java.math.BigDecimal]): Option[String] = {
		value.map(v => formatBigDecimal(v))
	}

	def formatLong(value: Long) = formatDouble(value.toDouble)

	def formatLong(value: Option[Long]): Option[String] = value.map(v => formatLong(v))

	def formatInteger(value: Int) = formatDouble(value.toDouble)

	def formatInteger(value: Option[java.lang.Integer]): Option[String] = value.map(v => formatInteger(v))

	def formatDouble(value: Option[java.lang.Double]): Option[String] = value.map(v => formatDouble(v))

	def formatDouble(value: Double): String = {
		val nf = new DecimalFormat()
		nf.setMaximumFractionDigits(0)
		nf.setDecimalFormatSymbols(createDecimalFormatSymbol)
		nf.format(value)
	}

	private def createDecimalFormatSymbol: DecimalFormatSymbols = {
		val dfs = new DecimalFormatSymbols()
		dfs.setGroupingSeparator(L(GlobalTexts.numberFormat_groupingSeparator).charAt(0))
		dfs.setDecimalSeparator(L(GlobalTexts.numberFormat_decimalSeparator).charAt(0))
		dfs
	}

	private def toDate(value: String, format: String): Date = {
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

	private def formatDateString(format: String, date: Date, locale: Locale): String = {
		if (date == null) return ""
		(new SimpleDateFormat(format, locale)).format(date)
	}

}