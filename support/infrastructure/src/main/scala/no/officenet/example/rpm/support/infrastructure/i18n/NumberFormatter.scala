package no.officenet.example.rpm.support.infrastructure.i18n

import java.text.{DecimalFormatSymbols, DecimalFormat}
import Localizer._
import no.officenet.example.rpm.support.infrastructure.scala.lang.ControlHelpers.?


object NumberFormatter {
	val defaultGroupingSeparator = L(GlobalTexts.numberFormat_groupingSeparator).charAt(0)
	val defaultDecimalSeparator = L(GlobalTexts.numberFormat_decimalSeparator).charAt(0)
	val defaultDecimalFormatPattern = L(GlobalTexts.decimalFormat_pattern)

	val decimalFormat = createDecimalFormat
	val bigDecimalFormat = createBigDecimalFormat
	val wholeNumberFormat = createWholeNumberFormat


	def createWholeNumberFormat = {
		val df = createDecimalFormat
		df.setMaximumFractionDigits(0)
		df
	}

	def createDecimalFormat = {
		val format = new DecimalFormat(defaultDecimalFormatPattern)
		format.setDecimalFormatSymbols(createDecimalFormatSymbol(defaultGroupingSeparator, defaultDecimalSeparator))
		format
	}

	def createBigDecimalFormat = {
		val df = createDecimalFormat
		df.setParseBigDecimal(true)
		df
	}

	def createDecimalFormatSymbol(groupingSeparator: Char, decimalSeparator: Char): DecimalFormatSymbols = {
		val dfs = new DecimalFormatSymbols
		dfs.setGroupingSeparator(groupingSeparator)
		dfs.setDecimalSeparator(decimalSeparator)
		dfs
	}

	def formatNumber(value: Double, minimumFractionDigits: Int, maximumFractionDigits: Int): String = {
		formatNumber(value, minimumFractionDigits, maximumFractionDigits, defaultGroupingSeparator, defaultDecimalSeparator)
	}

	def formatNumber(value: Double, minimumFractionDigits: Int, maximumFractionDigits: Int, groupingSeparator: Char, decimalSeparator: Char): String = {
		val nf = new DecimalFormat
		nf.setMinimumFractionDigits(minimumFractionDigits)
		nf.setMaximumFractionDigits(maximumFractionDigits)
		nf.setDecimalFormatSymbols(createDecimalFormatSymbol(groupingSeparator, decimalSeparator))
		nf.format(value)
	}


	def parse(number: String): Number = {
		decimalFormat.parse(number)
	}

	def parseBigDecimal(number: String): BigDecimal = {
		BigDecimal(bigDecimalFormat.parse(number).asInstanceOf[java.math.BigDecimal])
	}


	def formatWholeNumber(value: Double): String = {
		wholeNumberFormat.format(value)
	}

	def formatWholeNumber(value: BigDecimal): String = {
		formatWholeNumber(value.toDouble)
	}

	def formatWholeNumber(value: java.math.BigDecimal): String = formatWholeNumber(BigDecimal(value))

	def formatWholeNumber(value: Long): String = formatWholeNumber(value.toDouble)

	def formatWholeNumber(value: Int): String = formatWholeNumber(value.toDouble)


	def formatDecimalNumber(value: Double): String = {
		decimalFormat.format(value)
	}

	def formatDecimalNumber(value: java.lang.Double): String = {
		?(value.toDouble).map(formatDecimalNumber).getOrElse("")
	}


	def formatBigDecimalNumber(value: BigDecimal): String = {
		bigDecimalFormat.format(value.bigDecimal)
	}


	def formatBigDecimalNumber(value: java.math.BigDecimal): String = formatBigDecimalNumber(BigDecimal(value))


	def formatPercent(value: Option[java.math.BigDecimal]): String = {
		value.map(v => "%s %%" format formatBigDecimalNumber(BigDecimal(v))).getOrElse("")
	}

}
