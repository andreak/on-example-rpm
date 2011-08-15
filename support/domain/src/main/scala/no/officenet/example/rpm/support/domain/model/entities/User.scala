package no.officenet.example.rpm.support.domain.model.entities

import no.officenet.example.rpm.support.infrastructure.jpa.validation.ValidateWithMethod
import org.apache.commons.lang.builder.ToStringBuilder
import javax.persistence._
import org.joda.time.DateTime

@Entity
@Table(name = "t_user")
@SequenceGenerator(name = "SEQ_STORE", sequenceName = "user_id_seq", allocationSize = 1)
@ValidateWithMethod.List(
		Array(new ValidateWithMethod(fieldName = "firstName", ignoreIfNull = false, methodName = "validateFirstName"))
)
class User(_created: DateTime, _createdBy: User, _userName: String, _plainTextPassword: String)
	extends AbstractDomainObject (_created, _createdBy) {

	def this() {
		this(null, null, null, null)
	}

	@Column(name = "username", unique = true, nullable = false)
	@javax.validation.constraints.NotNull
	var userName: String = _userName

	@Column(name = "password", nullable = false)
	@javax.validation.constraints.NotNull
	var password: String = _

	@Column(name = "first_name")
	var firstName: String = _

	@Column(name = "last_name")
	var lastName: String = _

	@Transient
	var plainTextPassword: String = _plainTextPassword

	override def toString = new ToStringBuilder(this).append("id", id).append("userName", userName).toString

	def displayName = firstName + " " + lastName
	
	private def validateFirstName(): Boolean = {
		true
	}

}