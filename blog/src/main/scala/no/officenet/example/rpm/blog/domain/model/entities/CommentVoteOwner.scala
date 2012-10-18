package no.officenet.example.rpm.blog.domain.model.entities

import javax.persistence._
import org.apache.commons.lang.builder.ToStringBuilder
import org.joda.time.DateTime
import no.officenet.example.rpm.support.domain.model.entities.{User, AbstractDomainObject}
import no.officenet.example.rpm.support.infrastructure.jpa.CustomJpaType

@Entity
@Table(name = "comment_vote_owner")
@SequenceGenerator(name = "SEQ_STORE", sequenceName = "comment_vote_owner_id_seq", allocationSize = 1)
class CommentVoteOwner(_created: DateTime, _comment: Comment, _voter: User, _voteValue: Boolean)
	extends AbstractDomainObject {

	def this() {
		this(null, null, null, false)
	}

	@Column(name = "created", nullable = false, updatable = false)
	var created = _created

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "comment_id")
	var comment: Comment = _comment

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "voter_id")
	var voter: User = _voter

	@Column(name = "vote_value", nullable = false)
	var voteValue: Boolean = _voteValue

	override def toString = new ToStringBuilder(this).append("id", id).append("comment", comment.id).
		append("voter", voter.id).append("voteValue", voteValue).toString
}