package no.officenet.example.rpm.support.infrastructure.i18n

import org.joda.time.{LocalDate, DateTime}

object InputStringConverter {

	// Needed for pattern-matching as one cannot use classOf[SomeType] as an extractor
	val stringCls = classOf[String]
	val shortCls = classOf[java.lang.Short]
	val shortPrimitiveCls = java.lang.Short.TYPE
	val intCls = classOf[java.lang.Integer]
	val intPrimitiveCls = java.lang.Integer.TYPE
	val longCls = classOf[java.lang.Long]
	val longPrimitiveCls = java.lang.Long.TYPE
	val floatCls = classOf[java.lang.Float]
	val floatPrimitiveCls = java.lang.Float.TYPE
	val doubleCls = classOf[java.lang.Double]
	val doublePrimitiveCls = java.lang.Double.TYPE
	val javaBigDcmlCls = classOf[java.math.BigDecimal]
	val bigDcmlCls = classOf[BigDecimal]
	val dateCls = classOf[java.util.Date]
	val localDateCls = classOf[LocalDate]
	val dateTimeCls = classOf[DateTime]
	val optionCls = classOf[Option[_]]

	def isTypeOrOptionOfType[T](klass: Class[_], m: Manifest[T]): Boolean = {
		klass == (
			if (isOption(m) && m.typeArguments.length > 0) {
				m.typeArguments.head.erasure
			} else {m.erasure}
			)
	}

	def isOption[T](m: Manifest[T]): Boolean = m.erasure == InputStringConverter.optionCls

	def convert[T](fromValue: String)(implicit m: Manifest[T]): T = {
		val klass = m.erasure
		val _isOption: Boolean = isOption(m)

		val foundClass = if (_isOption && m.typeArguments.length > 0) m.typeArguments.head.erasure else klass
		val converted = foundClass match {
			case `stringCls` => fromValue
			case `shortCls` | `shortPrimitiveCls` => NumberFormatter.parse(fromValue).shortValue()
			case `intCls` | `intPrimitiveCls` => NumberFormatter.parse(fromValue).intValue()
			case `longCls` | `longPrimitiveCls` => NumberFormatter.parse(fromValue).longValue()
			case `floatCls` | `floatPrimitiveCls` => NumberFormatter.parse(fromValue).floatValue()
			case `doubleCls` | `doublePrimitiveCls` => NumberFormatter.parse(fromValue).doubleValue()
			case `javaBigDcmlCls` => NumberFormatter.parseBigDecimal(fromValue).bigDecimal
			case `bigDcmlCls` => NumberFormatter.parseBigDecimal(fromValue)
			case `dateCls` => DateFormatter.parseFullDate(fromValue)
			case `localDateCls` => new LocalDate(DateFormatter.parseFullDate(fromValue).getTime)
			case `dateTimeCls` => new DateTime(DateFormatter.parseFullDate(fromValue).getTime)
			case _ => throw new IllegalArgumentException("Don't know how to convert value " + fromValue + " of type " + foundClass.getName)
		}
		val retValue = if (_isOption) {
			Some(converted)
		} else {
			converted
		}
		retValue.asInstanceOf[T]
	}

}
