package no.officenet.example.rpm.support.domain.model.entities

import javax.persistence.{ManyToOne, JoinColumn, Column, Id, Table, Entity}
import org.joda.time.DateTime

/**
 * JPA-entity just to generate the persistent_logins table used for remember-me
 */
@Entity
@Table(name = "persistent_logins")
class PersistentLogin {

	@Id
	@Column(name = "series", length = 64)
	var series: String = null

	@ManyToOne(optional = false)
	@JoinColumn(name = "username", referencedColumnName = "username")
	var user: User = null

	@Column(name = "last_used", nullable = false)
	var lastUsed: DateTime = null

	@Column(name = "token", length = 64, nullable = false)
	var token: String = null
}
