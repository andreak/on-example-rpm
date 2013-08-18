package no.officenet.example.rpm.projectmgmt.domain.model.entities

import org.apache.commons.lang.builder.ToStringBuilder
import org.joda.time.DateTime
import java.util.ArrayList
import javax.persistence._
import no.officenet.example.rpm.projectmgmt.domain.model.enums.{ProjectTypeConverter, ProjectType}

import no.officenet.example.rpm.support.infrastructure.jpa.types._
import net.sf.oval.constraint.ValidateWithMethod
import no.officenet.example.rpm.support.infrastructure.jpa._
import no.officenet.example.rpm.support.domain.model.entities.{AbstractChangableEntity, User}
import no.officenet.example.rpm.support.infrastructure.validation.OptionalMax

@Entity
@Table(name = "project")
@SequenceGenerator(name = "ProjectSEQ_STORE", sequenceName = "project_id_seq", allocationSize = 1)
class Project(_created: DateTime, _createdBy: User)
	extends AbstractChangableEntity(_created, _createdBy) {

	def this() {
		this(null, null)
	}
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ProjectSEQ_STORE")
	var id: java.lang.Long = null

	@Column(name = "name", nullable = false)
	@net.sf.oval.constraint.NotNull
	@net.sf.oval.constraint.Size(max = 20)
	var name: String = null

	@Column(name = "description")
	@ValidateWithMethod(methodName = "validateDescription", parameterType = classOf[Option[String]],
	message = "validation_error_methodValidation_Project_description")
	var description: Option[String] = None

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "project", orphanRemoval = true)
	var activityList: java.util.List[Activity] = new ArrayList[Activity]()

	@Column(name = "project_type", nullable = false)
	@net.sf.oval.constraint.NotNull
	@Convert(converter = classOf[ProjectTypeConverter])
	var projectType: ProjectType.ExtendedValue = null

	@Column(name = "budget")
	@OptionalMax(value = 999999.0)
	@Convert(converter = classOf[OptionLongConverter])
	var budget: Option[Long] = None

	@Column(name = "estimate_hours")
	@OptionalMax(value = 999999.0)
	@Convert(converter = classOf[OptionIntConverter])
	var estimateHours: Option[Int] = None

	@Column(name = "estimated_start_date")
	var estimatedStartDate: Option[DateTime] = None

	override def toString = new ToStringBuilder(this).append("id", id).append("name", name).toString

	private def validateDescription(_description: Option[String]): Boolean = {
		_description.map(!_.contains("nisse")).getOrElse(true)
	}
}

object Project {
	object name extends StringField[Project]
	object description extends StringField[Project]
	object budget extends LongField[Project]
	object estimatedStartDate extends DateTimeField[Project]
	object projectType extends JpaField[Project, ProjectType.ExtendedValue]
}