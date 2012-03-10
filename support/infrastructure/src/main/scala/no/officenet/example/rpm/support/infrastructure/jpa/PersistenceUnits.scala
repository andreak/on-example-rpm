package no.officenet.example.rpm.support.infrastructure.jpa

import javax.persistence.{PersistenceContext, EntityManager}

object PersistenceUnits {

	trait PersistenceUnitRPM {
		self: RepositorySupport =>
		@PersistenceContext(unitName = "RPM")
		def setEntityManager(entityManager: EntityManager) {
			initializeEntityManager(entityManager)
		}
	}

}