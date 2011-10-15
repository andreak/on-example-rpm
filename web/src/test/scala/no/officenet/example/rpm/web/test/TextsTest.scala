package no.officenet.example.rpm.web.test

import java.util.Locale
import no.officenet.example.rpm.support.domain.i18n.ResourceBundleEnum
import no.officenet.example.rpm.support.domain.i18n.Localizer.L
import org.junit.Assert


trait TextsTest {

	val locales = List(new Locale("no", "NO"), Locale.ENGLISH)

	def assetResourceBundleEnumFound(bundleEnum: ResourceBundleEnum) {
		val errors = bundleEnum.getValues.flatMap(v => locales.map(l => (v, l))).flatMap(value => {
			val (bundle, locale) = value
			try {
				println("Testing locale: " + locale + " bundle: " + bundle)
				Assert.assertNotNull("was null: " + value, L(locale, bundle))
				None
			} catch {
				case e@_ =>
					Some("(" + bundle.bundle + ", " + locale + ", " + bundle.resourceKey + ")")
			}
		}).toSeq

		if (!errors.isEmpty) {
			Assert.fail("problems found: " + errors)
		}
	}

}