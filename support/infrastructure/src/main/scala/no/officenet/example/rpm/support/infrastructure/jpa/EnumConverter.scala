package no.officenet.example.rpm.support.infrastructure.jpa

import javax.persistence.{Converter, AttributeConverter}
import no.officenet.example.rpm.support.infrastructure.enums.EnumWithDescriptionAndObject

/**
 * Helper class to translate enum for JPA-2.1
 */

abstract class EnumConverter[T,D](et: EnumWithDescriptionAndObject[_]) extends AttributeConverter[T, String] {
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
			et.valueOf(dbData).get.asInstanceOf[T]
		}
	}

}

