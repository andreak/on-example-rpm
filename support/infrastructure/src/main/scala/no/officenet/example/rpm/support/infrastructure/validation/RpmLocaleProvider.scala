package no.officenet.example.rpm.support.infrastructure.validation

import net.sf.oval.localization.locale.LocaleProvider
import java.util.Locale
import org.springframework.context.i18n.LocaleContextHolder

object RpmLocaleProvider extends LocaleProvider {

	def getLocale: Locale = {
		var locale = LocaleContextHolder.getLocale
		if (locale == null) {
			locale = Locale.getDefault
		}
		locale
	}
}
