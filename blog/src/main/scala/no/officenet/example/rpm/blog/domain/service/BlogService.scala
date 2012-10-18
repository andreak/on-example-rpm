package no.officenet.example.rpm.blog.domain.service

import org.joda.time.{DateTimeUtils, DateTime}
import repository.BlogRepository
import collection.mutable.Buffer
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.beans.factory.annotation.Autowired
import no.officenet.example.rpm.support.domain.service.GenericDomainService
import no.officenet.example.rpm.blog.domain.model.entities.{BlogEntrySummary, Blog}


trait BlogService extends GenericDomainService[Blog] {
	val blogRepository: BlogRepository

	repository = blogRepository

	def createBlog(blog: Blog) = {
		blog.created = new DateTime(DateTimeUtils.currentTimeMillis())
		val persistentEntity = blogRepository.save(blog)
		persistentEntity
	}

	def updateBlog(blog: Blog) = {
		blog.modified = Some(new DateTime(DateTimeUtils.currentTimeMillis()))
		val persistentEntity = blogRepository.save(blog)
		persistentEntity
	}

	def findByNameForUser(blogName: String, userName: String): Option[Blog] = {
		blogRepository.findByNameForUser(blogName, userName)
	}

	def retrieveBlogSummaries(blogId: Long): Buffer[BlogEntrySummary] = {
		blogRepository.retrieveBlogSummaries(blogId)
	}
}

@Service
@Transactional
class BlogServiceImpl @Autowired()(val blogRepository: BlogRepository) extends BlogService {

}