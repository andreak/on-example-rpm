package no.officenet.example.rpm.support.domain.model.entities

import javax.persistence._

@MappedSuperclass
abstract class AbstractDomainObject extends AbstractModel[java.lang.Long] {
	@Version
	var version: Long = _

	var id: java.lang.Long

	@Transient
	def getPrimaryKey = id
}
