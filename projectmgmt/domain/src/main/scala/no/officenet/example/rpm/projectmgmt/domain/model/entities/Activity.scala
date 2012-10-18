package no.officenet.example.rpm.projectmgmt.domain.model.entities

import org.joda.time.DateTime
import no.officenet.example.rpm.projectmgmt.domain.model.enums.ActivityType
import org.apache.commons.lang.builder.ToStringBuilder
import javax.persistence._
import java.util.{List => JUList, ArrayList => JUArrayList}
import no.officenet.example.rpm.support.infrastructure.jpa.CustomJpaType
import no.officenet.example.rpm.support.domain.model.entities.{AbstractChangableEntity, User}

@Entity
@Table(name = "activity")
@SequenceGenerator(name = "SEQ_STORE", sequenceName = "activity_id_seq", allocationSize = 1)
class Activity(_created: DateTime, _createdBy: User, _name: String, _project: Project, _parent: Option[Activity])
	extends AbstractChangableEntity(_created, _createdBy) {

	def this() {
		this(null, null, null, null, None)
	}

	@Column(name = "name", nullable = false)
	@net.sf.oval.constraint.NotNull
	@net.sf.oval.constraint.Size(max = 20)
	var name = _name

	@Column(name = "description", nullable = true)
//	@net.sf.oval.constraint.Size(max = 100)
	@org.hibernate.annotations.Type(`type` = CustomJpaType.StringOptionUserType)
	var description: Option[String] = None

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "project_id")
	var project = _project

	@Column(name = "activity_type", nullable = false)
	@net.sf.oval.constraint.NotNull
	@org.hibernate.annotations.Type(`type` = CustomJpaType.ActivityUserType)
	var activityType: ActivityType.ExtendedValue = null

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "parent_id")
	private[this] var parent = _parent.orNull
	def parentOpt = Option(parent)
	def parentOpt_=(newVal:Option[Activity]) = parent = newVal.orNull

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "parent")
	var children: JUList[Activity] = new JUArrayList[Activity]

	override def toString = new ToStringBuilder(this).append("id", id).append("name", name).toString

}