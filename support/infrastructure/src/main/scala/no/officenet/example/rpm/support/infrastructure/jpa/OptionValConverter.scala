package no.officenet.example.rpm.support.infrastructure.jpa

import javax.persistence.{Converter, AttributeConverter}
import java.lang
import java.sql.Timestamp
import org.joda.time.DateTime

@Converter(autoApply = true)
class OptionLongConverter extends AttributeConverter[Option[Long], lang.Long]{

	def convertToDatabaseColumn(attribute: Option[Long]): lang.Long = {
		attribute.map(long2Long).orNull
	}

	def convertToEntityAttribute(dbData: lang.Long): Option[Long] = {
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

/*

class OptionLongUserType extends OptionBasicUserType[Long, java.lang.Long] {

	def getBasicType = StandardBasicTypes.LONG

	def fromJdbcType(value: java.lang.Long) = value.longValue()

	def fromStringType(s: String) = java.lang.Long.parseLong(s)

	def toJdbcType(value: Long) = java.lang.Long.valueOf(value)
}

class OptionStringUserType extends OptionBasicUserType[String, String] {

	def getBasicType = StandardBasicTypes.STRING

	def fromJdbcType(value: String) = value

	def fromStringType(s: String) = s

	def toJdbcType(value: String) = value
}

abstract class OptionEnumUserType(val et: Enumeration) extends OptionBasicUserType[Enumeration#Value, String] {

	def getBasicType = StandardBasicTypes.STRING

	def fromJdbcType(value: String) = et.withName(value)

	def fromStringType(s: String) = et.withName(s)

	def toJdbcType(value: Enumeration#Value) = value.toString
}

class OptionDateTimeUserType extends OptionUserType[DateTime, Timestamp] with IntegratorConfiguredType {
	val columnMapper = new TimestampColumnDateTimeMapper

	override def applyConfiguration(sessionFactory: SessionFactory) {
//		super.applyConfiguration(sessionFactory)
		var databaseZone: String = null
		if (parameterValues != null) {
			databaseZone = parameterValues.getProperty("databaseZone")
		}
		if (databaseZone == null) {
			databaseZone = ConfigurationHelper.getProperty("databaseZone")
		}
		if (databaseZone != null) {
			if ("jvm" == databaseZone) {
				columnMapper.setDatabaseZone(null)
			}
			else {
				columnMapper.setDatabaseZone(DateTimeZone.forID(databaseZone))
			}
		}
		var javaZone: String = null
		if (parameterValues != null) {
			javaZone = parameterValues.getProperty("javaZone")
		}
		if (javaZone == null) {
			javaZone = ConfigurationHelper.getProperty("javaZone")
		}
		if (javaZone != null) {
			if ("jvm" == javaZone) {
				columnMapper.setJavaZone(null)
			}
			else {
				columnMapper.setJavaZone(DateTimeZone.forID(javaZone))
			}
		}
	}
}

class OptionLocalDateUserType extends OptionUserType[LocalDate, java.sql.Date] {
	val columnMapper = new DateColumnLocalDateMapper
}

abstract class OptionUserType[T, JDBCType] extends AttributeConverter[T, JDBCType] {

	def convertToDatabaseColumn(attribute: T): JDBCType = {

	}

	def convertToEntityAttribute(dbData: JDBCType): T = {

	}

	override def nullSafeGet(resultSet: ResultSet, names: Array[String], session: SessionImplementor, owner: AnyRef): Option[T] = {
		beforeNullSafeOperation(session)
		try {
			val x = columnMapper.getHibernateType.nullSafeGet(resultSet, names(0), session)
			if (x == null) None else Some(columnMapper.fromNonNullValue(x.asInstanceOf[JDBCType]))
		} finally {
			afterNullSafeOperation(session)
		}
	}

	def nullSafeSet(preparedStatement: PreparedStatement, value: AnyRef, index: Int, session: SessionImplementor) {
		beforeNullSafeOperation(session)
		try {
			columnMapper.getHibernateType.nullSafeSet(preparedStatement,
				value.asInstanceOf[Option[T]].map(columnMapper.toNonNullValue).getOrElse(null),
				index, session)
		} finally {
			afterNullSafeOperation(session)
		}
	}
}

abstract class OptionBasicUserType[T, JDBCType] extends OptionUserType[T, JDBCType] {

	override final val columnMapper = new AbstractColumnMapper[T, JDBCType] {
		final def getSqlType = getBasicType.sqlType()
		final def getHibernateType = getBasicType
		final def fromNonNullValue(value: JDBCType): T = fromJdbcType(value)
		final def fromNonNullString(s: String): T = fromStringType(s)
		final def toNonNullValue(value: T): JDBCType = toJdbcType(value)
		final def toNonNullString(value: T) = value.toString
	}

	def getBasicType: AbstractSingleColumnStandardBasicType[JDBCType]
	def fromJdbcType(value: JDBCType): T
	def fromStringType(s: String): T
	def toJdbcType(value: T): JDBCType
}
*/
