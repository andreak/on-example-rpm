package no.officenet.example.rpm.support.infrastructure.jpa

import org.springframework.stereotype.Repository
import org.scala_libs.jpa.{ScalaEntityManager, ScalaEMFactory}
import javax.persistence.criteria.CriteriaQuery
import org.springframework.orm.jpa.EntityManagerFactoryInfo
import javax.persistence.EntityManager

@Repository
trait RepositorySupport {
	var entityManager: ExtScalaEntityManager = null

	def setEntityManager(entityManager: EntityManager)

	protected def initializeEntityManager(entityManager: EntityManager) {
		this.entityManager = new EMFactory(entityManager, getPersistentUnitName(entityManager)).newEM
	}

	def flush() {
		entityManager.flush()
	}

	private def getPersistentUnitName(entityManager: EntityManager) = {
		entityManager.getEntityManagerFactory.asInstanceOf[EntityManagerFactoryInfo].getPersistenceUnitName
	}

}

class ExtScalaEntityManager(owner: ScalaEMFactory, underlying: EntityManager) extends ScalaEntityManager {

	def em = underlying
	val factory = owner

	def getCriteriaBuilder = underlying.getCriteriaBuilder

	def createQuery[T <: AnyRef](criteriaQuery: CriteriaQuery[T]) = underlying.createQuery[T](criteriaQuery)

}

class EMFactory(entityManager: EntityManager, unitName: String) extends ScalaEMFactory {
	protected def openEM() = entityManager

	def closeEM(em: EntityManager) {
		em.close()
	}

	protected def getUnitName = unitName

	override def newEM = new ExtScalaEntityManager(this, entityManager)

}