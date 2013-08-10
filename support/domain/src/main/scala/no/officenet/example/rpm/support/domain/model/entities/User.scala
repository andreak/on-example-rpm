package no.officenet.example.rpm.support.domain.model.entities

import org.apache.commons.lang.builder.ToStringBuilder
import javax.persistence._
import org.joda.time.DateTime
import net.sf.oval.constraint.ValidateWithMethod
import no.officenet.example.rpm.support.infrastructure.jpa.OptionStringConverter

@Entity
@Table(name = "rpm_user")
@SequenceGenerator(name = "UserSEQ_STORE", sequenceName = "rpm_user_id_seq", allocationSize = 1)
class User(_created: DateTime, _createdBy: User, _userName: String, _plainTextPassword: String)
	extends AbstractChangableEntity (_created, _createdBy) {

	def this() {
		this(null, null, null, null)
	}
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "UserSEQ_STORE")
	var id: java.lang.Long = null


	@Column(name = "username", unique = true, nullable = false)
	@net.sf.oval.constraint.NotNull
	var userName: String = _userName

	@Column(name = "password", nullable = false)
	@net.sf.oval.constraint.NotNull
	var password: String = _

	@Column(name = "first_name")
	@ValidateWithMethod(methodName = "validateFirstName", parameterType = classOf[Option[String]])
	var firstName: Option[String] = None

	@Column(name = "last_name")
	var lastName: Option[String] = None

	@Column(name = "image_icon_path")
	var imageIconPath: Option[String] = None

	@Transient
	var plainTextPassword: String = _plainTextPassword

	override def toString = new ToStringBuilder(this).append("id", id).append("userName", userName).toString

	def displayName = firstName.getOrElse("") + (if (firstName.isDefined && lastName.isDefined) " " else "") + lastName.getOrElse("")
	
	private def validateFirstName(_firstName: Option[String]): Boolean = {
		true
	}

}