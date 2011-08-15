package no.officenet.example.rpm.support.domain.model.entities

import javax.persistence.Transient

abstract class AbstractModel[PK <: java.io.Serializable] extends Serializable  {

	@Transient
	def getPrimaryKey: PK

	override def hashCode = if (getPrimaryKey == null) 0 else getPrimaryKey.hashCode

	override def equals(other: Any) = other match {
		case that: AbstractModel[_] => (this.asInstanceOf[AnyRef]  eq that) || (getClass.isAssignableFrom(that.getClass) && null != getPrimaryKey && getPrimaryKey == that.getPrimaryKey)
		case _ => false
	}

	def toString: String

}