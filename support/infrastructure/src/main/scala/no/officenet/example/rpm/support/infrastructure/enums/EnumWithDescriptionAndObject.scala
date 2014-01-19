package no.officenet.example.rpm.support.infrastructure.enums

import scala.collection.mutable

/**
 * adds a valueOf function, assumes name is defined
 * add optional description and wrapped value (T)
 */

sealed trait ValueWithDescription[T]  {
	def description: String
	def name: String
	def wrapped: T
}

trait WithDescription {
	def description: String
	def name: String
}

trait NamedValue  {
	def name: String
}

trait NaturalKey[NK] {
	def key: NK
}

abstract class EnumWithDescriptionAndObject[T] extends Enumeration {

	type ExtendedValue = Value with ValueWithDescription[T]

	def Value(inDescription: String, inWrapped: T): ExtendedValue = {
		new Val(nextId) with ValueWithDescription[T] {
			def description = inDescription
			def name = toString()
			def wrapped = inWrapped
		}
	}

	def Value(inWrapped: T): ExtendedValue = Value("", inWrapped)

	def getValues = {
		super.values.map(v => v.asInstanceOf[ExtendedValue]).asInstanceOf[Set[ExtendedValue]].toSeq
	}

	def valueOf(name: String) = try{Some(withName(name).asInstanceOf[ExtendedValue])} catch {case _ => None}

	def unapply(value: String) = getValues.find(_.toString == value)

}

abstract class EnumWithDescriptionAndObjectNaturalKey[T, NK] extends Enumeration {

	type ExtendedValue = Value with ValueWithDescription[T] with NaturalKey[NK]

	protected val keys: mutable.HashMap[NK, ExtendedValue] = mutable.HashMap.empty

	def Value(_key: NK, inWrapped: T, inDescription: String): ExtendedValue = {
		val enumVal = new Val(nextId) with ValueWithDescription[T] with NaturalKey[NK] {
			def description = inDescription
			def name = toString()
			def wrapped = inWrapped
			def key = _key
		}
		if (keys.contains(_key)) throw new IllegalArgumentException("Duplicate key found in enum: " + _key)
		keys += _key -> enumVal
		enumVal
	}

	def Value(inKey: NK, inWrapped: T): ExtendedValue = Value(inKey, inWrapped, "")

	def getValues = {
		super.values.map(v => v.asInstanceOf[ExtendedValue]).asInstanceOf[Set[ExtendedValue]].toSeq
	}

	def getEnum(key: NK): Option[ExtendedValue] = {
		keys.get(key)
	}

	def valueOf(name: String) = try{Some(withName(name).asInstanceOf[ExtendedValue])} catch {case _ => None}

	def unapply(value: String) = getValues.find(_.toString == value)

}

abstract class EnumWithDescription extends Enumeration {

	type ExtendedValue = Value with WithDescription

	def Description(inDescription: String): ExtendedValue = {
		new Val(nextId) with WithDescription {
			def description = inDescription
			def name = toString()
		}
	}

	def getValues = {
		super.values.map(v => v.asInstanceOf[ExtendedValue]).asInstanceOf[Set[ExtendedValue]].toSeq
	}

	def valueOf(name: String) = try{Some(withName(name).asInstanceOf[ExtendedValue])} catch {case _ => None}

}

abstract class EnumWithName extends Enumeration {

	type ExtendedValue = Value with NamedValue

	def Name: ExtendedValue = {
		new Val(nextId) with NamedValue {
			def name = toString()
		}
	}

	def getValues = {
		super.values.map(v => v.asInstanceOf[ExtendedValue]).asInstanceOf[Set[ExtendedValue]].toSeq
	}

	def valueOf(name: String) = try{Some(withName(name).asInstanceOf[ExtendedValue])} catch {case _ => None}

}

abstract class EnumWithNaturalKey[T] extends Enumeration {

	type K
	type ExtendedValue = Value with NamedValue with K

	protected val keys: mutable.HashMap[T, ExtendedValue] = mutable.HashMap.empty

	def Name(key: T): ExtendedValue

	def getValues = {
		super.values.map(v => v.asInstanceOf[ExtendedValue]).asInstanceOf[Set[ExtendedValue]].toSeq
	}

	def getEnum(key: T): Option[ExtendedValue] = {
		keys.get(key)
	}

	def valueOf(name: String) = try{Some(withName(name).asInstanceOf[ExtendedValue])} catch {case _ => None}

}
