package no.officenet.example.rpm.support.domain.i18n

import no.officenet.example.rpm.support.infrastructure.util.ResourceBundleNameProvider

sealed trait WithBundle {
    def bundle: Bundle.ExtendedValue
    def resourceKey: String
}

abstract class ResourceBundleEnum extends Enumeration {
	type ExtendedValue = Value with WithBundle

	def BundleEnum(inBundle: Bundle.ExtendedValue): ExtendedValue = {
		new Val(nextId) with WithBundle {
			def bundle = inBundle
			def resourceKey = toString()
		}
	}

	def getValues = {
		super.values.map(v => v.asInstanceOf[ExtendedValue]).asInstanceOf[Set[ExtendedValue]].toSeq
	}

	def valueOf(name: String) = try{Some(withName(name).asInstanceOf[ExtendedValue])} catch {case _ => None}

}

abstract class ResourceBundleNameEnum extends Enumeration {
	type ExtendedValue = Value with ResourceBundleNameProvider

	def BundleName(inPath: String): ExtendedValue = {
		new Val(nextId) with ResourceBundleNameProvider {
			def path = inPath
		}
	}

	def getValues = {
		super.values.map(v => v.asInstanceOf[ExtendedValue]).asInstanceOf[Set[ExtendedValue]].toSeq
	}

	def valueOf(name: String) = try{Some(withName(name).asInstanceOf[ExtendedValue])} catch {case _ => None}

}
