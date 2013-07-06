package no.officenet.example.rpm.projectmgmt.domain.model.enums

import no.officenet.example.rpm.support.infrastructure.enums.EnumWithDescriptionAndObject
import javax.persistence.{AttributeConverter, Converter}
import no.officenet.example.rpm.support.infrastructure.jpa.EnumConverter

//class ProjectUserType extends EnumUserType(ProjectType)

@Converter(autoApply=true)
class ProjectTypeConverter extends AttributeConverter[ProjectType.ExtendedValue, String] {
	def convertToDatabaseColumn(attribute: ProjectType.ExtendedValue): String = {
		if (attribute eq null) {
			null
		} else {
			attribute.name
		}
	}

	def convertToEntityAttribute(dbData: String): ProjectType.ExtendedValue = {
		if (dbData eq null) {
			null
		} else {
			ProjectType.valueOf(dbData).get
		}
	}
}

object ProjectType extends EnumWithDescriptionAndObject[ProjectTexts.D.ExtendedValue] {

	val scrum = Value(ProjectTexts.D.type_scrum)
	val sales = Value(ProjectTexts.D.type_sales)
}