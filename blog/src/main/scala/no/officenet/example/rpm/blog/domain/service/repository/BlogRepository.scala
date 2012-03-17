package no.officenet.example.rpm.blog.domain.service.repository

import org.springframework.stereotype.Repository
import collection.mutable.Buffer
import no.officenet.example.rpm.support.infrastructure.jpa.{GenericEntityRepository, PersistenceUnits}
import no.officenet.example.rpm.blog.domain.model.entities.{BlogEntrySummary, Blog}

@Repository
class BlogRepositoryImpl extends BlogRepository with PersistenceUnits.PersistenceUnitRPM

trait BlogRepository extends GenericEntityRepository[Blog] {

	def findByNameForUser(blogName: String, userName: String): Option[Blog] = {
		val query = entityManager.createQuery[Blog]("""
		SELECT b FROM Blog b WHERE b.createdBy.userName = :userName AND b.key = :blogName
			""")
		query.setParams("userName" -> userName, "blogName" -> blogName).
			findOne
	}

	def retrieveBlogSummaries(id: Long): Buffer[BlogEntrySummary] = {
		val query = entityManager.createQuery[BlogEntrySummary]("""
		SELECT new no.officenet.example.rpm.blog.domain.model.entities.BlogEntrySummary(be.blog.id, be.id, be.created, be.createdBy,
		be.title, be.summary, be.content, be.comments.size) FROM BlogEntry be
		WHERE be.blog.id = :blogId
		ORDER BY be.created DESC
		""")
		query.setParams("blogId" -> id).
			setMaxResults(10)
		val results = query.getResultList()
		results
	}
}
