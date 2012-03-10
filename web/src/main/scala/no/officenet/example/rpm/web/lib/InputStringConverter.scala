package no.officenet.example.rpm.web.lib

import org.joda.time.DateTime
import no.officenet.example.rpm.support.infrastructure.i18n.{DateFormatter, NumberFormatter}

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
	val dateTimeCls = classOf[DateTime]
	val optionCls = classOf[Option[_]]

	def convert[T](fromValue: String)(implicit m: Manifest[T]): T = {
		val klass = m.erasure
		val isOption: Boolean = klass == InputStringConverter.optionCls

		if (fromValue == null) {
			if (isOption) {
				None.asInstanceOf[T]
			} else {
				null.asInstanceOf[T]
			}
		} else {
			val foundClass = if (isOption && m.typeArguments.length > 0) m.typeArguments.head.erasure else klass
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
				case `dateTimeCls` => new DateTime(DateFormatter.parseFullDate(fromValue))
				case _ => throw new IllegalArgumentException("Don't know how to convert value " + fromValue + " of type " + foundClass.getName)
			}
			val retValue = if (isOption) {
				Some(converted)
			} else {
				converted
			}
			retValue.asInstanceOf[T]
		}
	}

}
