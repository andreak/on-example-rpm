package no.officenet.example.rpm.support.infrastructure.jpa

import org.hibernate.usertype.UserType
import org.hibernate.`type`._

import java.io.Serializable
import java.sql.{PreparedStatement, ResultSet}

class StringOptionUserType extends OptionUserType[String] {def nullableType = StandardBasicTypes.STRING}
class IntegerOptionUserType extends OptionUserType[java.lang.Integer] {def nullableType = StandardBasicTypes.INTEGER}
class LongOptionUserType extends OptionUserType[java.lang.Long] {def nullableType = StandardBasicTypes.LONG}

abstract class OptionUserType[T <: AnyRef] extends UserType {

	def nullableType: AbstractSingleColumnStandardBasicType[T]

	def returnedClass = classOf[Option[T]]

	def sqlTypes = Array(nullableType.sqlType)

	def nullSafeGet(resultSet: ResultSet, names: Array[String], owner: AnyRef) = {
		val x = nullableType.nullSafeGet(resultSet, names(0))
		if (x == null) None else Some(x)
	}

	def nullSafeSet(preparedStatement: PreparedStatement, value: AnyRef, index: Int) {
		nullableType.nullSafeSet(preparedStatement, value.asInstanceOf[Option[T]].getOrElse(null.asInstanceOf[T]), index)
	}

	def isMutable = false

	def equals(x: AnyRef, y: AnyRef) = x.equals(y)

	def hashCode(x: AnyRef) = x.hashCode

	def deepCopy(value: AnyRef) = value

	def replace(original: AnyRef, target: AnyRef, owner: AnyRef) = original

	def disassemble(value: AnyRef) = value.asInstanceOf[Serializable]


	def assemble(cached: Serializable, owner: AnyRef) = cached

}