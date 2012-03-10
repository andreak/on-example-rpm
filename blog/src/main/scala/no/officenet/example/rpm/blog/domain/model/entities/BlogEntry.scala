package no.officenet.example.rpm.blog.domain.model.entities

import javax.persistence._
import org.apache.commons.lang.builder.ToStringBuilder
import org.joda.time.DateTime
import no.officenet.example.rpm.support.infrastructure.jpa.types.StringField
import no.officenet.example.rpm.support.domain.model.entities.{AbstractChangableEntity, User}


@Entity
@Table(name = "blog_entry")
@SequenceGenerator(name = "SEQ_STORE", sequenceName = "blog_entry_id_seq", allocationSize = 1)
class BlogEntry(_blog: Blog, _created: DateTime,
			  _createdBy: User,
			  _title: String,
			  _summary: String,
			  _content: String)
	extends AbstractChangableEntity(_created, _createdBy) {

	def this() {
		this(null, null, null, null, null, null)
	}

	def this(blog: Blog, created: DateTime, createdBy: User) {
		this(blog, created, createdBy, null, null, null)
	}

	def this(blog: Blog, createdBy: User) {
		this(blog, null, createdBy, null, null, null)
	}

	@ManyToOne(optional = false)
	@net.sf.oval.constraint.NotNull
	@JoinColumn(name = "blog_id")
	var blog = _blog

	@Column(name = "title_text", nullable = false)
	@net.sf.oval.constraint.NotNull
	@net.sf.oval.constraint.NotBlank
	var title: String = _title

	@Column(name = "summary_text")
	@net.sf.oval.constraint.NotNull
	@net.sf.oval.constraint.NotBlank
	var summary: String = _summary

	@Column(name = "content_text", nullable = false)
	@net.sf.oval.constraint.NotNull
	@net.sf.oval.constraint.NotBlank
	var content: String = _content

	@OneToMany(fetch = FetchType.LAZY)
	@JoinColumn(name = "commented_id")
	@OrderBy("created ASC")
	var comments: java.util.List[Comment] = new java.util.ArrayList[Comment]()

	override def toString = new ToStringBuilder(this).append("id", id).
		append("title", title).toString
}

object BlogEntryJPAFields {

	object title extends StringField[BlogEntry]

	object summary extends StringField[BlogEntry]

	object content extends StringField[BlogEntry]

}