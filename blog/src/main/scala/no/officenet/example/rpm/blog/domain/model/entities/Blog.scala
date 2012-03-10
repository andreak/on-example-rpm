package no.officenet.example.rpm.blog.domain.model.entities

import org.joda.time.DateTime
import org.apache.commons.lang.builder.ToStringBuilder
import no.officenet.example.rpm.support.infrastructure.jpa.CustomJpaType
import no.officenet.example.rpm.support.domain.model.entities.{AbstractChangableEntity, User}
import javax.persistence.{SequenceGenerator, Column, Entity, Table}

@Entity
@Table(name = "blog")
@SequenceGenerator(name = "SEQ_STORE", sequenceName = "blog_id_seq", allocationSize = 1)
class Blog(_created: DateTime, _createdBy: User, _key: String, _description: Option[String])
	extends AbstractChangableEntity(_created, _createdBy) {

	def this() {
		this(null, null, null, null)
	}

	@Column(name = "key", nullable = false)
	@net.sf.oval.constraint.NotNull
	@net.sf.oval.constraint.NotBlank
	var key: String = _key

	@Column(name = "description")
	@org.hibernate.annotations.Type(`type` = CustomJpaType.StringOptionUserType)
	var description: Option[String] = _description

	override def toString = new ToStringBuilder(this).append("id", id).append("key", key).toString
}
