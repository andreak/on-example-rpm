package no.officenet.example.rpm.support.infrastructure.jpa

import java.io.Serializable
import org.springframework.transaction.annotation.Transactional
import collection.JavaConversions.asScalaBuffer
import collection.JavaConversions.iterableAsScalaIterable
import javax.annotation.Resource
import javax.validation.{ConstraintViolation, ConstraintViolationException, Validator}
import validation.MethodValidationGroup
import no.officenet.example.rpm.support.infrastructure.errorhandling.ObjectNotFoundByPrimaryKeyException
import javax.persistence.criteria.{Order, CriteriaBuilder}
import collection.mutable.{ListBuffer, Buffer}

@Transactional
trait WritableRepository[T <: AnyRef, PK <: Serializable] extends RepositorySupport {

	@Resource
	var validator: Validator = _

	@Resource
	var validateRepositories: java.lang.Boolean = true

	def save(entity: T): T = {
		if (entity == null) throw new IllegalArgumentException(getClass.getSimpleName + ": Cannot save null-object")
		if (validateRepositories) {
			val fieldViolations = validator.validate(entity)
			val methodViolations = validator.validate(entity, classOf[MethodValidationGroup])
			val constraintViolations = new java.util.LinkedHashSet[ConstraintViolation[_]](fieldViolations.size() + methodViolations.size())
			fieldViolations.foreach(v => constraintViolations.add(v))
			methodViolations.foreach(v => constraintViolations.add(v))
			if (!constraintViolations.isEmpty) {
				throw new ConstraintViolationException(constraintViolations.asInstanceOf[java.util.Set[ConstraintViolation[_]]])
			}
		}
		entityManager.merge(entity)
	}
}

trait ReadableRepository[T <: AnyRef, PK <: Serializable] extends RepositorySupport {

	def retrieve(id: PK)(implicit m: Manifest[T]) = entityManager.find[T](m.erasure.asInstanceOf[Class[T]], id) match {
		case Some(e) => e
		case _ => throw new ObjectNotFoundByPrimaryKeyException(m.erasure.getSimpleName, id.toString)
	}

	def findAll(orderBy: OrderBy*)(implicit m: Manifest[T]): Buffer[T] = {
		findAll(None, None, orderBy:_*)
	}

	def findAll(offset: Option[Int], maxSize: Option[Int], orderBy: OrderBy*)(implicit m: Manifest[T]) = {
		val criteriaBuilder = entityManager.getCriteriaBuilder
		val criteriaQuery = criteriaBuilder.createQuery[T](m.erasure.asInstanceOf[Class[T]])
		val c = criteriaQuery.from(m.erasure)
		val orderByList = new ListBuffer[Order]
		for (ob <- orderBy) {
			val orderByElement = ob.order match {
				case no.officenet.example.rpm.support.infrastructure.jpa.Order.ASC => criteriaBuilder.asc(c.get(ob.fieldName))
				case _ => criteriaBuilder.desc(c.get(ob.fieldName))
			}
			orderByList += orderByElement
		}
		criteriaQuery.orderBy(orderByList:_*)
		val typedQuery = entityManager.createQuery[T](criteriaQuery)
		for (o <- offset) {
			typedQuery.setFirstResult(o)
		}
		for (l <- maxSize) {
			typedQuery.setMaxResults(l)
		}
		asScalaBuffer(typedQuery.getResultList)
	}

	def size(implicit m: Manifest[T]) = {
		val qb: CriteriaBuilder = entityManager.getCriteriaBuilder
		val cq = qb.createQuery(classOf[java.lang.Long])
		cq.select(qb.count(cq.from(m.erasure)))
		entityManager.createQuery(cq).getSingleResult
	}

}

trait DeletableRepository[T <: AnyRef, PK <: Serializable] extends RepositorySupport {
	def remove(entity: T) {
		val oldVersion = entityManager.merge(entity)
		entityManager.remove(oldVersion.asInstanceOf[AnyRef])
	}

	def remove(id: PK)(implicit m: Manifest[T]) {
		// Note: getReference throws EntityNotFoundException if not found
		entityManager.remove(entityManager.getReference(m.erasure, id).asInstanceOf[AnyRef])
	}
}

trait GenericRepository[T <: AnyRef, PK <: Serializable] extends ReadableRepository[T, PK]
															with WritableRepository[T, PK]
															with DeletableRepository[T, PK] {
}