package no.officenet.example.rpm.projectmgmt.domain.model.enums

import no.officenet.example.rpm.support.domain.util.{Bundle, ResourceBundleEnum}

object ProjectTexts {

	object D extends ResourceBundleEnum {
		val
		name,
		description,
		projectType,
		created,
		createdBy,
		modified,
		modifiedBy,
		projectEstimatedStart,
		type_scrum,
		type_sales,
		petId
		= BundleEnum(Bundle.PROJECT_D)
	}

	object V extends ResourceBundleEnum {
		val
		button_newProjectDialog_text,
		projectDialog_title_newProject,
		projectDialog_title_editProject,
		projectDialog_details_label_budget,
		projectDialog_details_label_estimatedStartDate,
		projectDialog_details_label_petName,
		projectDialog_button_save,
		color_black,
		color_red,
		color_orange,
		color_green,
		color_pink,

		label_chosenColor,
		label_niceColor,
		label_badColor

		= BundleEnum(Bundle.PROJECT_V)
	}

}