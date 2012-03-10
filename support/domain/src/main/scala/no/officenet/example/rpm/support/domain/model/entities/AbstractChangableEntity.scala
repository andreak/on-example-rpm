package no.officenet.example.rpm.support.domain.model.entities

/**
 * Copyright OfficeNet AS
 */

import javax.persistence.{FetchType, JoinColumn, ManyToOne, Column, MappedSuperclass}
import org.joda.time.DateTime
import no.officenet.example.rpm.support.infrastructure.jpa.CustomJpaType

@MappedSuperclass
abstract class AbstractChangableEntity(_created: DateTime, _createdBy: User)
	extends AbstractDomainObject {

	@Column(name = "created", nullable = false, updatable = false)
	@org.hibernate.annotations.Type(`type` = CustomJpaType.DateTime,
		parameters = Array(new org.hibernate.annotations.Parameter(name = "databaseZone", value = "jvm")))
	@net.sf.oval.constraint.NotNull
	var created = _created

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "created_by", updatable = false)
	@net.sf.oval.constraint.NotNull
	var createdBy = _createdBy

	@Column(name = "modified")
	@org.hibernate.annotations.Type(`type` = CustomJpaType.DateTime,
		parameters = Array(new org.hibernate.annotations.Parameter(name = "databaseZone", value = "jvm")))
	var modified: DateTime = null

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "modified_by")
	var modifiedBy: User = null
}