package no.officenet.example.rpm.support.domain.i18n

object GlobalTexts extends ResourceBundleEnum {
	val
	logged_in_user,
	dateformat_fullDate,
	dateformat_fullDateTime,
	dateformat_fullDateTimeSeconds,
	select_noItemSelected,

	validation_notANumber_number_text,
	validation_notANumber_integer_text,
	validation_notANumber_float_text,
	validation_invalidDate_text,

	decimalFormat_pattern,
	numberFormat_groupingSeparator,
	numberFormat_decimalSeparator,

	error_userNotFound,

	validationViolation_popup_title,
	validationViolation_popup_header,

	exception_popup_title,
	exception_popup_header,
	exception_page_title,
	exception_page_header,

	error_popup_serverError,

	exception_showHideStackTrace_text,
	exception_originalException_text,

	button_edit
	
	= BundleEnum(Bundle.GLOBAL)
}