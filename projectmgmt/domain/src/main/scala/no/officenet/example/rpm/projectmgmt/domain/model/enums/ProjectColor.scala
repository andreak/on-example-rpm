package no.officenet.example.rpm.projectmgmt.domain.model.enums

import no.officenet.example.rpm.support.domain.util.{EnumWithDescriptionAndObject, EnumWithDescription}

object ProjectColor extends EnumWithDescriptionAndObject[ProjectTexts.V.ExtendedValue] {
	val BLACK = Value(ProjectTexts.V.color_black)
	val RED = Value(ProjectTexts.V.color_red)
	val ORANGE = Value(ProjectTexts.V.color_orange)
	val GREEN = Value(ProjectTexts.V.color_green)
	val PINK = Value(ProjectTexts.V.color_pink)
}