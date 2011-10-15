package no.officenet.example.rpm.support.infrastructure.jpa

import java.io.Serializable
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Types

import org.hibernate.usertype.UserType

/**
 * Helper class to translate enum for hibernate
 */

abstract class EnumUserType(val et: Enumeration) extends UserType {

	val SQL_TYPES = Array({Types.VARCHAR})

	override def sqlTypes() = SQL_TYPES

	override def returnedClass = classOf[et.Value]

	override def equals(x: Object, y: Object): Boolean = x == y

	override def hashCode(x: Object) = x.hashCode

	override def nullSafeGet(resultSet: ResultSet, names: Array[String], owner: Object): Object = {
		val value = resultSet.getString(names(0))
		if (resultSet.wasNull()) {
			null
		}
		else {
			et.withName(value)
		}
	}

	override def nullSafeSet(statement: PreparedStatement, value: Object, index: Int) {
		if (value == null) {
			statement.setNull(index, Types.VARCHAR)
		} else {
			val en = value.toString
			statement.setString(index, en)
		}
	}

	override def deepCopy(value: Object): Object = value

	override def isMutable = false

	override def disassemble(value: Object) = value.asInstanceOf[Serializable]

	override def assemble(cached: Serializable, owner: Object): Serializable = cached

	override def replace(original: Object, target: Object, owner: Object) = original

}
