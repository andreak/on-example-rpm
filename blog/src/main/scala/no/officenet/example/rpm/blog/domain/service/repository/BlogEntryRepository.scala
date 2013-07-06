package no.officenet.example.rpm.blog.domain.service.repository

import org.springframework.stereotype.Repository
import collection.mutable.Buffer
import no.officenet.example.rpm.support.infrastructure.jpa.{PersistenceUnits, GenericEntityRepository}
import no.officenet.example.rpm.blog.domain.model.entities.{BlogEntrySummary, BlogEntry}

@Repository
class BlogEntryRepositoryImpl extends BlogEntryRepository with PersistenceUnits.PersistenceUnitRPM

trait BlogEntryRepository extends GenericEntityRepository[BlogEntry] {

	def findBlogEntrySummariesForUser(userName: String): Buffer[BlogEntrySummary] = {
		val query = entityManager.createQuery[BlogEntrySummary]("""
		SELECT new no.officenet.example.rpm.blog.domain.model.entities.BlogEntrySummary(be.blog.id, be.id, be.created, be.createdBy,
		be.title, be.summary, be.content, (SELECT COUNT(c) FROM Comment c WHERE c.commentedId = be.id)) FROM BlogEntry be
		WHERE be.createdBy.userName = :userName
		ORDER BY be.created DESC
		""")
		query.setParams("userName" -> userName).
			setMaxResults(10)
		val results = query.getResultList()
		results
	}
}
