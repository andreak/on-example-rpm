package no.officenet.example.rpm.projectmgmt.domain.model.enums

import no.officenet.example.rpm.support.infrastructure.enums.EnumWithDescriptionAndObject
import javax.persistence.{AttributeConverter, Converter}

//class ActivityUserType extends EnumUserType(ActivityType)

@Converter(autoApply=true)
class ActivityTypeConverter extends AttributeConverter[ActivityType.ExtendedValue, String] {
	def convertToDatabaseColumn(attribute: ActivityType.ExtendedValue): String = {
		if (attribute eq null) {
			null
		} else {
			attribute.name
		}
	}

	def convertToEntityAttribute(dbData: String): ActivityType.ExtendedValue = {
		if (dbData eq null) {
			null
		} else {
			ActivityType.valueOf(dbData).get
		}
	}
}

object ActivityType extends EnumWithDescriptionAndObject[ActivityTexts.D.ExtendedValue] {

	val bug = Value(ActivityTexts.D.type_bug)
	val improvement = Value(ActivityTexts.D.type_improvement)
	val feature = Value(ActivityTexts.D.type_feature)
	val task = Value(ActivityTexts.D.type_task)
}