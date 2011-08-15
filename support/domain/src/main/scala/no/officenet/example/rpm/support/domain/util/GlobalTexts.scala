package no.officenet.example.rpm.support.domain.util

object GlobalTexts extends ResourceBundleEnum {
	val
	dateformat_fullDate,
	dateformat_fullDateTime,
	select_noItemSelected,

	validation_notANumber_number_text,
	validation_notANumber_integer_text,
	validation_notANumber_float_text,
	validation_invalidDate_text,

	decimalFormat_pattern,
	numberFormat_groupingSeparator,
	numberFormat_decimalSeparator,

	error_userNotFound
	
	= BundleEnum(Bundle.GLOBAL)
}