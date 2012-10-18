package no.officenet.example.rpm.blog.domain.model.entities

import javax.persistence._
import org.apache.commons.lang.builder.ToStringBuilder
import org.joda.time.DateTime
import java.util.{ArrayList, List => juList}
import no.officenet.example.rpm.support.infrastructure.jpa.types.StringField
import no.officenet.example.rpm.support.domain.model.entities.{AbstractChangableEntity, User}

@Entity
@Table(name = "comment")
@SequenceGenerator(name = "SEQ_STORE", sequenceName = "comment_id_seq", allocationSize = 1)
class Comment(_created: DateTime,
			  _createdBy: User,
			  _commentText: String,
			  _commentedId: java.lang.Long)
	extends AbstractChangableEntity(_created, _createdBy) {

	def this() {
		this(null, null, null, null)
	}

	def this(createdBy: User) {
		this(null, createdBy, null, null)
	}

	@Column(name = "commented_id", nullable = false, updatable = false)
	@net.sf.oval.constraint.NotNull
	var commentedId = _commentedId

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_id")
	private[this] var parent: Comment = null
	def parentOpt = Option(parent)
	def parentOpt_=(newVal:Option[Comment]) = parent = newVal.orNull

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "parent")
	var children: juList[Comment] = new ArrayList[Comment]()

	@Column(name = "comment_text", nullable = false)
	@net.sf.oval.constraint.NotNull
	@net.sf.oval.constraint.NotBlank
	var commentText: String = _commentText

	@OneToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "comment_vote_id")
	var commentVote: CommentVote = null

	override def toString = new ToStringBuilder(this).append("id", id).
		append("commentText", commentText).
		append("parentId", if (parent == null) "<none>" else parent.id.toString).
		toString
}

object CommentJPAFields {

	object commentText extends StringField[Comment]

}