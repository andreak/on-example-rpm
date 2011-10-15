package no.officenet.example.rpm.web.test

import org.junit.Test
import no.officenet.example.rpm.pets.domain.model.enums.PetTexts

class PetTextsTest extends TextsTest {

	@Test
	def testDomain() {
		assetResourceBundleEnumFound(PetTexts.D)
	}
}