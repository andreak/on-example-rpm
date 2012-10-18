package no.officenet.example.rpm.support.infrastructure.jpa

import org.hibernate.`type`._

import org.hibernate.engine.spi.SessionImplementor
import java.sql.{Timestamp, PreparedStatement, ResultSet}
import org.hibernate.SessionFactory
import org.jadira.usertype.spi.shared.{AbstractUserType, IntegratorConfiguredType, ConfigurationHelper, AbstractColumnMapper}
import org.hibernate.usertype.{ParameterizedType, UserType}
import java.util.Properties
import org.joda.time.{LocalDate, DateTimeZone, DateTime}
import org.jadira.usertype.dateandtime.joda.columnmapper.{DateColumnLocalDateMapper, TimestampColumnDateTimeMapper}
import java.io.Serializable
import java.math.{BigDecimal => JBigDecimal}

class OptionBigDecimalUserType extends OptionBasicUserType[BigDecimal, java.math.BigDecimal] {

	def getBasicType = StandardBasicTypes.BIG_DECIMAL

	def fromJdbcType(value: java.math.BigDecimal) = BigDecimal(value)

	def fromStringType(s: String) = BigDecimal(s)

	def toJdbcType(value: BigDecimal) = value.bigDecimal
}

class OptionLongUserType extends OptionBasicUserType[Long, java.lang.Long] {

	def getBasicType = StandardBasicTypes.LONG

	def fromJdbcType(value: java.lang.Long) = value.longValue()

	def fromStringType(s: String) = java.lang.Long.parseLong(s)

	def toJdbcType(value: Long) = java.lang.Long.valueOf(value)
}

class OptionDoubleUserType extends OptionBasicUserType[Double, java.lang.Double] {

	def getBasicType = StandardBasicTypes.DOUBLE

	def fromJdbcType(value: java.lang.Double) = value.doubleValue()

	def fromStringType(s: String) = java.lang.Double.parseDouble(s)

	def toJdbcType(value: Double) = java.lang.Double.valueOf(value)
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

abstract class OptionUserType[T, JDBCType] extends AbstractUserType with UserType with ParameterizedType {
	var parameterValues: Properties = null

	def columnMapper: AbstractColumnMapper[T, JDBCType]

	def returnedClass = classOf[Option[T]]

	def sqlTypes = Array(columnMapper.getSqlType)

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

	def setParameterValues(parameters: Properties) {
		this.parameterValues = parameters
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

/**
 * Helper class to translate scala.bigDecimal for hibernate
 */
class BigDecimalUserType extends UserType {

	val `type` = StandardBasicTypes.BIG_DECIMAL

	override def sqlTypes() = Array(`type`.sqlType)

	override def returnedClass = classOf[BigDecimal]

	override def equals(x: Object, y: Object): Boolean = x == y

	override def hashCode(x: Object) = x.hashCode

	override def nullSafeGet(resultSet: ResultSet, names: Array[String], session: SessionImplementor, owner: Object): Object = {
		val x = `type`.nullSafeGet(resultSet, names(0), session)
		if (x == null) null else BigDecimal(x)
	}

	override def nullSafeSet(statement: PreparedStatement, value: Object, index: Int, session: SessionImplementor) {
		val bd = if (value == null) null.asInstanceOf[JBigDecimal] else value.asInstanceOf[BigDecimal].bigDecimal
		`type`.nullSafeSet(statement, bd, index, session)
	}

	def isMutable = false

	def deepCopy(value: AnyRef) = value

	def replace(original: AnyRef, target: AnyRef, owner: AnyRef) = original

	def disassemble(value: AnyRef) = value.asInstanceOf[Serializable]

	def assemble(cached: Serializable, owner: AnyRef) = cached

}
