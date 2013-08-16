package no.officenet.example.rpm.projectmgmt.domain.model.enums

import no.officenet.example.rpm.support.infrastructure.enums.EnumWithDescriptionAndObject
import javax.persistence.{AttributeConverter, Converter}
import no.officenet.example.rpm.support.infrastructure.jpa.EnumConverter

@Converter
class ProjectTypeConverter extends EnumConverter[ProjectType.ExtendedValue](ProjectType)

object ProjectType extends EnumWithDescriptionAndObject[ProjectTexts.D.ExtendedValue] {
	val scrum = Value(ProjectTexts.D.type_scrum)
	val sales = Value(ProjectTexts.D.type_sales)
}