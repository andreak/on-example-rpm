package no.officenet.example.rpm.web.test

import org.junit.Test
import no.officenet.example.rpm.projectmgmt.domain.model.enums.ActivityTexts


class ActivityTextsTest extends TextsTest {

	@Test
	def testDomain() {
		assetResourceBundleEnumFound(ActivityTexts.D)
	}
}