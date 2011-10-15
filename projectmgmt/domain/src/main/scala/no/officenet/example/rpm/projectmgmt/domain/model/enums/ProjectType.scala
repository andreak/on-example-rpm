package no.officenet.example.rpm.projectmgmt.domain.model.enums

import no.officenet.example.rpm.support.infrastructure.jpa.EnumUserType
import no.officenet.example.rpm.support.infrastructure.enums.EnumWithDescriptionAndObject

class ProjectUserType extends EnumUserType(ProjectType)

object ProjectType extends EnumWithDescriptionAndObject[ProjectTexts.D.ExtendedValue] {

	val scrum = Value(ProjectTexts.D.type_scrum)
	val sales = Value(ProjectTexts.D.type_sales)
}