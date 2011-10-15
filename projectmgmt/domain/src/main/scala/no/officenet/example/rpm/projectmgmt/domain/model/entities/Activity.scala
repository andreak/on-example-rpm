package no.officenet.example.rpm.projectmgmt.domain.model.entities

import org.joda.time.DateTime
import no.officenet.example.rpm.support.domain.model.entities.{AbstractDomainObject, User}
import no.officenet.example.rpm.projectmgmt.domain.model.enums.ActivityType
import org.apache.commons.lang.builder.ToStringBuilder
import javax.persistence._
import java.util.{List => JUList, ArrayList => JUArrayList}

@Entity
@Table(name = "t_activity")
@SequenceGenerator(name = "SEQ_STORE", sequenceName = "activity_id_seq", allocationSize = 1)
class Activity(_created: DateTime, _createdBy: User, _name: String, _project: Project, _parent: Activity) extends AbstractDomainObject(_created, _createdBy) {

	def this() {
		this(null, null, null, null, null)
	}

	@Column(name = "name", nullable = false)
	@javax.validation.constraints.NotNull
	@javax.validation.constraints.Size(max = 20)
	var name = _name

	@Column(name = "description", nullable = true)
	@javax.validation.constraints.Size(max = 100)
	var description: Option[String] = null

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "project_id")
	var project = _project

	@Column(name = "activity_type", nullable = false)
	@org.hibernate.annotations.Type(`type` = "no.officenet.example.rpm.projectmgmt.domain.model.enums.ActivityUserType")
	var activityType: ActivityType.ExtendedValue = null

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "parent_id")
	var parent = _parent

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "parent")
	var children: JUList[Activity] = new JUArrayList[Activity]

	override def toString = new ToStringBuilder(this).append("id", id).append("name", name).toString

}