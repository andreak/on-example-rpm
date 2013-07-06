package no.officenet.example.rpm.blog.domain.model.entities

import javax.persistence._
import org.apache.commons.lang.builder.ToStringBuilder
import no.officenet.example.rpm.support.domain.model.entities.AbstractDomainObject

@Entity
@Table(name = "comment_vote")
@SequenceGenerator(name = "CommentVoteSEQ_STORE", sequenceName = "comment_vote_id_seq", allocationSize = 1)
class CommentVote extends AbstractDomainObject {

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "CommentVoteSEQ_STORE")
	var id: java.lang.Long = null

	@Column(name = "vote_value", nullable = false)
	var voteValue: Int = 0

	override def toString = new ToStringBuilder(this).append("id", id).
		append("voteValue", voteValue).toString

	def addVote(vote: Boolean): Int = {
		if (vote) {
			voteValue += 1
		} else {
			voteValue -= 1
		}
		voteValue
	}
}