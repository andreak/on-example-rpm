package no.officenet.example.rpm.support.domain.util

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

sealed trait NamedValue  {
    def name: String
}

abstract class EnumWithDescriptionAndObject[T] extends Enumeration {

	val enum = new Enumeration(){}

	type ExtendedValue = Value with ValueWithDescription[T]

	def Value(inDescription: String, inWrapped: T): ExtendedValue = {
		new Val(nextId) with ValueWithDescription[T] {
			def description = inDescription
			def name = toString()
			def wrapped = inWrapped
		}
	}

	def Value(inWrapped: T): ExtendedValue = {
		new Val(nextId) with ValueWithDescription[T] {
			def description = ""
			def name = toString()
			def wrapped = inWrapped
		}
	}

	def getValues = {
		super.values.map(v => v.asInstanceOf[ExtendedValue]).asInstanceOf[Set[ExtendedValue]].toSeq
	}

	def valueOf(name: String) = try{Some(withName(name).asInstanceOf[ExtendedValue])} catch {case _ => None}

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
