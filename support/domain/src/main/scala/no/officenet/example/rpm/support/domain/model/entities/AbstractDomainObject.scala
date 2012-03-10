package no.officenet.example.rpm.support.domain.model.entities

import javax.persistence._

@MappedSuperclass
abstract class AbstractDomainObject extends AbstractModel[java.lang.Long] {
	@Version
	var version: Long = _

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_STORE")
	var id: java.lang.Long = null

	@Transient
	def getPrimaryKey = id
}
