package no.officenet.example.rpm.support.infrastructure.i18n

import xml.NodeSeq
import no.officenet.example.rpm.support.infrastructure.i18n.Localizer.{L_!, L}

sealed trait WithBundle {
	def bundle: Bundle.ExtendedValue
	def resourceKey: String
	def localize: String
	def localize_! : NodeSeq
	def localize(arguments: Any*): String
	def localize_!(arguments: Any*): NodeSeq
}

abstract class ResourceBundleEnum extends Enumeration {
	type ExtendedValue = Value with WithBundle

	def BundleEnum(inBundle: Bundle.ExtendedValue): ExtendedValue = {
		new Val(nextId) with WithBundle {
			override def bundle = inBundle
			override def resourceKey = toString()
			override def localize = L(this)
			override def localize_! = L_!(this)
			override def localize(arguments: Any*) = L(this, arguments:_*)
			override def localize_!(arguments: Any*) = L_!(this, arguments:_*)
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
