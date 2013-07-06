package no.officenet.example.rpm.blog.domain.model.entities

import org.joda.time.DateTime
import org.apache.commons.lang.builder.ToStringBuilder
import no.officenet.example.rpm.support.infrastructure.jpa.OptionStringConverter
import no.officenet.example.rpm.support.domain.model.entities.{AbstractChangableEntity, User}
import javax.persistence._

@Entity
@Table(name = "blog")
@SequenceGenerator(name = "BlogSEQ_STORE", sequenceName = "blog_id_seq", allocationSize = 1)
class Blog(_created: DateTime, _createdBy: User, _key: String, _description: Option[String])
	extends AbstractChangableEntity(_created, _createdBy) {

	def this() {
		this(null, null, null, null)
	}
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "BlogSEQ_STORE")
	var id: java.lang.Long = null


	@Column(name = "key", nullable = false)
	@net.sf.oval.constraint.NotNull
	@net.sf.oval.constraint.NotBlank
	var key: String = _key

	@Column(name = "description")
	@Convert(converter = classOf[OptionStringConverter])
	var description: Option[String] = _description

	override def toString = new ToStringBuilder(this).append("id", id).append("key", key).toString
}
