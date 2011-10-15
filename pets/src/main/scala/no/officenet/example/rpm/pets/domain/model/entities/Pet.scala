package no.officenet.example.rpm.pets.domain.model.entities

import org.apache.commons.lang.builder.ToStringBuilder
import org.joda.time.DateTime
import no.officenet.example.rpm.support.domain.model.entities.AbstractModel
import javax.persistence._
import no.officenet.example.rpm.support.infrastructure.jpa.types.StringField

@Entity
@Table(name = "t_pet")
@SequenceGenerator(name = "SEQ_STORE", sequenceName = "pet_id_seq", allocationSize = 1)
class Pet(_created: DateTime) extends AbstractModel[java.lang.Long] {

	def this() {
		this(null)
	}

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

	@Column(name = "modified")
	@org.hibernate.annotations.Type(`type` = "org.jadira.usertype.dateandtime.joda.PersistentDateTime",
									parameters = Array(new org.hibernate.annotations.Parameter(name = "databaseZone", value = "jvm")))
	var modified: DateTime = null

	@Column(name = "pet_name", nullable = false)
	@javax.validation.constraints.NotNull
	var petName: String = _

	@Transient
	def getPrimaryKey = id

	override def toString = new ToStringBuilder(this).append("id", id).append("petName", petName).toString

}

object Pet {
	object petName extends StringField[Pet]
}