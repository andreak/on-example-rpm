package no.officenet.example.rpm.support.infrastructure.enums

import scala.collection.mutable

/**
 * adds a valueOf function, assumes name is defined
 * add optional description and wrapped value (T)
*/

sealed trait ValueWithKey[K,T]  {
    def key: K
    def name: String
	def wrapped: T
}

abstract class EnumWithKeyAndValue[K, T] extends Enumeration {

	type ExtendedValue = Value with ValueWithKey[K,T]
	protected val keys: mutable.HashMap[K, ExtendedValue] = mutable.HashMap.empty

	def Value(inKey: K, inWrapped: T): ExtendedValue = {
		val enumVal = new Val(nextId) with ValueWithKey[K,T] {
			def key = inKey
			def name = toString()
			def wrapped = inWrapped
		}
		if (keys.contains(inKey)) throw new IllegalArgumentException("Duplicate key found in enum: " + inKey)
		keys += inKey -> enumVal
		enumVal
	}

	def getValues = {
		super.values.map(v => v.asInstanceOf[ExtendedValue]).asInstanceOf[Set[ExtendedValue]].toSeq
	}

	def valueOf(name: String) = try{Some(withName(name).asInstanceOf[ExtendedValue])} catch {case _ => None}

	def getEnum(key: K): Option[ExtendedValue] = {
		keys.get(key)
	}
}

abstract class EnumWithStringKeyAndValue[T] extends EnumWithKeyAndValue[String, T] {

	def Value(inWrapped: T): ExtendedValue = {
		new Val(nextId) with ValueWithKey[String,T] {
			def key = toString()
			def name = toString()
			def wrapped = inWrapped
		}
	}


}
