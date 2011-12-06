package no.officenet.example.rpm.support.infrastructure.jpa.types

import javax.persistence.metamodel.Attribute
import javax.persistence.metamodel.Attribute.PersistentAttributeType
import org.joda.time.DateTime


abstract class JpaField[A, T](implicit m: Manifest[T]) extends Attribute[A, T]{
	def getName: String = {
		val name = getClass.getSimpleName
		name.substring(name.indexOf('$')+1, name.length() - 1)
	}

	def getJavaType = m.erasure.asInstanceOf[Class[T]]

	def isAssociation = false

	def isCollection = false

	def getPersistentAttributeType = PersistentAttributeType.BASIC

	def getJavaMember = null

	def getDeclaringType = null
}

/**
 * object fiskString extends OptionalString[SomeEntity](None)
 */
abstract class StringField[A] extends JpaField[A, String]
abstract class OptionStringField[A] extends JpaField[A, Option[String]]
abstract class LongField[A] extends JpaField[A, Long]
abstract class OptionLongField[A] extends JpaField[A, Option[Long]]
abstract class IntegerField[A] extends JpaField[A, java.lang.Integer]
abstract class DateTimeField[A] extends JpaField[A, DateTime]