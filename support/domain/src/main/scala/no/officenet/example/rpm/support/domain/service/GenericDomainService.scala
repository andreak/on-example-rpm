package no.officenet.example.rpm.support.domain.service

import org.springframework.beans.factory.InitializingBean
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.Assert
import no.officenet.example.rpm.support.infrastructure.jpa.{GenericEntityRepository, OrderBy, GenericRepository}

@Transactional
trait GenericDomainService[T <: AnyRef] extends InitializingBean {

	var repository: GenericEntityRepository[T] = null

	def retrieve(id: java.lang.Long)(implicit m: Manifest[T]) = repository.retrieve(id)(m)

	def retrieveFull(id: java.lang.Long)(implicit m: Manifest[T]) = {
		val entity = retrieve(id)
		entity
	}

	def findAll(orderBy: OrderBy*)(implicit m: Manifest[T]) = repository.findAll(orderBy:_*)(m)

	def create(entity: T): T = {
		repository.save(entity)
	}

	def update(entity: T): T = {
		repository.save(entity)
	}

	def delete(entity: T) {
		repository.remove(entity)
	}

	def delete(id: java.lang.Long)(implicit m: Manifest[T]) {
		repository.remove(id)(m)
	}

	def afterPropertiesSet() {
		Assert.notNull(repository, "repository must not be null")
	}
}