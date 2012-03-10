package no.officenet.example.rpm.blog.domain.model.entities

import org.joda.time.DateTime
import no.officenet.example.rpm.support.domain.model.entities.User

case class BlogEntrySummary(blogId: java.lang.Long,
							id: java.lang.Long,
							created: DateTime,
							createdBy: User,
							title: String,
							summary: String,
							content: String,
							numComments: Int)