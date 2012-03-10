package no.officenet.example.rpm.blog.domain.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.beans.factory.annotation.Autowired
import repository.BlogEntryRepository
import org.joda.time.{DateTimeUtils, DateTime}
import no.officenet.example.rpm.blog.domain.model.entities.BlogEntry
import no.officenet.example.rpm.support.domain.service.GenericDomainService
import no.officenet.example.rpm.blog.domain.event.BlogEntryUpdatedEvent
import no.officenet.example.rpm.support.domain.events.{OperationType, AfterCommitEventDispatcher}

trait BlogEntryService extends GenericDomainService[BlogEntry] {
	val blogEntryRepository: BlogEntryRepository

	repository = blogEntryRepository

	def createBlogEntry(blogEntry: BlogEntry) = {
		blogEntry.created = new DateTime(DateTimeUtils.currentTimeMillis())
		val persistentEntity = blogEntryRepository.save(blogEntry)
		AfterCommitEventDispatcher.registerAfterCommitEvent(BlogEntryUpdatedEvent(OperationType.CREATE, persistentEntity))
		persistentEntity
	}

	def updateBlogEntry(blogEntry: BlogEntry) = {
		blogEntry.modified = new DateTime(DateTimeUtils.currentTimeMillis())
		val persistentEntity = blogEntryRepository.save(blogEntry)
		AfterCommitEventDispatcher.registerAfterCommitEvent(BlogEntryUpdatedEvent(OperationType.UPDATE, persistentEntity))
		persistentEntity
	}
}

@Service
@Transactional
class BlogEntryServiceImpl @Autowired()(val blogEntryRepository: BlogEntryRepository) extends BlogEntryService {

}