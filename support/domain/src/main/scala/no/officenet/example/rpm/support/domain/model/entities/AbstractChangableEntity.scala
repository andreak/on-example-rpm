package no.officenet.example.rpm.support.domain.model.entities

/**
 * Copyright OfficeNet AS
 */

import javax.persistence._
import org.joda.time.DateTime
import no.officenet.example.rpm.support.infrastructure.jpa.OptionDateTimeConverter

@MappedSuperclass
abstract class AbstractChangableEntity(_created: DateTime, _createdBy: User)
	extends AbstractDomainObject {

	@Column(name = "created", nullable = false, updatable = false)
	@net.sf.oval.constraint.NotNull
	var created = _created

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "created_by", updatable = false)
	@net.sf.oval.constraint.NotNull
	var createdBy = _createdBy

	@Column(name = "modified")
	var modified: Option[DateTime] = None

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "modified_by")
	private[this] var modifiedBy: User = null
	def modifiedByOpt = Option(modifiedBy)
	def modifiedByOpt_=(newVal:Option[User]) = modifiedBy = newVal.orNull

}