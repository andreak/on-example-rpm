package no.officenet.example.rpm.support.infrastructure.jpa.types

import javax.persistence.metamodel.Attribute
import javax.persistence.metamodel.Attribute.PersistentAttributeType
import org.joda.time.{LocalTime, LocalDate, DateTime}
import java.util.Date


abstract class JpaField[A, T](implicit m: Manifest[T]) extends Attribute[A, T]{
	def getName: String = {
		val name = getClass.getSimpleName
		name.substring(0, name.length() - 1)
	}

	def getJavaType = m.runtimeClass.asInstanceOf[Class[T]]

	def isAssociation = false

	def isCollection = false

	def getPersistentAttributeType = PersistentAttributeType.BASIC

	def getJavaMember = null

	def getDeclaringType = null
}

abstract class StringField[A] extends JpaField[A, String]

abstract class LongField[A] extends JpaField[A, Long]

abstract class IntegerField[A] extends JpaField[A, Int]

abstract class DateField[A] extends JpaField[A, Date]

abstract class LocalDateField[A] extends JpaField[A, LocalDate]

abstract class LocalTimeField[A] extends JpaField[A, LocalTime]

abstract class DateTimeField[A] extends JpaField[A, DateTime]

abstract class BigDecimalField[A] extends JpaField[A, BigDecimal]