package no.officenet.example.rpm.support.domain.service.repository

import javax.persistence.{PersistenceContext, EntityManager}
import no.officenet.example.rpm.support.infrastructure.jpa.RepositorySupport

object PersistenceUnits {

	trait PersistenceUnitRPM {
		self: RepositorySupport =>
		@PersistenceContext(unitName = "RPM")
		def setEntityManager(entityManager: EntityManager) {
			initializeEntityManager(entityManager)
		}
	}

	trait PersistenceUnitPETS {
		self: RepositorySupport =>
		@PersistenceContext(unitName = "PETS")
		def setEntityManager(entityManager: EntityManager) {
			initializeEntityManager(entityManager)
		}
	}
}