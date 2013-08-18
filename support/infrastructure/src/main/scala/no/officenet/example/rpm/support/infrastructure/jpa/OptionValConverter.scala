package no.officenet.example.rpm.support.infrastructure.jpa

import javax.persistence.{Converter, AttributeConverter}
import java.lang
import java.sql.{Time, Date, Timestamp}
import org.joda.time.{LocalTime, LocalDate, DateTime}
import java.math.{BigDecimal => JBigDecimal}

abstract class OptionConverter[T, JDBCType] extends AttributeConverter[Option[T], JDBCType] {
	def convertToDatabaseColumn(attribute: Option[T]): JDBCType = {
		attribute match {
			case Some(e) => toJdbcType(e)
			case _ => null.asInstanceOf[JDBCType]
		}
	}

	def convertToEntityAttribute(dbData: JDBCType): Option[T] = {
		if (dbData == null) {
			None
		} else {
			Some(fromJdbcType(dbData))
		}
	}

	def fromJdbcType(dbData: JDBCType): T
	def toJdbcType(attribute: T): JDBCType

}

@Converter(autoApply = true)
class OptionLongConverter extends OptionConverter[Long, lang.Long]{
	def fromJdbcType(dbData: lang.Long): Long = dbData
	def toJdbcType(attribute: Long): lang.Long = attribute
}

@Converter(autoApply = true)
class OptionIntConverter extends OptionConverter[Int, lang.Integer]{
	def fromJdbcType(dbData: Integer): Int = dbData
	def toJdbcType(attribute: Int): Integer = attribute
}

@Converter(autoApply = true)
class OptionFloatConverter extends OptionConverter[Float, lang.Float]{
	def fromJdbcType(dbData: lang.Float): Float = dbData
	def toJdbcType(attribute: Float): lang.Float = attribute
}

@Converter(autoApply = true)
class OptionDoubleConverter extends OptionConverter[Double, lang.Double]{
	def fromJdbcType(dbData: lang.Double): Double = dbData
	def toJdbcType(attribute: Double): lang.Double = attribute
}

@Converter(autoApply = true)
class OptionBigDecimalConverter extends OptionConverter[BigDecimal, JBigDecimal]{
	def fromJdbcType(dbData: JBigDecimal): BigDecimal = dbData
	def toJdbcType(attribute: BigDecimal): JBigDecimal = attribute.bigDecimal
}

@Converter(autoApply = true)
class OptionStringConverter extends OptionConverter[String, String] {
	def fromJdbcType(dbData: String): String = dbData
	def toJdbcType(attribute: String): String = attribute
}

@Converter(autoApply = true)
class OptionDateTimeConverter extends OptionConverter[DateTime, Timestamp] {
	def fromJdbcType(dbData: Timestamp): DateTime = new DateTime(dbData.getTime)
	def toJdbcType(attribute: DateTime): Timestamp = new Timestamp(attribute.getMillis)
}

@Converter(autoApply = true)
class OptionLocalDateConverter extends OptionConverter[LocalDate, Date] {
	def fromJdbcType(dbData: Date): LocalDate = new LocalDate(dbData.getTime)
	def toJdbcType(attribute: LocalDate): Date = new Date(attribute.toDateMidnight.getMillis)
}

@Converter(autoApply = true)
class OptionLocalTimeConverter extends OptionConverter[LocalTime, Time] {
	def fromJdbcType(dbData: Time): LocalTime = new LocalTime(dbData.getTime)
	def toJdbcType(attribute: LocalTime): Time = new Time(attribute.toDateTimeToday.getMillis)
}
