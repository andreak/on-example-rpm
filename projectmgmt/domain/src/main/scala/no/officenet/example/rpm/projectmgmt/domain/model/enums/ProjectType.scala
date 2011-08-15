package no.officenet.example.rpm.projectmgmt.domain.model.enums

import no.officenet.example.rpm.support.domain.util.{EnumUserType, EnumWithDescriptionAndObject}

class ProjectUserType extends EnumUserType(ProjectType)

object ProjectType extends EnumWithDescriptionAndObject[ProjectTexts.D.ExtendedValue] {

	val scrum = Value(ProjectTexts.D.type_scrum)
	val sales = Value(ProjectTexts.D.type_sales)
}