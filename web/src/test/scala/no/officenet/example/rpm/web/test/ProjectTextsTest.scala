package no.officenet.example.rpm.web.test

import org.junit.Test
import no.officenet.example.rpm.projectmgmt.domain.model.enums.ProjectTexts

class ProjectTextsTest extends TextsTest {

	@Test
	def testDomain() {
		assetResourceBundleEnumFound(ProjectTexts.D)
	}
	@Test
	def testView() {
		assetResourceBundleEnumFound(ProjectTexts.V)
	}
}