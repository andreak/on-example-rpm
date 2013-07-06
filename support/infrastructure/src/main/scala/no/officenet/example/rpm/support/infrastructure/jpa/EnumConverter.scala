package no.officenet.example.rpm.support.infrastructure.jpa

import javax.persistence.{Converter, AttributeConverter}

/**
 * Helper class to translate enum for hibernate
 */

trait EnumConverter[T] extends AttributeConverter[T, String] {
	def convertToDatabaseColumn(attribute: T): String = {
		if (attribute == null) {
			null
		} else {
			attribute.toString
		}
	}

	def convertToEntityAttribute(dbData: String): T = {
		if (dbData == null) {
			null.asInstanceOf[T]
		} else {
			valueOf(dbData)
		}
	}

	def valueOf(dbData: String): T

}

