package no.officenet.example.rpm.projectmgmt.domain.model.enums

import no.officenet.example.rpm.support.infrastructure.enums.EnumWithDescriptionAndObject
import javax.persistence.Converter
import no.officenet.example.rpm.support.infrastructure.jpa.EnumConverter

@Converter
class ActivityTypeConverter extends EnumConverter[ActivityType.ExtendedValue, String](ActivityType)

object ActivityType extends EnumWithDescriptionAndObject[ActivityTexts.D.ExtendedValue] {

	val bug = Value(ActivityTexts.D.type_bug)
	val improvement = Value(ActivityTexts.D.type_improvement)
	val feature = Value(ActivityTexts.D.type_feature)
	val task = Value(ActivityTexts.D.type_task)
}