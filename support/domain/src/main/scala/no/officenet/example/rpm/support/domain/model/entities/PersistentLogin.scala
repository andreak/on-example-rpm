package no.officenet.example.rpm.support.domain.model.entities

import javax.persistence.{ManyToOne, JoinColumn, Column, Id, Table, Entity}
import org.hibernate.annotations.{OnDeleteAction, OnDelete, Index}

/**
 * JPA-entity just to generate the persistent_logins table used for remember-me
 */
@Entity
@Table(name = "persistent_logins")
@org.hibernate.annotations.Table(appliesTo="persistent_logins",
	indexes = Array(new Index(name = "persistent_logins_username_idx", columnNames = Array("username"))))
class PersistentLogin {

	@Id
	@Column(name = "series", length = 64)
	var series: String = null

	@ManyToOne(optional = false)
	@JoinColumn(name = "username", referencedColumnName = "username")
	@OnDelete(action = OnDeleteAction.CASCADE)
	var user: User = null

	@Column(name = "last_used", nullable = false)
	var lastUsed: java.util.Date = null

	@Column(name = "token", length = 64, nullable = false)
	var token: String = null
}
