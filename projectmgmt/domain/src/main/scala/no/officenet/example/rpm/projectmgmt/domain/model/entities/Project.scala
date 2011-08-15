package no.officenet.example.rpm.projectmgmt.domain.model.entities

import org.apache.commons.lang.builder.ToStringBuilder
import org.joda.time.DateTime
import no.officenet.example.rpm.support.domain.model.entities.{AbstractDomainObject, User}
import java.util.ArrayList
import javax.persistence._
import no.officenet.example.rpm.projectmgmt.domain.model.enums.ProjectType
import no.officenet.example.rpm.support.infrastructure.jpa.validation.{MethodValidationGroup, ValidateWithMethod}

@Entity
@Table(name = "t_project")
@SequenceGenerator(name = "SEQ_STORE", sequenceName = "project_id_seq", allocationSize = 1)
@ValidateWithMethod.List(
		Array(new ValidateWithMethod(fieldName = "description", methodName = "validateDescription", groups = Array(classOf[MethodValidationGroup])))
)
class Project(_created: DateTime, _createdBy: User) extends AbstractDomainObject(_created, _createdBy) {

	def this() {
		this(null, null)
	}

	@Column(name = "name", nullable = false)
	@javax.validation.constraints.NotNull
	@javax.validation.constraints.Size(max = 20)
	var name: String = null

	@Column(name = "description")
	@javax.validation.constraints.Pattern(regexp = "\\D*")
	var description: String = null

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "project", orphanRemoval = true)
	var activityList: java.util.List[Activity] = new ArrayList[Activity]()

	@Column(name = "project_type", nullable = false)
	@org.hibernate.annotations.Type(`type` = "no.officenet.example.rpm.projectmgmt.domain.model.enums.ProjectUserType")
	@javax.validation.constraints.NotNull
	var projectType: ProjectType.ExtendedValue = null

	@Column(name = "budget")
	@javax.validation.constraints.Max(value = 999999L)
	var budget: java.lang.Long = null

	@Column(name = "estimated_start_date")
	@org.hibernate.annotations.Type(`type` = "org.jadira.usertype.dateandtime.joda.PersistentDateTime",
									parameters = Array(new org.hibernate.annotations.Parameter(name = "databaseZone", value = "jvm")))
	var estimatedStartDate: DateTime = null

	@Column(name = "pet_id")
	var petId: java.lang.Long = null

	override def toString = new ToStringBuilder(this).append("id", id).append("name", name).toString

	private def validateDescription(): Boolean = {
		!(description.contains("nisse"))
	}
}