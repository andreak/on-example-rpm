package no.officenet.example.rpm.support.infrastructure.errorhandling


trait Localizable {
	def getResourceBundleName: String
	def getResourceKey: String
	def getArguments: Seq[AnyRef]
}