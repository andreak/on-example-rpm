package no.officenet.example.rpm.support.infrastructure.i18n

import org.joda.time.DateTime
import java.util.{Locale, Date}
import org.springframework.context.i18n.LocaleContextHolder
import Localizer._
import java.text.{ParsePosition, SimpleDateFormat}
import no.officenet.example.rpm.support.infrastructure.errorhandling.{InvalidDateFormatException, InvalidDateInputException}


object DateFormatter {
	implicit def dateTimeToDate(in: DateTime): Date = in.toDate

	implicit def dateToDateTime(in: Date): DateTime = new DateTime(in.getTime)

	def format(pattern: String, date: Date, locale: Locale): String = {
		if (date == null) throw new IllegalArgumentException("date cannot be null. Use Option(date) with formatDate(pattern: String, date: Option[Date], locale: Locale) if you have nullable dates")
		if (locale == null) throw new IllegalArgumentException("locale cannot be null")
		try {
			(new SimpleDateFormat(pattern, locale)).format(date)
		} catch {
			case e => throw new InvalidDateFormatException(pattern)
		}
	}


	private def format(pattern: String, date: Option[Date]): String = {
		date.map(d => format(pattern, d, LocaleContextHolder.getLocale)).getOrElse("")
	}

	def formatFullDate(date: Option[Date]): String = {
		format(L(GlobalTexts.dateformat_fullDate), date)
	}


	def formatTimeWithSeconds(time: Date): String = {
		format(L(GlobalTexts.timeformat_timeWithSeconds), Some(time))
	}


	def formatFullDateTime(date: Date): String = format(L(GlobalTexts.dateformat_fullDateTime), date, LocaleContextHolder.getLocale)

	def formatFullDate(date: Date): String = format(L(GlobalTexts.dateformat_fullDate), date, LocaleContextHolder.getLocale)


	def parseFullDate(dateString: String): Date = {
		parse(L(GlobalTexts.dateformat_fullDate), dateString)
	}


	def parse(pattern: String, dateString: String): Date = {
		(try {
			parseDate(dateString, pattern)
		} catch {
			case e => throw new InvalidDateFormatException(pattern)
		}) match {
			case Some(date) => date
			case _ => throw new InvalidDateInputException(dateString)
		}
	}

	def parseDate(dateString: String, formatPattern: String): Option[Date] = {
		if (dateString == null || formatPattern == null) {
			return None;
		}
		var date = dateString
		// String sdate = "Mon, 11 Oct 2004 09:56:31 +0100";
		// String format = "EEE, dd MMM yyy HH:mm:ss Z";
		// if "s" is timezone like "+02:00", strip the colon
		val idx = date.lastIndexOf("+");
		if (idx != -1) {
			var zone = date.substring(idx);
			zone = zone.replaceAll(":", "");
			date = date.substring(0, idx).concat(zone);
		}
		val parsedDate = (new SimpleDateFormat((formatPattern))).parse(date, new ParsePosition(0))
		Option(parsedDate)
	}

	/**
	 * Format a date using the specified <code>format</code>.
	 *
	 * @param format String, A SimpleDateFormat compatibal format
	 * @param date   Date, The date object to format
	 */
	def formatDate(format: String, date: Date): String = {
		if (date == null) return ""
		(new SimpleDateFormat(format)).format(date);
	}

}

