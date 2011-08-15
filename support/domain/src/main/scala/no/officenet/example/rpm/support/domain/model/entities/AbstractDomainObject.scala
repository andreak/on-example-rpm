package no.officenet.example.rpm.support.domain.model.entities

import javax.persistence._
import org.joda.time.DateTime

@MappedSuperclass
abstract class AbstractDomainObject(_created: DateTime, _createdBy: User) extends AbstractModel[java.lang.Long] {
	@Version
	var version: Long = _

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_STORE")
	var id: java.lang.Long = null

	@Column(name = "created", nullable = false, updatable = false)
	@org.hibernate.annotations.Type(`type` = "org.jadira.usertype.dateandtime.joda.PersistentDateTime",
									parameters = Array(new org.hibernate.annotations.Parameter(name = "databaseZone", value = "jvm")))
	@javax.validation.constraints.NotNull
	var created = _created

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "created_by", updatable = false)
	@javax.validation.constraints.NotNull
	var createdBy = _createdBy

	@Column(name = "modified")
	@org.hibernate.annotations.Type(`type` = "org.jadira.usertype.dateandtime.joda.PersistentDateTime",
									parameters = Array(new org.hibernate.annotations.Parameter(name = "databaseZone", value = "jvm")))
	var modified: DateTime = null

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "modified_by")
	var modifiedBy: User = null

	@Transient
	def getPrimaryKey = id
}
