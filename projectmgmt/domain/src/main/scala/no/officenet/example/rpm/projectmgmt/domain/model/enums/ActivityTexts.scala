package no.officenet.example.rpm.projectmgmt.domain.model.enums

import no.officenet.example.rpm.support.domain.i18n.{Bundle, ResourceBundleEnum}

object ActivityTexts {

	object D extends ResourceBundleEnum {
		val
		name,
		description,
		activityType,
		created,
		createdBy,
		modified,
		modifiedBy,
		type_bug,
		type_improvement,
		type_feature,
		type_task
		= BundleEnum(Bundle.ACTIVITY_D)
	}

}