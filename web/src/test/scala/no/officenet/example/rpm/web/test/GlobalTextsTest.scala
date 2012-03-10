package no.officenet.example.rpm.web.test

import org.junit.Test
import no.officenet.example.rpm.support.infrastructure.i18n.GlobalTexts

class GlobalTextsTest extends TextsTest {

	@Test
	def testDomain() {
		assetResourceBundleEnumFound(GlobalTexts)
	}
}