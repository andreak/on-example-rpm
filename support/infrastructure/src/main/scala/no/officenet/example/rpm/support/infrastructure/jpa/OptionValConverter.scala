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

//// START converters for Option of primitive-types.
/*
Note: They are not autoApply because Scala generates byte-code for Option[java.lang.Object] so EL isn't able to apply them automatically.
 */
@Converter
class OptionLongConverter extends AttributeConverter[Option[Long], lang.Long]{

	def convertToDatabaseColumn(attribute: Option[Long]): lang.Long = {
		attribute.map(long2Long).orNull
	}

	def convertToEntityAttribute(dbData: lang.Long): Option[Long] = {
		if (dbData eq null) None
		else Some(dbData)
	}
}

@Converter
class OptionIntConverter extends AttributeConverter[Option[Int], lang.Integer]{

	def convertToDatabaseColumn(attribute: Option[Int]): lang.Integer = {
		attribute.map(int2Integer).orNull
	}

	def convertToEntityAttribute(dbData: lang.Integer): Option[Int] = {
		if (dbData eq null) None
		else Some(dbData)
	}
}

@Converter
class OptionFloatConverter extends AttributeConverter[Option[Float], lang.Float]{

	def convertToDatabaseColumn(attribute: Option[Float]): lang.Float = {
		attribute.map(float2Float).orNull
	}

	def convertToEntityAttribute(dbData: lang.Float): Option[Float] = {
		if (dbData eq null) None
		else Some(dbData)
	}
}

@Converter
class OptionDoubleConverter extends AttributeConverter[Option[Double], lang.Double]{

	def convertToDatabaseColumn(attribute: Option[Double]): lang.Double = {
		attribute.map(double2Double).orNull
	}

	def convertToEntityAttribute(dbData: lang.Double): Option[Double] = {
		if (dbData eq null) None
		else Some(dbData)
	}
}

@Converter
class OptionShortConverter extends AttributeConverter[Option[Short], lang.Short]{

	def convertToDatabaseColumn(attribute: Option[Short]): lang.Short = {
		attribute.map(short2Short).orNull
	}

	def convertToEntityAttribute(dbData: lang.Short): Option[Short] = {
		if (dbData eq null) None
		else Some(dbData)
	}
}

@Converter
class OptionByteConverter extends AttributeConverter[Option[Byte], lang.Byte]{

	def convertToDatabaseColumn(attribute: Option[Byte]): lang.Byte = {
		attribute.map(byte2Byte).orNull
	}

	def convertToEntityAttribute(dbData: lang.Byte): Option[Byte] = {
		if (dbData eq null) None
		else Some(dbData)
	}
}

@Converter
class OptionCharConverter extends AttributeConverter[Option[Char], lang.Character]{

	def convertToDatabaseColumn(attribute: Option[Char]): lang.Character = {
		attribute.map(char2Character).orNull
	}

	def convertToEntityAttribute(dbData: lang.Character): Option[Char] = {
		if (dbData eq null) None
		else Some(dbData)
	}
}

@Converter(autoApply = true)
class OptionBigDecimalConverter extends AttributeConverter[Option[BigDecimal], JBigDecimal]{

	def convertToDatabaseColumn(attribute: Option[BigDecimal]): JBigDecimal = {
		attribute.map(_.bigDecimal).orNull
	}

	def convertToEntityAttribute(dbData: JBigDecimal): Option[BigDecimal] = {
		if (dbData eq null) None
		else Some(dbData)
	}
}

@Converter(autoApply = true)
class OptionStringConverter extends AttributeConverter[Option[String], String] {

	def convertToDatabaseColumn(attribute: Option[String]): String = {
		attribute.orNull
	}

	def convertToEntityAttribute(dbData: String): Option[String] = Option(dbData)
}

@Converter(autoApply = true)
class OptionDateTimeConverter extends AttributeConverter[Option[DateTime], Timestamp] {
	def convertToDatabaseColumn(attribute: Option[DateTime]): Timestamp = {
		attribute.map(v => new Timestamp(v.getMillis)).orNull
	}

	def convertToEntityAttribute(dbData: Timestamp): Option[DateTime] = {
		Option(dbData).map(v => new DateTime(v.getTime))
	}
}

@Converter(autoApply = true)
class OptionLocalDateConverter extends AttributeConverter[Option[LocalDate], Date] {
	def convertToDatabaseColumn(attribute: Option[LocalDate]): Date = {
		attribute.map(v => new Date(v.toDateMidnight.getMillis)).orNull
	}

	def convertToEntityAttribute(dbData: Date): Option[LocalDate] = {
		Option(dbData).map(v => new LocalDate(v.getTime))
	}
}

@Converter(autoApply = true)
class OptionLocalTimeConverter extends AttributeConverter[Option[LocalTime], Time] {
	def convertToDatabaseColumn(attribute: Option[LocalTime]): Time = {
		attribute.map(v => new Time(v.toDateTimeToday.getMillis)).orNull
	}

	def convertToEntityAttribute(dbData: Time): Option[LocalTime] = {
		Option(dbData).map(v => new LocalTime(v.getTime))
	}
}
