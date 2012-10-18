package no.officenet.example.rpm.support.infrastructure.i18n

import org.springframework.context.i18n.LocaleContextHolder
import java.util.{Date, Locale, MissingResourceException}
import org.joda.time.{LocalTime, LocalDate, Duration, DateTime}
import java.text.{DecimalFormatSymbols, DecimalFormat}
import xml.{Unparsed, NodeSeq, Text}
import org.joda.time.format.{PeriodFormatter, PeriodFormatterBuilder}

object Localizer {

	def formatDuration(duration: Option[Duration]): Option[String] = {
		val builder = new PeriodFormatterBuilder()
		.appendDays()
		.appendHours()
		.appendMinutes()
		duration.map(_.toPeriod().toString(new PeriodFormatter(
			builder.toPrinter, builder.toParser
		)))
	}

	def strToNodeSeq(str: String) = NodeSeq.fromSeq(Unparsed(str))

	object L_! {
		def apply(withBundle: WithBundle, arguments: Any*): NodeSeq =
			apply(withBundle.bundle, withBundle.resourceKey, arguments:_*)

		def apply(bundle: Bundle.ExtendedValue, resourceKey: String, arguments: Any*): NodeSeq = {
			try {
				strToNodeSeq(ResourceBundleHelper.getMessage(LocaleContextHolder.getLocale, bundle.path, resourceKey, arguments:_*))
			} catch {
				case e: MissingResourceException => Text(e.getMessage + " " + e.getClassName + ": " + e.getKey)
			}
		}
	}

	object L {
		def apply(withBundle: WithBundle, arguments: Any*): String = {
			apply(LocaleContextHolder.getLocale, withBundle.bundle, withBundle.resourceKey, arguments:_*)
		}

		def apply(locale: Locale, withBundle: WithBundle, arguments: Any*): String = {
			apply(locale, withBundle.bundle, withBundle.resourceKey, arguments:_*)
		}

		def apply(bundle: Bundle.ExtendedValue, resourceKey: String, arguments: Any*): String =
			apply(LocaleContextHolder.getLocale, bundle, resourceKey, arguments:_*)

		def apply(locale: Locale, bundle: Bundle.ExtendedValue, resourceKey: String, arguments: Any*): String = {
			ResourceBundleHelper.getMessage(locale, bundle.path, resourceKey, arguments:_*)
		}

	}

	def formatDate(pattern: String, date: Date, locale: Locale): String = {
		DateFormatter.format(pattern, date, locale)
	}

	def formatFullDate(date:Date):String = formatDate(L(GlobalTexts.dateformat_fullDateTime), date, LocaleContextHolder.getLocale)

	def formatDate(date:Date):String = formatDate(L(GlobalTexts.dateformat_fullDate), date, LocaleContextHolder.getLocale)

	def formatDate(pattern: String, date: Option[Date]): Option[String] = {
		date.map(d => formatDate(pattern, d, LocaleContextHolder.getLocale))
	}

	def formatDate(pattern: String, date: Option[Date], locale: Locale): Option[String] = {
		date.map(d => formatDate(pattern, d, locale))
	}

	def formatDateTime(pattern: String, date: Option[DateTime], locale: Locale): Option[String] = {
		date.map(d => formatDate(pattern, d.toDate, locale))
	}

	def formatLocalTime(pattern: String, time: Option[LocalTime], locale: Locale): Option[String] = {
		time.map(d => d.toString(pattern, locale))
	}

	def formatDateTime(pattern: String, date: Option[DateTime]):Option[String] = formatDateTime(pattern, date, LocaleContextHolder.getLocale)

	def formatLocalTime(pattern: String, time: Option[LocalTime]):Option[String] = formatLocalTime(pattern, time, LocaleContextHolder.getLocale)

	def formatLocalDate(pattern: String, date: Option[LocalDate]):Option[String] = date.map(d => formatDate(pattern, d.toDateTimeAtStartOfDay.toDate, LocaleContextHolder.getLocale))

	def getDateFromString(pattern: String, dateString: String): Date = {
		DateFormatter.parse(dateString, pattern)
	}

	def getDateTimeFromString(pattern: String, dateString: String): DateTime = {
		new DateTime(DateFormatter.parse(dateString, pattern).getTime)
	}

	def formatDecimalNumber(value: Double): String= {
		val df = new DecimalFormat(L(GlobalTexts.decimalFormat_pattern))
		df.setDecimalFormatSymbols(createDecimalFormatSymbol);
		df.format(value)
	}

	def formatDecimalNumber(value: Option[Double]): Option[String] = {
		value.map(v => formatDecimalNumber(v))
	}

	def formatDecimal(value: BigDecimal): String = formatDecimalNumber(value.toDouble)

	def formatBigDecimal(value: BigDecimal): String = {
		formatDouble(value.toDouble)
	}

	def formatBigDecimal(value: Option[java.math.BigDecimal]): Option[String] = {
		value.map(v => formatBigDecimal(v))
	}

	def formatLong(value:Long) = formatDouble(value.toDouble)

	def formatLong(value: Option[Long]): Option[String] = value.map(v => formatLong(v))

	def formatInteger(value:Int) = formatDouble(value.toDouble)

	def formatInteger(value: Option[java.lang.Integer]): Option[String] = value.map(v => formatInteger(v))

	def formatDouble(value: Option[java.lang.Double]): Option[String] = value.map(v => formatDouble(v))

	def formatDouble(value:Double): String= {
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

	def percentFormatter(value: Option[java.math.BigDecimal]): String = {
		value.map(v => "%s %%" format formatDecimal(BigDecimal(v))).getOrElse("")
	}

}